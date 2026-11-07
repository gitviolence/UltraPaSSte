package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class RotationUtil implements MinecraftAccess {

    public static HitResult rayTrace(double doubleValue, float floatValue, float floatValueValue) {
        Vec3d cameraPosVec = mc.player.getCameraPosVec(1.0F);
        Vec3d rotationVector = getRotationVector(floatValueValue, floatValue);
        Vec3d position = cameraPosVec.add(rotationVector.x * doubleValue, rotationVector.y * doubleValue, rotationVector.z * doubleValue);
        return mc.world.raycast(new RaycastContext(cameraPosVec, position, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
    }

    public static Vec3d getBestVector(Entity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        Box boundingBox = entity.getBoundingBox();
        double doubleValue = 0.1;
        Vec3d position = null;
        double MAX_VALUE = Double.MAX_VALUE;
        for (double minX = boundingBox.minX; minX <= boundingBox.maxX; minX += doubleValue) {
            for (double minY = boundingBox.minY; minY <= boundingBox.maxY; minY += doubleValue) {
                for (double minZ = boundingBox.minZ; minZ <= boundingBox.maxZ; minZ += doubleValue) {
                    Vec3d positionValue = new Vec3d(minX, minY, minZ);
                    double doubleValueValue = eyePos.distanceTo(positionValue);
                    if (doubleValueValue < MAX_VALUE) {
                        MAX_VALUE = doubleValueValue;
                        position = positionValue;
                    }
                }
            }
        }
        return position;
    }

    public static Rotation fromVec3d(Vec3d position) {
        return new Rotation((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(position.z, position.x)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(-Math.atan2(position.y, Math.hypot(position.x, position.z)))));
    }

    @NotNull
    public static Vec3d getRotationVector(float floatValue, float floatValueValue) {
        return new Vec3d(MathHelper.sin(-floatValueValue * (float) (Math.PI / 180.0)) * MathHelper.cos(floatValue * (float) (Math.PI / 180.0)), -MathHelper.sin(floatValue * (float) (Math.PI / 180.0)), MathHelper.cos(-floatValueValue * (float) (Math.PI / 180.0)) * MathHelper.cos(floatValue * (float) (Math.PI / 180.0)));
    }

    public static Vec2f getRotations(Entity entity) {
        return getRotations(entity.getX(), entity.getY(), entity.getZ());
    }

    public static Vec2f getRotations(Vec3d position) {
        return getRotations(position.x, position.y, position.z);
    }

    public static Vec2f getRotations(double doubleValue, double doubleValueValue, double doubleValueResult) {
        double doubleValueItem = doubleValue - mc.player.getX();
        double doubleValueEntry = doubleValueValue - mc.player.getEyeY();
        double doubleValueCopy = doubleValueResult - mc.player.getZ();
        double squareRoot = MathHelper.sqrt((float) (doubleValueItem * doubleValueItem + doubleValueCopy * doubleValueCopy));
        float floatValue = (float) (MathHelper.atan2(doubleValueCopy, doubleValueItem) * (180.0 / Math.PI) - 90.0);
        float floatValueValue = (float) (-MathHelper.atan2(doubleValueEntry, squareRoot) * (180.0 / Math.PI));
        return new Vec2f(floatValue, floatValueValue);
    }

    public static float[] getRotations(Direction direction) {
        return switch(direction) {
            case DOWN ->
                new float[] { mc.player.getYaw(), 90.0F };
            case UP ->
                new float[] { mc.player.getYaw(), -90.0F };
            case NORTH ->
                new float[] { 180.0F, mc.player.getPitch() };
            case SOUTH ->
                new float[] { 0.0F, mc.player.getPitch() };
            case WEST ->
                new float[] { 90.0F, mc.player.getPitch() };
            case EAST ->
                new float[] { -90.0F, mc.player.getPitch() };
        };
    }

    public static float[] correctRotation(float[] values) {
        values[0] -= values[0] % MouseGcdUtil.getGCDValue();
        values[1] -= values[1] % MouseGcdUtil.getGCDValue();
        return new float[] { values[0], values[1] };
    }

    public static float getFixRotate(float floatValue) {
        return getDeltaMouse(floatValue) * MouseGcdUtil.getGCDValue();
    }

    public static float getDeltaMouse(float floatValue) {
        return Math.round(floatValue / MouseGcdUtil.getGCDValue());
    }

    public static Rotation calculate(Vec3d position) {
        return mc.player == null ? new Rotation(0.0F, 0.0F) : calculate(mc.player.getEyePos(), position);
    }

    public static Rotation calculate(Entity entity) {
        return entity == null ? new Rotation(0.0F, 0.0F) : calculate(entity.getPos().add(0.0, entity.getEyeHeight(entity.getPose()), 0.0));
    }

    public static Rotation calculate(Vec3d position, Vec3d positionValue) {
        Vec3d positionResult = positionValue.subtract(position);
        double doubleValue = Math.hypot(positionResult.x, positionResult.z);
        float floatValue = (float) (MathHelper.atan2(positionResult.z, positionResult.x) * 180.0 / Math.PI) - 90.0F;
        float floatValueValue = (float) (-(MathHelper.atan2(positionResult.y, doubleValue) * 180.0 / Math.PI));
        return new Rotation(MathHelper.wrapDegrees(floatValue), MathHelper.clamp(floatValueValue, -90.0F, 90.0F));
    }

    public static float calculateFov(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        Vector3f directionVector = getDirectionVector(floatValue, floatValueValue);
        Vector3f directionVectorValue = getDirectionVector(floatValueResult, floatValueItem);
        float floatValueEntry = directionVector.dot(directionVectorValue);
        floatValueEntry = MathHelper.clamp(floatValueEntry, -1.0F, 1.0F);
        return (float) Math.toDegrees((float) Math.acos(floatValueEntry));
    }

    public static Vector3f getDirectionVector(float floatValue, float floatValueValue) {
        float floatValueResult = (float) Math.toRadians(floatValue);
        float floatValueItem = (float) Math.toRadians(floatValueValue);
        float floatValueEntry = -MathHelper.cos(floatValueItem) * MathHelper.sin(floatValueResult);
        float floatValueCopy = -MathHelper.sin(floatValueItem);
        float floatValue2 = MathHelper.cos(floatValueItem) * MathHelper.cos(floatValueResult);
        return new Vector3f(floatValueEntry, floatValueCopy, floatValue2);
    }

    private RotationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
