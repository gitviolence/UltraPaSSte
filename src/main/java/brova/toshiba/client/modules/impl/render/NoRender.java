package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.MultiSetting;

public final class NoRender extends Module {

    public static NoRender INSTANCE = new NoRender();

    private final MultiSetting enabled = new MultiSetting("Элементы", new BooleanSetting("Погода", false), new BooleanSetting("Огонь", false), new BooleanSetting("Фон в блоке", false), new BooleanSetting("Облака", false), new BooleanSetting("Плохие эффекты", false), new BooleanSetting("Тотем рендер", false), new BooleanSetting("Свечение", false), new BooleanSetting("Партиклы", false), new BooleanSetting("БоссБар", false), new BooleanSetting("СкорБорд", false));

    public NoRender() {
        super("NoRender", "Убирает элементы рендера.", ModuleCategory.RENDER);
        this.addSettings(this.enabled);
    }

    public boolean isEnabled(String text) {
        return this.isEnable() && this.enabled.is(text);
    }

    public boolean isTotemAnimationDisabled() {
        return this.isEnabled("Тотем рендер");
    }
}
