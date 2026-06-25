package com.coradio.rotation.application.exception;

public class TrackNotFoundException extends PlaybackPreparationException {
    public TrackNotFoundException(String message) {
        super("Track not found: " + message);
    }
}
