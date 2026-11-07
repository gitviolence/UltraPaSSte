package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.shader.WorldPostProcessor;
import net.minecraft.client.MinecraftClient;

public final class CustomWorld extends Module {

    public static CustomWorld INSTANCE = new CustomWorld();

    private final MultiSetting blurEnabled = new MultiSetting("Настройки мира", new BooleanSetting("Время", true), new BooleanSetting("Туман", true), new BooleanSetting("Шейдер", false), new BooleanSetting("Блюр", false));

    private final FloatSetting forcedTime = new FloatSetting("Время", 12.0F, 0.0F, 24.0F, 1.0F).visible(() -> this.blurEnabled.is("Время"));

    private final FloatSetting fogDistance = new FloatSetting("Дистанция тумана", 100.0F, 20.0F, 200.0F, 1.0F).visible(() -> this.blurEnabled.is("Туман"));

    private final ModeSetting shaderMode = new ModeSetting("Режим шейдера", "Fog", "Fog", "Caustic", "Sunset", "Nebula", "Северное сияние").visible(() -> this.blurEnabled.is("Шейдер"));

    private final ColorSetting polarColor2 = new ColorSetting("Цвет шейдера", -1).visible(() -> this.blurEnabled.is("Шейдер") && !this.shaderMode.is("Fog"));

    private final FloatSetting polarSpeed = new FloatSetting("Скорость", 1.0F, 0.1F, 4.0F, 0.1F).visible(() -> this.blurEnabled.is("Шейдер") && !this.shaderMode.is("Fog"));

    private final FloatSetting blurStrength = new FloatSetting("Сила размытия", 3.0F, 1.0F, 20.0F, 0.5F).visible(() -> this.blurEnabled.is("Блюр"));

    private final FloatSetting blurDistance = new FloatSetting("Дистанция размытия", 80.0F, 0.0F, 200.0F, 1.0F).visible(() -> this.blurEnabled.is("Блюр"));

    private final BooleanSetting linearSampling = new BooleanSetting("Линейная выборка", true).visible(() -> this.blurEnabled.is("Блюр"));

    private WorldPostProcessor applyWorldPostProcessor;

    public CustomWorld() {
        super("CustomWorld", "Настройки мира", ModuleCategory.RENDER);
        this.addSettings(this.blurEnabled, this.forcedTime, this.fogDistance, this.shaderMode, this.polarColor2, this.polarSpeed, this.blurStrength, this.blurDistance, this.linearSampling);
    }

    public boolean isTimeEnabled() {
        return this.isEnable() && this.blurEnabled.is("Время");
    }

    public boolean isFogEnabled() {
        return this.isEnable() && this.blurEnabled.is("Туман");
    }

    public boolean isShaderSkyEnabled() {
        return this.isEnable() && this.blurEnabled.is("Шейдер");
    }

    public boolean isBlurEnabled() {
        return this.isEnable() && this.blurEnabled.is("Блюр");
    }

    public ModeSetting getShaderMode() {
        return this.shaderMode;
    }

    public long getForcedTime() {
        return (long) this.forcedTime.get() * 1000L;
    }

    public float getFogDistance() {
        return this.fogDistance.get();
    }

    public int getFogColor() {
        return Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color[0];
    }

    public float[] getPolarColor1() {
        int color = this.polarColor2.getColor();
        return new float[] { ColorMathUtil.redf(color), ColorMathUtil.greenf(color), ColorMathUtil.bluef(color) };
    }

    public float[] getPolarColor2() {
        int intValue = ColorMathUtil.darken(this.polarColor2.getColor(), 0.35F);
        return new float[] { ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue) };
    }

    public float getPolarSpeed() {
        return this.polarSpeed.get();
    }

    private int calculateThemeWithThemes(int intValue) {
        int[] color = Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color;
        return intValue != 0 && color.length >= 2 ? color[Math.min(1, color.length + -1)] : color[0];
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (this.isBlurEnabled()) {
            MinecraftClient instance = MinecraftClient.getInstance();
            if (instance.player != null && instance.world != null && instance.getFramebuffer() != null && (instance.gameRenderer == null || !instance.gameRenderer.isRenderingPanorama())) {
                if (this.applyWorldPostProcessor == null) {
                    this.applyWorldPostProcessor = new WorldPostProcessor();
                }
                this.applyWorldPostProcessor.apply(instance, this.blurStrength.get(), this.blurDistance.get(), this.linearSampling.isState());
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.applyWorldPostProcessor != null) {
            this.applyWorldPostProcessor.close();
            this.applyWorldPostProcessor = null;
        }
    }
}
