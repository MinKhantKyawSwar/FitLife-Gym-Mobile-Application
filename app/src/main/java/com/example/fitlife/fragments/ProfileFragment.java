package com.example.fitlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.activities.AuthActivity;
import com.example.fitlife.adapters.WorkoutHistoryAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.UserPreferencesController;
import com.example.fitlife.controllers.WorkoutHistoryController;
import com.example.fitlife.dialogs.EditProfileDialog;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WorkoutHistory;
import com.example.fitlife.utils.SMSHelper;
import com.example.fitlife.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements 
    EditProfileDialog.OnProfileUpdateListener, WorkoutHistoryAdapter.OnWorkoutHistoryClickListener {
    
    private TextView usernameText;
    private TextView emailText;
    private Button editProfileBtn;
    private Button changeUsernameBtn;
    private Button changePasswordBtn;
    private Button themeToggleBtn;
    private Button workoutHistoryBtn;
    private Button shareEquipmentBtn;
    private Button logoutBtn;
    
    // Workout History Section
    private TextView workoutStatsText;
    private RecyclerView workoutHistoryRecyclerView;
    private LinearLayout emptyHistoryLayout;
    private WorkoutHistoryAdapter historyAdapter;
    
    private AuthController authController;
    private UserPreferencesController preferencesController;
    private WorkoutHistoryController workoutHistoryController;
    private SMSHelper smsHelper;
    
    private List<WorkoutHistory> workoutHistoryList;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        authController = new AuthController(requireContext());
        preferencesController = new UserPreferencesController(requireContext());
        workoutHistoryController = new WorkoutHistoryController(requireContext());
        smsHelper = new SMSHelper(requireContext());
        workoutHistoryList = new ArrayList<>();
        
        initializeViews(view);
        loadUserInfo();
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);
        editProfileBtn = view.findViewById(R.id.editProfileBtn);
        changeUsernameBtn = view.findViewById(R.id.changeUsernameBtn);
        changePasswordBtn = view.findViewById(R.id.changePasswordBtn);
        themeToggleBtn = view.findViewById(R.id.themeToggleBtn);
        workoutHistoryBtn = view.findViewById(R.id.workoutHistoryBtn);
        shareEquipmentBtn = view.findViewById(R.id.shareEquipmentBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        
        // Workout History Views
        workoutStatsText = view.findViewById(R.id.workoutStatsText);
        workoutHistoryRecyclerView = view.findViewById(R.id.workoutHistoryRecyclerView);
        emptyHistoryLayout = view.findViewById(R.id.emptyHistoryLayout);
        
        // Setup RecyclerView
        historyAdapter = new WorkoutHistoryAdapter(getContext(), workoutHistoryList, this);
        workoutHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutHistoryRecyclerView.setAdapter(historyAdapter);
    }
    
    private void loadUserInfo() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            usernameText.setText(currentUser.getUsername());
            emailText.setText(currentUser.getEmail());
            updateThemeButtonText(currentUser.getUserId());
            loadWorkoutHistory(currentUser.getUserId());
            loadWorkoutStats(currentUser.getUserId());
        }
    }
    
    private void loadWorkoutHistory(int userId) {
        workoutHistoryList.clear();
        workoutHistoryList.addAll(workoutHistoryController.getUserWorkoutHistory(userId, 10)); // Show last 10 workouts
        historyAdapter.updateWorkoutHistory(workoutHistoryList);
        
        updateEmptyHistoryState();
    }
    
    private void loadWorkoutStats(int userId) {
        int[] stats = workoutHistoryController.getWorkoutStats(userId);
        int totalWorkouts = stats[0];
        int totalExercises = stats[1];
        int totalDurationHours = stats[2] / 60;
        
        workoutStatsText.setText(String.format("Total: %d workouts • %d exercises • %dh", 
            totalWorkouts, totalExercises, totalDurationHours));
    }
    
    private void updateEmptyHistoryState() {
        if (workoutHistoryList.isEmpty()) {
            emptyHistoryLayout.setVisibility(View.VISIBLE);
            workoutHistoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyHistoryLayout.setVisibility(View.GONE);
            workoutHistoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateThemeButtonText(int userId) {
        String currentTheme = preferencesController.getCurrentThemeDisplayName(userId);
        String oppositeTheme = "Dark".equals(currentTheme) ? "Light" : "Dark";
        themeToggleBtn.setText("Switch to " + oppositeTheme + " Theme (Current: " + currentTheme + ")");
    }
    
    private void setupClickListeners() {
        editProfileBtn.setOnClickListener(v -> {
            // Show profile editing options
            showProfileEditOptions();
        });
        
        changeUsernameBtn.setOnClickListener(v -> {
            EditProfileDialog dialog = EditProfileDialog.newInstance(EditProfileDialog.EDIT_USERNAME);
            dialog.show(getChildFragmentManager(), "EditUsernameDialog");
        });
        
        changePasswordBtn.setOnClickListener(v -> {
            EditProfileDialog dialog = EditProfileDialog.newInstance(EditProfileDialog.EDIT_PASSWORD);
            dialog.show(getChildFragmentManager(), "EditPasswordDialog");
        });
        
        themeToggleBtn.setOnClickListener(v -> {
            User currentUser = AuthController.getCurrentUser();
            if (currentUser != null) {
                boolean success = preferencesController.toggleTheme(currentUser.getUserId());
                if (success) {
                    updateThemeButtonText(currentUser.getUserId());
                    Toast.makeText(getContext(), "Theme updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Optionally restart activity to fully apply theme
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to update theme", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        workoutHistoryBtn.setOnClickListener(v -> {
            // TODO: Show workout history
            Toast.makeText(getContext(), "Workout history feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        shareEquipmentBtn.setOnClickListener(v -> {
            // Show equipment sharing options
            showEquipmentSharingOptions();
        });
        
        logoutBtn.setOnClickListener(v -> {
            authController.logout();
            
            // Navigate to AuthActivity
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
    
    private void showProfileEditOptions() {
        String[] options = {"Change Username", "Change Password"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Profile")
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0:
                           EditProfileDialog usernameDialog = EditProfileDialog.newInstance(EditProfileDialog.EDIT_USERNAME);
                           usernameDialog.show(getChildFragmentManager(), "EditUsernameDialog");
                           break;
                       case 1:
                           EditProfileDialog passwordDialog = EditProfileDialog.newInstance(EditProfileDialog.EDIT_PASSWORD);
                           passwordDialog.show(getChildFragmentManager(), "EditPasswordDialog");
                           break;
                   }
               })
               .show();
    }
    
    private void showEquipmentSharingOptions() {
        Toast.makeText(getContext(), "Equipment sharing feature - add routines to your weekly schedule to generate equipment lists", Toast.LENGTH_LONG).show();
    }
    
    // EditProfileDialog.OnProfileUpdateListener methods
    @Override
    public void onUsernameUpdated(String newUsername) {
        usernameText.setText(newUsername);
        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPasswordUpdated() {
        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
    }
    
    // WorkoutHistoryAdapter.OnWorkoutHistoryClickListener methods
    @Override
    public void onWorkoutHistoryClick(WorkoutHistory workoutHistory) {
        showWorkoutHistoryDetails(workoutHistory);
    }
    
    private void showWorkoutHistoryDetails(WorkoutHistory history) {
        StringBuilder details = new StringBuilder();
        details.append("Workout Date: ").append(java.text.DateFormat.getDateInstance().format(history.getWorkoutDate())).append("\n\n");
        
        if (history.getRoutine() != null) {
            details.append("Routine: ").append(history.getRoutine().getRoutineName()).append("\n");
        }
        
        if (history.getTotalDurationMinutes() > 0) {
            int hours = history.getTotalDurationMinutes() / 60;
            int minutes = history.getTotalDurationMinutes() % 60;
            details.append("Duration: ");
            if (hours > 0) {
                details.append(hours).append("h ");
            }
            details.append(minutes).append("m\n");
        }
        
        if (history.getExercisesCompleted() > 0) {
            details.append("Exercises Completed: ").append(history.getExercisesCompleted()).append("\n");
        }
        
        if (history.getNotes() != null && !history.getNotes().trim().isEmpty()) {
            details.append("\nNotes: ").append(history.getNotes());
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Workout Details")
               .setMessage(details.toString())
               .setPositiveButton("OK", null)
               .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }
}