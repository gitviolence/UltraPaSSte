package brova.toshiba.client.utils.rotate.modes;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import java.security.SecureRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ReallyWorldRotation extends RotationModeBase implements MinecraftAccess {

    private final AttackAura attackAura;

    private final SecureRandom secureRandom = new SecureRandom();

    private float yaw = 0.0F;

    private float pitch = 0.0F;

    private boolean stateFlag = false;

    public ReallyWorldRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public void reset() {
        this.stateFlag = false;
        if (mc.player != null) {
            this.yaw = mc.player.getYaw();
            this.pitch = mc.player.getPitch();
        }
    }

    @Override
    public void updateRotations(LivingEntity entity) {
        if (mc.player != null && entity != null) {
            if (!this.stateFlag) {
                this.yaw = mc.player.getYaw();
                this.pitch = mc.player.getPitch();
                this.stateFlag = true;
            }
            Vec2f rotation = this.createVec2fFromToDegreesWithAtan2(entity);
            float floatValue = MathHelper.wrapDegrees(rotation.x - this.yaw);
            float floatValueValue = rotation.y - this.pitch;
            float floatValueResult = (float) Math.hypot(floatValue, floatValueValue);
            boolean attackCooldownProgressWithWhiteRiseTicksToAttack = this.isAttackCooldownProgressWithWhiteRiseTicksToAttack(entity);
            float floatValueItem = attackCooldownProgressWithWhiteRiseTicksToAttack ? 1.0F : this.secureRandom.nextFloat() * 0.5F;
            float floatValueEntry = attackCooldownProgressWithWhiteRiseTicksToAttack ? 360.0F : 100.0F;
            float floatValueCopy = 180.0F;
            float floatValue2 = floatValueResult > 1.0E-4F ? Math.abs(floatValue / floatValueResult) * floatValueEntry : floatValueEntry;
            float floatValue3 = floatValueResult > 1.0E-4F ? Math.abs(floatValueValue / floatValueResult) * floatValueCopy : floatValueCopy;
            float clampedValue = MathHelper.clamp(floatValue, -floatValue2, floatValue2);
            float clampedValueValue = MathHelper.clamp(floatValueValue, -floatValue3, floatValue3);
            float floatValue4 = this.yaw + clampedValue * floatValueItem;
            float floatValue5 = this.pitch + clampedValueValue * floatValueItem;
            long currentTimeMillis = System.currentTimeMillis();
            float floatValue6 = 15.0F + this.secureRandom.nextFloat() * 130.0F;
            float floatValue7 = 15.0F + this.secureRandom.nextFloat() * 130.0F;
            float floatValue8 = this.secureRandom.nextFloat() * 3.0F;
            float floatValue9 = 0.5F + this.secureRandom.nextFloat() * 1.5F;
            float floatValue10 = floatValue8 * (float) Math.sin((double) currentTimeMillis / floatValue6);
            float floatValue11 = floatValue9 * (float) Math.sin((double) currentTimeMillis / floatValue7);
            floatValue4 += floatValue10;
            floatValue5 += floatValue11;
            float floatValue12 = MathHelper.wrapDegrees(floatValue4 - this.yaw);
            float floatValue13 = floatValue5 - this.pitch;
            float floatValue14 = this.calculateMouseSensitivityStep();
            floatValue12 = Math.round(floatValue12 / floatValue14) * floatValue14;
            floatValue13 = Math.round(floatValue13 / floatValue14) * floatValue14;
            float yaw = this.yaw + floatValue12;
            float pitch = MathHelper.clamp(this.pitch + floatValue13, -89.0F, 89.0F);
            this.attackAura.setRotationState(new Vec2f(this.yaw, this.pitch), rotation);
            this.yaw = yaw;
            this.pitch = pitch;
            RotationStorage.update(new Rotation(yaw, pitch), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, false);
            this.rotate = new Vec2f(yaw, pitch);
        }
    }

    private Vec2f createVec2fFromToDegreesWithAtan2(LivingEntity entity) {
        Vec3d position = this.attackAura.resolveRotationPoint(null);
        if (position != null) {
            Vec3d positionValue = position.subtract(mc.player.getEyePos());
            float floatValue = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(positionValue.z, positionValue.x)) + -90.0);
            float floatValueValue = (float) (-Math.toDegrees(Math.atan2(positionValue.y, Math.hypot(positionValue.x, positionValue.z))));
            return new Vec2f(floatValue, floatValueValue);
        } else if (!mc.player.isGliding()) {
            Vec3d positionResult = entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0).subtract(mc.player.getEyePos());
            float floatValueResult = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(positionResult.z, positionResult.x)) + -90.0);
            float floatValueItem = (float) (-Math.toDegrees(Math.atan2(positionResult.y, Math.hypot(positionResult.x, positionResult.z))));
            return new Vec2f(floatValueResult, floatValueItem);
        } else {
            Vec3d positionItem = Vec3d.fromPolar(entity.getLerpTargetPitch(), entity.getLerpTargetYaw());
            Vec3d rotationVector = entity.getRotationVector();
            Vec3d positionEntry = entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0).subtract(mc.player.getEyePos());
            Vec3d interpolatedValue = positionItem.normalize().lerp(rotationVector, positionItem.length());
            if (entity.isGliding() && ModuleRegistry.elytraTarget.isEnable()) {
                if (ModuleRegistry.elytraTarget.isVanillaMode()) {
                    positionEntry = ModuleRegistry.elytraTarget.getPredictedPoint(entity, entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0)).subtract(mc.player.getEyePos());
                } else {
                    positionEntry = positionEntry.add(interpolatedValue.normalize().multiply(ModuleRegistry.elytraTarget.getPredictionMultiplier(entity)));
                }
            }
            float floatValueEntry = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(positionEntry.z, positionEntry.x)) + -90.0);
            float floatValueCopy = (float) (-Math.toDegrees(Math.atan2(positionEntry.y, Math.hypot(positionEntry.x, positionEntry.z))));
            return new Vec2f(floatValueEntry, floatValueCopy);
        }
    }

    private boolean isAttackCooldownProgressWithWhiteRiseTicksToAttack(LivingEntity entity) {
        return entity == null ? false : mc.player.getAttackCooldownProgress(1.5F) >= 0.88F && this.attackAura.getWhiteRiseTicksToAttack() <= 1;
    }

    private float calculateMouseSensitivityStep() {
        double value = mc.options.getMouseSensitivity().getValue();
        double doubleValue = value * 0.6 + 0.2;
        return (float) (doubleValue * doubleValue * doubleValue * 1.2 * 0.15);
    }
}
