package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.mixin.ILivingEntity;
import brova.toshiba.mixin.IMinecraftClientAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class NoDelay extends Module {

    public static NoDelay INSTANCE = new NoDelay();

    public final MultiSetting noDelayModes = new MultiSetting("Режимы", new BooleanSetting("Прыжок", true), new BooleanSetting("ПКМ", true), new BooleanSetting("Опыт", true));

    public NoDelay() {
        super("NoDelay", "Убирает задержку на различные действия", ModuleCategory.PLAYER);
        this.addSettings(this.noDelayModes);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.noDelayModes.is("Прыжок")) {
                ((ILivingEntity) mc.player).setJumpingCooldown(0);
            }
            if (this.noDelayModes.is("ПКМ")) {
                ((IMinecraftClientAccessor) mc).setItemUseCooldown(0);
            } else if (this.noDelayModes.is("Опыт")) {
                ItemStack mainHandStack = mc.player.getMainHandStack();
                if (mainHandStack.isOf(Items.EXPERIENCE_BOTTLE)) {
                    ((IMinecraftClientAccessor) mc).setItemUseCooldown(0);
                }
            }
        }
    }
}
