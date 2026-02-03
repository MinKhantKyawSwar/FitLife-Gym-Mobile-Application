package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import android.widget.ImageButton;
import com.example.fitlife.adapters.WorkoutExerciseAdapter;
import com.example.fitlife.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Workout detail activity - displays and allows editing of workout routine
 * Includes "Start Workout" button to make workout current
 */
public class WorkoutDetailActivity extends AppCompatActivity {
    private MaterialTextView textWorkoutName;
    private RecyclerView recyclerViewExercises;
    private MaterialButton buttonAddExistingExercise;
    private MaterialButton buttonSave;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private long workoutId;
    private List<CreateWorkoutActivity.WorkoutExerciseItem> exercises = new ArrayList<>();
    private WorkoutExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        workoutId = getIntent().getLongExtra("workout_id", -1);
        if (workoutId == -1) {
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadWorkoutData();
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.workouts_title);
        }

        textWorkoutName = findViewById(R.id.textWorkoutName);
        recyclerViewExercises = findViewById(R.id.recyclerViewExercises);
        buttonAddExistingExercise = findViewById(R.id.buttonAddExistingExercise);
        buttonSave = findViewById(R.id.buttonSave);

        recyclerViewExercises.setNestedScrollingEnabled(false);

        buttonAddExistingExercise.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutDetailActivity.this, AddExerciseActivity.class);
            intent.putExtra(AddExerciseActivity.EXTRA_WORKOUT_ID, workoutId);
            startActivityForResult(intent, 1);
        });

        buttonSave.setOnClickListener(v -> {
            overridePendingTransition(0, 0);
            finish();
        });

        ImageButton buttonEditWorkout = findViewById(R.id.buttonEditWorkout);
        buttonEditWorkout.setOnClickListener(v -> showEditWorkoutNameDialog());
    }

    private void showEditWorkoutNameDialog() {
        String currentName = textWorkoutName.getText() != null ? textWorkoutName.getText().toString() : "";
        EditText editName = new EditText(this);
        editName.setHint(R.string.workout_name_label);
        editName.setText(currentName);
        editName.setSelection(currentName.length());
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        editName.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit)
                .setView(editName)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = editName.getText() != null ? editName.getText().toString().trim() : "";
                    if (newName.isEmpty()) {
                        Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (dbHelper.updateWorkoutRoutine(workoutId, newName)) {
                        textWorkoutName.setText(newName);
                        Toast.makeText(this, R.string.workout_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_edit_workout) {
            showEditWorkoutNameDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_delete_workout) {
            showDeleteWorkoutConfirm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteWorkoutConfirm() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_workout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteWorkout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteWorkout() {
        if (dbHelper.deleteWorkoutRoutine(workoutId)) {
            Toast.makeText(this, R.string.workout_deleted, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new WorkoutExerciseAdapter(exercises,
                exercise -> {
                    // Edit: open Exercise Details (CreateExerciseActivity in edit mode)
                    Intent intent = new Intent(WorkoutDetailActivity.this, CreateExerciseActivity.class);
                    intent.putExtra("exercise_id", exercise.exerciseId);
                    startActivityForResult(intent, 2);
                },
                exerciseId -> {
                    // Confirm then remove exercise from workout
                    new AlertDialog.Builder(WorkoutDetailActivity.this)
                            .setMessage(R.string.remove_exercise_from_workout_confirm)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                dbHelper.deleteWorkoutExercise(workoutId, exerciseId);
                                loadWorkoutData();
                                Toast.makeText(WorkoutDetailActivity.this, R.string.exercise_deleted, Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                });
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExercises.setAdapter(adapter);
    }

    private void loadWorkoutData() {
        Cursor cursor = dbHelper.getWorkout(workoutId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            textWorkoutName.setText(name);
        }
        cursor.close();

        // Load exercises
        exercises.clear();
        Cursor exCursor = dbHelper.getWorkoutExercises(workoutId);
        if (exCursor.moveToFirst()) {
            do {
                long exerciseId = exCursor.getLong(exCursor.getColumnIndexOrThrow("exercise_id"));
                String exerciseName = exCursor.getString(exCursor.getColumnIndexOrThrow("name"));
                int sets = exCursor.getInt(exCursor.getColumnIndexOrThrow("sets"));
                String reps = exCursor.getString(exCursor.getColumnIndexOrThrow("reps"));

                CreateWorkoutActivity.WorkoutExerciseItem item = new CreateWorkoutActivity.WorkoutExerciseItem();
                item.exerciseId = exerciseId;
                item.exerciseName = exerciseName;
                item.sets = sets;
                item.reps = reps;
                exercises.add(item);
            } while (exCursor.moveToNext());
        }
        exCursor.close();
        adapter.notifyDataSetChanged();
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
                        int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                        String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
                        dbHelper.insertWorkoutExercise(workoutId, exerciseId, sets, reps);
                    }
                    cursor.close();
                }
                loadWorkoutData();
            }
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            loadWorkoutData();
        }
    }

}
