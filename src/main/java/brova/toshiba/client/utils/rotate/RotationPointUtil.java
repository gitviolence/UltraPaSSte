package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.math.MathUtil;
import brova.toshiba.client.utils.world.EntityRaycastUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.ShapeType;

public final class RotationPointUtil implements MinecraftAccess {

    private static Vec3d rotationPoint = Vec3d.ZERO;

    private static Vec3d zero = Vec3d.ZERO;

    public static Vec3d getRotationPoint() {
        return rotationPoint;
    }

    public static Vec3d getNearestPoint(Entity entity) {
        Box boundingBox = entity.getBoundingBox();
        double doubleValue = 0.1;
        Vec3d position = null;
        double MAX_VALUE = Double.MAX_VALUE;
        for (double minX = boundingBox.minX; minX <= boundingBox.maxX; minX += doubleValue) {
            for (double minY = boundingBox.minY; minY <= boundingBox.maxY; minY += doubleValue) {
                for (double minZ = boundingBox.minZ; minZ <= boundingBox.maxZ; minZ += doubleValue) {
                    Vec3d positionValue = new Vec3d(minX, minY, minZ);
                    double doubleValueValue = mc.player.getEyePos().distanceTo(positionValue);
                    if (doubleValueValue < MAX_VALUE) {
                        MAX_VALUE = doubleValueValue;
                        position = positionValue;
                    }
                }
            }
        }
        return position;
    }

    public static Vec3d getPoint(Entity entity) {
        Box boundingBox = entity.getBoundingBox();
        double doubleValue = boundingBox.maxX - boundingBox.minX;
        double doubleValueValue = boundingBox.maxY - boundingBox.minY;
        double doubleValueResult = boundingBox.maxZ - boundingBox.minZ;
        double doubleValueItem = boundingBox.minX + doubleValue / 2.0;
        double doubleValueEntry = boundingBox.minY + doubleValueValue * 0.7;
        double doubleValueCopy = boundingBox.minZ + doubleValueResult / 2.0;
        double doubleValue2 = System.currentTimeMillis() / 50.0;
        int id = entity.getId();
        double doubleValue3 = Math.sin(doubleValue2 + id) * (doubleValue * 0.45);
        double doubleValue4 = Math.cos(doubleValue2 * 0.8 + id) * (doubleValueValue * 0.1);
        double doubleValue5 = Math.cos(doubleValue2 * 1.2 + id) * (doubleValueResult * 0.45);
        return new Vec3d(doubleValueItem + doubleValue3, doubleValueEntry + doubleValue4, doubleValueCopy + doubleValue5);
    }

    public static Vec3d getPoint2(Entity entity) {
        Box boundingBox = entity.getBoundingBox();
        double doubleValue = boundingBox.maxX - boundingBox.minX;
        double doubleValueValue = boundingBox.maxY - boundingBox.minY;
        double doubleValueResult = boundingBox.maxZ - boundingBox.minZ;
        double doubleValueItem = boundingBox.minX + doubleValue / 2.0;
        double doubleValueEntry = boundingBox.minY + doubleValueValue * 0.65;
        double doubleValueCopy = boundingBox.minZ + doubleValueResult / 2.0;
        double doubleValue2 = System.currentTimeMillis() / 65.0;
        int id = entity.getId();
        double doubleValue3 = Math.sin(doubleValue2 + id) * (doubleValue * 0.7);
        double doubleValue4 = Math.cos(doubleValue2 * 0.8 + id) * (doubleValueValue * 0.4);
        double doubleValue5 = Math.cos(doubleValue2 * 1.2 + id) * (doubleValueResult * 0.7);
        return new Vec3d(doubleValueItem + doubleValue3, doubleValueEntry + doubleValue4, doubleValueCopy + doubleValue5);
    }

    public static Vec3d getNearestVisiblePoint(Entity entity, Vec3d position, double doubleValue) {
        if (position != null && mc.player != null && mc.world != null) {
            if (canSeeTarget(entity, position, doubleValue)) {
                return position;
            } else {
                Box boundingBox = entity.getBoundingBox();
                double doubleValueValue = 0.12;
                Vec3d positionValue = null;
                double MAX_VALUE = Double.MAX_VALUE;
                for (double minX = boundingBox.minX; minX <= boundingBox.maxX; minX += doubleValueValue) {
                    for (double minY = boundingBox.minY; minY <= boundingBox.maxY; minY += doubleValueValue) {
                        for (double minZ = boundingBox.minZ; minZ <= boundingBox.maxZ; minZ += doubleValueValue) {
                            Vec3d positionResult = new Vec3d(minX, minY, minZ);
                            if (canSeeTarget(entity, positionResult, doubleValue)) {
                                double doubleValueResult = positionResult.squaredDistanceTo(position);
                                if (doubleValueResult < MAX_VALUE) {
                                    MAX_VALUE = doubleValueResult;
                                    positionValue = positionResult;
                                }
                            }
                        }
                    }
                }
                return positionValue != null ? positionValue : position;
            }
        } else {
            return position;
        }
    }

    private static boolean canSeeTarget(Entity entity, Vec3d position, double doubleValue) {
        Vec3d eyePos = mc.player.getEyePos();
        double doubleValueValue = eyePos.distanceTo(position);
        if (doubleValueValue > doubleValue) {
            return false;
        } else {
            Vec3d positionValue = position.subtract(eyePos).normalize();
            if (!EntityRaycastUtil.rayTrace(positionValue, doubleValueValue + 0.2, entity.getBoundingBox())) {
                return false;
            } else {
                BlockHitResult blockHitResult = EntityRaycastUtil.raycast(eyePos, position, ShapeType.COLLIDER, mc.player);
                return blockHitResult.getType() == Type.MISS || eyePos.squaredDistanceTo(blockHitResult.getPos()) >= eyePos.squaredDistanceTo(position) - 1.0E-4;
            }
        }
    }

    public static Vec3d getMultipoint(Entity entity, double doubleValue) {
        float floatValue = 0.005F;
        float floatValueValue = 0.015F;
        float floatValueResult = 0.0015F;
        float floatValueItem = 0.015F;
        double lengthX = entity.getBoundingBox().getLengthX();
        double lengthY = entity.getBoundingBox().getLengthY();
        double lengthZ = entity.getBoundingBox().getLengthZ();
        if (zero.equals(Vec3d.ZERO)) {
            zero = new Vec3d(MathUtil.randomBest(-0.02F, 0.02F), MathUtil.randomBest(-0.02F, 0.02F), MathUtil.randomBest(-0.02F, 0.02F));
        }
        if (rotationPoint.equals(Vec3d.ZERO)) {
            rotationPoint = new Vec3d(0.0, lengthY * 0.5, 0.0);
        }
        rotationPoint = rotationPoint.add(zero);
        double doubleValueValue = (lengthX - 0.1) / 2.0;
        double doubleValueResult = (lengthZ - 0.1) / 2.0;
        if (rotationPoint.x >= doubleValueValue) {
            zero = new Vec3d(-MathUtil.randomBest(floatValue, floatValueValue), zero.getY(), zero.getZ());
        } else if (rotationPoint.x <= -doubleValueValue) {
            zero = new Vec3d(MathUtil.randomBest(floatValue, floatValueValue), zero.getY(), zero.getZ());
        }
        if (rotationPoint.y >= lengthY * 0.75) {
            zero = new Vec3d(zero.getX(), -MathUtil.randomBest(floatValueResult, floatValueItem), zero.getZ());
        } else if (rotationPoint.y <= lengthY * 0.3) {
            zero = new Vec3d(zero.getX(), MathUtil.randomBest(floatValueResult, floatValueItem), zero.getZ());
        }
        if (rotationPoint.z >= doubleValueResult) {
            zero = new Vec3d(zero.getX(), zero.getY(), -MathUtil.randomBest(floatValue, floatValueValue));
        } else if (rotationPoint.z <= -doubleValueResult) {
            zero = new Vec3d(zero.getX(), zero.getY(), MathUtil.randomBest(floatValue, floatValueValue));
        }
        rotationPoint.add(MathUtil.randomBest(-0.05F, 0.05F), 0.0, MathUtil.randomBest(-0.05F, 0.05F));
        if (!EntityRaycastUtil.rayTrace(mc.player.getRotationVector(), doubleValue, entity.getBoundingBox())) {
            float floatValueEntry = (float) (lengthX / 2.0) * 0.8F;
            for (float index = -floatValueEntry; index <= floatValueEntry; index += 0.1F) {
                for (float indexValue = -floatValueEntry; indexValue <= floatValueEntry; indexValue += 0.1F) {
                    for (float indexResult = (float) (lengthY * 0.9); indexResult >= lengthY * 0.3; indexResult -= 0.1F) {
                        Vec3d position = new Vec3d(entity.getX() + index, entity.getY() + indexResult, entity.getZ() + indexValue);
                        Rotation rotation = RotationUtil.fromVec3d(position);
                        if (EntityRaycastUtil.rayTrace(rotation.toVector(), doubleValue, entity.getBoundingBox())) {
                            rotationPoint = new Vec3d(index, indexResult, indexValue);
                            return entity.getPos().add(rotationPoint);
                        }
                    }
                }
            }
        }
        return entity.getPos().add(rotationPoint);
    }

    private RotationPointUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
