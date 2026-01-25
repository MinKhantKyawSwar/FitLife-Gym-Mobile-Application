package com.example.fitlife.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.fragments.ExerciseDetailBottomSheet;
import com.example.fitlife.models.WeeklyWorkout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    
    public interface OnWorkoutActionListener {
        void onWorkoutCompleted(WeeklyWorkout workout);
        void onWorkoutDeleted(WeeklyWorkout workout);
        void onWorkoutClicked(WeeklyWorkout workout);
    }
    
    private Context context;
    private List<WeeklyWorkout> workouts;
    private OnWorkoutActionListener listener;
    
    public WorkoutAdapter(Context context, List<WeeklyWorkout> workouts, OnWorkoutActionListener listener) {
        this.context = context;
        this.workouts = workouts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weekly_workout, parent, false);
        return new WorkoutViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WeeklyWorkout workout = workouts.get(position);
        holder.bind(workout);
    }
    
    @Override
    public int getItemCount() {
        return workouts.size();
    }
    
    public void updateWorkouts(List<WeeklyWorkout> newWorkouts) {
        this.workouts = newWorkouts;
        notifyDataSetChanged();
    }
    
    public class WorkoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private TextView routineNameText;
        private TextView scheduledDateText;
        private TextView statusText;
        private TextView exerciseCountText;
        private View completedIndicator;
        
        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.workoutCardView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            scheduledDateText = itemView.findViewById(R.id.scheduledDateText);
            statusText = itemView.findViewById(R.id.completionStatusText);
            exerciseCountText = itemView.findViewById(R.id.routineDescriptionText);
            completedIndicator = itemView.findViewById(R.id.statusIcon);
            
            itemView.setOnClickListener(this);
        }
        
        public void bind(WeeklyWorkout workout) {
            if (workout.getRoutine() != null) {
                routineNameText.setText(workout.getRoutine().getRoutineName());
                
                int exerciseCount = workout.getRoutine().getExercises().size();
                exerciseCountText.setText(exerciseCount + " exercises");
            } else {
                routineNameText.setText("Unknown Routine");
                exerciseCountText.setText("0 exercises");
            }
            
            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
            scheduledDateText.setText(sdf.format(workout.getScheduledDate()));
            
            // Set completion status
            if (workout.isCompleted()) {
                statusText.setText("Completed");
                statusText.setTextColor(Color.GREEN);
                completedIndicator.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.7f);
            } else {
                statusText.setText("Scheduled");
                statusText.setTextColor(context.getColor(R.color.primary_orange));
                completedIndicator.setVisibility(View.GONE);
                cardView.setAlpha(1.0f);
            }
        }
        
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onWorkoutClicked(workouts.get(position));
            }
        }
    }
    
    // ItemTouchHelper for swipe gestures
    public static class SwipeToActionCallback extends ItemTouchHelper.SimpleCallback {
        private WorkoutAdapter adapter;
        private Paint paint;
        private ColorDrawable background;
        
        public SwipeToActionCallback(WorkoutAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
            this.paint = new Paint();
            this.background = new ColorDrawable();
        }
        
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false; // We don't support move
        }
        
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            WeeklyWorkout workout = adapter.workouts.get(position);
            
            if (direction == ItemTouchHelper.RIGHT) {
                // Swipe right - mark as completed
                if (adapter.listener != null) {
                    adapter.listener.onWorkoutCompleted(workout);
                }
            } else if (direction == ItemTouchHelper.LEFT) {
                // Swipe left - delete
                if (adapter.listener != null) {
                    adapter.listener.onWorkoutDeleted(workout);
                }
            }
        }
        
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, 
                               @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, 
                               int actionState, boolean isCurrentlyActive) {
            
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;
            
            if (dX > 0) { // Swiping to the right (complete)
                background.setColor(Color.GREEN);
                background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());
                background.draw(c);
                
                // Draw check mark icon
                paint.setColor(Color.WHITE);
                paint.setTextSize(48);
                paint.setTextAlign(Paint.Align.CENTER);
                c.drawText("✓", itemView.getLeft() + 60, 
                    itemView.getTop() + (itemView.getHeight() / 2) + 15, paint);
                    
            } else if (dX < 0) { // Swiping to the left (delete)
                background.setColor(Color.RED);
                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
                
                // Draw delete icon
                paint.setColor(Color.WHITE);
                paint.setTextSize(48);
                paint.setTextAlign(Paint.Align.CENTER);
                c.drawText("🗑", itemView.getRight() - 60, 
                    itemView.getTop() + (itemView.getHeight() / 2) + 15, paint);
            }
        }
    }
}