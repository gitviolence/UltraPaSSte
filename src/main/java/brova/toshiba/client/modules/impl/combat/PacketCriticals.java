package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.player.CriticalHitUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public class PacketCriticals extends Module {

    public static PacketCriticals INSTANCE = new PacketCriticals();

    public PacketCriticals() {
        super("PacketCriticals", "Бьет критами когда в паутине, с эффектом плавного падения.", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onAttack(TargetPlayerEvent event) {
        if (mc.player != null && mc.world != null) {
            boolean inCobweb = CriticalHitUtil.isInCobweb();
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            if (inCobweb) {
                mc.player.networkHandler.sendPacket(new PositionAndOnGround(x, y + 0.003, z, false, false));
                mc.player.networkHandler.sendPacket(new PositionAndOnGround(x, y, z, false, false));
            }
        }
    }
}
