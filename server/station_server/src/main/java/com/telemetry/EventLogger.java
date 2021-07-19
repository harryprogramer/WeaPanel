package com.telemetry;

import com.scom.StationClient;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;

public class EventLogger extends TelemetryStream {
    private static final Logger logger =  Logger.getLogger(EventLogger.class);

    public EventLogger() {
        super("Telemetry Event Logger");
    }


    @Override
    public void sessionStartAction(StationClient client) {
        logger.info("Session started, " + client.getUUID());
    }

    @Override
    public void packetSaveAction(TelemetryPacket telemetryPacket) {

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
