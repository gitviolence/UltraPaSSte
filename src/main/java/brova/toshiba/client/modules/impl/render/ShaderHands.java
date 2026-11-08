package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.ShaderHands.NamedFloatSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.helpertstorages.Theme;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.HandsOverlayRenderer;
import java.awt.Color;

public final class ShaderHands extends Module {

    private static final int cyanGradientColor = ColorMathUtil.rgba(127, 242, 255, 230);

    private static final int pinkGradientColor = ColorMathUtil.rgba(255, 50, 150, 230);

    public static ShaderHands INSTANCE = new ShaderHands();

    private static final HandsOverlayRenderer handsOverlayRenderer = HandsOverlayRenderer.getInstance();

    public final ModeSetting mode = new ModeSetting("Режим", "Свечение", "Свечение", "Красивый", "Плазма", "Жидкое стекло", "Огонь");

    public final ModeSetting fireColorMode = new ModeSetting("Цвет огня", "Тема", "Тема", "Свой", "Предмет").visible(() -> this.mode.is("Огонь"));

    public final ColorSetting fireColor = new ColorSetting("Свой цвет огня", -39288).visible(() -> this.mode.is("Огонь") && this.fireColorMode.is("Свой"));

    public final FloatSetting fireIntensity = new FloatSetting("Сила огня", 1.15F, 0.1F, 3.0F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireTrail = new FloatSetting("Длина следа", 0.92F, 0.7F, 0.94F, 0.005F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireSize = new FloatSetting("Размер пламени", 1.1F, 0.35F, 3.0F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireSpeed = new FloatSetting("Скорость пламени", 1.25F, 0.2F, 3.5F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireDensity = new FloatSetting("Плотность огня", 1.0F, 0.2F, 2.5F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireGlow = new FloatSetting("Свечение огня", 1.25F, 0.0F, 3.0F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireSmoke = new FloatSetting("Дым", 0.55F, 0.0F, 0.8F, 0.01F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireSoftness = new FloatSetting("Мягкость огня", 1.35F, 0.5F, 2.5F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireBlur = new FloatSetting("Размытие огня", 1.55F, 0.5F, 3.0F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireHandFade = new FloatSetting("Объём руки", 0.68F, 0.1F, 0.9F, 0.01F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireHandSoftness = new FloatSetting("Мягкость объёма", 1.3F, 0.5F, 2.5F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting fireHandBloom = new FloatSetting("Блюм объёма", 1.4F, 0.5F, 3.0F, 0.05F).visible(() -> this.mode.is("Огонь"));

    public final FloatSetting glassTransparency = new FloatSetting("Прозрачность стекла", 0.85F, 0.0F, 1.0F, 0.01F).visible(() -> this.mode.is("Жидкое стекло"));

    public final FloatSetting glassDistortion = new FloatSetting("Искажение стекла", 0.8F, 0.0F, 2.0F, 0.05F).visible(() -> this.mode.is("Жидкое стекло"));

    public final FloatSetting glassSpeed = new FloatSetting("Скорость стекла", 1.0F, 0.1F, 3.0F, 0.1F).visible(() -> this.mode.is("Жидкое стекло"));

    public final BooleanSetting glassBlur = new BooleanSetting("Блюр", true).visible(() -> this.mode.is("Жидкое стекло"));

    public final FloatSetting glassBlurIntensity = new FloatSetting("Интенсивность блюра", 4.0F, 0.0F, 10.0F, 0.1F).visible(() -> this.mode.is("Жидкое стекло") && this.glassBlur.isState());

    public final ColorSetting plasmaColorFirst = new ColorSetting("Первый цвет", -14360577).visible(() -> this.mode.is("Плазма"));

    public final ColorSetting plasmaColorSecond = new ColorSetting("Второй цвет", 0).visible(() -> this.mode.is("Плазма"));

    public final FloatSetting plasmaSpeed = new FloatSetting("Скорость плазмы", 1.0F, 0.1F, 3.0F, 0.1F).visible(() -> this.mode.is("Плазма"));

    public final FloatSetting plasmaScale = new FloatSetting("Масштаб плазмы", 6.0F, 2.0F, 16.0F, 0.5F).visible(() -> this.mode.is("Плазма"));

    public final FloatSetting waveSpeed = new FloatSetting("Скорость волн", 5.0F, 1.0F, 10.0F, 0.1F).visible(() -> this.mode.is("Красивый"));

    public final FloatSetting outline = new FloatSetting("Ширина обводки", 2.0F, 0.1F, 5.0F, 0.1F).visible(() -> !this.mode.is("Туман") && !this.mode.is("Плазма") && !this.mode.is("Огонь"));

    public final FloatSetting glow = new NamedFloatSetting(this, "Сила свечения", 1.0F, 0.0F, 5.0F, 0.1F).visible(() -> !this.mode.is("Туман") && !this.mode.is("Плазма") && !this.mode.is("Огонь"));

    public final FloatSetting fill = new FloatSetting("Прозрачность", 0.5F, 0.0F, 1.0F, 0.01F).visible(() -> !this.mode.is("Туман") && !this.mode.is("Жидкое стекло") && !this.mode.is("Огонь"));

    public final BooleanSetting optimize = new BooleanSetting("Оптимизация", false).visible(() -> !this.mode.is("Плазма") && !this.mode.is("Огонь"));

    public final BooleanSetting fogOnlyItems = new BooleanSetting("Только предметы", false).visible(() -> this.mode.is("Туман"));

    public final ModeSetting fogColorMode = new ModeSetting("Режим цвета", "Предмет", "Предмет", "Свой", "Клиент", "Радуга").visible(() -> this.mode.is("Туман"));

    public final ColorSetting fogColor = new ColorSetting("Цвет", ColorMathUtil.rgba(255, 255, 255, 230)).visible(() -> this.mode.is("Туман") && this.fogColorMode.is("Свой"));

    public final FloatSetting fogStrength = new FloatSetting("Сила", 0.85F, 0.0F, 2.0F, 0.05F).visible(() -> this.mode.is("Туман"));

    public final FloatSetting fogRiseSpeed = new FloatSetting("Скорость подъема", 0.0F, 0.0F, 2.0F, 0.05F).visible(() -> this.mode.is("Туман"));

    public final FloatSetting fogWobble = new FloatSetting("Колебания", 0.65F, 0.0F, 2.0F, 0.05F).visible(() -> this.mode.is("Туман"));

    public final FloatSetting fogLength = new FloatSetting("Длина", 0.95F, 0.1F, 2.5F, 0.05F).visible(() -> this.mode.is("Туман"));

    public final FloatSetting fogBrightness = new FloatSetting("Яркость", 0.9F, 0.0F, 2.0F, 0.05F).visible(() -> this.mode.is("Туман"));

    public ShaderHands() {
        super("ShaderHands", "Красивый шейдер на руки и предметы", ModuleCategory.RENDER);
        this.addSettings(this.mode, this.waveSpeed, this.plasmaColorFirst, this.plasmaColorSecond, this.plasmaSpeed, this.plasmaScale, this.fireColorMode, this.fireColor, this.fireIntensity, this.fireTrail, this.fireSize, this.fireSpeed, this.fireDensity, this.fireGlow, this.fireSmoke, this.fireSoftness, this.fireBlur, this.fireHandFade, this.fireHandSoftness, this.fireHandBloom, this.glassTransparency, this.glassDistortion, this.glassSpeed, this.glassBlur, this.glassBlurIntensity, this.outline, this.glow, this.fill, this.optimize, this.fogOnlyItems, this.fogColorMode, this.fogColor, this.fogStrength, this.fogRiseSpeed, this.fogWobble, this.fogLength, this.fogBrightness);
    }

    @Override
    public void onDisable() {
        handsOverlayRenderer.invalidateState();
        handsOverlayRenderer.resetTrail();
        super.onDisable();
    }

    public int resolveFogColor() {
        if (this.fogColorMode.is("Свой")) {
            return this.fogColor.getColor();
        } else if (this.fogColorMode.is("Клиент")) {
            if (Toshiba.INSTANCE != null && Toshiba.INSTANCE.themeStorage != null && Toshiba.INSTANCE.themeStorage.getThemes() != null) {
                Theme theme = Toshiba.INSTANCE.themeStorage.getThemes().getTheme();
                if (theme != null && theme.color != null && theme.color.length > 0) {
                    int intValue = theme.color[0];
                    int intValueValue = theme.color.length > 1 ? theme.color[1] : intValue;
                    float floatValue = (float) ((Math.sin(System.currentTimeMillis() / 520.0) + 1.0) * 0.5);
                    return ColorMathUtil.gradient(intValue, intValueValue, floatValue);
                }
            }
            float floatValueValue = (float) ((Math.sin(System.currentTimeMillis() / 520.0) + 1.0) * 0.5);
            return ColorMathUtil.gradient(cyanGradientColor, pinkGradientColor, floatValueValue);
        } else if (this.fogColorMode.is("Радуга")) {
            float floatValueResult = (float) (System.currentTimeMillis() % 4000L) / 4000.0F;
            return -436207616 | Color.HSBtoRGB(floatValueResult, 0.72F, 1.0F) & 16777215;
        } else {
            return this.fogColor.getColor();
        }
    }

    public int resolveFireColor() {
        if (this.fireColorMode.is("Тема") && Toshiba.INSTANCE != null && Toshiba.INSTANCE.themeStorage != null && Toshiba.INSTANCE.themeStorage.getThemes() != null) {
            Theme theme = Toshiba.INSTANCE.themeStorage.getThemes().getTheme();
            if (theme != null && theme.color != null && theme.color.length > 0) {
                return theme.color[0];
            }
        }
        return this.fireColor.getColor();
    }

    public boolean shouldRenderFog() {
        return mc != null && mc.player != null && mc.world != null ? !this.fogOnlyItems.isState() || !mc.player.getMainHandStack().isEmpty() || !mc.player.getOffHandStack().isEmpty() : false;
    }

    @EventHandler(priority = 0)
    public void onRender2D(HudRenderEvent event) {
        if (this.isEnable()) {
            handsOverlayRenderer.renderOverlayIfPending();
        }
    }

    public static class NamedFloatSetting extends FloatSetting {

        public final ShaderHands shaderHands;

        public NamedFloatSetting(ShaderHands shaderHands, String text, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
            super(text, floatValue, floatValueValue, floatValueResult, floatValueItem);
            this.shaderHands = shaderHands;
        }

        @Override
        public String displayName() {
            return this.shaderHands.mode.is("Жидкое стекло") ? "Прозрачность обводки" : super.displayName();
        }
    }
}
