package com.example.fitlife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.WorkoutHistory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutHistoryViewHolder> {
    
    public interface OnWorkoutHistoryClickListener {
        void onWorkoutHistoryClick(WorkoutHistory workoutHistory);
    }
    
    private Context context;
    private List<WorkoutHistory> workoutHistory;
    private OnWorkoutHistoryClickListener listener;
    
    public WorkoutHistoryAdapter(Context context, List<WorkoutHistory> workoutHistory, OnWorkoutHistoryClickListener listener) {
        this.context = context;
        this.workoutHistory = workoutHistory;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public WorkoutHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout_history, parent, false);
        return new WorkoutHistoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorkoutHistoryViewHolder holder, int position) {
        WorkoutHistory history = workoutHistory.get(position);
        holder.bind(history);
    }
    
    @Override
    public int getItemCount() {
        return workoutHistory.size();
    }
    
    public void updateWorkoutHistory(List<WorkoutHistory> newWorkoutHistory) {
        this.workoutHistory = newWorkoutHistory;
        notifyDataSetChanged();
    }
    
    public class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private TextView routineNameText;
        private TextView workoutDateText;
        private TextView durationText;
        private TextView exercisesCompletedText;
        private TextView completionTimeText;
        
        public WorkoutHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.workoutHistoryCardView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            workoutDateText = itemView.findViewById(R.id.workoutDateText);
            durationText = itemView.findViewById(R.id.durationText);
            exercisesCompletedText = itemView.findViewById(R.id.exercisesCompletedText);
            completionTimeText = itemView.findViewById(R.id.completionTimeText);
            
            itemView.setOnClickListener(this);
        }
        
        public void bind(WorkoutHistory history) {
            // Set routine name
            if (history.getRoutine() != null) {
                routineNameText.setText(history.getRoutine().getRoutineName());
            } else {
                routineNameText.setText("Unknown Routine");
            }
            
            // Format workout date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            workoutDateText.setText(dateFormat.format(history.getWorkoutDate()));
            
            // Format duration
            if (history.getTotalDurationMinutes() > 0) {
                int hours = history.getTotalDurationMinutes() / 60;
                int minutes = history.getTotalDurationMinutes() % 60;
                
                if (hours > 0) {
                    durationText.setText(String.format("%dh %dm", hours, minutes));
                } else {
                    durationText.setText(String.format("%dm", minutes));
                }
                durationText.setVisibility(View.VISIBLE);
            } else {
                durationText.setVisibility(View.GONE);
            }
            
            // Exercises completed
            if (history.getExercisesCompleted() > 0) {
                exercisesCompletedText.setText(history.getExercisesCompleted() + " exercises completed");
                exercisesCompletedText.setVisibility(View.VISIBLE);
            } else {
                exercisesCompletedText.setVisibility(View.GONE);
            }
            
            // Completion time
            if (history.getCompletionTime() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                completionTimeText.setText("Completed at " + timeFormat.format(history.getCompletionTime()));
                completionTimeText.setVisibility(View.VISIBLE);
            } else {
                completionTimeText.setVisibility(View.GONE);
            }
        }
        
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onWorkoutHistoryClick(workoutHistory.get(position));
            }
        }
    }
}