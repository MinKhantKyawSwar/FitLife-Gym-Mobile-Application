package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.WeeklyWorkout;

import java.util.ArrayList;
import java.util.List;

public class CurrentWorkoutHomeAdapter extends RecyclerView.Adapter<CurrentWorkoutHomeAdapter.CurrentWorkoutViewHolder> {
    
    private List<WeeklyWorkout> workouts;
    private OnWorkoutClickListener listener;
    
    public interface OnWorkoutClickListener {
        void onWorkoutClicked(WeeklyWorkout workout);
    }
    
    public CurrentWorkoutHomeAdapter(List<WeeklyWorkout> workouts, OnWorkoutClickListener listener) {
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CurrentWorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_current_workout_home, parent, false);
        return new CurrentWorkoutViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CurrentWorkoutViewHolder holder, int position) {
        WeeklyWorkout workout = workouts.get(position);
        holder.bind(workout);
    }
    
    @Override
    public int getItemCount() {
        return workouts.size();
    }
    
    public void updateWorkouts(List<WeeklyWorkout> newWorkouts) {
        this.workouts = newWorkouts != null ? newWorkouts : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    class CurrentWorkoutViewHolder extends RecyclerView.ViewHolder {
        private TextView workoutNameText;
        private TextView exerciseCountText;
        private TextView statusText;
        
        public CurrentWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutNameText = itemView.findViewById(R.id.workoutNameText);
            exerciseCountText = itemView.findViewById(R.id.exerciseCountText);
            statusText = itemView.findViewById(R.id.statusText);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onWorkoutClicked(workouts.get(position));
                }
            });
        }
        
        public void bind(WeeklyWorkout workout) {
            if (workout.getRoutine() != null) {
                workoutNameText.setText(workout.getRoutine().getRoutineName());
                
                int exerciseCount = workout.getRoutine().getExercises() != null 
                    ? workout.getRoutine().getExercises().size() 
                    : 0;
                exerciseCountText.setText(exerciseCount + " Exercises");
            } else {
                workoutNameText.setText("Unknown Workout");
                exerciseCountText.setText("0 Exercises");
            }
            
            // Set status
            if (workout.isCompleted()) {
                statusText.setText("Completed");
            } else {
                statusText.setText("Pending");
            }
        }
    }
}
