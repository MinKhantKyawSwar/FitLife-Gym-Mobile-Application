package com.example.fitlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.adapters.EquipmentChecklistAdapter;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.EquipmentChecklistController;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WeeklyEquipmentChecklist;
import com.example.fitlife.utils.SMSHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipmentChecklistFragment extends Fragment implements EquipmentChecklistAdapter.OnEquipmentActionListener {
    
    private TextView headerText;
    private TextView statsText;
    private RecyclerView checklistRecyclerView;
    private Button shareChecklistBtn;
    private View emptyStateLayout;
    
    private EquipmentChecklistAdapter adapter;
    private EquipmentChecklistController equipmentController;
    private SMSHelper smsHelper;
    
    private Map<String, List<WeeklyEquipmentChecklist>> categorizedEquipment;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment_checklist, container, false);
        
        initializeControllers();
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadEquipmentChecklist();
        
        return view;
    }
    
    private void initializeControllers() {
        equipmentController = new EquipmentChecklistController(requireContext());
        smsHelper = new SMSHelper(requireContext());
    }
    
    private void initializeViews(View view) {
        headerText = view.findViewById(R.id.headerText);
        statsText = view.findViewById(R.id.statsText);
        checklistRecyclerView = view.findViewById(R.id.checklistRecyclerView);
        shareChecklistBtn = view.findViewById(R.id.shareChecklistBtn);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }
    
    private void setupRecyclerView() {
        adapter = new EquipmentChecklistAdapter(getContext(), this);
        checklistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        checklistRecyclerView.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        shareChecklistBtn.setOnClickListener(v -> shareEquipmentChecklist());
    }
    
    private void loadEquipmentChecklist() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            categorizedEquipment = equipmentController.getCurrentWeekChecklist(currentUser.getUserId());
            adapter.updateEquipment(categorizedEquipment);
            
            updateStats(currentUser.getUserId());
            updateEmptyState();
        }
    }
    
    private void updateStats(int userId) {
        int[] stats = equipmentController.getChecklistStats(userId);
        int obtained = stats[0];
        int total = stats[1];
        
        if (total > 0) {
            statsText.setText(String.format("Progress: %d/%d items obtained (%d%%)", 
                obtained, total, (obtained * 100) / total));
            statsText.setVisibility(View.VISIBLE);
        } else {
            statsText.setVisibility(View.GONE);
        }
    }
    
    private void updateEmptyState() {
        if (categorizedEquipment.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            checklistRecyclerView.setVisibility(View.GONE);
            shareChecklistBtn.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            checklistRecyclerView.setVisibility(View.VISIBLE);
            shareChecklistBtn.setVisibility(View.VISIBLE);
        }
    }
    
    private void shareEquipmentChecklist() {
        if (categorizedEquipment == null || categorizedEquipment.isEmpty()) {
            Toast.makeText(getContext(), "No equipment checklist to share", Toast.LENGTH_SHORT).show();
            return;
        }
        
        smsHelper.shareEquipmentList(categorizedEquipment, new SMSHelper.OnSMSResultListener() {
            @Override
            public void onSMSSuccess() {
                Toast.makeText(getContext(), "Equipment checklist shared successfully!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onSMSFailure(String error) {
                Toast.makeText(getContext(), "Failed to share checklist: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onEquipmentStatusChanged(WeeklyEquipmentChecklist item, boolean isObtained) {
        boolean success = equipmentController.updateEquipmentStatus(item.getChecklistId(), isObtained);
        if (success) {
            item.setObtained(isObtained);
            User currentUser = AuthController.getCurrentUser();
            if (currentUser != null) {
                updateStats(currentUser.getUserId());
            }
            String message = isObtained ? "marked as obtained" : "marked as not obtained";
            Toast.makeText(getContext(), item.getEquipment().getEquipmentName() + " " + message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to update equipment status", Toast.LENGTH_SHORT).show();
            // Revert the change in UI
            loadEquipmentChecklist();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadEquipmentChecklist(); // Refresh data when fragment becomes visible
    }
}