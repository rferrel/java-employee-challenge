package com.reliaquest.api.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

public class EmployeeValidation {

    private EmployeeValidation() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that a name search string contains only alphabetic characters and spaces.
     *
     * @param searchString the search string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidNameSearch(String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return false;
        }
        // Trim whitespace and allow only letters (a-z, A-Z) and spaces
        String trimmed = searchString.trim();
        return trimmed.matches("^[a-zA-Z\\s]+$");
    }

    /**
     * Validates that a string is a valid UUID format.
     *
     * @param value the string to validate
     * @return true if valid UUID, false otherwise
     */
    public static boolean isValidUUID(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Custom validation annotation for UUID format.
     */
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UUIDValidatorImpl.class)
    public @interface ValidUUID {
        String message() default "Invalid UUID format";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Implementation of UUID validation constraint.
     */
    public static class UUIDValidatorImpl implements ConstraintValidator<ValidUUID, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return EmployeeValidation.isValidUUID(value);
        }
    }
}
