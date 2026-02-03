package com.example.fitlife.models;

public class User {
    private int userId;
    private String usernameEmail;
    private String password;

    public User() {
    }

    public User(int userId, String usernameEmail, String password) {
        this.userId = userId;
        this.usernameEmail = usernameEmail;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsernameEmail() {
        return usernameEmail;
    }

    public void setUsernameEmail(String usernameEmail) {
        this.usernameEmail = usernameEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
