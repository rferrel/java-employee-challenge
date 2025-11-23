package com.reliaquest.api.service;

import com.reliaquest.api.cache.EmployeeCache;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeServiceApi {

    private final Logger logger = LoggerFactory.getLogger(EmployeeServiceApi.class);
    private final EmployeeCache employeeCache;

    @Value("${mock.api.base-url}")
    private String mockApiBaseUrl;

    public EmployeeServiceApi(EmployeeCache employeeCache) {
        this.employeeCache = employeeCache;
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-get-all", fallbackMethod = "getAllEmployeesFallback")
    public ResponseEntity<List<Employee>> getAllEployees() {
        // Check cache first
        List<Employee> cachedEmployees = employeeCache.getAllEmployees();
        if (cachedEmployees != null) {
            logger.debug("Returning cached employees, count: {}", cachedEmployees.size());
            return ResponseEntity.ok(cachedEmployees);
        }

        // Cache miss - call API
        logger.debug("Cache miss - calling API for all employees");
        try {
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    mockApiBaseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            List<Employee> employees = response.getBody().getData();

            // Cache the result
            employeeCache.putAllEmployees(employees);
            logger.debug("Cached {} employees", employees.size());

            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            logger.error("Error fetching all employees", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-get-by-id", fallbackMethod = "getEmployeeByIdFallback")
    public ResponseEntity<Employee> getEmployeeById(String id) {
        // Check cache first
        Employee cachedEmployee = employeeCache.getEmployeeById(id);
        if (cachedEmployee != null) {
            logger.debug("Returning cached employee for id: {}", id);
            return ResponseEntity.ok(cachedEmployee);
        }

        // Cache miss - call API
        logger.debug("Cache miss - calling API for employee id: {}", id);
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = mockApiBaseUrl + "/" + id;
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData() != null) {
                Employee employee = response.getBody().getData();

                // Cache the result
                employeeCache.putEmployeeById(id, employee);
                logger.debug("Cached employee: {}", employee.getEmployeeName());

                return ResponseEntity.ok(employee);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Employee not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching employee with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        try {
            // Use cached getAllEmployees if available, otherwise call API
            ResponseEntity<List<Employee>> allEmployeesResponse = getAllEployees();

            if (!allEmployeesResponse.getStatusCode().is2xxSuccessful() || allEmployeesResponse.getBody() == null) {
                return ResponseEntity.ok(List.of());
            }

            // Filter employees by name containing the search string (case insensitive)
            List<Employee> filteredEmployees = allEmployeesResponse.getBody().stream()
                    .filter(employee -> employee.getEmployeeName() != null
                            && employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                    .toList();

            logger.debug("Filtered {} employees for search: '{}'", filteredEmployees.size(), searchString);
            return ResponseEntity.ok(filteredEmployees);
        } catch (Exception e) {
            logger.error("Error searching employees by name: {}", searchString, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-create", fallbackMethod = "createEmployeeFallback")
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    mockApiBaseUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(employeeInput),
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getData() != null) {
                Employee createdEmployee = response.getBody().getData();

                // Invalidate cache since we added a new employee
                employeeCache.invalidateAll();
                logger.debug("Cache invalidated after creating employee: {}", createdEmployee.getEmployeeName());

                return ResponseEntity.ok(createdEmployee);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (HttpClientErrorException e) {
            logger.error("Client error when creating employee: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Error creating employee", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-delete", fallbackMethod = "deleteEmployeeByIdFallback")
    public ResponseEntity<String> deleteEmployeeById(String id) {
        try {
            // First get the employee to retrieve the name
            ResponseEntity<Employee> employeeResponse = getEmployeeById(id);
            if (!employeeResponse.getStatusCode().is2xxSuccessful() || employeeResponse.getBody() == null) {
                return ResponseEntity.notFound().build();
            }

            String employeeName = employeeResponse.getBody().getEmployeeName();

            // Delete using employee name (as per API spec)
            RestTemplate restTemplate = new RestTemplate();
            String deleteUrl = mockApiBaseUrl + "/" + employeeName;
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    deleteUrl, HttpMethod.DELETE, null, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getStatusCode().is2xxSuccessful()) {
                // Invalidate cache since we deleted an employee
                employeeCache.invalidateEmployee(id);
                logger.debug("Cache invalidated after deleting employee: {}", employeeName);

                return ResponseEntity.ok(employeeName);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Employee not found for deletion with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting employee with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Fallback methods
    public ResponseEntity<List<Employee>> getAllEmployeesFallback(Exception ex) {
        logger.error("Circuit breaker fallback for getAllEmployees", ex);
        return ResponseEntity.ok(employeeCache.getAllEmployees() != null ? employeeCache.getAllEmployees() : List.of());
    }

    public ResponseEntity<Employee> getEmployeeByIdFallback(String id, Exception ex) {
        logger.error("Circuit breaker fallback for getEmployeeById: {}", id, ex);
        Employee cached = employeeCache.getEmployeeById(id);
        return cached != null
                ? ResponseEntity.ok(cached)
                : ResponseEntity.notFound().build();
    }

    public ResponseEntity<Employee> createEmployeeFallback(EmployeeInput employeeInput, Exception ex) {
        logger.error("Circuit breaker fallback for createEmployee", ex);
        return ResponseEntity.status(503).build();
    }

    public ResponseEntity<String> deleteEmployeeByIdFallback(String id, Exception ex) {
        logger.error("Circuit breaker fallback for deleteEmployeeById: {}", id, ex);
        return ResponseEntity.status(503).build();
    }
}
