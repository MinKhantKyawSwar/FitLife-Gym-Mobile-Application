package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.MyWorkoutsActivity;
import com.example.fitlife.R;
import java.util.List;

public class CurrentWorkoutExerciseAdapter extends RecyclerView.Adapter<CurrentWorkoutExerciseAdapter.ViewHolder> {
    private List<MyWorkoutsActivity.WorkoutExerciseItem> exercises;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(long exerciseId);
    }

    public CurrentWorkoutExerciseAdapter(List<MyWorkoutsActivity.WorkoutExerciseItem> exercises) {
        this(exercises, null);
    }

    public CurrentWorkoutExerciseAdapter(List<MyWorkoutsActivity.WorkoutExerciseItem> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_current_workout_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyWorkoutsActivity.WorkoutExerciseItem exercise = exercises.get(position);
        holder.textExerciseName.setText(exercise.exerciseName);
        holder.textSetsReps.setText("Set : " + exercise.sets + ", Reps: " + exercise.reps);
        holder.textStatus.setText(exercise.status);

        // Change background color if completed
        if ("Completed".equals(exercise.status)) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.completed));
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.surface));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExerciseClick(exercise.exerciseId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public long getExerciseIdAt(int position) {
        return exercises.get(position).exerciseId;
    }

    public void markComplete(int position) {
        if (position >= 0 && position < exercises.size()) {
            exercises.get(position).status = "Completed";
            notifyItemChanged(position);
        }
    }

    public boolean areAllCompleted() {
        for (MyWorkoutsActivity.WorkoutExerciseItem item : exercises) {
            if (!"Completed".equals(item.status)) {
                return false;
            }
        }
        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textExerciseName;
        TextView textSetsReps;
        TextView textStatus;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            textSetsReps = itemView.findViewById(R.id.textSetsReps);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
