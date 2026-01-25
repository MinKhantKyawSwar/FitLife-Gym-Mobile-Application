package com.example.fitlife.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.activities.CreateRoutineActivity;
import com.example.fitlife.adapters.RoutineAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.RoutineController;
import com.example.fitlife.controllers.WorkoutController;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.User;
import com.example.fitlife.utils.SMSHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManageItemsFragment extends Fragment implements RoutineAdapter.OnRoutineActionListener {
    
    private RecyclerView routinesRecyclerView;
    private Button addRoutineBtn;
    private LinearLayout emptyStateLayout;
    
    private RoutineAdapter routineAdapter;
    private RoutineController routineController;
    private WorkoutController workoutController;
    private SMSHelper smsHelper;
    
    private List<Routine> routines;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_items, container, false);
        
        initializeControllers();
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadRoutines();
        
        return view;
    }
    
    private void initializeControllers() {
        routineController = new RoutineController(requireContext());
        workoutController = new WorkoutController(requireContext());
        smsHelper = new SMSHelper(requireContext());
        routines = new ArrayList<>();
    }
    
    private void initializeViews(View view) {
        routinesRecyclerView = view.findViewById(R.id.routinesRecyclerView);
        addRoutineBtn = view.findViewById(R.id.addRoutineBtn);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }
    
    private void setupRecyclerView() {
        routineAdapter = new RoutineAdapter(getContext(), routines, this);
        routinesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        routinesRecyclerView.setAdapter(routineAdapter);
    }
    
    private void setupClickListeners() {
        addRoutineBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateRoutineActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadRoutines() {
        routines.clear();
        routines.addAll(routineController.getUserRoutines());
        routineAdapter.updateRoutines(routines);
        
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (routines.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            routinesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            routinesRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onRoutineEdit(Routine routine) {
        Intent intent = new Intent(getActivity(), CreateRoutineActivity.class);
        intent.putExtra("routine_id", routine.getRoutineId());
        intent.putExtra("edit_mode", true);
        startActivity(intent);
    }
    
    @Override
    public void onRoutineDelete(Routine routine) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Routine")
            .setMessage("Are you sure you want to delete \"" + routine.getRoutineName() + "\"? This action cannot be undone.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = routineController.deleteRoutine(routine.getRoutineId());
                if (success) {
                    routineAdapter.removeRoutine(routine);
                    updateEmptyState();
                    Toast.makeText(getContext(), "Routine deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete routine", Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onRoutineShare(Routine routine) {
        smsHelper.shareRoutine(routine, new SMSHelper.OnSMSResultListener() {
            @Override
            public void onSMSSuccess() {
                Toast.makeText(getContext(), "Routine shared successfully!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onSMSFailure(String error) {
                Toast.makeText(getContext(), "Failed to share routine: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onRoutineClick(Routine routine) {
        // Show routine details or navigate to routine detail view
        showRoutineDetails(routine);
    }
    
    @Override
    public void onAddToWeekly(Routine routine) {
        // Add routine to current week's schedule
        Date today = new Date();
        boolean success = workoutController.addRoutineToWeek(routine, today);
        
        if (success) {
            Toast.makeText(getContext(), "\"" + routine.getRoutineName() + "\" added to this week's schedule", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to add routine to weekly schedule", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showRoutineDetails(Routine routine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        StringBuilder details = new StringBuilder();
        details.append("Description: ").append(routine.getDescription() != null ? routine.getDescription() : "No description").append("\n\n");
        details.append("Exercises (").append(routine.getExercises().size()).append("):\n");
        
        for (int i = 0; i < routine.getExercises().size(); i++) {
            String exerciseName = routine.getExercises().get(i).getExercise().getExerciseName();
            int sets = routine.getExercises().get(i).getSets();
            int reps = routine.getExercises().get(i).getReps();
            details.append(String.format("%d. %s (%d × %d)\n", i + 1, exerciseName, sets, reps));
        }
        
        builder.setTitle(routine.getRoutineName())
               .setMessage(details.toString())
               .setPositiveButton("OK", null)
               .setNeutralButton("Edit", (dialog, which) -> onRoutineEdit(routine))
               .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadRoutines(); // Refresh data when returning from other activities
    }
}