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

public class RoutineWorkoutAdapter extends RecyclerView.Adapter<RoutineWorkoutAdapter.RoutineViewHolder> {
    
    private List<Routine> routines;
    private OnRoutineClickListener listener;
    
    public interface OnRoutineClickListener {
        void onRoutineClicked(Routine routine);
    }
    
    public RoutineWorkoutAdapter(List<Routine> routines, OnRoutineClickListener listener) {
        this.routines = routines != null ? routines : new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine_workout, parent, false);
        return new RoutineViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine);
    }
    
    @Override
    public int getItemCount() {
        return routines.size();
    }
    
    public void updateRoutines(List<Routine> newRoutines) {
        this.routines = newRoutines != null ? newRoutines : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    class RoutineViewHolder extends RecyclerView.ViewHolder {
        private TextView routineNameText;
        private TextView exerciseCountText;
        
        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            exerciseCountText = itemView.findViewById(R.id.exerciseCountText);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRoutineClicked(routines.get(position));
                }
            });
        }
        
        public void bind(Routine routine) {
            routineNameText.setText(routine.getRoutineName());
            
            int exerciseCount = routine.getExercises() != null ? routine.getExercises().size() : 0;
            exerciseCountText.setText(exerciseCount + " Exercises");
        }
    }
}
