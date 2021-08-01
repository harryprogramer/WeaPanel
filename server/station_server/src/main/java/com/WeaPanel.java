package com;

import com.http.HttpApi;
import com.scom.Server;
import com.telemetry.EventLogger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Properties;

public class WeaPanel {
    private static final Properties log4properties = new Properties();
    private static final Logger logger = Logger.getLogger(WeaPanel.class);

    private static void initLog(){
        log4properties.setProperty("log4j.rootLogger", "DEBUG, consoleAppender, fileAppender");
        log4properties.setProperty("log4j.appender.consoleAppender", "org.apache.log4j.ConsoleAppender");
        log4properties.setProperty("log4j.appender.consoleAppender.layout", "org.apache.log4j.PatternLayout");
        log4properties.setProperty("log4j.appender.consoleAppender.layout.ConversionPattern", "[ %-5p] [%d]%x - %m%n");
        log4properties.setProperty("log4j.appender.fileAppender", "org.apache.log4j.RollingFileAppender");
        log4properties.setProperty("log4j.appender.fileAppender.layout", "org.apache.log4j.PatternLayout");
        log4properties.setProperty("log4j.appender.fileAppender.layout.ConversionPattern", "[ %-5p] [%d] [%c] [%t]%x - %m%n");
        log4properties.setProperty("log4j.appender.fileAppender.File", "log/" + DateTimeFormatter.ofPattern("yyyy/MMM/dd/HH.mm.ss").format(LocalDateTime.now()) + ".log");
        PropertyConfigurator.configure(log4properties);
    }

    public static void main(String[] args) throws InterruptedException {
        initLog();
        Server server = new Server();
        HttpApi httpApi = new HttpApi();
        Server.addTelemetryTask(EventLogger.class);
        App.registerService(server);
        App.registerService(httpApi);
        App.runApp();
    }
}
