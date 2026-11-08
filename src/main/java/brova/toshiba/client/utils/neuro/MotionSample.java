package brova.toshiba.client.utils.neuro;

public record MotionSample(float yawVelocity, float pitchVelocity, int component, float probability, boolean stopped) {
}
