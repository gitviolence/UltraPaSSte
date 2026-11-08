package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.util.math.Vec3d;

public class VelocityEvent extends EventBase {

    private Vec3d velocity;

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }

    public VelocityEvent(Vec3d velocity) {
        this.velocity = velocity;
    }
}
