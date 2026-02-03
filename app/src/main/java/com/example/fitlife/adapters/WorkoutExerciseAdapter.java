package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.CreateWorkoutActivity;
import com.example.fitlife.R;
import java.util.List;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ViewHolder> {
    private List<CreateWorkoutActivity.WorkoutExerciseItem> exercises;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEditClick(CreateWorkoutActivity.WorkoutExerciseItem exercise);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(long exerciseId);
    }

    public WorkoutExerciseAdapter(List<CreateWorkoutActivity.WorkoutExerciseItem> exercises,
                                  OnEditClickListener editListener,
                                  OnDeleteClickListener deleteListener) {
        this.exercises = exercises;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CreateWorkoutActivity.WorkoutExerciseItem exercise = exercises.get(position);
        holder.textExerciseName.setText(exercise.exerciseName);
        holder.textSetsReps.setText("Set : " + exercise.sets + ", Reps: " + exercise.reps);

        holder.imageEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(exercise);
            }
        });

        holder.imageDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(exercise.exerciseId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textExerciseName;
        TextView textSetsReps;
        ImageView imageEdit;
        ImageView imageDelete;

        ViewHolder(View itemView) {
            super(itemView);
            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            textSetsReps = itemView.findViewById(R.id.textSetsReps);
            imageEdit = itemView.findViewById(R.id.imageEdit);
            imageDelete = itemView.findViewById(R.id.imageDelete);
        }
    }
}
