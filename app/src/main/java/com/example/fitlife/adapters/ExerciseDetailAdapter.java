package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.RoutineExercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDetailAdapter extends RecyclerView.Adapter<ExerciseDetailAdapter.ExerciseViewHolder> {
    
    private List<RoutineExercise> exercises;
    
    public ExerciseDetailAdapter(List<RoutineExercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ExerciseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        RoutineExercise routineExercise = exercises.get(position);
        holder.bind(routineExercise);
    }
    
    @Override
    public int getItemCount() {
        return exercises.size();
    }
    
    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView text1;
        private TextView text2;
        
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
        
        public void bind(RoutineExercise routineExercise) {
            if (routineExercise.getExercise() != null) {
                text1.setText(routineExercise.getExercise().getExerciseName());
                text2.setText(String.format("Sets: %d, Reps: %d", 
                    routineExercise.getSets(), routineExercise.getReps()));
            } else {
                text1.setText("Unknown Exercise");
                text2.setText(String.format("Sets: %d, Reps: %d", 
                    routineExercise.getSets(), routineExercise.getReps()));
            }
        }
    }
}
