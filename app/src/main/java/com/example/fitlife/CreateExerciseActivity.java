package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fitlife.utils.ImageHelper;
import com.example.fitlife.utils.SessionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create/Edit exercise activity - create new or edit existing exercise.
 * When launched with exercise_id extra, shows "Exercise Details" and pre-fills data.
 */
public class CreateExerciseActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    public static final String EXTRA_EXERCISE_ID = "exercise_id";

    private TextInputLayout inputLayoutExerciseName;
    private TextInputEditText editTextExerciseName;
    private LinearLayout layoutEquipments;
    private LinearLayout layoutImageUpload;
    private ImageView imageExercise;
    private LinearLayout layoutInstructions;
    private TextInputLayout inputLayoutSets;
    private TextInputLayout inputLayoutReps;
    private TextInputLayout inputLayoutRestTime;
    private TextInputEditText editTextSets;
    private TextInputEditText editTextReps;
    private TextInputEditText editTextRestTime;
    private MaterialButton buttonAddEquipment;
    private MaterialButton buttonAddInstruction;
    private MaterialButton buttonCreateExercise;
    private MaterialButton buttonDeleteExercise;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<TextInputEditText> equipmentFields = new ArrayList<>();
    private List<TextInputEditText> instructionFields = new ArrayList<>();
    private Bitmap selectedImage;
    private String imagePath;
    private long editExerciseId = -1;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exercise);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        editExerciseId = getIntent().getLongExtra(EXTRA_EXERCISE_ID, -1);
        isEditMode = editExerciseId > 0;

        initializeViews();
        if (isEditMode) {
            setTitle(R.string.exercise_details);
            buttonCreateExercise.setText(R.string.save_changes);
            buttonDeleteExercise.setVisibility(View.VISIBLE);
            buttonDeleteExercise.setOnClickListener(v -> showDeleteExerciseConfirm());
            loadExerciseData(editExerciseId);
        }
    }

    private void initializeViews() {
        inputLayoutExerciseName = findViewById(R.id.inputLayoutExerciseName);
        editTextExerciseName = findViewById(R.id.editTextExerciseName);
        layoutEquipments = findViewById(R.id.layoutEquipments);
        layoutImageUpload = findViewById(R.id.layoutImageUpload);
        imageExercise = findViewById(R.id.imageExercise);
        layoutInstructions = findViewById(R.id.layoutInstructions);
        inputLayoutSets = findViewById(R.id.inputLayoutSets);
        inputLayoutReps = findViewById(R.id.inputLayoutReps);
        inputLayoutRestTime = findViewById(R.id.inputLayoutRestTime);
        editTextSets = findViewById(R.id.editTextSets);
        editTextReps = findViewById(R.id.editTextReps);
        editTextRestTime = findViewById(R.id.editTextRestTime);
        buttonAddEquipment = findViewById(R.id.buttonAddEquipment);
        buttonAddInstruction = findViewById(R.id.buttonAddInstruction);
        buttonCreateExercise = findViewById(R.id.buttonCreateExercise);
        buttonDeleteExercise = findViewById(R.id.buttonDeleteExercise);

        layoutImageUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        buttonAddEquipment.setOnClickListener(v -> addEquipmentField(null));
        buttonAddInstruction.setOnClickListener(v -> addInstructionField(null));
        buttonCreateExercise.setOnClickListener(v -> {
            if (isEditMode) {
                handleUpdateExercise();
            } else {
                handleCreateExercise();
            }
        });
    }

    private void loadExerciseData(long exerciseId) {
        Cursor cursor = dbHelper.getExercise(exerciseId);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        int sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
        String reps = cursor.getString(cursor.getColumnIndexOrThrow("reps"));
        String restTime = cursor.getString(cursor.getColumnIndexOrThrow("rest_time"));
        String path = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
        cursor.close();

        editTextExerciseName.setText(name);
        editTextSets.setText(String.valueOf(sets));
        editTextReps.setText(reps != null ? reps : "");
        editTextRestTime.setText(restTime != null ? restTime : "");
        imagePath = path;

        if (path != null && !path.isEmpty()) {
            Bitmap bitmap = ImageHelper.loadImage(path);
            if (bitmap != null) {
                selectedImage = bitmap;
                imageExercise.setImageBitmap(bitmap);
                imageExercise.setVisibility(View.VISIBLE);
            }
        }

        List<String> equipment = dbHelper.getExerciseEquipment(exerciseId);
        layoutEquipments.removeAllViews();
        equipmentFields.clear();
        for (String eq : equipment) {
            addEquipmentField(eq);
        }
        if (equipment.isEmpty()) {
            addEquipmentField(null);
        }

        List<String> instructions = dbHelper.getExerciseInstructions(exerciseId);
        layoutInstructions.removeAllViews();
        instructionFields.clear();
        for (String inst : instructions) {
            addInstructionField(inst);
        }
        if (instructions.isEmpty()) {
            addInstructionField(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageExercise.setImageBitmap(selectedImage);
                imageExercise.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addEquipmentField(String initialValue) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint("Equipment " + (equipmentFields.size() + 1) + ":");
        if (initialValue != null) {
            editText.setText(initialValue);
        }
        layout.addView(editText);
        layoutEquipments.addView(layout);
        equipmentFields.add(editText);
    }

    private void addInstructionField(String initialValue) {
        TextInputLayout layout = new TextInputLayout(this);
        TextInputEditText editText = new TextInputEditText(this);
        editText.setHint("Instruction " + (instructionFields.size() + 1) + ":");
        if (initialValue != null) {
            editText.setText(initialValue);
        }
        layout.addView(editText);
        layoutInstructions.addView(layout);
        instructionFields.add(editText);
    }

    private void handleCreateExercise() {
        String name = editTextExerciseName.getText().toString().trim();
        String setsStr = editTextSets.getText().toString().trim();
        String reps = editTextReps.getText().toString().trim();
        String restTime = editTextRestTime.getText().toString().trim();

        if (name.isEmpty()) {
            inputLayoutExerciseName.setError("Please enter exercise name");
            return;
        }
        if (setsStr.isEmpty()) {
            inputLayoutSets.setError("Please enter sets");
            return;
        }
        int sets = Integer.parseInt(setsStr);

        if (selectedImage != null) {
            imagePath = ImageHelper.saveImage(this, selectedImage, name);
        }

        long exerciseId = dbHelper.insertExercise(name, sets, reps, restTime, imagePath);
        if (exerciseId > 0) {
            for (TextInputEditText field : equipmentFields) {
                String equipment = field.getText().toString().trim();
                if (!equipment.isEmpty()) {
                    dbHelper.insertExerciseEquipment(exerciseId, equipment);
                }
            }
            for (int i = 0; i < instructionFields.size(); i++) {
                String instruction = instructionFields.get(i).getText().toString().trim();
                if (!instruction.isEmpty()) {
                    dbHelper.insertExerciseInstruction(exerciseId, instruction, i + 1);
                }
            }
            Toast.makeText(this, R.string.exercise_created, Toast.LENGTH_SHORT).show();
            overridePendingTransition(0, 0);
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpdateExercise() {
        String name = editTextExerciseName.getText().toString().trim();
        String setsStr = editTextSets.getText().toString().trim();
        String reps = editTextReps.getText().toString().trim();
        String restTime = editTextRestTime.getText().toString().trim();

        if (name.isEmpty()) {
            inputLayoutExerciseName.setError("Please enter exercise name");
            return;
        }
        if (setsStr.isEmpty()) {
            inputLayoutSets.setError("Please enter sets");
            return;
        }
        int sets = Integer.parseInt(setsStr);

        if (selectedImage != null) {
            imagePath = ImageHelper.saveImage(this, selectedImage, name);
        }

        boolean updated = dbHelper.updateExercise(editExerciseId, name, sets, reps, restTime, imagePath);
        if (updated) {
            dbHelper.deleteExerciseEquipment(editExerciseId);
            dbHelper.deleteExerciseInstructions(editExerciseId);
            for (TextInputEditText field : equipmentFields) {
                String equipment = field.getText().toString().trim();
                if (!equipment.isEmpty()) {
                    dbHelper.insertExerciseEquipment(editExerciseId, equipment);
                }
            }
            for (int i = 0; i < instructionFields.size(); i++) {
                String instruction = instructionFields.get(i).getText().toString().trim();
                if (!instruction.isEmpty()) {
                    dbHelper.insertExerciseInstruction(editExerciseId, instruction, i + 1);
                }
            }
            Toast.makeText(this, R.string.exercise_updated, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            overridePendingTransition(0, 0);
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteExerciseConfirm() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_exercise_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteExercise())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteExercise() {
        if (dbHelper.deleteExercise(editExerciseId)) {
            Toast.makeText(this, R.string.exercise_deleted, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }
}
