package brova.toshiba.client.utils.neuro;

import brova.toshiba.client.utils.rotate.AimProjection;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class NeuroPredictor {

    public static final int FEATURE_COUNT = 88;

    public static final float STEP_SECONDS = 0.05F;

    public final GaussianMotionDistribution sampleDirectGaussianMotionDistribution;

    private final float[] featureMeans;

    private final float[] featureScales;

    private final float[] skipConnectionWeights;

    private final float[] normalizedFeatures = new float[88];

    private final float[] networkOutput = new float[49];

    private final RecurrentMotionNetwork recurrentNetwork;

    private final MotionDistributionSampler motionSampler;

    private final RotationStepDecoder motionDecoder = new RotationStepDecoder();

    private NeuroPredictor(float[] featureMeans, float[] featureScales, float[] values, float[] valuesValue, float[] valuesResult, float[] valuesItem, float[] valuesEntry, float[] skipConnectionWeights, GaussianMotionDistribution sampleDirectGaussianMotionDistribution) {
        this.featureMeans = featureMeans;
        this.featureScales = featureScales;
        this.skipConnectionWeights = skipConnectionWeights;
        this.sampleDirectGaussianMotionDistribution = sampleDirectGaussianMotionDistribution;
        this.recurrentNetwork = new RecurrentMotionNetwork(88, 48, this.networkOutput.length, values, valuesResult, valuesValue, valuesItem, valuesEntry);
        this.motionSampler = MotionDistributionSampler.forModel(this);
    }

    public synchronized RotationStep predict(float[] values, float floatValue, PitchStepBounds pitchStepBounds, AimProjection aimProjection) {
        if (validFeatures(values) && Float.isFinite(floatValue) && !(floatValue <= 0.0F)) {
            for (int index = 0; index < 88; index++) {
                this.normalizedFeatures[index] = this.featureScales[index] == 0.0F ? 0.0F : Math.max(-6.0F, Math.min(6.0F, (values[index] - this.featureMeans[index]) / this.featureScales[index]));
            }
            this.recurrentNetwork.step(this.normalizedFeatures, this.networkOutput);
            for (int indexValue = 0; indexValue < this.networkOutput.length; indexValue++) {
                float floatValueValue = 0.0F;
                int intValue = indexValue * 39;
                for (int indexResult = 0; indexResult < 39; indexResult++) {
                    floatValueValue += this.skipConnectionWeights[intValue + indexResult] * this.normalizedFeatures[indexResult];
                }
                this.networkOutput[indexValue] = this.networkOutput[indexValue] + floatValueValue;
                if (!Float.isFinite(this.networkOutput[indexValue])) {
                    this.reset();
                    return RotationStep.ZERO;
                }
            }
            ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
            MotionSample motionSample = this.sampleDirectGaussianMotionDistribution.sampleDirect(this.networkOutput, threadLocalRandom);
            motionSample = this.motionSampler.apply(motionSample, this.networkOutput, values, floatValue, pitchStepBounds, aimProjection, threadLocalRandom);
            return this.motionDecoder.decode(motionSample, values, floatValue, pitchStepBounds);
        } else {
            this.reset();
            return RotationStep.ZERO;
        }
    }

    public synchronized void reset() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this.motionDecoder, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((RotationStepDecoder) accessReceiver).reset();
        });
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this.recurrentNetwork, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((RecurrentMotionNetwork) accessReceiver).reset();
        });
        this.motionSampler.reset();
        Arrays.fill(this.normalizedFeatures, 0.0F);
        Arrays.fill(this.networkOutput, 0.0F);
    }

    public static boolean validFeatures(float[] values) {
        if (values != null && values.length == 88) {
            for (float floatValue : values) {
                if (!Float.isFinite(floatValue)) {
                    return false;
                }
            }
            return values[21] >= 0.005F && values[21] <= 0.25F;
        } else {
            return false;
        }
    }

    public static NeuroPredictor load(InputStream inputStream) throws IOException {
        try {
            NeuroPredictor neuroPredictor;
            try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream))) {
                if (dataInputStream.readInt() != 1314018353 || dataInputStream.readInt() != 27 || !"causal-combat-motion-v5".equals(dataInputStream.readUTF()) || dataInputStream.readInt() != 88 || dataInputStream.readInt() != 10) {
                    throw new IOException("Incompatible embedded neuro model");
                }
                int intValue = dataInputStream.readInt();
                int intValueValue = dataInputStream.readInt();
                if (intValue < 200 || intValueValue != 49 || dataInputStream.readInt() != 48) {
                    throw new IOException("Invalid embedded model dimensions");
                }
                float[] values = read(dataInputStream, 88);
                float[] valuesValue = read(dataInputStream, 88);
                for (float floatValue : valuesValue) {
                    if (floatValue < 0.0F) {
                        throw new IOException("Invalid feature scale");
                    }
                }
                float floatValueValue = dataInputStream.readFloat();
                float floatValueResult = dataInputStream.readFloat();
                float floatValueItem = dataInputStream.readFloat();
                float floatValueEntry = dataInputStream.readFloat();
                if (!Float.isFinite(floatValueValue) || !Float.isFinite(floatValueResult) || floatValueValue <= 0.0F || floatValueResult <= 0.0F || !Float.isFinite(floatValueItem) || !Float.isFinite(floatValueEntry) || floatValueItem < 0.0F || floatValueItem > 1.0F || floatValueEntry < 0.0F || floatValueEntry > 1.0F) {
                    throw new IOException("Invalid embedded motion scales");
                }
                float[] valuesResult = read(dataInputStream, 12672);
                float[] valuesItem = read(dataInputStream, 144);
                float[] valuesEntry = read(dataInputStream, 6912);
                float[] valuesCopy = read(dataInputStream, intValueValue * 48);
                float[] values2 = read(dataInputStream, intValueValue);
                float[] values3 = read(dataInputStream, intValueValue * 39);
                if (dataInputStream.read() != -1) {
                    throw new IOException("Unexpected trailing model data");
                }
                neuroPredictor = new NeuroPredictor(values, valuesValue, valuesResult, valuesItem, valuesEntry, valuesCopy, values2, values3, new GaussianMotionDistribution(floatValueValue, floatValueResult));
            }
            return neuroPredictor;
        } catch (EOFException failure) {
            throw new IOException("Truncated embedded neuro model", failure);
        }
    }

    private static float[] read(DataInputStream dataInputStream, int intValue) throws IOException {
        float[] values = new float[intValue];
        for (int index = 0; index < intValue; index++) {
            values[index] = dataInputStream.readFloat();
            if (!Float.isFinite(values[index])) {
                throw new IOException("Invalid model coefficient");
            }
        }
        return values;
    }
}
