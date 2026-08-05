package com.duoforge.lobby.ds;

import com.duoforge.lobby.model.Match;
import java.util.ArrayList;
import java.util.List;

public final class MatchStack {
    private Node top;
    private int size;

    private static final class Node {
        private final Match match;
        private Node next;
        private Node(Match match) { this.match = match; }
    }

    public void push(Match match) {
        Node node = new Node(match);
        node.next = top;
        top = node;
        size++;
    }

    public List<Match> values() {
        ArrayList<Match> matches = new ArrayList<>(size);
        Node current = top;
        while (current != null) {
            matches.add(current.match);
            current = current.next;
        }
        return matches;
    }

    public int size() { return size; }
    public boolean isEmpty() { return top == null; }
}
