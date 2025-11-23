package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeServiceApi;
import com.reliaquest.api.util.EmployeeValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@Tag(name = "Employee", description = "Employee management operations")
@Validated
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {
    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeServiceApi employeeServiceApi;

    public EmployeeController(EmployeeServiceApi employeeServiceApi) {
        this.employeeServiceApi = employeeServiceApi;
    }

    @Override
    @Operation(summary = "Get all employees", description = "Retrieves all employees from the system")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved employees"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<List<Employee>> getAllEmployees() {
        // todo: what kind of validation should be done here?
        // todo
        return employeeServiceApi.getAllEployees();
    }

    @Override
    @Operation(
            summary = "Search employees by name",
            description = "Search for employees by name fragment (letters and spaces only)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved matching employees"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid search string - only letters and spaces allowed"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(
            @Parameter(description = "Name fragment to search for", example = "John") String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        if (!EmployeeValidation.isValidNameSearch(searchString)) {
            logger.warn("Invalid name search string: '{}' - contains non-alphabetic characters", searchString);
            return ResponseEntity.badRequest().build();
        }

        return employeeServiceApi.getEmployeesByNameSearch(searchString);
    }

    @Override
    @Operation(summary = "Get employee by ID", description = "Retrieve a specific employee by their unique ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved employee"),
                @ApiResponse(responseCode = "404", description = "Employee not found"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<Employee> getEmployeeById(
            @Parameter(description = "Employee ID", example = "123e4567-e89b-12d3-a456-426614174000") String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Employee ID is null or empty");
            return ResponseEntity.badRequest().build();
        }
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID format: {}", id);
            return ResponseEntity.badRequest().build();
        }
        return employeeServiceApi.getEmployeeById(id);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEployees();

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null
                || response.getBody().isEmpty()) {
            logger.warn("No employees found or error retrieving employees");
            return ResponseEntity.ok(0);
        }

        Integer highestSalary = response.getBody().stream()
                .filter(employee -> employee.getEmployeeSalary() != null)
                .map(Employee::getEmployeeSalary)
                .max(Integer::compareTo)
                .orElse(0);

        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        ResponseEntity<List<Employee>> response = employeeServiceApi.getAllEployees();

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null
                || response.getBody().isEmpty()) {
            logger.warn("No employees found or error retrieving employees");
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<String> topTenNames = response.getBody().stream()
                .filter(employee -> employee.getEmployeeSalary() != null && employee.getEmployeeName() != null)
                .sorted((e1, e2) -> e2.getEmployeeSalary().compareTo(e1.getEmployeeSalary()))
                .limit(10)
                .map(Employee::getEmployeeName)
                .toList();

        return ResponseEntity.ok(topTenNames);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        if (employeeInput == null) {
            logger.warn("Employee input is null");
            return ResponseEntity.badRequest().build();
        }

        // Basic validation
        if (employeeInput.getName() == null || employeeInput.getName().trim().isEmpty()) {
            logger.warn("Employee name is null or empty");
            return ResponseEntity.badRequest().build();
        }

        if (employeeInput.getSalary() == null || employeeInput.getSalary() <= 0) {
            logger.warn("Employee salary is null or invalid");
            return ResponseEntity.badRequest().build();
        }

        if (employeeInput.getAge() == null || employeeInput.getAge() < 16 || employeeInput.getAge() > 75) {
            logger.warn("Employee age is null or out of range (16-75)");
            return ResponseEntity.badRequest().build();
        }

        if (employeeInput.getTitle() == null || employeeInput.getTitle().trim().isEmpty()) {
            logger.warn("Employee title is null or empty");
            return ResponseEntity.badRequest().build();
        }

        return employeeServiceApi.createEmployee(employeeInput);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("Employee ID is null or empty for deletion");
            return ResponseEntity.badRequest().build();
        }

        return employeeServiceApi.deleteEmployeeById(id);
    }
}
