#include <Ethernet.h>
#include <Adafruit_BMP085.h>
#include <virtuabotixRTC.h>
#include <ArduinoJson.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <StreamUtils.h>
#include <DHT.h>
#include <Wire.h>
#include <SdFat.h>

#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 32 // OLED display height, in pixels


#define TCAADDR 0x70
#define SYSVERSION 11
#define STATIONUUID "49c06063-dd23-42ed-92ca-762ccbdc7e79"
#define DHT_PIN 29
#define DHT_TYPE DHT11
#define _sock_recv_timeout 10000
#define _server_port 25760
#define _JSONdoc_buf 512
#define _sck_recv_timeout 10000
#define _sck_ack_timeout 10000

#define PACKETS_FREQUENCY 1500

#define MAX_ADDR 0x4A


#define _first_voltage_block A8
#define _second_voltage_block A9
#define MQ135_PIN A4
#define MQ7_PIN A5
#define MQ2_PIN A7

#define BMP_I2C_CHANNEL 1
#define DISPLAY_I2C_CHANNEL 3

#define SD_CS_PIN 53

typedef unsigned char BYTE;
typedef void VOID;

bool isTimeSync = false;

static DHT dht(DHT_PIN, DHT_TYPE);
const static uint8_t _rtc_clock_pins[] = {50, 49, 48};
static virtuabotixRTC clock(_rtc_clock_pins[0], _rtc_clock_pins[1], _rtc_clock_pins[2]);
static Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);
static EthernetClient client;
static Adafruit_BMP085 bmp;
static SdFat sd;
static File logFile;
const static IPAddress _server_ip(192, 168, 0, 13);
const static byte mac[] = {
  0x00,
  0xAA,
  0xBB,
  0xCC,
  0xDE,
  0x02
};

typedef enum _LOGType {
  INFO,
  WARN,
  CRIT
} _LOGType;

typedef struct HardwareState {
  bool dht;
  bool sdcard;
  bool firstBlock;
  bool secondBlock;
  bool mq135;
  bool mq7;
  bool mq2;
  bool bmp;
  bool oledDisplay;
} HardwareState;

typedef struct Telemetry {
  float temperature;
  uint8_t humidity;
  uint16_t sound;
  uint16_t mq135;
  uint16_t mq7;
  uint16_t mq2;
  uint32_t pressure_pa;
  uint16_t pressure_hPa;
  uint32_t light;
} Telemetry;

typedef struct basic_request {
  uint8_t id;
  StaticJsonDocument < _JSONdoc_buf > json_body;
  const unsigned long millis_time = millis();
}
basic_request;

#ifdef __arm__
extern "C" char* sbrk(int incr);
#else  // __ARM__
extern char *__brkval;
#endif  // __arm__

int freeMemory() {
  char top;
#ifdef __arm__
  return &top - reinterpret_cast<char*>(sbrk(0));
#elif defined(CORE_TEENSY) || (ARDUINO > 103 && ARDUINO != 151)
  return &top - __brkval;
#else  // __arm__
  return __brkval ? &top - __brkval : &top - __malloc_heap_start;
#endif  // __arm__
}

void TCA9548A(uint8_t i) {
  Wire.beginTransmission(TCAADDR);
  Wire.write(1 << i);
  Wire.endTransmission();
}

namespace Sensors {
  namespace GY49 {
    int begin()
    {
      Wire.beginTransmission(MAX_ADDR);
      Wire.write(0x02);
      Wire.write(0x40);
      return Wire.endTransmission();
    }

    float get_lux(void)
    {
      unsigned int data[2];
      Wire.beginTransmission(MAX_ADDR);
      Wire.write(0x03);
      Wire.endTransmission();

      Wire.requestFrom(MAX_ADDR, 2);

      if (Wire.available() == 2)
      {
          data[0] = Wire.read();
          data[1] = Wire.read();
      }
    
      int exponent = (data[0] & 0xF0) >> 4;
      int mantissa = ((data[0] & 0x0F) << 4) | (data[1] & 0x0F);

      float luminance = (float)(((0x00000001 << exponent) * (float)mantissa) * 0.045);
      
      return luminance; 
    }

  };

  float getTemperature(){
    TCA9548A(BMP_I2C_CHANNEL);
    return bmp.readTemperature();
  }

  uint8_t getHumidity(){
    return dht.readHumidity();
  }

  uint16_t getSoundLevel(){
    return 0;
  }

  uint16_t getMQ135_Level(){
    return analogRead(MQ135_PIN);
  }

  uint16_t getMQ7_Level(){
    return analogRead(MQ7_PIN);
  }

  uint16_t getMQ2_Level(){
    return analogRead(MQ2_PIN);
  }

  uint32_t getPressure_Pa(){
    TCA9548A(BMP_I2C_CHANNEL);
    return bmp.readPressure();
  }

  float getPressure_hPa(){
    return getPressure_Pa() / 100.0;
  }
};

void displayPrint(String& text){
  TCA9548A(DISPLAY_I2C_CHANNEL);
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 10);
  display.println(text);
  display.display(); 
}

void displayPrint(char* text){
  TCA9548A(DISPLAY_I2C_CHANNEL);
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 10);
  display.println(text);
  display.display(); 
}

float inline readAnalogVoltage(uint8_t pin){
  return analogRead(pin) * (5.0 / 1023.0);
}

static char * ipToCharArray(const IPAddress & ip) {
  static char ip_chars[16];
  sprintf(ip_chars, "%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
  return ip_chars;
}

static String getRTCTime(void)
{
  clock.updateTime();
  String time = String(clock.year) + "." + String(clock.month) + "." + String(clock.dayofmonth) +
      " " + String(clock.hours) + ":" + String(clock.minutes) + "." + String(clock.seconds);

  if(!isTimeSync){
    return "tns " + time;
  }
  return time;
}

static VOID sys_log(const _LOGType logtype, const String &message)
{
  char str_type[7];
  switch(logtype){
    case INFO: 
      strcpy(str_type, "[INFO]"); break;
    
    case WARN: 
      strcpy(str_type, "[WARN]"); break;
    
    case CRIT: 
      strcpy(str_type, "[CRIT]");
    
  }
  String msg = "[" +  getRTCTime() +  "] [" + millis() + "] " + str_type + " " + message; 
  if(Serial)
    Serial.print("\n" + msg);

  if(logFile.isOpen()){
    logFile.println(msg);
    logFile.flush();
  }
}

void checkEthLinkStatus(){
  if(Ethernet.linkStatus() == LinkOFF){
    displayPrint("Eth not plugged in");
    sys_log(CRIT, "[eth_link] Brak kabla sieciowego, oczekiwanie na polaczenie...");

    while(Ethernet.linkStatus() == LinkOFF);
    sys_log(INFO, "[eth_link] Kabel sieciowy podlaczony, wznawianie");
    displayPrint("    Reconnecting...");
    startSession();
  }
}

void DHCP_Configuration() {
  client.setConnectionTimeout(_sck_ack_timeout);
   displayPrint("  Configurating DHCP");
  for(;;){
    sys_log(INFO, "[dhcp_service] Konfiguracja za pomoca DHCP w toku");
    if (Ethernet.begin((uint8_t * ) mac) == 0) {
      continue;
    }
    sys_log(INFO, "[dhcp_service] Konfiguracja DHCP poprawna");
    return;
  }
}


/**
0x1 - timeout
0x2 - ok
*/

static BYTE _recvraw_sck(uint8_t ** recv, uint16_t timeout) {
  checkEthLinkStatus();
  if (client.connected()) {
    unsigned long start_time = millis();
    while (1) {
      int bytes = client.available();
      if (bytes == 0) {
        if (millis() - start_time > _sock_recv_timeout) {
          return 0x1;
        }
        continue;
      };

      uint8_t * buf = (uint8_t * ) malloc(bytes * sizeof(uint8_t));
      if (buf == nullptr) {
        sys_log(CRIT, "Alokacja pamieci nie udana");
        for (;;);
      }
      client.read(buf, bytes);
      buf[bytes] = '\0';
      * recv = buf;
      return 0x2;
    }
  }
}

static BYTE _send_request(basic_request& request, uint8_t ** recv, bool waitForData) {
  checkEthLinkStatus();

  if(client.connected()){
    StaticJsonDocument < _JSONdoc_buf > content;
    content["id"] = request.id;
    content["millis"] = request.millis_time;
    content["rtctime"] = getRTCTime();
    content["body"] = request.json_body;

    WriteBufferingStream bufferedJSON(client, _JSONdoc_buf);
    serializeJson(content, bufferedJSON);
    bufferedJSON.flush();
    client.print("\r\n");

    if (waitForData) {
      return _recvraw_sck(recv, _sck_recv_timeout);
    }

    return 0x2;
  }
  return 0x0;
}

static HardwareState& hardwareReport(bool _isPrint){
  HardwareState hardware_state;

  String displayText = "";

  hardware_state.dht = isnan(dht.readTemperature()) || isnan(dht.readHumidity()) || isnan(dht.readTemperature(true));
  if(hardware_state.dht){
    sys_log(WARN, "[diagnostic_service] Awaria sensora DHT");
    displayText = "Some hardware issues\n       DHT";
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Sensor DHT w normie");
    }
  }

  hardware_state.sdcard = false; // TODO

  hardware_state.firstBlock = !readAnalogVoltage(_first_voltage_block) >= 5;

  if(hardware_state.firstBlock){
    sys_log(WARN, "[diagnostic_service] Nieprawidlowe napiecie w bloku pierwszym");
    if(displayText.length() == 0){
      displayText = "Some hardware issues\n      Voltage";
    }else {
      displayText = displayText + " and more";
    }
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Pierwszy blok zasilania w normie");
    }
  }

  hardware_state.secondBlock = !readAnalogVoltage(_second_voltage_block) >= 5;

  if(hardware_state.secondBlock){
    sys_log(WARN, "[diagnostic_service] Nieprawidlowe napiecie w bloku drugim");
    if(displayText.length() == 0){
      displayText = "Some hardware issues\n      Voltage";
    }else {
      displayText = displayText + " and more";
    }
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Drugi blok zasilania w normie");
    }
  }

  hardware_state.mq135 = !readAnalogVoltage(MQ135_PIN) > 50;
  if(hardware_state.mq135){
    sys_log(WARN, "[diagnostic_service] Awaria sensora MQ135");
    if(displayText.length() == 0){
      displayText = "Some hardware issues\n      MQ135";
    }else {
      displayText = displayText + " and more";
    }
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Sensor MQ135 w normie");
    }
  }

  hardware_state.mq7 = !readAnalogVoltage(MQ7_PIN) > 50;
  
  if(hardware_state.mq7){
    sys_log(WARN, "[diagnostic_service] Awaria sensora MQ7");
    if(displayText.length() == 0){
      displayText = "Some hardware issues\n      MQ7";
    }else {
      displayText = displayText + " and more";
    }
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Sensor MQ7 w normie");
    }
  }

   hardware_state.mq2 = !readAnalogVoltage(MQ2_PIN) > 50;
  
  if(hardware_state.mq2){
    sys_log(WARN, "[diagnostic_service] Awaria sensora MQ2");
    if(displayText.length() == 0){
      displayText = "Some hardware issues\n      MQ2";
    }else {
      displayText = displayText + " and more";
    }
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Sensor MQ2 w normie");
    }
  }

  TCA9548A(BMP_I2C_CHANNEL);
  
  hardware_state.bmp = !bmp.begin();

  if(hardware_state.bmp){
    sys_log(WARN, "[diagnostic_service] Awaria barometru BMP");
    if(displayText.length() == 0){
      displayText = "Some hardware issues\n      BMP";
    }else {
      displayText = displayText + " and more";
    }
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Barometr BMP w normie");
    }
  }

  TCA9548A(DISPLAY_I2C_CHANNEL);
  hardware_state.oledDisplay = !display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  if(hardware_state.oledDisplay){
    sys_log(WARN, "[diagnostic_service] Awaria ekranu OLED");
  }else {
    if(_isPrint){
      sys_log(INFO, "[diagnostic_service] Ekran statusowy w normie");
    }
  }


  return hardware_state;
} 

static void startSession(void){
  if (client.connected()) {
    client.stop();
  }
  displayPrint("  Connecting..");
  sys_log(INFO, "[sessionDriver] Laczenie z serwerem...");
  for(;;){
    if (!client.connect(_server_ip, _server_port)) {
      delay(2000);
      continue;
    }
    sys_log(INFO, "[sessionDriver] Serwer dostepny, rozpoczynanie sesji..."); 
    client.println("ping");
    uint8_t* handshake_buf;
    BYTE handshakeStatus = _recvraw_sck(&handshake_buf, _sock_recv_timeout);
    if(handshakeStatus == 0x1){
      sys_log(WARN, "[sessionDriver] Uscisk dloni nie udany, przekroczono limit odpowiedzi");
      client.stop();
      continue;
    }else if(handshakeStatus = 0x2){
      if(strcmp((char*) handshake_buf, "pong\r\n") == 0){
        sys_log(INFO, "[sessionDriver] Uscisk dloni udany");
        free(handshake_buf);
      }else {
        sys_log(WARN, "[sessionDriver] Uscisk dloni nie udany, nieznany protokol, [" + String((char*) handshake_buf) + "\r]");
        free(handshake_buf);
        continue;
      }
    }else {
      sys_log(CRIT, "[sessionDriver] Nieznany blad");
    }
    StaticJsonDocument<_JSONdoc_buf> doc;
    basic_request request;
    request.id = 1;

    doc["sys_version"] =      SYSVERSION;
    doc["uuid"] =             STATIONUUID;
    doc["network"]["ip"] =          ipToCharArray(Ethernet.localIP());
    doc["network"]["mac"] =              "";
    doc["network"]["dns"] =              ipToCharArray(Ethernet.dnsServerIP());
    doc["network"]["gateway"] =          ipToCharArray(Ethernet.gatewayIP());
    doc["network"]["subnet_mask"] =      ipToCharArray(Ethernet.subnetMask());
    doc["network"]["remote_ip"] =        ipToCharArray(client.remoteIP());
    doc["network"]["remote_port"] =      client.remotePort();

    request.json_body = doc;
    uint8_t* buf;
    BYTE sessionStatus = _send_request(request, &buf, true);
    StaticJsonDocument<_JSONdoc_buf> response;
    if(sessionStatus == 0x2){
      deserializeJson(response, (char*) buf);
      if(strcmp((char*) response["status"], "OK") == 0){
        sys_log(INFO, "[sessionDriver] Sesja rozpoczeta");
        displayPrint("     Connected");
        free(buf);
        return;
      }else {
        sys_log(WARN, (String) "[sessionDriver] Serwer odmowil prosbe o rozpoczecie sesji, " + (char*) response["status"]);
        sys_log(WARN, (String) "[sessionDriver] Wiadomosc odmowienia sesji: " + String((char*) response["body"]["message"]));
        client.stop();
        free(buf);
        continue;
      }
    }else {
      sys_log(WARN, "[sessionDriver] Serwer przekroczyl czas odpowiedzi");
      client.stop();
      continue;
    }
  }
}

static Telemetry getTelemetry(void){
  Telemetry telemetry;
  telemetry.temperature = Sensors::getTemperature();
  telemetry.humidity = Sensors::getHumidity();
  telemetry.sound = 0; //TODO
  telemetry.mq135 = Sensors::getMQ135_Level();
  telemetry.mq7 =   Sensors::getMQ7_Level();
  telemetry.mq2 = Sensors::getMQ2_Level();
  telemetry.pressure_pa = Sensors::getPressure_Pa();
  telemetry.pressure_hPa = Sensors::getPressure_hPa();
  telemetry.light = 0; //TODO

  return telemetry;
}

void sendTelemetryPacket(){
  StaticJsonDocument<_JSONdoc_buf> doc;
  Telemetry telemetry;
  telemetry = getTelemetry();
  basic_request request;
  request.id = 3;
  doc["temperature"] =    telemetry.temperature;
  doc["humidity"] =       telemetry.humidity;
  doc["sound"] =          telemetry.sound;
  doc["mq135"] =          telemetry.mq135;
  doc["mq7"] =            telemetry.mq7;
  doc["mq2"] =            telemetry.mq2;
  doc["pressure_hpa"] =   telemetry.pressure_hPa;
  doc["pressure_pa"] =    telemetry.pressure_pa;
  doc["light"] =          telemetry.light;

  request.json_body = doc;

  _send_request(request, NULL, false);

}

void sendHardwareReport(void){
  StaticJsonDocument<_JSONdoc_buf> doc;
  HardwareState hardware_state = hardwareReport(false);
  basic_request request;
  request.id = 4;
  doc["memory"] = freeMemory();
  doc["status"]["dht"] = hardware_state.dht;
  doc["status"]["sound_meter"] = false; // TODO
  doc["status"]["sdcard"] = false; // TODO
  doc["status"]["first_block"] = hardware_state.firstBlock;
  doc["status"]["second_block"] = hardware_state.secondBlock;
  doc["status"]["mq135"] =        hardware_state.mq135;
  doc["status"]["mq7"] =          hardware_state.mq7;
  doc["status"]["mq2"] =          hardware_state.mq2;
  doc["status"]["bmp"] =          hardware_state.bmp;
  doc["status"]["gy-49"] =        true; // TODO
  doc["values"]["first_block"] =  readAnalogVoltage(_first_voltage_block);
  doc["values"]["second_block"] = readAnalogVoltage(_second_voltage_block);

  request.json_body = doc;

  _send_request(request, NULL, false);

}

void sendPackets(){
  if(client.connected()){
    sendTelemetryPacket();
    sendHardwareReport();
  }else {
    sys_log(CRIT, "[packets_handler] Utracono polaczenie z centrami danych");
    displayPrint("Connection lost");
    checkEthLinkStatus();
    startSession();
  }
}

void setup() {
  if (!sd.begin(SD_CS_PIN)) {
    Serial.println("SD card not detected");
  }
  logFile = sd.open("log.txt", O_RDWR | O_AT_END | O_CREAT );
  sys_log(INFO, "Uruchamianie w toku...");
  TCA9548A(DISPLAY_I2C_CHANNEL);
  display.begin();
  dht.begin();
  displayPrint("   Starting up...");
  Serial.begin(115200);
  hardwareReport(true);
  DHCP_Configuration();
  startSession();
}

void loop() {
  delay(PACKETS_FREQUENCY);
  sendPackets();
}
