package com.coradio.rotation.infrastructure.exception;

public class ScrobbleProviderNotFoundException extends RuntimeException {
    public ScrobbleProviderNotFoundException(String message) {
        super(message);
    }
}
