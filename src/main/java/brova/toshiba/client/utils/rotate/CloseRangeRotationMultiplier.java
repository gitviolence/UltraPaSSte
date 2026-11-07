package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class CloseRangeRotationMultiplier implements MinecraftAccess {

    public float getMultiplier(LivingEntity entity) {
        return this.calculateBoundingBoxWithEyePos(entity) <= 0.9 ? 0.93F : 1.0F;
    }

    private double calculateBoundingBoxWithEyePos(LivingEntity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        double clampedValue = MathHelper.clamp(eyePos.x, entity.getBoundingBox().minX, entity.getBoundingBox().maxX);
        double clampedValueValue = MathHelper.clamp(eyePos.z, entity.getBoundingBox().minZ, entity.getBoundingBox().maxZ);
        return Math.hypot(eyePos.x - clampedValue, eyePos.z - clampedValueValue);
    }
}
