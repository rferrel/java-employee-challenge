package com.reliaquest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmployeeInputTest {

    @Test
    void setName_SetsNameCorrectly() {
        // Given
        EmployeeInput input = new EmployeeInput();
        String name = "John Doe";

        // When
        input.setName(name);

        // Then
        assertEquals(name, input.getName());
    }

    @Test
    void setSalary_SetsSalaryCorrectly() {
        // Given
        EmployeeInput input = new EmployeeInput();
        Integer salary = 75000;

        // When
        input.setSalary(salary);

        // Then
        assertEquals(salary, input.getSalary());
    }

    @Test
    void setAge_SetsAgeCorrectly() {
        // Given
        EmployeeInput input = new EmployeeInput();
        Integer age = 30;

        // When
        input.setAge(age);

        // Then
        assertEquals(age, input.getAge());
    }

    @Test
    void setTitle_SetsTitleCorrectly() {
        // Given
        EmployeeInput input = new EmployeeInput();
        String title = "Software Engineer";

        // When
        input.setTitle(title);

        // Then
        assertEquals(title, input.getTitle());
    }
}
