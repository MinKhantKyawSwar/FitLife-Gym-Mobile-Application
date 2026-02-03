package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.example.fitlife.utils.SessionManager;

/**
 * Profile activity - displays user information and statistics
 * Uses hybrid calculation for stats (increment on actions, verify with queries)
 */
public class ProfileActivity extends AppCompatActivity {
    private TextView textUserName;
    private TextView textUserEmail;
    private TextView textAge;
    private TextView textGender;
    private TextView textHeight;
    private TextView textWeight;
    private TextView textTotalSessions;
    private TextView textTotalRoutines;
    private TextView textTotalExercises;
    private TextView textActiveDays;
    private MaterialButton buttonEdit;
    private MaterialButton buttonLogout;
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        loadUserData();
        loadStats();
    }

    private void initializeViews() {
        textUserName = findViewById(R.id.textUserName);
        textUserEmail = findViewById(R.id.textUserEmail);
        textAge = findViewById(R.id.textAge);
        textGender = findViewById(R.id.textGender);
        textHeight = findViewById(R.id.textHeight);
        textWeight = findViewById(R.id.textWeight);
        textTotalSessions = findViewById(R.id.textTotalSessions);
        textTotalRoutines = findViewById(R.id.textTotalRoutines);
        textTotalExercises = findViewById(R.id.textTotalExercises);
        textActiveDays = findViewById(R.id.textActiveDays);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonLogout = findViewById(R.id.buttonLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        buttonEdit.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            overridePendingTransition(0, 0);
        });

        buttonLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_my_workouts) {
                startActivity(new Intent(this, MyWorkoutsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_create) {
                startActivity(new Intent(this, CreateActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(this, WorkoutsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadUserData();
        loadStats();
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        Cursor accountCursor = dbHelper.getAccountInfo(userId);
        if (accountCursor != null && accountCursor.moveToFirst()) {
            String email = accountCursor.getString(accountCursor.getColumnIndexOrThrow("username_email"));
            int usernameIdx = accountCursor.getColumnIndex("username");
            String username = (usernameIdx >= 0) ? accountCursor.getString(usernameIdx) : null;
            textUserEmail.setText(email != null ? email : "");
            textUserName.setText((username != null && !username.isEmpty()) ? username : (email != null ? email : ""));
        } else {
            String usernameEmail = sessionManager.getUsernameEmail();
            textUserEmail.setText(usernameEmail);
            textUserName.setText(usernameEmail != null && usernameEmail.contains("@")
                    ? usernameEmail.substring(0, usernameEmail.indexOf("@")) : usernameEmail);
        }
        if (accountCursor != null) {
            accountCursor.close();
        }

        Cursor cursor = dbHelper.getUserDetails(userId);
        if (cursor.moveToFirst()) {
            int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
            double height = cursor.getDouble(cursor.getColumnIndexOrThrow("height"));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));

            textAge.setText(String.valueOf(age));
            textGender.setText(gender);
            textHeight.setText(String.format("%.0f cm", height));
            textWeight.setText(String.format("%.0f kg", weight));
        }
        cursor.close();
    }

    private void loadStats() {
        int userId = sessionManager.getUserId();
        
        // Hybrid approach: Get from stats table, but verify with actual queries
        Cursor statsCursor = dbHelper.getUserStats(userId);
        int totalSessions = 0;
        int totalRoutines = 0;
        int totalExercises = 0;
        int activeDays = 0;

        if (statsCursor.moveToFirst()) {
            totalSessions = statsCursor.getInt(statsCursor.getColumnIndexOrThrow("total_sessions"));
            totalRoutines = statsCursor.getInt(statsCursor.getColumnIndexOrThrow("total_routines"));
            totalExercises = statsCursor.getInt(statsCursor.getColumnIndexOrThrow("total_exercises"));
            activeDays = statsCursor.getInt(statsCursor.getColumnIndexOrThrow("active_days"));
        }
        statsCursor.close();

        // Verify with actual database queries
        int actualSessions = dbHelper.getActualTotalSessions(userId);
        int actualRoutines = dbHelper.getActualTotalRoutines(userId);
        int actualExercises = dbHelper.getActualTotalExercises(userId);

        // Use the higher value (in case of discrepancies)
        totalSessions = Math.max(totalSessions, actualSessions);
        totalRoutines = Math.max(totalRoutines, actualRoutines);
        totalExercises = Math.max(totalExercises, actualExercises);

        textTotalSessions.setText(String.valueOf(totalSessions));
        textTotalRoutines.setText(String.valueOf(totalRoutines));
        textTotalExercises.setText(String.valueOf(totalExercises));
        textActiveDays.setText(String.valueOf(activeDays));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }
}
