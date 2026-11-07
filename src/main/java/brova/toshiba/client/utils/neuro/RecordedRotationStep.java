package brova.toshiba.client.utils.neuro;

public class RecordedRotationStep {

    public final float yawDelta;

    public final float pitchDelta;

    public final long delayMs;

    public RecordedRotationStep(float yawDelta, float pitchDelta, long delayMs) {
        this.yawDelta = yawDelta;
        this.pitchDelta = pitchDelta;
        this.delayMs = delayMs;
    }
}
