package com.example.fitlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fitlife.R;
import com.example.fitlife.models.Exercise;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ExerciseDetailBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_EXERCISE = "exercise";
    private static final String ARG_ROUTINE_ID = "routine_id";
    
    private TextView exerciseNameText;
    private TextView instructionsText;
    private TextView equipmentText;
    private TextView setsRepsText;
    private ImageView exerciseImage;
    private Button markCompleteBtn;
    private Button editExerciseBtn;
    
    private Exercise exercise;
    private int routineId;
    
    public static ExerciseDetailBottomSheet newInstance(Exercise exercise, int routineId) {
        ExerciseDetailBottomSheet fragment = new ExerciseDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        args.putInt(ARG_ROUTINE_ID, routineId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable(ARG_EXERCISE);
            routineId = getArguments().getInt(ARG_ROUTINE_ID);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_exercise_detail, container, false);
        
        initializeViews(view);
        populateExerciseData();
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        exerciseNameText = view.findViewById(R.id.exerciseNameText);
        instructionsText = view.findViewById(R.id.instructionsText);
        equipmentText = view.findViewById(R.id.equipmentText);
        setsRepsText = view.findViewById(R.id.setsRepsText);
        exerciseImage = view.findViewById(R.id.exerciseImage);
        markCompleteBtn = view.findViewById(R.id.markCompleteBtn);
        editExerciseBtn = view.findViewById(R.id.editExerciseBtn);
    }
    
    private void populateExerciseData() {
        if (exercise != null) {
            exerciseNameText.setText(exercise.getExerciseName());
            
            // Instructions
            if (exercise.getInstructions() != null && !exercise.getInstructions().trim().isEmpty()) {
                instructionsText.setText(exercise.getInstructions());
                instructionsText.setVisibility(View.VISIBLE);
            } else {
                instructionsText.setText("No instructions available");
                instructionsText.setVisibility(View.VISIBLE);
            }
            
            // Equipment needed
            if (exercise.getEquipmentNeeded() != null && !exercise.getEquipmentNeeded().trim().isEmpty()) {
                equipmentText.setText("Equipment: " + exercise.getEquipmentNeeded());
                equipmentText.setVisibility(View.VISIBLE);
            } else {
                equipmentText.setText("Equipment: Bodyweight only");
                equipmentText.setVisibility(View.VISIBLE);
            }
            
            // Default sets and reps (this could be enhanced to show actual routine exercise data)
            setsRepsText.setText("3 sets × 10 reps");
            
            // Load exercise image if available
            if (exercise.getImagePath() != null && !exercise.getImagePath().trim().isEmpty()) {
                Glide.with(this)
                    .load(exercise.getImagePath())
                    .placeholder(R.drawable.ic_workouts)
                    .error(R.drawable.ic_workouts)
                    .into(exerciseImage);
                exerciseImage.setVisibility(View.VISIBLE);
            } else {
                // Show default placeholder
                exerciseImage.setImageResource(R.drawable.ic_workouts);
                exerciseImage.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void setupClickListeners() {
        markCompleteBtn.setOnClickListener(v -> {
            // TODO: Implement exercise completion tracking
            Toast.makeText(getContext(), "Exercise marked as complete!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        
        editExerciseBtn.setOnClickListener(v -> {
            // TODO: Open exercise editing interface
            Toast.makeText(getContext(), "Edit exercise feature coming soon", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}