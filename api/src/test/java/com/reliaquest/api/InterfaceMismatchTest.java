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
 * Tests for @ValidUUID annotation integration tests. These require a running API instance with mock
 * server. Disabled by default. To run: 1. Start mock server: ./gradlew server:bootRun 2. Run tests:
 * ./gradlew test --tests InterfaceMismatchTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Requires mock server running on port 8112")
class InterfaceMismatchTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void testInvalidUuidReturns400() {
        // Test that our API now properly validates UUID format
        String url = "http://localhost:" + port + "/api/v1/employee/test-id";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Should return 400 Bad Request due to our UUID validation
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testValidUuidFormat() {
        // Test with valid UUID format (even if employee doesn't exist)
        String url = "http://localhost:" + port + "/api/v1/employee/123e4567-e89b-12d3-a456-426614174000";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Should not return 400 (UUID validation passes)
        // May return 404, 503, etc. depending on mock server state
        assertNotEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
