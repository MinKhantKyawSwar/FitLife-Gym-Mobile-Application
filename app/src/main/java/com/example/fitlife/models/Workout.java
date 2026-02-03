package com.example.fitlife.models;

public class Workout {
    private long workoutId;
    private String name;
    private int userId;
    private String createdDate;
    private int exerciseCount;

    public Workout() {
    }

    public Workout(long workoutId, String name, int userId, String createdDate) {
        this.workoutId = workoutId;
        this.name = name;
        this.userId = userId;
        this.createdDate = createdDate;
    }

    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }
}
