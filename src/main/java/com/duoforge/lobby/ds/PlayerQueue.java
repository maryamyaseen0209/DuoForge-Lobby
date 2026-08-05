package com.duoforge.lobby.ds;

import com.duoforge.lobby.model.Player;
import java.util.ArrayList;
import java.util.List;

public final class PlayerQueue {
    private Node front;
    private Node rear;
    private int size;

    private static final class Node {
        private final Player player;
        private Node next;
        private Node(Player player) { this.player = player; }
    }

    public void enqueue(Player player) {
        if (contains(player.getPlayerId())) return;
        Node node = new Node(player);
        if (rear == null) front = rear = node;
        else {
            rear.next = node;
            rear = node;
        }
        size++;
    }

    public Player dequeue() {
        if (front == null) return null;
        Player player = front.player;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return player;
    }

    public boolean remove(int playerId) {
        Node previous = null;
        Node current = front;
        while (current != null) {
            if (current.player.getPlayerId() == playerId) {
                if (previous == null) front = current.next;
                else previous.next = current.next;
                if (current == rear) rear = previous;
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public boolean contains(int playerId) {
        Node current = front;
        while (current != null) {
            if (current.player.getPlayerId() == playerId) return true;
            current = current.next;
        }
        return false;
    }

    public List<Player> values() {
        ArrayList<Player> players = new ArrayList<>(size);
        Node current = front;
        while (current != null) {
            players.add(current.player);
            current = current.next;
        }
        return players;
    }

    public boolean hasTwoPlayers() { return size >= 2; }
    public int size() { return size; }
}
