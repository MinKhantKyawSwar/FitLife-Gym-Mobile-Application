package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.example.fitlife.adapters.ExerciseSelectAdapter;
import com.example.fitlife.models.Exercise;
import com.example.fitlife.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Add exercise activity - allows user to select existing exercises to add to workout.
 * Already-added exercises (in current workout or already selected) are hidden.
 */
public class AddExerciseActivity extends AppCompatActivity {
    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_ALREADY_ADDED_EXERCISE_IDS = "already_added_exercise_ids";
    private static final int REQUEST_CREATE_EXERCISE = 2;

    private RecyclerView recyclerViewExercises;
    private MaterialButton buttonAddToWorkout;
    private MaterialButton buttonCreateNewExercise;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ExerciseSelectAdapter adapter;
    private List<Long> selectedExerciseIds = new ArrayList<>();
    private Set<Long> excludedExerciseIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        buildExcludedExerciseIds();
        initializeViews();
        loadExercises();
    }

    private void buildExcludedExerciseIds() {
        long workoutId = getIntent().getLongExtra(EXTRA_WORKOUT_ID, -1);
        if (workoutId >= 0) {
            Cursor cursor = dbHelper.getWorkoutExercises(workoutId);
            if (cursor.moveToFirst()) {
                do {
                    long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                    excludedExerciseIds.add(exerciseId);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        long[] alreadyAdded = getIntent().getLongArrayExtra(EXTRA_ALREADY_ADDED_EXERCISE_IDS);
        if (alreadyAdded != null) {
            for (long id : alreadyAdded) {
                excludedExerciseIds.add(id);
            }
        }
    }

    private void initializeViews() {
        recyclerViewExercises = findViewById(R.id.recyclerViewExercises);
        buttonAddToWorkout = findViewById(R.id.buttonAddToWorkout);
        buttonCreateNewExercise = findViewById(R.id.buttonCreateNewExercise);

        buttonAddToWorkout.setOnClickListener(v -> {
            if (selectedExerciseIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one exercise", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            long[] ids = new long[selectedExerciseIds.size()];
            for (int i = 0; i < selectedExerciseIds.size(); i++) {
                ids[i] = selectedExerciseIds.get(i);
            }
            resultIntent.putExtra("selected_exercises", ids);
            setResult(RESULT_OK, resultIntent);
            overridePendingTransition(0, 0);
            finish();
        });

        buttonCreateNewExercise.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateExerciseActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_EXERCISE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_EXERCISE && resultCode == RESULT_OK) {
            loadExercises();
        }
    }

    private void loadExercises() {
        Cursor cursor = dbHelper.getAllExercises();
        List<Exercise> exercises = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                if (excludedExerciseIds.contains(exerciseId)) {
                    continue;
                }
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
                String restTime = cursor.getString(cursor.getColumnIndexOrThrow("rest_time"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));

                Exercise exercise = new Exercise(exerciseId, name, sets, reps, restTime, imagePath);
                exercises.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ExerciseSelectAdapter(exercises, exerciseId -> {
            if (selectedExerciseIds.contains(exerciseId)) {
                selectedExerciseIds.remove(exerciseId);
            } else {
                selectedExerciseIds.add(exerciseId);
            }
        });
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExercises.setAdapter(adapter);
    }
}
