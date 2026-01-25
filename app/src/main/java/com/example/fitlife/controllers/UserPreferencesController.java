package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.User;
import com.example.fitlife.models.UserPreferences;
import com.example.fitlife.utils.ThemeManager;

public class UserPreferencesController {
    private DatabaseHelper dbHelper;
    private Context context;
    
    public UserPreferencesController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }
    
    // Get user preferences
    public UserPreferences getUserPreferences(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query("user_preferences", null, selection, selectionArgs, null, null, null);
        
        UserPreferences preferences = null;
        if (cursor != null && cursor.moveToFirst()) {
            preferences = new UserPreferences();
            preferences.setPreferenceId(cursor.getInt(cursor.getColumnIndexOrThrow("preference_id")));
            preferences.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            preferences.setThemePreference(cursor.getString(cursor.getColumnIndexOrThrow("theme_preference")));
            preferences.setNotificationEnabled(cursor.getInt(cursor.getColumnIndexOrThrow("notification_enabled")) == 1);
            preferences.setDefaultRestTime(cursor.getInt(cursor.getColumnIndexOrThrow("default_rest_time")));
            preferences.setPreferredUnits(cursor.getString(cursor.getColumnIndexOrThrow("preferred_units")));
            preferences.setShakeSensitivity(cursor.getInt(cursor.getColumnIndexOrThrow("shake_sensitivity")));
            
            cursor.close();
        }
        
        db.close();
        
        // If no preferences found, create default ones
        if (preferences == null) {
            preferences = createDefaultPreferences(userId);
        }
        
        return preferences;
    }
    
    // Create default preferences for user
    private UserPreferences createDefaultPreferences(int userId) {
        UserPreferences preferences = new UserPreferences(userId);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("theme_preference", preferences.getThemePreference());
        values.put("notification_enabled", preferences.isNotificationEnabled());
        values.put("default_rest_time", preferences.getDefaultRestTime());
        values.put("preferred_units", preferences.getPreferredUnits());
        values.put("shake_sensitivity", preferences.getShakeSensitivity());
        
        long preferenceId = db.insert("user_preferences", null, values);
        db.close();
        
        if (preferenceId != -1) {
            preferences.setPreferenceId((int) preferenceId);
        }
        
        return preferences;
    }
    
    // Update theme preference
    public boolean updateThemePreference(int userId, String theme) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("theme_preference", theme);
        
        int rowsAffected = db.update("user_preferences", values, "user_id = ?", 
                                    new String[]{String.valueOf(userId)});
        db.close();
        
        if (rowsAffected > 0) {
            // Apply theme immediately
            ThemeManager.applyTheme(theme);
            return true;
        }
        
        return false;
    }
    
    // Toggle theme preference
    public boolean toggleTheme(int userId) {
        UserPreferences preferences = getUserPreferences(userId);
        String newTheme = ThemeManager.THEME_DARK.equals(preferences.getThemePreference()) ? 
            ThemeManager.THEME_LIGHT : ThemeManager.THEME_DARK;
        
        return updateThemePreference(userId, newTheme);
    }
    
    // Update notification setting
    public boolean updateNotificationEnabled(int userId, boolean enabled) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("notification_enabled", enabled);
        
        int rowsAffected = db.update("user_preferences", values, "user_id = ?", 
                                    new String[]{String.valueOf(userId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Update default rest time
    public boolean updateDefaultRestTime(int userId, int restTimeSeconds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("default_rest_time", restTimeSeconds);
        
        int rowsAffected = db.update("user_preferences", values, "user_id = ?", 
                                    new String[]{String.valueOf(userId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Update preferred units
    public boolean updatePreferredUnits(int userId, String units) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("preferred_units", units);
        
        int rowsAffected = db.update("user_preferences", values, "user_id = ?", 
                                    new String[]{String.valueOf(userId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Update shake sensitivity
    public boolean updateShakeSensitivity(int userId, int sensitivity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("shake_sensitivity", sensitivity);
        
        int rowsAffected = db.update("user_preferences", values, "user_id = ?", 
                                    new String[]{String.valueOf(userId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Apply user's saved theme
    public void applySavedTheme(int userId) {
        UserPreferences preferences = getUserPreferences(userId);
        ThemeManager.applyTheme(preferences.getThemePreference());
    }
    
    // Get current theme display name
    public String getCurrentThemeDisplayName(int userId) {
        UserPreferences preferences = getUserPreferences(userId);
        return ThemeManager.THEME_DARK.equals(preferences.getThemePreference()) ? "Dark" : "Light";
    }
}