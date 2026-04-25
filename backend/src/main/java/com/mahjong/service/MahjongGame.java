package com.mahjong.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mahjong.model.Tile;
import com.mahjong.model.Player;
import com.mahjong.util.GamePhase;
import com.mahjong.util.PlayerAction;
import com.mahjong.util.TileType;

public class MahjongGame {

    private static final int NUM_OF_SET = 4; // Four sets of each tile
    private List<Tile> tileStack = new ArrayList<>();
    private List<Tile> discardPile = new ArrayList<>();
    private Map<Integer, Player> playerMap = new HashMap<>();

    private int numPlayers = 4;
    private int currentPlayerTurn = 1;
    private int winnerId = -1; // -1 means no winner yet

    private ArrayList<String> moves = new ArrayList<String>();

    private GamePhase gamePhase = null;

    private boolean canDraw;
    private boolean canDiscard;

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public List<PlayerAction> getValidActions(int playerId) {
        List<PlayerAction> validActions = new ArrayList<>();
        if (getGamePhase() == GamePhase.AWAITING_DISCARD && getCurrentPlayerTurn() == playerId) {
            Collections.addAll(validActions,PlayerAction.DISCARD_TILE, PlayerAction.DECLARE_MAHJONG);
        }

        if (getGamePhase() == GamePhase.AWAITING_CLAIM && getCurrentPlayerTurn() == playerId) {
            Collections.addAll(validActions,PlayerAction.PONG, PlayerAction.KANG, PlayerAction.DRAW_TILE, PlayerAction.CHOW);
        }

         if (getGamePhase() == GamePhase.POST_KANG_DRAW && getCurrentPlayerTurn() == playerId) {
            Collections.addAll(validActions,PlayerAction.DISCARD_TILE, PlayerAction.DECLARE_MAHJONG);
        }

        return validActions;
    }

    public boolean isPlayerTurn(int playerId) {
        return currentPlayerTurn == playerId;
    }

    public void advanceToNextTurn() {
        currentPlayerTurn = (currentPlayerTurn % numPlayers) + 1;
        System.out.println("Advanced to player " + currentPlayerTurn + "'s turn");
        setCurrentPlayerTurn(currentPlayerTurn);
    }

    public List<Tile> getDiscardedTiles() {
        return discardPile;
    }

    public void setDiscardedTiles(List<Tile> discardedTiles) {
        this.discardPile = discardedTiles;
    }

    public void setTileStack(List<Tile> tileStack) {
        this.tileStack = tileStack;
    }

    public List<Tile> getTileStack() {
        return tileStack;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<String> moves) {
        this.moves = moves;
    }

    public boolean canDraw() {
        return canDraw;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public boolean canDiscard() {
        return canDiscard;
    }

    public void setCanDiscard(boolean canDiscard) {
        this.canDiscard = canDiscard;
    }

    public MahjongGame() {
        // Initialize players
        for (int i = 0; i < numPlayers; i++) {
            int id = i + 1;
            playerMap.put(id, new Player(id));
        }
    }

    // Initialize tiles
    public void initializeTiles() {
        for (TileType type : TileType.values()) {
            switch (type) {
                case BALLS, CHARS, STICKS -> {
                    // 9 values × 4 copies = 36 each
                    for (int i = 1; i <= 9; i++)
                        for (int j = 0; j < NUM_OF_SET; j++)
                            tileStack.add(new Tile(type, i, false));
                }
                case WINDS -> {
                    // 4 winds (East=1, South=2, West=3, North=4) × 4 copies = 16
                    // Treated as flowers in Filipino Mahjong — exchanged when drawn
                    for (int i = 1; i <= 4; i++)
                        for (int j = 0; j < NUM_OF_SET; j++)
                            tileStack.add(new Tile(type, i, true));
                }
                case DRAGONS -> {
                    // 3 dragons (Red=1, Green=2, White=3) × 4 copies = 12
                    // Treated as flowers in Filipino Mahjong — exchanged when drawn
                    for (int i = 1; i <= 3; i++)
                        for (int j = 0; j < NUM_OF_SET; j++)
                            tileStack.add(new Tile(type, i, true));
                }
                case FLOWERS -> {
                    // 8 unique flower/season tiles × 1 copy each = 8
                    for (int i = 1; i <= 8; i++)
                        tileStack.add(new Tile(type, i, true));
                }
            }
        }
        Collections.shuffle(tileStack);
    }

    public void dealTiles() {
        MahjongGame game = this;
        // Deal 16 tiles to each player
        for (int round = 0; round < 16; round++) {
            for (Player player : playerMap.values()) {
                // Draw from the top
                Tile tile = tileStack.remove(0);
                player.addTile(tile);

                // Always make player 1 the mano (starts with 17 tiles)
                if (getCurrentPlayerHand(1).size() == 16) {
                    Tile manoTile = tileStack.remove(0);
                    player.addTile(manoTile);
                }
            }
        }

        // Print players' hands
        for (Player player : playerMap.values()) {
            System.out.println(player + " hand: " + player.getHand());
            System.out.println("Player has " + player.getHand().size() + " tiles.");
        }

        // Set the tile stack
        System.out.println("Tiles remaining in stack: " + tileStack.size());
        game.setTileStack(tileStack);
        game.setGamePhase(GamePhase.AWAITING_DISCARD);
    }

    /**
     * Deals the same predefined set of tiles to all 4 players for
     * development/testing purposes.
     * This ensures consistent game states for debugging and testing.
     */
    public void dealSameTilesToAllPlayers() {
        // Clear all players' hands first
        for (Player player : playerMap.values()) {
            player.getHand().clear();
        }

        // Define a predefined set of 16 tiles for development
        List<Tile> developmentHand = createDevelopmentHand();

        // Deal the same tiles to each player
        for (Player player : playerMap.values()) {
            for (Tile tile : developmentHand) {
                // Create a copy of the tile for each player
                Tile tileCopy = new Tile(tile.getType(), tile.getNumber(), tile.isFlower());
                player.addTile(tileCopy);
            }
        }

        // Print players' hands
        for (Player player : playerMap.values()) {
            System.out.println(player + " hand: " + player.getHand());
            System.out.println("Player has " + player.getHand().size() + " tiles.");
        }
    }

    /**
     * Creates a predefined hand for development purposes.
     * This hand includes a mix of tiles that could potentially form winning
     * combinations.
     */
    private List<Tile> createDevelopmentHand() {
        List<Tile> hand = new ArrayList<>();

        // Add some sequential tiles for potential chows
        hand.add(new Tile(TileType.BALLS, 1, false));
        hand.add(new Tile(TileType.BALLS, 2, false));
        hand.add(new Tile(TileType.BALLS, 3, false));

        hand.add(new Tile(TileType.CHARS, 4, false));
        hand.add(new Tile(TileType.CHARS, 5, false));
        hand.add(new Tile(TileType.CHARS, 6, false));

        // Add some matching tiles for potential pongs
        hand.add(new Tile(TileType.STICKS, 7, false));
        hand.add(new Tile(TileType.STICKS, 7, false));
        hand.add(new Tile(TileType.STICKS, 7, false));

        hand.add(new Tile(TileType.BALLS, 9, false));
        hand.add(new Tile(TileType.BALLS, 9, false));

        // Add some random tiles
        hand.add(new Tile(TileType.CHARS, 1, false));
        hand.add(new Tile(TileType.CHARS, 8, false));
        hand.add(new Tile(TileType.STICKS, 2, false));
        hand.add(new Tile(TileType.STICKS, 5, false));

        // Add a flower tile
        hand.add(new Tile(TileType.FLOWERS, 1, true));

        return hand;
    }

    public void setPlayerFinishedHand(int playerId, List<List<Tile>> finishedHand) {
        Player player = playerMap.get(playerId);
        if (player != null) {
            player.setFinishedHand(finishedHand);
        }
    }

    public void setPlayerHand(int playerId, List<Tile> hand) {
        Player player = playerMap.get(playerId);
        if (player != null) {
            player.setHand(hand);
        }
    }

    public List<Tile> getCurrentPlayerHand(int playerId) {
        Player player = playerMap.get(playerId);
        if (player != null) {
            System.out.println("Current Player " + playerId + " hand: " + player.getHand());
            return player.getHand();
        }

        return new ArrayList<>();
    }

    public List<List<Tile>> getCurrentPlayerFinishedHand(int playerId) {
        Player player = playerMap.get(playerId);
        if (player != null) {
            System.out.println("Current Player " + playerId + " finished hand: " + player.getFinishedHand());
            return player.getFinishedHand();
        }
        return new ArrayList<>();
    }

    public void exchangeSingleFlower(int playerId, Tile flowerTile) {
        Player player = playerMap.get(playerId);
        player.removeTile(flowerTile);
        System.out.println("Exchanging " + flowerTile + " from " + playerId + " to " + player.getHand());
    }

    public List<Tile> exchangeTiles(int playerId) {
        int flowerCount = 0;
        Player player = playerMap.get(playerId);

        if (player != null) {
            List<Tile> hand = player.getHand();
            List<Tile> flowersToRemove = new ArrayList<>();

            // Count how many flowers in your hand
            for (Tile tile : hand) {
                if (tile.isFlower()) {
                    flowersToRemove.add(tile);
                }
            }

            for (Tile flower : flowersToRemove) {
                player.removeTile(flower);
                if (!tileStack.isEmpty()) {
                    Tile newTile = tileStack.remove(0);
                    player.addTile(newTile);
                    System.out.println(
                            "Player " + playerId + " exchanged flower tile " + flower + " for new tile "
                                    + newTile);
                } else {
                    System.out.println("No more tiles in the stack to exchange.");
                }
            }
            System.out.println("Player " + playerId + " has " + flowerCount + " flowers in hand.");
            return hand;
        }

        return List.of();

    }

    public List<Tile> sortTiles(int playerId) {
        Player player = playerMap.get(playerId);
        if (player != null) {
            List<Tile> hand = player.getHand();

            List<Tile> sortedHand = new ArrayList<>(hand);
            Collections.sort(sortedHand, Comparator.comparing(Tile::getType).thenComparingInt(Tile::getNumber));
            System.out.println("Player " + playerId + " sorted hand: " + sortedHand);
            player.setHand(sortedHand);
            return sortedHand;
        }
        return new ArrayList<>(); // Empty list if player not found
    }

    public void drawTile(int playerId) {
        Player player = playerMap.get(playerId);
        if (player != null && !tileStack.isEmpty()) {
            Tile drawnTile = tileStack.remove(0);
            player.addTile(drawnTile);
            System.out.println("Player " + playerId + " drew tile: " + drawnTile);
            setGamePhase(GamePhase.AWAITING_DISCARD);
        } else {
            System.out.println("No tiles left to draw or player not found.");
        }
    }

    public boolean checkChow(Tile selectedTile, List<Tile> chowTiles, int playerId) {
        if (chowTiles == null || chowTiles.size() != 2) {
            System.out.println("Chow requires exactly 2 tiles from hand.");
            return false;
        }

        Tile h1 = chowTiles.get(0);
        Tile h2 = chowTiles.get(1);

        // All 3 tiles must be suited (BALLS, CHARS, STICKS) — no honor or flower tiles
        if (!isSuited(selectedTile) || !isSuited(h1) || !isSuited(h2)) {
            System.out.println("Chow: only suited tiles (balls/chars/sticks) can form a sequence.");
            return false;
        }
        if (selectedTile.getType() != h1.getType() || selectedTile.getType() != h2.getType()) {
            System.out.println("Chow: tiles are not all the same suit.");
            return false;
        }

        // The 3 numbers must form a consecutive run
        int[] nums = { selectedTile.getNumber(), h1.getNumber(), h2.getNumber() };
        Arrays.sort(nums);
        if (nums[1] != nums[0] + 1 || nums[2] != nums[0] + 2) {
            System.out.println("No Chow found for tile: " + selectedTile);
            return false;
        }

        // Valid chow — remove the two hand tiles and build the declared set
        Player player = playerMap.get(playerId);
        List<Tile> playerHand = player.getHand();
        playerHand.remove(h1);
        playerHand.remove(h2);

        List<Tile> chowSet = new ArrayList<>(List.of(selectedTile, h1, h2));
        player.getFinishedHand().add(chowSet);

        discardPile.remove(selectedTile);

        System.out.println("Chow: " + selectedTile + " + " + h1 + " + " + h2);
        return true;
    }

    /**
     * Checks if a player has a winning Mahjong hand:
     * exactly 1 eye (pair of identical tiles) and 5 bahays
     * (sequences of 3 consecutive tiles of the same suit),
     * combining their declared finished sets and current hand.
     *
     * Rules:
     *  - Every set already declared in the finished hand must itself be a valid bahay.
     *  - The remaining tiles in the current hand must form (5 - finishedBahays) bahays + 1 eye.
     */
    public boolean checkBahayMahjong(int playerId) {
        Player player = playerMap.get(playerId);
        if (player == null) return false;

        List<List<Tile>> finishedSets = player.getFinishedHand();
        List<Tile> hand = new ArrayList<>(player.getHand());

        // Every declared set must be a valid bahay; count how many we already have
        int finishedBahays = 0;
        for (List<Tile> set : finishedSets) {
            if (!isValidBahay(set)) {
                System.out.println("Declared set is not a bahay (consecutive sequence): " + set);
                return false;
            }
            finishedBahays++;
        }

        if (finishedBahays > 5) {
            System.out.println("Player " + playerId + " has more than 5 finished bahays — invalid.");
            return false;
        }

        int remainingBahays = 5 - finishedBahays;
        // Current hand must have exactly remainingBahays*3 + 2 tiles (the 2 being the eye)
        int expectedSize = remainingBahays * 3 + 2;
        if (hand.size() != expectedSize) {
            System.out.println("Hand size " + hand.size() + " does not match expected " + expectedSize
                    + " for " + remainingBahays + " remaining bahays + 1 eye.");
            return false;
        }

        // Sort for deterministic greedy processing
        hand.sort(Comparator.comparing(Tile::getType).thenComparingInt(Tile::getNumber));

        // Try each adjacent pair as the eye
        for (int i = 0; i < hand.size() - 1; i++) {
            Tile t1 = hand.get(i);
            Tile t2 = hand.get(i + 1);
            if (t1.equals(t2)) {
                List<Tile> remaining = new ArrayList<>(hand);
                remaining.remove(i + 1);
                remaining.remove(i);
                if (canFormBahays(remaining, remainingBahays)) {
                    System.out.println("Player " + playerId + " has Mahjong! Eye: " + t1
                            + ", finished bahays: " + finishedBahays + ", hand bahays: " + remainingBahays);
                    return true;
                }
                // Skip duplicate tiles to avoid redundant checks
                while (i + 1 < hand.size() - 1 && hand.get(i + 1).equals(t1)) i++;
            }
        }

        System.out.println("Player " + playerId + " does not have a winning hand.");
        return false;
    }

    /**
     * Returns true if a set of tiles is a valid bahay:
     * exactly 3 tiles of the same suit with consecutive numbers.
     */
    private boolean isValidBahay(List<Tile> set) {
        if (set.size() != 3) return false;
        List<Tile> sorted = new ArrayList<>(set);
        sorted.sort(Comparator.comparing(Tile::getType).thenComparingInt(Tile::getNumber));
        Tile a = sorted.get(0), b = sorted.get(1), c = sorted.get(2);
        return a.getType() == b.getType()
                && b.getType() == c.getType()
                && b.getNumber() == a.getNumber() + 1
                && c.getNumber() == a.getNumber() + 2;
    }

    /**
     * Greedily checks whether the given (sorted) tile list can form exactly {@code count} bahays.
     * The smallest tile must always start a sequence; if it cannot, the configuration is impossible.
     */
    private boolean canFormBahays(List<Tile> tiles, int count) {
        if (count == 0) return tiles.isEmpty();
        if (tiles.size() < 3) return false;

        Tile first = tiles.get(0);
        List<Tile> remaining = new ArrayList<>(tiles);
        remaining.remove(0);

        int idx2 = indexOfTile(remaining, first.getType(), first.getNumber() + 1);
        if (idx2 < 0) return false;
        remaining.remove(idx2);

        int idx3 = indexOfTile(remaining, first.getType(), first.getNumber() + 2);
        if (idx3 < 0) return false;
        remaining.remove(idx3);

        return canFormBahays(remaining, count - 1);
    }

    /**
     * Returns the index of the first tile in the list matching the given type and number,
     * or -1 if not found.
     */
    private int indexOfTile(List<Tile> tiles, TileType type, int number) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t.getType() == type && t.getNumber() == number) return i;
        }
        return -1;
    }

    public boolean checkMahjong(int playerId) {
        // get the player's finished hand
        Player player = playerMap.get(playerId);
        List<List<Tile>> finishedHand = player.getFinishedHand();
        // get the player's hand
        List<Tile> hand = player.getHand();

        int pairs = 0;
        int triplets = 0;
        int quads = 0;

        for (int i = 0; i < hand.size(); i++) {
            Tile currentTile = hand.get(i);
            Tile nextTile = hand.get(i + 1);
            if (currentTile == nextTile) {
                pairs++;
            } else if (currentTile.getType() == nextTile.getType()
                    && currentTile.getNumber() + 1 == nextTile.getNumber()) {
                // Check for triplet
                if (i + 2 < hand.size() && hand.get(i + 2).getType() == currentTile.getType()
                        && hand.get(i + 2).getNumber() - currentTile.getNumber() == 2) {
                    triplets++;
                    i += 2; // Skip the next two tiles as they are part of the triplet
                }
            }
        }

        // count each List<Tile> in finishedHand
        // if the List<Tile> has 3 tiles, increment triplets
        for (List<Tile> set : finishedHand) {
            if (set.size() == 2) {
                pairs++;
            } else if (set.size() == 3) {
                triplets++;
            } else if (set.size() == 4) {
                quads++;
            }
        }
        if ((pairs * 2) + (triplets * 3) + (quads * 4) == 17) {
            System.out.println("Player " + playerId + " has Mahjong!");
            return true;
        } else {
            System.out.println("Player " + playerId + " does not have Mahjong.");
            return false;
        }
    }

    public boolean checkPong(Tile selectedTile, List<Tile> pongTiles, int playerId) {
        TileType type = selectedTile.getType();
        int number = selectedTile.getNumber();
        Player player = playerMap.get(playerId);
        List<Tile> playerHand = player.getHand();

        List<Tile> tilesToPong = new ArrayList<>(pongTiles);

        // If the tilesToPong are not in the player's hand, return false
        for (Tile tile : tilesToPong) {
            if (!playerHand.contains(tile)) {
                System.out.println("Tile " + tile + " not found in player's hand.");
                return false;
            }
        }

        // If the tilesToPong are not equal to one another, return false
        boolean tilesToPongAreTheSame = tilesToPong.getFirst().getType() == tilesToPong.getLast().getType()
                && tilesToPong.getFirst().getNumber() == tilesToPong.getLast().getNumber();
        if (!tilesToPongAreTheSame) {
            System.out.println("Tiles to Pong are not the same: " + tilesToPong);
            return false;
        }

        // if the selectedTile's type and number match tilesToPong, then return true
        Tile tileToMatch = tilesToPong.getFirst();
        if (tileToMatch.getType() == type && tileToMatch.getNumber() == number) {
            System.out.println("Pong found with tiles: " + selectedTile + ", " + tileToMatch);

            // Remove the pongTiles from player's hand
            for (Tile t : tilesToPong) {
                playerHand.remove(t);
            }
            tilesToPong.add(selectedTile);

            // Add to finished hand
            List<List<Tile>> finishedHand = player.getFinishedHand();
            finishedHand.add(new ArrayList<>(tilesToPong));
            player.setFinishedHand(finishedHand);

            // Remove selectedTile from discard pile if present
            discardPile.remove(selectedTile);

            return true;
        }

        return false;
    }

    public boolean checkKang(Tile selectedTile, List<Tile> kangTiles, int playerId) {
        if (kangTiles == null || kangTiles.size() != 3) {
            System.out.println("Kang requires exactly 3 tiles from hand.");
            return false;
        }

        TileType type = selectedTile.getType();
        int number = selectedTile.getNumber();
        Player player = playerMap.get(playerId);
        if (player == null) {
            System.out.println("Player " + playerId + " not found.");
            return false;
        }

        List<Tile> playerHand = player.getHand();

        // All 3 hand tiles must be in the player's hand
        for (Tile tile : kangTiles) {
            if (!playerHand.contains(tile)) {
                System.out.println("Tile " + tile + " not found in player's hand.");
                return false;
            }
        }

        // All 3 hand tiles must match the selected tile
        for (Tile tile : kangTiles) {
            if (tile.getType() != type || tile.getNumber() != number) {
                System.out.println("Kang tiles do not all match selected tile " + selectedTile);
                return false;
            }
        }

        // Remove the 3 hand tiles
        for (Tile t : kangTiles) {
            playerHand.remove(t);
        }

        // Build the 4-tile kang set and add to finished hand
        List<Tile> kangSet = new ArrayList<>(kangTiles);
        kangSet.add(selectedTile);
        player.getFinishedHand().add(kangSet);

        // Remove selectedTile from discard pile if present
        discardPile.remove(selectedTile);

        System.out.println("Kang declared by player " + playerId + ": " + kangSet);
        return true;
    }

    public Tile drawTileAndReturn(int playerId) {
        Player player = playerMap.get(playerId);
        if (player == null || tileStack.isEmpty()) return null;
        Tile drawn = tileStack.remove(0);
        player.addTile(drawn);
        System.out.println("Player " + playerId + " drew tile after kang: " + drawn);
        setGamePhase(GamePhase.AWAITING_DISCARD);
        return drawn;
    }

    public void discardTile(int playerId, TileType type, int number) {

        if (!isPlayerTurn(playerId)) {
            throw new IllegalStateException("Not player " + playerId + "'s turn");
        }

        Player player = playerMap.get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }

        List<Tile> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            Tile tile = hand.get(i);
            if (tile.getType() == type && tile.getNumber() == number) {
                // Remove the tile from player's hand
                Tile discardedTile = hand.remove(i);
                System.out.println("Player " + playerId + " discarded: " + discardedTile);
                moves.add("Player " + playerId + " discarded: " + discardedTile);

                // Add the discarded tile to the discard pile
                discardPile.add(discardedTile);
                System.out.println("Discard pile now contains: " + discardPile.size() + " tiles");
                System.out.println("Discard pile: " + discardPile);

                setCanDiscard(false);
                setCanDraw(true);
                setGamePhase(GamePhase.AWAITING_CLAIM);

                advanceToNextTurn();
                return;
            }
        }
        System.out.println("Tile not found in player's hand");

    }

    public void handleComputerTurn(int playerId) {
        if (currentPlayerTurn != playerId) {
            throw new IllegalArgumentException("It is " + currentPlayerTurn + "'s turn, not " + playerId);
        }

        if (currentPlayerTurn == 1) {
            throw new IllegalArgumentException("It is Player 1 (main player)'s turn.");
        }

        System.out.println("Computer player " + playerId + " taking turn");

        // 1. Draw a tile
        drawTile(currentPlayerTurn);

        // 2. Exchange all flowers — loop in case a replacement is also a flower
        List<Tile> hand = getCurrentPlayerHand(playerId);
        while (hand.stream().anyMatch(Tile::isFlower)) {
            exchangeTiles(currentPlayerTurn);
        }

        // 3. Check win condition — declare mahjong if possible
        if (checkBahayMahjong(playerId)) {
            winnerId = playerId;
            System.out.println("Computer player " + playerId + " wins!");
            return;
        }

        // 4. Smart discard: keep tiles that contribute to sequences
        hand = getCurrentPlayerHand(currentPlayerTurn);
        if (!hand.isEmpty()) {
            Tile tileToDiscard = chooseTileToDiscard(hand);
            discardTile(currentPlayerTurn, tileToDiscard.getType(), tileToDiscard.getNumber());
        }
    }

    private Tile chooseTileToDiscard(List<Tile> hand) {
        Tile worst = null;
        int lowestScore = Integer.MAX_VALUE;

        for (Tile tile : hand) {
            if (tile.isFlower()) continue;
            int score = scoreTileForKeeping(tile, hand);
            if (score < lowestScore) {
                lowestScore = score;
                worst = tile;
            }
        }
        return worst != null ? worst : hand.get(hand.size() - 1);
    }

    private int scoreTileForKeeping(Tile tile, List<Tile> hand) {
        if (tile.isFlower()) return -100;

        int score = 0;
        int v = tile.getNumber();
        TileType t = tile.getType();

        if (isSuited(tile)) {
            // Complete sequences this tile participates in
            if (countInHand(hand, v - 2, t) >= 1 && countInHand(hand, v - 1, t) >= 1) score += 6;
            if (countInHand(hand, v - 1, t) >= 1 && countInHand(hand, v + 1, t) >= 1) score += 6;
            if (countInHand(hand, v + 1, t) >= 1 && countInHand(hand, v + 2, t) >= 1) score += 6;

            // Partial sequences (two adjacent tiles toward a bahay)
            if (countInHand(hand, v + 1, t) >= 1 || countInHand(hand, v - 1, t) >= 1) score += 2;
            if (countInHand(hand, v + 2, t) >= 1 || countInHand(hand, v - 2, t) >= 1) score += 1;

            // Edge tile penalty — 1s and 9s can only extend in one direction
            if (v == 1 || v == 9) score -= 3;
        }

        // Pair/triple bonus applies to all types (useful as the eye or pong)
        long countInHandLong = hand.stream().filter(t2 -> t2.equals(tile)).count();
        if (countInHandLong >= 2) score += 5;

        return score;
    }

    private int countInHand(List<Tile> hand, int number, TileType type) {
        int count = 0;
        for (Tile t : hand) {
            if (t.getNumber() == number && t.getType() == type) count++;
        }
        return count;
    }

    private boolean isSuited(Tile tile) {
        TileType t = tile.getType();
        return t == TileType.BALLS || t == TileType.CHARS || t == TileType.STICKS;
    }

    /**
     * Initializes the game for development mode where all players get the same
     * tiles.
     * This method sets up the tile stack and deals identical hands to all players.
     */
    public void initializeGameForDevelopment() {
        initializeTiles();
        System.out.println("=== Development Mode: Dealing same tiles to all players ===");
        dealSameTilesToAllPlayers();
    }

    public static void main(String[] args) {
        MahjongGame game = new MahjongGame();

        // Regular game mode with random tile distribution
        // game.initializeTiles();
        // System.out.println("Total tiles: " + game.tileStack.size());
        // game.dealTiles();

        // Development mode with same tiles for all players
        game.initializeGameForDevelopment();
    }
}
