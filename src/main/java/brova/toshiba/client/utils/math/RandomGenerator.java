package brova.toshiba.client.utils.math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomGenerator extends Random {

    private static final long serialVersionUID = -1034002978452164795L;

    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    private Random random = null;

    private volatile boolean stateFlagForNextInt;

    private volatile boolean stateFlagForUpdateSeed;

    private volatile long seedTimestamp;

    private void updateSeed() {
        if (this.random == null) {
            Random random = new Random(this.seedTimestamp);
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, random, (accessReceiver, accessValue) -> {
                accessReceiver.random = accessValue;
            });
            this.stateFlagForUpdateSeed = false;
        } else if (this.stateFlagForUpdateSeed) {
            this.random.setSeed(this.seedTimestamp);
            this.stateFlagForUpdateSeed = false;
        }
    }

    public static long mix(long longValue, long longValueValue) {
        longValue *= longValue * 6364136223846793005L + 1442695040888963407L;
        return longValue + longValueValue;
    }

    @Override
    public void setSeed(long seedTimestamp) {
        this.seedTimestamp = seedTimestamp;
        this.stateFlagForNextInt = true;
        this.stateFlagForUpdateSeed = true;
    }

    @Override
    public void nextBytes(byte[] values) {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            this.random.nextBytes(values);
        } else {
            this.threadLocalRandom.nextBytes(values);
        }
    }

    @Override
    public int nextInt() {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            return this.random.nextInt();
        } else {
            return this.threadLocalRandom.nextInt();
        }
    }

    @Override
    public int nextInt(int intValue) {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            return this.random.nextInt(intValue);
        } else {
            return this.threadLocalRandom.nextInt(intValue);
        }
    }

    @Override
    public long nextLong() {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            return this.random.nextLong();
        } else {
            return this.threadLocalRandom.nextLong();
        }
    }

    @Override
    public boolean nextBoolean() {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            return this.random.nextBoolean();
        } else {
            return this.threadLocalRandom.nextBoolean();
        }
    }

    @Override
    public float nextFloat() {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            return this.random.nextFloat();
        } else {
            return this.threadLocalRandom.nextFloat();
        }
    }

    @Override
    public double nextDouble() {
        if (this.stateFlagForNextInt) {
            this.updateSeed();
            return this.random.nextDouble();
        } else {
            return this.threadLocalRandom.nextDouble();
        }
    }

    @Override
    public double nextGaussian() {
        if (this.stateFlagForNextInt) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((RandomGenerator) accessReceiver).updateSeed();
            });
            return this.random.nextGaussian();
        } else {
            return this.threadLocalRandom.nextGaussian();
        }
    }
}
