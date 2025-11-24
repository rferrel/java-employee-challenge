package com.reliaquest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    private ApiResponse<String> stringResponse;
    private ApiResponse<Integer> integerResponse;
    private ApiResponse<List<String>> listResponse;

    @BeforeEach
    void setUp() {
        stringResponse = new ApiResponse<>("test-data", "success");
        integerResponse = new ApiResponse<>(42, "success");
        listResponse = new ApiResponse<>(Arrays.asList("item1", "item2"), "success");
    }

    // ========== Constructor Tests ==========

    @Test
    void noArgsConstructor_CreatesResponseWithNullFields() {
        // When
        ApiResponse<String> response = new ApiResponse<>();

        // Then
        assertNull(response.getData());
        assertNull(response.getStatus());
    }

    @Test
    void parameterizedConstructor_WithStringData() {
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
    void parameterizedConstructor_WithIntegerData() {
        // Given
        Integer data = 100;
        String status = "success";

        // When
        ApiResponse<Integer> response = new ApiResponse<>(data, status);

        // Then
        assertEquals(data, response.getData());
        assertEquals(status, response.getStatus());
    }

    @Test
    void parameterizedConstructor_WithListData() {
        // Given
        List<String> data = Arrays.asList("one", "two", "three");
        String status = "success";

        // When
        ApiResponse<List<String>> response = new ApiResponse<>(data, status);

        // Then
        assertEquals(data, response.getData());
        assertEquals(status, response.getStatus());
        assertEquals(3, response.getData().size());
    }

    // ========== Getter Tests ==========

    @Test
    void getData_ReturnsData() {
        assertEquals("test-data", stringResponse.getData());
    }

    @Test
    void getStatus_ReturnsStatus() {
        assertEquals("success", stringResponse.getStatus());
    }

    @Test
    void getData_WithIntegerType() {
        assertEquals(42, integerResponse.getData());
    }

    @Test
    void getData_WithListType() {
        List<String> data = listResponse.getData();
        assertEquals(2, data.size());
        assertTrue(data.contains("item1"));
        assertTrue(data.contains("item2"));
    }

    // ========== Setter Tests ==========

    @Test
    void setData_UpdatesDataCorrectly() {
        // When
        stringResponse.setData("new-data");

        // Then
        assertEquals("new-data", stringResponse.getData());
    }

    @Test
    void setStatus_UpdatesStatusCorrectly() {
        // When
        stringResponse.setStatus("error");

        // Then
        assertEquals("error", stringResponse.getStatus());
    }

    @Test
    void setData_WithDifferentType() {
        // When
        integerResponse.setData(999);

        // Then
        assertEquals(999, integerResponse.getData());
    }

    @Test
    void setData_WithListType() {
        // When
        List<String> newData = Arrays.asList("a", "b", "c", "d");
        listResponse.setData(newData);

        // Then
        assertEquals(newData, listResponse.getData());
        assertEquals(4, listResponse.getData().size());
    }

    // ========== Multiple Setter Tests ==========

    @Test
    void multipleSetters_AllUpdatesApplied() {
        // When
        stringResponse.setData("updated-data");
        stringResponse.setStatus("pending");

        // Then
        assertEquals("updated-data", stringResponse.getData());
        assertEquals("pending", stringResponse.getStatus());
    }

    // ========== Null Value Tests ==========

    @Test
    void setData_WithNullValue() {
        // When
        stringResponse.setData(null);

        // Then
        assertNull(stringResponse.getData());
    }

    @Test
    void setStatus_WithNullValue() {
        // When
        stringResponse.setStatus(null);

        // Then
        assertNull(stringResponse.getStatus());
    }

    @Test
    void parameterizedConstructor_WithNullData() {
        // When
        ApiResponse<String> response = new ApiResponse<>(null, "success");

        // Then
        assertNull(response.getData());
        assertEquals("success", response.getStatus());
    }

    @Test
    void parameterizedConstructor_WithNullStatus() {
        // When
        ApiResponse<String> response = new ApiResponse<>("data", null);

        // Then
        assertEquals("data", response.getData());
        assertNull(response.getStatus());
    }
}
