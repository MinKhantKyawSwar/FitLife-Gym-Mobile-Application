package com.example.fitlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.activities.CreateRoutineActivity;
import com.example.fitlife.adapters.RoutineWorkoutAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.RoutineController;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.User;

import java.util.ArrayList;
import java.util.List;

public class WorkoutsFragment extends Fragment implements RoutineWorkoutAdapter.OnRoutineClickListener {
    
    private RecyclerView routinesRecyclerView;
    private Button createRoutineBtn;
    private RoutineWorkoutAdapter routineAdapter;
    private RoutineController routineController;
    private List<Routine> routines;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);
        
        initializeControllers();
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadRoutines();
        
        return view;
    }
    
    private void initializeControllers() {
        routineController = new RoutineController(requireContext());
        routines = new ArrayList<>();
    }
    
    private void initializeViews(View view) {
        routinesRecyclerView = view.findViewById(R.id.routinesRecyclerView);
        createRoutineBtn = view.findViewById(R.id.createRoutineBtn);
    }
    
    private void setupRecyclerView() {
        routineAdapter = new RoutineWorkoutAdapter(routines, this);
        routinesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        routinesRecyclerView.setAdapter(routineAdapter);
    }
    
    private void setupClickListeners() {
        createRoutineBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateRoutineActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadRoutines() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            routines.clear();
            routines.addAll(routineController.getUserRoutines());
            routineAdapter.updateRoutines(routines);
        }
    }
    
    @Override
    public void onRoutineClicked(Routine routine) {
        // Navigate to workout detail page
        if (getActivity() instanceof com.example.fitlife.MainActivity) {
            com.example.fitlife.MainActivity mainActivity = 
                (com.example.fitlife.MainActivity) getActivity();
            mainActivity.showRoutineDetail(routine);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadRoutines();
    }
}
