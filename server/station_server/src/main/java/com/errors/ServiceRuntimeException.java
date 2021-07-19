package com.errors;

public class ServiceRuntimeException extends RuntimeException{
    public ServiceRuntimeException(String message){
        super(message);
    }
}
