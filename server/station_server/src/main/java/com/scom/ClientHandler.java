package com.scom;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;


public class ClientHandler {
    private final Socket sck;
    private BufferedReader in;
    private StationClient client;
    private static final Logger logger = Logger.getLogger(ClientHandler.class);



    protected ClientHandler(Socket socket) {
        this.sck = socket;
    }

    protected void startHandle(StationClient stationClient, BufferedReader in) throws IOException {
        client = stationClient;
        this.in = in;
        for(;;){
            String packet;
           try {
               packet = this.in.readLine();
           }catch (SocketException e){
               logger.warn("Lost connection with [" + client.getUUID() + "]", e);
               client.disconnect();
               break;
            }

            if(packet == null){
                logger.warn("Connection lost with [" + stationClient.getUUID() + "]");
                throw new IOException("connection closed");
            }
            PacketHandler.handlePacket(packet, client);
        }
    }
}
