package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;

public class KeyBind extends EventBase {

    private final int key;

    private final InputDevice bindType;

    public boolean isKeyDown(int intValue) {
        return this.key == intValue;
    }

    public KeyBind(int key, InputDevice bindType) {
        this.key = key;
        this.bindType = bindType;
    }

    public int getKey() {
        return this.key;
    }

    public InputDevice getBindType() {
        return this.bindType;
    }
}
