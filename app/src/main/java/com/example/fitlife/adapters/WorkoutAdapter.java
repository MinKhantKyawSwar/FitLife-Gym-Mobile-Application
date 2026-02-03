package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.button.MaterialButton;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.R;
import com.example.fitlife.models.Workout;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
    private List<Workout> workouts;
    private OnWorkoutClickListener listener;
    private OnEditClickListener editListener;
    private OnPlayClickListener playListener;
    private OnShareClickListener shareListener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }

    public interface OnEditClickListener {
        void onEditClick(Workout workout);
    }

    public interface OnPlayClickListener {
        void onPlayClick(Workout workout);
    }

    public interface OnShareClickListener {
        void onShareClick(Workout workout);
    }

    public WorkoutAdapter(List<Workout> workouts, OnWorkoutClickListener listener,
                          OnEditClickListener editListener,
                          OnPlayClickListener playListener, OnShareClickListener shareListener) {
        this.workouts = workouts;
        this.listener = listener;
        this.editListener = editListener;
        this.playListener = playListener;
        this.shareListener = shareListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.textWorkoutName.setText(workout.getName());
        holder.textExerciseCount.setText(workout.getExerciseCount() + " Exercises");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
        });

        holder.buttonEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(workout);
            }
        });

        holder.buttonShare.setOnClickListener(v -> {
            if (shareListener != null) {
                shareListener.onShareClick(workout);
            }
        });

        holder.buttonStartWorkout.setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onPlayClick(workout);
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
        ImageButton buttonEdit;
        ImageButton buttonShare;
        MaterialButton buttonStartWorkout;

        ViewHolder(View itemView) {
            super(itemView);
            textWorkoutName = itemView.findViewById(R.id.textWorkoutName);
            textExerciseCount = itemView.findViewById(R.id.textExerciseCount);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonShare = itemView.findViewById(R.id.buttonShare);
            buttonStartWorkout = itemView.findViewById(R.id.buttonStartWorkout);
        }
    }
}
