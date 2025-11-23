package com.reliaquest.api.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmployeeValidationTest {

    @Test
    void isValidNameSearch_ValidNames() {
        assertTrue(EmployeeValidation.isValidNameSearch("John"));
        assertTrue(EmployeeValidation.isValidNameSearch("Jane Doe"));
        assertTrue(EmployeeValidation.isValidNameSearch("Mary Jane Watson"));
        assertTrue(EmployeeValidation.isValidNameSearch("a"));
        assertTrue(EmployeeValidation.isValidNameSearch("A B C"));
    }

    @Test
    void isValidNameSearch_InvalidNames() {
        assertFalse(EmployeeValidation.isValidNameSearch("John123"));
        assertFalse(EmployeeValidation.isValidNameSearch("Jane@Doe"));
        assertFalse(EmployeeValidation.isValidNameSearch("Mary-Jane"));
        assertFalse(EmployeeValidation.isValidNameSearch("John.Smith"));
        assertFalse(EmployeeValidation.isValidNameSearch("Test_User"));
        assertFalse(EmployeeValidation.isValidNameSearch("User#1"));
        assertFalse(EmployeeValidation.isValidNameSearch("Name$"));
    }

    @Test
    void isValidNameSearch_NullAndEmpty() {
        assertFalse(EmployeeValidation.isValidNameSearch(null));
        assertFalse(EmployeeValidation.isValidNameSearch(""));
        assertFalse(EmployeeValidation.isValidNameSearch("   "));
    }

    @Test
    void isValidNameSearch_SpecialCases() {
        assertTrue(EmployeeValidation.isValidNameSearch("John  Doe")); // Multiple spaces (allowed)
        assertTrue(EmployeeValidation.isValidNameSearch("John Doe")); // Single space
        assertTrue(EmployeeValidation.isValidNameSearch(" John")); // Leading space (trimmed)
        assertTrue(EmployeeValidation.isValidNameSearch("John ")); // Trailing space (trimmed)
        assertTrue(EmployeeValidation.isValidNameSearch("  John Doe  ")); // Leading and trailing spaces (trimmed)
    }
}
