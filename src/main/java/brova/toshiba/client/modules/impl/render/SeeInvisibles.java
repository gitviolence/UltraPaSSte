package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class SeeInvisibles extends Module {

    public static final float INVISIBLE_ALPHA = 0.7F;

    public static final int INVISIBLE_COLOR = Math.round(178.5F) << 24 | 16777215;

    public static SeeInvisibles INSTANCE = new SeeInvisibles();

    public final FloatSetting alpha = new FloatSetting("Прозрачность", 0.7F, 0.05F, 1.0F, 0.05F);

    public SeeInvisibles() {
        super("SeeInvisibles", "Показывает невидимых игроков", ModuleCategory.RENDER);
        this.addSettings(this.alpha);
    }

    public int getInvisibleColor() {
        int clampedValue = MathHelper.clamp(Math.round(this.alpha.get() * 255.0F), 0, 255);
        return clampedValue << 24 | 16777215;
    }

    public boolean shouldRenderInvisible(PlayerEntity player) {
        return this.isEnable() && mc.player != null && player != null && (player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY));
    }
}
