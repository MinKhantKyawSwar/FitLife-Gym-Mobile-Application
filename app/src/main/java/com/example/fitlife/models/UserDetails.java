package com.example.fitlife.models;

public class UserDetails {
    private int userId;
    private int age;
    private String gender;
    private double height;
    private double weight;

    public UserDetails() {
    }

    public UserDetails(int userId, int age, String gender, double height, double weight) {
        this.userId = userId;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
