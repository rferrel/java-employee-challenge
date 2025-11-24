package com.reliaquest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("123", "John Doe", 75000, 30, "Engineer", "john@company.com");
    }

    // ========== Constructor Tests ==========

    @Test
    void constructor_WithAllParameters_InitializesCorrectly() {
        // Given & When
        Employee emp = new Employee("456", "Jane Smith", 85000, 35, "Manager", "jane@company.com");

        // Then
        assertEquals("456", emp.getId());
        assertEquals("Jane Smith", emp.getEmployeeName());
        assertEquals(85000, emp.getEmployeeSalary());
        assertEquals(35, emp.getEmployeeAge());
        assertEquals("Manager", emp.getEmployeeTitle());
        assertEquals("jane@company.com", emp.getEmployeeEmail());
    }

    @Test
    void noArgsConstructor_CreatesEmployeeWithNullFields() {
        // When
        Employee emp = new Employee();

        // Then
        assertNull(emp.getId());
        assertNull(emp.getEmployeeName());
        assertNull(emp.getEmployeeSalary());
        assertNull(emp.getEmployeeAge());
        assertNull(emp.getEmployeeTitle());
        assertNull(emp.getEmployeeEmail());
    }

    // ========== Getter Tests ==========

    @Test
    void getId_ReturnsIdCorrectly() {
        assertEquals("123", employee.getId());
    }

    @Test
    void getEmployeeName_ReturnsNameCorrectly() {
        assertEquals("John Doe", employee.getEmployeeName());
    }

    @Test
    void getEmployeeSalary_ReturnsSalaryCorrectly() {
        assertEquals(75000, employee.getEmployeeSalary());
    }

    @Test
    void getEmployeeAge_ReturnsAgeCorrectly() {
        assertEquals(30, employee.getEmployeeAge());
    }

    @Test
    void getEmployeeTitle_ReturnsTitleCorrectly() {
        assertEquals("Engineer", employee.getEmployeeTitle());
    }

    @Test
    void getEmployeeEmail_ReturnsEmailCorrectly() {
        assertEquals("john@company.com", employee.getEmployeeEmail());
    }

    // ========== Setter Tests ==========

    @Test
    void setId_UpdatesIdCorrectly() {
        // When
        employee.setId("999");

        // Then
        assertEquals("999", employee.getId());
    }

    @Test
    void setEmployeeName_UpdatesNameCorrectly() {
        // When
        employee.setEmployeeName("Jane Smith");

        // Then
        assertEquals("Jane Smith", employee.getEmployeeName());
    }

    @Test
    void setEmployeeSalary_UpdatesSalaryCorrectly() {
        // When
        employee.setEmployeeSalary(95000);

        // Then
        assertEquals(95000, employee.getEmployeeSalary());
    }

    @Test
    void setEmployeeAge_UpdatesAgeCorrectly() {
        // When
        employee.setEmployeeAge(35);

        // Then
        assertEquals(35, employee.getEmployeeAge());
    }

    @Test
    void setEmployeeTitle_UpdatesTitleCorrectly() {
        // When
        employee.setEmployeeTitle("Senior Engineer");

        // Then
        assertEquals("Senior Engineer", employee.getEmployeeTitle());
    }

    @Test
    void setEmployeeEmail_UpdatesEmailCorrectly() {
        // When
        employee.setEmployeeEmail("newemail@company.com");

        // Then
        assertEquals("newemail@company.com", employee.getEmployeeEmail());
    }

    // ========== Setter Chaining Tests ==========

    @Test
    void multipleSetters_AllUpdatesApplied() {
        // When
        employee.setId("555");
        employee.setEmployeeName("Bob Johnson");
        employee.setEmployeeSalary(120000);
        employee.setEmployeeAge(45);
        employee.setEmployeeTitle("Director");
        employee.setEmployeeEmail("bob@company.com");

        // Then
        assertEquals("555", employee.getId());
        assertEquals("Bob Johnson", employee.getEmployeeName());
        assertEquals(120000, employee.getEmployeeSalary());
        assertEquals(45, employee.getEmployeeAge());
        assertEquals("Director", employee.getEmployeeTitle());
        assertEquals("bob@company.com", employee.getEmployeeEmail());
    }

    // ========== Null Value Tests ==========

    @Test
    void setters_WithNullValues_AcceptNulls() {
        // When
        employee.setId(null);
        employee.setEmployeeName(null);
        employee.setEmployeeSalary(null);
        employee.setEmployeeAge(null);
        employee.setEmployeeTitle(null);
        employee.setEmployeeEmail(null);

        // Then
        assertNull(employee.getId());
        assertNull(employee.getEmployeeName());
        assertNull(employee.getEmployeeSalary());
        assertNull(employee.getEmployeeAge());
        assertNull(employee.getEmployeeTitle());
        assertNull(employee.getEmployeeEmail());
    }
}
