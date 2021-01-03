package com.games.the_mind.model;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

public class Player implements Principal {
    private String id;
    private String username;
    private Set<Integer> hand;

    Player() {
        this.hand = new HashSet<>();
    }

    public Player(String id, String username) {
        this();
        this.id = id;
        this.username = username;
    }

    public void reset() {
        this.hand.clear();
    }

    @Override
    public String getName() {
        return id;
    }

    public void addToHand(Integer card) {
        this.hand.add(card);
    }

    public void removeFromHand(Integer card) {
        this.hand.remove(card);
    }

    public boolean hasLowerCard(Integer card) {
        for (Integer handCard : this.hand) {
            if (handCard < card) {
                return true;
            }
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
