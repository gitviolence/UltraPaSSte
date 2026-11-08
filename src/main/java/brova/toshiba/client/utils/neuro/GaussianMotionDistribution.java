package brova.toshiba.client.utils.neuro;

import java.util.random.RandomGenerator;

public final class GaussianMotionDistribution {

    public static final int mixtureCount = 8;

    public static final int componentWidth = 6;

    public static final int outputSize = 49;

    private final float cachedScalarForYawScale;

    private final float cachedScalarForPitchScale;

    private final double[] sampleDirectDoubleValues = new double[9];

    private final double[] expDoubleValues = new double[9];

    public GaussianMotionDistribution(float cachedScalarForYawScale, float cachedScalarForPitchScale) {
        this.cachedScalarForYawScale = cachedScalarForYawScale;
        this.cachedScalarForPitchScale = cachedScalarForPitchScale;
    }

    public float yawScale() {
        return this.cachedScalarForYawScale;
    }

    public float pitchScale() {
        return this.cachedScalarForPitchScale;
    }

    public MotionSample sampleDirect(float[] values, RandomGenerator randomGenerator) {
        this.updateExpWithOffset(values);
        return this.sampleDirect(values, this.sampleDirectDoubleValues, randomGenerator);
    }

    public MotionSample sampleYaw(float[] values, MotionSample motionSample, float floatValue, TruncatedGaussianBounds truncatedGaussianBounds, RandomGenerator randomGenerator) {
        return this.createMotionSampleFromExpWithPitchVelocity(values, motionSample, floatValue, truncatedGaussianBounds, false, randomGenerator);
    }

    public MotionSample sampleRestingYaw(float[] values, MotionSample motionSample, float floatValue, RandomGenerator randomGenerator) {
        return this.createMotionSampleFromExpWithPitchVelocity(values, motionSample, floatValue, TruncatedGaussianBounds.physical(-180.0, Math.nextDown(180.0F)), true, randomGenerator);
    }

    private MotionSample createMotionSampleFromExpWithPitchVelocity(float[] values, MotionSample motionSample, float floatValue, TruncatedGaussianBounds truncatedGaussianBounds, boolean flag, RandomGenerator randomGenerator) {
        this.updateExpWithOffset(values);
        double doubleValue = (double) this.cachedScalarForYawScale * floatValue / 0.05F;
        double doubleValueValue = (double) this.cachedScalarForPitchScale * floatValue / 0.05F;
        double doubleValueResult = (double) motionSample.pitchVelocity() * floatValue;
        double[] valuesValue = new double[8];
        double[] valuesResult = new double[8];
        double[] valuesItem = new double[8];
        double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;
        for (int index = 0; index < 8; index++) {
            int intValue = offset(index);
            double doubleValueItem = Math.exp(calculateDouble(values[intValue + 3], -4.0, 2.0)) * doubleValue;
            double doubleValueEntry = Math.exp(calculateDouble(values[intValue + 4], -4.0, 2.0)) * doubleValueValue;
            double doubleValueCopy = (doubleValueResult - values[intValue + 2] * doubleValueValue) / doubleValueEntry;
            double doubleValue2 = 0.95 * Math.tanh(values[intValue + 5]);
            valuesValue[index] = values[intValue + 1] * doubleValue + doubleValue2 * doubleValueItem * doubleValueCopy;
            valuesResult[index] = doubleValueItem * Math.sqrt(1.0 - doubleValue2 * doubleValue2);
            valuesItem[index] = values[intValue] - Math.log(doubleValueEntry) - 0.5 * doubleValueCopy * doubleValueCopy + truncatedGaussianBounds.logProbability(valuesValue[index], valuesResult[index]);
            if (flag) {
                double doubleValue3 = valuesValue[index] / valuesResult[index];
                valuesItem[index] -= Math.log(valuesResult[index]) + 0.5 * doubleValue3 * doubleValue3;
            }
            NEGATIVE_INFINITY = Math.max(NEGATIVE_INFINITY, valuesItem[index]);
        }
        if (!Double.isFinite(NEGATIVE_INFINITY)) {
            return motionSample;
        } else {
            double doubleValue4 = 0.0;
            for (int indexValue = 0; indexValue < 8; indexValue++) {
                valuesItem[indexValue] = Math.exp(valuesItem[indexValue] - NEGATIVE_INFINITY);
                doubleValue4 += valuesItem[indexValue];
            }
            double doubleValue5 = randomGenerator.nextDouble() * doubleValue4;
            int intValueValue = 7;
            for (int indexResult = 0; indexResult < 8; indexResult++) {
                doubleValue5 -= valuesItem[indexResult];
                if (doubleValue5 < 0.0) {
                    intValueValue = indexResult;
                    break;
                }
            }
            double doubleValue6 = truncatedGaussianBounds.sample(valuesValue[intValueValue], valuesResult[intValueValue], randomGenerator);
            return new MotionSample((float) (doubleValue6 / floatValue), motionSample.pitchVelocity(), intValueValue + 1, (float) this.sampleDirectDoubleValues[intValueValue + 1], false);
        }
    }

    public MotionSample sampleDirect(float[] values, double[] valuesValue, RandomGenerator randomGenerator) {
        double doubleValue = randomGenerator.nextDouble();
        int intValue = valuesValue.length - 1;
        for (int index = 0; index < valuesValue.length; index++) {
            doubleValue -= valuesValue[index];
            if (doubleValue < 0.0) {
                intValue = index;
                break;
            }
        }
        if (intValue == 0) {
            return new MotionSample(0.0F, 0.0F, 0, (float) valuesValue[0], true);
        } else {
            int intValueValue = offset(intValue - 1);
            double doubleValueValue = randomGenerator.nextGaussian();
            double doubleValueResult = 0.95 * Math.tanh(values[intValueValue + 5]);
            double doubleValueItem = doubleValueResult * doubleValueValue + Math.sqrt(1.0 - doubleValueResult * doubleValueResult) * randomGenerator.nextGaussian();
            double doubleValueEntry = values[intValueValue + 1] + doubleValueValue * Math.exp(calculateDouble(values[intValueValue + 3], -4.0, 2.0));
            double doubleValueCopy = values[intValueValue + 2] + doubleValueItem * Math.exp(calculateDouble(values[intValueValue + 4], -4.0, 2.0));
            return this.createMotionSampleFromDouble(doubleValueEntry, doubleValueCopy, intValue, valuesValue[intValue]);
        }
    }

    private MotionSample createMotionSampleFromDouble(double doubleValue, double doubleValueValue, int intValue, double doubleValueResult) {
        return new MotionSample((float) (doubleValue * this.cachedScalarForYawScale / 0.05F), (float) (doubleValueValue * this.cachedScalarForPitchScale / 0.05F), intValue, (float) doubleValueResult, false);
    }

    private void updateExpWithOffset(float[] values) {
        double doubleValue = values[0];
        for (int index = 0; index < 8; index++) {
            doubleValue = Math.max(doubleValue, (double) values[offset(index)]);
        }
        double doubleValueValue = Math.exp(values[0] - doubleValue);
        for (int indexValue = 0; indexValue < 8; indexValue++) {
            doubleValueValue += Math.exp(values[offset(indexValue)] - doubleValue);
        }
        double doubleValueResult = doubleValue + Math.log(doubleValueValue);
        this.expDoubleValues[0] = values[0] - doubleValueResult;
        this.sampleDirectDoubleValues[0] = Math.exp(this.expDoubleValues[0]);
        for (int indexResult = 0; indexResult < 8; indexResult++) {
            this.expDoubleValues[indexResult + 1] = values[offset(indexResult)] - doubleValueResult;
            this.sampleDirectDoubleValues[indexResult + 1] = Math.exp(this.expDoubleValues[indexResult + 1]);
        }
    }

    public static int offset(int intValue) {
        return 1 + intValue * 6;
    }

    private static double calculateDouble(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return Math.max(doubleValueValue, Math.min(doubleValueResult, doubleValue));
    }
}
