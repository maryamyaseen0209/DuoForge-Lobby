# DuoForge Lobby — Project Report

## 1. Introduction

DuoForge Lobby is a web-based duo game matchmaking system developed from an original Java console application. The product demonstrates how fundamental Data Structures and Algorithms can support a realistic interactive system rather than remaining isolated console exercises.

The application registers players, searches them by ID, divides them into rank tiers, pairs compatible players, supports friend matches, updates competitive progress, and keeps newest-first match history.

## 2. Problem statement

A game lobby needs to perform several operations efficiently:

1. retain every registered player;
2. locate a player using an identifier;
3. maintain fair first-in-first-out matchmaking;
4. keep recently completed matches immediately accessible;
5. represent friendship relations;
6. present the system in a usable, understandable interface.

The project solves these requirements using custom Java structures and exposes them through a REST API and cinematic browser UI.

## 3. Objectives

- Implement both linear and non-linear data structures.
- Compare Linked List traversal with BST search and traversal.
- Apply Queue behavior to realistic matchmaking.
- Apply Stack behavior to match history.
- Build a complete product without hiding the DSA implementation behind framework collections.
- Improve validation, correctness and usability of the original console application.

## 4. Data structures

### 4.1 Player Linked List

Every new player is inserted at the head of a custom singly linked list. Head insertion is O(1). A sequential player search is O(n). The list remains the complete registration record.

### 4.2 Player Binary Search Tree

The same Player object is inserted into a custom BST, using player ID as the key. Average search and insertion are O(log n), although an unbalanced worst case can become O(n). In-order traversal returns players sorted by ID.

### 4.3 Rank matchmaking Queues

Three custom linked queues represent Rookie, Elite and Master tiers. Enqueue and dequeue are O(1) because both front and rear references are maintained. When a queue contains two players, the first two are paired automatically.

### 4.4 Match Stack

Every completed match is pushed onto a custom linked Stack. The newest match becomes the top node, giving O(1) push behavior and naturally producing newest-first history.

### 4.5 ArrayList friendship network

Each player owns an ArrayList of friend IDs. The product adds friendship in both directions and prevents duplicate or self relations.

## 5. Product workflow

### Online matchmaking

```text
Player ID submitted
→ BST search
→ create Player when absent
→ calculate rank tier
→ remove duplicate queue entry
→ enqueue in tier Queue
→ if two players exist, dequeue both
→ select winner
→ update win/loss and points
→ push Match onto Stack
→ return updated product state
```

### Friend duel

```text
Two IDs submitted
→ validate different IDs
→ locate/create both players
→ create mutual friendship
→ remove both from public queues
→ complete match
→ push result to history Stack
```

## 6. Correctness improvements

The original implementation awarded win points to both participants when a match started. DuoForge selects one winner and records one loss. It also prevents duplicate queue entries, validates player IDs, prevents self-challenges, supports queue cancellation, and synchronizes engine mutations so simultaneous browser requests cannot corrupt linked structures.

## 7. Technology

- Java 21
- Built-in Java HTTP Server
- HTML5
- CSS3
- Vanilla JavaScript
- Canvas 2D animation
- Docker
- GitHub Actions

No external Java or JavaScript dependencies are required.

## 8. Testing

A dependency-free Java test program checks registration, BST lookup, online queue behavior, automatic match formation, friend matches, mutual friendships, history Stack state and sorted BST traversal. API smoke testing also validates health, dashboard state and POST matchmaking behavior.

## 9. Limitations

- Data is in-memory and resets after process restart.
- The BST is not self-balancing.
- Match winner selection is simulated.
- There is no authentication or real-time socket communication.
- One Java process represents one lobby instance.

## 10. Future work

- AVL or Red-Black Tree for guaranteed O(log n) search.
- Persistent PostgreSQL or MongoDB storage while retaining DSA views for teaching.
- WebSocket queue updates.
- Player login and profiles.
- ELO/MMR matchmaking.
- Tournaments, teams and party queues.
- Administrative moderation dashboard.
