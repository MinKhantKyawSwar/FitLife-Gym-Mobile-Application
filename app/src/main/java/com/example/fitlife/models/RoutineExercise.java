package com.example.fitlife.models;

import java.io.Serializable;

public class RoutineExercise implements Serializable {
    private int routineExerciseId;
    private int routineId;
    private int exerciseId;
    private int sets;
    private int reps;
    private int restSeconds;
    
    // For joined data
    private Exercise exercise;
    
    public RoutineExercise() {
        this.sets = 1;
        this.reps = 1;
        this.restSeconds = 60;
    }
    
    public RoutineExercise(int exerciseId, int sets, int reps, int restSeconds) {
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
    }
    
    // Getters and Setters
    public int getRoutineExerciseId() { return routineExerciseId; }
    public void setRoutineExerciseId(int routineExerciseId) { this.routineExerciseId = routineExerciseId; }
    
    public int getRoutineId() { return routineId; }
    public void setRoutineId(int routineId) { this.routineId = routineId; }
    
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    
    public int getRestSeconds() { return restSeconds; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }
    
    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }
}