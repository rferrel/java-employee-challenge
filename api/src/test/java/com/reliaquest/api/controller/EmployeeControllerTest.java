package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeServiceApi;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeServiceApi employeeServiceApi;

    private EmployeeController employeeController;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        employeeController = new EmployeeController(employeeServiceApi);

        testEmployee1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        testEmployee2 = new Employee("2", "Jane Smith", 75000, 35, "Manager", "jane@company.com");
        testEmployees = Arrays.asList(testEmployee1, testEmployee2);
    }

    @Test
    void getAllEmployees_Success() {
        // Given
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(testEmployees));

        // When
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployees, response.getBody());
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getAllEmployees_ServiceReturnsError() {
        // Given
        when(employeeServiceApi.getAllEployees())
                .thenReturn(ResponseEntity.internalServerError().build());

        // When
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getEmployeesByNameSearch_ValidSearch() {
        // Given
        String searchString = "John";
        List<Employee> filteredEmployees = Arrays.asList(testEmployee1);
        when(employeeServiceApi.getEmployeesByNameSearch(searchString))
                .thenReturn(ResponseEntity.ok(filteredEmployees));

        // When
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(searchString);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filteredEmployees, response.getBody());
        verify(employeeServiceApi).getEmployeesByNameSearch(searchString);
    }

    @Test
    void getEmployeesByNameSearch_NullSearchString() {
        // When
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeesByNameSearch_EmptySearchString() {
        // When
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeesByNameSearch_InvalidCharacters() {
        // Given
        String invalidSearchString = "John123";

        // When
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(invalidSearchString);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeesByNameSearch_SpecialCharacters() {
        // Given
        String invalidSearchString = "John@Doe";

        // When
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(invalidSearchString);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeeById_ValidId() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        when(employeeServiceApi.getEmployeeById(employeeId)).thenReturn(ResponseEntity.ok(testEmployee1));

        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployee1, response.getBody());
        verify(employeeServiceApi).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_NullId() {
        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeeById_EmptyId() {
        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById("");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeeById_InvalidUuidFormat() {
        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById("test-id");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void getEmployeeById_NotFound() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174999";
        when(employeeServiceApi.getEmployeeById(employeeId))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(employeeServiceApi).getEmployeeById(employeeId);
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        // Given
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(testEmployees));

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(75000, response.getBody()); // Jane's salary is highest
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getHighestSalaryOfEmployees_NoEmployees() {
        // Given
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody());
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getHighestSalaryOfEmployees_ServiceError() {
        // Given
        when(employeeServiceApi.getAllEployees())
                .thenReturn(ResponseEntity.internalServerError().build());

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody());
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getHighestSalaryOfEmployees_NullSalaries() {
        // Given
        Employee empWithNullSalary = new Employee("3", "Test User", null, 25, "Tester", "test@company.com");
        List<Employee> employeesWithNulls = Arrays.asList(testEmployee1, empWithNullSalary);
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(employeesWithNulls));

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(50000, response.getBody()); // Should ignore null salary
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Given
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(testEmployees));

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Jane Smith", response.getBody().get(0)); // Highest salary first
        assertEquals("John Doe", response.getBody().get(1));
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_NoEmployees() {
        // Given
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ServiceError() {
        // Given
        when(employeeServiceApi.getAllEployees())
                .thenReturn(ResponseEntity.internalServerError().build());

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_WithNullValues() {
        // Given
        Employee empWithNullSalary = new Employee("3", "Test User", null, 25, "Tester", "test@company.com");
        Employee empWithNullName = new Employee("4", null, 60000, 28, "Developer", "dev@company.com");
        List<Employee> employeesWithNulls =
                Arrays.asList(testEmployee1, testEmployee2, empWithNullSalary, empWithNullName);
        when(employeeServiceApi.getAllEployees()).thenReturn(ResponseEntity.ok(employeesWithNulls));

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size()); // Should filter out null salary and null name
        assertTrue(response.getBody().contains("Jane Smith"));
        assertTrue(response.getBody().contains("John Doe"));
        verify(employeeServiceApi).getAllEployees();
    }

    @Test
    void createEmployee_ValidInput() {
        // Given
        EmployeeInput employeeInput = new EmployeeInput("Test User", 60000, 28, "Tester");
        Employee createdEmployee = new Employee("new-id", "Test User", 60000, 28, "Tester", "test@company.com");
        when(employeeServiceApi.createEmployee(employeeInput)).thenReturn(ResponseEntity.ok(createdEmployee));

        // When
        ResponseEntity<Employee> response = employeeController.createEmployee(employeeInput);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdEmployee, response.getBody());
        verify(employeeServiceApi).createEmployee(employeeInput);
    }

    @Test
    void createEmployee_NullInput() {
        // When
        ResponseEntity<Employee> response = employeeController.createEmployee(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void createEmployee_InvalidName() {
        // Given
        EmployeeInput employeeInput = new EmployeeInput("", 60000, 28, "Tester");

        // When
        ResponseEntity<Employee> response = employeeController.createEmployee(employeeInput);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void createEmployee_InvalidSalary() {
        // Given
        EmployeeInput employeeInput = new EmployeeInput("Test User", -1000, 28, "Tester");

        // When
        ResponseEntity<Employee> response = employeeController.createEmployee(employeeInput);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void createEmployee_InvalidAge() {
        // Given
        EmployeeInput employeeInput = new EmployeeInput("Test User", 60000, 15, "Tester");

        // When
        ResponseEntity<Employee> response = employeeController.createEmployee(employeeInput);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void deleteEmployeeById_ValidId() {
        // Given
        String employeeId = "1";
        String employeeName = "John Doe";
        when(employeeServiceApi.deleteEmployeeById(employeeId)).thenReturn(ResponseEntity.ok(employeeName));

        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(employeeName, response.getBody());
        verify(employeeServiceApi).deleteEmployeeById(employeeId);
    }

    @Test
    void deleteEmployeeById_NullId() {
        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void deleteEmployeeById_EmptyId() {
        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById("");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(employeeServiceApi);
    }

    @Test
    void deleteEmployeeById_NotFound() {
        // Given
        String employeeId = "999";
        when(employeeServiceApi.deleteEmployeeById(employeeId))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(employeeServiceApi).deleteEmployeeById(employeeId);
    }
}
