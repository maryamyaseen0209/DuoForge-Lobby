package com.duoforge.lobby;

import com.duoforge.lobby.model.Match;
import com.duoforge.lobby.model.Player;
import com.duoforge.lobby.service.LobbyEngine;

public final class LobbyEngineTest {
    public static void main(String[] args) {
        LobbyEngine engine = new LobbyEngine();
        engine.resetWithDemoData();

        Player created = engine.getOrCreatePlayer(9001);
        require(created.getPlayerId() == 9001, "Player registration failed");
        require(engine.getPlayer(9001) != null, "BST lookup failed");

        engine.cancelQueue(518);
        var waiting = engine.joinOnlineQueue(7001);
        require(!waiting.matched(), "First queue player should wait");
        var matched = engine.joinOnlineQueue(7002);
        require(matched.matched(), "Second queue player should create a match");
        require(matched.match() != null, "Online match missing");

        Match friendMatch = engine.createFriendMatch(8001, 8002);
        require(friendMatch.getPlayer1Id() == 8001, "Friend match player mismatch");
        require(engine.getPlayer(8001).getFriendList().contains(8002), "Friend list not updated");
        require(!engine.getMatches().isEmpty(), "Match stack should contain history");
        require(engine.getPlayersInBstOrder().get(0).getPlayerId() == 101, "BST inorder sorting failed");

        System.out.println("All DuoForge engine tests passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
