package com.example.a3minsgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class RoomLobbyActivity extends AppCompatActivity {

    private TextView tvRoomCode, tvMessage;
    private LinearLayout playersContainer;
    private Button btnStartGame, btnLeaveRoom;
    private DatabaseReference databaseReference;
    private String roomCode;
    private boolean isHost;
    private List<String> playerNames = new ArrayList<>();
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_lobby);

        tvRoomCode = findViewById(R.id.tv_room_code);
        tvMessage = findViewById(R.id.tv_message);
        playersContainer = findViewById(R.id.players_container);
        btnStartGame = findViewById(R.id.btn_start_game);
        btnLeaveRoom = findViewById(R.id.btn_leave_room);

        roomCode = getIntent().getStringExtra("roomCode");
        isHost = getIntent().getBooleanExtra("isHost", false);
        currentUsername = getIntent().getStringExtra("username");

        tvRoomCode.setText("Room Code: " + roomCode);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (isHost) {
            btnStartGame.setVisibility(View.VISIBLE);
            btnStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGame();
                }
            });
        } else {
            btnStartGame.setVisibility(View.GONE);
            tvMessage.setText("Waiting for Host to Start");
        }

        btnLeaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveRoom();
            }
        });

        addPlayerToRoom();
        listenForPlayers();
        listenForGameState();
    }

    private void addPlayerToRoom() {
        DatabaseReference roomRef = databaseReference.child("rooms").child(roomCode).child("players").child(currentUsername);
        roomRef.setValue(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Failed to join room", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForPlayers() {
        DatabaseReference playersRef = databaseReference.child("rooms").child(roomCode).child("players");
        playersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playerNames.clear();
                playersContainer.removeAllViews();

                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerName = playerSnapshot.getKey();
                    playerNames.add(playerName);

                    TextView playerTextView = new TextView(RoomLobbyActivity.this);
                    playerTextView.setText("- " + playerName);
                    playerTextView.setTextSize(18);
                    playerTextView.setTextColor(getResources().getColor(R.color.black));
                    playersContainer.addView(playerTextView);
                }

                if (isHost && playerNames.size() >= 2) {
                    btnStartGame.setEnabled(true);
                    tvMessage.setText("Ready to Start");
                } else if (isHost) {
                    btnStartGame.setEnabled(false);
                    tvMessage.setText("Waiting for More Players");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomLobbyActivity.this, "Error loading players.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame() {
        DatabaseReference roomRef = databaseReference.child("rooms").child(roomCode);
        roomRef.child("gameState").setValue("countdown");

        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                roomRef.child("countdown").setValue(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                roomRef.child("gameState").setValue("started");
                Intent intent = new Intent(RoomLobbyActivity.this, MultiGameActivity.class);
                intent.putExtra("roomCode", roomCode);
                intent.putExtra("isHost", isHost);
                intent.putExtra("username", currentUsername);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void listenForGameState() {
        DatabaseReference roomRef = databaseReference.child("rooms").child(roomCode);
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String gameState = snapshot.child("gameState").getValue(String.class);
                if (gameState != null) {
                    switch (gameState) {
                        case "countdown":
                            Long countdown = snapshot.child("countdown").getValue(Long.class);
                            if (countdown != null) {
                                tvMessage.setText("Game starting in " + countdown + " seconds...");
                            } else {
                                tvMessage.setText("Game starting soon...");
                            }
                            break;
                        case "started":
                            Intent intent = new Intent(RoomLobbyActivity.this, MultiGameActivity.class);
                            intent.putExtra("roomCode", roomCode);
                            intent.putExtra("isHost", isHost);
                            intent.putExtra("username", currentUsername);
                            startActivity(intent);
                            finish();
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomLobbyActivity.this, "Error loading game state.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveRoom() {
        databaseReference.child("rooms").child(roomCode).child("players").child(currentUsername).removeValue()
                .addOnCompleteListener(task -> {
                    Toast.makeText(RoomLobbyActivity.this, "You have left the room", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
