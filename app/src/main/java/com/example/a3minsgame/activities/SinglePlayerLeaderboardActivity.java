package com.example.a3minsgame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a3minsgame.R;
import com.example.a3minsgame.adapters.LeaderboardAdapter;
import com.example.a3minsgame.models.PlayerScore;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SinglePlayerLeaderboardActivity extends AppCompatActivity {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter leaderboardAdapter;
    private List<PlayerScore> playerScores;
    private DatabaseReference usersRef;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_leaderboard);

        leaderboardRecyclerView = findViewById(R.id.recycler_leaderboard);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playerScores = new ArrayList<>();
        leaderboardAdapter = new LeaderboardAdapter(playerScores);
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(SinglePlayerLeaderboardActivity.this, "No leaderboard data available!", Toast.LENGTH_SHORT).show();
                    return;
                }

                playerScores.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    Integer totalPoints = userSnapshot.child("totalPoints").getValue(Integer.class);

                    if (username != null && totalPoints != null) {
                        playerScores.add(new PlayerScore(username, totalPoints));
                    }
                }

                Collections.sort(playerScores, (a, b) -> Integer.compare(b.getScore(), a.getScore()));

                leaderboardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SinglePlayerLeaderboardActivity.this, "Failed to load leaderboard.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
