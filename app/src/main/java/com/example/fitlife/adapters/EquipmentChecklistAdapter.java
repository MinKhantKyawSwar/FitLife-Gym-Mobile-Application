package com.example.fitlife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.models.WeeklyEquipmentChecklist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipmentChecklistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_EQUIPMENT = 1;
    
    public interface OnEquipmentActionListener {
        void onEquipmentStatusChanged(WeeklyEquipmentChecklist item, boolean isObtained);
    }
    
    private Context context;
    private OnEquipmentActionListener listener;
    private List<Object> items; // Mixed list of categories (String) and equipment items
    
    public EquipmentChecklistAdapter(Context context, OnEquipmentActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.items = new ArrayList<>();
    }
    
    public void updateEquipment(Map<String, List<WeeklyEquipmentChecklist>> categorizedEquipment) {
        items.clear();
        
        for (Map.Entry<String, List<WeeklyEquipmentChecklist>> entry : categorizedEquipment.entrySet()) {
            // Add category header
            items.add(entry.getKey());
            
            // Add equipment items in this category
            items.addAll(entry.getValue());
        }
        
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_CATEGORY : TYPE_EQUIPMENT;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_equipment_category, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_equipment_checklist, parent, false);
            return new EquipmentViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategoryViewHolder) {
            String category = (String) items.get(position);
            ((CategoryViewHolder) holder).bind(category);
        } else if (holder instanceof EquipmentViewHolder) {
            WeeklyEquipmentChecklist equipment = (WeeklyEquipmentChecklist) items.get(position);
            ((EquipmentViewHolder) holder).bind(equipment);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryText;
        private TextView categoryIcon;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
        }
        
        public void bind(String category) {
            categoryText.setText(category.toUpperCase());
            
            // Set category icon
            switch (category.toLowerCase()) {
                case "strength":
                    categoryIcon.setText("🏋️");
                    break;
                case "cardio":
                    categoryIcon.setText("🏃");
                    break;
                case "flexibility":
                    categoryIcon.setText("🧘");
                    break;
                case "accessories":
                default:
                    categoryIcon.setText("🎯");
                    break;
            }
        }
    }
    
    public class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private CheckBox equipmentCheckbox;
        private TextView equipmentNameText;
        private TextView equipmentDescriptionText;
        
        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            equipmentCheckbox = itemView.findViewById(R.id.equipmentCheckbox);
            equipmentNameText = itemView.findViewById(R.id.equipmentNameText);
            equipmentDescriptionText = itemView.findViewById(R.id.equipmentDescriptionText);
        }
        
        public void bind(WeeklyEquipmentChecklist item) {
            equipmentNameText.setText(item.getEquipment().getEquipmentName());
            
            if (item.getEquipment().getDescription() != null && !item.getEquipment().getDescription().trim().isEmpty()) {
                equipmentDescriptionText.setText(item.getEquipment().getDescription());
                equipmentDescriptionText.setVisibility(View.VISIBLE);
            } else {
                equipmentDescriptionText.setVisibility(View.GONE);
            }
            
            // Set checkbox state without triggering listener
            equipmentCheckbox.setOnCheckedChangeListener(null);
            equipmentCheckbox.setChecked(item.isObtained());
            
            // Set listener after setting state
            equipmentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onEquipmentStatusChanged(item, isChecked);
                }
            });
        }
    }
}