package brova.toshiba.client.utils.time;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TickTaskScheduler {

    private final List<ScheduledTickTask> finished = new CopyOnWriteArrayList<>();

    private int cachedCount = 0;

    public TickTaskScheduler cleanup() {
        this.finished.clear();
        this.cachedCount = 0;
        return this;
    }

    public TickTaskScheduler cleanupIfFinished() {
        if (this.isFinished()) {
            this.cleanup();
        }
        return this;
    }

    public boolean isFinished() {
        return this.finished.isEmpty();
    }

    public void addTickStep(int intValue, Runnable runnable) {
        this.finished.add(new ScheduledTickTask(this.cachedCount + intValue, runnable));
    }

    public void update() {
        if (!this.finished.isEmpty()) {
            ArrayList<ScheduledTickTask> values = new ArrayList<>();
            for (ScheduledTickTask scheduledTickTask : this.finished) {
                if (this.cachedCount >= scheduledTickTask.cachedCount) {
                    values.add(scheduledTickTask);
                }
            }
            this.finished.removeAll(values);
            for (ScheduledTickTask scheduledTickTaskValue : values) {
                brova.toshiba.client.utils.runtime.AccessGuard.read(scheduledTickTaskValue, accessReceiver -> accessReceiver.runnable).run();
            }
            this.cachedCount++;
        }
    }
}
