package brova.toshiba.client.utils.rotate.modes.shard;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.player.AttackTimer;
import brova.toshiba.client.utils.rotate.CloseRangeRotationMultiplier;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import brova.toshiba.client.utils.rotate.RotationPointUtil;
import brova.toshiba.client.utils.rotate.RotationUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ShardRotation extends RotationModeBase implements MinecraftAccess {

    private final AttackAura attackAura;

    private final CloseRangeRotationMultiplier closeRangeRotationMultiplier = new CloseRangeRotationMultiplier();

    private final AttackTimer attackTimer = new AttackTimer();

    private final StochasticRotationParameters startValueStochasticRotationParameters = new StochasticRotationParameters();

    private LivingEntity livingEntity;

    private Vec3d vec3d;

    private float quantizedYaw;

    private float quantizedPitch;

    private float smoothedYaw;

    private float smoothedPitch;

    private float yawVelocity;

    private float pitchVelocity;

    private float previousYawVelocity;

    private float previousPitchVelocity;

    private float minimum;

    private float yawNoise;

    private float pitchNoise;

    private float motionStrength;

    private float speedNoise;

    private float motionDuration = 0.18F;

    private float accelerationDuration;

    private float decelerationDuration;

    private float accelerationBias;

    private float groundMotionScale;

    private float phaseElapsedSeconds;

    private float cachedScalarForUpdateVec2f;

    private AccelerationPhase accelerating = AccelerationPhase.ACCELERATING;

    private boolean initializationPending;

    private boolean recoveryPending;

    private boolean hadPlayer;

    public ShardRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public boolean isReturningToCamera() {
        return false;
    }

    public void reset() {
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.livingEntity = (LivingEntity) accessValue;
        });
        this.vec3d = null;
        this.motionStrength = this.speedNoise = this.yawVelocity = this.pitchVelocity = this.previousYawVelocity = this.previousPitchVelocity = this.minimum = 0.0F;
        this.yawNoise = this.pitchNoise = 0.0F;
        this.accelerationDuration = this.decelerationDuration = this.accelerationBias = this.groundMotionScale = 0.0F;
        this.phaseElapsedSeconds = this.cachedScalarForUpdateVec2f = 0.0F;
        this.initializationPending = true;
        this.motionDuration = 0.18F;
        value = AccelerationPhase.ACCELERATING;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.accelerating = (AccelerationPhase) accessValue;
        });
        this.recoveryPending = false;
        AttackAura.otvodkaPitch = 0.0F;
        AttackAura.otvodkaYaw = 0.0F;
        this.attackTimer.reset();
        this.startValueStochasticRotationParameters.resetRandomChannels();
        if (this.hadPlayer = mc.player != null) {
            this.quantizedYaw = this.smoothedYaw = mc.player.getYaw();
            this.quantizedPitch = this.smoothedPitch = mc.player.getPitch();
        }
    }

    public void onAttack() {
        if (this.hadPlayer && this.livingEntity != null && mc.player != null && !mc.player.isGliding() && this.accelerating != AccelerationPhase.BRAKING) {
            this.recoveryPending = false;
        }
    }

    public void returnToCamera() {
        if (RotationStorage.instance != null) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(RotationStorage.instance, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((RotationStorage) accessReceiver).stopRotation();
            });
        }
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((ShardRotation) accessReceiver).reset();
        });
    }

    @Override
    public void updateRotations(LivingEntity livingEntity) {
        if (this.attackTimer.update()) {
            this.onAttack();
        }
        if (mc.player != null && livingEntity != null) {
            if (mc.player.isBlocking()) {
                this.rotate = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
                this.updateYawWithPitch();
            } else {
                if (!this.hadPlayer) {
                    this.reset();
                }
                ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
                boolean gliding = mc.player.isGliding();
                boolean flag = gliding && livingEntity.isGliding();
                if (this.livingEntity != livingEntity) {
                    this.livingEntity = livingEntity;
                    this.vec3d = null;
                    this.updateYawWithPitch();
                    this.motionStrength = 0.0F;
                    this.initializationPending = true;
                    this.startValueStochasticRotationParameters.resetRandomChannels();
                }
                StochasticRotationParameters startValueStochasticRotationParameters = this.startValueStochasticRotationParameters;
                startValueStochasticRotationParameters.updateRandomChannels();
                float clampedValue = MathHelper.clamp(0.85F * startValueStochasticRotationParameters.velocityPersistenceScale.value, 0.65F, 0.96F);
                this.speedNoise = MathHelper.clamp(this.speedNoise * clampedValue + (float) threadLocalRandom.nextGaussian() * 0.045F * startValueStochasticRotationParameters.velocityNoiseScale.value, -0.25F, 0.25F);
                Vec2f rotation = this.createVec2fFromSubtractWithCenter(livingEntity);
                boolean flagValue = mc.player.getAttackCooldownProgress(1.0F) > 0.9F && this.attackAura.getWhiteRiseTicksToAttack() <= 1;
                this.updateVec2f(rotation, flag, threadLocalRandom);
                // Агрессия: множитель «Сила ротации» — гонит скорость доводки, разгона и отклика.
                float aggression = this.attackAura.getRotationStrength();
                float clampedValueValue = MathHelper.clamp(this.motionStrength / this.motionDuration, 0.0F, 1.0F);
                if (clampedValueValue == 0.0F) {
                    this.updateUpdate(flag);
                } else {
                    float floatValue = this.motionStrength * (flagValue ? 1.0F + 0.45F * startValueStochasticRotationParameters.boostSpeedScale.value : 1.0F);
                    floatValue *= MathHelper.clamp(1.0F + this.speedNoise * 0.85F, 0.85F, 1.15F);
                    floatValue = MathHelper.clamp(floatValue, 0.0F, (gliding ? 0.552F : 0.408F) * startValueStochasticRotationParameters.maximumSpeedScale.value) * 0.95F * startValueStochasticRotationParameters.overallSpeedScale.value * aggression;
                    float floatValueValue = floatValue * (0.89F + this.speedNoise * 1.11F) * startValueStochasticRotationParameters.yawSpeedScale.value;
                    float floatValueResult = Math.min(floatValue * 0.69451F * startValueStochasticRotationParameters.pitchSpeedLimitScale.value, floatValueValue * 0.413F * startValueStochasticRotationParameters.pitchToYawSpeedScale.value);
                    float floatValueItem = 0.9F + threadLocalRandom.nextFloat() * 0.2F;
                    float floatValueEntry = (gliding ? 86.4F : (flagValue ? 38.4F : 28.8F)) * floatValueItem * startValueStochasticRotationParameters.yawAccelerationScale.value * aggression;
                    float floatValueCopy = (gliding ? 14.4F : (flagValue ? 5.4F : 3.36F)) * floatValueItem * startValueStochasticRotationParameters.pitchAccelerationScale.value * aggression;
                    float floatValue2 = 4.8F * startValueStochasticRotationParameters.rotationStepLimitScale.value;
                    float floatValue3 = this.calculateNextFloatWithSignum(MathHelper.wrapDegrees(rotation.x - this.smoothedYaw), floatValueEntry, threadLocalRandom);
                    float floatValue4 = this.calculateNextFloatWithSignum(rotation.y - this.smoothedPitch, floatValueCopy, threadLocalRandom);
                    float floatValue5 = 3.0F * startValueStochasticRotationParameters.yawOvershootScale.value;
                    float floatValue6 = 3.0F * startValueStochasticRotationParameters.pitchOvershootScale.value;
                    float floatValue7 = 0.4F * startValueStochasticRotationParameters.errorThresholdScale.value;
                    float floatValue8 = floatValue7 + (1.0F - floatValue7) * Math.min(Math.abs(floatValue3) / floatValue5, 1.0F);
                    float floatValue9 = floatValue7 + (1.0F - floatValue7) * Math.min(Math.abs(floatValue4) / floatValue6, 1.0F);
                    float floatValue10 = (0.16F + threadLocalRandom.nextFloat() * 0.08F) * startValueStochasticRotationParameters.yawResponseScale.value * floatValue8 * (0.6F + 0.4F * aggression);
                    float floatValue11 = (0.45F + threadLocalRandom.nextFloat() * 0.15F) * startValueStochasticRotationParameters.pitchResponseScale.value * floatValue9 * (0.6F + 0.4F * aggression);
                    float floatValue12 = 0.55F * startValueStochasticRotationParameters.noisePersistenceScale.value;
                    float floatValue13 = (0.006F + Math.abs(this.yawVelocity) * 0.012F * startValueStochasticRotationParameters.velocityNoiseCouplingScale.value) * startValueStochasticRotationParameters.yawNoiseAmplitudeScale.value;
                    float floatValue14 = (0.02F + Math.abs(this.pitchVelocity) * 0.05F * startValueStochasticRotationParameters.velocityNoiseCouplingScale.value) * startValueStochasticRotationParameters.pitchNoiseAmplitudeScale.value;
                    this.yawNoise = (this.yawNoise * floatValue12 + (float) threadLocalRandom.nextGaussian() * floatValue13 * (1.0F - floatValue12)) * clampedValueValue;
                    this.pitchNoise = (this.pitchNoise * floatValue12 + (float) threadLocalRandom.nextGaussian() * floatValue14 * (1.0F - floatValue12)) * clampedValueValue;
                    float floatValue15 = 0.7F * startValueStochasticRotationParameters.yawFrictionScale.value + Math.abs(this.yawVelocity) * 0.2F * startValueStochasticRotationParameters.yawVelocityFrictionScale.value;
                    float yawVelocity = MathHelper.clamp((floatValue3 * floatValueValue - this.yawVelocity) * floatValue10 + this.yawNoise, this.minimum - floatValue15, this.minimum + floatValue15);
                    this.yawVelocity += yawVelocity;
                    this.minimum = yawVelocity;
                    this.pitchVelocity = this.calculateNextFloatWithSignum(this.pitchVelocity + (floatValue4 * floatValueResult - this.pitchVelocity) * floatValue11 + this.pitchNoise, floatValue2, threadLocalRandom);
                    float floatValue16 = 0.5F * startValueStochasticRotationParameters.yawVelocityBlendScale.value;
                    float floatValue17 = 0.5F * startValueStochasticRotationParameters.pitchVelocityBlendScale.value;
                    this.smoothedYaw = this.smoothedYaw + (this.yawVelocity * floatValue16 + this.previousYawVelocity * (1.0F - floatValue16)) * clampedValueValue;
                    this.smoothedPitch = MathHelper.clamp(this.smoothedPitch + (this.pitchVelocity * floatValue17 + this.previousPitchVelocity * (1.0F - floatValue17)) * clampedValueValue, -89.0F, 89.0F);
                    this.previousYawVelocity = this.yawVelocity;
                    this.previousPitchVelocity = this.pitchVelocity;
                    float floatValue18 = this.closeRangeRotationMultiplier.getMultiplier(livingEntity) * clampedValueValue;
                    this.smoothedYaw = this.smoothedYaw + (float) Math.sin((float) (System.currentTimeMillis() % 1000000L) / 46.0F) * 1.3F * clampedValueValue;
                    this.smoothedPitch = MathHelper.clamp(this.smoothedPitch + (float) Math.sin((float) (System.currentTimeMillis() % 1000000L) / 41.0F) * 0.4F * clampedValueValue, -89.0F, 89.0F);
                    float gCDValue = MouseGcdUtil.getGCDValue();
                    float smoothedYaw = this.smoothedYaw;
                    float smoothedPitch = this.smoothedPitch;
                    if (gCDValue > 0.0F) {
                        smoothedYaw = this.quantizedYaw + (float) Math.round(((double) this.smoothedYaw - this.quantizedYaw) / gCDValue) * gCDValue;
                        smoothedPitch = this.quantizedPitch + (float) Math.round((double) this.calculateNextFloatWithSignum(this.smoothedPitch - this.quantizedPitch, floatValue2, threadLocalRandom) / gCDValue) * gCDValue;
                        if (Math.abs(MathHelper.wrapDegrees(this.smoothedYaw - smoothedYaw)) > gCDValue * 3.0F) {
                            this.smoothedYaw = this.smoothedYaw + (smoothedYaw - this.smoothedYaw) * 0.5F * startValueStochasticRotationParameters.aimSmoothingScale.value;
                            this.yawVelocity = this.yawVelocity * (0.4F * startValueStochasticRotationParameters.recoveryDampingScale.value);
                        }
                        if (Math.abs(this.smoothedPitch - smoothedPitch) > gCDValue * 3.0F) {
                            this.smoothedPitch = this.smoothedPitch + (smoothedPitch - this.smoothedPitch) * 0.5F * startValueStochasticRotationParameters.aimSmoothingScale.value;
                            this.pitchVelocity = this.pitchVelocity * (0.4F * startValueStochasticRotationParameters.recoveryDampingScale.value);
                        }
                    }
                    smoothedPitch = MathHelper.clamp(smoothedPitch, -89.0F, 89.0F);
                    Rotation rotationValue = new Rotation(smoothedYaw, smoothedPitch);
                    float floatValue19 = (flag ? 360.0F : 45.0F) * startValueStochasticRotationParameters.yawRangeScale.value;
                    RotationStorage.update(rotationValue, floatValue19, floatValue19, floatValue19, floatValue19, 10, 1, false);
                    this.quantizedYaw = mc.player.getYaw();
                    this.quantizedPitch = mc.player.getPitch();
                    this.rotate = new Vec2f(this.quantizedYaw, this.quantizedPitch);
                }
            }
        }
    }

    private void updateVec2f(Vec2f rotation, boolean flag, ThreadLocalRandom threadLocalRandom) {
        if (this.initializationPending) {
            this.updateNextFloat(rotation, flag, threadLocalRandom);
        }
        this.phaseElapsedSeconds += 0.004166667F;
        if (this.accelerating == AccelerationPhase.ACCELERATING) {
            float clampedValue = MathHelper.clamp(this.phaseElapsedSeconds / this.accelerationDuration, 0.0F, 1.0F);
            this.motionStrength = this.cachedScalarForUpdateVec2f + (this.motionDuration - this.cachedScalarForUpdateVec2f) * this.calculateFloat(clampedValue, false);
            if (clampedValue == 1.0F) {
                this.motionStrength = this.motionDuration;
                this.updateVec2f(rotation);
            }
        } else {
            float clampedValueValue = MathHelper.clamp(this.phaseElapsedSeconds / this.decelerationDuration, 0.0F, 1.0F);
            this.motionStrength = this.cachedScalarForUpdateVec2f * (1.0F - this.calculateFloat(clampedValueValue, true));
            if (clampedValueValue == 1.0F || this.motionStrength == 0.0F) {
                this.motionStrength = 0.0F;
                this.initializationPending = true;
            }
        }
    }

    private void updateNextFloat(Vec2f rotation, boolean flag, ThreadLocalRandom threadLocalRandom) {
        this.phaseElapsedSeconds = 0.0F;
        this.cachedScalarForUpdateVec2f = this.motionStrength;
        this.accelerating = AccelerationPhase.ACCELERATING;
        this.initializationPending = false;
        this.recoveryPending = false;
        this.motionDuration = (flag ? 0.27F : 0.18F) * (0.92F + threadLocalRandom.nextFloat() * 0.16F) * (1.0F - Math.min(0.35F, (this.attackAura.getRotationStrength() - 1.0F) * 0.15F));
        this.accelerationDuration = flag ? 0.085F + threadLocalRandom.nextFloat() * 0.05F : 0.12F + threadLocalRandom.nextFloat() * 0.075F;
        this.decelerationDuration = flag ? 0.115F + threadLocalRandom.nextFloat() * 0.08F : 0.155F + threadLocalRandom.nextFloat() * 0.095F;
        this.updateGliding(rotation);
    }

    private void updateVec2f(Vec2f rotation) {
        this.phaseElapsedSeconds = 0.0F;
        this.cachedScalarForUpdateVec2f = this.motionStrength;
        this.accelerating = AccelerationPhase.BRAKING;
        this.recoveryPending = false;
        this.updateGliding(rotation);
    }

    private void updateGliding(Vec2f rotation) {
        float floatValue = this.calculateToDegreesWithAtan2(rotation);
        float floatValueValue = floatValue / (1.0F + floatValue);
        this.accelerationBias = (this.accelerating == AccelerationPhase.ACCELERATING ? 4.0F : -4.0F) * (2.0F * floatValueValue - 1.0F);
        this.groundMotionScale = mc.player.isGliding() ? 0.0F : 0.55F + floatValueValue * 0.45F;
    }

    private float calculateToDegreesWithAtan2(Vec2f rotation) {
        Box predictedBox = this.getPredictedBox(this.livingEntity);
        double doubleValue = Math.max(mc.player.getEyePos().distanceTo(predictedBox.getCenter()), 0.01);
        double doubleValueValue = Math.toDegrees(Math.atan2(Math.max(predictedBox.getLengthX(), predictedBox.getLengthZ()) * 0.5, doubleValue));
        double doubleValueResult = Math.toDegrees(Math.atan2(predictedBox.getLengthY() * 0.5, doubleValue));
        double doubleValueItem = MathHelper.wrapDegrees(rotation.x - this.quantizedYaw) / Math.max(doubleValueValue, 0.01);
        double doubleValueEntry = (rotation.y - this.quantizedPitch) / Math.max(doubleValueResult, 0.01);
        return (float) Math.hypot(doubleValueItem, doubleValueEntry);
    }

    private float calculateFloat(float floatValue, boolean flag) {
        double doubleValue = floatValue;
        double doubleValueValue = doubleValue * (1.0 - doubleValue);
        double doubleValueResult = doubleValue * doubleValue * doubleValue * (doubleValue * (doubleValue * 6.0 - 15.0) + 10.0);
        double doubleValueItem = flag ? 1.0 - doubleValue : doubleValue;
        double doubleValueEntry = 1.0 - doubleValueItem;
        double doubleValueCopy = doubleValueEntry * doubleValueEntry;
        double doubleValue2 = this.groundMotionScale * doubleValueItem * doubleValueCopy * doubleValueCopy * doubleValueCopy;
        double doubleValue3 = doubleValueResult + this.accelerationBias * doubleValueValue * doubleValueValue * doubleValueValue;
        return (float) MathHelper.clamp(doubleValue3 + (flag ? -doubleValue2 : doubleValue2), 0.0, 1.0);
    }

    private void updateUpdate(boolean flag) {
        this.updateYawWithPitch();
        Rotation rotation = new Rotation(this.quantizedYaw, this.quantizedPitch);
        float floatValue = flag ? 360.0F : 45.0F;
        RotationStorage.update(rotation, 0.0F, 0.0F, floatValue, floatValue, 10, 1, false);
        Vec2f rotationValue = new Vec2f(this.quantizedYaw, this.quantizedPitch);
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, rotationValue, (accessReceiver, accessValue) -> {
            accessReceiver.rotate = accessValue;
        });
    }

    private void updateYawWithPitch() {
        this.quantizedYaw = this.smoothedYaw = mc.player.getYaw();
        this.quantizedPitch = this.smoothedPitch = mc.player.getPitch();
        this.yawVelocity = this.pitchVelocity = this.previousYawVelocity = this.previousPitchVelocity = this.minimum = 0.0F;
        this.yawNoise = this.pitchNoise = 0.0F;
    }

    private Vec2f createVec2fFromSubtractWithCenter(LivingEntity entity) {
        Vec3d pos = entity.getPos();
        Box boundingBox = entity.getBoundingBox();
        Vec3d vec3d = RotationPointUtil.getMultipoint(entity, 128.0).subtract(pos);
        Vec3d position = new Vec3d(boundingBox.getCenter().x - pos.x + boundingBox.getLengthX() * this.startValueStochasticRotationParameters.targetOffsetXChannel.value, boundingBox.minY - pos.y + boundingBox.getLengthY() * this.startValueStochasticRotationParameters.targetHeightChannel.value, boundingBox.getCenter().z - pos.z + boundingBox.getLengthZ() * this.startValueStochasticRotationParameters.targetOffsetZChannel.value);
        vec3d = vec3d.lerp(position, this.startValueStochasticRotationParameters.aimPointBlendChannel.value);
        if (this.vec3d == null) {
            this.vec3d = vec3d;
        } else {
            double doubleValue = 1.0 - Math.exp(-0.004166667F / (0.05 * this.startValueStochasticRotationParameters.aimPointResponseChannel.value));
            this.vec3d = this.vec3d.lerp(vec3d, doubleValue);
        }
        Vec3d positionValue = pos.add(this.vec3d);
        positionValue = new Vec3d(MathHelper.clamp(positionValue.x, boundingBox.minX, boundingBox.maxX), MathHelper.clamp(positionValue.y, boundingBox.minY, boundingBox.maxY), MathHelper.clamp(positionValue.z, boundingBox.minZ, boundingBox.maxZ));
        this.vec3d = positionValue.subtract(pos);
        if (this.shouldUseElytraPredict(entity)) {
            positionValue = this.getPredictedPoint(entity, positionValue);
        }
        return RotationUtil.getRotations(positionValue);
    }

    private float calculateNextFloatWithSignum(float floatValue, float floatValueValue, ThreadLocalRandom threadLocalRandom) {
        if (floatValueValue <= 0.0F) {
            return 0.0F;
        } else {
            float magnitude = Math.abs(floatValue);
            float floatValueResult = floatValueValue * (0.85F + threadLocalRandom.nextFloat() * 0.3F) * this.startValueStochasticRotationParameters.motionPhaseDurationScale.value;
            float clampedValue = MathHelper.clamp((0.5F + threadLocalRandom.nextFloat() * 0.15F) * this.startValueStochasticRotationParameters.motionResponseRatioScale.value, 0.3F, 0.85F);
            float floatValueItem = floatValueResult * clampedValue;
            if (magnitude <= floatValueItem) {
                return floatValue;
            } else {
                float floatValueEntry = magnitude - floatValueItem;
                float floatValueCopy = floatValueResult - floatValueItem;
                return Math.signum(floatValue) * (floatValueItem + floatValueCopy * (floatValueEntry / (floatValueEntry + floatValueCopy * this.startValueStochasticRotationParameters.responseCurvatureScale.value)) + Math.min(floatValueEntry * 0.1F * this.startValueStochasticRotationParameters.responseOvershootScale.value, floatValueCopy * 0.8F));
            }
        }
    }
}
