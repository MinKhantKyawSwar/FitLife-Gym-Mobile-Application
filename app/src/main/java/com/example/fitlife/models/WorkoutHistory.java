package com.example.fitlife.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class WorkoutHistory implements Serializable {
    private int historyId;
    private int userId;
    private int routineId;
    private Date workoutDate;
    private Date startTime;
    private Date completionTime;
    private int totalDurationMinutes;
    private int exercisesCompleted;
    private String notes;
    
    // For joined data
    private Routine routine;
    private List<ExerciseCompletion> exerciseCompletions;
    
    public WorkoutHistory() {}
    
    public WorkoutHistory(int userId, int routineId, Date workoutDate) {
        this.userId = userId;
        this.routineId = routineId;
        this.workoutDate = workoutDate;
    }
    
    // Getters and Setters
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getRoutineId() { return routineId; }
    public void setRoutineId(int routineId) { this.routineId = routineId; }
    
    public Date getWorkoutDate() { return workoutDate; }
    public void setWorkoutDate(Date workoutDate) { this.workoutDate = workoutDate; }
    
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    
    public Date getCompletionTime() { return completionTime; }
    public void setCompletionTime(Date completionTime) { this.completionTime = completionTime; }
    
    public int getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(int totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
    
    public int getExercisesCompleted() { return exercisesCompleted; }
    public void setExercisesCompleted(int exercisesCompleted) { this.exercisesCompleted = exercisesCompleted; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Routine getRoutine() { return routine; }
    public void setRoutine(Routine routine) { this.routine = routine; }
    
    public List<ExerciseCompletion> getExerciseCompletions() { return exerciseCompletions; }
    public void setExerciseCompletions(List<ExerciseCompletion> exerciseCompletions) { this.exerciseCompletions = exerciseCompletions; }
}