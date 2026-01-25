package com.example.fitlife.models;

import java.io.Serializable;
import java.util.Date;

public class WeeklyEquipmentChecklist implements Serializable {
    private int checklistId;
    private int userId;
    private int equipmentId;
    private Date weekStartDate;
    private boolean isObtained;
    private String notes;
    
    // For joined data
    private Equipment equipment;
    
    public WeeklyEquipmentChecklist() {
        this.isObtained = false;
    }
    
    public WeeklyEquipmentChecklist(int userId, int equipmentId, Date weekStartDate) {
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.weekStartDate = weekStartDate;
        this.isObtained = false;
    }
    
    // Getters and Setters
    public int getChecklistId() { return checklistId; }
    public void setChecklistId(int checklistId) { this.checklistId = checklistId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }
    
    public Date getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(Date weekStartDate) { this.weekStartDate = weekStartDate; }
    
    public boolean isObtained() { return isObtained; }
    public void setObtained(boolean obtained) { isObtained = obtained; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
}