package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.render.WorldFramebufferRenderer;

public final class ColorSaturation extends Module {

    public final FloatSetting saturation = new FloatSetting("Насыщенность", 1.0F, 0.1F, 2.0F, 0.1F);

    public final BooleanSetting vanillaHud = new BooleanSetting("Изменять ванильный худ", false);

    public ColorSaturation() {
        super("ColorSaturation", "Изменяет насыщенность цветов игры", ModuleCategory.RENDER);
        this.addSettings(this.saturation, this.vanillaHud);
    }

    @Override
    public void onDisable() {
        WorldFramebufferRenderer.getInstance().release();
        super.onDisable();
    }
}
