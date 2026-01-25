package com.example.fitlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.fitlife.R;
import com.example.fitlife.activities.CreateRoutineActivity;

public class CreateFragment extends Fragment {
    
    private Button createWorkoutRoutineBtn;
    private Button createNewExerciseBtn;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        createWorkoutRoutineBtn = view.findViewById(R.id.createWorkoutRoutineBtn);
        createNewExerciseBtn = view.findViewById(R.id.createNewExerciseBtn);
    }
    
    private void setupClickListeners() {
        createWorkoutRoutineBtn.setOnClickListener(v -> {
            // Navigate to Create Routine Activity
            Intent intent = new Intent(requireContext(), CreateRoutineActivity.class);
            startActivity(intent);
        });
        
        createNewExerciseBtn.setOnClickListener(v -> {
            // Navigate to Add Exercise Fragment
            if (getActivity() instanceof com.example.fitlife.MainActivity) {
                com.example.fitlife.MainActivity mainActivity = 
                    (com.example.fitlife.MainActivity) getActivity();
                mainActivity.showAddExerciseFragment();
            }
        });
    }
}
