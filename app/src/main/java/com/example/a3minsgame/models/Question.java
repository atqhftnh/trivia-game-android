package com.example.a3minsgame.models;

import java.util.List;

public class Question {
    private String question;
    private String correctAnswer;
    private List<String> options;
    private String difficulty;
    private boolean textInputRequired;

    // Default constructor required for Firebase
    public Question() {
    }

    public Question(String question, String correctAnswer, List<String> options, String difficulty, boolean textInputRequired) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.difficulty = difficulty;
        this.textInputRequired = textInputRequired;
    }

    // Getters and setters
    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isTextInputRequired() {
        return textInputRequired;
    }
}
