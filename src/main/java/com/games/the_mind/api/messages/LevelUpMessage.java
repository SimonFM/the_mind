package com.games.the_mind.api.messages;

public class LevelUpMessage extends RoomMessage {
    private final boolean levelUp;

    public LevelUpMessage(boolean levelUp) {
        this.levelUp = levelUp;
    }

    public boolean isLevelUp() {
        return levelUp;
    }
}
