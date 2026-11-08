package brova.toshiba.client.utils.profile;

import java.util.concurrent.atomic.AtomicLong;

public class ProfileCounters {

    public final AtomicLong addAndGetCounter = new AtomicLong();

    public final AtomicLong incrementAndGetCounter = new AtomicLong();

    public final AtomicLong accumulateAndGetCounter = new AtomicLong();

    ProfileCounters() {
    }

    public void add(long longValue) {
        this.addAndGetCounter.addAndGet(longValue);
        this.incrementAndGetCounter.incrementAndGet();
        this.accumulateAndGetCounter.accumulateAndGet(longValue, Math::max);
    }
}
