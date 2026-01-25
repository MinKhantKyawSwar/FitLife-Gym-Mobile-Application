package com.example.fitlife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.Routine;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {
    
    public interface OnRoutineActionListener {
        void onRoutineEdit(Routine routine);
        void onRoutineDelete(Routine routine);
        void onRoutineShare(Routine routine);
        void onRoutineClick(Routine routine);
        void onAddToWeekly(Routine routine);
    }
    
    private Context context;
    private List<Routine> routines;
    private OnRoutineActionListener listener;
    
    public RoutineAdapter(Context context, List<Routine> routines, OnRoutineActionListener listener) {
        this.context = context;
        this.routines = routines;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_routine, parent, false);
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
        this.routines = newRoutines;
        notifyDataSetChanged();
    }
    
    public void addRoutine(Routine routine) {
        routines.add(0, routine);
        notifyItemInserted(0);
    }
    
    public void removeRoutine(Routine routine) {
        int position = routines.indexOf(routine);
        if (position >= 0) {
            routines.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateRoutine(Routine updatedRoutine) {
        for (int i = 0; i < routines.size(); i++) {
            if (routines.get(i).getRoutineId() == updatedRoutine.getRoutineId()) {
                routines.set(i, updatedRoutine);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public class RoutineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private TextView routineNameText;
        private TextView descriptionText;
        private TextView exerciseCountText;
        private TextView createdDateText;
        private Button menuButton;
        private Button addToWeeklyButton;
        
        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.routineCardView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            exerciseCountText = itemView.findViewById(R.id.exerciseCountText);
            createdDateText = itemView.findViewById(R.id.createdDateText);
            menuButton = itemView.findViewById(R.id.menuButton);
            addToWeeklyButton = itemView.findViewById(R.id.addToWeeklyButton);
            
            itemView.setOnClickListener(this);
            menuButton.setOnClickListener(v -> showPopupMenu(v));
            addToWeeklyButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAddToWeekly(routines.get(position));
                }
            });
        }
        
        public void bind(Routine routine) {
            routineNameText.setText(routine.getRoutineName());
            
            if (routine.getDescription() != null && !routine.getDescription().trim().isEmpty()) {
                descriptionText.setText(routine.getDescription());
                descriptionText.setVisibility(View.VISIBLE);
            } else {
                descriptionText.setVisibility(View.GONE);
            }
            
            int exerciseCount = routine.getExercises() != null ? routine.getExercises().size() : 0;
            exerciseCountText.setText(exerciseCount + " exercises");
            
            // Format created date
            if (routine.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                createdDateText.setText("Created: " + sdf.format(routine.getCreatedAt()));
                createdDateText.setVisibility(View.VISIBLE);
            } else {
                createdDateText.setVisibility(View.GONE);
            }
        }
        
        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.routine_context_menu, popupMenu.getMenu());
            
            popupMenu.setOnMenuItemClickListener(item -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION || listener == null) {
                    return false;
                }
                
                Routine routine = routines.get(position);
                int itemId = item.getItemId();
                
                if (itemId == R.id.menu_edit) {
                    listener.onRoutineEdit(routine);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    listener.onRoutineDelete(routine);
                    return true;
                } else if (itemId == R.id.menu_share) {
                    listener.onRoutineShare(routine);
                    return true;
                }
                
                return false;
            });
            
            popupMenu.show();
        }
        
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onRoutineClick(routines.get(position));
            }
        }
    }
}