package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for UUID validation using the @ValidUUID annotation.
 * These tests require a running API instance with mock server.
 *
 * Disabled by default. To run:
 * 1. Start mock server: ./gradlew server:bootRun
 * 2. Run tests: ./gradlew test --tests InterfaceMismatchTest
 *
 * The @ValidUUID annotation (defined in EmployeeValidation) validates UUID format
 * on the id parameter for the getEmployeeById endpoint.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Requires mock server running on port 8112")
class InterfaceMismatchTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void getEmployeeById_InvalidUuidFormat_Returns400() {
        // Given an invalid UUID format
        String url = "http://localhost:" + port + "/api/v1/employee/test-id";

        // When calling the endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then it should return 400 Bad Request due to @ValidUUID validation
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getEmployeeById_ValidUuidFormat_NotBadRequest() {
        // Given a valid UUID format
        String url = "http://localhost:" + port + "/api/v1/employee/123e4567-e89b-12d3-a456-426614174000";

        // When calling the endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then it should not return 400 (UUID validation passes)
        // May return 404, 503, or 200 depending on mock server state
        assertNotEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
