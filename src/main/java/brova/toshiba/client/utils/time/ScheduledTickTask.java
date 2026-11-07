package brova.toshiba.client.utils.time;

public class ScheduledTickTask {

    public int cachedCount;

    public Runnable runnable;

    public ScheduledTickTask(int cachedCount, Runnable runnable) {
        this.cachedCount = cachedCount;
        this.runnable = runnable;
    }
}
