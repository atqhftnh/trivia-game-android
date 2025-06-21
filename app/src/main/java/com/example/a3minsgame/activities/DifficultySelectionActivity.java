package com.example.a3minsgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;

public class DifficultySelectionActivity extends AppCompatActivity {

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_selection);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnBeginner = findViewById(R.id.btn_beginner);
        Button btnIntermediate = findViewById(R.id.btn_intermediate);
        Button btnAdvanced = findViewById(R.id.btn_advanced);

        btnBeginner.setOnClickListener(v -> startGame("beginner", currentUsername));
        btnIntermediate.setOnClickListener(v -> startGame("intermediate", currentUsername));
        btnAdvanced.setOnClickListener(v -> startGame("advanced", currentUsername));
    }

    private void startGame(String difficulty, String currentUsername) {
        Intent intent = new Intent(DifficultySelectionActivity.this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("username", currentUsername); // 🔥 Pass username
        startActivity(intent);
    }
}