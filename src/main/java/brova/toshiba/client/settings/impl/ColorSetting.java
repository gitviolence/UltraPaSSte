package brova.toshiba.client.settings.impl;

import brova.toshiba.Toshiba;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.storages.implement.ThemeStorage;
import brova.toshiba.client.storages.implement.ThemeStorage.Themes;
import brova.toshiba.client.storages.implement.helpertstorages.Theme;
import brova.toshiba.client.utils.render.ColorMathUtil;
import java.awt.Color;
import java.util.function.Supplier;

public final class ColorSetting extends SettingBase {

    private float hue = 0.0F;

    private float saturation = 1.0F;

    private float brightness = 1.0F;

    private float alpha = 1.0F;

    public ColorSetting(String text, int intValue) {
        super(text);
        this.setColor(intValue);
    }

    public static ColorSetting of(String text, int intValue) {
        return new ColorSetting(text, intValue);
    }

    public ColorSetting visible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }

    public void setHue(float hue) {
        this.hue = hue;
        this.updateClientTheme();
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
        this.updateClientTheme();
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
        this.updateClientTheme();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        this.updateClientTheme();
    }

    public int getColor() {
        int intValue = Math.round(this.alpha * 255.0F);
        if (intValue == 0) {
            intValue = 1;
        }
        return this.getColorWithAlpha() & 16777215 | intValue << 24;
    }

    public int getColorWithAlpha() {
        return Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
    }

    public ColorSetting setColor(int color) {
        float[] values = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
        this.hue = values[0];
        this.saturation = values[1];
        this.brightness = values[2];
        this.alpha = (color >> 24 & 0xFF) / 255.0F;
        this.updateClientTheme();
        return this;
    }

    public void updateClientTheme() {
        if (this.name().equals("Цвет темы") && Toshiba.INSTANCE != null && Toshiba.INSTANCE.themeStorage != null) {
            Theme theme = Themes.Custom.getTheme();
            int color = this.getColor();
            theme.color = new int[] { color, ColorMathUtil.darken(color, 0.35F) };
            Toshiba.INSTANCE.themeStorage.setThemes(Themes.Custom);
        }
    }

    public float getHue() {
        return this.hue;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public float getBrightness() {
        return this.brightness;
    }

    public float getAlpha() {
        return this.alpha;
    }
}
