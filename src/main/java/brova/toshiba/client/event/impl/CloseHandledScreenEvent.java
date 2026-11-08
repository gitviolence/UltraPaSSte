package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;

public class CloseHandledScreenEvent extends EventBase {

    public int windowId;

    public CloseHandledScreenEvent(int windowId) {
        this.windowId = windowId;
    }
}
