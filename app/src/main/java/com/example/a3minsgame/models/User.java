package com.example.a3minsgame.models;

public class User {
    private String email;
    private String username;
    private int totalPoints;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String email, String username, int totalPoints) {
        this.email = email;
        this.username = username;
        this.totalPoints = totalPoints;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
