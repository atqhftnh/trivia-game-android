package com.example.a3minsgame.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;
import com.example.a3minsgame.models.Question;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView tvTimer, tvQuestion, tvScore;
    private EditText etAnswer;
    private Button btnSubmit;
    private TextView option1, option2, option3, option4;
    private DatabaseReference databaseReference;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int questionsAttempted = 0;
    private String difficulty;
    private CountDownTimer timer;
    private String currentUsername;

    // 🔥 Sound Effects
    private MediaPlayer correctSound, wrongSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        currentUsername = getIntent().getStringExtra("username");

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        difficulty = getIntent().getStringExtra("difficulty");
        Log.d("GameActivity", "Difficulty: " + difficulty);

        if (difficulty == null || !difficulty.matches("beginner|intermediate|advanced")) {
            Log.e("GameActivity", "Invalid difficulty level");
            Toast.makeText(this, "Invalid difficulty level", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Log.d("GameActivity", "Firebase initialized");

        tvTimer = findViewById(R.id.tv_timer);
        tvQuestion = findViewById(R.id.tv_question);
        tvScore = findViewById(R.id.tv_score);
        etAnswer = findViewById(R.id.et_answer);
        btnSubmit = findViewById(R.id.btn_submit);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound);

        loadQuestions();
        startTimer();
    }

    private void loadQuestions() {
        DatabaseReference questionsRef = databaseReference.child("questions").child(difficulty);
        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList = new ArrayList<>();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null) {
                        questionList.add(question);
                    }
                }

                if (questionList.isEmpty()) {
                    Log.e("GameActivity", "No questions found for difficulty: " + difficulty);
                    Toast.makeText(GameActivity.this, "No questions available!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Collections.shuffle(questionList); // 🔥 Shuffle questions
                    showNextQuestion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameActivity", "Error loading questions: " + error.getMessage());
                Toast.makeText(GameActivity.this, "Failed to load questions.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startTimer() {
        timer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time Left: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                Log.d("GameActivity", "Timer finished, ending game.");
                endGame();
            }
        }.start();
    }

    private void showNextQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            Log.d("GameActivity", "No more questions, ending game...");
            endGame();
            return;
        }

        Question currentQuestion = questionList.get(currentQuestionIndex);
        if (currentQuestion == null || currentQuestion.getCorrectAnswer() == null) {
            currentQuestionIndex++;
            showNextQuestion();
            return;
        }

        tvQuestion.setText(currentQuestion.getQuestion());

        etAnswer.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        option1.setVisibility(View.GONE);
        option2.setVisibility(View.GONE);
        option3.setVisibility(View.GONE);
        option4.setVisibility(View.GONE);

        resetOptionBackgrounds(); // Reset background colors

        if (currentQuestion.isTextInputRequired()) {
            etAnswer.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            etAnswer.setText("");

            btnSubmit.setOnClickListener(v -> {
                String userAnswer = etAnswer.getText().toString().trim();
                if (userAnswer.isEmpty()) {
                    etAnswer.setError("Please enter an answer");
                    return;
                }
                checkAnswer(userAnswer, currentQuestion.getCorrectAnswer(), null);
            });
        } else {
            List<String> options = new ArrayList<>(currentQuestion.getOptions());
            Collections.shuffle(options);

            option1.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option3.setVisibility(View.VISIBLE);
            option4.setVisibility(View.VISIBLE);

            option1.setText(options.get(0));
            option2.setText(options.get(1));
            option3.setText(options.get(2));
            option4.setText(options.get(3));

            enableOptionClicks();
        }
    }

    private void enableOptionClicks() {
        option1.setOnClickListener(v -> checkAnswer(option1.getText().toString(), questionList.get(currentQuestionIndex).getCorrectAnswer(), option1));
        option2.setOnClickListener(v -> checkAnswer(option2.getText().toString(), questionList.get(currentQuestionIndex).getCorrectAnswer(), option2));
        option3.setOnClickListener(v -> checkAnswer(option3.getText().toString(), questionList.get(currentQuestionIndex).getCorrectAnswer(), option3));
        option4.setOnClickListener(v -> checkAnswer(option4.getText().toString(), questionList.get(currentQuestionIndex).getCorrectAnswer(), option4));
    }

    private void checkAnswer(String selectedAnswer, String correctAnswer, TextView selectedOption) {
        boolean isCorrect = selectedAnswer.equalsIgnoreCase(correctAnswer);

        if (selectedOption != null) {
            selectedOption.setBackgroundResource(isCorrect ? R.drawable.option_correct : R.drawable.option_wrong);
        }

        if (isCorrect) {
            correctSound.start();
            score += getPointsGained();
            correctAnswers++;
        } else {
            wrongSound.start();
            score += getPointsLost();
        }

        tvScore.setText("Score: " + score);

        new Handler().postDelayed(() -> {
            resetOptionBackgrounds();
            currentQuestionIndex++;
            questionsAttempted++;
            showNextQuestion();
        }, 800);
    }

    private int getPointsGained() {
        switch (difficulty.toLowerCase()) {
            case "beginner": return 1;
            case "intermediate": return 1;
            case "advanced": return 2;
            default: return 0;
        }
    }

    private int getPointsLost() {
        switch (difficulty.toLowerCase()) {
            case "beginner": return -1;
            case "intermediate": return -2;
            case "advanced": return -3;
            default: return 0;
        }
    }

    private void resetOptionBackgrounds() {
        option1.setBackgroundResource(R.drawable.rounded_option);
        option2.setBackgroundResource(R.drawable.rounded_option);
        option3.setBackgroundResource(R.drawable.rounded_option);
        option4.setBackgroundResource(R.drawable.rounded_option);
    }

    private void endGame() {
        if (timer != null) {
            timer.cancel();
        }

        tvTimer.setText("⏳ Time Up!");

        Intent intent = new Intent(GameActivity.this, SinglePlayerResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("questionsAttempted", questionsAttempted);
        intent.putExtra("username", currentUsername);
        startActivity(intent);
        finish();
    }
}
