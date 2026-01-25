package com.example.fitlife.models;

import java.io.Serializable;
import java.util.Date;

public class WeeklyWorkout implements Serializable {
    private int weeklyWorkoutId;
    private int userId;
    private int routineId;
    private Date scheduledDate;
    private boolean isCompleted;
    private Date completionTime;
    private Date weekStartDate;
    
    // For joined data
    private Routine routine;
    
    public WeeklyWorkout() {
        this.isCompleted = false;
    }
    
    public WeeklyWorkout(int userId, int routineId, Date scheduledDate, Date weekStartDate) {
        this.userId = userId;
        this.routineId = routineId;
        this.scheduledDate = scheduledDate;
        this.weekStartDate = weekStartDate;
        this.isCompleted = false;
    }
    
    // Getters and Setters
    public int getWeeklyWorkoutId() { return weeklyWorkoutId; }
    public void setWeeklyWorkoutId(int weeklyWorkoutId) { this.weeklyWorkoutId = weeklyWorkoutId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getRoutineId() { return routineId; }
    public void setRoutineId(int routineId) { this.routineId = routineId; }
    
    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public Date getCompletionTime() { return completionTime; }
    public void setCompletionTime(Date completionTime) { this.completionTime = completionTime; }
    
    public Date getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(Date weekStartDate) { this.weekStartDate = weekStartDate; }
    
    public Routine getRoutine() { return routine; }
    public void setRoutine(Routine routine) { this.routine = routine; }
}