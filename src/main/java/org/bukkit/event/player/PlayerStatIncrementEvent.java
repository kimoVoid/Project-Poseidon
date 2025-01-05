package org.bukkit.event.player;

import org.bukkit.entity.Player;

public class PlayerStatIncrementEvent extends PlayerEvent {

    private final int id;
    private final int value;

    public PlayerStatIncrementEvent(Player who, int id, int value) {
        super(Type.PLAYER_STAT_INCREMENT, who);
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return this.id;
    }

    public int getValue() {
        return this.value;
    }
}