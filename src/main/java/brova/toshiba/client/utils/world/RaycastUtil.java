package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.rotate.Rotation;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class RaycastUtil implements MinecraftAccess {

    public static BlockHitResult raycast(double doubleValue, Rotation rotation, boolean flag) {
        return mc.player == null ? null : raycast(mc.player.getCameraPosVec(1.0F), doubleValue, rotation, flag);
    }

    public static BlockHitResult raycast(Vec3d position, double doubleValue, Rotation rotation, boolean flag) {
        Entity cameraEntity = mc.cameraEntity;
        if (cameraEntity != null && mc.world != null) {
            Vec3d positionValue = rotation.toVector();
            Vec3d positionResult = position.add(positionValue.x * doubleValue, positionValue.y * doubleValue, positionValue.z * doubleValue);
            FluidHandling fluidHandling = flag ? FluidHandling.ANY : FluidHandling.NONE;
            RaycastContext raycastContext = new RaycastContext(position, positionResult, ShapeType.OUTLINE, fluidHandling, cameraEntity);
            return mc.world.raycast(raycastContext);
        } else {
            return null;
        }
    }

    public static BlockHitResult raycast(Vec3d position, Vec3d positionValue, ShapeType shapeType) {
        return mc.world == null ? null : mc.world.raycast(new RaycastContext(position, positionValue, shapeType, FluidHandling.NONE, mc.player));
    }

    public static boolean rayTrace(Rotation rotation, double doubleValue, Box box) {
        if (mc.player == null) {
            return false;
        } else {
            Vec3d eyePos = mc.player.getEyePos();
            return box.contains(eyePos) || box.raycast(eyePos, eyePos.add(rotation.toVector().multiply(doubleValue))).isPresent();
        }
    }

    public static boolean rayTrace(Vec3d position, double doubleValue, Box box) {
        if (mc.player == null) {
            return false;
        } else {
            Vec3d eyePos = mc.player.getEyePos();
            return box.contains(eyePos) || box.raycast(eyePos, eyePos.add(position.multiply(doubleValue))).isPresent();
        }
    }

    public static BlockHitResult raycastBlock(BlockPos position, double doubleValue) {
        return mc.player != null && mc.world != null ? raycastBlock(mc.player, position, doubleValue) : null;
    }

    public static BlockHitResult raycastBlock(Rotation rotation, BlockPos position, double doubleValue) {
        return mc.player != null && mc.world != null ? raycastBlock(rotation, mc.player, position, doubleValue) : null;
    }

    public static BlockHitResult raycastBlock(PlayerEntity player, BlockPos position, double doubleValue) {
        Vec3d eyePos = player.getEyePos();
        Vec3d rotationVec = player.getRotationVec(1.0F);
        Vec3d positionValue = eyePos.add(rotationVec.multiply(doubleValue));
        Box box = new Box(position);
        var optional = box.raycast(eyePos, positionValue);
        if (optional.isEmpty()) {
            return null;
        } else {
            Vec3d positionResult = optional.get();
            Direction direction = createDirectionFromPosition(positionResult, position);
            return new BlockHitResult(positionResult, direction, position, false);
        }
    }

    public static BlockHitResult raycastBlock(Rotation rotation, PlayerEntity player, BlockPos position, double doubleValue) {
        Vec3d eyePos = player.getEyePos();
        Vec3d rotationVector = player.getRotationVector(rotation.getPitch(), rotation.getYaw());
        Vec3d positionValue = eyePos.add(rotationVector.multiply(doubleValue));
        Box box = new Box(position);
        var optional = box.raycast(eyePos, positionValue);
        if (optional.isEmpty()) {
            return null;
        } else {
            Vec3d positionResult = optional.get();
            Direction direction = createDirectionFromPosition(positionResult, position);
            return new BlockHitResult(positionResult, direction, position, false);
        }
    }

    public static boolean isLookingAtEntity(Entity entity, double doubleValue) {
        if (mc.player != null && entity != null) {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d rotationVec = mc.player.getRotationVec(1.0F);
            Vec3d position = eyePos.add(rotationVec.multiply(doubleValue));
            Box boundingBox = entity.getBoundingBox();
            return boundingBox.raycast(eyePos, position).isPresent() || boundingBox.contains(eyePos);
        } else {
            return false;
        }
    }

    public static boolean isLookingAtEntity(Rotation rotation, Entity entity, double doubleValue) {
        if (mc.player != null && entity != null) {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d rotationVector = mc.player.getRotationVector(rotation.getPitch(), rotation.getYaw());
            Vec3d position = eyePos.add(rotationVector.multiply(doubleValue));
            Box boundingBox = entity.getBoundingBox();
            return boundingBox.raycast(eyePos, position).isPresent() || boundingBox.contains(eyePos);
        } else {
            return false;
        }
    }

    public static boolean canReachBox(Box box, double doubleValue) {
        if (mc.player == null) {
            return false;
        } else {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d closestPointOnBox = getClosestPointOnBox(eyePos, box);
            return eyePos.squaredDistanceTo(closestPointOnBox) <= doubleValue * doubleValue;
        }
    }

    public static boolean canReachBlock(BlockPos position, double doubleValue) {
        if (mc.player == null) {
            return false;
        } else {
            Vec3d eyePos = mc.player.getEyePos();
            Box box = new Box(position);
            Vec3d closestPointOnBox = getClosestPointOnBox(eyePos, box);
            return eyePos.squaredDistanceTo(closestPointOnBox) <= doubleValue * doubleValue;
        }
    }

    public static boolean canReachEntity(Entity entity, double doubleValue) {
        if (mc.player != null && entity != null) {
            Vec3d eyePos = mc.player.getEyePos();
            Box boundingBox = entity.getBoundingBox();
            Vec3d closestPointOnBox = getClosestPointOnBox(eyePos, boundingBox);
            return eyePos.squaredDistanceTo(closestPointOnBox) <= doubleValue * doubleValue;
        } else {
            return false;
        }
    }

    public static Vec3d getClosestPointOnBox(Vec3d position, Box box) {
        double doubleValue = Math.max(box.minX, Math.min(box.maxX, position.x));
        double doubleValueValue = Math.max(box.minY, Math.min(box.maxY, position.y));
        double doubleValueResult = Math.max(box.minZ, Math.min(box.maxZ, position.z));
        return new Vec3d(doubleValue, doubleValueValue, doubleValueResult);
    }

    private static Direction createDirectionFromPosition(Vec3d position, BlockPos positionValue) {
        double doubleValue = position.x - (positionValue.getX() + 0.5);
        double doubleValueValue = position.y - (positionValue.getY() + 0.5);
        double doubleValueResult = position.z - (positionValue.getZ() + 0.5);
        double magnitude = Math.abs(doubleValue);
        double magnitudeValue = Math.abs(doubleValueValue);
        double magnitudeResult = Math.abs(doubleValueResult);
        if (magnitude > magnitudeValue && magnitude > magnitudeResult) {
            return doubleValue > 0.0 ? Direction.EAST : Direction.WEST;
        } else if (magnitudeValue > magnitudeResult) {
            return doubleValueValue > 0.0 ? Direction.UP : Direction.DOWN;
        } else {
            return doubleValueResult > 0.0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    private RaycastUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
