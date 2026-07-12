package com.coradio.rotation.application.exception;

import org.springframework.http.HttpStatus;

public record ErrorMessage(
        String status,
        String message
) {
    public ErrorMessage(HttpStatus status, String message) {
        this("%d: %s".formatted(status.value(), status.name()), message);
    }
}
