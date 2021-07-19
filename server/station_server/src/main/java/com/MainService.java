package com;

import com.errors.MainServiceViolation;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class MainService extends Service{
    private static MainService mainService = null;
    private static final Logger logger = Logger.getLogger(MainService.class);

    public MainService(@NotNull String name) {
        super(name);
        if(mainService != null){
            throw new MainServiceViolation("main service must have only one instance");
        }
        mainService = this;
    }

    protected void restartService(){
        logger.info("Restarting main service [" + getName() + "]");
        stopService();
        runService();
    }



}
