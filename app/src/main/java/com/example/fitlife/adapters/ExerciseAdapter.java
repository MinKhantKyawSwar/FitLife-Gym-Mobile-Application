package com.example.fitlife.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitlife.R;
import com.example.fitlife.models.Exercise;
import com.example.fitlife.utils.ImageHelper;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private OnExerciseClickListener listener;
    private final int layoutResId;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exercises, OnExerciseClickListener listener) {
        this(exercises, listener, R.layout.item_exercise);
    }

    /** Use for home recommendations: pass R.layout.item_exercise_recommendation for fixed-size cards. */
    public ExerciseAdapter(List<Exercise> exercises, OnExerciseClickListener listener, int layoutResId) {
        this.exercises = exercises;
        this.listener = listener;
        this.layoutResId = layoutResId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.textExerciseName.setText(exercise.getName());
        holder.textSetsReps.setText("Set: " + exercise.getSets() + ", Reps: " + exercise.getReps());

        boolean useFixedCard = (layoutResId == R.layout.item_exercise_recommendation);
        if (exercise.getImagePath() != null && !exercise.getImagePath().isEmpty()) {
            Bitmap bitmap = ImageHelper.loadImage(exercise.getImagePath());
            holder.imageExercise.setImageBitmap(bitmap);
            holder.imageExercise.setVisibility(View.VISIBLE);
        } else {
            holder.imageExercise.setImageBitmap(null);
            holder.imageExercise.setVisibility(useFixedCard ? View.VISIBLE : View.GONE);
        }

        // Only "View Details" button opens detail; card click does nothing
        if (holder.buttonViewDetails != null) {
            holder.buttonViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageExercise;
        TextView textExerciseName;
        TextView textSetsReps;
        MaterialButton buttonViewDetails;

        ViewHolder(View itemView) {
            super(itemView);
            imageExercise = itemView.findViewById(R.id.imageExercise);
            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            textSetsReps = itemView.findViewById(R.id.textSetsReps);
            buttonViewDetails = itemView.findViewById(R.id.buttonViewDetails);
        }
    }
}
