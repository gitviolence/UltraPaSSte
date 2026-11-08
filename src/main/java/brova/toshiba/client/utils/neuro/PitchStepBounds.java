package brova.toshiba.client.utils.neuro;

public record PitchStepBounds(float minimumStep, float maximumStep) {

    public PitchStepBounds(float minimumStep, float maximumStep) {
        if (Float.isFinite(minimumStep) && Float.isFinite(maximumStep) && !(minimumStep > maximumStep)) {
            this.minimumStep = minimumStep;
            this.maximumStep = maximumStep;
        } else {
            throw new IllegalArgumentException("Invalid pitch bounds");
        }
    }

    public static PitchStepBounds fromProjection(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, float floatValue) {
        if (Double.isFinite(doubleValue) && Double.isFinite(doubleValueValue) && Double.isFinite(doubleValueResult) && Double.isFinite(doubleValueItem) && Float.isFinite(floatValue) && !(doubleValue < 0.0) && !(doubleValueValue < doubleValue) && !(doubleValueItem < doubleValueResult)) {
            double doubleValueEntry = calculateToDegreesWithAtan2(doubleValueResult, doubleValue);
            double doubleValueCopy = calculateToDegreesWithAtan2(doubleValueResult, doubleValueValue);
            double doubleValue2 = calculateToDegreesWithAtan2(doubleValueItem, doubleValue);
            double doubleValue3 = calculateToDegreesWithAtan2(doubleValueItem, doubleValueValue);
            float floatValueValue = (float) Math.min(Math.min(doubleValueEntry, doubleValueCopy), Math.min(doubleValue2, doubleValue3));
            float floatValueResult = (float) Math.max(Math.max(doubleValueEntry, doubleValueCopy), Math.max(doubleValue2, doubleValue3));
            return new PitchStepBounds(floatValueValue - floatValue, floatValueResult - floatValue);
        } else {
            throw new IllegalArgumentException("Invalid box projection");
        }
    }

    public boolean accepts(short shortValue) {
        return this.minimumStep <= 0.0F && this.maximumStep >= 0.0F || shortValue == 0 || this.minimumStep > 0.0F && shortValue > 0 || this.maximumStep < 0.0F && shortValue < 0;
    }

    private static double calculateToDegreesWithAtan2(double doubleValue, double doubleValueValue) {
        return -Math.toDegrees(Math.atan2(doubleValue, doubleValueValue));
    }
}
