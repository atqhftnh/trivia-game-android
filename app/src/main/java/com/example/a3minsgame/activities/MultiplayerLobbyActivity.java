package com.example.a3minsgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a3minsgame.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MultiplayerLobbyActivity extends AppCompatActivity {

    private Button btnCreateRoom, btnJoinRoom;
    private EditText etRoomCode; // Add EditText for room code input
    private DatabaseReference databaseReference;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_lobby);

        btnCreateRoom = findViewById(R.id.btn_create_room);
        btnJoinRoom = findViewById(R.id.btn_join_room);
        etRoomCode = findViewById(R.id.et_room_code);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            Log.e("MultiplayerLobby", "Username is null. Check if it was passed correctly from MainMenuActivity.");
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.d("MultiplayerLobby", "Username retrieved: " + currentUsername);
        }

        btnCreateRoom.setOnClickListener(v -> createRoom());
        btnJoinRoom.setOnClickListener(v -> joinRoom());
    }

    private void createRoom() {
        String roomCode = generateRoomCode();

        databaseReference.child("rooms").child(roomCode).child("players").child(currentUsername).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(MultiplayerLobbyActivity.this, RoomLobbyActivity.class);
                        intent.putExtra("roomCode", roomCode);
                        intent.putExtra("isHost", true);
                        intent.putExtra("username", currentUsername);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Failed to create room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void joinRoom() {
        String roomCode = etRoomCode.getText().toString().trim();

        if (roomCode.isEmpty()) {
            Toast.makeText(this, "Please enter a room code", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("rooms").child(roomCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(MultiplayerLobbyActivity.this, RoomLobbyActivity.class);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("isHost", false);
                    intent.putExtra("username", currentUsername);
                    startActivity(intent);
                } else {
                    Toast.makeText(MultiplayerLobbyActivity.this, "Room not found. Please check the room code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MultiplayerLobbyActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateRoomCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}