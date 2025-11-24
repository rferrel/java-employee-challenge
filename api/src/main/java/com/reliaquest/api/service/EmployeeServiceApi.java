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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class EmployeeServiceApi {

    private final Logger logger = LoggerFactory.getLogger(EmployeeServiceApi.class);
    private final EmployeeCache employeeCache;
    private final RestTemplate restTemplate;

    @Value("${mock.api.base-url}")
    private String mockApiBaseUrl;

    public EmployeeServiceApi(EmployeeCache employeeCache, RestTemplate restTemplate) {
        this.employeeCache = employeeCache;
        this.restTemplate = restTemplate;
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-get-all", fallbackMethod = "getAllEmployeesFallback")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        logger.debug("Calling API for all employees");

        ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                mockApiBaseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

        // Check response status
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            logger.warn("API returned non-success status: {}", response.getStatusCode());
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }

        List<Employee> employees = response.getBody().getData();
        if (employees == null) {
            logger.warn("API returned null data");
            employees = List.of();
        }

        // Cache the result
        employeeCache.putAllEmployees(employees);
        logger.debug("Cached {} employees", employees.size());

        return ResponseEntity.ok(employees);
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-get-by-id", fallbackMethod = "getEmployeeByIdFallback")
    public ResponseEntity<Employee> getEmployeeById(String id) {
        logger.debug("Calling API for employee id: {}", id);
        String url = UriComponentsBuilder.fromHttpUrl(mockApiBaseUrl)
                .path("/{id}")
                .buildAndExpand(id)
                .toUriString();
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
    }

    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        try {
            // Use cached getAllEmployees if available, otherwise call API
            ResponseEntity<List<Employee>> allEmployeesResponse = getAllEmployees();

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
        } catch (io.github.resilience4j.circuitbreaker.CallNotPermittedException e) {
            logger.warn("Circuit breaker open while searching employees by name: {}", searchString);
            return ResponseEntity.status(503).build();
        } catch (Exception e) {
            logger.error("Error searching employees by name: {}", searchString, e);
            return ResponseEntity.ok(List.of()); // Return empty list on error as graceful degradation
        }
    }

    @CircuitBreaker(name = "employee-api-get-all", fallbackMethod = "getHighestSalaryOfEmployeesFallback")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        ResponseEntity<List<Employee>> response = getAllEmployees();

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isEmpty()) {
            logger.warn("No employees found or error retrieving employees");
            return ResponseEntity.ok(0);
        }

        Integer highestSalary = response.getBody().stream()
                .filter(employee -> employee.getEmployeeSalary() != null)
                .map(Employee::getEmployeeSalary)
                .max(Integer::compareTo)
                .orElse(0);

        logger.debug("Retrieved highest salary: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    public ResponseEntity<Integer> getHighestSalaryOfEmployeesFallback(Exception ex) {
        logger.error("Circuit breaker fallback for getHighestSalaryOfEmployees", ex);
        return ResponseEntity.ok(0);
    }

    @CircuitBreaker(name = "employee-api-get-all", fallbackMethod = "getTopTenHighestEarningEmployeeNamesFallback")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        ResponseEntity<List<Employee>> response = getAllEmployees();

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isEmpty()) {
            logger.warn("No employees found or error retrieving employees");
            return ResponseEntity.ok(List.of());
        }

        List<String> topTenNames = response.getBody().stream()
                .filter(employee -> employee.getEmployeeSalary() != null && employee.getEmployeeName() != null)
                .sorted((e1, e2) -> e2.getEmployeeSalary().compareTo(e1.getEmployeeSalary()))
                .limit(10)
                .map(Employee::getEmployeeName)
                .toList();

        logger.debug("Retrieved top 10 highest earning employees: {}", topTenNames.size());
        return ResponseEntity.ok(topTenNames);
    }

    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNamesFallback(Exception ex) {
        logger.error("Circuit breaker fallback for getTopTenHighestEarningEmployeeNames", ex);
        return ResponseEntity.ok(List.of());
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-create", fallbackMethod = "createEmployeeFallback")
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        logger.debug("Calling API to create employee: {}", employeeInput.getName());
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
    }

    @Retry(name = "employee-api")
    @CircuitBreaker(name = "employee-api-delete", fallbackMethod = "deleteEmployeeByIdFallback")
    public ResponseEntity<String> deleteEmployeeById(String id) {
        logger.debug("Calling API to delete employee with id: {}", id);
        // Fetch employee name directly from API (without circuit breaker)
        String employeeName = fetchEmployeeNameById(id);
        if (employeeName == null) {
            logger.warn("Employee not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }

        // Delete using employee name (as per API spec)
        String deleteUrl = UriComponentsBuilder.fromHttpUrl(mockApiBaseUrl)
                .path("/{name}")
                .buildAndExpand(employeeName)
                .toUriString();
        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                deleteUrl, HttpMethod.DELETE, null, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            // Invalidate both individual employee and all employees cache
            employeeCache.invalidateEmployee(id);
            logger.debug("Cache invalidated after deleting employee: {}", employeeName);

            return ResponseEntity.ok(employeeName);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private String fetchEmployeeNameById(String id) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(mockApiBaseUrl)
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUriString();
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Employee employee = response.getBody().getData();
                return employee != null ? employee.getEmployeeName() : null;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error fetching employee name for id: {}", id, e);
            return null;
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
