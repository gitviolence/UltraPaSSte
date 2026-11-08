package brova.toshiba.client.utils.neuro;

import brova.toshiba.client.utils.rotate.AimProjection;
import java.util.Arrays;

public final class MotionFeaturePipeline {

    public static final int CONTEXT_FEATURES = 39;

    private final MotionHistoryFeatures snapshotMotionHistoryFeatures = new MotionHistoryFeatures();

    private float previousYaw;

    private float previousPitch;

    private float currentYaw;

    private float currentPitch;

    private float targetYawDelta;

    private float yawAcceleration;

    private float previousYawVelocity;

    private float previousYawAcceleration;

    private float idleSeconds;

    private float sampleDeltaSeconds = 0.05F;

    private boolean stateFlag;

    public float[] features(float[] values) {
        if (values != null && values.length == 39) {
            this.currentYaw = calculateFloat(values[0]);
            this.currentPitch = values[1];
            float[] valuesValue = Arrays.copyOf(values, 88);
            valuesValue[0] = this.currentYaw;
            valuesValue[1] = calculateFloat(this.currentPitch, -180.0F, 180.0F);
            valuesValue[2] = calculateFloat(values[2], 0.1F, 64.0F);
            valuesValue[3] = this.targetYawDelta;
            valuesValue[4] = this.yawAcceleration;
            valuesValue[5] = this.previousYawVelocity;
            valuesValue[6] = this.previousYawAcceleration;
            valuesValue[7] = this.stateFlag ? calculateFloat(this.currentYaw - this.previousYaw) : 0.0F;
            valuesValue[8] = this.stateFlag ? calculateFloat(this.currentPitch - this.previousPitch, -180.0F, 180.0F) : 0.0F;
            valuesValue[14] = this.idleSeconds;
            valuesValue[15] = calculateFloat(values[15], 0.0F, 3.0F);
            valuesValue[16] = calculateFloat(values[16], 0.0F, 1.0F);
            valuesValue[21] = calculateFloat(values[21], 5.0E-4F, 0.25F);
            float[] valuesResult = this.snapshotMotionHistoryFeatures.snapshot();
            System.arraycopy(valuesResult, 0, valuesValue, 39, valuesResult.length);
            byte byteValue = 80;
            float floatValue = Math.max(5.0E-4F, this.sampleDeltaSeconds);
            valuesValue[byteValue] = valuesValue[7] / floatValue;
            valuesValue[byteValue + 1] = valuesValue[8] / floatValue;
            valuesValue[byteValue + 2] = this.stateFlag ? CombatMotionSchema.targetYawDelta(valuesValue[7], this.targetYawDelta) / floatValue : 0.0F;
            valuesValue[byteValue + 3] = this.stateFlag ? (valuesValue[8] + this.yawAcceleration) / floatValue : 0.0F;
            valuesValue[byteValue + 4] = calculateFloat(valuesValue[0] / Math.max(values[29], 0.1F), -16.0F, 16.0F);
            valuesValue[byteValue + 5] = calculateFloat(valuesValue[1] / Math.max(values[30], 0.1F), -16.0F, 16.0F);
            valuesValue[byteValue + 6] = this.stateFlag && this.currentYaw * this.previousYaw < 0.0F && Math.abs(this.currentYaw - this.previousYaw) < 180.0F ? 1.0F : 0.0F;
            valuesValue[byteValue + 7] = this.stateFlag && this.currentPitch * this.previousPitch < 0.0F ? 1.0F : 0.0F;
            return valuesValue;
        } else {
            throw new IllegalArgumentException("Invalid neuro context");
        }
    }

    public RotationStep update(NeuroPredictor neuroPredictor, float[] values, float floatValue, float floatValueValue) {
        return this.update(neuroPredictor, values, floatValue, floatValueValue, null);
    }

    public RotationStep update(NeuroPredictor neuroPredictor, float[] values, float floatValue, float floatValueValue, PitchStepBounds pitchStepBounds) {
        return this.update(neuroPredictor, values, floatValue, floatValueValue, pitchStepBounds, null);
    }

    public RotationStep update(NeuroPredictor neuroPredictor, float[] values, float floatValue, float floatValueValue, PitchStepBounds pitchStepBounds, AimProjection aimProjection) {
        if (neuroPredictor != null && Float.isFinite(floatValue) && Float.isFinite(floatValueValue) && !(floatValueValue <= 0.0F) && values != null && values.length == 39) {
            for (float floatValueResult : values) {
                if (!Float.isFinite(floatValueResult)) {
                    this.reset();
                    return RotationStep.ZERO;
                }
            }
            float[] valuesValue = this.features(values);
            RotationStep rotationStep = neuroPredictor.predict(valuesValue, floatValueValue, pitchStepBounds, aimProjection);
            float floatValueItem = rotationStep.yaw();
            float floatValueEntry = calculateFloat(floatValue + rotationStep.pitch(), -90.0F, 90.0F) - floatValue;
            return new RotationStep(floatValueItem, floatValueEntry);
        } else {
            this.reset();
            return RotationStep.ZERO;
        }
    }

    public void observe(float targetYawDelta, float yawAcceleration, float floatValue, float sampleDeltaSeconds) {
        if (Float.isFinite(targetYawDelta) && Float.isFinite(yawAcceleration) && Float.isFinite(floatValue) && !(floatValue <= 0.0F) && Float.isFinite(sampleDeltaSeconds) && !(sampleDeltaSeconds < 5.0E-4F)) {
            this.snapshotMotionHistoryFeatures.push(targetYawDelta / sampleDeltaSeconds, yawAcceleration / sampleDeltaSeconds, targetYawDelta - Math.round(targetYawDelta / floatValue) * floatValue, yawAcceleration - Math.round(yawAcceleration / floatValue) * floatValue, sampleDeltaSeconds);
            this.previousYawVelocity = this.targetYawDelta;
            this.previousYawAcceleration = this.yawAcceleration;
            this.targetYawDelta = targetYawDelta;
            this.yawAcceleration = yawAcceleration;
            this.previousYaw = this.currentYaw;
            this.previousPitch = this.currentPitch;
            this.sampleDeltaSeconds = sampleDeltaSeconds;
            this.idleSeconds = Math.abs(targetYawDelta) < 1.0E-5F && Math.abs(yawAcceleration) < 1.0E-5F ? Math.min(10.0F, this.idleSeconds + sampleDeltaSeconds) : 0.0F;
            this.stateFlag = true;
        } else {
            this.reset();
        }
    }

    public void reset() {
        this.snapshotMotionHistoryFeatures.reset();
        this.previousYaw = this.previousPitch = this.currentYaw = this.currentPitch = 0.0F;
        this.targetYawDelta = this.yawAcceleration = this.previousYawVelocity = this.previousYawAcceleration = 0.0F;
        this.idleSeconds = 0.0F;
        this.sampleDeltaSeconds = 0.05F;
        this.stateFlag = false;
    }

    private static float calculateFloat(float floatValue) {
        float floatValueValue = floatValue % 360.0F;
        if (floatValueValue >= 180.0F) {
            floatValueValue -= 360.0F;
        }
        if (floatValueValue < -180.0F) {
            floatValueValue += 360.0F;
        }
        return floatValueValue;
    }

    private static float calculateFloat(float floatValue, float floatValueValue, float floatValueResult) {
        return Math.max(floatValueValue, Math.min(floatValueResult, floatValue));
    }
}
