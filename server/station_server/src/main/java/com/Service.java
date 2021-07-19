package com;


import com.errors.ServiceNotInitYetException;
import com.errors.ServiceRuntimeException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class Service implements ServiceInterface {
    private final String name;
    private Thread thread;
    private boolean isRun = false;
    private static final Logger logger = Logger.getLogger(Service.class);

    public Service(@NotNull String name) {
        this.name = name;
    }

    protected void runService(){
        if(!isRun && thread == null) {
            logger.info("Starting [" + getName() + "] service...");
            onRun();
            thread = new Thread(this::mainTask);
            thread.start();
            isRun = true;
        }else {
            throw new ServiceRuntimeException("service is already initialized");

        }
    }

    protected void stopService() {
        if (isRun && !(thread == null)) {
            logger.info("Shutting down service [" + getName() + "]");
            onShutdown();
            logger.info("Waiting for [" + getName() + "] to finish thread...");
            long millis = System.currentTimeMillis();
            while(thread.isAlive()){
                if(System.currentTimeMillis() - millis > 10000){
                    logger.info("Service [" + getName() + "] timeout, killing thread");
                    thread.interrupt();
                    break;
                }
            }
            isRun = false;
        }
    }

    protected Thread getThread(){
        if(thread == null){
            throw new ServiceNotInitYetException();
        }
        return thread;
    }

    public String getName() {
        return name;
    }
}
