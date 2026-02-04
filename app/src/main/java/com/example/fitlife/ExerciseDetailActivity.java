package com.example.fitlife;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.example.fitlife.utils.ImageHelper;
import java.util.List;

/**
 * Exercise detail activity - displays exercise information with Edit and Delete.
 */
public class ExerciseDetailActivity extends AppCompatActivity {
    private ImageView imageExercise;
    private TextView textExerciseName;
    private TextView textEquipmentList;
    private TextView textSetsRepsDetails;
    private TextView textInstructionsList;
    private MaterialButton buttonEditExercise;
    private MaterialButton buttonDeleteExercise;
    private DatabaseHelper dbHelper;
    private long exerciseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        dbHelper = new DatabaseHelper(this);

        exerciseId = getIntent().getLongExtra("exercise_id", -1);
        if (exerciseId == -1) {
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        initializeViews();
        loadExerciseData(exerciseId);
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.exercise_details);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imageExercise = findViewById(R.id.imageExercise);
        textExerciseName = findViewById(R.id.textExerciseName);
        textEquipmentList = findViewById(R.id.textEquipmentList);
        textSetsRepsDetails = findViewById(R.id.textSetsRepsDetails);
        textInstructionsList = findViewById(R.id.textInstructionsList);
        buttonEditExercise = findViewById(R.id.buttonEditExercise);
        buttonDeleteExercise = findViewById(R.id.buttonDeleteExercise);

        buttonEditExercise.setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseDetailActivity.this, CreateExerciseActivity.class);
            intent.putExtra(CreateExerciseActivity.EXTRA_EXERCISE_ID, exerciseId);
            startActivity(intent);
        });

        buttonDeleteExercise.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_exercise_confirm)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        if (dbHelper.deleteExercise(exerciseId)) {
                            Toast.makeText(this, R.string.exercise_deleted, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
    }

    private void loadExerciseData(long exerciseId) {
        android.database.Cursor cursor = dbHelper.getExercise(exerciseId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
            String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
            String restTime = cursor.getString(cursor.getColumnIndexOrThrow("rest_time"));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));

            textExerciseName.setText(name);
            textSetsRepsDetails.setText("Set: " + sets + "\nReps: " + reps + "\nRest Time: " + restTime);

            // Load image
            if (imagePath != null && !imagePath.isEmpty()) {
                Bitmap bitmap = ImageHelper.loadImage(imagePath);
                if (bitmap != null) {
                    imageExercise.setImageBitmap(bitmap);
                }
            }

            // Load equipment
            List<String> equipment = dbHelper.getExerciseEquipment(exerciseId);
            StringBuilder equipmentText = new StringBuilder();
            for (String eq : equipment) {
                equipmentText.append("• ").append(eq).append("\n");
            }
            textEquipmentList.setText(equipmentText.toString());

            // Load instructions
            List<String> instructions = dbHelper.getExerciseInstructions(exerciseId);
            StringBuilder instructionsText = new StringBuilder();
            for (int i = 0; i < instructions.size(); i++) {
                instructionsText.append((i + 1)).append(". ").append(instructions.get(i)).append("\n");
            }
            textInstructionsList.setText(instructionsText.toString());
        }
        cursor.close();
    }
}
