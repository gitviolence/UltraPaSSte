package brova.toshiba.client.utils.render;

import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import net.minecraft.util.math.MathHelper;

public final class ColorUtil {

    private static Animation lightProgress;

    private ColorUtil() {
    }

    public static boolean isLight() {
        return BackgroundSettings.CLIENT_THEME.is("Белый");
    }

    public static int rgba(int intValue, int intValueValue, int intValueResult, int intValueItem) {
        float lightProgress = getLightProgress();
        return ColorMathUtil.rgba(Math.round((float) MathHelper.lerp(lightProgress, intValue, 255 - intValue)), Math.round((float) MathHelper.lerp(lightProgress, intValueValue, 255 - intValueValue)), Math.round((float) MathHelper.lerp(lightProgress, intValueResult, 255 - intValueResult)), intValueItem);
    }

    public static float getLightProgress() {
        float floatValue = isLight() ? 1.0F : 0.0F;
        if (lightProgress == null) {
            lightProgress = new Animation(floatValue, 7.0F, PowerEasing.CUBIC_OUT);
        }
        lightProgress.update(floatValue);
        return MathHelper.clamp(lightProgress.getValue(), 0.0F, 1.0F);
    }

    public static int neutralColor(int intValue) {
        int intValueValue = intValue >>> 16 & 0xFF;
        int intValueResult = intValue >>> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        return intValueValue == intValueResult && intValueResult == intValueItem ? rgba(intValueValue, intValueResult, intValueItem, intValue >>> 24 & 0xFF) : intValue;
    }
}
