package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.User;
import com.example.fitlife.models.UserPreferences;
import com.example.fitlife.utils.ErrorHandler;

public class AuthController {
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private Context context;
    
    private static final String PREF_NAME = "FitLifePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private static User currentUser = null;
    
    public AuthController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Load current user if logged in
        if (isLoggedIn()) {
            loadCurrentUser();
        }
    }
    
    public boolean register(String username, String email, String password) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return false;
        }
        
        // Check if user already exists
        if (userExists(username, email)) {
            return false;
        }
        
        String passwordHash = hashPassword(password);
        if (passwordHash == null) {
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            // Insert user
            ContentValues userValues = new ContentValues();
            userValues.put("username", username);
            userValues.put("email", email);
            userValues.put("password_hash", passwordHash);
            
            long userId = db.insert("users", null, userValues);
            
            if (userId != -1) {
                // Create default user preferences
                ContentValues prefValues = new ContentValues();
                prefValues.put("user_id", userId);
                prefValues.put("theme_preference", "light");
                prefValues.put("notification_enabled", true);
                prefValues.put("default_rest_time", 60);
                prefValues.put("preferred_units", "metric");
                prefValues.put("shake_sensitivity", 2);
                
                long prefId = db.insert("user_preferences", null, prefValues);
                
                if (prefId != -1) {
                    db.setTransactionSuccessful();
                    
                    // Create user object and set as current user
                    User user = new User(username, email, passwordHash);
                    user.setUserId((int) userId);
                    user.setCreatedAt(new Date());
                    
                    saveUserSession(user);
                    currentUser = user;
                    
                    return true;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseError(context, (SQLException) e, "user registration");
        } finally {
            db.endTransaction();
            db.close();
        }
        
        return false;
    }
    
    public boolean login(String usernameOrEmail, String password) {
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            return false;
        }
        
        String passwordHash = hashPassword(password);
        if (passwordHash == null) {
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "(username = ? OR email = ?) AND password_hash = ?";
        String[] selectionArgs = {usernameOrEmail, usernameOrEmail, passwordHash};
        
        Cursor cursor = db.query("users", null, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
            
            cursor.close();
            db.close();
            
            saveUserSession(user);
            currentUser = user;
            
            return true;
        }
        
        if (cursor != null) cursor.close();
        db.close();
        
        return false;
    }
    
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        currentUser = null;
    }
    
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public boolean changePassword(String currentPassword, String newPassword) {
        if (currentUser == null || currentPassword.isEmpty() || newPassword.isEmpty()) {
            return false;
        }
        
        // Verify current password
        String currentPasswordHash = hashPassword(currentPassword);
        if (!currentPasswordHash.equals(currentUser.getPasswordHash())) {
            return false;
        }
        
        // Update password
        String newPasswordHash = hashPassword(newPassword);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("password_hash", newPasswordHash);
        
        int rowsAffected = db.update("users", values, "user_id = ?", 
            new String[]{String.valueOf(currentUser.getUserId())});
        
        db.close();
        
        if (rowsAffected > 0) {
            currentUser.setPasswordHash(newPasswordHash);
            return true;
        }
        
        return false;
    }
    
    public boolean updateUsername(String newUsername) {
        if (currentUser == null || newUsername.isEmpty()) {
            return false;
        }
        
        // Check if username already exists
        if (usernameExists(newUsername)) {
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        
        int rowsAffected = db.update("users", values, "user_id = ?", 
            new String[]{String.valueOf(currentUser.getUserId())});
        
        db.close();
        
        if (rowsAffected > 0) {
            currentUser.setUsername(newUsername);
            
            // Update shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, newUsername);
            editor.apply();
            
            return true;
        }
        
        return false;
    }
    
    private boolean userExists(String username, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "username = ? OR email = ?";
        String[] selectionArgs = {username, email};
        
        Cursor cursor = db.query("users", new String[]{"user_id"}, selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) cursor.close();
        db.close();
        
        return exists;
    }
    
    private boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "username = ?";
        String[] selectionArgs = {username};
        
        Cursor cursor = db.query("users", new String[]{"user_id"}, selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) cursor.close();
        db.close();
        
        return exists;
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    private void loadCurrentUser() {
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        if (userId != -1) {
            currentUser = new User();
            currentUser.setUserId(userId);
            currentUser.setUsername(sharedPreferences.getString(KEY_USERNAME, ""));
            currentUser.setEmail(sharedPreferences.getString(KEY_EMAIL, ""));
        }
    }
}