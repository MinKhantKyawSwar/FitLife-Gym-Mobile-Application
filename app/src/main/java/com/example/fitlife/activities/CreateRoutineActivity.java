package com.example.fitlife.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fitlife.R;
import com.example.fitlife.controllers.RoutineController;
import com.example.fitlife.models.Routine;
import com.example.fitlife.utils.ValidationHelper;

public class CreateRoutineActivity extends AppCompatActivity {
    
    private EditText routineNameEdit;
    private Button saveRoutineBtn;
    private Button addExistingExercisesBtn;
    
    private RoutineController routineController;
    private boolean editMode = false;
    private int routineId = -1;
    private Routine editingRoutine;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);
        
        initializeControllers();
        initializeViews();
        setupToolbar();
        checkEditMode();
        setupClickListeners();
    }
    
    private void initializeControllers() {
        routineController = new RoutineController(this);
    }
    
    private void initializeViews() {
        routineNameEdit = findViewById(R.id.routineNameEdit);
        saveRoutineBtn = findViewById(R.id.saveRoutineBtn);
        addExistingExercisesBtn = findViewById(R.id.addExistingExercisesBtn);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Workout Routine");
        }
    }
    
    private void checkEditMode() {
        if (getIntent().hasExtra("routine_id")) {
            editMode = getIntent().getBooleanExtra("edit_mode", false);
            routineId = getIntent().getIntExtra("routine_id", -1);
            
            if (editMode && routineId != -1) {
                loadRoutineForEditing();
                saveRoutineBtn.setText("Update Routine");
            }
        }
    }
    
    private void loadRoutineForEditing() {
        editingRoutine = routineController.getRoutineById(routineId);
        if (editingRoutine != null) {
            routineNameEdit.setText(editingRoutine.getRoutineName());
            // Description field removed from layout
        }
    }
    
    private void setupClickListeners() {
        saveRoutineBtn.setOnClickListener(v -> saveRoutine());
        addExistingExercisesBtn.setOnClickListener(v -> {
            // TODO: Implement add existing exercises functionality
            Toast.makeText(this, "Add existing exercises feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void saveRoutine() {
        // Get and sanitize input values
        String routineName = ValidationHelper.sanitizeInput(routineNameEdit.getText().toString());
        String routineDescription = ""; // Description is optional, empty for now
        
        // Validate routine name
        boolean isNameValid = ValidationHelper.validateAndSetError(routineNameEdit, 
            ValidationHelper.validateRoutineName(routineName));
        
        // Check for SQL injection
        if (!ValidationHelper.isSqlSafe(routineName) || !ValidationHelper.isSqlSafe(routineDescription)) {
            Toast.makeText(this, "Invalid characters detected in input", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (!isNameValid) {
            routineNameEdit.requestFocus();
            return;
        }
        
        // Save routine
        boolean success;
        if (editMode && editingRoutine != null) {
            success = routineController.updateRoutine(routineId, routineName, routineDescription);
            if (success) {
                Toast.makeText(this, "Routine updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update routine", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            long newRoutineId = routineController.createRoutine(routineName, routineDescription);
            success = newRoutineId != -1;
            if (success) {
                Toast.makeText(this, "Routine created successfully!", Toast.LENGTH_SHORT).show();
                
                // TODO: Navigate to exercise selection activity
                showExerciseSelectionPrompt(newRoutineId);
            } else {
                Toast.makeText(this, "Failed to create routine", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        if (success) {
            finish(); // Return to previous screen
        }
    }
    
    private void showExerciseSelectionPrompt(long routineId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Exercises")
            .setMessage("Would you like to add exercises to this routine now?")
            .setPositiveButton("Add Exercises", (dialog, which) -> {
                // TODO: Navigate to exercise selection activity
                Toast.makeText(this, "Exercise selection feature coming soon", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Later", (dialog, which) -> {
                finish();
            })
            .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}