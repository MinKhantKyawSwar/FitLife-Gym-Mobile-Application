package com.example.fitlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fitlife.R;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.ExerciseController;
import com.example.fitlife.models.Exercise;
import com.example.fitlife.models.User;
import com.example.fitlife.utils.ImageCaptureHelper;
import com.example.fitlife.utils.PermissionManager;
import com.example.fitlife.utils.ValidationHelper;

import java.util.ArrayList;
import java.util.List;

public class AddExerciseFragment extends Fragment implements ImageCaptureHelper.ImageCaptureCallback {
    
    // UI Components
    private EditText exerciseNameEditText;
    private EditText equipment1EditText;
    private EditText equipment2EditText;
    private EditText instruction1EditText;
    private EditText instruction2EditText;
    private EditText setsEditText;
    private EditText repsEditText;
    private EditText restTimeEditText;
    private ImageView exerciseImageView;
    private LinearLayout imageUploadArea;
    private Button addNewEquipmentBtn;
    private Button addNewInstructionsBtn;
    private Button saveExerciseBtn;
    
    // Controllers and Helpers
    private ExerciseController exerciseController;
    private ImageCaptureHelper imageCaptureHelper;
    
    // Data
    private String currentImagePath;
    private List<EditText> equipmentFields;
    private List<EditText> instructionFields;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseController = new ExerciseController(requireContext());
        imageCaptureHelper = new ImageCaptureHelper(requireContext());
        imageCaptureHelper.setCallback(this);
        equipmentFields = new ArrayList<>();
        instructionFields = new ArrayList<>();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_exercise, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        exerciseNameEditText = view.findViewById(R.id.exerciseNameEditText);
        equipment1EditText = view.findViewById(R.id.equipment1EditText);
        equipment2EditText = view.findViewById(R.id.equipment2EditText);
        instruction1EditText = view.findViewById(R.id.instruction1EditText);
        instruction2EditText = view.findViewById(R.id.instruction2EditText);
        setsEditText = view.findViewById(R.id.setsEditText);
        repsEditText = view.findViewById(R.id.repsEditText);
        restTimeEditText = view.findViewById(R.id.restTimeEditText);
        exerciseImageView = view.findViewById(R.id.exerciseImageView);
        imageUploadArea = view.findViewById(R.id.imageUploadArea);
        addNewEquipmentBtn = view.findViewById(R.id.addNewEquipmentBtn);
        addNewInstructionsBtn = view.findViewById(R.id.addNewInstructionsBtn);
        saveExerciseBtn = view.findViewById(R.id.saveExerciseBtn);
        
        // Add initial fields to lists
        equipmentFields.add(equipment1EditText);
        equipmentFields.add(equipment2EditText);
        instructionFields.add(instruction1EditText);
        instructionFields.add(instruction2EditText);
        
        // Set default values
        setsEditText.setText("3");
        repsEditText.setText("10");
        restTimeEditText.setText("60");
        
        updateImageUI();
    }
    
    private void setupClickListeners() {
        imageUploadArea.setOnClickListener(v -> {
            if (PermissionManager.hasCameraPermissions(requireContext()) && 
                PermissionManager.hasStoragePermissions(requireContext())) {
                imageCaptureHelper.showImageSourceDialog(this);
            } else {
                // Request both permissions
                if (!PermissionManager.hasCameraPermissions(requireContext())) {
                    PermissionManager.requestCameraPermissions(this);
                }
                if (!PermissionManager.hasStoragePermissions(requireContext())) {
                    PermissionManager.requestStoragePermissions(this);
                }
            }
        });
        
        addNewEquipmentBtn.setOnClickListener(v -> addNewEquipmentField());
        
        addNewInstructionsBtn.setOnClickListener(v -> addNewInstructionField());
        
        saveExerciseBtn.setOnClickListener(v -> saveExercise());
    }
    
    private void addNewEquipmentField() {
        // TODO: Implement dynamic equipment field addition
        Toast.makeText(getContext(), "Add new equipment field functionality", Toast.LENGTH_SHORT).show();
    }
    
    private void addNewInstructionField() {
        // TODO: Implement dynamic instruction field addition
        Toast.makeText(getContext(), "Add new instruction field functionality", Toast.LENGTH_SHORT).show();
    }
    
    private void saveExercise() {
        // Validate inputs
        String exerciseName = ValidationHelper.sanitizeInput(exerciseNameEditText.getText().toString());
        
        // Combine equipment fields
        StringBuilder equipmentBuilder = new StringBuilder();
        for (EditText field : equipmentFields) {
            String equipment = ValidationHelper.sanitizeInput(field.getText().toString());
            if (!TextUtils.isEmpty(equipment)) {
                if (equipmentBuilder.length() > 0) {
                    equipmentBuilder.append(", ");
                }
                equipmentBuilder.append(equipment);
            }
        }
        String equipment = equipmentBuilder.toString();
        
        // Combine instruction fields
        StringBuilder instructionsBuilder = new StringBuilder();
        int instructionNum = 1;
        for (EditText field : instructionFields) {
            String instruction = ValidationHelper.sanitizeInput(field.getText().toString());
            if (!TextUtils.isEmpty(instruction)) {
                if (instructionsBuilder.length() > 0) {
                    instructionsBuilder.append("\n");
                }
                instructionsBuilder.append(instructionNum).append(". ").append(instruction);
                instructionNum++;
            }
        }
        String instructions = instructionsBuilder.toString();
        
        String setsStr = setsEditText.getText().toString().trim();
        String repsStr = repsEditText.getText().toString().trim();
        String restTimeStr = restTimeEditText.getText().toString().trim();
        
        // Validate exercise name
        if (TextUtils.isEmpty(exerciseName)) {
            exerciseNameEditText.setError("Exercise name is required");
            exerciseNameEditText.requestFocus();
            return;
        }
        
        if (exerciseName.length() < 2) {
            exerciseNameEditText.setError("Exercise name must be at least 2 characters");
            exerciseNameEditText.requestFocus();
            return;
        }
        
        // Validate numeric inputs
        int sets, reps, restTime;
        try {
            sets = Integer.parseInt(setsStr);
            if (sets < 1 || sets > 20) {
                setsEditText.setError("Sets must be between 1 and 20");
                setsEditText.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            setsEditText.setError("Please enter a valid number");
            setsEditText.requestFocus();
            return;
        }
        
        try {
            reps = Integer.parseInt(repsStr);
            if (reps < 1 || reps > 100) {
                repsEditText.setError("Reps must be between 1 and 100");
                repsEditText.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            repsEditText.setError("Please enter a valid number");
            repsEditText.requestFocus();
            return;
        }
        
        try {
            restTime = Integer.parseInt(restTimeStr);
            if (restTime < 0 || restTime > 600) {
                restTimeEditText.setError("Rest time must be between 0 and 600 seconds");
                restTimeEditText.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            restTimeEditText.setError("Please enter a valid number");
            restTimeEditText.requestFocus();
            return;
        }
        
        // Get current user
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create exercise object
        Exercise exercise = new Exercise();
        exercise.setUserId(currentUser.getUserId());
        exercise.setExerciseName(exerciseName);
        exercise.setEquipmentNeeded(equipment);
        exercise.setInstructions(instructions);
        exercise.setImagePath(currentImagePath);
        
        // Save exercise
        long exerciseId = exerciseController.createExercise(exercise);
        
        if (exerciseId > 0) {
            Toast.makeText(getContext(), "Exercise created successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate back
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else {
            Toast.makeText(getContext(), "Failed to create exercise", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateImageUI() {
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            imageUploadArea.setVisibility(View.GONE);
            exerciseImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(currentImagePath)
                .placeholder(R.drawable.ic_exercise_placeholder)
                .error(R.drawable.ic_exercise_placeholder)
                .into(exerciseImageView);
        } else {
            imageUploadArea.setVisibility(View.VISIBLE);
            exerciseImageView.setVisibility(View.GONE);
        }
    }
    
    private void removeCurrentImage() {
        currentImagePath = null;
        updateImageUI();
    }
    
    @Override
    public void onImageCaptured(String imagePath) {
        currentImagePath = imagePath;
        updateImageUI();
    }
    
    @Override
    public void onImageCaptureFailed(String error) {
        Toast.makeText(getContext(), "Error capturing image: " + error, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionResult(requireContext(), requestCode, permissions, grantResults, null);
    }
}
