package com.duoforge.lobby.service;

import com.duoforge.lobby.ds.MatchStack;
import com.duoforge.lobby.ds.PlayerBST;
import com.duoforge.lobby.ds.PlayerLinkedList;
import com.duoforge.lobby.ds.PlayerQueue;
import com.duoforge.lobby.model.Match;
import com.duoforge.lobby.model.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class LobbyEngine {
    private PlayerLinkedList playerList;
    private PlayerBST playerBST;
    private PlayerQueue rookieQueue;
    private PlayerQueue eliteQueue;
    private PlayerQueue masterQueue;
    private MatchStack matchHistory;
    private int matchCounter;
    private final Random random = new Random(42);

    public LobbyEngine() {
        resetWithDemoData();
    }

    public synchronized Player getOrCreatePlayer(int playerId) {
        validateId(playerId);
        Player player = playerBST.search(playerId);
        if (player == null) {
            player = new Player(playerId);
            playerList.insert(player);
            playerBST.insert(player);
        }
        return player;
    }

    public synchronized Player getPlayer(int playerId) {
        return playerBST.search(playerId);
    }

    public synchronized MatchmakingResult joinOnlineQueue(int playerId) {
        Player player = getOrCreatePlayer(playerId);
        removeFromAllQueues(playerId);
        PlayerQueue queue = queueFor(player);
        queue.enqueue(player);

        if (!queue.hasTwoPlayers()) {
            return new MatchmakingResult(false, null,
                    "Player " + playerId + " entered the " + player.getTier() + " queue.");
        }

        Player player1 = queue.dequeue();
        Player player2 = queue.dequeue();
        Match match = completeMatch(player1, player2, "ONLINE", player.getTier());
        return new MatchmakingResult(true, match,
                "Match found: Player " + player1.getPlayerId() + " vs Player " + player2.getPlayerId() + ".");
    }

    public synchronized Match createFriendMatch(int playerId, int friendId) {
        validateId(playerId);
        validateId(friendId);
        if (playerId == friendId) throw new IllegalArgumentException("A player cannot challenge themselves.");

        Player player = getOrCreatePlayer(playerId);
        Player friend = getOrCreatePlayer(friendId);
        player.addFriend(friendId);
        friend.addFriend(playerId);
        removeFromAllQueues(playerId);
        removeFromAllQueues(friendId);
        return completeMatch(player, friend, "FRIEND", mixedTier(player, friend));
    }

    public synchronized boolean cancelQueue(int playerId) {
        return removeFromAllQueues(playerId);
    }

    public synchronized List<Player> getPlayersInBstOrder() {
        return playerBST.inOrder();
    }

    public synchronized List<Player> getLeaderboard() {
        ArrayList<Player> players = new ArrayList<>(playerList.values());
        players.sort(Comparator.comparingInt(Player::getRank).reversed()
                .thenComparing(Comparator.comparingInt(Player::getPoints).reversed())
                .thenComparing(Comparator.comparingInt(Player::getWins).reversed())
                .thenComparingInt(Player::getPlayerId));
        return players;
    }

    public synchronized List<Match> getMatches() {
        return matchHistory.values();
    }

    public synchronized List<Player> getQueue(String tier) {
        return switch (tier.toUpperCase()) {
            case "ROOKIE" -> rookieQueue.values();
            case "ELITE" -> eliteQueue.values();
            case "MASTER" -> masterQueue.values();
            default -> List.of();
        };
    }

    public synchronized int getPlayerCount() { return playerList.size(); }
    public synchronized int getMatchCount() { return matchHistory.size(); }
    public synchronized int getQueuedCount() { return rookieQueue.size() + eliteQueue.size() + masterQueue.size(); }

    public synchronized void resetWithDemoData() {
        playerList = new PlayerLinkedList();
        playerBST = new PlayerBST();
        rookieQueue = new PlayerQueue();
        eliteQueue = new PlayerQueue();
        masterQueue = new PlayerQueue();
        matchHistory = new MatchStack();
        matchCounter = 0;

        Player p101 = getOrCreatePlayer(101); p101.setDemoProgress(6, 150, 42, 18);
        Player p205 = getOrCreatePlayer(205); p205.setDemoProgress(4, 120, 31, 22);
        Player p309 = getOrCreatePlayer(309); p309.setDemoProgress(3, 80, 24, 16);
        Player p412 = getOrCreatePlayer(412); p412.setDemoProgress(2, 190, 18, 13);
        Player p518 = getOrCreatePlayer(518); p518.setDemoProgress(1, 90, 9, 8);
        Player p624 = getOrCreatePlayer(624); p624.setDemoProgress(1, 40, 4, 5);

        p101.addFriend(205); p205.addFriend(101);
        p309.addFriend(412); p412.addFriend(309);
        completeMatch(p101, p205, "FRIEND", "MASTER");
        completeMatch(p309, p412, "ONLINE", "ELITE");
        rookieQueue.enqueue(p518);
    }

    private Match completeMatch(Player player1, Player player2, String type, String tier) {
        Player winner = random.nextBoolean() ? player1 : player2;
        Player loser = winner == player1 ? player2 : player1;
        winner.awardWin();
        loser.awardLoss();
        Match match = new Match(++matchCounter, player1.getPlayerId(), player2.getPlayerId(),
                winner.getPlayerId(), type, tier);
        matchHistory.push(match);
        return match;
    }

    private PlayerQueue queueFor(Player player) {
        return switch (player.getTier()) {
            case "ROOKIE" -> rookieQueue;
            case "ELITE" -> eliteQueue;
            default -> masterQueue;
        };
    }

    private String mixedTier(Player player1, Player player2) {
        if (player1.getRank() >= 5 || player2.getRank() >= 5) return "MASTER";
        if (player1.getRank() >= 3 || player2.getRank() >= 3) return "ELITE";
        return "ROOKIE";
    }

    private boolean removeFromAllQueues(int playerId) {
        boolean removed = rookieQueue.remove(playerId);
        removed = eliteQueue.remove(playerId) || removed;
        removed = masterQueue.remove(playerId) || removed;
        return removed;
    }

    private void validateId(int playerId) {
        if (playerId <= 0 || playerId > 999_999) {
            throw new IllegalArgumentException("Player ID must be between 1 and 999999.");
        }
    }

    public record MatchmakingResult(boolean matched, Match match, String message) {}
}
