package brova.toshiba.client.gui.click;

import brova.toshiba.Toshiba;
import brova.toshiba.client.storages.implement.ThemeStorage.Themes;
import brova.toshiba.client.utils.math.HoverUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

public class ThemeSelector {

    public void render(DrawContext context, Window window, float floatValue, float floatValueValue, int intValue) {
        if (context != null && window != null) {
            var themeList = Toshiba.INSTANCE.themeStorage.getThemeList();
            if (themeList != null && !themeList.isEmpty()) {
                float floatValueResult = themeList.size() * 8.0F + (themeList.size() - 1) * 4.0F;
                float floatValueItem = floatValueResult + 8.0F;
                float floatValueEntry = this.calculateScaledWidth(window, floatValueItem);
                float floatValueCopy = 100.0F + floatValue;
                float floatValue2 = floatValueEntry + 4.0F;
                float floatValue3 = floatValueCopy + 3.5F;
                Renderer2D.drawGradientRect(context.getMatrices(), floatValueEntry, floatValueCopy, floatValueItem, 15.0F, 3.5F, ColorMathUtil.darken(ColorMathUtil.getThemeColor(), 0.12F), ColorMathUtil.darken(ColorMathUtil.getThemeColor(), 0.1F), false);
                if ((intValue >> 24 & 0xFF) > 0) {
                    Renderer2D.drawRoundedRect(context.getMatrices(), floatValueEntry, floatValueCopy, floatValueItem, 15.0F, 3.5F, intValue);
                }
                Themes themes = Toshiba.INSTANCE.themeStorage.getThemes();
                for (int index = 0; index < themeList.size(); index++) {
                    Themes themesValue = themeList.get(index);
                    float floatValue4 = floatValue2 + index * 12.0F;
                    if (themesValue == themes) {
                        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue4 - 0.5F, floatValue3 - 0.5F, 9.0F, 9.0F, 2.5F, ColorMathUtil.setAlphaColor(-1, Math.max(1, (int) (200.0F * floatValueValue))));
                    }
                    Renderer2D.drawRoundedRect(context.getMatrices(), floatValue4, floatValue3, 8.0F, 8.0F, 2.0F, ColorMathUtil.applyAlpha(this.calculateColorWithTheme(themesValue), Math.max(0.55F, floatValueValue)));
                }
            }
        }
    }

    public boolean handleClick(Window window, double doubleValue, double doubleValueValue, int intValue, float floatValue) {
        if (window != null && intValue == 0) {
            var themeList = Toshiba.INSTANCE.themeStorage.getThemeList();
            if (themeList != null && !themeList.isEmpty()) {
                float floatValueValue = themeList.size() * 8.0F + (themeList.size() - 1) * 4.0F;
                float floatValueResult = floatValueValue + 8.0F;
                float floatValueItem = this.calculateScaledWidth(window, floatValueResult);
                float floatValueEntry = 100.0F + floatValue;
                float floatValueCopy = floatValueItem + 4.0F;
                float floatValue2 = floatValueEntry + 3.5F;
                if (!HoverUtil.isHovered(doubleValue, doubleValueValue, floatValueItem, floatValueEntry, floatValueResult, 15.0)) {
                    return false;
                } else {
                    for (int index = 0; index < themeList.size(); index++) {
                        float floatValue3 = floatValueCopy + index * 12.0F;
                        if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue3, floatValue2, 8.0, 8.0)) {
                            Toshiba.INSTANCE.themeStorage.setThemes(themeList.get(index));
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int calculateColorWithTheme(Themes themes) {
        int color = themes.getTheme().getColor(0);
        return ColorMathUtil.alpha(color) == 0 ? ColorMathUtil.rgba(220, 220, 220, 180) : color;
    }

    private float calculateScaledWidth(Window window, float floatValue) {
        return window.getScaledWidth() / 2.0F - floatValue / 2.0F;
    }
}
