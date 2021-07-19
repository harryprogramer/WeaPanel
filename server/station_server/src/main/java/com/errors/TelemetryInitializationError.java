package com.errors;

public class TelemetryInitializationError extends RuntimeException{
    public TelemetryInitializationError(String message){
        super(message);
    }
}
