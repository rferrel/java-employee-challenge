package com.reliaquest.api.exception;

import com.reliaquest.api.model.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCircuitBreakerOpen_ReturnsServiceUnavailable() {
        // Given
        CallNotPermittedException exception = mock(CallNotPermittedException.class);
        when(exception.getCausingCircuitBreakerName()).thenReturn("test-circuit");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleCircuitBreakerOpen(exception);

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Service Unavailable", body.getError());
        assertTrue(body.getMessage().contains("test-circuit"));
        assertEquals(503, body.getStatus());
    }

    @Test
    void handleRateLimitExceeded_ReturnsTooManyRequests() {
        // Given
        RequestNotPermitted exception = mock(RequestNotPermitted.class);
        when(exception.getMessage()).thenReturn("Rate limit exceeded");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleRateLimitExceeded(exception);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Too Many Requests", body.getError());
        assertEquals("Rate limit exceeded. Please try again later.", body.getMessage());
        assertEquals(429, body.getStatus());
    }

    @Test
    void handleUpstreamRateLimit_ReturnsTooManyRequests() {
        // Given
        HttpClientErrorException.TooManyRequests exception = mock(HttpClientErrorException.TooManyRequests.class);
        when(exception.getMessage()).thenReturn("Upstream rate limit");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUpstreamRateLimit(exception);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Too Many Requests", body.getError());
        assertEquals("Upstream service rate limit exceeded. Please try again later.", body.getMessage());
        assertEquals(429, body.getStatus());
    }

    @Test
    void handleValidationException_ReturnsBadRequest() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid UUID format");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Bad Request", body.getError());
        assertEquals("Invalid UUID format", body.getMessage());
        assertEquals(400, body.getStatus());
    }

    @Test
    void handleValidationError_ReturnsBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationError(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Bad Request", body.getError());
        assertEquals("Invalid input", body.getMessage());
        assertEquals(400, body.getStatus());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void handleGeneral_ReturnsInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Internal Server Error", body.getError());
        assertEquals("An unexpected error occurred", body.getMessage());
        assertEquals(500, body.getStatus());
        assertNotNull(body.getTimestamp());
    }
}
