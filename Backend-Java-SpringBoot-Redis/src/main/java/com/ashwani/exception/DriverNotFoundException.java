package com.ashwani.exception;

public class DriverNotFoundException extends RuntimeException{
    public DriverNotFoundException(String message) {
        super(message);
    }
}
