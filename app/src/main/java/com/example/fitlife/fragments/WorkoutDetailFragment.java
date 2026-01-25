package com.example.fitlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.adapters.ExerciseDetailAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.RoutineController;
import com.example.fitlife.controllers.WeeklyWorkoutController;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.RoutineExercise;
import com.example.fitlife.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WorkoutDetailFragment extends Fragment {
    
    private static final String ARG_ROUTINE = "routine";
    
    private TextView workoutNameText;
    private TextView exerciseCountText;
    private RecyclerView exercisesRecyclerView;
    private Button addToWeekBtn;
    
    private Routine routine;
    private ExerciseDetailAdapter exerciseAdapter;
    private RoutineController routineController;
    private WeeklyWorkoutController weeklyWorkoutController;
    
    public static WorkoutDetailFragment newInstance(Routine routine) {
        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTINE, routine);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routineController = new RoutineController(requireContext());
        weeklyWorkoutController = new WeeklyWorkoutController(requireContext());
        
        if (getArguments() != null) {
            routine = (Routine) getArguments().getSerializable(ARG_ROUTINE);
            // Reload routine with full exercise data
            if (routine != null && routine.getRoutineId() > 0) {
                Routine fullRoutine = routineController.getRoutineById(routine.getRoutineId());
                if (fullRoutine != null) {
                    routine = fullRoutine;
                }
            }
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_detail, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        populateData();
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        workoutNameText = view.findViewById(R.id.workoutNameText);
        exerciseCountText = view.findViewById(R.id.exerciseCountText);
        exercisesRecyclerView = view.findViewById(R.id.exercisesRecyclerView);
        addToWeekBtn = view.findViewById(R.id.addToWeekBtn);
    }
    
    private void setupRecyclerView() {
        List<RoutineExercise> exercises = routine != null && routine.getExercises() != null 
            ? routine.getExercises() 
            : new ArrayList<>();
        exerciseAdapter = new ExerciseDetailAdapter(exercises);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        exercisesRecyclerView.setAdapter(exerciseAdapter);
    }
    
    private void populateData() {
        if (routine != null) {
            workoutNameText.setText(routine.getRoutineName());
            
            int exerciseCount = routine.getExercises() != null ? routine.getExercises().size() : 0;
            exerciseCountText.setText(exerciseCount + " Exercises");
        }
    }
    
    private void setupClickListeners() {
        addToWeekBtn.setOnClickListener(v -> {
            if (routine != null) {
                addRoutineToWeek();
            }
        });
    }
    
    private void addRoutineToWeek() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to add workouts", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add to today's date
        Date today = new Date();
        boolean success = weeklyWorkoutController.addRoutineToWeek(
            currentUser.getUserId(),
            routine.getRoutineId(),
            today
        );
        
        if (success) {
            Toast.makeText(getContext(), "Workout added to this week!", Toast.LENGTH_SHORT).show();
            // Navigate back
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else {
            Toast.makeText(getContext(), "Failed to add workout", Toast.LENGTH_SHORT).show();
        }
    }
}
