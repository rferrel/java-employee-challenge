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
        when(employeeServiceApi.getAllEmployees()).thenReturn(ResponseEntity.ok(testEmployees));

        // When
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployees, response.getBody());
        verify(employeeServiceApi).getAllEmployees();
    }

    @Test
    void getAllEmployees_ServiceReturnsError() {
        // Given
        when(employeeServiceApi.getAllEmployees())
                .thenReturn(ResponseEntity.internalServerError().build());

        // When
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(employeeServiceApi).getAllEmployees();
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
    void getEmployeeById_ValidId_CallsService() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        when(employeeServiceApi.getEmployeeById(employeeId)).thenReturn(ResponseEntity.ok(testEmployee1));

        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(employeeServiceApi).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_NotFound_ReturnsNotFound() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        when(employeeServiceApi.getEmployeeById(employeeId))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(employeeServiceApi).getEmployeeById(employeeId);
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
        when(employeeServiceApi.getHighestSalaryOfEmployees()).thenReturn(ResponseEntity.ok(75000));

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(75000, response.getBody());
        verify(employeeServiceApi).getHighestSalaryOfEmployees();
    }

    @Test
    void getHighestSalaryOfEmployees_NoEmployees() {
        // Given
        when(employeeServiceApi.getHighestSalaryOfEmployees()).thenReturn(ResponseEntity.ok(0));

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody());
        verify(employeeServiceApi).getHighestSalaryOfEmployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Given
        List<String> topTenNames = Arrays.asList("Jane Smith", "John Doe");
        when(employeeServiceApi.getTopTenHighestEarningEmployeeNames())
                .thenReturn(ResponseEntity.ok(topTenNames));

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Jane Smith", response.getBody().get(0)); // Highest salary first
        assertEquals("John Doe", response.getBody().get(1));
        verify(employeeServiceApi).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_NoEmployees() {
        // Given
        when(employeeServiceApi.getTopTenHighestEarningEmployeeNames())
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verify(employeeServiceApi).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ServiceError() {
        // Given
        when(employeeServiceApi.getTopTenHighestEarningEmployeeNames())
                .thenReturn(ResponseEntity.status(503).build());

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(employeeServiceApi).getTopTenHighestEarningEmployeeNames();
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
    void createEmployee_ValidInput_CallsService() {
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
    void deleteEmployeeById_ValidId_Success() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        when(employeeServiceApi.deleteEmployeeById(employeeId))
                .thenReturn(ResponseEntity.ok(testEmployee1.getEmployeeName()));

        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployee1.getEmployeeName(), response.getBody());
        verify(employeeServiceApi).deleteEmployeeById(employeeId);
    }

    @Test
    void deleteEmployeeById_NotFound() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        when(employeeServiceApi.deleteEmployeeById(employeeId))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(employeeServiceApi).deleteEmployeeById(employeeId);
    }
}
