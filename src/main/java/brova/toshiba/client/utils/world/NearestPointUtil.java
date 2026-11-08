package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class NearestPointUtil implements MinecraftAccess {

    public static Vec3d getClosestPoint(Entity entity) {
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

    public static Vec3d getNearestPoint(Entity entity, double doubleValue) {
        return getNearestPoint(entity, doubleValue, 0.1);
    }

    public static Vec3d getNearestPoint(Entity entity, double doubleValue, double doubleValueValue) {
        if (mc.player != null && entity != null) {
            Box boundingBox = entity.getBoundingBox();
            Vec3d position = null;
            double MAX_VALUE = Double.MAX_VALUE;
            Vec3d eyePos = mc.player.getEyePos();
            double doubleValueResult = boundingBox.minX + doubleValue;
            while (doubleValueResult <= boundingBox.maxX - doubleValue) {
                for (double index = boundingBox.minY + doubleValue; index <= boundingBox.maxY - doubleValue; index += doubleValueValue) {
                    for (double indexValue = boundingBox.minZ + doubleValue; indexValue <= boundingBox.maxZ - doubleValue; indexValue += doubleValueValue) {
                        Vec3d positionValue = new Vec3d(doubleValueResult, index, indexValue);
                        double doubleValueItem = eyePos.distanceTo(positionValue);
                        if (doubleValueItem < MAX_VALUE) {
                            MAX_VALUE = doubleValueItem;
                            position = positionValue;
                        }
                    }
                }
                doubleValueResult += doubleValueValue;
            }
            return position != null ? position : entity.getPos().add(0.0, entity.getHeight() * 0.5, 0.0);
        } else {
            return null;
        }
    }

    private NearestPointUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
