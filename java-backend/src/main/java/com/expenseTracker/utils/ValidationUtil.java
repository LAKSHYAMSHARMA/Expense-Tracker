package com.expenseTracker.utils;

import com.expenseTracker.exception.BusinessException;
import java.util.regex.Pattern;

/**
 * Input validation and sanitization utility
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern ALPHANUMERIC_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9\\s\\-_]*$");
    
    private static final int MAX_STRING_LENGTH = 1000;
    private static final int MIN_STRING_LENGTH = 1;

    /**
     * Validate and sanitize string input
     */
    public static String sanitizeString(String input, String fieldName, int minLength, int maxLength) {
        if (input == null) {
            throw new BusinessException(fieldName + " cannot be null");
        }
        
        String trimmed = input.trim();
        
        if (trimmed.isEmpty()) {
            throw new BusinessException(fieldName + " cannot be empty");
        }
        
        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            throw new BusinessException(fieldName + " must be between " + minLength + " and " + maxLength + " characters");
        }
        
        return trimmed;
    }

    /**
     * Validate email format
     */
    public static void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new BusinessException("Email cannot be null or empty");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("Invalid email format");
        }
    }

    /**
     * Validate string contains only alphanumeric characters
     */
    public static void validateAlphanumeric(String input, String fieldName) {
        if (input != null && !input.isEmpty() && !ALPHANUMERIC_PATTERN.matcher(input).matches()) {
            throw new BusinessException(fieldName + " contains invalid characters");
        }
    }

    /**
     * Validate positive integer
     */
    public static void validatePositiveInteger(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BusinessException(fieldName + " must be a positive integer");
        }
    }

    /**
     * Validate year format
     */
    public static void validateYear(int year) {
        int currentYear = java.time.Year.now().getValue();
        if (year < 1900 || year > currentYear) {
            throw new BusinessException("Year must be between 1900 and " + currentYear);
        }
    }

    /**
     * Validate month format
     */
    public static void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new BusinessException("Month must be between 1 and 12");
        }
    }

    /**
     * Prevent XSS attacks by removing HTML tags
     */
    public static String stripHtmlTags(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]*>", "");
    }
}
