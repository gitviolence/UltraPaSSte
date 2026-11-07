package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.network.BotManager;
import java.util.UUID;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class ServerRPSpoofer extends Module {

    public static ServerRPSpoofer INSTANCE = new ServerRPSpoofer();

    public ServerRPSpoofer() {
        super("ServerRPSpoofer", "Кидает пакет серверу что вы приняли ресурс пак но на самом деле отклонили.", ModuleCategory.PLAYER);
    }

    @EventHandler
    public void onReceivePacket(EventPacket eventPacket) {
        if (eventPacket.getPacket() instanceof ResourcePackSendS2CPacket resourcePackSendS2CPacket && (this.isEnable() || BotManager.shouldBypassResourcePacks())) {
            UUID uuid = resourcePackSendS2CPacket.id();
            mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(uuid, Status.ACCEPTED));
            mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(uuid, Status.SUCCESSFULLY_LOADED));
            eventPacket.setCancelled(true);
        }
    }
}
