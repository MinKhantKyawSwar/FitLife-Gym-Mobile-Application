package com.example.fitlife.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.R;
import com.example.fitlife.models.Exercise;
import java.util.List;

public class ExerciseSelectAdapter extends RecyclerView.Adapter<ExerciseSelectAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private OnExerciseSelectListener listener;

    public interface OnExerciseSelectListener {
        void onExerciseSelect(long exerciseId);
    }

    public ExerciseSelectAdapter(List<Exercise> exercises, OnExerciseSelectListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.textExerciseName.setText(exercise.getName());
        holder.textSetsReps.setText("Set : " + exercise.getSets() + ", Reps: " + exercise.getReps());

        holder.checkboxSelect.setOnCheckedChangeListener(null);
        holder.checkboxSelect.setChecked(false);
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onExerciseSelect(exercise.getExerciseId());
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
        CheckBox checkboxSelect;

        ViewHolder(View itemView) {
            super(itemView);
            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            textSetsReps = itemView.findViewById(R.id.textSetsReps);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }
    }
}
