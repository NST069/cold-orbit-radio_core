package com.coradio.rotation.application.exception;

public class FileDownloadingException extends PlaybackPreparationException {
    public FileDownloadingException(String message) {
        super("Failed to download file: " + message);
    }
}
