package com.telemetry;

import com.App;
import com.scom.StationClient;
import org.apache.log4j.Logger;
import org.slf4j.helpers.Util;
import util.GetTime;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class EventLogger extends TelemetryStream {
    private static final Logger logger =  Logger.getLogger(EventLogger.class);
    String fileName = "telemetry/" + GetTime.getTimeString(
            GetTime.TimeFormat.YEARS,
            GetTime.TimeFormat.MONTHS,
            GetTime.TimeFormat.DAYS,
            GetTime.TimeFormat.HOURS,
            GetTime.TimeFormat.MINUTES,
            GetTime.TimeFormat.SECONDS
    ) + ".tel";
    FileOutputStream trest = new FileOutputStream("telemetry/test.txt", false);
    FileOutputStream fileOutputStream;
    FileInputStream fileInputStream;

    private static final String Version = "1.0";
    private EventLoggerProtocol.Date getDate(LocalDateTime localDateTime){
        EventLoggerProtocol.Date.Builder date = EventLoggerProtocol.Date.newBuilder();
        date.setMillis(localDateTime.getNano());
        date.setSeconds(localDateTime.getSecond());
        date.setMinutes(localDateTime.getMinute());
        date.setHours(localDateTime.getHour());
        date.setDay(localDateTime.getDayOfMonth());
        date.setMonth(localDateTime.getMonth().getValue());
        date.setYear(localDateTime.getYear());

        return date.build();
    }

    public EventLogger() throws FileNotFoundException {
        super("Telemetry Event Logger");
    }
    @Override
    public void sessionStartAction(StationClient client) {
        EventLoggerProtocol.Telemetry.Builder telemetryFile = EventLoggerProtocol.Telemetry.newBuilder();
        EventLoggerProtocol.Header.Builder header = EventLoggerProtocol.Header.newBuilder();
        header.setStartTime(getDate(LocalDateTime.now()));
        header.setStationIp(client.getIp());
        header.setStationMac(client.getMac());
        header.setStationDns(client.getDns());
        header.setStationGateway(client.getGateway());
        header.setSysVersion(client.getSys_version());
        header.setStationUuid(client.getUUID());
        header.setServerVersion(App.ServerVersion);
        header.setTelemetryPluginVer(Version);
        telemetryFile.setHeader(header.build());
        try {
            fileOutputStream = new FileOutputStream(fileName, false);
            telemetryFile.build().writeDelimitedTo(fileOutputStream);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void packetSaveAction(TelemetryPacket telemetryPacket) {
        EventLoggerProtocol.Telemetry.Builder telemetryFile = EventLoggerProtocol.Telemetry.newBuilder();
        EventLoggerProtocol.TelemetryPacket.Builder telemetryBuilder = EventLoggerProtocol.TelemetryPacket.newBuilder();
        telemetryBuilder.setDate(getDate(LocalDateTime.now()));
        telemetryBuilder.setTemperature(telemetryPacket.getTemperature());
        telemetryBuilder.setHumidity((int) telemetryPacket.getHumidity());
        telemetryBuilder.setPressure(telemetryPacket.getPressure_pa());
        telemetryBuilder.setMq135(telemetryPacket.getMQ135_ppm());
        telemetryBuilder.setMq7(telemetryPacket.getMQ7_ppm());
        telemetryBuilder.setMq2(telemetryPacket.getMQ2_ppm());
        try {
            fileInputStream = new FileInputStream(fileName);
            telemetryFile.mergeDelimitedFrom(fileInputStream);
            fileOutputStream = new FileOutputStream(fileName, false);
            telemetryFile.addPacket(telemetryBuilder.build());
            telemetryFile.build().writeDelimitedTo(fileOutputStream);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sessionEndEvent(EndStatus endStatus, LocalDateTime session_end_time) {

    }

    @Override
    public void hardwareStatus_Action(HardwareTelemetryPacket data) {

    }

    @Override
    public void initTelemetryAction() {
        logger.info("Initializing " + getName());
    }
}
