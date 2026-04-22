package com.mahjong.controller;

import com.mahjong.service.GameService;
import com.mahjong.dto.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/api/hello")
    public Response<String> hello() {
        Response<String> response = new Response<>();
        response.setData("Hello, Mahjong!");
        return response;
    }


    @GetMapping("/api/game/new")
    public Response<GameStateResponse> newGame() {

        try {
            int gameId = gameService.createNewGame(); // Call the service to create a new game
            GameStateResponse gameState = gameService.getGameState(gameId);
            gameState.setGameId(gameId);
            return Response.success(gameState);
        } catch (Exception e) {
            return Response.error("Failed to create game: " + e.getMessage());
        }
    }

    @GetMapping("/api/game/getGameState")
    public Response<GameStateResponse> getGameState(@RequestParam("gameId") int gameId) {
        GameStateResponse gameState = gameService.getGameState(gameId);
        return Response.success(gameState);
    }

    @GetMapping("/api/game/newTestGame")
    public Response<GameStateResponse> newTestGame() {
        try {
            int gameId = gameService.createTestGame();
            GameStateResponse gameState = gameService.getGameState(gameId);
            gameState.setGameId(gameId);
            return Response.success(gameState);
        } catch (Exception e) {
            return Response.error("Failed to create test game: " + e.getMessage());
        }
    }

    @PostMapping("/api/game/exchangeFlowers")
    public Response<GameStateResponse> exchangeFlowers(@RequestParam("gameId") int gameId, Integer playerId) {

        GameStateResponse gameState = gameService.exchangeFlowers(gameId, playerId);
        gameState.setGameId(gameId);
        return Response.success(gameState);

    }

    @GetMapping("/api/game/sortTiles")
    public Response<GameStateResponse> sortTiles(@RequestParam("gameId") int gameId, Integer playerId) {
        GameStateResponse gameState = gameService.sortTiles(gameId, playerId);
        gameState.setGameId(gameId);
        return Response.success(gameState);
    }

    @GetMapping("/api/game/drawTile")
    public Response<GameStateResponse> drawTile(@RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId) {
        GameStateResponse gameState = gameService.drawTile(gameId);
        gameState.setGameId(gameId);
        return Response.success(gameState);

    }

    @PostMapping("/api/game/discardTile")
    public Response<GameStateResponse> discardTile(@RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId, @RequestBody DiscardTileRequest request) {
        System.out.println(request);

        GameStateResponse gameState = gameService.discardTile(gameId, playerId, request);
        gameState.setGameId(gameId);
        return Response.success(gameState);
    }

    @PostMapping("/api/game/chow")
    public Response<GameStateResponse> chow(
            @RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId,
            @RequestBody ChowRequest request) {
        request.setPlayerId(playerId);
        GameStateResponse gameState = gameService.chow(gameId, request);
        gameState.setGameId(gameId);
        return Response.success(gameState);
    }

    @PostMapping("/api/game/pong")
    public Response<GameStateResponse> pong(
            @RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId,
            @RequestBody PongRequest request) {
        request.setPlayerId(playerId);
        GameStateResponse gameState = gameService.pong(gameId, request);
        gameState.setGameId(gameId);
        return Response.success(gameState);
    }

    @PostMapping("/api/game/kang")
    public Response<GameStateResponse> kang(
            @RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId,
            @RequestBody KangRequest request) {
        request.setPlayerId(playerId);
        GameStateResponse gameState = gameService.kang(gameId, request);
        gameState.setGameId(gameId);
        return Response.success(gameState);
        }
    

    @GetMapping("/api/game/checkMahjong")
    public Response<GameStateResponse> checkMahjong(
            @RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId) {
        GameStateResponse gameState = gameService.checkBahayMahjong(gameId, playerId);
        gameState.setGameId(gameId);
        if (!gameState.isValidMove()) {
            Response<GameStateResponse> response = new Response<>();
            response.setData(gameState);
            response.addError("Not a winning hand: need 1 eye and 5 bahays (consecutive sequences of the same suit)");
            return response;
        }
        return Response.success(gameState);
    }

    @PostMapping("/api/game/mahjong")
    public Response<GameStateResponse> mahjong(
            @RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId) {
        GameStateResponse gameState = gameService.mahjong(gameId, playerId);
        gameState.setGameId(gameId);
        if (!gameState.isValidMove()) {
            Response<GameStateResponse> response = new Response<>();
            response.setData(gameState);
            response.addError("Invalid move: cannot declare Mahjong now");
            return response;
        }
        return Response.success(gameState);
    }

    @GetMapping("/api/game/computerTurn")
    public Response<GameStateResponse> computerTurn(@RequestParam("gameId") int gameId,
            @RequestParam("playerId") int playerId) {
        GameStateResponse gameState = gameService.handleComputerTurn(gameId, playerId);
        return Response.success(gameState);

    }

}
