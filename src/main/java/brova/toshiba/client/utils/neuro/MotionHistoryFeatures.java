package brova.toshiba.client.utils.neuro;

import java.util.Arrays;

public final class MotionHistoryFeatures {

    public static final int FEATURE_COUNT = 41;

    private static final float[] floatValuesForSnapshot = new float[] { 0.05F, 0.15F, 0.35F };

    private final float[][] angularVelocityHistory = new float[2][1024];

    private final float[] sampleDeltaHistory = new float[1024];

    private final float[] copyOfFloatValues = new float[8];

    private int sampleCount;

    private int writeIndex;

    public float[] snapshot() {
        float[] values = Arrays.copyOf(this.copyOfFloatValues, 41);
        this.updateFloat(values, this.copyOfFloatValues.length, floatValuesForSnapshot[0]);
        for (int index = 1; index < floatValuesForSnapshot.length; index++) {
            int intValue = this.copyOfFloatValues.length + 5 + (index - 1) * 14;
            this.updateSqrt(values, intValue, floatValuesForSnapshot[index]);
        }
        return values;
    }

    public void push(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry) {
        if (Float.isFinite(floatValue) && Float.isFinite(floatValueValue) && Float.isFinite(floatValueEntry) && !(floatValueEntry < 5.0E-4F) && Float.isFinite(floatValueResult) && Float.isFinite(floatValueItem)) {
            float floatValueCopy = this.sampleCount == 0 ? 0.0F : (floatValue - this.copyOfFloatValues[0]) / floatValueEntry;
            float floatValue2 = this.sampleCount == 0 ? 0.0F : (floatValueValue - this.copyOfFloatValues[1]) / floatValueEntry;
            this.copyOfFloatValues[4] = this.sampleCount < 2 ? 0.0F : (floatValueCopy - this.copyOfFloatValues[2]) / floatValueEntry;
            this.copyOfFloatValues[5] = this.sampleCount < 2 ? 0.0F : (floatValue2 - this.copyOfFloatValues[3]) / floatValueEntry;
            this.copyOfFloatValues[0] = floatValue;
            this.copyOfFloatValues[1] = floatValueValue;
            this.copyOfFloatValues[2] = floatValueCopy;
            this.copyOfFloatValues[3] = floatValue2;
            this.copyOfFloatValues[6] = floatValueResult;
            this.copyOfFloatValues[7] = floatValueItem;
            this.angularVelocityHistory[0][this.writeIndex] = floatValue;
            this.angularVelocityHistory[1][this.writeIndex] = floatValueValue;
            this.sampleDeltaHistory[this.writeIndex] = floatValueEntry;
            this.writeIndex = (this.writeIndex + 1) % 1024;
            this.sampleCount = Math.min(1024, this.sampleCount + 1);
        } else {
            this.reset();
        }
    }

    public void reset() {
        Arrays.fill(this.copyOfFloatValues, 0.0F);
        this.sampleCount = 0;
        this.writeIndex = 0;
    }

    private void updateFloat(float[] values, int intValue, float floatValue) {
        double doubleValue = 0.0;
        double doubleValueValue = 0.0;
        double doubleValueResult = 0.0;
        double doubleValueItem = 0.0;
        double doubleValueEntry = 0.0;
        for (int index = 0; index < this.sampleCount && doubleValue < floatValue; index++) {
            int intValueValue = (this.writeIndex - index - 1 + 1024) % 1024;
            double doubleValueCopy = Math.min((double) this.sampleDeltaHistory[intValueValue], floatValue - doubleValue);
            float floatValueValue = this.angularVelocityHistory[0][intValueValue];
            float floatValueResult = this.angularVelocityHistory[1][intValueValue];
            doubleValueValue += Math.abs(floatValueValue) * doubleValueCopy;
            doubleValueResult += Math.abs(floatValueResult) * doubleValueCopy;
            if (Math.abs(floatValueValue) > 1.0E-4F) {
                doubleValueItem += doubleValueCopy;
            }
            if (Math.abs(floatValueResult) > 1.0E-4F) {
                doubleValueEntry += doubleValueCopy;
            }
            doubleValue += doubleValueCopy;
        }
        if (!(doubleValue <= 0.0)) {
            values[intValue] = (float) doubleValueValue;
            values[intValue + 1] = (float) doubleValueResult;
            values[intValue + 2] = (float) (doubleValueItem / doubleValue);
            values[intValue + 3] = (float) (doubleValueEntry / doubleValue);
            values[intValue + 4] = (float) Math.min(1.0, doubleValue / floatValue);
        }
    }

    private void updateSqrt(float[] values, int intValue, float floatValue) {
        double doubleValue = 0.0;
        double[] valuesValue = new double[2];
        double[] valuesResult = new double[2];
        double[] valuesItem = new double[2];
        double[] valuesEntry = new double[2];
        double[] valuesCopy = new double[2];
        float[] values2 = new float[2];
        double doubleValueValue = 0.0;
        for (int index = 0; index < this.sampleCount && doubleValue < floatValue; index++) {
            int intValueValue = (this.writeIndex - index - 1 + 1024) % 1024;
            double doubleValueResult = Math.min((double) this.sampleDeltaHistory[intValueValue], floatValue - doubleValue);
            for (int indexValue = 0; indexValue < 2; indexValue++) {
                float floatValueValue = this.angularVelocityHistory[indexValue][intValueValue];
                valuesValue[indexValue] += floatValueValue * doubleValueResult;
                valuesResult[indexValue] += Math.abs(floatValueValue) * doubleValueResult;
                valuesItem[indexValue] += (double) floatValueValue * floatValueValue * doubleValueResult;
                if (Math.abs(floatValueValue) > 1.0E-4F) {
                    valuesEntry[indexValue] += doubleValueResult;
                }
                if (index > 0 && floatValueValue * values2[indexValue] < 0.0F) {
                    valuesCopy[indexValue]++;
                }
                values2[indexValue] = floatValueValue;
            }
            doubleValueValue += (double) this.angularVelocityHistory[0][intValueValue] * this.angularVelocityHistory[1][intValueValue] * doubleValueResult;
            doubleValue += doubleValueResult;
        }
        if (!(doubleValue <= 0.0)) {
            double[] values3 = new double[2];
            for (int indexResult = 0; indexResult < 2; indexResult++) {
                double doubleValueItem = valuesValue[indexResult] / doubleValue;
                values3[indexResult] = Math.max(0.0, valuesItem[indexResult] / doubleValue - doubleValueItem * doubleValueItem);
                values[intValue + indexResult] = (float) valuesValue[indexResult];
                values[intValue + 2 + indexResult] = (float) valuesResult[indexResult];
                values[intValue + 4 + indexResult] = (float) doubleValueItem;
                values[intValue + 6 + indexResult] = (float) Math.sqrt(values3[indexResult]);
                values[intValue + 8 + indexResult] = (float) (valuesEntry[indexResult] / doubleValue);
                values[intValue + 10 + indexResult] = (float) (valuesCopy[indexResult] / doubleValue);
            }
            double squareRoot = Math.sqrt(values3[0] * values3[1]);
            double doubleValueEntry = doubleValueValue / doubleValue - valuesValue[0] * valuesValue[1] / (doubleValue * doubleValue);
            values[intValue + 12] = squareRoot < 1.0E-8 ? 0.0F : (float) Math.max(-1.0, Math.min(1.0, doubleValueEntry / squareRoot));
            values[intValue + 13] = (float) Math.min(1.0, doubleValue / floatValue);
        }
    }
}
