package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.example.fitlife.adapters.ExerciseAdapter;
import com.example.fitlife.adapters.CurrentWorkoutAdapter;
import com.example.fitlife.models.Exercise;
import com.example.fitlife.models.CurrentWorkout;
import com.example.fitlife.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Home activity - main entry point after login
 * Displays greeting, recommendations, current workouts, and navigation
 */
public class HomeActivity extends AppCompatActivity {
    private TextView textGreeting;
    private MaterialButton buttonCreateNewWorkout;
    private MaterialButton buttonCheckWorkoutList;
    private MaterialButton buttonCreateExercise;
    private MaterialButton buttonCheckExercisesList;
    private TextView textSeeAllRecommendations;
    private TextView textViewMore;
    private RecyclerView recyclerViewRecommendations;
    private RecyclerView recyclerViewCurrentWorkouts;
    private TextView textEmpty;
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        loadData();
    }

    private void initializeViews() {
        textGreeting = findViewById(R.id.textGreeting);
        buttonCreateNewWorkout = findViewById(R.id.buttonCreateNewWorkout);
        buttonCheckWorkoutList = findViewById(R.id.buttonCheckWorkoutList);
        buttonCreateExercise = findViewById(R.id.buttonCreateExercise);
        buttonCheckExercisesList = findViewById(R.id.buttonCheckExercisesList);
        textSeeAllRecommendations = findViewById(R.id.textSeeAllRecommendations);
        textViewMore = findViewById(R.id.textViewMore);
        recyclerViewRecommendations = findViewById(R.id.recyclerViewRecommendations);
        recyclerViewCurrentWorkouts = findViewById(R.id.recyclerViewCurrentWorkouts);
        textEmpty = findViewById(R.id.textEmpty);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Set greeting
        String username = sessionManager.getUsernameEmail();
        if (username != null && username.contains("@")) {
            username = username.substring(0, username.indexOf("@"));
        }
        textGreeting.setText(getString(R.string.hi_user, username != null ? username : "User"));

        buttonCreateNewWorkout.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CreateWorkoutActivity.class));
            overridePendingTransition(0, 0);
        });

        buttonCheckWorkoutList.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, WorkoutsActivity.class));
            overridePendingTransition(0, 0);
        });

        buttonCreateExercise.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CreateExerciseActivity.class));
            overridePendingTransition(0, 0);
        });

        buttonCheckExercisesList.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ExerciseListActivity.class));
            overridePendingTransition(0, 0);
        });

        textSeeAllRecommendations.setOnClickListener(v -> {
            // Navigate to workouts tab
            startActivity(new Intent(HomeActivity.this, WorkoutsActivity.class));
            overridePendingTransition(0, 0);
        });

        textViewMore.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, WorkoutsActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadData();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
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
                startActivity(new Intent(this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadData() {
        loadRecommendations();
        loadCurrentWorkouts();
    }

    private void loadRecommendations() {
        // Load exercises from database for recommendations
        Cursor cursor = dbHelper.getAllExercises();
        List<Exercise> exercises = new ArrayList<>();
        
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                        String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
                        String restTime = cursor.getString(cursor.getColumnIndexOrThrow("rest_time"));
                        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                        
                        Exercise exercise = new Exercise(exerciseId, name, sets, reps, restTime, imagePath);
                        exercises.add(exercise);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        // Show only 2 exercises on Home recommendations
        if (exercises.size() > 2) {
            Collections.shuffle(exercises);
            exercises = new ArrayList<>(exercises.subList(0, 2));
        }

        if (recyclerViewRecommendations != null) {
            ExerciseAdapter adapter = new ExerciseAdapter(
                    exercises,
                    exercise -> {
                        Intent intent = new Intent(HomeActivity.this, ExerciseDetailActivity.class);
                        intent.putExtra("exercise_id", exercise.getExerciseId());
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    },
                    R.layout.item_exercise_recommendation);
            recyclerViewRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerViewRecommendations.setAdapter(adapter);
        }
    }

    private void loadCurrentWorkouts() {
        int userId = sessionManager.getUserId();
        Cursor cursor = dbHelper.getCurrentWorkouts(userId);
        List<CurrentWorkout> workouts = new ArrayList<>();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow("workout_id"));
                        String workoutName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                        
                        // Count exercises
                        Cursor exCursor = dbHelper.getWorkoutExercises(workoutId);
                        int exerciseCount = 0;
                        if (exCursor != null) {
                            exerciseCount = exCursor.getCount();
                            exCursor.close();
                        }
                        
                        CurrentWorkout workout = new CurrentWorkout();
                        workout.setWorkoutId(workoutId);
                        workout.setWorkoutName(workoutName);
                        workout.setStatus(status);
                        workout.setExerciseCount(exerciseCount);
                        workouts.add(workout);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        if (workouts.isEmpty()) {
            if (textEmpty != null) {
                textEmpty.setVisibility(View.VISIBLE);
            }
            if (recyclerViewCurrentWorkouts != null) {
                recyclerViewCurrentWorkouts.setVisibility(View.GONE);
            }
        } else {
            if (textEmpty != null) {
                textEmpty.setVisibility(View.GONE);
            }
            if (recyclerViewCurrentWorkouts != null) {
                recyclerViewCurrentWorkouts.setVisibility(View.VISIBLE);
                
                CurrentWorkoutAdapter adapter = new CurrentWorkoutAdapter(workouts, workout -> {
                    Intent intent = new Intent(HomeActivity.this, MyWorkoutsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                });
                recyclerViewCurrentWorkouts.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewCurrentWorkouts.setAdapter(adapter);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
