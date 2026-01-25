package com.example.fitlife.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

public class ValidationHelper {
    
    // Username validation
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    
    // Password validation
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 50;
    
    // Routine name validation
    private static final int MIN_ROUTINE_NAME_LENGTH = 3;
    private static final int MAX_ROUTINE_NAME_LENGTH = 50;
    
    // Exercise name validation
    private static final int MIN_EXERCISE_NAME_LENGTH = 2;
    private static final int MAX_EXERCISE_NAME_LENGTH = 50;
    
    public static class ValidationResult {
        private boolean isValid;
        private String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() { return isValid; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    // Validate username
    public static ValidationResult validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return new ValidationResult(false, "Username is required");
        }
        
        if (username.length() < 3) {
            return new ValidationResult(false, "Username must be at least 3 characters");
        }
        
        if (username.length() > 20) {
            return new ValidationResult(false, "Username must be less than 20 characters");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, "Username can only contain letters, numbers, hyphens, and underscores");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate email
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false, "Email is required");
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate password
    public static ValidationResult validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return new ValidationResult(false, "Password is required");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return new ValidationResult(false, "Password must be less than " + MAX_PASSWORD_LENGTH + " characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate password confirmation
    public static ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        ValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            return new ValidationResult(false, "Password confirmation is required");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate routine name
    public static ValidationResult validateRoutineName(String routineName) {
        if (TextUtils.isEmpty(routineName)) {
            return new ValidationResult(false, "Routine name is required");
        }
        
        String trimmed = routineName.trim();
        if (trimmed.length() < MIN_ROUTINE_NAME_LENGTH) {
            return new ValidationResult(false, "Routine name must be at least " + MIN_ROUTINE_NAME_LENGTH + " characters");
        }
        
        if (trimmed.length() > MAX_ROUTINE_NAME_LENGTH) {
            return new ValidationResult(false, "Routine name must be less than " + MAX_ROUTINE_NAME_LENGTH + " characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate exercise name
    public static ValidationResult validateExerciseName(String exerciseName) {
        if (TextUtils.isEmpty(exerciseName)) {
            return new ValidationResult(false, "Exercise name is required");
        }
        
        String trimmed = exerciseName.trim();
        if (trimmed.length() < MIN_EXERCISE_NAME_LENGTH) {
            return new ValidationResult(false, "Exercise name must be at least " + MIN_EXERCISE_NAME_LENGTH + " characters");
        }
        
        if (trimmed.length() > MAX_EXERCISE_NAME_LENGTH) {
            return new ValidationResult(false, "Exercise name must be less than " + MAX_EXERCISE_NAME_LENGTH + " characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate phone number
    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return new ValidationResult(false, "Phone number is required");
        }
        
        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^\\d]", "");
        
        if (digitsOnly.length() < 10) {
            return new ValidationResult(false, "Phone number must have at least 10 digits");
        }
        
        if (digitsOnly.length() > 15) {
            return new ValidationResult(false, "Phone number must have less than 15 digits");
        }
        
        return new ValidationResult(true, null);
    }
    
    // Validate positive integer
    public static ValidationResult validatePositiveInteger(String value, String fieldName) {
        if (TextUtils.isEmpty(value)) {
            return new ValidationResult(false, fieldName + " is required");
        }
        
        try {
            int intValue = Integer.parseInt(value);
            if (intValue <= 0) {
                return new ValidationResult(false, fieldName + " must be greater than 0");
            }
            return new ValidationResult(true, null);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, fieldName + " must be a valid number");
        }
    }
    
    // Validate sets and reps range
    public static ValidationResult validateSetsReps(String value, String fieldName) {
        ValidationResult intResult = validatePositiveInteger(value, fieldName);
        if (!intResult.isValid()) {
            return intResult;
        }
        
        try {
            int intValue = Integer.parseInt(value);
            if (intValue > 100) {
                return new ValidationResult(false, fieldName + " cannot exceed 100");
            }
            return new ValidationResult(true, null);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, fieldName + " must be a valid number");
        }
    }
    
    // Helper method to set error on EditText
    public static boolean validateAndSetError(EditText editText, ValidationResult result) {
        if (result.isValid()) {
            editText.setError(null);
            return true;
        } else {
            editText.setError(result.getErrorMessage());
            return false;
        }
    }
    
    // Validate and focus on first error
    public static boolean validateForm(EditText... editTexts) {
        boolean allValid = true;
        EditText firstErrorField = null;
        
        for (EditText editText : editTexts) {
            if (editText.getError() != null) {
                allValid = false;
                if (firstErrorField == null) {
                    firstErrorField = editText;
                }
            }
        }
        
        if (!allValid && firstErrorField != null) {
            firstErrorField.requestFocus();
        }
        
        return allValid;
    }
    
    // Sanitize input (remove excessive whitespace)
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }
    
    // Check if string is safe for SQL (basic check)
    public static boolean isSqlSafe(String input) {
        if (input == null) {
            return true;
        }
        
        String lowerInput = input.toLowerCase();
        String[] sqlKeywords = {"select", "insert", "update", "delete", "drop", "create", "alter", "exec", "union"};
        
        for (String keyword : sqlKeywords) {
            if (lowerInput.contains(keyword)) {
                return false;
            }
        }
        
        return true;
    }
}