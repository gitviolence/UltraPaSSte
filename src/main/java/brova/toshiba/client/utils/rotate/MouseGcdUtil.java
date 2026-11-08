package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.core.MinecraftAccess;

public class MouseGcdUtil implements MinecraftAccess {

    public static float getFixedRotation(float floatValue) {
        return getDeltaMouse(floatValue) * getGCDValue();
    }

    public static float getGCDValue() {
        return getGCD() * 0.15F;
    }

    public static float getGCD() {
        if (mc != null && mc.options != null) {
            double doubleValue = mc.options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
            return (float) (doubleValue * doubleValue * doubleValue * 8.0);
        } else {
            double doubleValueValue = 0.5000000149011612;
            return (float) (doubleValueValue * doubleValueValue * doubleValueValue * 8.0);
        }
    }

    public static float getDeltaMouse(float floatValue) {
        return Math.round(floatValue / getGCDValue());
    }
}
