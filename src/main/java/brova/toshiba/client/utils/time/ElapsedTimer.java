package brova.toshiba.client.utils.time;

public class ElapsedTimer {

    public long lastMS = System.currentTimeMillis();

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }

    public boolean isReached(long longValue) {
        return System.currentTimeMillis() - this.lastMS > longValue;
    }

    public void setLastMS(long lastMS) {
        this.lastMS = System.currentTimeMillis() + lastMS;
    }

    public void setTime(long lastMS) {
        this.lastMS = lastMS;
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public boolean isRunning() {
        return System.currentTimeMillis() - this.lastMS <= 0L;
    }

    public boolean hasTimeElapsed() {
        return this.lastMS < System.currentTimeMillis();
    }

    public long getLastMS() {
        return this.lastMS;
    }
}
