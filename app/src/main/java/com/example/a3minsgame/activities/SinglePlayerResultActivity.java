package com.example.a3minsgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;
import com.google.firebase.database.*;

public class SinglePlayerResultActivity extends AppCompatActivity {

    private String username;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_result);

        TextView tvScore = findViewById(R.id.tv_score);
        TextView tvCorrectAnswers = findViewById(R.id.tv_correct_answers);
        TextView tvTotalPoints = findViewById(R.id.tv_total_points);
        Button btnBackToMenu = findViewById(R.id.btn_back_to_menu);

        // Get data from the intent
        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        int correctAnswers = intent.getIntExtra("correctAnswers", 0);
        int questionsAttempted = intent.getIntExtra("questionsAttempted", 0);
        username = intent.getStringExtra("username");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(username);

        // Display results
        tvScore.setText("Score: " + score);
        tvCorrectAnswers.setText("Correct Answers: " + correctAnswers + "/" + questionsAttempted);

        // 🔥 Retrieve and update total points
        databaseReference.child("totalPoints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int previousTotalPoints = 0;

                if (snapshot.exists()) {
                    previousTotalPoints = snapshot.getValue(Integer.class);
                }

                int newTotalPoints = previousTotalPoints + score; // 🔥 Add new score to existing total

                // 🔥 Update total points in Firebase
                databaseReference.child("totalPoints").setValue(newTotalPoints).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvTotalPoints.setText("Total Points: " + newTotalPoints); // 🔥 Display updated points
                    } else {
                        Toast.makeText(SinglePlayerResultActivity.this, "Failed to update total points.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SinglePlayerResultActivity.this, "Error retrieving total points.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle back to menu button click
        btnBackToMenu.setOnClickListener(v -> {
            Intent mainIntent = new Intent(SinglePlayerResultActivity.this, MainMenuActivity.class);
            mainIntent.putExtra("username", username);
            startActivity(mainIntent);
            finish();
        });
    }
}
