package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.ModeSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public final class Velocity extends Module {

    public static Velocity INSTANCE = new Velocity();

    private final ModeSetting mode = new ModeSetting("Режим", "Обычный", "Обычный", "Легитный");

    public Velocity() {
        super("Velocity", "Отключает отдачу от урона", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode);
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null && eventPacket.getType() == Type.RECEIVE) {
            if (eventPacket.getPacket() instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket) {
                if (entityVelocityUpdateS2CPacket.getEntityId() != mc.player.getId()) {
                    return;
                }
                if (this.mode.is("Обычный")) {
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(eventPacket, new Object[] {}, (accessReceiver, accessArguments) -> {
                        ((EventPacket) accessReceiver).cancel();
                    });
                }
                if (this.mode.is("Легитный")) {
                    eventPacket.cancel();
                    double doubleValue = entityVelocityUpdateS2CPacket.getVelocityY() / 8000.0;
                    if (mc.player.isOnGround() && doubleValue > 0.0) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
                    } else if (doubleValue > 0.0) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
                    }
                }
            }
            if (eventPacket.getPacket() instanceof ExplosionS2CPacket && (this.mode.is("Обычный") || this.mode.is("Легитный"))) {
                eventPacket.cancel();
            }
        }
    }
}
