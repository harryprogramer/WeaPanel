package com.telemetry;

import com.errors.TelemetryInitializationError;
import com.scom.StationClient;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

interface TelemetryInterface {
    void sessionStartAction(StationClient client);

    void packetSaveAction(TelemetryPacket telemetryPacket);

    void sessionEndEvent(TelemetryStream.EndStatus endStatus, LocalDateTime session_end_time);

    void hardwareStatus_Action(HardwareTelemetryPacket data);

    void initTelemetryAction();
}

public abstract class TelemetryStream implements TelemetryInterface {
    enum Status {
        NOT_INITIALIZED,
        SAVING,
        FINISHED,
        ;
    }
    enum EndStatus {
        SESSION_END,
        CONNECTION_LOST
        ;
    }
    private static final Logger logger = Logger.getLogger(TelemetryStream.class);
    private Status status = Status.NOT_INITIALIZED;
    private StationClient client;
    private final String name;

    public TelemetryStream(@NotNull String name){
        this.name = name;
    }

    public final void initTelemetry(StationClient client){
        this.client = client;
        logger.info("Starting telemetry for [" + client.getUUID() + "]");
        initTelemetryAction();
        status = Status.SAVING;
        sessionStartAction(client);
    }

    public final synchronized void savePacket(TelemetryPacket telemetryPacket){
        if(status != Status.SAVING) {
            throw new TelemetryInitializationError("telemetry " + getName() + " is not initialized");
        }
         new Thread(() ->
                 packetSaveAction(telemetryPacket)
         ).start();
    }

    public final void stopTelemetry(EndStatus endStatus){
        LocalDateTime now = LocalDateTime.now();
        logger.info("Stopping telemetry for [" + client.getUUID() + "] status: " + endStatus.toString());
        status = Status.FINISHED;
        sessionEndEvent(endStatus, now);
    }

    public final Status getTelemetryStatus(){
        return status;
    }

    public String getName(){
        return name;
    }
}
