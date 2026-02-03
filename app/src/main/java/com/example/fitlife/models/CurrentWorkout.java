package com.example.fitlife.models;

public class CurrentWorkout {
    private long id;
    private long workoutId;
    private String workoutName;
    private String status;
    private String startedDate;
    private int exerciseCount;

    public CurrentWorkout() {
    }

    public CurrentWorkout(long id, long workoutId, String workoutName, String status, String startedDate) {
        this.id = id;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.status = status;
        this.startedDate = startedDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(String startedDate) {
        this.startedDate = startedDate;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }
}
