package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.entity.projectile.FireworkRocketEntity;

public class FireworkEvent extends EventBase {

    private final FireworkRocketEntity firework;

    public FireworkRocketEntity getFirework() {
        return this.firework;
    }

    public FireworkEvent(FireworkRocketEntity firework) {
        this.firework = firework;
    }
}
