package com.reliaquest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmployeeTest {

    @Test
    void getId_ReturnsIdCorrectly() {
        // Given
        Employee employee = new Employee("123", "John Doe", 75000, 30, "Engineer", "john@company.com");

        // When & Then
        assertEquals("123", employee.getId());
    }

    @Test
    void getEmployeeAge_ReturnsAgeCorrectly() {
        // Given
        Employee employee = new Employee("123", "John Doe", 75000, 30, "Engineer", "john@company.com");

        // When & Then
        assertEquals(30, employee.getEmployeeAge());
    }

    @Test
    void getEmployeeTitle_ReturnsTitleCorrectly() {
        // Given
        Employee employee = new Employee("123", "John Doe", 75000, 30, "Engineer", "john@company.com");

        // When & Then
        assertEquals("Engineer", employee.getEmployeeTitle());
    }

    @Test
    void getEmployeeEmail_ReturnsEmailCorrectly() {
        // Given
        Employee employee = new Employee("123", "John Doe", 75000, 30, "Engineer", "john@company.com");

        // When & Then
        assertEquals("john@company.com", employee.getEmployeeEmail());
    }
}
