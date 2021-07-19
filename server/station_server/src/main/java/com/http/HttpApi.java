package com.http;
import com.Service;
import com.scom.SComService;
import com.telemetry.HardwareTelemetryPacket;
import com.telemetry.TelemetryPacket;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import static spark.Spark.*;


public class HttpApi extends Service {
    private static final Logger logger = Logger.getLogger(HttpApi.class);

    public HttpApi() {
        super("HTTP API");
    }


    private void startServer(){
        port(25740);
        initTraces();
        logger.info("HTTP server started");
    }

    private void initTraces(){
        get("/api/v2", (req, res) -> {
            if (req.requestMethod().equalsIgnoreCase("GET")) {
                JSONObject response = new JSONObject();

                TelemetryPacket telemetry = SComService.getTelemetry();
                HardwareTelemetryPacket hardwareTelemetry = SComService.getHardwareTelemetry();
                res.header("Content-Type", "application/json");
                if (telemetry != null && hardwareTelemetry != null && SComService.getStationConn() != null) {
                    res.header("message", "ok");
                    response.put("conn", "OK");
                    response.put("time", new JSONObject()
                            .put("y", telemetry.getTime().getYear())
                            .put("m", telemetry.getTime().getMonthValue())
                            .put("d", telemetry.getTime().getDayOfMonth())
                            .put("h", telemetry.getTime().getHour())
                            .put("mn", telemetry.getTime().getMinute())
                            .put("s", telemetry.getTime().getSecond())
                            .put("nn", telemetry.getTime().getNano()));
                    response.put("data", new JSONObject()
                            .put("temperature", telemetry.getTemperature())
                            .put("humidity", telemetry.getHumidity())
                            .put("sound", telemetry.getSoundLevel())
                            .put("MQ135", telemetry.getMQ135_ppm())
                            .put("MQ7", telemetry.getMQ7_ppm())
                            .put("MQ2", telemetry.getMQ3_ppm())
                            .put("pressure_hPa", telemetry.getPressure_hPa())
                            .put("light", telemetry.getLight_Lux())
                            .put("pressure_pa", telemetry.getPressure_pa()));
                    response.put("hardware", new JSONObject()
                    .put("dht", hardwareTelemetry.dht_Status())
                    .put("sound", hardwareTelemetry.soundMeter_Status())
                    .put("sdCard", hardwareTelemetry.sdCard_Status())
                    .put("firstBlock", hardwareTelemetry.firstBlock_Status())
                    .put("secondBlock", hardwareTelemetry.secondBlock_Status())
                    .put("MQ135", hardwareTelemetry.MQ135_Status())
                    .put("MQ7", hardwareTelemetry.MQ7_Status())
                    .put("MQ2", hardwareTelemetry.MQ2_Status())
                    .put("barometer", hardwareTelemetry.barometerStatus())
                    .put("memory", hardwareTelemetry.freeARMMemory())
                    .put("firstBlockVoltage", hardwareTelemetry.firstBlockVoltage())
                    .put("secondBlockVoltage", hardwareTelemetry.secondBlockVoltage()));
                } else {
                    if(SComService.getStationConn() != null){
                        res.header("message", "internal error");
                    }else {
                        res.header("message", "no connection");
                    }
                    response.put("conn", "error");
                    res.status(503);
                    return "";
                }
                res.status(200);
                return response.toString();
            }
            res.status(405);
            return "";
        });
    }

    private void stopServer(){
        stop();
    }

    @Override
    public void onRun() {
        startServer();
    }

    @Override
    public void onShutdown() {
        stopServer();
    }

    @Override
    public void mainTask() {
        initTraces();
    }
}
