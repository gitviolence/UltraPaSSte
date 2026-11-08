package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.entity.player.PlayerEntity;

public class TotemPopEvent extends EventBase {

    private final PlayerEntity player;

    public TotemPopEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}
