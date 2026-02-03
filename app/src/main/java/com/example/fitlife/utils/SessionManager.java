package com.example.fitlife.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session using SharedPreferences
 * Stores and retrieves logged-in user information
 */
public class SessionManager {
    private static final String PREF_NAME = "FitLifeSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME_EMAIL = "usernameEmail";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(int userId, String usernameEmail) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME_EMAIL, usernameEmail);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsernameEmail() {
        return pref.getString(KEY_USERNAME_EMAIL, null);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }
}
