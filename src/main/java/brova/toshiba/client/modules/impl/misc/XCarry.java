package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarry extends Module {

    public static XCarry INSTANCE = new XCarry();

    public XCarry() {
        super("XCarry", "Дополнительные слоты", ModuleCategory.MISC);
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null && eventPacket.getPacket() instanceof CloseHandledScreenC2SPacket && mc.currentScreen instanceof InventoryScreen) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(eventPacket, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((EventPacket) accessReceiver).cancel();
            });
        }
    }
}
