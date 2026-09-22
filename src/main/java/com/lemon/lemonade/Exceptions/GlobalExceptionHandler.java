package com.lemon.lemonade.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

// Extends ResponseEntityExceptionHandler so Spring's own errors (e.g. 413 upload too large) are answered here.
// Otherwise they're forwarded to /error, which isn't public, and the client gets a 401 instead.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFountException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFountException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(TokenNotValidException.class)
    public ResponseEntity<String> handleTokenNotValid(TokenNotValidException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Errors returned by SeaweedFS, e.g. 416 for a Range past the end of a song
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<String> handleStorage(S3Exception e) {
        String message = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
        return ResponseEntity.status(e.statusCode()).body("Storage error: " + message);
    }

    // SeaweedFS isn't reachable, e.g. the container isn't running
    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<String> handleStorageUnavailable(SdkClientException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Storage unavailable: " + e.getMessage());
    }
}
