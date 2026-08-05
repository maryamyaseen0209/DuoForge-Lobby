package com.duoforge.lobby.server;

import com.duoforge.lobby.model.Match;
import com.duoforge.lobby.model.Player;
import com.duoforge.lobby.service.LobbyEngine;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Json {
    private Json() {}

    public static int requiredInt(String body, String field) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(body == null ? "" : body);
        if (!matcher.find()) throw new IllegalArgumentException("Missing numeric field: " + field);
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric field: " + field);
        }
    }

    public static String quote(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    public static String player(Player player) {
        StringBuilder friends = new StringBuilder("[");
        for (int i = 0; i < player.getFriendList().size(); i++) {
            if (i > 0) friends.append(',');
            friends.append(player.getFriendList().get(i));
        }
        friends.append(']');
        return "{" +
                "\"playerId\":" + player.getPlayerId() + ',' +
                "\"rank\":" + player.getRank() + ',' +
                "\"tier\":" + quote(player.getTier()) + ',' +
                "\"points\":" + player.getPoints() + ',' +
                "\"wins\":" + player.getWins() + ',' +
                "\"losses\":" + player.getLosses() + ',' +
                "\"friends\":" + friends + ',' +
                "\"createdAt\":" + quote(player.getCreatedAt().toString()) +
                "}";
    }

    public static String match(Match match) {
        if (match == null) return "null";
        return "{" +
                "\"matchId\":" + match.getMatchId() + ',' +
                "\"player1Id\":" + match.getPlayer1Id() + ',' +
                "\"player2Id\":" + match.getPlayer2Id() + ',' +
                "\"winnerId\":" + match.getWinnerId() + ',' +
                "\"matchType\":" + quote(match.getMatchType()) + ',' +
                "\"tier\":" + quote(match.getTier()) + ',' +
                "\"playedAt\":" + quote(match.getPlayedAt().toString()) +
                "}";
    }

    public static String players(List<Player> players) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) json.append(',');
            json.append(player(players.get(i)));
        }
        return json.append(']').toString();
    }

    public static String matches(List<Match> matches) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < matches.size(); i++) {
            if (i > 0) json.append(',');
            json.append(match(matches.get(i)));
        }
        return json.append(']').toString();
    }

    public static String state(LobbyEngine engine) {
        return "{" +
                "\"overview\":{" +
                "\"players\":" + engine.getPlayerCount() + ',' +
                "\"matches\":" + engine.getMatchCount() + ',' +
                "\"queued\":" + engine.getQueuedCount() + ',' +
                "\"engine\":\"ONLINE\"}," +
                "\"players\":" + players(engine.getLeaderboard()) + ',' +
                "\"bstPlayers\":" + players(engine.getPlayersInBstOrder()) + ',' +
                "\"matches\":" + matches(engine.getMatches()) + ',' +
                "\"queues\":{" +
                "\"ROOKIE\":" + players(engine.getQueue("ROOKIE")) + ',' +
                "\"ELITE\":" + players(engine.getQueue("ELITE")) + ',' +
                "\"MASTER\":" + players(engine.getQueue("MASTER")) +
                "}}";
    }
}
