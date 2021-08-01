package com.scom.protocol;

import com.errors.StationRegistrationException;
import com.scom.Server;
import com.scom.StationClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class PacketHandler {
    private static final Logger logger = Logger.getLogger(PacketHandler.class);

    private static final int SESSION_REQUEST = 1;

    protected static void handlePacket(String json, StationClient stationClient){
        int id;

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            id = jsonObject.getInt("id");
            ProtocolHandler.handleID(id, jsonObject, stationClient);
        }catch (JSONException e) {
            logger.warn("Invalid JSON signature from " + stationClient.getUUID());
            jsonObject = new JSONObject();
            jsonObject.put("status", "error");
            jsonObject.put("body", new JSONObject().put("message", e.getMessage()));
        }
    }

    public static boolean handleSession(String json, StationClient client) {
        try {
            JSONObject packet_json = new JSONObject(json);

            JSONObject network = packet_json.getJSONObject("body").getJSONObject("network");
            client.station_uuid = packet_json.getJSONObject("body").getString("uuid");
            client.ip = network.getString("ip");
            client.mac = network.getString("mac");
            client.dns = network.getString("dns");
            client.gateway = network.getString("gateway");
            client.subnet_mask = network.getString("subnet_mask");
            client.remote_ip = network.getString("remote_ip");
            client.remote_port = network.getInt("remote_port");
            client.sys_version = packet_json.getJSONObject("body").getString("sys_version");

            try {
                Server.setActiveStation(client);
                JSONObject response = new JSONObject();
                response.put("status", "OK");
                response.put("id", SESSION_REQUEST);
                client.sendJSONPacket(response);
                return true;
            } catch (StationRegistrationException e) {
                if (!Objects.isNull(Server.getStationConn())) {
                    logger.warn("Another station " + Server.getStationConn().getUUID() + " is already connected");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("status", "failed");
                    jsonObject.put("id", SESSION_REQUEST);
                    jsonObject.put("body", new JSONObject().put("message", "another station is connected"));
                    client.sendJSONPacket(jsonObject);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("status", "failed");
                    jsonObject.put("id", SESSION_REQUEST);
                    jsonObject.put("body", new JSONObject().put("message", "unknown error, " + e.getMessage()));
                    client.sendJSONPacket(jsonObject);
                }
            }
        }catch (JSONException e){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "failed");
            jsonObject.put("id", SESSION_REQUEST);
            jsonObject.put("body", new JSONObject().put("message", "json syntax error, " + e.getMessage()));
            client.sendJSONPacket(jsonObject);
            logger.fatal(e.getMessage());
        }

        return false;
    }
}
