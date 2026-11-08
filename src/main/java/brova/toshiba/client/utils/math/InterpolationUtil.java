package brova.toshiba.client.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.math.MathHelper;

public final class InterpolationUtil {

    public static float clamp01(float floatValue) {
        return (float) clamp(0.0, 1.0, (double) floatValue);
    }

    public static double getRandom(double doubleValue, double doubleValueValue) {
        if (doubleValue == doubleValueValue) {
            return doubleValue;
        } else {
            if (doubleValue > doubleValueValue) {
                double doubleValueResult = doubleValue;
                doubleValue = doubleValueValue;
                doubleValueValue = doubleValueResult;
            }
            return ThreadLocalRandom.current().nextDouble() * (doubleValueValue - doubleValue) + doubleValue;
        }
    }

    public static float calculateDelta(float floatValue, float floatValueValue) {
        return floatValue - floatValueValue;
    }

    public static double round(double doubleValue, int intValue) {
        double doubleValueValue = Math.pow(10.0, intValue);
        return Math.round(doubleValue * doubleValueValue) / doubleValueValue;
    }

    public static Number round(double doubleValue, double doubleValueValue) {
        if (doubleValueValue <= 0.0) {
            throw new IllegalArgumentException("Increment must be greater than zero");
        } else {
            double doubleValueResult = Math.round(doubleValue / doubleValueValue) * doubleValueValue;
            BigDecimal bigDecimal = BigDecimal.valueOf(doubleValueResult);
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
            return bigDecimal.doubleValue();
        }
    }

    public static String formatTime(long longValue) {
        long longValueValue = longValue / 3600000L;
        long longValueResult = longValue % 3600000L / 60000L;
        long longValueItem = longValue % 360000L % 60000L / 1000L;
        return String.format("%02d:%02d:%02d", longValueValue, longValueResult, longValueItem);
    }

    public static float slerp(float floatValue, float floatValueValue, float floatValueResult) {
        floatValueResult = Math.max(0.0F, Math.min(1.0F, floatValueResult));
        float floatValueItem = (float) Math.toRadians(floatValue);
        float floatValueEntry = (float) Math.toRadians(floatValueValue);
        float floatValueCopy = (float) Math.cos(floatValueItem) * (float) Math.cos(floatValueEntry) + (float) Math.sin(floatValueItem) * (float) Math.sin(floatValueEntry);
        float floatValue2 = (float) Math.acos(floatValueCopy);
        if (Math.abs(floatValue2) < 0.001F) {
            return floatValue;
        } else {
            float floatValue3 = (float) (Math.sin((1.0F - floatValueResult) * floatValue2) / Math.sin(floatValue2));
            float floatValue4 = (float) (Math.sin(floatValueResult * floatValue2) / Math.sin(floatValue2));
            float floatValue5 = floatValue * floatValue3 + floatValueValue * floatValue4;
            return (float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(floatValue5)), (double) floatValue, (double) floatValueValue);
        }
    }

    public static double round(double doubleValue, int intValue, double doubleValueValue) {
        double doubleValueResult = doubleValueValue / 2.0;
        double doubleValueItem = Math.floor(doubleValue / doubleValueValue) * doubleValueValue;
        return doubleValue >= doubleValueItem + doubleValueResult ? new BigDecimal(Math.ceil(doubleValue / doubleValueValue) * doubleValueValue).setScale(intValue, RoundingMode.HALF_UP).doubleValue() : new BigDecimal(doubleValueItem).setScale(intValue, RoundingMode.HALF_UP).doubleValue();
    }

    public static double step(double doubleValue, double doubleValueValue) {
        double doubleValueResult = Math.round(doubleValue / doubleValueValue) * doubleValueValue;
        doubleValueResult *= 1000.0;
        doubleValueResult = (int) doubleValueResult;
        return doubleValueResult / 1000.0;
    }

    public static double getDistance(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry, double doubleValueCopy) {
        double doubleValue2 = doubleValueItem - doubleValue;
        double doubleValue3 = doubleValueEntry - doubleValueValue;
        double doubleValue4 = doubleValueCopy - doubleValueResult;
        return MathHelper.sqrt((float) (doubleValue2 * doubleValue2 + doubleValue3 * doubleValue3 + doubleValue4 * doubleValue4));
    }

    public static double clamp(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return Math.max(doubleValue, Math.min(doubleValueValue, doubleValueResult));
    }

    public static int clamp(int intValue, int intValueValue, int intValueResult) {
        return Math.max(intValue, Math.min(intValueValue, intValueResult));
    }

    public static float normalize(float floatValue, float floatValueValue, float floatValueResult) {
        return (floatValue - floatValueValue) / (floatValueResult - floatValueValue);
    }

    public static double interporate(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return doubleValueValue + doubleValue * (doubleValueResult - doubleValueValue);
    }

    public static float lerp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + (floatValueValue - floatValue) * floatValueResult;
    }

    public static float easeOutExpo(float floatValue) {
        return floatValue == 1.0F ? 1.0F : (float) (1.0 - Math.pow(2.0, -10.0F * floatValue));
    }

    private InterpolationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
