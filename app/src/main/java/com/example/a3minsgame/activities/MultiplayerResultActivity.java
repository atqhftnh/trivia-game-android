package com.example.a3minsgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiplayerResultActivity extends AppCompatActivity {

    private TextView tvResults;
    private Button btnExitRoom;
    private DatabaseReference databaseReference;
    private String roomCode, username;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_result);

        tvResults = findViewById(R.id.tv_results);
        btnExitRoom = findViewById(R.id.btn_exit_room);

        roomCode = getIntent().getStringExtra("roomCode");
        username = getIntent().getStringExtra("username");
        currentUsername = getIntent().getStringExtra("username");

        if (roomCode == null || username == null) {
            Toast.makeText(this, "Error: Missing room data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

        new Handler().postDelayed(this::loadScores, 1000);

        btnExitRoom.setOnClickListener(v -> exitRoom());
    }

    private void loadScores() {
        databaseReference.child("scores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(MultiplayerResultActivity.this, "No scores found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<PlayerScore> playerScores = new ArrayList<>();

                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerName = playerSnapshot.getKey();
                    Integer playerScore = playerSnapshot.getValue(Integer.class);

                    if (playerName != null && playerScore != null) {
                        playerScores.add(new PlayerScore(playerName, playerScore));
                    }
                }

                Collections.sort(playerScores, (a, b) -> Integer.compare(b.score, a.score));

                StringBuilder resultText = new StringBuilder("🏆 Multiplayer Rankings 🏆\n\n");
                int rank = 1;
                for (PlayerScore player : playerScores) {
                    resultText.append(rank).append(". ").append(player.name).append(" - ").append(player.score).append(" points\n");
                    rank++;
                }

                tvResults.setText(resultText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MultiplayerResultActivity.this, "Failed to load scores.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exitRoom() {
        if (username == null || username.isEmpty()) {
            databaseReference.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        String foundUsername = playerSnapshot.getKey();
                        if (foundUsername != null) {
                            username = foundUsername;
                            removePlayerAndExit();
                            return;
                        }
                    }
                    Toast.makeText(MultiplayerResultActivity.this, "Username not found. Please login again.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MultiplayerResult", "Failed to retrieve username: " + error.getMessage());
                }
            });
        } else {
            removePlayerAndExit();
        }
    }

    private void removePlayerAndExit() {
        databaseReference.child("players").child(username).removeValue().addOnCompleteListener(task -> {
            checkAndDeleteRoom();

            Intent intent = new Intent(MultiplayerResultActivity.this, MainMenuActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
        });
    }

    private void checkAndDeleteRoom() {
        databaseReference.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    databaseReference.removeValue().addOnCompleteListener(task -> {
                        Log.d("MultiplayerResult", "Room " + roomCode + " deleted.");
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MultiplayerResult", "Failed to check room deletion: " + error.getMessage());
            }
        });
    }

    private static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}
