package com.example.fitlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.adapters.WorkoutAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.WorkoutController;
import com.example.fitlife.fragments.ExerciseDetailBottomSheet;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WeeklyWorkout;
import com.example.fitlife.utils.PatternShakeDetector;

import java.util.ArrayList;
import java.util.List;

public class MyWorkoutsFragment extends Fragment implements 
    WorkoutAdapter.OnWorkoutActionListener, PatternShakeDetector.OnShakeListener {
    
    private RecyclerView workoutsRecyclerView;
    private TextView emptyStateLayout;
    private TextView myWorkoutsTabLink;
    private WorkoutAdapter workoutAdapter;
    private PatternShakeDetector shakeDetector;
    
    private WorkoutController workoutController;
    private List<WeeklyWorkout> weeklyWorkouts;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_workouts, container, false);
        
        initializeControllers();
        initializeViews(view);
        setupRecyclerView();
        setupShakeDetector();
        setupClickListeners();
        loadWorkouts();
        
        return view;
    }
    
    private void initializeControllers() {
        workoutController = new WorkoutController(requireContext());
        weeklyWorkouts = new ArrayList<>();
    }
    
    private void initializeViews(View view) {
        workoutsRecyclerView = view.findViewById(R.id.workoutsRecyclerView);
        myWorkoutsTabLink = view.findViewById(R.id.myWorkoutsTabLink);
    }
    
    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter(getContext(), weeklyWorkouts, this);
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutsRecyclerView.setAdapter(workoutAdapter);
        
        // Setup swipe gestures
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
            new WorkoutAdapter.SwipeToActionCallback(workoutAdapter)
        );
        itemTouchHelper.attachToRecyclerView(workoutsRecyclerView);
    }
    
    private void setupShakeDetector() {
        shakeDetector = new PatternShakeDetector(requireContext());
        shakeDetector.setOnShakeListener(this);
    }
    
    private void setupClickListeners() {
        myWorkoutsTabLink.setOnClickListener(v -> {
            // Navigate to Workouts tab
            if (getActivity() instanceof com.example.fitlife.MainActivity) {
                com.example.fitlife.MainActivity mainActivity = 
                    (com.example.fitlife.MainActivity) getActivity();
                mainActivity.switchToWorkoutsTab();
            }
        });
    }
    
    private void loadWorkouts() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            weeklyWorkouts.clear();
            weeklyWorkouts.addAll(workoutController.getCurrentWeekWorkouts(currentUser.getUserId()));
            workoutAdapter.updateWorkouts(weeklyWorkouts);
            updateEmptyState();
        }
    }
    
    private void updateEmptyState() {
        if (weeklyWorkouts.isEmpty()) {
            workoutsRecyclerView.setVisibility(View.GONE);
            myWorkoutsTabLink.setVisibility(View.VISIBLE);
        } else {
            workoutsRecyclerView.setVisibility(View.VISIBLE);
            myWorkoutsTabLink.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onWorkoutCompleted(WeeklyWorkout workout) {
        if (!workout.isCompleted()) {
            boolean success = workoutController.markWorkoutCompleted(workout.getWeeklyWorkoutId());
            if (success) {
                workout.setCompleted(true);
                workout.setCompletionTime(new java.util.Date());
                workoutAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Workout marked as completed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to mark workout as completed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Workout is already completed", Toast.LENGTH_SHORT).show();
        }
        
        loadWorkouts();
    }
    
    @Override
    public void onWorkoutDeleted(WeeklyWorkout workout) {
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout from your weekly schedule?")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = workoutController.deleteWeeklyWorkout(workout.getWeeklyWorkoutId());
                if (success) {
                    weeklyWorkouts.remove(workout);
                    workoutAdapter.updateWorkouts(weeklyWorkouts);
                    updateEmptyState();
                    Toast.makeText(getContext(), "Workout deleted from schedule", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete workout", Toast.LENGTH_SHORT).show();
                    loadWorkouts();
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                loadWorkouts();
            })
            .setCancelable(false)
            .show();
    }
    
    @Override
    public void onWorkoutClicked(WeeklyWorkout workout) {
        // Navigate to workout detail page
        if (getActivity() instanceof com.example.fitlife.MainActivity) {
            com.example.fitlife.MainActivity mainActivity = 
                (com.example.fitlife.MainActivity) getActivity();
            mainActivity.showWorkoutDetail(workout);
        }
    }
    
    @Override
    public void onShakeDetected() {
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Weekly Workouts")
            .setMessage("Are you sure you want to reset all workouts for this week? This will remove all scheduled workouts.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Reset", (dialog, id) -> {
                boolean success = workoutController.clearCurrentWeek();
                if (success) {
                    weeklyWorkouts.clear();
                    workoutAdapter.updateWorkouts(weeklyWorkouts);
                    updateEmptyState();
                    Toast.makeText(getContext(), "Weekly workouts reset successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to reset weekly workouts", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.startListening();
        loadWorkouts();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.stopListening();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (shakeDetector != null) {
            shakeDetector.stopListening();
        }
    }
}
