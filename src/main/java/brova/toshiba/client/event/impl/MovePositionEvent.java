package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.util.math.Vec3d;

public class MovePositionEvent extends EventBase {

    private Vec3d movePos;

    public Vec3d getMovePos() {
        return this.movePos;
    }

    public void setMovePos(Vec3d movePos) {
        this.movePos = movePos;
    }

    public MovePositionEvent() {
    }

    public MovePositionEvent(Vec3d movePos) {
        this.movePos = movePos;
    }
}
