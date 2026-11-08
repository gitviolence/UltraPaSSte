package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;

public class PostPlayerUpdateEvent extends EventBase {

    private int iterations;

    public int getIterations() {
        return this.iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
