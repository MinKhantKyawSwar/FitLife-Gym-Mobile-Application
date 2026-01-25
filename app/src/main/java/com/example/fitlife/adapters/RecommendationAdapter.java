package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.RoutineExercise;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {
    
    private List<Routine> routines;
    private OnRecommendationClickListener listener;
    
    public interface OnRecommendationClickListener {
        void onRecommendationClicked(Routine routine);
    }
    
    public RecommendationAdapter(List<Routine> routines, OnRecommendationClickListener listener) {
        this.routines = routines != null ? routines : new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation_card, parent, false);
        return new RecommendationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine);
    }
    
    @Override
    public int getItemCount() {
        return routines.size();
    }
    
    public void updateRecommendations(List<Routine> newRoutines) {
        this.routines = newRoutines != null ? newRoutines : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private TextView workoutNameText;
        private TextView workoutDetailsText;
        
        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutNameText = itemView.findViewById(R.id.workoutNameText);
            workoutDetailsText = itemView.findViewById(R.id.workoutDetailsText);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRecommendationClicked(routines.get(position));
                }
            });
        }
        
        public void bind(Routine routine) {
            workoutNameText.setText(routine.getRoutineName());
            
            // Get first exercise details for display
            if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
                RoutineExercise firstExercise = routine.getExercises().get(0);
                workoutDetailsText.setText(String.format("Set: %d, Reps: %d", 
                    firstExercise.getSets(), firstExercise.getReps()));
            } else {
                workoutDetailsText.setText("No exercises");
            }
        }
    }
}
