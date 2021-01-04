package com.games.the_mind.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class CardDeck {
    final Integer TOTAL_CARDS = 100;
    final Integer MINIMUM_CARDS = 1;
    ArrayList<Integer> cards;
    Stack<Integer> deck;

    CardDeck() {
        this.cards = new ArrayList<Integer>();
        for (int i = MINIMUM_CARDS; i <= TOTAL_CARDS; i++) {
            this.cards.add(i);
        }
        initDeck();
    }

    private void initDeck() {
        Stack<Integer> cardStack = new Stack<Integer>();
        this.shuffle();
        cardStack.addAll(this.cards);
        this.deck = cardStack;
    }

    public Integer drawCard() {
        if (this.deck == null || this.deck.isEmpty()) {
            return -1;
        }
        return this.deck.pop();
    }

    public void shuffle() {
        Collections.shuffle(this.cards);
    }
}
