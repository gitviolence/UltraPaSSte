package brova.toshiba.client.storages.implement;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ClientTickEvent;
import brova.toshiba.client.events.implement.EventPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class ServerStorage implements MinecraftAccess {

    private int serverSlot;

    private float serverYaw;

    private float serverPitch;

    private float fallDistance;

    private double serverX;

    private double serverY;

    private double serverZ;

    private boolean serverOnGround;

    private boolean serverSprinting;

    private boolean serverSneaking;

    private boolean serverHorizontalCollision;

    public void ServerManager() {
        EventBus.register(this);
    }

    @EventHandler
    public void onTick(ClientTickEvent event) {
        if (mc.player != null && mc.world != null) {
            double doubleValue = mc.player.prevY - mc.player.getY();
            if (mc.player.isOnGround()) {
                this.fallDistance = 0.0F;
            } else if (doubleValue > 0.0) {
                this.fallDistance += (float) doubleValue;
            }
        }
    }

    @EventHandler
    public void onPacketSend(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null) {
            if (eventPacket.getPacket() instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
                if (playerMoveC2SPacket.changesPosition()) {
                    this.serverX = playerMoveC2SPacket.getX(mc.player.getX());
                    this.serverY = playerMoveC2SPacket.getY(mc.player.getY());
                    this.serverZ = playerMoveC2SPacket.getZ(mc.player.getZ());
                }
                if (playerMoveC2SPacket.changesLook()) {
                    this.serverYaw = playerMoveC2SPacket.getYaw(mc.player.getYaw());
                    this.serverPitch = playerMoveC2SPacket.getPitch(mc.player.getPitch());
                }
                this.serverOnGround = playerMoveC2SPacket.isOnGround();
                this.serverHorizontalCollision = playerMoveC2SPacket.horizontalCollision();
            }
            if (eventPacket.getPacket() instanceof UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket) {
                this.serverSlot = updateSelectedSlotC2SPacket.getSelectedSlot();
            }
            if (eventPacket.getPacket() instanceof ClientCommandC2SPacket clientCommandC2SPacket) {
                switch(clientCommandC2SPacket.getMode()) {
                    case START_SPRINTING:
                        eventPacket.setCancelled(this.serverSprinting);
                        if (!eventPacket.isCancelled()) {
                            this.serverSprinting = true;
                        }
                        break;
                    case STOP_SPRINTING:
                        eventPacket.setCancelled(!this.serverSprinting);
                        if (!eventPacket.isCancelled()) {
                            this.serverSprinting = false;
                        }
                        break;
                    case PRESS_SHIFT_KEY:
                        this.serverSneaking = true;
                        break;
                    case RELEASE_SHIFT_KEY:
                        this.serverSneaking = false;
                }
            }
        }
    }

    public int getServerSlot() {
        return this.serverSlot;
    }

    public float getServerYaw() {
        return this.serverYaw;
    }

    public float getServerPitch() {
        return this.serverPitch;
    }

    public float getFallDistance() {
        return this.fallDistance;
    }

    public double getServerX() {
        return this.serverX;
    }

    public double getServerY() {
        return this.serverY;
    }

    public double getServerZ() {
        return this.serverZ;
    }

    public boolean isServerOnGround() {
        return this.serverOnGround;
    }

    public boolean isServerSprinting() {
        return this.serverSprinting;
    }

    public boolean isServerSneaking() {
        return this.serverSneaking;
    }

    public boolean isServerHorizontalCollision() {
        return this.serverHorizontalCollision;
    }
}
