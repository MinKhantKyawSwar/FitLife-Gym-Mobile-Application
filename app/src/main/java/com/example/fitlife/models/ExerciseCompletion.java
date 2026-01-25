package com.example.fitlife.models;

import java.io.Serializable;
import java.util.Date;

public class ExerciseCompletion implements Serializable {
    private int completionId;
    private int historyId;
    private int exerciseId;
    private int setsCompleted;
    private int repsCompleted;
    private float weightUsed;
    private Date completionTime;
    
    // For joined data
    private Exercise exercise;
    
    public ExerciseCompletion() {}
    
    public ExerciseCompletion(int historyId, int exerciseId, int setsCompleted, int repsCompleted) {
        this.historyId = historyId;
        this.exerciseId = exerciseId;
        this.setsCompleted = setsCompleted;
        this.repsCompleted = repsCompleted;
    }
    
    // Getters and Setters
    public int getCompletionId() { return completionId; }
    public void setCompletionId(int completionId) { this.completionId = completionId; }
    
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    
    public int getSetsCompleted() { return setsCompleted; }
    public void setSetsCompleted(int setsCompleted) { this.setsCompleted = setsCompleted; }
    
    public int getRepsCompleted() { return repsCompleted; }
    public void setRepsCompleted(int repsCompleted) { this.repsCompleted = repsCompleted; }
    
    public float getWeightUsed() { return weightUsed; }
    public void setWeightUsed(float weightUsed) { this.weightUsed = weightUsed; }
    
    public Date getCompletionTime() { return completionTime; }
    public void setCompletionTime(Date completionTime) { this.completionTime = completionTime; }
    
    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }
}