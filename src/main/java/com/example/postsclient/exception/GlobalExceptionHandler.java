package com.example.postsclient.exception;

import com.example.postsclient.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // The external API answered 404 (e.g. /posts/999)
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ApiError> handleNotFound(HttpClientErrorException.NotFound ex) {
        ApiError error = new ApiError(404, "NOT_FOUND", "Resource not found in JSONPlaceholder");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // No internet, DNS error, timeout...
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiError> handleUnavailable(ResourceAccessException ex) {
        log.error("JSONPlaceholder is not reachable: {}", ex.getMessage());
        ApiError error = new ApiError(503, "EXTERNAL_API_UNAVAILABLE", "JSONPlaceholder is not reachable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
