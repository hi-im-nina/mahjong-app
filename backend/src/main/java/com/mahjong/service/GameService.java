package com.mahjong.service;

import com.mahjong.dao.GameDao;
import com.mahjong.dto.ChowRequest;
import com.mahjong.dto.DiscardTileRequest;
import com.mahjong.dto.GameStateResponse;
import com.mahjong.dto.KangRequest;
import com.mahjong.dto.PongRequest;
import com.mahjong.model.Tile;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    @Autowired
    private GameDao gameDao;

    /**
     * Creates a new Mahjong game
     * 
     * @return the new game ID
     */
    public int createNewGame() {
        // Initialize and setup new game
        MahjongGame mahjongGame = new MahjongGame();
        mahjongGame.initializeTiles();
        mahjongGame.dealTiles();
        System.out.println("Created a new Mahjong game.");

        // Convert to response/state object
        GameStateResponse gameState = convertToGameStateResponse(mahjongGame);
        gameState.setCurrentPlayerTurn(1);
        gameState.setWinnerId(-1);
        gameState.setCanDraw(true);

        // Save to database
        int gameId = gameDao.createGame(gameState);

        return gameId;
    }

    public GameStateResponse sortTiles(int gameId, int playerId) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        MahjongGame mahjongGame = reconstructMahjongGame(gameState);

        mahjongGame.setPlayerHand(playerId, mahjongGame.sortTiles(playerId));

        // Convert back to response
        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);

        // Save updated state to database
        gameDao.updateGame(gameId, updatedState);

        return updatedState;
    }

    public GameStateResponse exchangeFlowers(int gameId, int playerId) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        MahjongGame mahjongGame = reconstructMahjongGame(gameState);

        mahjongGame.exchangeTiles(1);
        mahjongGame.setPlayerHand(1, mahjongGame.sortTiles(1));

        mahjongGame.exchangeTiles(2);
        mahjongGame.setPlayerHand(2, mahjongGame.sortTiles(2));

        mahjongGame.exchangeTiles(3);
        mahjongGame.setPlayerHand(3, mahjongGame.sortTiles(3));

        mahjongGame.exchangeTiles(4);
        mahjongGame.setPlayerHand(4, mahjongGame.sortTiles(4));

        // Convert back to response
        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);

        // Save updated state to database
        gameDao.updateGame(gameId, updatedState);

        return updatedState;
    }

    /**
     * Gets the current state of a game
     * 
     * @param gameId the game ID
     * @return the current game state
     */
    public GameStateResponse getGameState(int gameId) {
        GameStateResponse gameState = gameDao.getGame(gameId);

        if (gameState == null) {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }

        return gameState;
    }

    /**
     * Draws a tile for the current player
     * 
     * @param gameId the game ID
     * @return updated game state
     */
    public GameStateResponse drawTile(int gameId) {
        // Get current game state from database
        GameStateResponse gameState = gameDao.getGame(gameId);

        if (gameState == null) {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }

        // Reconstruct MahjongGame from state
        MahjongGame mahjongGame = reconstructMahjongGame(gameState);

        // Perform game logic
        mahjongGame.drawTile(1);

        // Convert back to response
        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);

        // Save updated state to database
        gameDao.updateGame(gameId, updatedState);

        return updatedState;
    }

    /**
     * Discards a tile for the current player
     * 
     * @param gameId the game ID
     * @return updated game state
     */
    public GameStateResponse discardTile(int gameId, int playerId, DiscardTileRequest request) {
        // Get current game state from database
        GameStateResponse gameState = gameDao.getGame(gameId);

        if (gameState == null) {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }

        // Reconstruct MahjongGame from state
        MahjongGame mahjongGame = reconstructMahjongGame(gameState);

        // Perform game logic
        mahjongGame.discardTile(playerId, request.getType(), request.getNumber());

        // Convert back to response
        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);

        // Save updated state to database
        gameDao.updateGame(gameId, updatedState);

        return updatedState;
    }

    /**
     * Discards a tile for the current player
     * 
     * @param gameId the game ID
     * @return updated game state
     */
    public GameStateResponse handleComputerTurn(int gameId, int playerId) {
        // Get current game state from database
        GameStateResponse gameState = gameDao.getGame(gameId);

        if (gameState == null) {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }

        // Reconstruct MahjongGame from state
        MahjongGame mahjongGame = reconstructMahjongGame(gameState);

        // Perform game logic
        mahjongGame.handleComputerTurn(playerId);

        // Convert back to response
        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);

        // Save updated state to database
        gameDao.updateGame(gameId, updatedState);

        return updatedState;
    }

    public int createTestGame() {
        MahjongGame mahjongGame = new MahjongGame();
        mahjongGame.initializeTiles();
        mahjongGame.dealSameTilesToAllPlayers();
        System.out.println("Created a new test Mahjong game.");
        GameStateResponse gameState = convertToGameStateResponse(mahjongGame);
        gameState.setCurrentPlayerTurn(1);
        gameState.setWinnerId(-1);
        return gameDao.createGame(gameState);
    }

    /**
     * Checks whether player 1 has a winning bahay Mahjong hand (1 eye + 5 bahays).
     * Considers both the player's current hand and their declared finished sets.
     * If the hand is valid, the winner is recorded and the game state is persisted.
     */
    public GameStateResponse checkBahayMahjong(int gameId, int playerId) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        if (gameState == null) throw new RuntimeException("Game not found with ID: " + gameId);

        MahjongGame mahjongGame = reconstructMahjongGame(gameState);
        boolean isMahjong = mahjongGame.checkBahayMahjong(playerId);

        if (isMahjong) {
            mahjongGame.setWinnerId(playerId);
        }

        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
        updatedState.setValidMove(isMahjong);

        if (isMahjong) {
            gameDao.updateGame(gameId, updatedState);
        }

        return updatedState;
    }

    public GameStateResponse mahjong(int gameId, int playerId) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        if (gameState == null) throw new RuntimeException("Game not found with ID: " + gameId);
        MahjongGame mahjongGame = reconstructMahjongGame(gameState);
        boolean isValid = mahjongGame.checkMahjong(playerId);
        if (isValid) mahjongGame.setWinnerId(playerId);
        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
        updatedState.setValidMove(isValid);
        gameDao.updateGame(gameId, updatedState);
        return updatedState;
    }

    public GameStateResponse pong(int gameId, PongRequest request) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        if (gameState == null) throw new RuntimeException("Game not found with ID: " + gameId);

        MahjongGame mahjongGame = reconstructMahjongGame(gameState);
        int playerId = request.getPlayerId();

        if (!tilesExistInHand(mahjongGame, playerId, request.getTiles())) {
            GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
            updatedState.setValidMove(false);
            return updatedState;
        }

        boolean isValid = mahjongGame.checkPong(request.getSelectedTile(), request.getTiles(), playerId);
        if (isValid) mahjongGame.setCurrentPlayerTurn(playerId);

        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
        updatedState.setValidMove(isValid);
        gameDao.updateGame(gameId, updatedState);
        return updatedState;
    }

    public GameStateResponse kang(int gameId, KangRequest request) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        if (gameState == null) throw new RuntimeException("Game not found with ID: " + gameId);

        MahjongGame mahjongGame = reconstructMahjongGame(gameState);
        int playerId = request.getPlayerId();

        if (!tilesExistInHand(mahjongGame, playerId, request.getTiles())) {
            GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
            updatedState.setValidMove(false);
            return updatedState;
        }

        boolean isValid = mahjongGame.checkKang(request.getSelectedTile(), request.getTiles(), playerId);
        if (!isValid) {
            GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
            updatedState.setValidMove(false);
            return updatedState;
        }

        mahjongGame.setCurrentPlayerTurn(playerId);

        // Auto-draw a bonus tile after kang
        Tile drawn = mahjongGame.drawTileAndReturn(playerId);
        String message;
        if (drawn == null) {
            message = "Kang declared — no tiles left to draw.";
        } else if (drawn.isFlower()) {
            message = "Drew a flower (" + drawn.getType() + " " + drawn.getNumber() + ") after kang — exchanging automatically.";
            List<com.mahjong.model.Tile> hand = mahjongGame.getCurrentPlayerHand(playerId);
            while (hand.stream().anyMatch(com.mahjong.model.Tile::isFlower)) {
                mahjongGame.exchangeTiles(playerId);
            }
        } else {
            message = "Drew " + drawn.getType() + " " + drawn.getNumber() + " after kang.";
        }

        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
        updatedState.setValidMove(true);
        updatedState.setMessage(message);
        gameDao.updateGame(gameId, updatedState);
        return updatedState;
    }

    public GameStateResponse chow(int gameId, ChowRequest request) {
        GameStateResponse gameState = gameDao.getGame(gameId);
        if (gameState == null) throw new RuntimeException("Game not found with ID: " + gameId);

        MahjongGame mahjongGame = reconstructMahjongGame(gameState);
        int playerId = request.getPlayerId();

        // Chow can only be claimed by the player whose turn it is —
        // i.e. the player immediately after the one who just discarded.
        if (playerId != mahjongGame.getCurrentPlayerTurn()) {
            GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
            updatedState.setValidMove(false);
            return updatedState;
        }

        if (!tilesExistInHand(mahjongGame, playerId, request.getTiles())) {
            GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
            updatedState.setValidMove(false);
            return updatedState;
        }

        boolean isValid = mahjongGame.checkChow(request.getSelectedTile(), request.getTiles(), playerId);
        if (isValid) mahjongGame.setCurrentPlayerTurn(playerId);

        GameStateResponse updatedState = convertToGameStateResponse(mahjongGame);
        updatedState.setValidMove(isValid);
        gameDao.updateGame(gameId, updatedState);
        return updatedState;
    }

    private boolean tilesExistInHand(MahjongGame game, int playerId, List<Tile> tiles) {
        List<Tile> hand = game.getCurrentPlayerHand(playerId);
        for (Tile tile : tiles) {
            if (!hand.contains(tile)) return false;
        }
        return true;
    }

    /**
     * Reconstructs MahjongGame from GameStateResponse
     *
     */
    private MahjongGame reconstructMahjongGame(GameStateResponse state) {
        MahjongGame game = new MahjongGame();

        // Reconstruct the game from the saved state
        game.setCurrentPlayerTurn(state.getCurrentPlayerTurn());
        game.setWinnerId(state.getWinnerId());
        game.setDiscardedTiles(state.getDiscardedTiles());
        game.setTileStack(state.getTileStack());
        game.setMoves(new ArrayList<>(state.getMoves()));

        // Set player hands
        game.setPlayerHand(1, state.getCurrentPlayerHand());
        List<List<Tile>> opponentHands = state.getOpponentHands();
        for (int i = 0; i < opponentHands.size(); i++) {
            game.setPlayerHand(i + 2, opponentHands.get(i));
        }

        // Set finished hands
        game.setPlayerFinishedHand(1, state.getCurrentPlayerFinishedHand());

        return game;
    }

    /**
     * Converts MahjongGame object to GameStateResponse
     */
    private GameStateResponse convertToGameStateResponse(MahjongGame game) {
        GameStateResponse response = new GameStateResponse();

        response.setCurrentPlayerHand(game.getCurrentPlayerHand(1));

        List<List<Tile>> opponentHands = new ArrayList<>();
        opponentHands.add(game.getCurrentPlayerHand(2));
        opponentHands.add(game.getCurrentPlayerHand(3));
        opponentHands.add(game.getCurrentPlayerHand(4));
        response.setOpponentHands(opponentHands);

        response.setGamePhase(game.getGamePhase());
        response.setValidActions(game.getValidActions(1));

        response.setMoves(game.getMoves());
        response.setTileStack(game.getTileStack());
        response.setDiscardedTiles(game.getDiscardedTiles());
        response.setCurrentPlayerTurn(game.getCurrentPlayerTurn());
        response.setYourTurn(game.isPlayerTurn(1));
        response.setCanDiscard(game.canDiscard());
        response.setCanDraw(game.canDraw());
        response.setCurrentPlayerFinishedHand(game.getCurrentPlayerFinishedHand(1));
        response.setWinnerId(game.getWinnerId());

        return response;
    }

}
