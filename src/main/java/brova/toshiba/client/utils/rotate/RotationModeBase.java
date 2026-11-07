package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public abstract class RotationModeBase implements MinecraftAccess {

    public Vec2f rotate = Vec2f.ZERO;

    public abstract void updateRotations(LivingEntity entity);

    public static Vec2f correctRotation(float floatValue, float floatValueValue) {
        if ((floatValue != -90.0F || floatValueValue != 90.0F) && floatValue != -180.0F) {
            float gCD = MouseGcdUtil.getGCD();
            floatValue -= floatValue % gCD;
            floatValueValue -= floatValueValue % gCD;
            return new Vec2f(floatValue, floatValueValue);
        } else {
            return new Vec2f(mc.player.getYaw(), mc.player.getPitch());
        }
    }

    public boolean shouldUseElytraPredict(LivingEntity entity) {
        return mc.player != null && entity != null && mc.player.isGliding() && entity.isGliding() && ModuleRegistry.elytraTarget != null && ModuleRegistry.elytraTarget.isEnable();
    }

    public int getElytraPredictTicks() {
        return ModuleRegistry.elytraTarget == null ? 0 : Math.max(0, ModuleRegistry.elytraTarget.forward.getValue().intValue());
    }

    public Vec3d getPredictedPoint(LivingEntity entity, Vec3d position) {
        return !this.shouldUseElytraPredict(entity) ? position : ModuleRegistry.elytraTarget.getPredictedPoint(entity, position);
    }

    public Box getPredictedBox(LivingEntity entity) {
        Box boundingBox = entity.getBoundingBox();
        if (!this.shouldUseElytraPredict(entity)) {
            return boundingBox;
        } else {
            Vec3d center = boundingBox.getCenter();
            Vec3d predictedPoint = this.getPredictedPoint(entity, center);
            return boundingBox.offset(predictedPoint.subtract(center));
        }
    }
}
