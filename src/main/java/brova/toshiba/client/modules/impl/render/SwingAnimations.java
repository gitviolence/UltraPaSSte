package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;

public final class SwingAnimations extends Module {

    public static SwingAnimations INSTANCE = new SwingAnimations();

    public boolean swimmingAnimation = true;

    public boolean climbAndCrawl = true;

    public boolean mb3DCompat = false;

    public final BooleanSetting hmiEnable = new BooleanSetting("Мод на красивые руки", false);

    public final FloatSetting hmiSmoothness = new FloatSetting("Плавность анимации", 1.0F, 0.1F, 2.0F, 0.05F).visible(this.hmiEnable::isState);

    public final BooleanSetting smoothEnabled = new BooleanSetting("Плавная анимация", false).visible(() -> !this.hmiEnable.isState());

    public final FloatSetting slowAnimationSpeed = new FloatSetting("Плавность", 12.0F, 1.0F, 20.0F, 1.0F).visible(() -> !this.hmiEnable.isState() && this.smoothEnabled.isState());

    public final BooleanSetting auraTargetOnly = new BooleanSetting("Только с AttackAura", false).visible(() -> !this.hmiEnable.isState());

    public final BooleanSetting eatAnim = new BooleanSetting("Анимация еды", false).visible(() -> !this.hmiEnable.isState());

    public final ModeSetting swingType = new ModeSetting("Тип свинга", "1", "1", "Static", "2", "3", "4", "5").visible(() -> !this.hmiEnable.isState());

    public final FloatSetting swingStrength = new FloatSetting("Сила анимации", 1.0F, 0.1F, 3.0F, 0.01F).visible(() -> !this.hmiEnable.isState());

    public final FloatSetting corner = new FloatSetting("Угол 5", 12.0F, 1.0F, 360.0F, 1.0F).visible(() -> !this.hmiEnable.isState() && (this.swingType.is("5") || this.swingType.is("DropDown") || this.swingType.is("3")));

    public final FloatSetting slant = new FloatSetting("Наклон 5", 12.0F, 1.0F, 360.0F, 1.0F).visible(() -> !this.hmiEnable.isState() && (this.swingType.is("5") || this.swingType.is("DropDown") || this.swingType.is("3")));

    public SwingAnimations() {
        super("SwingAnimations", "Красивые анимации на руки", ModuleCategory.RENDER);
        this.addSettings(this.hmiEnable, this.hmiSmoothness, this.smoothEnabled, this.slowAnimationSpeed, this.auraTargetOnly, this.eatAnim, this.swingType, this.swingStrength, this.corner, this.slant);
    }
}
