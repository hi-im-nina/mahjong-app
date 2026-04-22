package com.mahjong.dto;

import com.mahjong.model.Tile;

import java.util.ArrayList;
import java.util.List;

public class GameStateResponse {

    private List<Tile> currentPlayerHand;
    private List<List<Tile>> opponentHands;
    private List<Tile> discardedTiles;

    private List<Tile> tileStack;
    private int currentPlayerTurn;
    private boolean isYourTurn;
    private List<List<Tile>> currentPlayerFinishedHand;
    private boolean isValidMove;
    private int winnerId;
    private int gameId;
    private List<String> moves;
    private boolean canDraw;
    private boolean canDiscard;
    private String message;

    public GameStateResponse() {
        this.currentPlayerHand = new ArrayList<>();
        this.opponentHands = new ArrayList<>();
        this.discardedTiles = new ArrayList<>();
        this.currentPlayerFinishedHand = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.tileStack = new ArrayList<>();
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public List<Tile> getCurrentPlayerHand() {
        return currentPlayerHand;
    }

    public void setCurrentPlayerHand(List<Tile> currentPlayerHand) {
        this.currentPlayerHand = currentPlayerHand;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public List<List<Tile>> getOpponentHands() {
        return opponentHands;
    }

    public void setOpponentHands(List<List<Tile>> opponentHands) {
        this.opponentHands = opponentHands;
    }

    public List<Tile> getDiscardedTiles() {
        return discardedTiles;
    }

    public void setDiscardedTiles(List<Tile> discardedTiles) {
        this.discardedTiles = discardedTiles;
    }

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public boolean isYourTurn() {
        return isYourTurn;
    }

    public void setYourTurn(boolean isYourTurn) {
        this.isYourTurn = isYourTurn;
    }

    public List<List<Tile>> getCurrentPlayerFinishedHand() {
        return currentPlayerFinishedHand;
    }

    public void setCurrentPlayerFinishedHand(List<List<Tile>> currentPlayerFinishedHand) {
        this.currentPlayerFinishedHand = currentPlayerFinishedHand;
    }

    public boolean isValidMove() {
        return isValidMove;
    }

    public void setValidMove(boolean isValidMove) {
        this.isValidMove = isValidMove;
    }

    public List<Tile> getTileStack() {
        return tileStack;
    }

    public void setTileStack(List<Tile> tileStack) {
        this.tileStack = tileStack;
    }

    public boolean isCanDraw() {
        return canDraw;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public boolean isCanDiscard() {
        return canDiscard;
    }

    public void setCanDiscard(boolean canDiscard) {
        this.canDiscard = canDiscard;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
