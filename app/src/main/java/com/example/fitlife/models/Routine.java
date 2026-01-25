package com.example.fitlife.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Routine implements Serializable {
    private int routineId;
    private int userId;
    private String routineName;
    private String description;
    private Date createdAt;
    private List<RoutineExercise> exercises;
    
    public Routine() {
        this.exercises = new ArrayList<>();
    }
    
    public Routine(String routineName, String description) {
        this.routineName = routineName;
        this.description = description;
        this.exercises = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getRoutineId() { return routineId; }
    public void setRoutineId(int routineId) { this.routineId = routineId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getRoutineName() { return routineName; }
    public void setRoutineName(String routineName) { this.routineName = routineName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public List<RoutineExercise> getExercises() { return exercises; }
    public void setExercises(List<RoutineExercise> exercises) { this.exercises = exercises; }
    
    // Convenience methods
    public String getName() { return routineName; }
    public void setName(String name) { this.routineName = name; }
    
    public int getId() { return routineId; }
    public void setId(int id) { this.routineId = id; }
    
    public void addExercise(RoutineExercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }
}