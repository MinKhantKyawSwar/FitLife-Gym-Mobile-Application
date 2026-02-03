package com.example.fitlife.models;

public class UserStats {
    private int userId;
    private int totalSessions;
    private int totalRoutines;
    private int totalExercises;
    private int activeDays;

    public UserStats() {
    }

    public UserStats(int userId, int totalSessions, int totalRoutines, int totalExercises, int activeDays) {
        this.userId = userId;
        this.totalSessions = totalSessions;
        this.totalRoutines = totalRoutines;
        this.totalExercises = totalExercises;
        this.activeDays = activeDays;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public int getTotalRoutines() {
        return totalRoutines;
    }

    public void setTotalRoutines(int totalRoutines) {
        this.totalRoutines = totalRoutines;
    }

    public int getTotalExercises() {
        return totalExercises;
    }

    public void setTotalExercises(int totalExercises) {
        this.totalExercises = totalExercises;
    }

    public int getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }
}
