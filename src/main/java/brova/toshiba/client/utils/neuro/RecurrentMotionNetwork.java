package brova.toshiba.client.utils.neuro;

import java.util.Arrays;

public final class RecurrentMotionNetwork {

    private final int inputSize;

    private final int hiddenSize;

    private final int outputSize;

    private final float[] inputWeights;

    private final float[] recurrentWeights;

    private final float[] gateBiases;

    private final float[] outputWeights;

    private final float[] outputBiases;

    private final float[] hiddenState;

    private final float[] updateGate;

    private final float[] resetGate;

    private final float[] candidateState;

    private final float[] resetHiddenState;

    private final float[] nextHiddenState;

    public RecurrentMotionNetwork(int inputSize, int hiddenSize, int outputSize, float[] inputWeights, float[] recurrentWeights, float[] gateBiases, float[] outputWeights, float[] outputBiases) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.inputWeights = inputWeights;
        this.recurrentWeights = recurrentWeights;
        this.gateBiases = gateBiases;
        this.outputWeights = outputWeights;
        this.outputBiases = outputBiases;
        this.hiddenState = new float[hiddenSize];
        this.updateGate = new float[hiddenSize];
        this.resetGate = new float[hiddenSize];
        this.candidateState = new float[hiddenSize];
        this.resetHiddenState = new float[hiddenSize];
        this.nextHiddenState = new float[hiddenSize];
    }

    public void reset() {
        Arrays.fill(this.hiddenState, 0.0F);
        Arrays.fill(this.updateGate, 0.0F);
        Arrays.fill(this.resetGate, 0.0F);
        Arrays.fill(this.candidateState, 0.0F);
        Arrays.fill(this.resetHiddenState, 0.0F);
        Arrays.fill(this.nextHiddenState, 0.0F);
    }

    public void step(float[] values, float[] valuesValue) {
        this.updateTanh(0, values, this.hiddenState, this.updateGate, false);
        this.updateTanh(1, values, this.hiddenState, this.resetGate, false);
        for (int index = 0; index < this.hiddenSize; index++) {
            this.resetHiddenState[index] = this.resetGate[index] * this.hiddenState[index];
        }
        this.updateTanh(2, values, this.resetHiddenState, this.candidateState, true);
        for (int indexValue = 0; indexValue < this.hiddenSize; indexValue++) {
            this.nextHiddenState[indexValue] = this.hiddenState[indexValue] + this.updateGate[indexValue] * (this.candidateState[indexValue] - this.hiddenState[indexValue]);
        }
        System.arraycopy(this.nextHiddenState, 0, this.hiddenState, 0, this.hiddenSize);
        for (int indexResult = 0; indexResult < this.outputSize; indexResult++) {
            float floatValue = this.outputBiases[indexResult];
            int intValue = indexResult * this.hiddenSize;
            for (int indexItem = 0; indexItem < this.hiddenSize; indexItem++) {
                floatValue += this.outputWeights[intValue + indexItem] * this.hiddenState[indexItem];
            }
            valuesValue[indexResult] = floatValue;
        }
    }

    private void updateTanh(int intValue, float[] values, float[] valuesValue, float[] valuesResult, boolean flag) {
        int intValueValue = intValue * this.hiddenSize * this.inputSize;
        int intValueResult = intValue * this.hiddenSize * this.hiddenSize;
        int intValueItem = intValue * this.hiddenSize;
        for (int index = 0; index < this.hiddenSize; index++) {
            float floatValue = this.gateBiases[intValueItem + index];
            int intValueEntry = intValueValue + index * this.inputSize;
            for (int indexValue = 0; indexValue < this.inputSize; indexValue++) {
                floatValue += this.inputWeights[intValueEntry + indexValue] * values[indexValue];
            }
            int intValueCopy = intValueResult + index * this.hiddenSize;
            for (int indexResult = 0; indexResult < this.hiddenSize; indexResult++) {
                floatValue += this.recurrentWeights[intValueCopy + indexResult] * valuesValue[indexResult];
            }
            valuesResult[index] = flag ? (float) Math.tanh(floatValue) : calculateExp(floatValue);
        }
    }

    private static float calculateExp(float floatValue) {
        if (floatValue >= 0.0F) {
            return (float) (1.0 / (1.0 + Math.exp(-floatValue)));
        } else {
            double doubleValue = Math.exp(floatValue);
            return (float) (doubleValue / (1.0 + doubleValue));
        }
    }
}
