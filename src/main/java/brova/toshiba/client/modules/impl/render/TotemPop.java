package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.text.ChatUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class TotemPop extends Module {

    public static TotemPop INSTANCE = new TotemPop();

    public TotemPop() {
        super("TotemPop", "Отображает эффект и пишет в чат при срабатывании тотема", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.world != null && mc.player != null && eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof EntityStatusS2CPacket entityStatusS2CPacket && entityStatusS2CPacket.getStatus() == 35) {
            mc.execute(() -> this.updateEntityWithName(entityStatusS2CPacket));
        }
    }

    private void updateEntityWithName(EntityStatusS2CPacket entityStatusS2CPacket) {
        if (mc.world != null && mc.player != null && entityStatusS2CPacket.getEntity(mc.world) instanceof AbstractClientPlayerEntity player && player != mc.player) {
            String string = player.getName().getString();
            ChatUtil.sendMessage(string + " §7потерял тотем");
        }
    }
}
