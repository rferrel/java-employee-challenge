package com.reliaquest.api.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UUIDValidatorTest {

    private final UUIDValidator validator = new UUIDValidator();

    @Test
    void isValid_ValidUUID_ReturnsTrue() {
        assertTrue(validator.isValid("123e4567-e89b-12d3-a456-426614174000", null));
    }

    @Test
    void isValid_InvalidUUID_ReturnsFalse() {
        assertFalse(validator.isValid("test-id", null));
    }

    @Test
    void isValid_NullValue_ReturnsFalse() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void isValid_EmptyValue_ReturnsFalse() {
        assertFalse(validator.isValid("", null));
    }
}
