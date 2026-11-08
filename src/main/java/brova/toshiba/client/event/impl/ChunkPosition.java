package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;

public class ChunkPosition extends EventBase {

    private final int chunkX;

    private final int chunkZ;

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public ChunkPosition(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}
