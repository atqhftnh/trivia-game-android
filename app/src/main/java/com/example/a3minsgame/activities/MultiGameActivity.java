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
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiGameActivity extends AppCompatActivity {

    private TextView tvTimer, tvQuestion, tvOption1, tvOption2, tvOption3, tvOption4, tvScore;
    private EditText etAnswer;
    private Button btnSubmit;
    private DatabaseReference databaseReference;
    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int totalQuestions = 0;
    private String roomCode;
    private CountDownTimer gameTimer;
    private String currentUsername;

    // 🔥 Sound Effects
    private MediaPlayer correctSound, wrongSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_game);

        // Initialize views
        tvTimer = findViewById(R.id.tv_timer);
        tvQuestion = findViewById(R.id.tv_question);
        tvOption1 = findViewById(R.id.option1);
        tvOption2 = findViewById(R.id.option2);
        tvOption3 = findViewById(R.id.option3);
        tvOption4 = findViewById(R.id.option4);
        tvScore = findViewById(R.id.tv_score);
        etAnswer = findViewById(R.id.et_answer);
        btnSubmit = findViewById(R.id.btn_submit);

        // 🔥 Load Sound Effects
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound);

        currentUsername = getIntent().getStringExtra("username");
        roomCode = getIntent().getStringExtra("roomCode");

        if (roomCode == null || currentUsername == null) {
            Log.e("MultiGameActivity", "Room code or username is missing.");
            Toast.makeText(this, "Error: Missing game details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        setupOptionListeners();
        startGameCountdown();
    }

    private void startGameCountdown() {
        databaseReference.child("rooms").child(roomCode).child("gameState")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String gameState = snapshot.getValue(String.class);
                        if ("ended".equals(gameState)) {
                            finish();
                            return;
                        }

                        new CountDownTimer(5000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                tvTimer.setText("Your game is about to start in " + millisUntilFinished / 1000 + "...");
                            }

                            @Override
                            public void onFinish() {
                                tvTimer.setText("Game Started!");
                                loadQuestions();
                                startGameTimer();
                            }
                        }.start();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MultiGameActivity", "Failed to check game state: " + error.getMessage());
                    }
                });
    }

    private void startGameTimer() {
        gameTimer = new CountDownTimer(180000, 1000) { // 3-minute timer
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time left: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void loadQuestions() {
        DatabaseReference questionsRef = databaseReference.child("questions");
        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();

                for (DataSnapshot difficultySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot questionSnapshot : difficultySnapshot.getChildren()) {
                        Question question = questionSnapshot.getValue(Question.class);
                        if (question != null) {
                            questionList.add(question);
                        }
                    }
                }

                if (questionList.isEmpty()) {
                    Toast.makeText(MultiGameActivity.this, "No valid questions available!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    totalQuestions = questionList.size();
                    Collections.shuffle(questionList);
                    showNextQuestion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MultiGameActivity", "Error loading questions: " + error.getMessage());
                Toast.makeText(MultiGameActivity.this, "Failed to load questions.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showNextQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            endGame();
            return;
        }

        Question currentQuestion = questionList.get(currentQuestionIndex);
        tvQuestion.setText(currentQuestion.getQuestion());

        resetOptionBackgrounds();

        if (currentQuestion.isTextInputRequired()) {
            etAnswer.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            tvOption1.setVisibility(View.GONE);
            tvOption2.setVisibility(View.GONE);
            tvOption3.setVisibility(View.GONE);
            tvOption4.setVisibility(View.GONE);

            btnSubmit.setOnClickListener(v -> {
                String userAnswer = etAnswer.getText().toString().trim();
                if (userAnswer.isEmpty()) {
                    etAnswer.setError("Please enter an answer");
                    return;
                }
                checkAnswer(userAnswer, null);
            });
        } else {
            if (currentQuestion.getOptions() != null && currentQuestion.getOptions().size() == 4) {
                etAnswer.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.GONE);
                tvOption1.setVisibility(View.VISIBLE);
                tvOption2.setVisibility(View.VISIBLE);
                tvOption3.setVisibility(View.VISIBLE);
                tvOption4.setVisibility(View.VISIBLE);

                tvOption1.setText(currentQuestion.getOptions().get(0));
                tvOption2.setText(currentQuestion.getOptions().get(1));
                tvOption3.setText(currentQuestion.getOptions().get(2));
                tvOption4.setText(currentQuestion.getOptions().get(3));
            }
        }
    }

    private void setupOptionListeners() {
        tvOption1.setOnClickListener(v -> checkAnswer(tvOption1.getText().toString(), tvOption1));
        tvOption2.setOnClickListener(v -> checkAnswer(tvOption2.getText().toString(), tvOption2));
        tvOption3.setOnClickListener(v -> checkAnswer(tvOption3.getText().toString(), tvOption3));
        tvOption4.setOnClickListener(v -> checkAnswer(tvOption4.getText().toString(), tvOption4));
    }

    private void checkAnswer(String selectedAnswer, TextView selectedOption) {
        Question currentQuestion = questionList.get(currentQuestionIndex);

        boolean isCorrect = selectedAnswer.equalsIgnoreCase(currentQuestion.getCorrectAnswer());

        if (selectedOption != null) {
            selectedOption.setBackgroundResource(isCorrect ? R.drawable.option_correct : R.drawable.option_wrong);
        }

        if (isCorrect) {
            correctSound.start();
            score += 1;
            correctAnswers++;
        } else {
            wrongSound.start();
        }

        tvScore.setText("Score: " + score);

        new Handler().postDelayed(() -> {
            resetOptionBackgrounds();
            currentQuestionIndex++;
            showNextQuestion();
        }, 800);
    }

    private void resetOptionBackgrounds() {
        tvOption1.setBackgroundResource(R.drawable.rounded_option);
        tvOption2.setBackgroundResource(R.drawable.rounded_option);
        tvOption3.setBackgroundResource(R.drawable.rounded_option);
        tvOption4.setBackgroundResource(R.drawable.rounded_option);
    }

    private void endGame() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // 🔥 Set game state to "ended" so it doesn't restart
        databaseReference.child("rooms").child(roomCode).child("gameState").setValue("ended");

        // 🔥 Display "Time Up!" before switching to results
        tvTimer.setText("⏳ Time Up!");

        new android.os.Handler().postDelayed(() -> {
            // ✅ Store player's final score in Firebase
            DatabaseReference scoresRef = FirebaseDatabase.getInstance()
                    .getReference("rooms")
                    .child(roomCode)
                    .child("scores")
                    .child(getIntent().getStringExtra("username"));

            scoresRef.setValue(score).addOnCompleteListener(task -> {
                // 🔥 Redirect all players to Multiplayer Result Activity
                Intent intent = new Intent(MultiGameActivity.this, MultiplayerResultActivity.class);
                intent.putExtra("roomCode", roomCode);
                intent.putExtra("username", getIntent().getStringExtra("username"));
                intent.putExtra("username", currentUsername);
                startActivity(intent);
                finish();
            });
        }, 2000); // 🔥 Show "Time Up!" for 2 seconds
    }
}
