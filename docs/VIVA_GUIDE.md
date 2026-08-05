# DuoForge Viva Guide

## One-minute introduction

“DuoForge is a Java-based duo matchmaking product created from my console Data Structures project. It intentionally uses a custom Linked List to retain players, a Binary Search Tree to search and sort players by ID, three FIFO Queues for rank-based matchmaking, a LIFO Stack for newest-first match history, and ArrayLists for friendships. I added a Java REST server and cinematic browser interface so these structures can be observed in a complete functioning product.”

## Why store a Player in both Linked List and BST?

The Linked List demonstrates linear storage and O(1) head insertion. The BST provides a faster average ID lookup and sorted in-order traversal. Both structures hold references to the same Player object, so progress changes remain consistent.

## Why is Queue suitable for matchmaking?

Matchmaking should normally be fair to waiting players. FIFO behavior means the player who entered first is matched first. Front and rear references make enqueue and dequeue O(1).

## Why is Stack suitable for history?

Users usually want the latest match first. A Stack pushes the newest match onto the top in O(1), naturally producing reverse chronological order.

## Time complexity summary

| Operation | Structure | Complexity |
|---|---|---|
| Add player record | Linked List | O(1) |
| Search player linearly | Linked List | O(n) |
| Search player average | BST | O(log n) |
| Search player worst case | BST | O(n) |
| Enqueue player | Queue | O(1) |
| Dequeue player | Queue | O(1) |
| Push match | Stack | O(1) |
| Display all history | Stack traversal | O(n) |

## Why can BST become O(n)?

This is a normal unbalanced BST. If IDs are inserted in sorted order, nodes can form a chain. AVL or Red-Black Tree could keep it balanced.

## What was wrong in the original match logic?

Both players called `winGame()`, so both received win points. The product selects a single winner, increments the winner’s wins and points, and records a loss for the other player.

## How does ranking work?

A winner earns 10 points. At 200 points the player advances one rank and the threshold points are consumed. Ranks 1–2 are Rookie, 3–4 are Elite, and rank 5 or above is Master.

## How does the frontend communicate with Java?

The Java built-in HTTP server exposes JSON endpoints. The browser uses Fetch API calls. The same Java JAR serves both static frontend files and API responses, which avoids cross-origin configuration.

## Why not use a HashMap for players?

A HashMap would be practical in a commercial system, but the academic purpose is to visibly demonstrate Linked List and BST behavior. The product therefore preserves those structures.

## Is this production-ready?

It is product-style and fully functioning for a semester demonstration, but commercial production would require persistence, authentication, security hardening, distributed matchmaking, monitoring and a self-balancing index.

## Demo sequence

1. Open the homepage and explain the four DSA structures.
2. Enter a new ID in Quick Match; show it waiting in a tier Queue.
3. Enter another new ID; show automatic FIFO pairing and winner animation.
4. Start a Friend Duel; show friendship and match history update.
5. Switch from Leaderboard to BST in-order view.
6. Click a player to open their profile drawer.
7. Point out newest-first Stack history.
8. Reset the demo arena for repeatable presentation.
