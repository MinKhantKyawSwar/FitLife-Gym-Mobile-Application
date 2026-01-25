package com.example.fitlife.utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    
    public static void applyTheme(Context context, String theme) {
        if (THEME_DARK.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    public static void applyTheme(String theme) {
        if (THEME_DARK.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    public static String getCurrentTheme() {
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        return currentNightMode == AppCompatDelegate.MODE_NIGHT_YES ? THEME_DARK : THEME_LIGHT;
    }
    
    public static boolean isDarkTheme() {
        return THEME_DARK.equals(getCurrentTheme());
    }
}