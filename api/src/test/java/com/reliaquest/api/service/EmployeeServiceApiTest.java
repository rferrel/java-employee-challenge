package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.cache.EmployeeCache;
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
    private EmployeeCache mockCache;
    private RestTemplate mockRestTemplate;
    private final String testApiBaseUrl = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        mockCache = mock(EmployeeCache.class);
        mockRestTemplate = mock(RestTemplate.class);
        employeeServiceApi = new EmployeeServiceApi(mockCache, mockRestTemplate);
        ReflectionTestUtils.setField(employeeServiceApi, "mockApiBaseUrl", testApiBaseUrl);
    }

    // ========== Configuration Tests ==========

    @Test
    void serviceConfiguration_BaseUrlIsSet() {
        String actualUrl = (String) ReflectionTestUtils.getField(employeeServiceApi, "mockApiBaseUrl");
        assertEquals(testApiBaseUrl, actualUrl);
    }

    @Test
    void serviceConfiguration_CacheIsInjected() {
        EmployeeCache actualCache = (EmployeeCache) ReflectionTestUtils.getField(employeeServiceApi, "employeeCache");
        assertNotNull(actualCache);
    }

    @Test
    void serviceConfiguration_RestTemplateIsInjected() {
        RestTemplate actualTemplate = (RestTemplate) ReflectionTestUtils.getField(employeeServiceApi, "restTemplate");
        assertNotNull(actualTemplate);
    }

    // ========== getAllEmployees Tests ==========

    @Test
    void getAllEmployees_SuccessfulApiCall_CachesResult() {
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
        verify(mockCache).putAllEmployees(apiEmployees);
        verify(mockRestTemplate)
                .exchange(eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllEmployees_ApiReturnsEmptyList_CachesEmptyList() {
        // Given
        List<Employee> emptyList = Arrays.asList();
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(emptyList);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyList, response.getBody());
        verify(mockCache).putAllEmployees(emptyList);
        verify(mockRestTemplate)
                .exchange(eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllEmployees_ApiReturnsNull_HandleGracefully() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        when(mockCache.getAllEmployees()).thenReturn(null);
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEmployees();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    // ========== getEmployeeById Tests ==========

    @Test
    void getEmployeeById_SuccessfulApiCall_CachesResult() {
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
        verify(mockCache).putEmployeeById(employeeId, apiEmployee);
    }

    @Test
    void getEmployeeById_ApiReturnsBothSuccessAndData_CachesAndReturns() {
        // Given
        String employeeId = "456e4567-e89b-12d3-a456-426614174000";
        Employee apiEmployee = new Employee(
                "456e4567-e89b-12d3-a456-426614174000", "Carol", 65000, 40, "Director", "carol@company.com");
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
        verify(mockCache).putEmployeeById(employeeId, apiEmployee);
    }

    @Test
    void getEmployeeById_ApiReturnsNotFound_Returns404() {
        // Given
        String employeeId = "999e4567-e89b-12d3-a456-426614174000";
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        ResponseEntity<Employee> response = employeeServiceApi.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getEmployeeById_ApiReturnsSuccessButNoData_Returns404() {
        // Given
        String employeeId = "888e4567-e89b-12d3-a456-426614174000";
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<Employee> response = employeeServiceApi.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== getEmployeesByNameSearch Tests ==========

    @Test
    void getEmployeesByNameSearch_ValidSearch_ReturnsFiltered() {
        // Given
        String searchString = "John";
        List<Employee> allEmployees = Arrays.asList(
                new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com"),
                new Employee("2", "Jane Smith", 75000, 35, "Manager", "jane@company.com"));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(allEmployees);

        when(mockCache.getAllEmployees()).thenReturn(null);
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getEmployeesByNameSearch(searchString);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getEmployeeName().contains("John"));
    }

    @Test
    void getEmployeesByNameSearch_NoMatches_ReturnsEmptyList() {
        // Given
        String searchString = "Nonexistent";
        List<Employee> allEmployees =
                Arrays.asList(new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com"));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(allEmployees);

        when(mockCache.getAllEmployees()).thenReturn(null);
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<List<Employee>> response = employeeServiceApi.getEmployeesByNameSearch(searchString);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    // ========== createEmployee Tests ==========

    @Test
    void createEmployee_Success_InvalidatesCache() {
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
        verify(mockCache).invalidateAll();
    }

    @Test
    void createEmployee_BadRequest_ReturnsBadRequest() {
        // Given
        EmployeeInput input = new EmployeeInput();
        input.setName("Invalid");
        input.setSalary(-1000); // Invalid salary

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        // When
        ResponseEntity<Employee> response = employeeServiceApi.createEmployee(input);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== deleteEmployeeById Tests ==========

    @Test
    void deleteEmployeeById_Success_InvalidatesCache() {
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
                        contains("John Doe"), // Delete by name
                        eq(HttpMethod.DELETE),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        // When
        ResponseEntity<String> response = employeeServiceApi.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody());
        verify(mockCache).invalidateEmployee(employeeId);
    }

    // ========== API Failure Tests ==========

    @Test
    void getAllEmployees_ApiFails_ThrowsException() {
        // Given
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> employeeServiceApi.getAllEmployees());
    }

    @Test
    void getAllEmployees_ApiReturnsNonSuccessStatus_ThrowsException() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(List.of());

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.status(500).body(apiResponse));

        // When & Then
        assertThrows(RuntimeException.class, () -> employeeServiceApi.getAllEmployees());
    }

    @Test
    void getAllEmployees_ApiReturnsNullBody_ThrowsException() {
        // Given
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When & Then
        assertThrows(RuntimeException.class, () -> employeeServiceApi.getAllEmployees());
    }

    @Test
    void getEmployeeById_ApiReturnsNullBody_Returns404() {
        // Given
        String employeeId = "777e4567-e89b-12d3-a456-426614174000";
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When
        ResponseEntity<Employee> response = employeeServiceApi.getEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createEmployee_Success_InvalidatesCacheAndReturnsEmployee() {
        // Given
        EmployeeInput input = new EmployeeInput("New Employee", 55000, 28, "Junior Developer");
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
        verify(mockCache).invalidateAll();
    }

    @Test
    void createEmployee_ApiReturnsBadRequest_Returns400() {
        // Given
        EmployeeInput input = new EmployeeInput("Invalid", -1000, 15, "Tester");

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        // When
        ResponseEntity<Employee> response = employeeServiceApi.createEmployee(input);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createEmployee_ApiReturnsSuccessButNoData_Returns400() {
        // Given
        EmployeeInput input = new EmployeeInput("Test", 50000, 25, "Tester");
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // When
        ResponseEntity<Employee> response = employeeServiceApi.createEmployee(input);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteEmployeeById_Success_InvalidatesCacheAndReturnsName() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        Employee employee = new Employee(
                "123e4567-e89b-12d3-a456-426614174000", "John Doe", 50000, 30, "Developer", "john@company.com");
        ApiResponse<Employee> getResponse = new ApiResponse<>();
        getResponse.setData(employee);

        ApiResponse<Boolean> deleteResponse = new ApiResponse<>();
        deleteResponse.setData(true);

        // Mock the GET request to fetch employee name
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(getResponse));

        // Mock the DELETE request
        when(mockRestTemplate.exchange(
                        contains("John Doe"), // Delete by name
                        eq(HttpMethod.DELETE),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        // When
        ResponseEntity<String> response = employeeServiceApi.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody());
        verify(mockCache).invalidateEmployee(employeeId);
    }

    @Test
    void deleteEmployeeById_EmployeeNotFound_Returns404() {
        // Given
        String employeeId = "999e4567-e89b-12d3-a456-426614174000";

        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        ResponseEntity<String> response = employeeServiceApi.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteEmployeeById_ApiReturnsSuccess_ButDeleteFails_Returns400() {
        // Given
        String employeeId = "123e4567-e89b-12d3-a456-426614174000";
        Employee employee = new Employee(
                "123e4567-e89b-12d3-a456-426614174000", "John Doe", 50000, 30, "Developer", "john@company.com");
        ApiResponse<Employee> getResponse = new ApiResponse<>();
        getResponse.setData(employee);

        // Mock GET succeeds
        when(mockRestTemplate.exchange(
                        eq(testApiBaseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(getResponse));

        // Mock DELETE fails
        when(mockRestTemplate.exchange(
                        contains("John Doe"), eq(HttpMethod.DELETE), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        // When
        ResponseEntity<String> response = employeeServiceApi.deleteEmployeeById(employeeId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== Fallback Method Tests ==========

    @Test
    void getAllEmployeesFallback_WithCache_ReturnsCachedData() {
        // Given
        List<Employee> cachedEmployees =
                Arrays.asList(new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com"));
        when(mockCache.getAllEmployees()).thenReturn(cachedEmployees);

        // When
        ResponseEntity<List<Employee>> response =
                employeeServiceApi.getAllEmployeesFallback(new Exception("Circuit breaker open"));

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedEmployees, response.getBody());
    }

    @Test
    void getAllEmployeesFallback_NoCache_ReturnsEmptyList() {
        // Given
        when(mockCache.getAllEmployees()).thenReturn(null);

        // When
        ResponseEntity<List<Employee>> response =
                employeeServiceApi.getAllEmployeesFallback(new Exception("Circuit breaker open"));

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void getEmployeeByIdFallback_WithCache_ReturnsCachedEmployee() {
        // Given
        String employeeId = "123";
        Employee cachedEmployee = new Employee("123", "John Doe", 50000, 30, "Developer", "john@company.com");
        when(mockCache.getEmployeeById(employeeId)).thenReturn(cachedEmployee);

        // When
        ResponseEntity<Employee> response =
                employeeServiceApi.getEmployeeByIdFallback(employeeId, new Exception("Circuit breaker open"));

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedEmployee, response.getBody());
    }

    @Test
    void getEmployeeByIdFallback_NoCache_ReturnsNotFound() {
        // Given
        String employeeId = "123";
        when(mockCache.getEmployeeById(employeeId)).thenReturn(null);

        // When
        ResponseEntity<Employee> response =
                employeeServiceApi.getEmployeeByIdFallback(employeeId, new Exception("Circuit breaker open"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createEmployeeFallback_ReturnsServiceUnavailable() {
        // Given
        EmployeeInput input = new EmployeeInput();

        // When
        ResponseEntity<Employee> response =
                employeeServiceApi.createEmployeeFallback(input, new Exception("Circuit breaker open"));

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void deleteEmployeeByIdFallback_ReturnsServiceUnavailable() {
        // Given
        String employeeId = "123";

        // When
        ResponseEntity<String> response =
                employeeServiceApi.deleteEmployeeByIdFallback(employeeId, new Exception("Circuit breaker open"));

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
