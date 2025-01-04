package org.bukkit.event.player;

import org.bukkit.entity.Player;

public class PlayerPlayReadyEvent extends PlayerEvent {

    public PlayerPlayReadyEvent(Player who) {
        super(Type.PLAYER_PLAY_READY, who);
    }
}