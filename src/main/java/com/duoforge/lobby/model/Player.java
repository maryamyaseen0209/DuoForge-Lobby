package com.duoforge.lobby.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Player {
    public static final int POINTS_PER_WIN = 10;
    public static final int POINTS_PER_RANK = 200;

    private final int playerId;
    private int rank;
    private int points;
    private int wins;
    private int losses;
    private final ArrayList<Integer> friendList;
    private final Instant createdAt;

    public Player(int playerId) {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Player ID must be a positive number.");
        }
        this.playerId = playerId;
        this.rank = 1;
        this.points = 0;
        this.friendList = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public int getPlayerId() { return playerId; }
    public int getRank() { return rank; }
    public int getPoints() { return points; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Integer> getFriendList() { return Collections.unmodifiableList(friendList); }

    public String getTier() {
        if (rank <= 2) return "ROOKIE";
        if (rank <= 4) return "ELITE";
        return "MASTER";
    }

    public boolean addFriend(int friendId) {
        if (friendId == playerId || friendList.contains(friendId)) return false;
        friendList.add(friendId);
        return true;
    }

    public boolean awardWin() {
        wins++;
        points += POINTS_PER_WIN;
        boolean levelledUp = false;
        while (points >= POINTS_PER_RANK) {
            points -= POINTS_PER_RANK;
            rank++;
            levelledUp = true;
        }
        return levelledUp;
    }

    public void awardLoss() {
        losses++;
    }

    public void setDemoProgress(int rank, int points, int wins, int losses) {
        this.rank = Math.max(1, rank);
        this.points = Math.max(0, Math.min(points, POINTS_PER_RANK - 1));
        this.wins = Math.max(0, wins);
        this.losses = Math.max(0, losses);
    }
}
