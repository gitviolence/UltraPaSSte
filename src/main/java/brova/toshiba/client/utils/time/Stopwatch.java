package brova.toshiba.client.utils.time;

import brova.toshiba.client.core.MinecraftAccess;

public final class Stopwatch implements MinecraftAccess {

    private long startTime = System.currentTimeMillis();

    private long millis;

    public Stopwatch() {
        this.reset();
    }

    public static Stopwatch create() {
        return new Stopwatch();
    }

    public boolean finished(long longValue) {
        return System.currentTimeMillis() - longValue >= this.millis;
    }

    public void reset() {
        this.millis = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.millis;
    }

    public double deltaTime() {
        return mc.getCurrentFps() > 0 ? 1.0 / mc.getCurrentFps() : 1.0;
    }

    public boolean every(long longValue) {
        boolean flag = this.getMillis(System.nanoTime() - this.millis) >= longValue;
        if (flag) {
            this.reset();
        }
        return flag;
    }

    public boolean passed(long longValue) {
        return System.currentTimeMillis() - this.startTime > longValue;
    }

    public long getMillis(long longValue) {
        return longValue / 1000000L;
    }

    public long getTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    public void setTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getMillis() {
        return this.millis;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
