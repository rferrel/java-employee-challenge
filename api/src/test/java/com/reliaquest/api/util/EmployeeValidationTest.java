package com.reliaquest.api.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmployeeValidationTest {

    // ========== isValidNameSearch Tests ==========

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

    // ========== isValidUUID Tests ==========

    @Test
    void isValidUUID_ValidUUIDs() {
        assertTrue(EmployeeValidation.isValidUUID("123e4567-e89b-12d3-a456-426614174000"));
        assertTrue(EmployeeValidation.isValidUUID("00000000-0000-0000-0000-000000000000"));
        assertTrue(EmployeeValidation.isValidUUID("ffffffff-ffff-ffff-ffff-ffffffffffff"));
        assertTrue(EmployeeValidation.isValidUUID("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void isValidUUID_InvalidUUIDs() {
        assertFalse(EmployeeValidation.isValidUUID("test-id"));
        assertFalse(EmployeeValidation.isValidUUID("123456789"));
        assertFalse(EmployeeValidation.isValidUUID("not-a-uuid-format"));
        assertFalse(EmployeeValidation.isValidUUID("123e4567-e89b-12d3-a456")); // Incomplete
        assertFalse(EmployeeValidation.isValidUUID("123e4567-e89b-12d3-a456-426614174000-extra")); // Extra characters
        assertFalse(EmployeeValidation.isValidUUID("123E4567-E89B-12D3-A456-426614174000G")); // Invalid character
    }

    @Test
    void isValidUUID_NullAndEmpty() {
        assertFalse(EmployeeValidation.isValidUUID(null));
        assertFalse(EmployeeValidation.isValidUUID(""));
        assertFalse(EmployeeValidation.isValidUUID("   "));
    }

    @Test
    void isValidUUID_SpecialCases() {
        // Whitespace should be trimmed, but still invalid UUID format after trim
        assertFalse(EmployeeValidation.isValidUUID("  123456  "));
    }

    // ========== ValidUUID Annotation Validator Tests ==========

    @Test
    void validUUIDValidator_ValidUUID_ReturnsTrue() {
        EmployeeValidation.UUIDValidatorImpl validator = new EmployeeValidation.UUIDValidatorImpl();
        assertTrue(validator.isValid("123e4567-e89b-12d3-a456-426614174000", null));
    }

    @Test
    void validUUIDValidator_InvalidUUID_ReturnsFalse() {
        EmployeeValidation.UUIDValidatorImpl validator = new EmployeeValidation.UUIDValidatorImpl();
        assertFalse(validator.isValid("invalid-uuid", null));
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid(null, null));
    }
}
