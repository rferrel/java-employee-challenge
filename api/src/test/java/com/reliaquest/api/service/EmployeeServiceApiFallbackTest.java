package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.cache.EmployeeCache;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceApiFallbackTest {

    @Mock
    private EmployeeCache employeeCache;

    private EmployeeServiceApi employeeServiceApi;

    @BeforeEach
    void setUp() {
        employeeServiceApi = new EmployeeServiceApi(employeeCache);
    }

    @Test
    void getAllEmployeesFallback_ReturnsCachedData() {
        // Given
        List<Employee> cachedEmployees = List.of(new Employee());
        when(employeeCache.getAllEmployees()).thenReturn(cachedEmployees);

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEmployeesFallback(new RuntimeException());

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedEmployees, response.getBody());
    }

    @Test
    void getAllEmployeesFallback_ReturnsEmptyWhenNoCacheData() {
        // Given
        when(employeeCache.getAllEmployees()).thenReturn(null);

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEmployeesFallback(new RuntimeException());

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getEmployeeByIdFallback_ReturnsCachedEmployee() {
        // Given
        Employee cachedEmployee = new Employee();
        when(employeeCache.getEmployeeById("123")).thenReturn(cachedEmployee);

        // When
        ResponseEntity<Employee> response = employeeServiceApi.getEmployeeByIdFallback("123", new RuntimeException());

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedEmployee, response.getBody());
    }

    @Test
    void getEmployeeByIdFallback_ReturnsNotFoundWhenNoCacheData() {
        // Given
        when(employeeCache.getEmployeeById("123")).thenReturn(null);

        // When
        ResponseEntity<Employee> response = employeeServiceApi.getEmployeeByIdFallback("123", new RuntimeException());

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createEmployeeFallback_ReturnsServiceUnavailable() {
        // Given
        EmployeeInput input = new EmployeeInput();

        // When
        ResponseEntity<Employee> response = employeeServiceApi.createEmployeeFallback(input, new RuntimeException());

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void deleteEmployeeByIdFallback_ReturnsServiceUnavailable() {
        // When
        ResponseEntity<String> response = employeeServiceApi.deleteEmployeeByIdFallback("123", new RuntimeException());

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
