package com.duoforge.lobby.ds;

import com.duoforge.lobby.model.Player;
import java.util.ArrayList;
import java.util.List;

public final class PlayerLinkedList {
    private Node head;
    private int size;

    private static final class Node {
        private final Player player;
        private Node next;
        private Node(Player player) { this.player = player; }
    }

    public void insert(Player player) {
        Node node = new Node(player);
        node.next = head;
        head = node;
        size++;
    }

    public Player search(int playerId) {
        Node current = head;
        while (current != null) {
            if (current.player.getPlayerId() == playerId) return current.player;
            current = current.next;
        }
        return null;
    }

    public List<Player> values() {
        ArrayList<Player> players = new ArrayList<>(size);
        Node current = head;
        while (current != null) {
            players.add(current.player);
            current = current.next;
        }
        return players;
    }

    public int size() { return size; }
}
