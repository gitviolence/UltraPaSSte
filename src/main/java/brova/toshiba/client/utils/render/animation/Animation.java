package brova.toshiba.client.utils.render.animation;

import brova.toshiba.client.utils.animation.Easing;
import net.minecraft.util.math.MathHelper;

public class Animation {

    private float value;

    private float target;

    private float startValue;

    private float speed;

    private Easing easing;

    private long startTimeNanos;

    private double durationSeconds;

    private boolean running;

    public Animation(float value, float speed, Easing easing) {
        this.value = value;
        this.target = value;
        this.startValue = value;
        this.speed = speed;
        this.easing = easing != null ? easing : PowerEasing.LINEAR;
        this.startTimeNanos = 0L;
        this.durationSeconds = 0.0;
        this.running = false;
    }

    public Animation(float floatValue, float floatValueValue) {
        this(floatValue, floatValueValue, PowerEasing.LINEAR);
    }

    public void update(float target) {
        if (this.target != target || !this.running) {
            this.target = target;
            this.startValue = this.value;
            this.startTimeNanos = System.nanoTime();
            this.durationSeconds = 1.0 / this.speed * 2.0;
            this.running = true;
        }
        if (this.isDone()) {
            this.value = this.target;
            this.running = false;
        } else {
            double progress = this.getProgress();
            float floatValue = (float) this.easing.ease(progress);
            this.value = MathHelper.lerp(floatValue, this.startValue, this.target);
        }
    }

    private double getProgress() {
        if (!this.running) {
            return 1.0;
        } else {
            long currentTimeNanos = System.nanoTime();
            double doubleValue = (currentTimeNanos - this.startTimeNanos) / 1.0E9;
            return MathHelper.clamp(doubleValue / this.durationSeconds, 0.0, 1.0);
        }
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        this.value = value;
        this.target = value;
        this.startValue = value;
        this.running = false;
    }

    public float getTarget() {
        return this.target;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.durationSeconds = 1.0 / speed;
    }

    public void setEasing(Easing easing) {
        this.easing = easing != null ? easing : PowerEasing.LINEAR;
    }

    public boolean isDone() {
        return this.getProgress() >= 1.0;
    }

    public boolean isAlive() {
        return !this.isDone();
    }
}
