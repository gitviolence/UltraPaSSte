package brova.toshiba.client.utils.neuro;

public final class RotationStepDecoder {

    private float yawResidual;

    private float pitchResidual;

    public RotationStep decode(MotionSample motionSample, float[] values, float floatValue, PitchStepBounds pitchStepBounds) {
        if (!NeuroPredictor.validFeatures(values)) {
            this.reset();
            return RotationStep.ZERO;
        } else {
            return this.createRotationStepFromFiniteWithResidual((double) motionSample.yawVelocity() * values[21], (double) motionSample.pitchVelocity() * values[21], motionSample.stopped(), values, floatValue, pitchStepBounds);
        }
    }

    private RotationStep createRotationStepFromFiniteWithResidual(double doubleValue, double doubleValueValue, boolean flag, float[] values, float floatValue, PitchStepBounds pitchStepBounds) {
        if (!NeuroPredictor.validFeatures(values) || !Float.isFinite(floatValue) || floatValue <= 0.0F || !Double.isFinite(doubleValue) || !Double.isFinite(doubleValueValue)) {
            this.reset();
            return RotationStep.ZERO;
        } else if (flag) {
            this.yawResidual = this.pitchResidual = 0.0F;
            return RotationStep.ZERO;
        } else {
            DecodedMotion decodedMotion = createDecodedMotionFromDouble(doubleValue, -180.0, Math.nextDown(180.0F), floatValue, this.yawResidual);
            DecodedMotion decodedMotionValue = createDecodedMotionFromDouble(doubleValueValue, pitchStepBounds == null ? -180.0 : pitchStepBounds.minimumStep(), pitchStepBounds == null ? 180.0 : pitchStepBounds.maximumStep(), floatValue, this.pitchResidual);
            this.yawResidual = decodedMotion.residual();
            this.pitchResidual = decodedMotionValue.residual();
            return new RotationStep(decodedMotion.movement(), decodedMotionValue.movement());
        }
    }

    public void reset() {
        this.yawResidual = this.pitchResidual = 0.0F;
    }

    private static DecodedMotion createDecodedMotionFromDouble(double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, float floatValueValue) {
        double doubleValueItem = Math.max(doubleValueValue, Math.min(doubleValueResult, doubleValue));
        double doubleValueEntry = doubleValueItem + floatValueValue;
        long longValue = Math.round(doubleValueEntry / floatValue);
        long roundedUp = (long) Math.ceil(doubleValueValue / floatValue);
        long roundedDown = (long) Math.floor(doubleValueResult / floatValue);
        if (roundedUp > roundedDown) {
            return new DecodedMotion(0.0F, 0.0F);
        } else {
            long longValueValue = Math.max(roundedUp, Math.min(roundedDown, longValue));
            float floatValueResult = (float) longValueValue * floatValue;
            float floatValueItem = longValueValue == longValue && doubleValueItem == doubleValue ? (float) (doubleValueEntry - floatValueResult) : 0.0F;
            return new DecodedMotion(floatValueResult, floatValueItem);
        }
    }
}
