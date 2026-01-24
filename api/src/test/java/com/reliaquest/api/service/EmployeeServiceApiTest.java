package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceApiTest {

    private EmployeeServiceApi employeeServiceApi;
    private RestTemplate mockRestTemplate;
    private final String testApiBaseUrl = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        mockRestTemplate = mock(RestTemplate.class);
        employeeServiceApi = new EmployeeServiceApi(mockRestTemplate);
        ReflectionTestUtils.setField(employeeServiceApi, "mockApiBaseUrl", testApiBaseUrl);
    }

    @Test
    void getAllEmployees_SuccessfulApiCall_ReturnsEmployees() {
        // Given
        Employee emp1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee emp2 = new Employee("2", "Jane Smith", 75000, 35, "Manager", "jane@company.com");
        List<Employee> apiEmployees = Arrays.asList(emp1, emp2);
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(apiEmployees);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiEmployees, response.getBody());
    }

    @Test
    void getEmployeeById_SuccessfulApiCall_ReturnsEmployee() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        Employee apiEmployee =
                new Employee("123e4567-e89b-12d3-a456-426614174000", "Bob", 55000, 32, "Architect", "bob@company.com");
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(apiEmployee);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<Employee> response = employeeServiceApi.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiEmployee, response.getBody());
    }

    @Test
    void createEmployee_Success_ReturnsEmployee() {
        // Given
        EmployeeInput input = new EmployeeInput();
        input.setName("New Employee");
        input.setSalary(55000);
        input.setAge(28);
        input.setTitle("Junior Developer");

        Employee createdEmployee =
                new Employee("999", "New Employee", 55000, 28, "Junior Developer", "new@company.com");
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(createdEmployee);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<Employee> response = employeeServiceApi.createEmployee(input);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdEmployee, response.getBody());
    }

    @Test
    void deleteEmployeeById_Success_ReturnsEmployeeName() {
        // Given
        String employeeId = "123";
        Employee employee = new Employee("123", "John Doe", 50000, 30, "Developer", "john@company.com");
        ApiResponse<Employee> getResponse = new ApiResponse<>();
        getResponse.setData(employee);

        ApiResponse<Boolean> deleteResponse = new ApiResponse<>();
        deleteResponse.setData(true);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(getResponse));

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        // When
        ResponseEntity<String> response = employeeServiceApi.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody());
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        // Given
        List<Employee> allEmployees = Arrays.asList(
                new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com"),
                new Employee("2", "Jane Smith", 75000, 35, "Manager", "jane@company.com"),
                new Employee("3", "Bob Johnson", 80000, 40, "Director", "bob@company.com"));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(allEmployees);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<Integer> response = employeeServiceApi.getHighestSalaryOfEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(80000, response.getBody());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Given
        List<Employee> allEmployees = Arrays.asList(
                new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com"),
                new Employee("2", "Jane Smith", 75000, 35, "Manager", "jane@company.com"),
                new Employee("3", "Bob Johnson", 80000, 40, "Director", "bob@company.com"));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(allEmployees);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<List<String>> response = employeeServiceApi.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("Bob Johnson", response.getBody().get(0));
    }
}
