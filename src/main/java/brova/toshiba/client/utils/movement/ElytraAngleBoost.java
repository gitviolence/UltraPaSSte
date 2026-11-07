package brova.toshiba.client.utils.movement;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.util.math.MathHelper;

public final class ElytraAngleBoost implements MinecraftAccess {

    public static double getBoost() {
        int[] values = new int[] { -45, 45, 135, -135 };
        int[] valuesValue = new int[] { -90, 90, 180, -180, 0 };
        int[] valuesResult = new int[] { -45, 45 };
        float prevYaw = mc.player.prevYaw;
        float prevPitch = mc.player.prevPitch;
        int intValue = calculateWrapDegrees(prevYaw, values);
        float magnitude = Math.abs(MathHelper.wrapDegrees(prevYaw) - values[intValue]);
        int intValueValue = calculateWrapDegrees(prevYaw, valuesValue);
        float magnitudeValue = Math.abs(MathHelper.wrapDegrees(prevYaw) - valuesValue[intValueValue]);
        float floatValue = intValue == -1 ? 1.5F : 1.95F - magnitude * 0.56F / 45.0F;
        if (magnitudeValue < 10.0F) {
            floatValue += 0.1F - 0.1F * magnitudeValue / 10.0F;
        }
        int intValueResult = calculateWrapDegrees(prevPitch, valuesResult);
        float magnitudeResult = Math.abs(Math.abs(prevPitch) - Math.abs(valuesResult[intValueResult]));
        if (magnitudeResult < 26.0F) {
            floatValue = Math.max(1.94F, floatValue);
            floatValue += 0.05F - magnitudeResult * 0.05F / 26.0F;
        }
        floatValue = Math.min(2.045F, floatValue);
        if (mc.player.prevPitch > -55.0F && mc.player.prevPitch < -19.0F) {
            floatValue = 1.91F;
        } else if (mc.player.prevPitch < -55.0F) {
            floatValue = 1.54F;
        }
        if (mc.player.prevPitch > 19.0F && mc.player.prevPitch < 55.0F) {
            floatValue = 1.8F;
        } else if (mc.player.prevPitch > 55.0F) {
            floatValue = 1.54F;
        }
        return floatValue;
    }

    private static int calculateWrapDegrees(float floatValue, int[] values) {
        int intValue = 0;
        int intValueValue = -1;
        float MAX_VALUE = Float.MAX_VALUE;
        for (int intValueResult : values) {
            float magnitude = Math.abs(MathHelper.wrapDegrees(floatValue) - intValueResult);
            if (magnitude < MAX_VALUE) {
                MAX_VALUE = magnitude;
                intValueValue = intValue;
            }
            intValue++;
        }
        return intValueValue;
    }

    private ElytraAngleBoost() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
