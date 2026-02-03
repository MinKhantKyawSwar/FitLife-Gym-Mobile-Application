package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.fitlife.adapters.WorkoutAdapter;
import com.example.fitlife.models.Workout;
import com.example.fitlife.utils.SMSHelper;
import com.example.fitlife.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen list of workout routines. Opened from "See All" on Workouts page.
 */
public class WorkoutListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewWorkouts;
    private TextView textEmpty;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.workout_routines);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts);
        textEmpty = findViewById(R.id.textEmpty);
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(this));

        loadWorkouts();
    }

    private void loadWorkouts() {
        int userId = sessionManager.getUserId();
        Cursor cursor = dbHelper.getUserWorkouts(userId);
        List<Workout> workouts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow("workout_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String createdDate = cursor.getString(cursor.getColumnIndexOrThrow("created_date"));

                Cursor exCursor = dbHelper.getWorkoutExercises(workoutId);
                int exerciseCount = exCursor.getCount();
                exCursor.close();

                Workout workout = new Workout(workoutId, name, userId, createdDate);
                workout.setExerciseCount(exerciseCount);
                workouts.add(workout);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (workouts.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerViewWorkouts.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerViewWorkouts.setVisibility(View.VISIBLE);
            WorkoutAdapter adapter = new WorkoutAdapter(
                    workouts,
                    workout -> {
                        Intent intent = new Intent(WorkoutListActivity.this, WorkoutDetailActivity.class);
                        intent.putExtra("workout_id", workout.getWorkoutId());
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    },
                    workout -> {
                        Intent intent = new Intent(WorkoutListActivity.this, WorkoutDetailActivity.class);
                        intent.putExtra("workout_id", workout.getWorkoutId());
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    },
                    this::showSetCurrentWorkoutDialog,
                    this::showShareWorkoutDialog
            );
            recyclerViewWorkouts.setAdapter(adapter);
        }
    }

    private void showSetCurrentWorkoutDialog(Workout workout) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.set_as_current_workout_question)
                .setPositiveButton(R.string.yes, (dialog, which) -> setWorkoutAsCurrent(workout))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void setWorkoutAsCurrent(Workout workout) {
        int userId = sessionManager.getUserId();
        long result = dbHelper.insertCurrentWorkout(workout.getWorkoutId(), userId);
        if (result > 0) {
            Toast.makeText(this, R.string.workout_started, Toast.LENGTH_SHORT).show();
        } else if (result == -1) {
            Toast.makeText(this, getString(R.string.workout_started), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showShareWorkoutDialog(Workout workout) {
        EditText editPhone = new EditText(this);
        editPhone.setHint(R.string.enter_phone_number);
        editPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        int padding = (int) (32 * getResources().getDisplayMetrics().density);
        editPhone.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.share_workout_to_friend)
                .setView(editPhone)
                .setPositiveButton(R.string.send, (dialog, which) -> {
                    String phone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";
                    if (phone.isEmpty()) {
                        Toast.makeText(this, R.string.enter_phone_number, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String message = buildWorkoutMessage(workout);
                    SMSHelper.shareWorkoutToPhone(this, phone, message);
                    Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String buildWorkoutMessage(Workout workout) {
        StringBuilder message = new StringBuilder();
        message.append("FitLife Workout: ").append(workout.getName()).append("\n\n");

        Cursor cursor = dbHelper.getWorkoutExercises(workout.getWorkoutId());
        int index = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                index++;
                long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
                String restTime = "";
                try {
                    int restIdx = cursor.getColumnIndexOrThrow("rest_time");
                    if (!cursor.isNull(restIdx)) {
                        restTime = cursor.getString(restIdx);
                    }
                } catch (IllegalArgumentException ignored) { }

                message.append("── ").append(index).append(". ").append(exerciseName).append(" ──\n");
                message.append("  Sets: ").append(sets).append(", Reps: ").append(reps);
                if (restTime != null && !restTime.isEmpty()) {
                    message.append(", Rest: ").append(restTime);
                }
                message.append("\n");

                List<String> exEquipment = dbHelper.getExerciseEquipment(exerciseId);
                if (exEquipment != null && !exEquipment.isEmpty()) {
                    message.append("  Equipment: ").append(String.join(", ", exEquipment)).append("\n");
                }

                List<String> exInstructions = dbHelper.getExerciseInstructions(exerciseId);
                if (exInstructions != null && !exInstructions.isEmpty()) {
                    message.append("  Instructions:\n");
                    for (int i = 0; i < exInstructions.size(); i++) {
                        message.append("    ").append(i + 1).append(". ").append(exInstructions.get(i)).append("\n");
                    }
                }
                message.append("\n");
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        message.append("Shared from FitLife App");
        return message.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }
}
