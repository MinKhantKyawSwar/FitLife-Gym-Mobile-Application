package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fitlife.adapters.WorkoutExerciseAdapter;
import com.example.fitlife.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Create workout activity - allows user to create a new workout routine
 */
public class CreateWorkoutActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutWorkoutName;
    private TextInputEditText editTextWorkoutName;
    private RecyclerView recyclerViewExercises;
    private MaterialButton buttonAddExistingExercises;
    private MaterialButton buttonCreateNewWorkout;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<WorkoutExerciseItem> selectedExercises = new ArrayList<>();
    private WorkoutExerciseAdapter adapter;
    private long editingExerciseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
    }

    private void initializeViews() {
        inputLayoutWorkoutName = findViewById(R.id.inputLayoutWorkoutName);
        editTextWorkoutName = findViewById(R.id.editTextWorkoutName);
        recyclerViewExercises = findViewById(R.id.recyclerViewExercises);
        buttonAddExistingExercises = findViewById(R.id.buttonAddExistingExercises);
        buttonCreateNewWorkout = findViewById(R.id.buttonCreateNewWorkout);

        buttonAddExistingExercises.setOnClickListener(v -> {
            Intent intent = new Intent(CreateWorkoutActivity.this, AddExerciseActivity.class);
            long[] alreadyAdded = new long[selectedExercises.size()];
            for (int i = 0; i < selectedExercises.size(); i++) {
                alreadyAdded[i] = selectedExercises.get(i).exerciseId;
            }
            intent.putExtra(AddExerciseActivity.EXTRA_ALREADY_ADDED_EXERCISE_IDS, alreadyAdded);
            startActivityForResult(intent, 1);
        });

        buttonCreateNewWorkout.setOnClickListener(v -> {
            handleCreateWorkout();
        });
    }

    private void setupRecyclerView() {
        adapter = new WorkoutExerciseAdapter(selectedExercises,
                exercise -> {
                    // Edit: open Exercise Details
                    editingExerciseId = exercise.exerciseId;
                    Intent intent = new Intent(CreateWorkoutActivity.this, CreateExerciseActivity.class);
                    intent.putExtra(CreateExerciseActivity.EXTRA_EXERCISE_ID, exercise.exerciseId);
                    startActivityForResult(intent, 2);
                },
                exerciseId -> {
                    // Remove exercise from list
                    selectedExercises.removeIf(item -> item.exerciseId == exerciseId);
                    adapter.notifyDataSetChanged();
                });
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExercises.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            long[] exerciseIds = data.getLongArrayExtra("selected_exercises");
            if (exerciseIds != null) {
                for (long exerciseId : exerciseIds) {
                    Cursor cursor = dbHelper.getExercise(exerciseId);
                    if (cursor.moveToFirst()) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                        String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
                        
                        WorkoutExerciseItem item = new WorkoutExerciseItem();
                        item.exerciseId = exerciseId;
                        item.exerciseName = name;
                        item.sets = sets;
                        item.reps = reps;
                        selectedExercises.add(item);
                    }
                    cursor.close();
                }
                adapter.notifyDataSetChanged();
            }
        }
        if (requestCode == 2 && resultCode == RESULT_OK && editingExerciseId > 0) {
            refreshExerciseInList(editingExerciseId);
            editingExerciseId = -1;
        }
    }

    private void refreshExerciseInList(long exerciseId) {
        Cursor cursor = dbHelper.getExercise(exerciseId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
            String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
            cursor.close();
            for (WorkoutExerciseItem item : selectedExercises) {
                if (item.exerciseId == exerciseId) {
                    item.exerciseName = name;
                    item.sets = sets;
                    item.reps = reps;
                    break;
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            cursor.close();
            selectedExercises.removeIf(item -> item.exerciseId == exerciseId);
            adapter.notifyDataSetChanged();
        }
    }

    private void handleCreateWorkout() {
        String workoutName = editTextWorkoutName.getText().toString().trim();

        if (workoutName.isEmpty()) {
            inputLayoutWorkoutName.setError("Please enter workout name");
            return;
        }

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "Please add at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        long workoutId = dbHelper.insertWorkoutRoutine(workoutName, userId);

        if (workoutId > 0) {
            // Add exercises to workout
            for (WorkoutExerciseItem item : selectedExercises) {
                dbHelper.insertWorkoutExercise(workoutId, item.exerciseId, item.sets, item.reps);
            }

            Toast.makeText(this, R.string.workout_created, Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public static class WorkoutExerciseItem {
        public long exerciseId;
        public String exerciseName;
        public int sets;
        public String reps;
    }
}
