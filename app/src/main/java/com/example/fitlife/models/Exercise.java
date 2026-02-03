package com.example.fitlife.models;

import java.util.List;

public class Exercise {
    private long exerciseId;
    private String name;
    private int sets;
    private String reps;
    private String restTime;
    private String imagePath;
    private List<String> equipment;
    private List<String> instructions;

    public Exercise() {
    }

    public Exercise(long exerciseId, String name, int sets, String reps, String restTime, String imagePath) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.imagePath = imagePath;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public String getRestTime() {
        return restTime;
    }

    public void setRestTime(String restTime) {
        this.restTime = restTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }
}
