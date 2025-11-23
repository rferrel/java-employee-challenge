package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Employee {
    private String id;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_salary")
    private Integer employeeSalary;

    @JsonProperty("employee_age")
    private Integer employeeAge;

    @JsonProperty("employee_title")
    private String employeeTitle;

    @JsonProperty("employee_email")
    private String employeeEmail;

    // Constructors
    public Employee() {}

    public Employee(
            String id,
            String employeeName,
            Integer employeeSalary,
            Integer employeeAge,
            String employeeTitle,
            String employeeEmail) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeSalary = employeeSalary;
        this.employeeAge = employeeAge;
        this.employeeTitle = employeeTitle;
        this.employeeEmail = employeeEmail;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getEmployeeSalary() {
        return employeeSalary;
    }

    public void setEmployeeSalary(Integer employeeSalary) {
        this.employeeSalary = employeeSalary;
    }

    public Integer getEmployeeAge() {
        return employeeAge;
    }

    public void setEmployeeAge(Integer employeeAge) {
        this.employeeAge = employeeAge;
    }

    public String getEmployeeTitle() {
        return employeeTitle;
    }

    public void setEmployeeTitle(String employeeTitle) {
        this.employeeTitle = employeeTitle;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }
}
