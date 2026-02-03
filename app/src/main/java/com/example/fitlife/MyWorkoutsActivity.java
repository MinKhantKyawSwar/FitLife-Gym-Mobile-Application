package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.fitlife.adapters.CurrentWorkoutExerciseAdapter;
import com.example.fitlife.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * My Workouts activity - displays current workout exercises with gesture controls
 * Implements swipe left to delete, swipe right to mark complete, shake to reset
 */
public class MyWorkoutsActivity extends AppCompatActivity implements SensorEventListener {
    private RecyclerView recyclerViewCurrentWorkouts;
    private TextView textEmpty;
    private TextView textCurrentWorkouts;
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    /** Debounce: ignore shake events for this long after handling one (ms). */
    private static final long SHAKE_DEBOUNCE_MS = 2500;
    private long lastShakeHandledAt = 0;
    private long currentWorkoutId = -1;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workouts);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        userId = sessionManager.getUserId();
        initializeViews();
        setupBottomNavigation();
        setupGestureControls();
        setupShakeDetection();
        loadCurrentWorkout();
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.current_workouts);
        }
        recyclerViewCurrentWorkouts = findViewById(R.id.recyclerViewCurrentWorkouts);
        textEmpty = findViewById(R.id.textEmpty);
        textCurrentWorkouts = findViewById(R.id.textCurrentWorkouts);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_workouts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_current_workout) {
            showDeleteCurrentWorkoutConfirm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteCurrentWorkoutConfirm() {
        if (currentWorkoutId == -1) {
            Toast.makeText(this, R.string.no_current_workouts, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_current_workout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteCurrentWorkout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteCurrentWorkout() {
        if (currentWorkoutId != -1 && dbHelper.deleteCurrentWorkout(currentWorkoutId, userId)) {
            Toast.makeText(this, R.string.workout_deleted, Toast.LENGTH_SHORT).show();
            currentWorkoutId = -1;
            loadCurrentWorkout();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_my_workouts);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_my_workouts) {
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

    private void setupGestureControls() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                CurrentWorkoutExerciseAdapter adapter = (CurrentWorkoutExerciseAdapter) recyclerViewCurrentWorkouts.getAdapter();
                if (adapter != null) {
                    long exerciseId = adapter.getExerciseIdAt(position);
                    
                    if (direction == ItemTouchHelper.RIGHT) {
                        // Mark as completed
                        markExerciseComplete(exerciseId, position);
                    } else if (direction == ItemTouchHelper.LEFT) {
                        // Delete exercise from current workout
                        deleteExerciseFromWorkout(exerciseId, position);
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewCurrentWorkouts);
    }

    private void setupShakeDetection() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadCurrentWorkout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        loadCurrentWorkout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    long now = System.currentTimeMillis();
                    if (now - lastShakeHandledAt >= SHAKE_DEBOUNCE_MS) {
                        lastShakeHandledAt = now;
                        resetWorkoutSession();
                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void loadCurrentWorkout() {
        Cursor cursor = dbHelper.getCurrentWorkouts(userId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    currentWorkoutId = cursor.getLong(cursor.getColumnIndexOrThrow("workout_id"));
                    String workoutName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if (textCurrentWorkouts != null) {
                        textCurrentWorkouts.setVisibility(View.VISIBLE);
                        textCurrentWorkouts.setText(workoutName);
                    }
                    // Load exercises for this workout
                    loadWorkoutExercises();
                } else {
                    if (textCurrentWorkouts != null) {
                        textCurrentWorkouts.setVisibility(View.GONE);
                    }
                    if (textEmpty != null) {
                        textEmpty.setVisibility(View.VISIBLE);
                    }
                    if (recyclerViewCurrentWorkouts != null) {
                        recyclerViewCurrentWorkouts.setVisibility(View.GONE);
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            if (textCurrentWorkouts != null) {
                textCurrentWorkouts.setVisibility(View.GONE);
            }
            if (textEmpty != null) {
                textEmpty.setVisibility(View.VISIBLE);
            }
            if (recyclerViewCurrentWorkouts != null) {
                recyclerViewCurrentWorkouts.setVisibility(View.GONE);
            }
        }
    }

    private void loadWorkoutExercises() {
        if (currentWorkoutId == -1) return;

        Cursor cursor = dbHelper.getCurrentWorkoutExercises(currentWorkoutId, userId);
        List<WorkoutExerciseItem> exercises = new ArrayList<>();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                        String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                        String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
                        String status = "Pending";
                        int statusIdx = cursor.getColumnIndex("exercise_status");
                        if (statusIdx >= 0) {
                            status = cursor.getString(statusIdx);
                            if (status == null) status = "Pending";
                        }
                        WorkoutExerciseItem item = new WorkoutExerciseItem();
                        item.exerciseId = exerciseId;
                        item.exerciseName = exerciseName;
                        item.sets = sets;
                        item.reps = reps;
                        item.status = status;
                        exercises.add(item);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        if (exercises.isEmpty()) {
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
                
                CurrentWorkoutExerciseAdapter adapter = new CurrentWorkoutExerciseAdapter(exercises, exerciseId -> {
                    Intent intent = new Intent(MyWorkoutsActivity.this, ExerciseDetailActivity.class);
                    intent.putExtra("exercise_id", exerciseId);
                    startActivity(intent);
                });
                recyclerViewCurrentWorkouts.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewCurrentWorkouts.setAdapter(adapter);
            }
        }
    }

    private void markExerciseComplete(long exerciseId, int position) {
        // Persist completion to database so it stays when user returns to the page
        if (currentWorkoutId != -1) {
            dbHelper.setCurrentWorkoutExerciseStatus(currentWorkoutId, userId, exerciseId, "Completed");
        }
        // Mark exercise as completed (update status in UI)
        CurrentWorkoutExerciseAdapter adapter = (CurrentWorkoutExerciseAdapter) recyclerViewCurrentWorkouts.getAdapter();
        if (adapter != null) {
            adapter.markComplete(position);
        }
        // Check if all exercises are completed
        if (adapter != null && adapter.areAllCompleted()) {
            dbHelper.updateCurrentWorkoutStatus(currentWorkoutId, userId, "completed");
            new AlertDialog.Builder(this)
                    .setTitle("Workout Completed!")
                    .setMessage("Congratulations! You've completed the workout.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void deleteExerciseFromWorkout(long exerciseId, int position) {
        // Remove from current tab/session only; do NOT delete from actual workout routine
        if (currentWorkoutId != -1) {
            dbHelper.addCurrentWorkoutRemovedExercise(currentWorkoutId, userId, exerciseId);
            loadWorkoutExercises();
        }
    }

    private void resetWorkoutSession() {
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
        new AlertDialog.Builder(this)
                .setTitle("Reset Workout")
                .setMessage("Are you sure you want to reset the entire workout session?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteAllCurrentWorkouts(userId);
                    loadCurrentWorkout();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public static class WorkoutExerciseItem {
        public long exerciseId;
        public String exerciseName;
        public int sets;
        public String reps;
        public String status;
    }
}
