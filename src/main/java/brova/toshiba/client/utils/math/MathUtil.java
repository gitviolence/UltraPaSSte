package brova.toshiba.client.utils.math;

import brova.toshiba.client.core.MinecraftAccess;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtil implements MinecraftAccess {

    public static RandomGenerator fastRandomize = new RandomGenerator();

    public static double direction(float floatValue, double doubleValue, double doubleValueValue) {
        if (doubleValue < 0.0) {
            floatValue += 180.0F;
        }
        float floatValueValue = 1.0F;
        if (doubleValue < 0.0) {
            floatValueValue = -0.5F;
        } else if (doubleValue > 0.0) {
            floatValueValue = 0.5F;
        }
        if (doubleValueValue > 0.0) {
            floatValue -= 90.0F * floatValueValue;
        }
        if (doubleValueValue < 0.0) {
            floatValue += 90.0F * floatValueValue;
        }
        return Math.toRadians(floatValue);
    }

    public static float randomNew(double doubleValue, double doubleValueValue) {
        return doubleValue > doubleValueValue ? (float) (fastRandomize.nextFloat() * (doubleValue - doubleValueValue) + doubleValueValue) : (float) (fastRandomize.nextFloat() * (doubleValueValue - doubleValue) + doubleValue);
    }

    public static double getBps(Entity entity) {
        double doubleValue = entity.getX() - entity.prevX;
        double doubleValueValue = entity.getY() - entity.prevY;
        double doubleValueResult = entity.getZ() - entity.prevZ;
        double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult);
        return squareRoot * 20.0;
    }

    public static float calculateBPS() {
        if (mc.player == null) {
            return 0.0F;
        } else {
            double doubleValue = mc.player.getX() - mc.player.prevX;
            double doubleValueValue = mc.player.getY() - mc.player.prevY;
            double doubleValueResult = mc.player.getZ() - mc.player.prevZ;
            double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult);
            float floatValue = 1.0F;
            float floatValueValue = (float) (squareRoot * floatValue * 20.0);
            return Math.round(floatValueValue * 10.0F) / 10.0F;
        }
    }

    public static double getTargetCompensatedSpeed(Entity entity) {
        double doubleValue = 1.5;
        if (entity == null) {
            return 1.5;
        } else {
            double doubleValueValue = calculateBPS();
            double doubleValueResult = 0.00342;
            double doubleValueItem = doubleValueValue * 0.00342;
            return 1.5 + doubleValueItem;
        }
    }

    public static float random(float floatValue, float floatValueValue) {
        SecureRandom secureRandom = new SecureRandom();
        double doubleValue = secureRandom.nextDouble();
        double doubleValueValue = secureRandom.nextDouble();
        double doubleValueResult = secureRandom.nextGaussian() * 0.02F;
        double doubleValueItem = Math.pow(doubleValue, 1.0 + secureRandom.nextDouble() * 0.7);
        double doubleValueEntry = (doubleValueValue * 0.8 + 0.1) * (Math.log1p(doubleValue * 3.0) * 0.5 + 0.5);
        return (float) (floatValue + (floatValueValue - floatValue) * doubleValueItem * doubleValueEntry + doubleValueResult);
    }

    public static double randomBest(double doubleValue, double doubleValueValue) {
        return ThreadLocalRandom.current().nextDouble() * (doubleValueValue - doubleValue) + doubleValue;
    }

    public static boolean isHovered(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry, double doubleValueCopy) {
        return doubleValueEntry >= doubleValue && doubleValueCopy >= doubleValueValue && doubleValueEntry < doubleValue + doubleValueResult && doubleValueCopy < doubleValueValue + doubleValueItem;
    }

    public static float interpolate(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + (floatValueValue - floatValue) * floatValueResult;
    }

    public static Vec3d interpolate(Vec3d position, Vec3d positionValue, float floatValue) {
        return new Vec3d(interpolate(position.getX(), positionValue.getX(), (double) floatValue), interpolate(position.getY(), positionValue.getY(), (double) floatValue), interpolate(position.getZ(), positionValue.getZ(), (double) floatValue));
    }

    public static Vec3d interpolate(Entity entity, float floatValue) {
        double interpolatedValue = MathHelper.lerp((double) floatValue, entity.prevX, entity.getX());
        double interpolatedValueValue = MathHelper.lerp((double) floatValue, entity.prevY, entity.getY());
        double interpolatedValueResult = MathHelper.lerp((double) floatValue, entity.prevZ, entity.getZ());
        return new Vec3d(interpolatedValue, interpolatedValueValue, interpolatedValueResult);
    }

    public static double interpolate(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return doubleValueValue + (doubleValue - doubleValueValue) * doubleValueResult;
    }

    public static float round(float floatValue) {
        return Math.round(floatValue * 10.0F) / 10.0F;
    }

    public static double round(double doubleValue, double doubleValueValue) {
        double doubleValueResult = Math.round(doubleValue / doubleValueValue) * doubleValueValue;
        BigDecimal bigDecimal = new BigDecimal(doubleValueResult);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    public static float lerp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + (floatValueValue - floatValue) * clamp(floatValueResult, 0.0F, 1.0F);
    }

    public static float clamp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue <= floatValueValue ? floatValueValue : Math.min(floatValue, floatValueResult);
    }

    public static double clamp(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return Math.max(doubleValue, Math.min(doubleValueValue, doubleValueResult));
    }

    public static int ler1p(int start, int end, double progress) {
        return (int) Math.round(start + progress * ((double) end - start));
    }

    public static double ler1p(double start, double end, double progress) {
        return start + progress * (end - start);
    }

    public static float ler1p(float start, float end, double progress) {
        return (float) (start + progress * ((double) end - start));
    }

    public static long ler1p(long start, long end, double progress) {
        return Math.round(start + progress * ((double) end - start));
    }

    public static short ler1p(short start, short end, double progress) {
        return (short) Math.round(start + progress * ((double) end - start));
    }

    public static byte ler1p(byte start, byte end, double progress) {
        return (byte) Math.round(start + progress * ((double) end - start));
    }
}
