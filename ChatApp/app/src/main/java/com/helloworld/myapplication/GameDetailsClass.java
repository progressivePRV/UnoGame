package com.helloworld.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class GameDetailsClass implements Serializable {
    String player1Id;
    String player2Id;
    ArrayList<String> rejectedPlayers = new ArrayList<>();
    ArrayList<UnoCardClass> deckCards = new ArrayList<>();
    ArrayList<UnoCardClass> player1Cards = new ArrayList<>();
    ArrayList<UnoCardClass> player2Cards = new ArrayList<>();

    //can be requested, inprogress, draw, won - PlayerID
    String gameState;

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public ArrayList<String> getRejectedPlayers() {
        return rejectedPlayers;
    }

    public void setRejectedPlayers(ArrayList<String> rejectedPlayers) {
        this.rejectedPlayers = rejectedPlayers;
    }

    public ArrayList<UnoCardClass> getDeckCards() {
        return deckCards;
    }

    public void setDeckCards(ArrayList<UnoCardClass> deckCards) {
        this.deckCards = deckCards;
    }

    public ArrayList<UnoCardClass> getPlayer1Cards() {
        return player1Cards;
    }

    public void setPlayer1Cards(ArrayList<UnoCardClass> player1Cards) {
        this.player1Cards = player1Cards;
    }

    public ArrayList<UnoCardClass> getPlayer2Cards() {
        return player2Cards;
    }

    public void setPlayer2Cards(ArrayList<UnoCardClass> player2Cards) {
        this.player2Cards = player2Cards;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    @Override
    public String toString() {
        return "GameDetailsClass{" +
                "player1Id='" + player1Id + '\'' +
                ", player2Id='" + player2Id + '\'' +
                ", rejectedPlayers=" + rejectedPlayers +
                ", deckCards=" + deckCards +
                ", player1Cards=" + player1Cards +
                ", player2Cards=" + player2Cards +
                ", gameState='" + gameState + '\'' +
                '}';
    }
}
