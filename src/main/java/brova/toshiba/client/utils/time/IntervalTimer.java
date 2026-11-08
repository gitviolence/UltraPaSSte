package brova.toshiba.client.utils.time;

public final class IntervalTimer {

    private long millis;

    public IntervalTimer() {
        this.reset();
    }

    public boolean finished(float floatValue) {
        return (float) System.currentTimeMillis() - floatValue >= (float) this.millis;
    }

    public boolean finished(long longValue) {
        return System.currentTimeMillis() - this.millis >= longValue;
    }

    public void reset() {
        this.millis = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.millis;
    }

    public long getMillis() {
        return this.millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
