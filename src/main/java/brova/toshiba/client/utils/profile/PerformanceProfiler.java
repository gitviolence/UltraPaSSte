package brova.toshiba.client.utils.profile;

import brova.toshiba.client.event.EventBus;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceProfiler {

    public static final PerformanceProfiler INSTANCE = new PerformanceProfiler();

    private boolean running = false;

    private long lastUpdateMillis = 0L;

    private long lastUpdateTimestamp = 0L;

    private ProfileReport profileReport = null;

    private final Map<String, ProfileCounters> ifAbsentProfileCountersEntries = new ConcurrentHashMap<>();

    public synchronized boolean start(int intValue) {
        if (this.running) {
            return false;
        } else {
            this.ifAbsentProfileCountersEntries.clear();
            this.lastUpdateTimestamp = intValue * 1000L;
            this.lastUpdateMillis = System.currentTimeMillis();
            this.running = true;
            EventBus.setPerfDebug(true);
            return true;
        }
    }

    public synchronized boolean stop() {
        if (!this.running) {
            return false;
        } else {
            this.running = false;
            boolean flag = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                EventBus.setPerfDebug(((boolean) accessArguments[0]));
            });
            this.updateTotalMsWithFormat();
            return true;
        }
    }

    public boolean isRunning() {
        if (this.running && System.currentTimeMillis() - this.lastUpdateMillis >= this.lastUpdateTimestamp) {
            this.stop();
        }
        return this.running;
    }

    public String status() {
        if (!this.running) {
            return "Профайлер выключен.";
        } else {
            long longValue = (System.currentTimeMillis() - this.lastUpdateMillis) / 1000L;
            long longValueValue = this.lastUpdateTimestamp / 1000L;
            return "Профайлер работает [" + longValue + "/" + longValueValue + " сек]...";
        }
    }

    public ProfileReport lastReport() {
        return this.profileReport;
    }

    public synchronized boolean clearResult() {
        if (this.running) {
            return false;
        } else {
            Object value = null;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                accessReceiver.profileReport = (ProfileReport) accessValue;
            });
            return true;
        }
    }

    public void record(String text, long longValue) {
        if (this.running) {
            this.ifAbsentProfileCountersEntries.computeIfAbsent(text, input -> new ProfileCounters()).add(longValue);
        }
    }

    private void updateTotalMsWithFormat() {
        ArrayList<ProfileSnapshot> profileSnapshots = new ArrayList<>();
        long longValue = 0L;
        for (var entry : this.ifAbsentProfileCountersEntries.entrySet()) {
            ProfileCounters value = entry.getValue();
            long longValueValue = value.addAndGetCounter.get();
            long longValueResult = brova.toshiba.client.utils.runtime.AccessGuard.read(value, accessReceiver -> accessReceiver.incrementAndGetCounter).get();
            long longValueItem = brova.toshiba.client.utils.runtime.AccessGuard.read(value, accessReceiver -> accessReceiver.accumulateAndGetCounter).get();
            if (longValueResult > 0L) {
                double doubleValue = (double) longValueValue / longValueResult / 1000000.0;
                double doubleValueValue = longValueItem / 1000000.0;
                double doubleValueResult = longValueValue / 1000000.0;
                profileSnapshots.add(new ProfileSnapshot(entry.getKey(), longValueResult, doubleValueResult, doubleValue, doubleValueValue));
                longValue += longValueValue;
            }
        }
        profileSnapshots.sort((leftProfileSnapshot, rightProfileSnapshot) -> Double.compare(rightProfileSnapshot.totalMs(), leftProfileSnapshot.totalMs()));
        ArrayList<String> values = new ArrayList<>();
        values.add("=== ОТЧЁТ ПРОФАЙЛЕРА ПРОИЗВОДИТЕЛЬНОСТИ ===");
        values.add(String.format("Длительность замера: %.2f сек", (System.currentTimeMillis() - this.lastUpdateMillis) / 1000.0));
        values.add("---------------------------------------------");
        if (profileSnapshots.isEmpty()) {
            values.add("За время замера не было зафиксировано вызовов.");
        } else {
            for (ProfileSnapshot profileSnapshot : profileSnapshots) {
                values.add(String.format("[%s] Всего: %.1fms | Вызовов: %d | Ср: %.3fms | Макс: %.2fms", profileSnapshot.name(), profileSnapshot.totalMs(), profileSnapshot.count(), profileSnapshot.avgMs(), profileSnapshot.maxMs()));
            }
        }
        values.add("=============================================");
        this.profileReport = new ProfileReport(values);
    }
}
