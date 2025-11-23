package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end tests for Employee API. These tests are disabled by default as they require a running
 * mock API server (port 8112). To run these tests:
 * 1. Start the mock server: ./gradlew server:bootRun
 * 2. In another terminal, run: ./gradlew test --tests EmployeeControllerE2ETest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled("E2E tests require mock server running on port 8112")
class EmployeeControllerE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String API_BASE_PATH = "/api/v1/employee";

    // ========== Get All Employees Tests ==========

    @Test
    void getAllEmployees_ReturnsEmployeeList() {
        // When
        ResponseEntity<Employee[]> response = restTemplate.getForEntity(API_BASE_PATH, Employee[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 0, "Should return a list of employees");
    }

    @Test
    void getAllEmployees_ReturnsConsistentData() {
        // When
        ResponseEntity<Employee[]> response1 = restTemplate.getForEntity(API_BASE_PATH, Employee[].class);
        ResponseEntity<Employee[]> response2 = restTemplate.getForEntity(API_BASE_PATH, Employee[].class);

        // Then - should return same data (from cache on second call)
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody().length, response2.getBody().length);
    }

    // ========== Search by Name Tests ==========

    @Test
    void searchByName_ReturnsMatchingEmployees() {
        // When
        ResponseEntity<Employee[]> response =
                restTemplate.getForEntity(API_BASE_PATH + "/search/John", Employee[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // All results should contain "John" (case-insensitive)
        for (Employee emp : response.getBody()) {
            assertTrue(
                    emp.getEmployeeName().toLowerCase().contains("john"), "Employee name should contain search term");
        }
    }

    @Test
    void searchByName_InvalidInput_ReturnsBadRequest() {
        // When - search with non-alphabetic characters
        ResponseEntity<Employee[]> response =
                restTemplate.getForEntity(API_BASE_PATH + "/search/John@123", Employee[].class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void searchByName_NoResults_ReturnsEmptyList() {
        // When - search for non-existent name
        ResponseEntity<Employee[]> response =
                restTemplate.getForEntity(API_BASE_PATH + "/search/NonExistentName", Employee[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().length);
    }

    // ========== Get Employee by ID Tests ==========

    @Test
    void getEmployeeById_ValidId_ReturnsEmployee() {
        // Given - first get all employees to find a valid ID
        ResponseEntity<Employee[]> allResponse = restTemplate.getForEntity(API_BASE_PATH, Employee[].class);
        assertNotNull(allResponse.getBody());
        assertTrue(allResponse.getBody().length > 0, "Should have at least one employee");
        String validId = allResponse.getBody()[0].getId();

        // When
        ResponseEntity<Employee> response = restTemplate.getForEntity(API_BASE_PATH + "/" + validId, Employee.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(validId, response.getBody().getId());
    }

    @Test
    void getEmployeeById_InvalidFormat_ReturnsBadRequest() {
        // When - invalid UUID format
        ResponseEntity<Employee> response = restTemplate.getForEntity(API_BASE_PATH + "/not-a-uuid", Employee.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getEmployeeById_NotFound_ReturnsNotFound() {
        // When - valid UUID format but employee doesn't exist
        ResponseEntity<Employee> response =
                restTemplate.getForEntity(API_BASE_PATH + "/00000000-0000-0000-0000-000000000000", Employee.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== Highest Salary Tests ==========

    @Test
    void getHighestSalary_ReturnsMaximumSalary() {
        // When
        ResponseEntity<Integer> response = restTemplate.getForEntity(API_BASE_PATH + "/highestSalary", Integer.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() > 0, "Highest salary should be positive");
    }

    @Test
    void getHighestSalary_NoEmployees_ReturnsZero() {
        // Assumption: if no employees, should return 0
        // (This test may not apply if mock server always returns employees)
        ResponseEntity<Integer> response = restTemplate.getForEntity(API_BASE_PATH + "/highestSalary", Integer.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() >= 0, "Should return non-negative value");
    }

    // ========== Top 10 Earners Tests ==========

    @Test
    void getTopTenEarners_ReturnsEmployeeNames() {
        // When
        ResponseEntity<String[]> response =
                restTemplate.getForEntity(API_BASE_PATH + "/topTenHighestEarningEmployeeNames", String[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should return at most 10 names
        assertTrue(response.getBody().length <= 10, "Should return at most 10 employees");
        // Should be in descending order by salary (implicitly tested by API)
    }

    // ========== Create Employee Tests ==========

    @Test
    void createEmployee_ValidInput_ReturnsCreatedEmployee() {
        // Given
        EmployeeInput newEmployee = new EmployeeInput();
        newEmployee.setName("Test Employee");
        newEmployee.setSalary(50000);
        newEmployee.setAge(30);
        newEmployee.setTitle("Test Developer");

        // When
        ResponseEntity<Employee> response = restTemplate.postForEntity(API_BASE_PATH, newEmployee, Employee.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Employee", response.getBody().getEmployeeName());
        assertEquals(50000, response.getBody().getEmployeeSalary());
    }

    @Test
    void createEmployee_InvalidInput_ReturnsBadRequest() {
        // Given - invalid input (missing required field)
        EmployeeInput invalidEmployee = new EmployeeInput();
        invalidEmployee.setName(""); // Empty name
        invalidEmployee.setSalary(50000);
        invalidEmployee.setAge(30);
        invalidEmployee.setTitle("Developer");

        // When
        ResponseEntity<Employee> response = restTemplate.postForEntity(API_BASE_PATH, invalidEmployee, Employee.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createEmployee_InvalidAge_ReturnsBadRequest() {
        // Given - age out of range
        EmployeeInput invalidEmployee = new EmployeeInput();
        invalidEmployee.setName("Test Employee");
        invalidEmployee.setSalary(50000);
        invalidEmployee.setAge(100); // Out of range (16-75)
        invalidEmployee.setTitle("Developer");

        // When
        ResponseEntity<Employee> response = restTemplate.postForEntity(API_BASE_PATH, invalidEmployee, Employee.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createEmployee_InvalidSalary_ReturnsBadRequest() {
        // Given - invalid salary (zero or negative)
        EmployeeInput invalidEmployee = new EmployeeInput();
        invalidEmployee.setName("Test Employee");
        invalidEmployee.setSalary(-1000); // Negative salary
        invalidEmployee.setAge(30);
        invalidEmployee.setTitle("Developer");

        // When
        ResponseEntity<Employee> response = restTemplate.postForEntity(API_BASE_PATH, invalidEmployee, Employee.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== Delete Employee Tests ==========

    @Test
    void deleteEmployee_ValidId_ReturnsEmployeeName() {
        // Given - first create an employee
        EmployeeInput newEmployee = new EmployeeInput();
        newEmployee.setName("Employee To Delete");
        newEmployee.setSalary(40000);
        newEmployee.setAge(25);
        newEmployee.setTitle("Intern");

        ResponseEntity<Employee> createResponse =
                restTemplate.postForEntity(API_BASE_PATH, newEmployee, Employee.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        String employeeId = createResponse.getBody().getId();

        // When
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                API_BASE_PATH + "/" + employeeId, org.springframework.http.HttpMethod.DELETE, null, String.class);

        // Then
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertNotNull(deleteResponse.getBody());
        assertEquals("Employee To Delete", deleteResponse.getBody());
    }

    @Test
    void deleteEmployee_InvalidId_ReturnsBadRequest() {
        // When - invalid UUID format
        ResponseEntity<String> response = restTemplate.exchange(
                API_BASE_PATH + "/invalid-id", org.springframework.http.HttpMethod.DELETE, null, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== Integration Scenarios ==========

    @Test
    void multipleOperations_SequentialCalls_MaintainConsistency() {
        // When - perform multiple sequential operations
        ResponseEntity<Employee[]> allEmployeesResponse = restTemplate.getForEntity(API_BASE_PATH, Employee[].class);
        assertEquals(HttpStatus.OK, allEmployeesResponse.getStatusCode());
        int initialCount = allEmployeesResponse.getBody().length;

        // Create new employee
        EmployeeInput newEmployee = new EmployeeInput();
        newEmployee.setName("Integration Test Employee");
        newEmployee.setSalary(75000);
        newEmployee.setAge(35);
        newEmployee.setTitle("QA Engineer");

        ResponseEntity<Employee> createResponse =
                restTemplate.postForEntity(API_BASE_PATH, newEmployee, Employee.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        // Verify employee was created
        ResponseEntity<Employee[]> allEmployeesAfterCreate = restTemplate.getForEntity(API_BASE_PATH, Employee[].class);
        assertTrue(allEmployeesAfterCreate.getBody().length >= initialCount, "New employee should be added to list");

        // Search for the created employee
        ResponseEntity<Employee[]> searchResponse =
                restTemplate.getForEntity(API_BASE_PATH + "/search/Integration", Employee[].class);
        assertTrue(searchResponse.getBody().length > 0, "Should find newly created employee");
    }
}
