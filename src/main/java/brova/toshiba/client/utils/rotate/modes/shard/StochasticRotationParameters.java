package brova.toshiba.client.utils.rotate.modes.shard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class StochasticRotationParameters {

    private final Random random = new Random();

    private final List<SmoothedRandomScalar> channels = new ArrayList<>();

    final SmoothedRandomScalar overallSpeedScale = this.createSmoothedRandomScalarFromFloat(0.88F, 1.12F);

    final SmoothedRandomScalar maximumSpeedScale = this.createSmoothedRandomScalarFromFloat(0.88F, 1.12F);

    final SmoothedRandomScalar boostSpeedScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar yawSpeedScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar pitchSpeedLimitScale = this.createSmoothedRandomScalarFromFloat(0.75F, 1.25F);

    final SmoothedRandomScalar pitchToYawSpeedScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar velocityPersistenceScale = this.createSmoothedRandomScalarFromFloat(0.9F, 1.1F);

    final SmoothedRandomScalar velocityNoiseScale = this.createSmoothedRandomScalarFromFloat(0.65F, 1.35F);

    final SmoothedRandomScalar yawAccelerationScale = this.createSmoothedRandomScalarFromFloat(0.85F, 1.15F);

    final SmoothedRandomScalar pitchAccelerationScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar rotationStepLimitScale = this.createSmoothedRandomScalarFromFloat(0.85F, 1.15F);

    final SmoothedRandomScalar yawOvershootScale = this.createSmoothedRandomScalarFromFloat(0.75F, 1.25F);

    final SmoothedRandomScalar pitchOvershootScale = this.createSmoothedRandomScalarFromFloat(0.75F, 1.25F);

    final SmoothedRandomScalar errorThresholdScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar yawResponseScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar pitchResponseScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar noisePersistenceScale = this.createSmoothedRandomScalarFromFloat(0.85F, 1.15F);

    final SmoothedRandomScalar velocityNoiseCouplingScale = this.createSmoothedRandomScalarFromFloat(0.75F, 1.25F);

    final SmoothedRandomScalar yawNoiseAmplitudeScale = this.createSmoothedRandomScalarFromFloat(0.6F, 1.4F);

    final SmoothedRandomScalar pitchNoiseAmplitudeScale = this.createSmoothedRandomScalarFromFloat(0.6F, 1.4F);

    final SmoothedRandomScalar yawFrictionScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar yawVelocityFrictionScale = this.createSmoothedRandomScalarFromFloat(0.75F, 1.25F);

    final SmoothedRandomScalar yawVelocityBlendScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar pitchVelocityBlendScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar aimSmoothingScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar recoveryDampingScale = this.createSmoothedRandomScalarFromFloat(0.75F, 1.25F);

    final SmoothedRandomScalar yawRangeScale = this.createSmoothedRandomScalarFromFloat(0.85F, 1.15F);

    final SmoothedRandomScalar motionPhaseDurationScale = this.createSmoothedRandomScalarFromFloat(0.9F, 1.1F);

    final SmoothedRandomScalar motionResponseRatioScale = this.createSmoothedRandomScalarFromFloat(0.85F, 1.15F);

    final SmoothedRandomScalar responseCurvatureScale = this.createSmoothedRandomScalarFromFloat(0.8F, 1.2F);

    final SmoothedRandomScalar responseOvershootScale = this.createSmoothedRandomScalarFromFloat(0.7F, 1.3F);

    final SmoothedRandomScalar targetOffsetXChannel = this.createSmoothedRandomScalarFromFloat(-0.35F, 0.35F);

    final SmoothedRandomScalar targetHeightChannel = this.createSmoothedRandomScalarFromFloat(0.32F, 0.86F);

    final SmoothedRandomScalar targetOffsetZChannel = this.createSmoothedRandomScalarFromFloat(-0.35F, 0.35F);

    final SmoothedRandomScalar aimPointBlendChannel = this.createSmoothedRandomScalarFromFloat(0.15F, 0.65F);

    final SmoothedRandomScalar aimPointResponseChannel = this.createSmoothedRandomScalarFromFloat(0.65F, 1.45F);

    StochasticRotationParameters() {
    }

    private SmoothedRandomScalar createSmoothedRandomScalarFromFloat(float floatValue, float floatValueValue) {
        SmoothedRandomScalar smoothedRandomScalar = new SmoothedRandomScalar(floatValue, floatValueValue);
        this.channels.add(smoothedRandomScalar);
        return smoothedRandomScalar;
    }

    void updateRandomChannels() {
        for (SmoothedRandomScalar smoothedRandomScalar : this.channels) {
            smoothedRandomScalar.update(this.random);
        }
    }

    void resetRandomChannels() {
        for (SmoothedRandomScalar smoothedRandomScalar : this.channels) {
            smoothedRandomScalar.reset();
        }
    }
}
