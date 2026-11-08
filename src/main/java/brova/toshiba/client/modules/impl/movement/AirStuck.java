package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.MovePositionEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.network.PacketUtil;
import brova.toshiba.client.utils.player.InventoryUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

public final class AirStuck extends Module {

    public static AirStuck INSTANCE = new AirStuck();

    private final ModeSetting mode = new ModeSetting("Мод", "Обычный", "Обычный", "SpaceTime");

    private final BooleanSetting cancelPackets = new BooleanSetting("Отменять пакеты", true);

    private final BooleanSetting swapElytra = new BooleanSetting("Свапать элитру", true);

    private Vec3d zero = Vec3d.ZERO;

    private boolean stateFlag = false;

    public AirStuck() {
        super("AirStuck", "Зависает в воздухе", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode, this.cancelPackets, this.swapElytra);
    }

    @Override
    public void onEnable() {
        this.stateFlag = false;
        if (mc.player != null && this.swapElytra.isState()) {
            this.equipElytra();
        }
        if (mc.player != null) {
            Vec3d pos = mc.player.getPos();
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, pos, (accessReceiver, accessValue) -> {
                accessReceiver.zero = accessValue;
            });
            if (this.mode.is("Обычный")) {
                this.stateFlag = true;
            }
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.stateFlag = false;
        super.onDisable();
    }

    private void equipElytra() {
        ItemStack equippedStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (equippedStack.isOf(Items.ELYTRA)) {
            int intValue = InventoryUtil.findBestChestplateSlot();
            if (intValue != -1) {
                this.swapChestSlot(intValue);
            }
        }
    }

    private void swapChestSlot(int intValue) {
        if (intValue >= 0 && intValue < 9) {
            mc.interactionManager.clickSlot(0, 6, intValue, SlotActionType.SWAP, mc.player);
        } else {
            mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.SWAP, mc.player);
            mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.SWAP, mc.player);
            mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.SWAP, mc.player);
        }
        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
    }

    @EventHandler
    public void onMove(MovePositionEvent event) {
        if (mc.player != null) {
            if (this.mode.is("SpaceTime") && !this.stateFlag && mc.player.fallDistance > 0.0F && mc.player.getVelocity().y < 0.0) {
                Vec3d pos = mc.player.getPos();
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, pos, (accessReceiver, accessValue) -> {
                    accessReceiver.zero = accessValue;
                });
                this.stateFlag = true;
            }
            if (this.stateFlag) {
                event.setMovePos(Vec3d.ZERO);
                mc.player.setPosition(this.zero.x, this.zero.y, this.zero.z);
                mc.player.setVelocity(0.0, 0.0, 0.0);
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && this.stateFlag && eventPacket.getType() == Type.SEND && eventPacket.getPacket() instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
            if (this.cancelPackets.isState()) {
                eventPacket.cancel();
            } else {
                eventPacket.cancel();
                PacketUtil.sendSilentPacket(this.freezeMovementPacket(playerMoveC2SPacket));
            }
        }
    }

    private PlayerMoveC2SPacket freezeMovementPacket(PlayerMoveC2SPacket playerMoveC2SPacket) {
        boolean onGround = playerMoveC2SPacket.isOnGround();
        boolean flag = playerMoveC2SPacket.horizontalCollision();
        if (playerMoveC2SPacket.changesPosition() && playerMoveC2SPacket.changesLook()) {
            return new Full(this.zero.x, this.zero.y, this.zero.z, playerMoveC2SPacket.getYaw(mc.player.getYaw()), playerMoveC2SPacket.getPitch(mc.player.getPitch()), onGround, flag);
        } else if (playerMoveC2SPacket.changesPosition()) {
            return new PositionAndOnGround(this.zero.x, this.zero.y, this.zero.z, onGround, flag);
        } else {
            return (playerMoveC2SPacket.changesLook() ? new LookAndOnGround(playerMoveC2SPacket.getYaw(mc.player.getYaw()), playerMoveC2SPacket.getPitch(mc.player.getPitch()), onGround, flag) : new OnGroundOnly(onGround, flag));
        }
    }
}
