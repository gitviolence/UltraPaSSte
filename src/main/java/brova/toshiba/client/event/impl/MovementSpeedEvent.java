package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.util.math.Vec3d;

public class MovementSpeedEvent extends EventBase {

    private float speed;

    private Vec3d movementInput;

    public float getSpeed() {
        return this.speed;
    }

    public Vec3d getMovementInput() {
        return this.movementInput;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setMovementInput(Vec3d movementInput) {
        this.movementInput = movementInput;
    }

    public MovementSpeedEvent(float speed, Vec3d movementInput) {
        this.speed = speed;
        this.movementInput = movementInput;
    }
}
