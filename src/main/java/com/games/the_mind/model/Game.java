package com.games.the_mind.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Game {
    String name;
    String invite;
    String id;
    boolean started;
    LocalDateTime validUntil;
    Set<Player> players;

    public Game() { }

    public Game(String name, String invite, String id, LocalDateTime validUntil) {
        this();
        this.name = name;
        this.invite = invite;
        this.id = id;
        this.validUntil = validUntil;
        this.players = new HashSet<Player>();
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvite() {
        return invite;
    }

    public void setInvite(String invite) {
        this.invite = invite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }


    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }
}
