package com.example.fitlife.utils;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;
import android.widget.Toast;

public class ErrorHandler {
    
    private static final String TAG = "FitLifeError";
    
    public enum ErrorType {
        DATABASE_ERROR,
        NETWORK_ERROR,
        VALIDATION_ERROR,
        PERMISSION_ERROR,
        FILE_ERROR,
        UNKNOWN_ERROR
    }
    
    public static class AppError {
        private ErrorType type;
        private String message;
        private String userMessage;
        private Throwable cause;
        
        public AppError(ErrorType type, String message, String userMessage, Throwable cause) {
            this.type = type;
            this.message = message;
            this.userMessage = userMessage;
            this.cause = cause;
        }
        
        public ErrorType getType() { return type; }
        public String getMessage() { return message; }
        public String getUserMessage() { return userMessage; }
        public Throwable getCause() { return cause; }
    }
    
    // Handle database errors
    public static void handleDatabaseError(Context context, SQLException e, String operation) {
        String errorMessage = "Database error during " + operation + ": " + e.getMessage();
        String userMessage = "Unable to save data. Please try again.";
        
        AppError error = new AppError(ErrorType.DATABASE_ERROR, errorMessage, userMessage, e);
        logError(error);
        showUserError(context, error);
    }
    
    // Handle general exceptions
    public static void handleGeneralError(Context context, Exception e, String operation) {
        String errorMessage = "Error during " + operation + ": " + e.getMessage();
        String userMessage = "Something went wrong. Please try again.";
        
        AppError error = new AppError(ErrorType.UNKNOWN_ERROR, errorMessage, userMessage, e);
        logError(error);
        showUserError(context, error);
    }
    
    // Handle validation errors
    public static void handleValidationError(Context context, String validationMessage) {
        AppError error = new AppError(ErrorType.VALIDATION_ERROR, validationMessage, validationMessage, null);
        logError(error);
        showUserError(context, error);
    }
    
    // Handle permission errors
    public static void handlePermissionError(Context context, String permission, String feature) {
        String errorMessage = "Permission denied: " + permission + " for feature: " + feature;
        String userMessage = "Permission required for " + feature + ". Please grant permission in settings.";
        
        AppError error = new AppError(ErrorType.PERMISSION_ERROR, errorMessage, userMessage, null);
        logError(error);
        showUserError(context, error);
    }
    
    // Handle SMS errors
    public static void handleSMSError(Context context, Exception e) {
        String errorMessage = "SMS sending failed: " + e.getMessage();
        String userMessage = "Failed to send SMS. Please check your network connection and try again.";
        
        AppError error = new AppError(ErrorType.NETWORK_ERROR, errorMessage, userMessage, e);
        logError(error);
        showUserError(context, error);
    }
    
    // Handle file I/O errors
    public static void handleFileError(Context context, Exception e, String operation) {
        String errorMessage = "File operation failed during " + operation + ": " + e.getMessage();
        String userMessage = "Unable to access file. Please check storage permissions.";
        
        AppError error = new AppError(ErrorType.FILE_ERROR, errorMessage, userMessage, e);
        logError(error);
        showUserError(context, error);
    }
    
    // Log error for debugging
    private static void logError(AppError error) {
        Log.e(TAG, "[" + error.getType() + "] " + error.getMessage());
        if (error.getCause() != null) {
            Log.e(TAG, "Caused by: ", error.getCause());
        }
    }
    
    // Show user-friendly error message
    private static void showUserError(Context context, AppError error) {
        if (context != null && error.getUserMessage() != null) {
            Toast.makeText(context, error.getUserMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    // Create safe error message for user display
    public static String getSafeErrorMessage(Exception e, String defaultMessage) {
        if (e == null) {
            return defaultMessage;
        }
        
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return defaultMessage;
        }
        
        // Remove technical details that might confuse users
        message = message.replaceAll("java\\..*Exception:", "");
        message = message.replaceAll("android\\.database\\..*:", "");
        message = message.trim();
        
        return message.isEmpty() ? defaultMessage : message;
    }
    
    // Check if error is recoverable
    public static boolean isRecoverableError(Exception e) {
        if (e instanceof SQLException) {
            return true; // Database errors are often recoverable
        }
        
        if (e instanceof SecurityException) {
            return true; // Permission errors can be fixed by user
        }
        
        return false;
    }
    
    // Handle routine creation errors
    public static void handleRoutineCreationError(Context context, Exception e) {
        String userMessage = "Failed to create routine. Please check your input and try again.";
        handleGeneralError(context, e, "routine creation");
    }
    
    // Handle workout completion errors
    public static void handleWorkoutCompletionError(Context context, Exception e) {
        String userMessage = "Failed to mark workout as completed. Please try again.";
        handleGeneralError(context, e, "workout completion");
    }
    
    // Handle authentication errors
    public static void handleAuthenticationError(Context context, String operation) {
        String errorMessage = "Authentication failed during " + operation;
        String userMessage = "Login failed. Please check your credentials and try again.";
        
        AppError error = new AppError(ErrorType.VALIDATION_ERROR, errorMessage, userMessage, null);
        logError(error);
        showUserError(context, error);
    }
    
    // Handle network connectivity issues
    public static void handleNetworkError(Context context, String operation) {
        String errorMessage = "Network error during " + operation;
        String userMessage = "Network connection required. Please check your internet connection.";
        
        AppError error = new AppError(ErrorType.NETWORK_ERROR, errorMessage, userMessage, null);
        logError(error);
        showUserError(context, error);
    }
    
    // Wrap dangerous operations with error handling
    public static void safeExecute(Context context, String operationName, RiskyOperation operation) {
        try {
            operation.execute();
        } catch (SQLException e) {
            handleDatabaseError(context, e, operationName);
        } catch (SecurityException e) {
            handlePermissionError(context, "Unknown permission", operationName);
        } catch (Exception e) {
            handleGeneralError(context, e, operationName);
        }
    }
    
    @FunctionalInterface
    public interface RiskyOperation {
        void execute() throws Exception;
    }
}