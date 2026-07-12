package com.coradio.rotation.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getBindingResult()
                .getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", ")));
    }

    @ExceptionHandler(UnknownLiquidsoapEventException.class)
    public ResponseEntity<ErrorMessage> handleUnknownLiquidsoapEventException(UnknownLiquidsoapEventException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(QueueItemNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleQueueItemNotFoundException(QueueItemNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HistoryItemNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleHistoryItemNotFoundException(HistoryItemNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TrackNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleTrackNotFoundException(TrackNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new ErrorMessage(httpStatus, message), httpStatus);
    }
}
