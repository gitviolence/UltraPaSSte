package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class ElytraHop extends Module {

    public static ElytraHop INSTANCE = new ElytraHop();

    private boolean jumpStarted;

    private boolean elytraDeployed;

    public ElytraHop() {
        super("ElytraHop", "Spams jump while an elytra is equipped", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        this.jumpStarted = false;
        this.elytraDeployed = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.applyHandleWithPressed();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null) {
            if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                this.applyHandleWithPressed();
                this.jumpStarted = false;
            } else if (mc.player.isGliding()) {
                this.applyHandleWithPressed();
                this.jumpStarted = false;
            } else {
                mc.options.jumpKey.setPressed(true);
                this.elytraDeployed = true;
                if (!mc.player.isOnGround() && !mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
                    mc.player.startGliding();
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
                }
            }
        }
    }

    private void applyHandleWithPressed() {
        if (this.elytraDeployed && mc.options != null) {
            long handle = mc.getWindow().getHandle();
            mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(handle, mc.options.jumpKey.getDefaultKey().getCode()));
            this.jumpStarted = false;
            this.elytraDeployed = false;
        }
    }
}
