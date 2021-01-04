package com.games.the_mind.services;

import com.games.the_mind.api.RoomMessage;
import com.games.the_mind.model.Player;
import com.games.the_mind.model.TheMindGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class GameManagerService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final static String GAME_BASE_DESTINATION = "/topic/game";
    private final static String GAME_START_DESTINATION = GAME_BASE_DESTINATION + "/start";

    private final static String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final static Integer INVITE_CODE_SIZE = 6;

    Map<String, TheMindGame> activeGames;
    Map<String, Player> activePlayers;

    @Autowired
    public GameManagerService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.activeGames = new HashMap<>();
        this.activePlayers = new HashMap<>();
    }

    public Player connect(Principal principal) {
        Player user = new Player(principal.getName(), Integer.toString(activePlayers.size() + 1));
        activePlayers.put(principal.getName(), user);
        return user;
    }

    /***
     * Removes players from the active players list & also the games they are involved in.
     * TODO: Remove players from their games too.
     *
     * @param playerId - UUID of the player to remove
     */
    public void disconnect(String playerId) {
        activePlayers.remove(playerId);
    }

    /***
     * Creates a TheMindGame with the creator involved or will return the TheMindGame with that invite name.
     *
     * @param name - Name of the game
     * @param playerId - Creator of the game
     * @return TheMindGame
     */
    public TheMindGame create(String name, String playerId) {
        boolean hasName = name != null && !name.isBlank();
        String shortCode = hasName ? name : generateShortCode();
        TheMindGame theMind = getTheMindGameForByName(name);
        if (theMind == null) {
            Set<Player> players = new HashSet<>();
            if (this.activePlayers.containsKey(playerId)) {
                players.add(this.activePlayers.get(playerId));
            }
            theMind = new TheMindGame(players, shortCode);
            activeGames.put(shortCode, theMind);
        }
        return theMind;
    }

    /***
     * Starts the TheMindGame with a specific name & sends a message to the relevant Players.
     * @param name - Name of the game
     */
    public void startGame(String name) {
        TheMindGame game = getTheMindGameForByName(name);
        if (game == null || game.getPlayers().isEmpty()) {
            return;
        }
        game.start();
        sendGameMessage(game, getRoomDestination(game), game, "Game has started...");
    }

    /***
     * Starts the TheMindGame with a specific name & sends a message to the relevant Players.
     * @param name - Name of the game
     */
    public void playCard(String name, String playerId, Integer card) {
        TheMindGame game = getTheMindGameForByName(name);
        if (game == null || game.getPlayers().isEmpty() || !this.activePlayers.containsKey(playerId) || card <= 0) {
            return;
        }
        Player player = this.activePlayers.get(playerId);
        if (game.playCard(player, card)) {
            sendGameMessage(game, getRoomDestination(game), game, "Card Played by " + player.getUsername());
        } else {
            sendGameMessage(game, getRoomDestination(game), game, "Unable to play card " + player.getUsername());
        }
        if (game.isGameOver()) {
            sendGameMessage(game, getRoomDestination(game), game, "GAME OVER, you lost all your lives");
        } else if (game.roundFinished()) {
            sendGameMessage(game, getRoomDestination(game), game, "Round is finished " + player.getUsername());
        }
    }

    /***
     * Gets a TheMindGame for a specific invite/name code.
     * @param name - Name of the TheMindGame
     * @return TheMindGame
     */
    public TheMindGame getTheMindGameForByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return activeGames.computeIfAbsent(name, game -> null);
    }

    /**
     * Adds a given Player with their given Id to the list of active players for that game.
     *
     * @param invite - Name of the TheMindGame to join
     * @param playerId - PlayerId who wishes to join
     * @return TheMindGame
     */
    public TheMindGame join(String invite, String playerId) {
        TheMindGame game = getTheMindGameForByName(invite);
        Set<Player> users = game != null ? game.getPlayers() : null;
        if (users == null) {
            return null;
        }
        for (Player player : users) {
            if (player.getId().equalsIgnoreCase(playerId)) {
                return game;
            }
        }
        Player player = this.activePlayers.get(playerId);
        game.getPlayers().add(player);

        sendGameMessage(game, getRoomDestination(game), game, player.getId().concat(" has joined the room"));

        return game;
    }

    private String getRoomDestination(TheMindGame game) {
        return GAME_BASE_DESTINATION.concat("/").concat(game.getId());
    }

    /***
     * Generates a random Alpha String in uppercase with the specific length of INVITE_CODE_SIZE
     * @return String
     */
    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_SIZE);
        for (int i = 0; i < INVITE_CODE_SIZE; i++) {
            int index = (int)(ALPHA_CAPS.length() * Math.random());
            sb.append(ALPHA_CAPS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Sends a message to desired TheMindGame with a specific message. Default payload is applied (Empty)
     * @param game - Game to send it to
     * @param url - Type of message to send
     * @param message - Message to display
     */
    void sendGameMessage(TheMindGame game, String url, String message) {
        sendGameMessage(game, url, new HashMap<>(), message);
    }

    /**
     * Sends a message to desired TheMindGame with a specific message. Default payload needs to be specified.
     * @param game - Game to send it to
     * @param url - Type of message to send
     * @param payload - Object to send to the clients.
     * @param message - Message to display
     */
    void sendGameMessage(TheMindGame game, String url, Object payload, String message) {
        if (game == null) {
            return;
        }
        RoomMessage roomMessage = new RoomMessage();
        roomMessage.setRoomId(game.getId());
        roomMessage.setUserId("SYSTEM");
        roomMessage.setData(payload);
        roomMessage.setMessage(message);

        for (Player player : game.getPlayers()) {
            simpMessagingTemplate.convertAndSendToUser(player.getId(), url, roomMessage);
        }
    }
}
