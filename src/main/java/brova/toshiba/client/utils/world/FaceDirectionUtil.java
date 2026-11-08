package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.rotate.Rotation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FaceDirectionUtil implements MinecraftAccess {

    public static Direction getFaceDirection(PlayerEntity player, BlockPos position) {
        Vec3d eyePos = player.getEyePos();
        Vec3d rotationVec = player.getRotationVec(1.0F);
        double doubleValue = position.getX() + 0.5;
        double doubleValueValue = position.getY() + 0.5;
        double doubleValueResult = position.getZ() + 0.5;
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();
        double doubleValueItem = position.getX() + 1.0;
        double doubleValueEntry = position.getY() + 1.0;
        double doubleValueCopy = position.getZ() + 1.0;
        Direction direction = null;
        double MAX_VALUE = Double.MAX_VALUE;
        double doubleValue2 = intersectHorizontalFace(eyePos, rotationVec, doubleValueEntry, x, doubleValueItem, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.UP;
        }
        doubleValue2 = intersectHorizontalFace(eyePos, rotationVec, y, x, doubleValueItem, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.DOWN;
        }
        doubleValue2 = intersectNorthSouthFace(eyePos, rotationVec, z, x, doubleValueItem, y, doubleValueEntry);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.NORTH;
        }
        doubleValue2 = intersectNorthSouthFace(eyePos, rotationVec, doubleValueCopy, x, doubleValueItem, y, doubleValueEntry);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.SOUTH;
        }
        doubleValue2 = intersectEastWestFace(eyePos, rotationVec, x, y, doubleValueEntry, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.WEST;
        }
        doubleValue2 = intersectEastWestFace(eyePos, rotationVec, doubleValueItem, y, doubleValueEntry, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            direction = Direction.EAST;
        }
        if (direction == null) {
            direction = createDirectionFromPosition(eyePos, doubleValue, doubleValueValue, doubleValueResult);
        }
        return direction;
    }

    public static Direction getFaceDirection(Rotation rotation, PlayerEntity player, BlockPos position) {
        Vec3d eyePos = player.getEyePos();
        Vec3d rotationVector = player.getRotationVector(rotation.getPitch(), rotation.getYaw());
        double doubleValue = position.getX() + 0.5;
        double doubleValueValue = position.getY() + 0.5;
        double doubleValueResult = position.getZ() + 0.5;
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();
        double doubleValueItem = position.getX() + 1.0;
        double doubleValueEntry = position.getY() + 1.0;
        double doubleValueCopy = position.getZ() + 1.0;
        Direction direction = null;
        double MAX_VALUE = Double.MAX_VALUE;
        double doubleValue2 = intersectHorizontalFace(eyePos, rotationVector, doubleValueEntry, x, doubleValueItem, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.UP;
        }
        doubleValue2 = intersectHorizontalFace(eyePos, rotationVector, y, x, doubleValueItem, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.DOWN;
        }
        doubleValue2 = intersectNorthSouthFace(eyePos, rotationVector, z, x, doubleValueItem, y, doubleValueEntry);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.NORTH;
        }
        doubleValue2 = intersectNorthSouthFace(eyePos, rotationVector, doubleValueCopy, x, doubleValueItem, y, doubleValueEntry);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.SOUTH;
        }
        doubleValue2 = intersectEastWestFace(eyePos, rotationVector, x, y, doubleValueEntry, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            MAX_VALUE = doubleValue2;
            direction = Direction.WEST;
        }
        doubleValue2 = intersectEastWestFace(eyePos, rotationVector, doubleValueItem, y, doubleValueEntry, z, doubleValueCopy);
        if (doubleValue2 > 0.0 && doubleValue2 < MAX_VALUE) {
            direction = Direction.EAST;
        }
        if (direction == null) {
            direction = createDirectionFromPosition(eyePos, doubleValue, doubleValueValue, doubleValueResult);
        }
        return direction;
    }

    private static double intersectHorizontalFace(Vec3d position, Vec3d positionValue, double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry) {
        if (Math.abs(positionValue.y) < 1.0E-6) {
            return -1.0;
        } else {
            double doubleValueCopy = (doubleValue - position.y) / positionValue.y;
            if (doubleValueCopy <= 0.0) {
                return -1.0;
            } else {
                double doubleValue2 = position.x + positionValue.x * doubleValueCopy;
                double doubleValue3 = position.z + positionValue.z * doubleValueCopy;
                return doubleValue2 >= doubleValueValue && doubleValue2 <= doubleValueResult && doubleValue3 >= doubleValueItem && doubleValue3 <= doubleValueEntry ? doubleValueCopy : -1.0;
            }
        }
    }

    private static double intersectNorthSouthFace(Vec3d position, Vec3d positionValue, double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry) {
        if (Math.abs(positionValue.z) < 1.0E-6) {
            return -1.0;
        } else {
            double doubleValueCopy = (doubleValue - position.z) / positionValue.z;
            if (doubleValueCopy <= 0.0) {
                return -1.0;
            } else {
                double doubleValue2 = position.x + positionValue.x * doubleValueCopy;
                double doubleValue3 = position.y + positionValue.y * doubleValueCopy;
                return doubleValue2 >= doubleValueValue && doubleValue2 <= doubleValueResult && doubleValue3 >= doubleValueItem && doubleValue3 <= doubleValueEntry ? doubleValueCopy : -1.0;
            }
        }
    }

    private static double intersectEastWestFace(Vec3d position, Vec3d positionValue, double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry) {
        if (Math.abs(positionValue.x) < 1.0E-6) {
            return -1.0;
        } else {
            double doubleValueCopy = (doubleValue - position.x) / positionValue.x;
            if (doubleValueCopy <= 0.0) {
                return -1.0;
            } else {
                double doubleValue2 = position.y + positionValue.y * doubleValueCopy;
                double doubleValue3 = position.z + positionValue.z * doubleValueCopy;
                return doubleValue2 >= doubleValueValue && doubleValue2 <= doubleValueResult && doubleValue3 >= doubleValueItem && doubleValue3 <= doubleValueEntry ? doubleValueCopy : -1.0;
            }
        }
    }

    private static Direction createDirectionFromPosition(Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult) {
        double doubleValueItem = position.x - doubleValue;
        double doubleValueEntry = position.y - doubleValueValue;
        double doubleValueCopy = position.z - doubleValueResult;
        double magnitude = Math.abs(doubleValueItem);
        double magnitudeValue = Math.abs(doubleValueEntry);
        double magnitudeResult = Math.abs(doubleValueCopy);
        if (magnitude >= magnitudeValue && magnitude >= magnitudeResult) {
            return doubleValueItem > 0.0 ? Direction.EAST : Direction.WEST;
        } else if (magnitudeValue >= magnitude && magnitudeValue >= magnitudeResult) {
            return doubleValueEntry > 0.0 ? Direction.UP : Direction.DOWN;
        } else {
            return doubleValueCopy > 0.0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
