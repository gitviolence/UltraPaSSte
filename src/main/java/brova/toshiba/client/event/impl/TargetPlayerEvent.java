package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetPlayerEvent extends EventBase {

    private final PlayerEntity player;

    private final Entity target;

    public TargetPlayerEvent(PlayerEntity player, Entity target) {
        this.player = player;
        this.target = target;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }

    public Entity getTarget() {
        return this.target;
    }
}
