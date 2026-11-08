package brova.toshiba.client.gui.click;

import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import java.util.List;
import net.minecraft.util.math.MathHelper;

public final class BackgroundSettings {

    public static final ModeSetting CLIENT_THEME = new ModeSetting("Тема клиента", "Темный", "Темный", "Белый");

    public static final BooleanSetting BLUR = new BooleanSetting("Блюр", true);

    public static final FloatSetting BLUR_STRENGTH = new FloatSetting("Сила блюра", 3.0F, 0.0F, 5.0F, 0.1F).visible(BLUR::isState);

    public static final BooleanSetting TRANSPARENCY = new BooleanSetting("Прозрачность", true);

    public static final FloatSetting OPACITY = new FloatSetting("Значение", 90.0F, 0.0F, 100.0F, 1.0F).visible(TRANSPARENCY::isState);

    public static final BooleanSetting GLOW = new BooleanSetting("Глов", true);

    public static final FloatSetting GLOW_STRENGTH = new FloatSetting("Сила глова", 1.0F, 0.0F, 5.0F, 0.1F).visible(GLOW::isState);

    public static final FloatSetting HUD_SCALE = new FloatSetting("Размер HUD", 1.0F, 0.3F, 2.0F, 0.1F);

    public static final FloatSetting GUI_SCALE = new FloatSetting("Размер GUI", 1.0F, 0.3F, 2.0F, 0.1F);

    private static final List<SettingBase> settings = List.of(CLIENT_THEME, BLUR, BLUR_STRENGTH, TRANSPARENCY, OPACITY, GLOW, GLOW_STRENGTH, HUD_SCALE, GUI_SCALE);

    private BackgroundSettings() {
    }

    public static List<SettingBase> getSettings() {
        return settings;
    }

    public static boolean isBlurEnabled() {
        return BLUR.isState() && BLUR_STRENGTH.get() > 0.0F;
    }

    public static float getOpacity() {
        return TRANSPARENCY.isState() ? OPACITY.get() / 100.0F : 1.0F;
    }

    public static int backgroundColor(int intValue, int intValueValue, int intValueResult, float floatValue) {
        int intValueItem = Math.round(255.0F * getOpacity() * MathHelper.clamp(floatValue, 0.0F, 1.0F));
        return ColorUtil.rgba(intValue, intValueValue, intValueResult, intValueItem);
    }

    public static float getOverlayOpacity() {
        return MathHelper.clamp(getOpacity() / 0.9F, 0.0F, 1.0F);
    }

    public static int overlayColor(int intValue, int intValueValue, int intValueResult, int intValueItem, float floatValue) {
        int intValueEntry = Math.round(intValueItem * getOverlayOpacity() * MathHelper.clamp(floatValue, 0.0F, 1.0F));
        return intValue == intValueValue && intValueValue == intValueResult ? ColorUtil.rgba(intValue, intValueValue, intValueResult, intValueEntry) : ColorMathUtil.rgba(intValue, intValueValue, intValueResult, intValueEntry);
    }
}
