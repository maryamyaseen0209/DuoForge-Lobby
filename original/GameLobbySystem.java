/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.techwebdocs.gamelobbysystem;

import java.util.ArrayList;
import java.util.Scanner;

// ----------------------
// Node class (stores Player or Match)
// ----------------------
class Node {
    Player player;
    Match match;
    Node next;

    Node(Player player) { this.player = player; }
    Node(Match match) { this.match = match; }
}

// ----------------------
// LinkedList class (stores Players)
// For Record of all Players
// ----------------------
class LinkedList {
    Node head;

    void insert(Player player) {
        Node newNode = new Node(player);
        newNode.next = head;
        head = newNode;
    }

    Player search(int keyPlayerID) {
        Node temp = head;
        while (temp != null) {
            if (temp.player.playerID == keyPlayerID)
                return temp.player;
            temp = temp.next;
        }
        return null;
    }

    void displayAllPlayers() {
        Node temp = head;
        System.out.println("Registered Players:");
        while (temp != null) {
            System.out.println("PlayerID: " + temp.player.playerID +
                    ", Rank: " + temp.player.rank +
                    ", Points: " + temp.player.points);
            temp = temp.next;
        }
    }
}

// ----------------------
// PlayerQueue (linked list-based queue)
// Matchmaking queue (linked list-based stack)
// ----------------------
class PlayerQueue {
    Node front, rear;
    int size;

    void enqueue(Player player)    //composition 
    {
        Node newNode = new Node(player);
        if (rear == null) {
            front = rear = newNode;
        }
        else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    Player dequeue() {
        if (front == null) {
            System.out.println("Queue is empty!");
            return null;
        }
        Player temp = front.player;
        front = front.next;
        if (front == null){     //For 1 node only
            rear = null;
        }
        size--;
        return temp;
    }

    boolean hasTwoPlayers() {   //Mandatory for Macthmaking
        return size >= 2; 
    }
}

// ----------------------
// MatchStack (linked list-based stack)
// ----------------------
class MatchStack {
    Node top;
    
    boolean isEmpty() { 
        return top == null; 
    }

    void push(Match match) {
        Node newNode = new Node(match);
        newNode.next = top;
        top = newNode;
    }

    void displayAll() {
        Node temp = top;
        System.out.println("---- Match History ----");
        while (temp != null) {
            temp.match.displayMatch();
            temp = temp.next;
        }
    }
}

// ----------------------
// Player class
// ----------------------
class Player {
    int playerID;
    int rank;   // (<= 2 = low) (<= 4 = mid) (> 4 = high)
    int points;
    ArrayList<Integer> friendList;  //Id of friend used here

    Player(int playerID) {
        this.playerID = playerID;
        this.rank = 1;
        this.points = 0;
        this.friendList = new ArrayList<>();
    }

    void addFriend(int friendID) {
        if (!friendList.contains(friendID))
            friendList.add(friendID);
    }

    void winGame() {
        points += 10;
        if (points >= 200) {
            rank++;
            points = 0;
            System.out.println("Yayyyyyyyyy!!! \n Levelup!!");
            System.out.println("Player " + playerID + " leveled up to Rank " + rank + "!");
        }
    }
}

// ----------------------
// Match class
// ----------------------
class Match {
    static int matchCounter = 0;
    int matchID;
    int player1ID; //to reduce space complexity we didn't make object
    int player2ID;

    Match(int player1ID, int player2ID) {
        this.matchID = ++matchCounter;
        this.player1ID = player1ID;
        this.player2ID = player2ID;
    }

    void displayMatch() {
        System.out.println("MatchID: " + matchID + ", Players: " + player1ID + " vs " + player2ID);
    }
}

// ----------------------
// BST Node and PlayerBST classes (non-linear data structure)
// To reduce time complexity as compared to linked-list
// ----------------------
class BSTNode {
    Player player;
    BSTNode left, right;

    BSTNode(Player player) {
        this.player = player;
    }
}

class PlayerBST {
    BSTNode root;

    void insert(Player player) {
        root = insertRec(root, player);
    }

    BSTNode insertRec(BSTNode root, Player player) {
        if (root == null) {
            return new BSTNode(player);
        }
        if (player.playerID < root.player.playerID) {
             root.left = insertRec(root.left, player);
        }          
        else if (player.playerID > root.player.playerID) {
            root.right = insertRec(root.right, player);
        }
        return root;
    }

    Player search(int playerID) {
        return searchRec(root, playerID);
    }

    Player searchRec(BSTNode root, int keyPlayerID) {
        if (root == null) {
            return null;
        }
        if (root.player.playerID == keyPlayerID) {
            return root.player; 
        }           
        if (keyPlayerID < root.player.playerID) {
            return searchRec(root.left, keyPlayerID);
        }
        else {
            return searchRec(root.right, keyPlayerID);
        }
    }

    void inorder() {
        inorderRec(root);
    }

    void inorderRec(BSTNode root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.println("PlayerID: " + root.player.playerID +
                               ", Rank: " + root.player.rank +
                               ", Points: " + root.player.points);
            inorderRec(root.right);
        }
    }
}

// ----------------------
// Main class
// ----------------------
public class GameLobbySystem {
    static Scanner sc = new Scanner(System.in);
    static LinkedList playerList = new LinkedList(); // linear structure
    static PlayerBST playerBST = new PlayerBST();    // non-linear structure
    static PlayerQueue lowRankQueue = new PlayerQueue();
    static PlayerQueue midRankQueue = new PlayerQueue();
    static PlayerQueue highRankQueue = new PlayerQueue();
    static MatchStack matchHistory = new MatchStack();

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n=== DUO GAME MATCHMAKING LOBBY SYSTEM ===");
            System.out.println("1. Online Match");
            System.out.println("2. Friend Match");
            System.out.println("3. Display Match History");
            System.out.println("4. Display All Players (LinkedList)");  //more time complexity
            System.out.println("5. Display All Players (BST)");         //reducetime complexity
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: onlineMatch(); break;
                case 2: friendMatch(); break;
                case 3: historyCheck(); break;
                case 4: playerList.displayAllPlayers(); break; //through linked-list
                case 5: playerBST.inorder(); break;            // through BST
                case 6: System.out.println("Exiting..."); break;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 6);
    }
    
   // Custom Function 
    static Player getPlayer(int id) {
        // First try BST search
        Player player = playerBST.search(id);
        if (player == null) {
            player = new Player(id);
            playerList.insert(player); //Also insert into linked-list
            playerBST.insert(player); 
            System.out.println("New player created with ID " + id);
        }
        return player;
    }
    
 
    static PlayerQueue getQueueByRank(int rank) {
        if (rank <= 2) return lowRankQueue;
        else if (rank <= 4) return midRankQueue;
        else return highRankQueue;
    }
    
    
    static void startMatch(Player p1, Player p2) {
        System.out.println("Match: Player " + p1.playerID + " vs Player " + p2.playerID);
        p1.winGame();
        p2.winGame();

        Match match = new Match(p1.playerID, p2.playerID);
        matchHistory.push(match);
        System.out.println("Match recorded with MatchID: " + match.matchID);
    }
    
    
    static void onlineMatch() {
        System.out.print("Enter your playerID: ");
        int id = sc.nextInt();
        Player player = getPlayer(id);

        PlayerQueue targetQueue = getQueueByRank(player.rank);
        targetQueue.enqueue(player);
        System.out.println("Player " + id + " added to Rank " + player.rank + " Queue.");

        if (targetQueue.hasTwoPlayers()) {
            Player p1 = targetQueue.dequeue();
            Player p2 = targetQueue.dequeue();
            startMatch(p1, p2);
        }
    }

    static void friendMatch() {
        System.out.print("Enter your playerID: ");
        int id = sc.nextInt();
        Player player = getPlayer(id);

        System.out.print("Enter friend's playerID: ");
        int friendID = sc.nextInt();
        player.addFriend(friendID);
        System.out.println("Friend " + friendID + " added to your friend list.");
        Player friend = getPlayer(friendID);

        startMatch(player, friend);
    }

   
    static void historyCheck() {
        if (matchHistory.isEmpty())
            System.out.println("No match history found.");
        else
            matchHistory.displayAll();
    }   
}

