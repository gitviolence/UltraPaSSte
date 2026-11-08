package brova.toshiba.client.utils.color.fontscolor;

import brova.toshiba.client.utils.math.MathUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Objects;
import net.minecraft.util.math.MathHelper;

public class ColorRGBA {

    public static final ColorRGBA WHITE = new ColorRGBA(255, 255, 255);

    public static final ColorRGBA BLACK = new ColorRGBA(0, 0, 0);

    public static final ColorRGBA GREEN = new ColorRGBA(0, 255, 0);

    public static final ColorRGBA RED = new ColorRGBA(255, 0, 0);

    public static final ColorRGBA BLUE = new ColorRGBA(0, 0, 255);

    public static final ColorRGBA YELLOW = new ColorRGBA(255, 255, 0);

    public static final ColorRGBA GRAY = new ColorRGBA(88, 87, 93);

    public static final ColorRGBA TRANSPARENT = new ColorRGBA(0, 0, 0, 0);

    private transient float[] cachedHsv;

    private final int red;

    private final int green;

    private final int blue;

    private final int alpha;

    private static final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);

    public ColorRGBA(int intValue) {
        this(ColorMathUtil.red(intValue), ColorMathUtil.green(intValue), ColorMathUtil.blue(intValue), ColorMathUtil.alpha(intValue));
    }

    public ColorRGBA(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public ColorRGBA(int intValue, int intValueValue, int intValueResult) {
        this(intValue, intValueValue, intValueResult, 255);
    }

    public ColorRGBA(int red, int green, int blue, int alpha) {
        red = MathHelper.clamp(red, 0, 255);
        green = MathHelper.clamp(green, 0, 255);
        blue = MathHelper.clamp(blue, 0, 255);
        alpha = MathHelper.clamp(alpha, 0, 255);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public ColorRGBA(int red, int green, int blue, float floatValue) {
        red = MathHelper.clamp(red, 0, 255);
        green = MathHelper.clamp(green, 0, 255);
        blue = MathHelper.clamp(blue, 0, 255);
        floatValue = MathHelper.clamp(floatValue, 0.0F, 255.0F);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = (int) floatValue;
    }

    public int getRGB() {
        int intValue = Math.round((float) this.clampColorChannel(this.alpha));
        int intValueValue = Math.round((float) this.clampColorChannel(this.red));
        int intValueResult = Math.round((float) this.clampColorChannel(this.green));
        int intValueItem = Math.round((float) this.clampColorChannel(this.blue));
        return (intValue & 0xFF) << 24 | (intValueValue & 0xFF) << 16 | (intValueResult & 0xFF) << 8 | intValueItem & 0xFF;
    }

    private int clampColorChannel(float floatValue) {
        return (int) Math.max(0.0F, Math.min(255.0F, floatValue));
    }

    public static ColorRGBA fromHex(String text) {
        String textValue = text.startsWith("#") ? text.substring(1) : text;
        if (textValue.length() != 6 && textValue.length() != 8) {
            throw new IllegalArgumentException("Hex color must be in the format #RRGGBB or #RRGGBBAA");
        } else {
            int intValue = Integer.parseInt(textValue.substring(0, 2), 16);
            int intValueValue = Integer.parseInt(textValue.substring(2, 4), 16);
            int intValueResult = Integer.parseInt(textValue.substring(4, 6), 16);
            int intValueItem = textValue.length() == 8 ? Integer.parseInt(textValue.substring(6, 8), 16) : 255;
            return new ColorRGBA(intValue, intValueValue, intValueResult, intValueItem);
        }
    }

    public static ColorRGBA lerp(ColorRGBA color, ColorRGBA colorValue, float floatValue) {
        float floatValueValue = Math.max(0.0F, Math.min(1.0F, floatValue));
        int intValue = (int) (color.getRed() + (colorValue.getRed() - color.getRed()) * floatValueValue);
        int intValueValue = (int) (color.getGreen() + (colorValue.getGreen() - color.getGreen()) * floatValueValue);
        int intValueResult = (int) (color.getBlue() + (colorValue.getBlue() - color.getBlue()) * floatValueValue);
        int intValueItem = (int) (color.getAlpha() + (colorValue.getAlpha() - color.getAlpha()) * floatValueValue);
        return new ColorRGBA(intValue, intValueValue, intValueResult, intValueItem);
    }

    public static ColorRGBA fromInt(int intValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        return new ColorRGBA(intValueResult, intValueItem, intValueEntry, intValueValue);
    }

    public ColorRGBA withAlpha(float floatValue) {
        return new ColorRGBA(this.red, this.green, this.blue, (int) floatValue);
    }

    public ColorRGBA withAlpha(int intValue) {
        return new ColorRGBA(this.red, this.green, this.blue, intValue);
    }

    public ColorRGBA mulAlpha(float floatValue) {
        int intValue = (int) (this.alpha * floatValue);
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(this, new Object[] { intValue }, (accessReceiver, accessArguments) -> ((ColorRGBA) accessReceiver).withAlpha(((int) accessArguments[0])));
    }

    public ColorRGBA mix(ColorRGBA color, float floatValue) {
        floatValue = Math.min(1.0F, Math.max(0.0F, floatValue));
        return new ColorRGBA((int) MathUtil.interpolate((float) this.getRed(), (float) color.getRed(), floatValue), (int) MathUtil.interpolate((float) this.getGreen(), (float) color.getGreen(), floatValue), (int) MathUtil.interpolate((float) this.getBlue(), (float) color.getBlue(), floatValue), (int) MathUtil.interpolate((float) this.getAlpha(), (float) color.getAlpha(), floatValue));
    }

    public ColorRGBA darker(float floatValue) {
        floatValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
        return new ColorRGBA((int) (this.red * (1.0F - floatValue)), (int) (this.green * (1.0F - floatValue)), (int) (this.blue * (1.0F - floatValue)), this.alpha);
    }

    public static ColorRGBA fromHSB(float floatValue, float floatValueValue, float floatValueResult) {
        if (floatValueValue == 0.0F) {
            int intValue = (int) (floatValueResult * 255.0F + 0.5F);
            return new ColorRGBA(intValue, intValue, intValue);
        } else {
            float floatValueItem = (floatValue - (float) Math.floor(floatValue)) * 6.0F;
            float floatValueEntry = floatValueItem - (float) Math.floor(floatValueItem);
            float floatValueCopy = floatValueResult * (1.0F - floatValueValue);
            float floatValue2 = floatValueResult * (1.0F - floatValueValue * floatValueEntry);
            float floatValue3 = floatValueResult * (1.0F - floatValueValue * (1.0F - floatValueEntry));
            float floatValue4 = 0.0F;
            float floatValue5 = 0.0F;
            float floatValue6 = 0.0F;
            switch((int) floatValueItem) {
                case 0:
                    floatValue4 = floatValueResult;
                    floatValue5 = floatValue3;
                    floatValue6 = floatValueCopy;
                    break;
                case 1:
                    floatValue4 = floatValue2;
                    floatValue5 = floatValueResult;
                    floatValue6 = floatValueCopy;
                    break;
                case 2:
                    floatValue4 = floatValueCopy;
                    floatValue5 = floatValueResult;
                    floatValue6 = floatValue3;
                    break;
                case 3:
                    floatValue4 = floatValueCopy;
                    floatValue5 = floatValue2;
                    floatValue6 = floatValueResult;
                    break;
                case 4:
                    floatValue4 = floatValue3;
                    floatValue5 = floatValueCopy;
                    floatValue6 = floatValueResult;
                    break;
                case 5:
                    floatValue4 = floatValueResult;
                    floatValue5 = floatValueCopy;
                    floatValue6 = floatValue2;
            }
            return new ColorRGBA((int) (floatValue4 * 255.0F), (int) (floatValue5 * 255.0F), (int) (floatValue6 * 255.0F));
        }
    }

    public float getHue() {
        return this.getCachedHsv()[0];
    }

    public float getSaturation() {
        return this.getCachedHsv()[2];
    }

    public float getBrightness() {
        return this.getCachedHsv()[1];
    }

    private float[] getCachedHsv() {
        if (this.cachedHsv == null) {
            this.cachedHsv = this.calculateHsv();
        }
        return this.cachedHsv;
    }

    private float[] calculateHsv() {
        float floatValue = this.red / 255.0F;
        float floatValueValue = this.green / 255.0F;
        float floatValueResult = this.blue / 255.0F;
        float floatValueItem = Math.max(floatValue, Math.max(floatValueValue, floatValueResult));
        float floatValueEntry = Math.min(floatValue, Math.min(floatValueValue, floatValueResult));
        float floatValueCopy = floatValueItem - floatValueEntry;
        float floatValue2 = 0.0F;
        if (floatValueCopy != 0.0F) {
            if (floatValueItem == floatValue) {
                floatValue2 = (floatValueValue - floatValueResult) / floatValueCopy;
            } else if (floatValueItem == floatValueValue) {
                floatValue2 = (floatValueResult - floatValue) / floatValueCopy + 2.0F;
            } else {
                floatValue2 = (floatValue - floatValueValue) / floatValueCopy + 4.0F;
            }
            floatValue2 /= 6.0F;
            if (floatValue2 < 0.0F) {
                floatValue2++;
            }
        }
        float floatValue3 = floatValueItem == 0.0F ? 0.0F : floatValueCopy / floatValueItem;
        return new float[] { floatValue2, floatValue3, floatValueItem };
    }

    public ColorRGBA brighter(float floatValue) {
        floatValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
        return new ColorRGBA((int) (this.red + (255.0F - this.red) * floatValue), (int) (this.green + (255.0F - this.green) * floatValue), (int) (this.blue + (255.0F - this.blue) * floatValue), this.alpha);
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) {
            return true;
        } else if (value != null && this.getClass() == value.getClass()) {
            ColorRGBA color = (ColorRGBA) value;
            return Float.compare(this.red, color.red) == 0 && Float.compare(this.green, color.green) == 0 && Float.compare(this.blue, color.blue) == 0 && Float.compare(this.alpha, color.alpha) == 0;
        } else {
            return false;
        }
    }

    public float difference(ColorRGBA color) {
        return Math.abs(this.getHue() - color.getHue()) + Math.abs(this.getBrightness() - color.getBrightness()) + Math.abs(this.getSaturation() - color.getSaturation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.red, this.green, this.blue, this.alpha);
    }

    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getAlpha() {
        return this.alpha;
    }
}
