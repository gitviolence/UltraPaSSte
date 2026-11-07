package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBright extends Module {

    public static FullBright INSTANCE = new FullBright();

    public FullBright() {
        super("FullBright", "Выдает эффект ночного зрения навсегда", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 5000, 1));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            super.onDisable();
        }
    }
}
