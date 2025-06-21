package com.example.a3minsgame.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a3minsgame.R;
import com.example.a3minsgame.models.PlayerScore;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<PlayerScore> playerScores;

    public LeaderboardAdapter(List<PlayerScore> playerScores) {
        this.playerScores = playerScores;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        PlayerScore player = playerScores.get(position);
        holder.rank.setText(String.valueOf(position + 1)); // Ranking starts from 1
        holder.username.setText(player.getName());
        holder.score.setText(player.getScore() + " pts");
    }

    @Override
    public int getItemCount() {
        return playerScores.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView rank, username, score;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.tv_rank);
            username = itemView.findViewById(R.id.tv_username);
            score = itemView.findViewById(R.id.tv_score);
        }
    }
}
