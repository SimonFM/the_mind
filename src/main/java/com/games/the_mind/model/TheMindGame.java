package com.games.the_mind.model;

import java.util.*;

public class TheMindGame {

    final static Integer LAST_LEVEL = 12;

    String id;
    String name;
    Set<Player> players;
    Player currentPlayer;
    CardDeck cardDeck;
    Stack<Integer> playedCards;
    boolean started;

    Integer currentLevel;
    Integer ninjaStars;
    Integer lives;

    private TheMindGame(String name) {
        this.players = new HashSet<>();
        this.cardDeck = new CardDeck();
        this.ninjaStars = 0;
        this.lives = 0;
        this.currentLevel = 1;
        this.name = name != null ? name : "";
        this.id = UUID.randomUUID().toString();
    }

    public TheMindGame(Set<Player> players, String name) {
        this(name);
        if (players == null || players.isEmpty() || name == null || name.isBlank()) {
            return;
        }
        this.players.addAll(players);
        this.init();
        Optional<Player> existingPlayer = this.players.stream().findFirst();
        existingPlayer.ifPresent(player -> this.currentPlayer = player);
    }

    public Set<Player> getPlayers() {
        if (this.players == null) {
            this.players = new HashSet<>();
        }
        return this.players;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public Integer getNinjaStars() {
        return ninjaStars;
    }

    public Integer getLives() {
        return lives;
    }

    public boolean isGameOver() {
        return this.lives <= 0;
    }

    private void init() {
        switch (this.players.size()) {
            case 2:
                this.setTwoPlayerRules();
                break;
            case 3:
                this.setThreePlayerRules();
                break;
            default:
                this.setDefaultPlayerRules();
        }
        this.playedCards = new Stack<>();
        this.cardDeck.shuffle();
    }

    public void start() {
        this.started = true;
        this.drawCards();
    }

    private void resetCards() {
        this.playedCards = new Stack<>();
        this.cardDeck.shuffle();
        this.drawCards();
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isNotStarted() {
        return !this.started;
    }

    private void drawCards() {
        if (this.isNotStarted()) {
            return;
        }
        for (int i = 0; i < this.currentLevel; i++) {
            for (Player player : this.players) {
                Integer drawnCard = this.cardDeck.drawCard();
                player.addToHand(drawnCard);
            }
        }
    }

    public boolean isFinished() {
        return currentLevel >= LAST_LEVEL;
    }

    private void setTwoPlayerRules() {
        this.lives = 2;
        this.ninjaStars = 1;
    }

    private void setThreePlayerRules() {
        this.lives = 2;
        this.ninjaStars = 1;
    }

    private void setDefaultPlayerRules() {
        this.lives = 2;
        this.ninjaStars = 1;
    }

    public boolean levelUp() {
        if (this.isNotStarted()) {
            return false;
        }
        this.currentLevel++;
        return this.isFinished();
    }

    public boolean playCard(Player player, Integer card) {
        if (this.isNotStarted()) {
            return false;
        }
        boolean noLowerCardsExist = this.isValidCard(card) == null;
        if (noLowerCardsExist) {
            player.removeFromHand(card);
            this.playedCards.push(card);
        }
        if (!noLowerCardsExist) {
            this.removeLife();
        }
        return noLowerCardsExist;
    }

    private void removeLife() {
        this.lives--;
    }

    public boolean roundFinished() {
        for (Player player : this.players) {
            if (!player.isHandEmpty()) {
                return false;
            }
        }
        this.levelUp();
        this.resetCards();
        return true;
    }

    private Player isValidCard(Integer card) {
        for (Player player : this.players) {
            if (player.hasLowerCard(card)) {
                return player;
            }
        }
        return null;
    }
}
