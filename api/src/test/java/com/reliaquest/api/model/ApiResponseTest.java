package com.reliaquest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void constructor_SetsDataAndStatus() {
        // Given
        String data = "test-data";
        String status = "success";

        // When
        ApiResponse<String> response = new ApiResponse<>(data, status);

        // Then
        assertEquals(data, response.getData());
        assertEquals(status, response.getStatus());
    }

    @Test
    void getStatus_ReturnsStatus() {
        // Given
        ApiResponse<String> response = new ApiResponse<>("data", "test-status");

        // When & Then
        assertEquals("test-status", response.getStatus());
    }
}
