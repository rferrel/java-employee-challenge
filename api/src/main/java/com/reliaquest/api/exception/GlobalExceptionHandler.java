package com.reliaquest.api.exception;

import com.reliaquest.api.model.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerOpen(CallNotPermittedException e) {
        logger.warn("Circuit breaker open: {}", e.getCausingCircuitBreakerName());
        ErrorResponse error = new ErrorResponse(
                "Service Unavailable",
                "Service is temporarily unavailable. Circuit breaker: " + e.getCausingCircuitBreakerName(),
                HttpStatus.SERVICE_UNAVAILABLE.value());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RequestNotPermitted e) {
        logger.warn("Rate limit exceeded: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS.value());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ErrorResponse> handleUpstreamRateLimit(HttpClientErrorException.TooManyRequests e) {
        logger.warn("Upstream rate limit exceeded: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Too Many Requests",
                "Upstream service rate limit exceeded. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS.value());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException ex) {
        logger.warn("Validation error: {}", ex.getMessage());
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        ErrorResponse error = new ErrorResponse("Bad Request", message, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException e) {
        logger.warn("Validation error: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("Bad Request", e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        logger.error("Unexpected error", e);
        ErrorResponse error = new ErrorResponse(
                "Internal Server Error", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.internalServerError().body(error);
    }
}
