package brova.toshiba.client.utils.rotate.modes;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.rotate.CloseRangeRotationMultiplier;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import brova.toshiba.client.utils.rotate.RotationPointUtil;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.world.EntityRaycastUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SpookyTimeRotation extends RotationModeBase implements MinecraftAccess {

    private final AttackAura attackAura;

    private final CloseRangeRotationMultiplier closeRangeRotationMultiplier = new CloseRangeRotationMultiplier();

    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    private LivingEntity livingEntity;

    private float yaw;

    private float pitch;

    private float x;

    private float y;

    private float yawMomentum;

    private float pitchMomentum;

    private float rotationStrength;

    private boolean decelerating;

    private boolean rotationDirection;

    private int cachedCount;

    private long lastUpdateTimestamp;

    private double sin;

    public SpookyTimeRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public void reset() {
        this.livingEntity = null;
        this.rotationDirection = false;
        this.rotationStrength = 0.0F;
        this.decelerating = false;
        this.cachedCount = 0;
        this.lastUpdateTimestamp = 0L;
        this.yawMomentum = 0.0F;
        this.pitchMomentum = 0.0F;
        this.sin = this.threadLocalRandom.nextDouble(Math.PI * 2);
        if (mc.player != null) {
            this.yaw = mc.player.getYaw();
            this.pitch = mc.player.getPitch();
        }
        this.rotate = new Vec2f(this.yaw, this.pitch);
    }

    public void onAttack() {
        long lastUpdateTimestamp = System.nanoTime();
        if (this.lastUpdateTimestamp == 0L || lastUpdateTimestamp - this.lastUpdateTimestamp >= 150000000L) {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            this.cachedCount = 1;
            this.decelerating = false;
            this.rotationStrength = Math.max(this.rotationStrength, 0.12F);
            this.yawMomentum *= 0.88F;
            this.pitchMomentum *= 0.86F;
        }
    }

    public void returnToCamera() {
        if (RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(RotationStorage.instance, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((RotationStorage) accessReceiver).stopRotation();
            });
        }
        this.reset();
    }

    @Override
    public void updateRotations(LivingEntity entity) {
        if (mc.player != null && entity != null) {
            Vec2f rotation = this.createVec2fFromResolveRotationPointWithMultipoint(entity);
            if (!this.rotationDirection || this.livingEntity != entity) {
                this.updateYawWithPitch(entity, rotation);
            }
            Box box = entity.getBoundingBox().expand(0.05);
            Vec3d eyePos = mc.player.getEyePos();
            boolean flag = mc.player.isGliding() && box.raycast(new Vec3d(mc.player.prevX, mc.player.prevY, mc.player.prevZ), mc.player.getPos()).isPresent();
            boolean flagValue = flag || box.contains(eyePos) || mc.player.getBoundingBox().intersects(box);
            float floatValue = this.calculateFloat(1.0F - MathHelper.clamp((float) (this.calculateSqrt(eyePos, box) / 0.9), 0.0F, 1.0F));
            if (flagValue) {
                floatValue = 1.0F;
            }
            rotation = this.createVec2fFromLerpWithSignum(rotation, floatValue);
            float clampedValue = MathHelper.clamp(MathHelper.wrapDegrees(rotation.x - this.yaw), -45.0F, 45.0F);
            float clampedValueValue = MathHelper.clamp(rotation.y - this.pitch, -30.0F, 30.0F);
            float magnitude = Math.abs(clampedValue);
            float magnitudeValue = Math.abs(clampedValueValue);
            this.updateFloat(magnitude);
            float clampedValueResult = MathHelper.clamp((magnitude + magnitudeValue) / 30.0F, 0.15F, 1.0F);
            float floatValueValue = Math.max(this.rotationStrength, 0.0F) * clampedValueResult;
            if (floatValue > 0.0F) {
                floatValueValue *= MathHelper.lerp(floatValue, 1.0F, 0.76F);
            }
            if (mc.player.hurtTime > 0) {
                floatValueValue *= 0.6F;
            }
            if (this.cachedCount > 0) {
                this.cachedCount--;
                floatValueValue *= 0.65F;
            }
            float floatValueResult = this.calculateFloat(MathHelper.clamp(Math.max(this.rotationStrength, 0.0F) / 0.03F, 0.0F, 1.0F));
            boolean flagResult = mc.player.getVelocity().horizontalLengthSquared() > 0.003 && entity.getVelocity().horizontalLengthSquared() > 0.003;
            boolean flagItem = EntityRaycastUtil.rayTrace(mc.player.getRotationVector(this.pitch, this.yaw), 6.0, box);
            if (flagItem && magnitude < 3.5F && magnitudeValue < 3.0F) {
                this.yawMomentum = this.yawMomentum * MathHelper.lerp(floatValueResult, 0.72F, 0.987F);
                this.pitchMomentum *= 0.985F;
            } else {
                float floatValueItem = flagResult ? 0.32F : 0.25F;
                this.yawMomentum = this.yawMomentum * floatValueItem + (clampedValue + (float) this.threadLocalRandom.nextGaussian() * 0.055F) * floatValueValue * (1.0F - floatValueItem) * 1.2F;
                this.pitchMomentum = this.pitchMomentum * floatValueItem + (clampedValueValue + (float) this.threadLocalRandom.nextGaussian() * 0.028F) * floatValueValue * (1.0F - floatValueItem);
            }
            this.sin++;
            float floatValueEntry = this.rotationStrength <= 0.0F ? 0.65F : this.closeRangeRotationMultiplier.getMultiplier(entity);
            float floatValueCopy = this.yaw + this.yawMomentum * floatValueResult + (float) Math.sin(this.sin) * 0.1F * floatValueEntry * floatValueResult;
            float floatValue2 = this.pitch + this.pitchMomentum * 0.85F;
            float floatValue3 = flagResult ? 10.8F : 15.6F;
            if (magnitude < 10.0F) {
                floatValue3 = Math.min(floatValue3, magnitude * 1.32F + 1.2F);
            }
            floatValueCopy = this.yaw + MathHelper.clamp(floatValueCopy - this.yaw, -floatValue3, floatValue3);
            floatValue2 = this.pitch + MathHelper.clamp(floatValue2 - this.pitch, -floatValue3 * 0.14F, floatValue3 * 0.14F);
            if (floatValueResult == 0.0F) {
                floatValueCopy = this.yaw;
            }
            this.apply(this.createVec2fFromGCDValueWithWrapDegrees(floatValueCopy, floatValue2), rotation);
        } else {
            this.reset();
        }
    }

    private Vec2f createVec2fFromResolveRotationPointWithMultipoint(LivingEntity entity) {
        Vec3d position = this.attackAura.resolveRotationPoint(null);
        if (position == null) {
            position = RotationPointUtil.getMultipoint(entity, 128.0);
        }
        if (this.shouldUseElytraPredict(entity)) {
            position = this.getPredictedPoint(entity, position);
        }
        return RotationUtil.getRotations(position);
    }

    private Vec2f createVec2fFromLerpWithSignum(Vec2f rotation, float floatValue) {
        if (floatValue <= 0.0F) {
            this.x = rotation.x;
            this.y = rotation.y;
            return rotation;
        } else {
            float floatValueValue = MathHelper.wrapDegrees(rotation.x - this.x);
            float floatValueResult = rotation.y - this.y;
            float interpolatedValue = MathHelper.lerp(floatValue, 8.0F, 2.15F);
            float interpolatedValueValue = MathHelper.lerp(floatValue, 2.8F, 0.52F);
            float floatValueItem = floatValueValue * MathHelper.lerp(floatValue, 0.74F, 0.39F) + Math.signum(floatValueValue) * Math.min(floatValueValue * floatValueValue * 0.0025F, 0.45F);
            float floatValueEntry = floatValueResult * MathHelper.lerp(floatValue, 0.66F, 0.29F) + Math.signum(floatValueResult) * Math.min(floatValueResult * floatValueResult * 0.003F, 0.16F);
            this.x = this.x + MathHelper.clamp(floatValueItem, -interpolatedValue, interpolatedValue);
            this.y = this.y + MathHelper.clamp(floatValueEntry, -interpolatedValueValue, interpolatedValueValue);
            return new Vec2f(this.x, this.y);
        }
    }

    private void updateFloat(float floatValue) {
        if (this.decelerating) {
            this.rotationStrength -= floatValue > 30.0F ? 0.012F : 0.022F;
            if (this.rotationStrength <= 0.0F) {
                this.rotationStrength = 0.0F;
                this.decelerating = false;
            }
        } else {
            this.rotationStrength = Math.min(0.24F, this.rotationStrength + 0.003F);
            if (this.rotationStrength == 0.24F) {
                this.decelerating = true;
            }
        }
    }

    private void apply(Vec2f rotation, Vec2f rotationValue) {
        RotationStorage.update(new Rotation(rotation.x, rotation.y), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, mc.player.isGliding());
        this.yaw = mc.player.getYaw();
        this.pitch = mc.player.getPitch();
        this.rotate = new Vec2f(this.yaw, this.pitch);
        this.attackAura.setCurrentRotations(this.rotate);
        this.attackAura.setTargetRotations(rotationValue);
    }

    private void updateYawWithPitch(LivingEntity livingEntity, Vec2f rotation) {
        this.livingEntity = livingEntity;
        this.yaw = mc.player.getYaw();
        this.pitch = mc.player.getPitch();
        this.x = rotation.x;
        this.y = rotation.y;
        this.yawMomentum = 0.0F;
        this.pitchMomentum = 0.0F;
        this.rotationDirection = true;
    }

    private Vec2f createVec2fFromGCDValueWithWrapDegrees(float floatValue, float floatValueValue) {
        float gCDValue = MouseGcdUtil.getGCDValue();
        if (gCDValue <= 0.0F) {
            return new Vec2f(floatValue, floatValueValue);
        } else {
            float floatValueResult = Math.round(MathHelper.wrapDegrees(floatValue - this.yaw) / gCDValue) * gCDValue;
            float floatValueItem = Math.round((floatValueValue - this.pitch) / gCDValue) * gCDValue;
            return new Vec2f(this.yaw + floatValueResult, MathHelper.clamp(this.pitch + floatValueItem, -90.0F, 90.0F));
        }
    }

    private double calculateSqrt(Vec3d position, Box box) {
        double doubleValue = position.x - MathHelper.clamp(position.x, box.minX, box.maxX);
        double doubleValueValue = position.y - MathHelper.clamp(position.y, box.minY, box.maxY);
        double doubleValueResult = position.z - MathHelper.clamp(position.z, box.minZ, box.maxZ);
        return Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult);
    }

    private float calculateFloat(float floatValue) {
        float clampedValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
        return clampedValue * clampedValue * (3.0F - 2.0F * clampedValue);
    }
}
