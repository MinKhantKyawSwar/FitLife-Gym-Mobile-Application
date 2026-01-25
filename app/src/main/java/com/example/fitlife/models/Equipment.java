package com.example.fitlife.models;

import java.io.Serializable;
import java.util.Date;

public class Equipment implements Serializable {
    private int equipmentId;
    private String equipmentName;
    private String category; // strength, cardio, flexibility, accessories
    private String description;
    private Date createdAt;
    
    public Equipment() {}
    
    public Equipment(String equipmentName, String category, String description) {
        this.equipmentName = equipmentName;
        this.category = category;
        this.description = description;
    }
    
    // Getters and Setters
    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }
    
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // Convenience methods
    public String getName() { return equipmentName; }
    public void setName(String name) { this.equipmentName = name; }
}