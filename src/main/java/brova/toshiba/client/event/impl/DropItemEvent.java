package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;

public class DropItemEvent extends EventBase {

    private final int slot;

    public DropItemEvent(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }
}
