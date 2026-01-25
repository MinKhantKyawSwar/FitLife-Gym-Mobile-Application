package com.example.fitlife.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.WeeklyWorkout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WeeklyWorkoutAdapter extends RecyclerView.Adapter<WeeklyWorkoutAdapter.WeeklyWorkoutViewHolder> {
    
    public interface OnWorkoutActionListener {
        void onWorkoutCompleted(WeeklyWorkout workout, int position);
        void onWorkoutDeleted(WeeklyWorkout workout, int position);
        void onWorkoutClicked(WeeklyWorkout workout);
    }
    
    private Context context;
    private List<WeeklyWorkout> weeklyWorkouts;
    private OnWorkoutActionListener listener;
    
    public WeeklyWorkoutAdapter(Context context, List<WeeklyWorkout> weeklyWorkouts, OnWorkoutActionListener listener) {
        this.context = context;
        this.weeklyWorkouts = weeklyWorkouts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public WeeklyWorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weekly_workout, parent, false);
        return new WeeklyWorkoutViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WeeklyWorkoutViewHolder holder, int position) {
        WeeklyWorkout workout = weeklyWorkouts.get(position);
        holder.bind(workout, position);
    }
    
    @Override
    public int getItemCount() {
        return weeklyWorkouts.size();
    }
    
    public void updateWorkouts(List<WeeklyWorkout> newWorkouts) {
        this.weeklyWorkouts = newWorkouts;
        notifyDataSetChanged();
    }
    
    public class WeeklyWorkoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private TextView routineNameText;
        private TextView routineDescriptionText;
        private TextView scheduledDateText;
        private TextView completionStatusText;
        private ImageView statusIcon;
        private ImageView swipeHintIcon;
        
        public WeeklyWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.workoutCardView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            routineDescriptionText = itemView.findViewById(R.id.routineDescriptionText);
            scheduledDateText = itemView.findViewById(R.id.scheduledDateText);
            completionStatusText = itemView.findViewById(R.id.completionStatusText);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            swipeHintIcon = itemView.findViewById(R.id.swipeHintIcon);
            
            itemView.setOnClickListener(this);
        }
        
        public void bind(WeeklyWorkout workout, int position) {
            // Set routine information
            if (workout.getRoutine() != null) {
                routineNameText.setText(workout.getRoutine().getRoutineName());
                
                String description = workout.getRoutine().getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    routineDescriptionText.setText(description);
                    routineDescriptionText.setVisibility(View.VISIBLE);
                } else {
                    routineDescriptionText.setVisibility(View.GONE);
                }
            } else {
                routineNameText.setText("Unknown Routine");
                routineDescriptionText.setVisibility(View.GONE);
            }
            
            // Set scheduled date
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
            scheduledDateText.setText(dateFormat.format(workout.getScheduledDate()));
            
            // Set completion status
            if (workout.isCompleted()) {
                completionStatusText.setText("✅ Completed");
                completionStatusText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark, null));
                statusIcon.setImageResource(R.drawable.ic_check_circle);
                statusIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_green_dark, null));
                
                // Strike through text for completed workouts
                routineNameText.setPaintFlags(routineNameText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                
                // Show completion time if available
                if (workout.getCompletionTime() != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    completionStatusText.setText("✅ Completed at " + timeFormat.format(workout.getCompletionTime()));
                }
                
                swipeHintIcon.setVisibility(View.GONE);
            } else {
                completionStatusText.setText("⏳ Pending");
                completionStatusText.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark, null));
                statusIcon.setImageResource(R.drawable.ic_pending);
                statusIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_dark, null));
                
                // Remove strike through
                routineNameText.setPaintFlags(routineNameText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                
                swipeHintIcon.setVisibility(View.VISIBLE);
            }
            
            // Update card appearance
            if (workout.isCompleted()) {
                cardView.setAlpha(0.7f);
                cardView.setCardElevation(dp(1));
            } else {
                cardView.setAlpha(1.0f);
                cardView.setCardElevation(dp(4));
            }
        }
        
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                WeeklyWorkout workout = weeklyWorkouts.get(position);
                listener.onWorkoutClicked(workout);
            }
        }
        
        // Helper method to convert dp to pixels
        private int dp(int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}