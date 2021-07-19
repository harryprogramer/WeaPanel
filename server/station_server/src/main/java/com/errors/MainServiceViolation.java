package com.errors;

public class MainServiceViolation extends RuntimeException{
    public MainServiceViolation(String message){
        super(message);
    }
}
