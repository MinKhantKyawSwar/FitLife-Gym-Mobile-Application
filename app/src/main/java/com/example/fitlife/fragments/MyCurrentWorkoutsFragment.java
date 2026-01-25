package com.example.fitlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.adapters.WeeklyWorkoutAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.WeeklyWorkoutController;
import com.example.fitlife.controllers.WorkoutHistoryController;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WeeklyWorkout;
import com.example.fitlife.models.Routine;
import com.example.fitlife.utils.PatternShakeDetector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyCurrentWorkoutsFragment extends Fragment implements 
    WeeklyWorkoutAdapter.OnWorkoutActionListener, PatternShakeDetector.OnPatternShakeListener {
    
    // UI Components
    private RecyclerView workoutsRecyclerView;
    private LinearLayout emptyWorkoutsLayout;
    private TextView weeklyProgressText;
    
    // Controllers
    private WeeklyWorkoutController weeklyWorkoutController;
    private WorkoutHistoryController workoutHistoryController;
    
    // Adapters and Helpers
    private WeeklyWorkoutAdapter workoutAdapter;
    private PatternShakeDetector shakeDetector;
    
    // Data
    private List<WeeklyWorkout> weeklyWorkouts;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weeklyWorkoutController = new WeeklyWorkoutController(requireContext());
        workoutHistoryController = new WorkoutHistoryController(requireContext());
        weeklyWorkouts = new ArrayList<>();
        
        // Initialize shake detector
        shakeDetector = new PatternShakeDetector(requireContext());
        shakeDetector.setOnPatternShakeListener(this);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_current_workouts, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadWeeklyWorkouts();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadWeeklyWorkouts();
        shakeDetector.startListening();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.stopListening();
    }
    
    private void initializeViews(View view) {
        workoutsRecyclerView = view.findViewById(R.id.workoutsRecyclerView);
        emptyWorkoutsLayout = view.findViewById(R.id.emptyWorkoutsLayout);
        weeklyProgressText = view.findViewById(R.id.weeklyProgressText);
    }
    
    private void setupRecyclerView() {
        workoutAdapter = new WeeklyWorkoutAdapter(getContext(), weeklyWorkouts, this);
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutsRecyclerView.setAdapter(workoutAdapter);
        
        // Setup swipe gestures
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToActionCallback());
        itemTouchHelper.attachToRecyclerView(workoutsRecyclerView);
    }
    
    private void loadWeeklyWorkouts() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            weeklyWorkouts.clear();
            weeklyWorkouts.addAll(weeklyWorkoutController.getCurrentWeekWorkouts(currentUser.getUserId()));
            workoutAdapter.updateWorkouts(weeklyWorkouts);
            updateEmptyState();
            updateProgressText();
        }
    }
    
    private void updateEmptyState() {
        if (weeklyWorkouts.isEmpty()) {
            emptyWorkoutsLayout.setVisibility(View.VISIBLE);
            workoutsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyWorkoutsLayout.setVisibility(View.GONE);
            workoutsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateProgressText() {
        int totalWorkouts = weeklyWorkouts.size();
        int completedWorkouts = 0;
        
        for (WeeklyWorkout workout : weeklyWorkouts) {
            if (workout.isCompleted()) {
                completedWorkouts++;
            }
        }
        
        weeklyProgressText.setText(String.format("Weekly Progress: %d/%d workouts completed", 
                                                 completedWorkouts, totalWorkouts));
    }
    
    // WeeklyWorkoutAdapter.OnWorkoutActionListener methods
    @Override
    public void onWorkoutCompleted(WeeklyWorkout workout, int position) {
        completeWorkout(workout, position);
    }
    
    @Override
    public void onWorkoutDeleted(WeeklyWorkout workout, int position) {
        deleteWorkout(workout, position);
    }
    
    @Override
    public void onWorkoutClicked(WeeklyWorkout workout) {
        // Show workout detail bottom sheet
        // TODO: Implement workout detail bottom sheet
        showWorkoutDetails(workout);
    }
    
    private void completeWorkout(WeeklyWorkout workout, int position) {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) return;
        
        // Mark workout as completed in database
        boolean success = weeklyWorkoutController.markWorkoutCompleted(workout.getWeeklyWorkoutId());
        
        if (success) {
            // Update local data
            workout.setCompleted(true);
            workout.setCompletionTime(new Date());
            workoutAdapter.notifyItemChanged(position);
            
            // Create workout history entry
            createWorkoutHistory(workout);
            
            updateProgressText();
            
            Toast.makeText(getContext(), "Workout completed! 🎉", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to complete workout", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteWorkout(WeeklyWorkout workout, int position) {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Workout")
               .setMessage("Are you sure you want to remove this workout from your weekly schedule?")
               .setPositiveButton("Delete", (dialog, which) -> {
                   boolean success = weeklyWorkoutController.deleteWeeklyWorkout(workout.getWeeklyWorkoutId());
                   
                   if (success) {
                       weeklyWorkouts.remove(position);
                       workoutAdapter.notifyItemRemoved(position);
                       updateEmptyState();
                       updateProgressText();
                       
                       Toast.makeText(getContext(), "Workout removed", Toast.LENGTH_SHORT).show();
                   } else {
                       Toast.makeText(getContext(), "Failed to remove workout", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void createWorkoutHistory(WeeklyWorkout workout) {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) return;
        
        try {
            // Create workout history record
            Date now = new Date();
            Date startTime = new Date(now.getTime() - (45 * 60 * 1000)); // Assume 45 minutes ago as start time
            
            // Get exercise count from routine (simplified)
            int exerciseCount = getExerciseCountForWorkout(workout);
            
            String notes = "Completed via FitLife app";
            
            workoutHistoryController.createWorkoutHistory(
                currentUser.getUserId(),
                workout.getRoutineId(),
                now,                    // workout date
                startTime,              // start time
                now,                    // completion time
                exerciseCount,          // exercises completed
                notes                   // notes
            );
            
        } catch (Exception e) {
            // Log error but don't fail the completion
            e.printStackTrace();
        }
    }
    
    private int getExerciseCountForWorkout(WeeklyWorkout workout) {
        // This is a simplified implementation
        // In a full app, you'd query the routine_exercises table
        return 5; // Default value
    }
    
    private void showWorkoutDetails(WeeklyWorkout workout) {
        // Create a simple info dialog for now
        StringBuilder details = new StringBuilder();
        
        if (workout.getRoutine() != null) {
            details.append("Routine: ").append(workout.getRoutine().getRoutineName()).append("\n");
            details.append("Description: ").append(workout.getRoutine().getDescription()).append("\n\n");
        }
        
        details.append("Scheduled Date: ").append(java.text.DateFormat.getDateInstance().format(workout.getScheduledDate())).append("\n");
        details.append("Status: ").append(workout.isCompleted() ? "✅ Completed" : "⏳ Pending").append("\n");
        
        if (workout.isCompleted() && workout.getCompletionTime() != null) {
            details.append("Completed: ").append(java.text.DateFormat.getDateTimeInstance().format(workout.getCompletionTime()));
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Workout Details")
               .setMessage(details.toString())
               .setPositiveButton("OK", null);
               
        if (!workout.isCompleted()) {
            builder.setNeutralButton("Mark Complete", (dialog, which) -> {
                int position = weeklyWorkouts.indexOf(workout);
                if (position >= 0) {
                    completeWorkout(workout, position);
                }
            });
        }
        
        builder.show();
    }
    
    // PatternShakeDetector.OnPatternShakeListener method
    @Override
    public void onPatternShakeDetected() {
        showResetWeeklyWorkoutsDialog();
    }
    
    private void showResetWeeklyWorkoutsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Weekly Workouts")
               .setMessage("Are you sure you want to reset all workouts for this week? This will remove all scheduled workouts.")
               .setIcon(R.drawable.ic_warning)
               .setPositiveButton("Reset", (dialog, which) -> {
                   resetWeeklyWorkouts();
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void resetWeeklyWorkouts() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            boolean success = weeklyWorkoutController.clearCurrentWeekWorkouts(currentUser.getUserId());
            
            if (success) {
                weeklyWorkouts.clear();
                workoutAdapter.notifyDataSetChanged();
                updateEmptyState();
                updateProgressText();
                
                Toast.makeText(getContext(), "Weekly workouts reset successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to reset weekly workouts", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // Custom ItemTouchHelper for swipe gestures
    private class SwipeToActionCallback extends ItemTouchHelper.SimpleCallback {
        
        public SwipeToActionCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }
        
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false; // We don't support move
        }
        
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            WeeklyWorkout workout = weeklyWorkouts.get(position);
            
            if (direction == ItemTouchHelper.RIGHT) {
                // Swipe right: Complete workout
                if (!workout.isCompleted()) {
                    completeWorkout(workout, position);
                } else {
                    // Already completed, revert the swipe
                    workoutAdapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Workout already completed", Toast.LENGTH_SHORT).show();
                }
            } else if (direction == ItemTouchHelper.LEFT) {
                // Swipe left: Delete workout
                deleteWorkout(workout, position);
            }
        }
        
        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getAdapterPosition();
            WeeklyWorkout workout = weeklyWorkouts.get(position);
            
            // Allow both directions, but visually indicate what each does
            return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }
    }
}