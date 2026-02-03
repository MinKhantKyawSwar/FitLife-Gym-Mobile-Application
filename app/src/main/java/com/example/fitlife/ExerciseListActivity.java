package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.adapters.ExerciseAdapter;
import com.example.fitlife.models.Exercise;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists all exercises; tap to open exercise detail.
 */
public class ExerciseListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewExercises;
    private TextView textEmpty;
    private DatabaseHelper dbHelper;
    private List<Exercise> exercises = new ArrayList<>();
    private ExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        dbHelper = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.check_exercises_list);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewExercises = findViewById(R.id.recyclerViewExercises);
        textEmpty = findViewById(R.id.textEmpty);
        recyclerViewExercises.setLayoutManager(new GridLayoutManager(this, 2));

        loadExercises();
    }

    private void loadExercises() {
        Cursor cursor = dbHelper.getAllExercises();
        exercises.clear();

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

        if (exercises.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerViewExercises.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerViewExercises.setVisibility(View.VISIBLE);
            adapter = new ExerciseAdapter(exercises, exercise -> {
                Intent intent = new Intent(ExerciseListActivity.this, ExerciseDetailActivity.class);
                intent.putExtra("exercise_id", exercise.getExerciseId());
                startActivity(intent);
            });
            recyclerViewExercises.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExercises();
    }
}
