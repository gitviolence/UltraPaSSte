package brova.toshiba.client.utils.neuro;

public record RotationStep(float yaw, float pitch) {

    public static final RotationStep ZERO = new RotationStep(0.0F, 0.0F);
}
