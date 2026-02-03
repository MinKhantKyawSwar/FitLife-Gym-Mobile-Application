package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.example.fitlife.utils.SessionManager;

/**
 * Create activity - allows user to create workout routine or exercise
 */
public class CreateActivity extends AppCompatActivity {
    private MaterialButton buttonCreateWorkoutRoutine;
    private MaterialButton buttonCreateNewExercise;
    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        buttonCreateWorkoutRoutine = findViewById(R.id.buttonCreateWorkoutRoutine);
        buttonCreateNewExercise = findViewById(R.id.buttonCreateNewExercise);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        buttonCreateWorkoutRoutine.setOnClickListener(v -> {
            startActivity(new Intent(CreateActivity.this, CreateWorkoutActivity.class));
            overridePendingTransition(0, 0);
        });

        buttonCreateNewExercise.setOnClickListener(v -> {
            startActivity(new Intent(CreateActivity.this, CreateExerciseActivity.class));
            overridePendingTransition(0, 0);
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_create);
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
                return true;
            } else if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(this, WorkoutsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
