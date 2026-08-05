package com.duoforge.lobby.model;

import java.time.Instant;

public final class Match {
    private final int matchId;
    private final int player1Id;
    private final int player2Id;
    private final int winnerId;
    private final String matchType;
    private final String tier;
    private final Instant playedAt;

    public Match(int matchId, int player1Id, int player2Id, int winnerId, String matchType, String tier) {
        this.matchId = matchId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = winnerId;
        this.matchType = matchType;
        this.tier = tier;
        this.playedAt = Instant.now();
    }

    public int getMatchId() { return matchId; }
    public int getPlayer1Id() { return player1Id; }
    public int getPlayer2Id() { return player2Id; }
    public int getWinnerId() { return winnerId; }
    public String getMatchType() { return matchType; }
    public String getTier() { return tier; }
    public Instant getPlayedAt() { return playedAt; }
}
