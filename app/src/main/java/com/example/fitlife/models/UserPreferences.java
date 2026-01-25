package com.example.fitlife.models;

import java.io.Serializable;

public class UserPreferences implements Serializable {
    private int preferenceId;
    private int userId;
    private String themePreference; // light, dark
    private boolean notificationEnabled;
    private int defaultRestTime;
    private String preferredUnits; // metric, imperial
    private int shakeSensitivity; // 1=low, 2=medium, 3=high
    
    public UserPreferences() {
        this.themePreference = "light";
        this.notificationEnabled = true;
        this.defaultRestTime = 60;
        this.preferredUnits = "metric";
        this.shakeSensitivity = 2;
    }
    
    public UserPreferences(int userId) {
        this();
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getPreferenceId() { return preferenceId; }
    public void setPreferenceId(int preferenceId) { this.preferenceId = preferenceId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
    
    public boolean isNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    
    public int getDefaultRestTime() { return defaultRestTime; }
    public void setDefaultRestTime(int defaultRestTime) { this.defaultRestTime = defaultRestTime; }
    
    public String getPreferredUnits() { return preferredUnits; }
    public void setPreferredUnits(String preferredUnits) { this.preferredUnits = preferredUnits; }
    
    public int getShakeSensitivity() { return shakeSensitivity; }
    public void setShakeSensitivity(int shakeSensitivity) { this.shakeSensitivity = shakeSensitivity; }
}