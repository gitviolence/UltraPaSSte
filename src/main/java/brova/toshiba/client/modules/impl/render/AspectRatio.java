package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;

public final class AspectRatio extends Module {

    private final ModeSetting preset = new ModeSetting("Пресет", "4:3", "16:9", "16:10", "4:3", "Custom");

    private final FloatSetting stretchAmount = new FloatSetting("Сила растяга", 1.0F, 0.1F, 10.0F, 0.1F).visible(() -> this.preset.is("Custom"));

    public AspectRatio() {
        super("AspectRatio", "Изменяет соотношение сторон игровой камеры", ModuleCategory.RENDER);
        this.addSettings(this.preset, this.stretchAmount);
    }

    public float getProjectionAspect(float floatValue) {
        if (this.preset.is("16:10")) {
            return 2.2F;
        } else if (this.preset.is("4:3")) {
            return 1.6F;
        } else {
            return this.preset.is("Custom") ? this.stretchAmount.get() : floatValue;
        }
    }
}
