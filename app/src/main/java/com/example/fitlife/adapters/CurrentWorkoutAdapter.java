package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.R;
import com.example.fitlife.models.CurrentWorkout;
import java.util.List;

public class CurrentWorkoutAdapter extends RecyclerView.Adapter<CurrentWorkoutAdapter.ViewHolder> {
    private List<CurrentWorkout> workouts;
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(CurrentWorkout workout);
    }

    public CurrentWorkoutAdapter(List<CurrentWorkout> workouts, OnWorkoutClickListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_current_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CurrentWorkout workout = workouts.get(position);
        holder.textWorkoutName.setText(workout.getWorkoutName());
        holder.textExerciseCount.setText(workout.getExerciseCount() + " Exercises");
        holder.textStatus.setText(workout.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textWorkoutName;
        TextView textExerciseCount;
        TextView textStatus;

        ViewHolder(View itemView) {
            super(itemView);
            textWorkoutName = itemView.findViewById(R.id.textWorkoutName);
            textExerciseCount = itemView.findViewById(R.id.textExerciseCount);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
