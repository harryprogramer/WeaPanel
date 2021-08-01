package com.scom;

import com.telemetry.TelemetryStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class SocketCore {
    public final ServerSocket sck_server;
    private boolean server_flag = false;
    private final Logger logger = Logger.getLogger(SocketCore.class.getName());
    private ArrayList<Class<? extends TelemetryStream>> tel_tasks;

    protected SocketCore(ServerSocket serverSocket){
        this.sck_server = serverSocket;
    }

    protected void startServer(){
        if(!server_flag){
            server_flag = true;
            serverLoop();
        }

    }

    private void serverLoop() {
        for(;;){
            if(!server_flag){
                logger.warn("Stopping client listening...");
                break;
            }
            try {
                new StationClient(sck_server.accept(), tel_tasks);
            } catch (IOException ioException) {
                logger.fatal("Client accept io error");
                ioException.printStackTrace();
            }
        }
    }

    public void registerTelemetryTasks(ArrayList<Class<? extends TelemetryStream>> tasks){
        this.tel_tasks = tasks;
    }

    protected void stopServer(){
        if(server_flag){
            server_flag = false;
        }
    }
}
