package com;

import org.apache.log4j.Logger;

import java.util.ArrayList;

public class App {
    private static ArrayList<Service> services = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(App.class);


    public static void registerService(Service service){
        services.add(service);
    }

    public static void runApp(){
        logger.info("Starting up...");

        long millis = System.currentTimeMillis();
        for(Service service : services){
            service.runService();
        }

        logger.info("Done! in " + (System.currentTimeMillis() - millis) / 1000.0 + ".s");
    }
}
