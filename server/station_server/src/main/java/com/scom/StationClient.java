package com.scom;

import com.scom.protocol.PacketHandler;
import com.telemetry.TelemetryStream;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class StationClient extends ClientHandler{
    private static final Logger logger = Logger.getLogger(StationClient.class.getName());
    private final Socket client;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ArrayList<TelemetryStream> telemetries = new ArrayList<>();
    protected int session_id = 0;

    private String ip;
    private String mac;
    private String dns;
    private String gateway;
    private String remote_ip;
    private String subnet_mask;
    private int remote_port;
    private String station_uuid;
    private String sys_version;
    private String sd_type;
    private String fat_type;
    private String volume_size;
    private String free_space;
    private String used_space;
    private int station_id;


    protected StationClient(Socket socket, ArrayList<Class<? extends TelemetryStream>> telemetries) throws IOException {
        super(socket);
        this.client = socket;
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        logger.info("Connection from [" + socket.getInetAddress().toString() + "]");
        new Thread(this::performHandshake).start();
        for(Class<? extends TelemetryStream> telemetry : telemetries){
            try {
                this.telemetries.add((TelemetryStream) Class.forName(telemetry.getName()).getDeclaredConstructor().newInstance());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void performSession(){
        try {
            logger.info("Starting session with [" + getUUID() + "]");
            client.setSoTimeout(15000);
            try {
                String data = in.readLine();
                if(data == null){
                    logger.warn("Connection unexpected lost with [" + getUUID() + "], session failed");
                    disconnect();
                }else {
                    logger.info(data);
                    if (!PacketHandler.handleSession(data, this)) {
                        logger.warn("Session with " + getUUID() + " failed");
                        disconnect();
                        return;
                    }
                }
            }catch (SocketTimeoutException ignored){
                logger.warn("Session with " + getUUID() + " is timeout");
                disconnect();
                return;
            }
            logger.info("Session started with id " + session_id + " uuid: " + getUUID());
            for(TelemetryStream telemetryStream : telemetries){
                telemetryStream.initTelemetry(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Failed to start session with [" + getUUID() + "]");
            return;
        }
        try {
            startHandle(this, in);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            logger.warn("IO " + getUUID() + " error");
            disconnect();
        }
    }

    private void performHandshake(){
        try {
            client.setSoTimeout(15000);
            String handshake;
            try {
                logger.info("Waiting for handshake from [" + getUUID() + "]");
                handshake = in.readLine();
                if(handshake.equalsIgnoreCase("ping")){
                    Thread.sleep(200);
                    out.println("pong");
                    logger.info("Handshake with [" + getUUID() + "] succesful");
                    client.setSoTimeout(0);
                    performSession();
                }else {
                    logger.warn("Handshake error with [" + getUUID() + "]");
                    disconnect();
                }
            }catch (SocketTimeoutException e){
                logger.warn("Station [" + client.getInetAddress().toString() + "] is timeout handshake");
                disconnect();
            }
        } catch (Exception ioException) {
            ioException.printStackTrace();
            logger.warn("IO error while trying start handle packets, station [" + client.getInetAddress().toString() + "], " + ioException.getMessage());
            disconnect();
        }
    }


    protected ArrayList<TelemetryStream> getTelemetries(){
        return telemetries;
    }

    public synchronized void disconnect(){
        logger.warn("Disconnecting station " + getUUID());
        if(client.isConnected()){
            try {
                client.close();
            } catch (IOException ioException) {
                logger.warn(String.format("Failed to close station connection id: [%s], cause: [%s]", station_uuid, ioException.getMessage()));
                ioException.printStackTrace();
            }
        }
        if(Server.getStationConn() != null
                && Server.getStationConn().getUUID().
                equalsIgnoreCase(getUUID())) {
            Server.disconnectActiveStation();
        }
    }

    public boolean isConnected(){
        return client.isConnected();
    }


    public synchronized void sendRawMsg(Object msg){
        if(!client.isConnected()){
            logger.warn("Connection with station [" + getUUID() +  " is lost");
            disconnect();
        }
        out.println(msg.toString());
    }

    public void sendJSONPacket(JSONObject jsonObject){
        if(!client.isConnected()){
            logger.warn("Packet [" + jsonObject.getInt("id") + "] can't be send, because station is not connected");
            disconnect();
        }
        sendRawMsg(jsonObject);
    }

    public String getUUID(){
        if(station_uuid == null || station_uuid.length() == 0){
            return client.getInetAddress().toString();
        }
        return station_uuid;
    }

    public String getIp() {
        return ip;
    }

    public String getGateway() {
        return gateway;
    }

    public int getRemote_port() {
        return remote_port;
    }

    public int getStation_id() {
        return station_id;
    }

    public String getDns() {
        return dns;
    }

    public String getMac() {
        return mac;
    }

    public String getSubnet_mask() {
        return subnet_mask;
    }

    public String getSys_version() {
        return sys_version;
    }

    public String getRemote_ip() {
        return remote_ip;
    }

}
