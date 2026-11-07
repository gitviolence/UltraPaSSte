package brova.toshiba.client.event;

import java.lang.reflect.InvocationTargetException;

public class EventBase {

    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public void call() {
        try {
            EventBus.invoke(this);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException failure) {
            throw new RuntimeException("Failed to Invoke Method", failure);
        }
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
