package com.reliaquest.api.util;

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
}
