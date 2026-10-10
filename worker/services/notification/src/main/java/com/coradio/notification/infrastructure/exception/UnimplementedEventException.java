package com.coradio.notification.infrastructure.exception;

public class UnimplementedEventException extends RuntimeException {
    public UnimplementedEventException(String eventName) {
        super("Event " + eventName + " is not implemented yet");
    }
}
