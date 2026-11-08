package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.util.math.BlockPos;

public class BlockShapeEvent extends EventBase {

    private final BlockPos pos;

    public BlockShapeEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
