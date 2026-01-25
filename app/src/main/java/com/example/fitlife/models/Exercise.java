package com.example.fitlife.models;

import java.io.Serializable;
import java.util.Date;

public class Exercise implements Serializable {
    private int exerciseId;
    private int userId;
    private String exerciseName;
    private String equipmentNeeded;
    private String instructions;
    private String imagePath;
    private Date createdAt;
    
    public Exercise() {}
    
    public Exercise(String exerciseName, String equipmentNeeded, String instructions) {
        this.exerciseName = exerciseName;
        this.equipmentNeeded = equipmentNeeded;
        this.instructions = instructions;
    }
    
    // Getters and Setters
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
    
    public String getEquipmentNeeded() { return equipmentNeeded; }
    public void setEquipmentNeeded(String equipmentNeeded) { this.equipmentNeeded = equipmentNeeded; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // Convenience methods
    public String getName() { return exerciseName; }
    public void setName(String name) { this.exerciseName = name; }
}