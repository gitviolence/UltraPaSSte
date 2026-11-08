package brova.toshiba.client.utils.render;

import brova.toshiba.client.utils.color.fontscolor.ColorRGBA;
import java.util.List;

public class QuadColor {

    public final ColorRGBA topLeftColor;

    public final ColorRGBA bottomLeftColor;

    public final ColorRGBA topRightColor;

    public final ColorRGBA bottomRightColor;

    public QuadColor(ColorRGBA topLeftColor, ColorRGBA bottomLeftColor, ColorRGBA topRightColor, ColorRGBA bottomRightColor) {
        this.topLeftColor = topLeftColor;
        this.bottomLeftColor = bottomLeftColor;
        this.topRightColor = topRightColor;
        this.bottomRightColor = bottomRightColor;
    }

    public static QuadColor of(ColorRGBA color, ColorRGBA colorValue, ColorRGBA colorResult, ColorRGBA colorItem) {
        return new QuadColor(color, colorValue, colorResult, colorItem);
    }

    public static QuadColor of(List<ColorRGBA> colors) {
        return new QuadColor(colors.get(0), colors.get(1), colors.get(2), colors.get(3));
    }

    public QuadColor rotate() {
        return this;
    }

    public QuadColor mulAlpha(float floatValue) {
        ColorRGBA topLeftColor = this.topLeftColor;
        ColorRGBA color = brova.toshiba.client.utils.runtime.AccessGuard.invoke(topLeftColor, new Object[] { floatValue }, (accessReceiver, accessArguments) -> ((ColorRGBA) accessReceiver).mulAlpha(((float) accessArguments[0])));
        ColorRGBA colorValue = this.bottomLeftColor.mulAlpha(floatValue);
        topLeftColor = this.topRightColor;
        return new QuadColor(color, colorValue, brova.toshiba.client.utils.runtime.AccessGuard.invoke(topLeftColor, new Object[] { floatValue }, (accessReceiver, accessArguments) -> ((ColorRGBA) accessReceiver).mulAlpha(((float) accessArguments[0]))), this.bottomRightColor.mulAlpha(floatValue));
    }

    public ColorRGBA getTopLeftColor() {
        return this.topLeftColor;
    }

    public ColorRGBA getBottomLeftColor() {
        return this.bottomLeftColor;
    }

    public ColorRGBA getTopRightColor() {
        return this.topRightColor;
    }

    public ColorRGBA getBottomRightColor() {
        return this.bottomRightColor;
    }
}
