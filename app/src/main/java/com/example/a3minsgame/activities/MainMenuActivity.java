package com.example.a3minsgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;

public class MainMenuActivity extends AppCompatActivity {

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnSinglePlayer = findViewById(R.id.btn_single_player);
        Button btnMultiplayer = findViewById(R.id.btn_multiplayer);
        Button btnLeaderboard = findViewById(R.id.btn_leaderboard);
        Button btnLogout = findViewById(R.id.btn_logout);

        btnSinglePlayer.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, DifficultySelectionActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        btnMultiplayer.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MultiplayerLobbyActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(MainMenuActivity.this, SinglePlayerLeaderboardActivity.class))
        );

        btnLogout.setOnClickListener(v -> {
            finish();
        });
    }
}