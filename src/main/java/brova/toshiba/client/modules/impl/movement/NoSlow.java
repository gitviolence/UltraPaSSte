package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.Cancellable;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.network.ProtocolVersionUtil;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public final class NoSlow extends Module {

    public static NoSlow INSTANCE = new NoSlow();

    private final ModeSetting mode = new ModeSetting("Мод", "Старый грим", "Старый грим", "Грим");

    private final BooleanSetting sprint = new BooleanSetting("Спринт", true);

    public NoSlow() {
        super("NoSlow", "Убирает замедление во время еды", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode, this.sprint);
    }

    @EventHandler
    public void onSlowDown(Cancellable cancellable) {
        if (mc.player != null && mc.player.isUsingItem()) {
            if (this.mode.is("Грим") && mc.player.getItemUseTime() % 2 == 0) {
                cancellable.setCancelled(true);
            }
            if (this.mode.is("Старый грим")) {
                Hand activeHand = mc.player.getActiveHand();
                boolean targetProtocolBelowOneNineteen = ProtocolVersionUtil.isTargetProtocolBelowOneNineteen();
                if (this.sprint.isState()) {
                    mc.player.setSprinting((ModuleRegistry.sprint.isEnable() && Sprint.isSprinting() || mc.options.sprintKey.isPressed()) && mc.player.input.movementForward > 0.0F && (!targetProtocolBelowOneNineteen || !mc.player.horizontalCollision && !mc.player.collidedSoftly) && !mc.player.isGliding());
                }
                Hand hand = activeHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(hand, 0, mc.player.getYaw(), mc.player.getPitch()));
                cancellable.setCancelled(true);
            }
        }
    }
}
