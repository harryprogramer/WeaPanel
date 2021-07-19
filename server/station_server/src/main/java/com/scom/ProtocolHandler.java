package com.scom;

import com.errors.StationRegistrationException;
import com.telemetry.HardwareTelemetryPacket;
import com.telemetry.TelemetryStream;
import com.telemetry.TelemetryPacket;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class ProtocolHandler {
    private static final int TIME_SYNC = 2;
    private static final int TELEMETRY_PACKET = 3;
    private static final int PROBE_REPORT = 4;

    private static final Logger logger = Logger.getLogger(ProtocolHandler.class);

    protected static void handleID(int packetFlag, JSONObject packet_json, @NotNull StationClient client) {
        try {
            JSONObject body = packet_json.getJSONObject("body");
            switch (packetFlag) {
                case TIME_SYNC: {

                }

                case TELEMETRY_PACKET: {
                    float temperature = body.getFloat("temperature");
                    float humidity = body.getFloat("humidity");
                    int sound = body.getInt("sound");
                    int MQ135 = body.getInt("mq135");
                    int MQ7 = body.getInt("mq7");
                    int MQ3 = body.getInt("mq2");
                    int pressure_hPa = body.getInt("pressure_hpa");
                    int pressure_pa = body.getInt("pressure_pa");
                    float light_lux = body.getFloat("light");
                    TelemetryPacket telemetryPacket = new TelemetryPacket.Builder(temperature, humidity,
                            sound, MQ135, MQ7, MQ3, pressure_hPa, pressure_pa, light_lux);

                    for (TelemetryStream telemetryStream : client.getTelemetries()) {
                        telemetryStream.savePacket(telemetryPacket);
                    }

                    SComService.saveTelemetryPacket(telemetryPacket, client);
                    break;
                }

                case PROBE_REPORT: {
                    JSONObject status =     body.getJSONObject("status");
                    int memory =            body.getInt("memory");
                    int firstBlockV =       body.getJSONObject("values").getInt("first_block");
                    int secondBlockV =      body.getJSONObject("values").getInt("second_block");
                    boolean dht =           status.getBoolean("dht");
                    boolean sdCard =        status.getBoolean("sdcard");
                    boolean firstBlock =    status.getBoolean("first_block");
                    boolean secondBlock =   status.getBoolean("second_block");
                    boolean soundMeter =    status.getBoolean("sound_meter");
                    boolean MQ135 =         status.getBoolean("mq135");
                    boolean MQ7 =           status.getBoolean("mq7");
                    boolean MQ2 =           status.getBoolean("mq2");
                    boolean barometer =     status.getBoolean("bmp");
                    boolean GY49 =          status.getBoolean("gy-49");

                    HardwareTelemetryPacket hardw_telemetry = new HardwareTelemetryPacket.Builder(
                            dht, soundMeter, sdCard,
                            firstBlock, secondBlock, MQ135,
                            MQ7, MQ2, barometer,GY49, memory,
                            firstBlockV, secondBlockV);
                    SComService.updateHardwareTelemetry(hardw_telemetry);
                    break;

                }

                default: {
                    // TODO
                }

            }

        } catch (JSONException e) {
            logger.warn("Invalid json signature, " + e.getMessage() + " from " + client.getUUID());
        }
    }
}
