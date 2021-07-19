package com.errors;

public class ServiceNotInitYetException extends RuntimeException{
    public ServiceNotInitYetException(){
        super("service not run or not initialized yet");
    }
}
