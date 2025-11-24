package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmployeeInput {
    @NotBlank(message = "Employee name cannot be null or empty")
    private String name;

    @NotNull(message = "Employee salary cannot be null") @Min(value = 1, message = "Employee salary must be greater than 0")
    private Integer salary;

    @NotNull(message = "Employee age cannot be null") @Min(value = 16, message = "Employee age must be at least 16")
    @Max(value = 75, message = "Employee age must be at most 75")
    private Integer age;

    @NotBlank(message = "Employee title cannot be null or empty")
    private String title;

    // Constructors
    public EmployeeInput() {}

    public EmployeeInput(String name, Integer salary, Integer age, String title) {
        this.name = name;
        this.salary = salary;
        this.age = age;
        this.title = title;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
