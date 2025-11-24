package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeServiceApi;
import com.reliaquest.api.util.EmployeeValidation;
import com.reliaquest.api.util.EmployeeValidation.ValidUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
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
        // No validation required - this endpoint accepts no parameters and always returns all employees
        return employeeServiceApi.getAllEmployees();
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
                @ApiResponse(responseCode = "400", description = "Invalid employee ID format"),
                @ApiResponse(responseCode = "404", description = "Employee not found"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<Employee> getEmployeeById(
            @Parameter(description = "Employee ID", example = "123e4567-e89b-12d3-a456-426614174000") @ValidUUID
                    String id) {
        return employeeServiceApi.getEmployeeById(id);
    }

    @Override
    @Operation(summary = "Get highest salary", description = "Retrieves the highest salary among all employees")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved highest salary"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return employeeServiceApi.getHighestSalaryOfEmployees();
    }

    @Override
    @Operation(
            summary = "Get top 10 highest earning employees",
            description = "Retrieves the names of the top 10 highest earning employees")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved top 10 employees"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return employeeServiceApi.getTopTenHighestEarningEmployeeNames();
    }

    @Override
    @Operation(summary = "Create employee", description = "Creates a new employee in the system")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully created employee"),
                @ApiResponse(responseCode = "400", description = "Invalid employee input - validation failed"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<Employee> createEmployee(@Valid EmployeeInput employeeInput) {
        return employeeServiceApi.createEmployee(employeeInput);
    }

    @Override
    @Operation(summary = "Delete employee by ID", description = "Deletes an employee by their unique ID")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully deleted employee"),
                @ApiResponse(responseCode = "400", description = "Invalid employee ID format"),
                @ApiResponse(responseCode = "404", description = "Employee not found"),
                @ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
            })
    public ResponseEntity<String> deleteEmployeeById(@ValidUUID String id) {
        return employeeServiceApi.deleteEmployeeById(id);
    }
}
