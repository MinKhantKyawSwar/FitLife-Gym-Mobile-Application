package com.example.fitlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.activities.CreateRoutineActivity;
import com.example.fitlife.adapters.CurrentWorkoutHomeAdapter;
import com.example.fitlife.adapters.RecommendationAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.RoutineController;
import com.example.fitlife.controllers.WeeklyWorkoutController;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WeeklyWorkout;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    
    private TextView greetingText;
    private TextView taglineText;
    private Button createWorkoutBtn;
    private Button checkWorkoutListBtn;
    private TextView seeAllRecommendations;
    private TextView seeAllWorkouts;
    private TextView viewMoreShare;
    
    private RecyclerView recommendationsRecyclerView;
    private RecyclerView currentWorkoutsRecyclerView;
    
    private RecommendationAdapter recommendationAdapter;
    private CurrentWorkoutHomeAdapter currentWorkoutAdapter;
    
    private RoutineController routineController;
    private WeeklyWorkoutController weeklyWorkoutController;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initializeViews(view);
        setupRecyclerViews();
        loadHomeContent();
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        greetingText = view.findViewById(R.id.greetingText);
        taglineText = view.findViewById(R.id.taglineText);
        createWorkoutBtn = view.findViewById(R.id.createWorkoutBtn);
        checkWorkoutListBtn = view.findViewById(R.id.checkWorkoutListBtn);
        seeAllRecommendations = view.findViewById(R.id.seeAllRecommendations);
        seeAllWorkouts = view.findViewById(R.id.seeAllWorkouts);
        viewMoreShare = view.findViewById(R.id.viewMoreShare);
        
        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView);
        currentWorkoutsRecyclerView = view.findViewById(R.id.currentWorkoutsRecyclerView);
        
        routineController = new RoutineController(requireContext());
        weeklyWorkoutController = new WeeklyWorkoutController(requireContext());
    }
    
    private void setupRecyclerViews() {
        // Setup recommendations RecyclerView (horizontal)
        recommendationAdapter = new RecommendationAdapter(new ArrayList<>(), routine -> {
            // Handle recommendation click - navigate to workout details
            // TODO: Implement navigation
        });
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendationsRecyclerView.setAdapter(recommendationAdapter);
        
        // Setup current workouts RecyclerView (vertical)
        currentWorkoutAdapter = new CurrentWorkoutHomeAdapter(new ArrayList<>(), workout -> {
            // Handle workout click - navigate to workout details
            // TODO: Implement navigation
        });
        currentWorkoutsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        currentWorkoutsRecyclerView.setAdapter(currentWorkoutAdapter);
    }
    
    private void loadHomeContent() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            // Update greeting with username
            String username = currentUser.getUsername();
            if (username != null && !username.isEmpty()) {
                greetingText.setText("Hi, " + username + "!");
            }
            
            // Load recommendations (user's routines)
            loadRecommendations(currentUser.getUserId());
            
            // Load current workouts
            loadCurrentWorkouts(currentUser.getUserId());
        }
    }
    
    private void loadRecommendations(int userId) {
        List<Routine> routines = routineController.getUserRoutines();
        
        // Limit to first 5 routines for recommendations
        List<Routine> recommendations = new ArrayList<>();
        int count = Math.min(5, routines.size());
        for (int i = 0; i < count; i++) {
            recommendations.add(routines.get(i));
        }
        
        recommendationAdapter.updateRecommendations(recommendations);
    }
    
    private void loadCurrentWorkouts(int userId) {
        List<WeeklyWorkout> workouts = weeklyWorkoutController.getCurrentWeekWorkouts(userId);
        
        // Limit to first 3 workouts for home display
        List<WeeklyWorkout> displayWorkouts = new ArrayList<>();
        int count = Math.min(3, workouts.size());
        for (int i = 0; i < count; i++) {
            displayWorkouts.add(workouts.get(i));
        }
        
        currentWorkoutAdapter.updateWorkouts(displayWorkouts);
    }
    
    private void setupClickListeners() {
        createWorkoutBtn.setOnClickListener(v -> {
            // Navigate to Create Routine Activity
            Intent intent = new Intent(requireContext(), CreateRoutineActivity.class);
            startActivity(intent);
        });
        
        checkWorkoutListBtn.setOnClickListener(v -> {
            // Navigate to My Workouts tab
            if (getActivity() instanceof com.example.fitlife.MainActivity) {
                com.example.fitlife.MainActivity mainActivity = 
                    (com.example.fitlife.MainActivity) getActivity();
                // Switch to workouts tab
                mainActivity.switchToWorkoutsTab();
            }
        });
        
        seeAllRecommendations.setOnClickListener(v -> {
            // Navigate to recommendations/workouts page
            // TODO: Implement navigation
        });
        
        seeAllWorkouts.setOnClickListener(v -> {
            // Navigate to My Workouts tab
            if (getActivity() instanceof com.example.fitlife.MainActivity) {
                com.example.fitlife.MainActivity mainActivity = 
                    (com.example.fitlife.MainActivity) getActivity();
                mainActivity.switchToWorkoutsTab();
            }
        });
        
        viewMoreShare.setOnClickListener(v -> {
            // Show share options
            // TODO: Implement share functionality
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadHomeContent();
    }
}
