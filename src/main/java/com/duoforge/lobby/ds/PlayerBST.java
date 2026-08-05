package com.duoforge.lobby.ds;

import com.duoforge.lobby.model.Player;
import java.util.ArrayList;
import java.util.List;

public final class PlayerBST {
    private Node root;

    private static final class Node {
        private final Player player;
        private Node left;
        private Node right;
        private Node(Player player) { this.player = player; }
    }

    public void insert(Player player) {
        root = insert(root, player);
    }

    private Node insert(Node node, Player player) {
        if (node == null) return new Node(player);
        if (player.getPlayerId() < node.player.getPlayerId()) node.left = insert(node.left, player);
        else if (player.getPlayerId() > node.player.getPlayerId()) node.right = insert(node.right, player);
        return node;
    }

    public Player search(int playerId) {
        Node current = root;
        while (current != null) {
            if (playerId == current.player.getPlayerId()) return current.player;
            current = playerId < current.player.getPlayerId() ? current.left : current.right;
        }
        return null;
    }

    public List<Player> inOrder() {
        ArrayList<Player> players = new ArrayList<>();
        traverse(root, players);
        return players;
    }

    private void traverse(Node node, List<Player> players) {
        if (node == null) return;
        traverse(node.left, players);
        players.add(node.player);
        traverse(node.right, players);
    }
}
