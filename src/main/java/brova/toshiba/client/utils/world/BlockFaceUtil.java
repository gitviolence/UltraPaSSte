package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationUtil;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BlockFaceUtil implements MinecraftAccess {

    public static Direction getBestVisibleFaceToObsidian(BlockPos position, PlayerEntity player) {
        if (mc.world != null && player != null) {
            Direction direction = null;
            double doubleValue = 0.0;
            for (Direction directionValue : Direction.values()) {
                BlockPos positionValue = position.offset(directionValue);
                if (mc.world.getBlockState(positionValue).isAir()) {
                    Vec3d facePoint = getFacePoint(position, directionValue);
                    Rotation rotation = RotationUtil.calculate(facePoint);
                    if (RaycastUtil.raycastBlock(rotation, position, 5.5) != null) {
                        double damageOfGhostBlock = ExplosionDamageUtil.getDamageOfGhostBlock(positionValue.up().toCenterPos(), player, positionValue);
                        if (damageOfGhostBlock > doubleValue) {
                            doubleValue = damageOfGhostBlock;
                            direction = directionValue;
                        }
                    }
                }
            }
            return direction;
        } else {
            return null;
        }
    }

    public static Direction getBestVisibleFace(BlockPos position, PlayerEntity player) {
        if (mc.world != null && player != null) {
            Vec3d eyePos = player.getEyePos();
            Direction direction = null;
            double MAX_VALUE = Double.MAX_VALUE;
            for (Direction directionValue : Direction.values()) {
                Vec3d facePoint = getFacePoint(position, directionValue);
                Rotation rotation = RotationUtil.calculate(facePoint);
                if (RaycastUtil.raycastBlock(rotation, position, 5.5) != null) {
                    double doubleValue = eyePos.squaredDistanceTo(facePoint);
                    if (doubleValue < MAX_VALUE) {
                        MAX_VALUE = doubleValue;
                        direction = directionValue;
                    }
                }
            }
            return direction;
        } else {
            return null;
        }
    }

    public static Vec3d getFacePoint(BlockPos position, Direction direction) {
        return new Vec3d(position.getX() + 0.5 + direction.getOffsetX() * 0.5, position.getY() + 0.5 + direction.getOffsetY() * 0.5, position.getZ() + 0.5 + direction.getOffsetZ() * 0.5);
    }

    public static boolean isBox(Box box, Predicate<BlockPos> predicate) {
        return BlockPos.stream(box).anyMatch(predicate);
    }

    public static boolean isBoxInBlock(Box box, Block block) {
        return mc.world == null ? false : isBox(box, input -> mc.world.getBlockState(input).getBlock().equals(block));
    }

    public static boolean isBoxInBlocks(Box box, List<Block> blocks) {
        return mc.world == null ? false : isBox(box, input -> blocks.contains(mc.world.getBlockState(input).getBlock()));
    }

    private BlockFaceUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
