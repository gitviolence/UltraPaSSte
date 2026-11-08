package brova.toshiba.client.utils.neuro;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.rotate.RotationUtil;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class CombatContextFeatures implements MinecraftAccess {

    private CombatContextFeatures() {
    }

    public static float[] capture(LivingEntity entity, Box box, Vec3d position, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d positionValue = position == null ? defaultPoint(box) : position;
        Vec3d positionResult = positionValue.subtract(eyePos);
        Vec2f rotations = RotationUtil.getRotations(positionValue);
        Vec3d positionItem = Vec3d.fromPolar(floatValueValue, floatValue);
        Optional<Vec3d> positionOptional = box.raycast(eyePos, eyePos.add(positionItem.multiply(8.0)));
        Vec3d positionEntry = eyePos.add(positionItem.multiply(Math.max(0.0, box.getCenter().subtract(eyePos).dotProduct(positionItem))));
        Vec3d positionCopy = positionOptional.orElseGet(() -> createPositionFromPosition(positionEntry, box));
        float[] values = createFloatFromToRadiansWithSin(entity.getVelocity(), floatValue);
        float[] valuesValue = createFloatFromToRadiansWithSin(mc.player.getVelocity(), floatValue);
        float[] valuesResult = createFloatFromToRadiansWithSin(positionResult, floatValue);
        float length = (float) positionResult.length();
        float floatValueEntry = (float) Math.toDegrees(Math.atan2(Math.hypot(box.getLengthX(), box.getLengthZ()) * 0.5, Math.max((double) length, 0.01)));
        float floatValueCopy = (float) Math.toDegrees(Math.atan2(box.getLengthY() * 0.5, Math.max((double) length, 0.01)));
        float floatValue2 = CombatMotionSchema.rotationGcd(mc.options.getMouseSensitivity().getValue().floatValue());
        return new float[] { MathHelper.wrapDegrees(rotations.x - floatValue), rotations.y - floatValueValue, length, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, values[0], values[1], values[2], valuesValue[0], valuesValue[1], 0.0F, floatValueItem, mc.player.getAttackCooldownProgress(0.0F), calculateDouble(positionCopy.x, box.minX, box.getLengthX()) - 0.5F, calculateDouble(positionCopy.y, box.minY, box.getLengthY()) - 0.5F, calculateDouble(positionCopy.z, box.minZ, box.getLengthZ()) - 0.5F, !positionOptional.isPresent() && !box.contains(eyePos) ? 0.0F : 1.0F, floatValueResult, valuesResult[0], valuesResult[1], valuesResult[2], values[0] - valuesValue[0], values[1] - valuesValue[1], values[2] - valuesValue[2], valuesValue[2], floatValueEntry, floatValueCopy, (float) eyePos.distanceTo(createPositionFromPosition(eyePos, box)), mc.player.isOnGround() ? 1.0F : 0.0F, entity.isOnGround() ? 1.0F : 0.0F, mc.player.isGliding() ? 1.0F : 0.0F, entity.isGliding() ? 1.0F : 0.0F, mc.player.hurtTime / 10.0F, entity.hurtTime / 10.0F, floatValue2 };
    }

    public static Vec3d defaultPoint(Box box) {
        double doubleValue = box.getLengthY() * 0.1;
        double clampedValue = MathHelper.clamp(mc.player.getEyeY(), box.minY + doubleValue, box.maxY - doubleValue);
        return new Vec3d((box.minX + box.maxX) * 0.5, clampedValue, (box.minZ + box.maxZ) * 0.5);
    }

    public static PitchStepBounds pitchBounds(Box box, Vec3d position, float floatValue) {
        if (position != null && !box.contains(position)) {
            return null;
        } else {
            Vec3d eyePos = mc.player.getEyePos();
            double doubleValue = Math.max(0.0, Math.max(box.minX - eyePos.x, eyePos.x - box.maxX));
            double doubleValueValue = Math.max(0.0, Math.max(box.minZ - eyePos.z, eyePos.z - box.maxZ));
            double doubleValueResult = Math.max(Math.abs(box.minX - eyePos.x), Math.abs(box.maxX - eyePos.x));
            double doubleValueItem = Math.max(Math.abs(box.minZ - eyePos.z), Math.abs(box.maxZ - eyePos.z));
            return PitchStepBounds.fromProjection(Math.hypot(doubleValue, doubleValueValue), Math.hypot(doubleValueResult, doubleValueItem), box.minY - eyePos.y, box.maxY - eyePos.y, floatValue);
        }
    }

    private static float[] createFloatFromToRadiansWithSin(Vec3d position, float floatValue) {
        double doubleValue = Math.toRadians(floatValue);
        double doubleValueValue = Math.sin(doubleValue);
        double doubleValueResult = Math.cos(doubleValue);
        return new float[] { (float) (-position.x * doubleValueValue + position.z * doubleValueResult), (float) (position.x * doubleValueResult + position.z * doubleValueValue), (float) position.y };
    }

    private static Vec3d createPositionFromPosition(Vec3d position, Box box) {
        return new Vec3d(MathHelper.clamp(position.x, box.minX, box.maxX), MathHelper.clamp(position.y, box.minY, box.maxY), MathHelper.clamp(position.z, box.minZ, box.maxZ));
    }

    private static float calculateDouble(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return MathHelper.clamp((float) ((doubleValue - doubleValueValue) / Math.max(doubleValueResult, 1.0E-6)), 0.0F, 1.0F);
    }
}
