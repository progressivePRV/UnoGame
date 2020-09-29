package com.helloworld.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class GameDetailsClass implements Serializable {
    String player1Id;
    String player2Id;
    String player1Name;
    String player2Name;
    ArrayList<String> rejectedPlayers = new ArrayList<>();
    ArrayList<UnoCardClass> deckCards = new ArrayList<>();
    ArrayList<UnoCardClass> player1Cards = new ArrayList<>();
    ArrayList<UnoCardClass> player2Cards = new ArrayList<>();
    ArrayList<UnoCardClass> discardCards = new ArrayList<>();
    String turn;

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

    public ArrayList<UnoCardClass> getDiscardCards() {
        return discardCards;
    }

    public void setDiscardCards(ArrayList<UnoCardClass> discardCards) {
        this.discardCards = discardCards;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
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
                ", discardCards='" + discardCards + '\'' +
                ", turn='" + turn + '\'' +
                ", player1Name='" + player1Name + '\'' +
                ", player2Name='" + player2Name + '\'' +
                '}';
    }
}
