package com.scom;

import com.Service;
import com.errors.StationRegistrationException;
import com.telemetry.HardwareTelemetryPacket;
import com.telemetry.TelemetryPacket;
import com.telemetry.TelemetryStream;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("unused")
public class SComService extends Service {
    private static volatile int sock_port = 25760;
    private static final Logger logger = Logger.getLogger(SComService.class);
    private static StationClient conn = null;
    private static ServerSocket sck_server;
    private static SocketCore socketCore;
    private static final ArrayList<TelemetryStream> TELEMETRY___STREAM___TASKS = new ArrayList<>();
    private static final Object lock = new Object();
    private static volatile TelemetryPacket lastPacket = null;
    private static volatile HardwareTelemetryPacket hardwareTelemetryPacket = null;
    public SComService(){
        super("SCOM service");

    }

    public void changePort(int new_port){
        sock_port = new_port;
    }

    public void startServer(){
        synchronized (lock) {
            if (Objects.isNull(sck_server)) {
                try {
                    sck_server = new ServerSocket(sock_port);
                    socketCore = new SocketCore(sck_server);
                    socketCore.registerTelemetryTask(TELEMETRY___STREAM___TASKS.toArray(new TelemetryStream[0]));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    public void stopServer(){
        synchronized (lock) {
            if (!Objects.isNull(sck_server) && !sck_server.isClosed()) {
                logger.warn("Stopping SCOM service...");
                sck_server = null;
                if (!Objects.isNull(conn)) {
                    conn.disconnect();
                }
            }
        }
    }

     protected synchronized static void setActiveStation(@NotNull StationClient stationClient) throws StationRegistrationException{
        if (Objects.isNull(conn)) {
            conn = stationClient;
        } else {
            throw new StationRegistrationException("Another station [" + conn.getUUID() + "] is already connect to server");
        }

    }

    protected synchronized static void disconnectActiveStation(){
        if (!Objects.isNull(conn)) {
            logger.warn("Disconnecting the active station [" + conn.getUUID() + "]");
            conn = null;
        }

    }

    protected static void saveTelemetryPacket(TelemetryPacket data, @NotNull StationClient stationClient){
        if(getStationConn() != null) {
            if (stationClient.getUUID().equalsIgnoreCase(getStationConn().getUUID())) {
                lastPacket = data;
                return;
            }
        }

        throw new IllegalStateException("station is not connected or not valid instance");
    }

    protected static void updateHardwareTelemetry(@NotNull HardwareTelemetryPacket telemetryPacket){
        hardwareTelemetryPacket = telemetryPacket;
        // TODO system alarmowania podczas awarii
    }

    public static @Nullable TelemetryPacket getTelemetry(){
        return lastPacket;
    }

    public static @Nullable HardwareTelemetryPacket getHardwareTelemetry(){
        return hardwareTelemetryPacket;
    }

    public static void addTelemetryTask(TelemetryStream task){
        TELEMETRY___STREAM___TASKS.add(task);
    }

    /**
        @return active station instance
     */
    public static @Nullable StationClient getStationConn(){
        return conn;
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
        socketCore.startServer();
    }
}
