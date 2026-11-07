package brova.toshiba.client.utils.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.storages.implement.ThemeStorage;
import brova.toshiba.client.storages.implement.ThemeStorage.Themes;
import brova.toshiba.client.storages.implement.helpertstorages.Theme;
import brova.toshiba.client.utils.math.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ColorMathUtil {

    public static final Color green = new Color(36, 218, 118);

    public static final Color yellow = new Color(255, 196, 67);

    public static final Color orange = new Color(255, 134, 0);

    public static final Color red = new Color(239, 72, 54);

    public static final Color Blues = new Color(125, 217, 250);

    public static int red(int intValue) {
        return intValue >> 16 & 0xFF;
    }

    public static int green(int intValue) {
        return intValue >> 8 & 0xFF;
    }

    public static float redf(int intValue) {
        return red(intValue) / 255.0F;
    }

    public static float greenf(int intValue) {
        return green(intValue) / 255.0F;
    }

    public static float bluef(int intValue) {
        return blue(intValue) / 255.0F;
    }

    public static float alphaf(int intValue) {
        return alpha(intValue) / 255.0F;
    }

    public static int getColor(int intValue, int intValueValue) {
        return getColor(intValue, intValue, intValue, intValueValue);
    }

    public static int gradient(int intValue, int intValueValue, float floatValue) {
        floatValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
        int interpolatedValue = MathHelper.lerp(floatValue, red(intValue), red(intValueValue));
        int interpolatedValueValue = MathHelper.lerp(floatValue, green(intValue), green(intValueValue));
        int interpolatedValueResult = MathHelper.lerp(floatValue, blue(intValue), blue(intValueValue));
        int interpolatedValueItem = MathHelper.lerp(floatValue, alpha(intValue), alpha(intValueValue));
        return rgba(interpolatedValue, interpolatedValueValue, interpolatedValueResult, interpolatedValueItem);
    }

    public static int toColor(String text) {
        if (text != null && text.length() == 7 && text.startsWith("#")) {
            try {
                int intValue = Integer.parseInt(text.substring(1), 16);
                return 0xFF000000 | intValue;
            } catch (NumberFormatException failure) {
                return -16777216;
            }
        } else {
            return -16777216;
        }
    }

    public static int applyAlpha(int intValue, float floatValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = (int) (intValueValue * Math.max(0.0F, Math.min(1.0F, floatValue)));
        return intValue & 16777215 | intValueResult << 24;
    }

    public static int r(int intValue) {
        return intValue >> 16 & 0xFF;
    }

    public static int g(int intValue) {
        return intValue >> 8 & 0xFF;
    }

    public static int b(int intValue) {
        return intValue & 0xFF;
    }

    public static int a(int intValue) {
        return intValue >> 24 & 0xFF;
    }

    public static int hexToRgb(String text) {
        if (text.startsWith("#")) {
            text = text.substring(1);
        }
        if (text.length() != 6) {
            throw new IllegalArgumentException("Недопустимый формат HEX: " + text);
        } else {
            int intValue = Integer.parseInt(text.substring(0, 2), 16);
            int intValueValue = Integer.parseInt(text.substring(2, 4), 16);
            int intValueResult = Integer.parseInt(text.substring(4, 6), 16);
            return rgb(intValue, intValueValue, intValueResult);
        }
    }

    public static int getThemeColor() {
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(Toshiba.INSTANCE.themeStorage, new Object[] {}, (accessReceiver, accessArguments) -> ((ThemeStorage) accessReceiver).getThemes()).getTheme().color[0];
    }

    public static int getThemeColor(int intValue) {
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage).getThemes(), new Object[] {}, (accessReceiver, accessArguments) -> ((Themes) accessReceiver).getTheme()).getColor(intValue);
    }

    public static int getThemeStaticColor() {
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(brova.toshiba.client.utils.runtime.AccessGuard.invoke(Toshiba.INSTANCE.themeStorage, new Object[] {}, (accessReceiver, accessArguments) -> ((ThemeStorage) accessReceiver).getThemes()), new Object[] {}, (accessReceiver, accessArguments) -> ((Themes) accessReceiver).getTheme()).color[0];
    }

    public static int interpolate(int intValue, int intValueValue, double doubleValue) {
        doubleValue = (float) MathUtil.clamp(0.0, 1.0, doubleValue);
        return getColor(MathUtil.ler1p(red(intValue), red(intValueValue), doubleValue), MathUtil.ler1p(green(intValue), green(intValueValue), doubleValue), MathUtil.ler1p(blue(intValue), blue(intValueValue), doubleValue), MathUtil.ler1p(alpha(intValue), alpha(intValueValue), doubleValue));
    }

    public static int[] genGradientForText(int intValue, int intValueValue, int intValueResult) {
        int[] values = new int[intValueResult];
        for (int index = 0; index < intValueResult; index++) {
            double doubleValue = (double) index / (intValueResult - 1);
            values[index] = interpolate(intValue, intValueValue, doubleValue);
        }
        return values;
    }

    public static int blue(int intValue) {
        return intValue & 0xFF;
    }

    public static int overCol(int intValue, int intValueValue, float floatValue) {
        return getColor(red(intValue) * (1.0F - floatValue) + red(intValueValue) * floatValue, green(intValue) * (1.0F - floatValue) + green(intValueValue) * floatValue, blue(intValue) * (1.0F - floatValue) + blue(intValueValue) * floatValue, alpha(intValue) * (1.0F - floatValue) + alpha(intValueValue) * floatValue);
    }

    public static int darken(int intValue, float floatValue) {
        float[] colorT = getColorT(intValue);
        float[] values = Color.RGBtoHSB((int) (colorT[0] * 255.0F), (int) (colorT[1] * 255.0F), (int) (colorT[2] * 255.0F), null);
        values[2] *= floatValue;
        values[2] = Math.max(0.0F, Math.min(1.0F, values[2]));
        int intValueValue = Color.HSBtoRGB(values[0], values[1], values[2]);
        return applyOpacity(intValueValue, (int) (colorT[3] * 255.0F));
    }

    public static int multDark(int intValue, float floatValue) {
        return getColor(red(intValue) * floatValue, green(intValue) * floatValue, blue(intValue) * floatValue, (float) alpha(intValue));
    }

    public static int overCol(int intValue, int intValueValue) {
        return overCol(intValue, intValueValue, 0.5F);
    }

    public static int alpha(int intValue) {
        return intValue >> 24 & 0xFF;
    }

    public static int multAlpha(int intValue, float floatValue) {
        return getColor((float) red(intValue), (float) green(intValue), (float) blue(intValue), alpha(intValue) * floatValue);
    }

    public static int replAlpha(int intValue, int intValueValue) {
        intValueValue = Math.max(0, Math.min(255, intValueValue));
        return intValueValue << 24 | intValue & 16777215;
    }

    public static Color random() {
        return new Color(Color.HSBtoRGB((float) Math.random(), (float) (0.75 + Math.random() / 4.0), (float) (0.75 + Math.random() / 4.0)));
    }

    public static int getColor(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        return new Color((int) floatValue, (int) floatValueValue, (int) floatValueResult, (int) floatValueItem).getRGB();
    }

    public static float[] getRGBAf(int intValue) {
        return new float[] { redf(intValue), greenf(intValue), bluef(intValue), alphaf(intValue) };
    }

    public static float[] getRGBAf1(int intValue) {
        return new float[] { red(intValue) / 255.0F, green(intValue) / 255.0F, blue(intValue) / 255.0F, alpha(intValue) / 255.0F };
    }

    public static Color interpolateTwoColors(int intValue, int intValueValue, Color color, Color colorValue, boolean flag) {
        int intValueResult = 0;
        if (intValue == 0) {
            intValueResult = intValueValue % 360;
        } else {
            intValueResult = (int) ((System.currentTimeMillis() / intValue + intValueValue) % 360L);
        }
        intValueResult = (intValueResult >= 180 ? 360 - intValueResult : intValueResult) * 2;
        return flag ? interpolateColorHue(color, colorValue, intValueResult / 360.0F) : interpolateColorC(color, colorValue, intValueResult / 360.0F);
    }

    public static Color interpolateTwoColors(int intValue, int intValueValue, Color color, Color colorValue) {
        return interpolateTwoColors(intValue, intValueValue, color, colorValue, false);
    }

    public static Color astolfo(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float floatValueEntry = 1800.0F;
        float floatValueCopy = (float) (System.currentTimeMillis() % (int) floatValueEntry) + (floatValueValue - floatValue) * floatValueItem;
        while (floatValueCopy > floatValueEntry) {
            floatValueCopy -= floatValueEntry;
        }
        floatValueCopy /= floatValueEntry;
        if (floatValueCopy > 1.0F) {
            floatValueCopy = 1.0F - (floatValueCopy - 1.0F);
        }
        return Color.getHSBColor(++floatValueCopy, floatValueResult, 1.0F);
    }

    private static int calculateCurrentTimeMillis(int intValue, int intValueValue) {
        long currentTimeMillis = System.currentTimeMillis();
        long longValue = (currentTimeMillis / intValue + intValueValue) % 360L;
        return (int) longValue;
    }

    public static void setColor(Color color, float floatValue) {
        float floatValueValue = color.getRed() / 255.0F;
        float floatValueResult = color.getGreen() / 255.0F;
        float floatValueItem = color.getBlue() / 255.0F;
        RenderSystem.setShaderColor(floatValueValue, floatValueResult, floatValueItem, floatValue);
    }

    public static int rgb(int intValue, int intValueValue, int intValueResult) {
        return 0xFF000000 | intValue << 16 | intValueValue << 8 | intValueResult;
    }

    public static int rgba(int intValue, int intValueValue, int intValueResult, int intValueItem) {
        return intValueItem << 24 | intValue << 16 | intValueValue << 8 | intValueResult;
    }

    public static float[] rgba(int intValue) {
        return new float[] { (intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, (intValue >> 24 & 0xFF) / 255.0F };
    }

    public static int rgba(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem) {
        return rgba((int) doubleValue, (int) doubleValueValue, (int) doubleValueResult, (int) doubleValueItem);
    }

    public static int getRed(int intValue) {
        return intValue >> 16 & 0xFF;
    }

    public static int getGreen(int intValue) {
        return intValue >> 8 & 0xFF;
    }

    public static int interpolate(int intValue, int intValueValue, float floatValue) {
        float[] values = rgba(intValue);
        float[] valuesValue = rgba(intValueValue);
        return rgba((int) MathUtil.interpolate(values[0] * 255.0F, valuesValue[0] * 255.0F, floatValue), (int) MathUtil.interpolate(values[1] * 255.0F, valuesValue[1] * 255.0F, floatValue), (int) MathUtil.interpolate(values[2] * 255.0F, valuesValue[2] * 255.0F, floatValue), (int) MathUtil.interpolate(values[3] * 255.0F, valuesValue[3] * 255.0F, floatValue));
    }

    public static int interpolateColor(int intValue, int intValueValue, float floatValue) {
        floatValue = Math.min(1.0F, Math.max(0.0F, floatValue));
        int red = getRed(intValue);
        int green = getGreen(intValue);
        int blue = getBlue(intValue);
        int alpha = getAlpha(intValue);
        int redValue = getRed(intValueValue);
        int greenValue = getGreen(intValueValue);
        int blueValue = getBlue(intValueValue);
        int alphaValue = getAlpha(intValueValue);
        int intValueResult = interpolateInt(red, redValue, floatValue);
        int intValueItem = interpolateInt(green, greenValue, floatValue);
        int intValueEntry = interpolateInt(blue, blueValue, floatValue);
        int intValueCopy = interpolateInt(alpha, alphaValue, floatValue);
        return intValueCopy << 24 | intValueResult << 16 | intValueItem << 8 | intValueEntry;
    }

    public static MutableText gradient(String text, int intValue, int intValueValue) {
        MutableText mutableText = Text.empty();
        for (int index = 0; index < text.length(); index++) {
            int intValueResult = interpolateColor(intValue, intValueValue, (float) index / text.length());
            MutableText mutableTextValue = Text.literal(String.valueOf(text.charAt(index))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(intValueResult)));
            mutableText.append(mutableTextValue);
        }
        return mutableText;
    }

    public static Text replace(Text text, String textValue, String textResult) {
        if (text != null && textValue != null && textResult != null) {
            String string = text.getString();
            String textItem = string.replace(textValue, textResult);
            return Text.literal(textItem);
        } else {
            return text;
        }
    }

    public static int gradient(int intValue, int intValueValue, int... intValueResult) {
        int intValueItem = (int) ((System.currentTimeMillis() / intValue + intValueValue) % 360L);
        intValueItem = (intValueItem > 180 ? 360 - intValueItem : intValueItem) + 180;
        int intValueEntry = (int) (intValueItem / 360.0F * intValueResult.length);
        if (intValueEntry == intValueResult.length) {
            intValueEntry--;
        }
        int intValueCopy = intValueResult[intValueEntry];
        int intValue2 = intValueResult[intValueEntry == intValueResult.length - 1 ? 0 : intValueEntry + 1];
        return interpolateColor(intValueCopy, intValue2, intValueItem / 360.0F * intValueResult.length - intValueEntry);
    }

    public static int themeGradient(int intValue, int intValueValue, float floatValue) {
        int themeColor = getThemeColor();
        return gradient(intValue, intValueValue, themeColor, darken(themeColor, floatValue));
    }

    public static int getBlue(int intValue) {
        return intValue & 0xFF;
    }

    public static int getAlpha(int intValue) {
        return intValue >> 24 & 0xFF;
    }

    public static int getColor(int intValue, int intValueValue, int intValueResult, int intValueItem) {
        int intValueEntry = 0;
        intValueEntry |= intValueItem << 24;
        intValueEntry |= intValue << 16;
        intValueEntry |= intValueValue << 8;
        return intValueEntry | intValueResult;
    }

    public static int getColor(int intValue) {
        return getColor(intValue, intValue, intValue, 255);
    }

    public static float[] getColorA(int intValue) {
        return new float[] { red(intValue) / 255.0F, green(intValue) / 255.0F, blue(intValue) / 255.0F, alphaf(intValue) };
    }

    public static float[] getColorT(int intValue) {
        return new float[] { red(intValue) / 255.0F, green(intValue) / 255.0F, blue(intValue) / 255.0F, alphaf(intValue) };
    }

    public static void setColor(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem) {
        GL11.glColor4d(doubleValue, doubleValueValue, doubleValueResult, doubleValueItem);
    }

    public static int setAlphaColor(int intValue, int intValueValue) {
        return intValue & 16777215 | intValueValue << 24;
    }

    public static float lerp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + floatValueResult * (floatValueValue - floatValue);
    }

    public static Color interpolateColorC(Color color, Color colorValue, float floatValue) {
        floatValue = Math.min(1.0F, Math.max(0.0F, floatValue));
        return new Color(interpolateInt(color.getRed(), colorValue.getRed(), floatValue), interpolateInt(color.getGreen(), colorValue.getGreen(), floatValue), interpolateInt(color.getBlue(), colorValue.getBlue(), floatValue), interpolateInt(color.getAlpha(), colorValue.getAlpha(), floatValue));
    }

    public static Double interpolate(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return doubleValue + (doubleValueValue - doubleValue) * doubleValueResult;
    }

    public static float interpolateFloat(float floatValue, float floatValueValue, double doubleValue) {
        double doubleValueValue = floatValue;
        double doubleValueResult = floatValueValue;
        double doubleValueItem = (float) doubleValue;
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] { doubleValueValue, doubleValueResult, doubleValueItem }, (accessReceiver, accessArguments) -> ColorMathUtil.interpolate(((double) accessArguments[0]), ((double) accessArguments[1]), ((double) accessArguments[2]))).floatValue();
    }

    public static int interpolateInt(int intValue, int intValueValue, double doubleValue) {
        return interpolate(intValue, intValueValue, (float) doubleValue);
    }

    public static Color interpolateColorHue(Color color, Color colorValue, float floatValue) {
        floatValue = Math.min(1.0F, Math.max(0.0F, floatValue));
        float[] values = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float[] valuesValue = Color.RGBtoHSB(colorValue.getRed(), colorValue.getGreen(), colorValue.getBlue(), null);
        Color hSBColor = Color.getHSBColor(interpolateFloat(values[0], valuesValue[0], floatValue), interpolateFloat(values[1], valuesValue[1], floatValue), interpolateFloat(values[2], valuesValue[2], floatValue));
        return new Color(hSBColor.getRed(), hSBColor.getGreen(), hSBColor.getBlue(), interpolateInt(color.getAlpha(), colorValue.getAlpha(), floatValue));
    }

    public static void setColor(Color color) {
        if (color == null) {
            color = Color.white;
        }
        setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    public static void setColor(int color) {
        setColor(color, (color >> 24 & 0xFF) / 255.0F);
    }

    public static void setColor(int intValue, float floatValue) {
        float floatValueValue = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValueResult = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValueItem = (intValue & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(floatValueValue, floatValueResult, floatValueItem, floatValue);
    }

    public static int applyOpacity(int intValue, float floatValue) {
        return rgba((double) getRed(intValue), (double) getGreen(intValue), (double) getBlue(intValue), (double) (getAlpha(intValue) * floatValue / 255.0F));
    }

    public static int reFactorColor(int intValue, float floatValue) {
        return rgba((double) (extractRedf(intValue) * floatValue), (double) (extractGreenf(intValue) * floatValue), (double) (extractBluef(intValue) * floatValue), (double) extractAlphaf(intValue));
    }

    public static float extractRedf(int intValue) {
        return (intValue >> 16 & 0xFF) / 255.0F;
    }

    public static int extractRed(int intValue) {
        return intValue >> 16 & 0xFF;
    }

    public static float extractBluef(int intValue) {
        return (intValue & 0xFF) / 255.0F;
    }

    public static int extractBlue(int intValue) {
        return intValue & 0xFF;
    }

    public static float extractGreenf(int intValue) {
        return (intValue >> 8 & 0xFF) / 255.0F;
    }

    public static int extractGreen(int intValue) {
        return intValue >> 8 & 0xFF;
    }

    public static float extractAlphaf(int intValue) {
        return (intValue >> 24 & 0xFF) / 255.0F;
    }

    public static int extractAlpha(int intValue) {
        return intValue >> 24 & 0xFF;
    }
}
