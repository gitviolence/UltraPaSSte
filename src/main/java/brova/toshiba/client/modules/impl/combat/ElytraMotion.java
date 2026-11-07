package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ClientTickEndEvent;
import brova.toshiba.client.event.impl.MovePositionEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.network.PacketUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public final class ElytraMotion extends Module {

    public static ElytraMotion INSTANCE = new ElytraMotion();

    public ModeSetting mode = new ModeSetting("Режим", "Обычный", "Обычный", "GroundedGrim");

    public FloatSetting distance = new FloatSetting("Дистанция до игрока", 3.0F, 0.0F, 6.0F, 0.1F);

    private int motionTicks;

    private int boostCooldownTicks;

    private boolean stateFlag;

    private double cachedCoordinate;

    public ElytraMotion() {
        super("ElytraMotion", "Зависает рядом с таргетом на элитре", ModuleCategory.COMBAT);
        this.addSettings(this.mode, this.distance);
    }

    @Override
    public void onEnable() {
        this.motionTicks = 0;
        this.boostCooldownTicks = 0;
        this.stateFlag = false;
        this.cachedCoordinate = 0.0;
        super.onEnable();
    }

    private boolean isTargetWithGliding() {
        AttackAura aura = ModuleRegistry.aura;
        return mc.player != null && mc.world != null && aura.getTarget() != null && mc.player.isGliding() && mc.player.distanceTo(aura.getTarget()) < brova.toshiba.client.utils.runtime.AccessGuard.invoke(this.distance, new Object[] {}, (accessReceiver, accessArguments) -> ((FloatSetting) accessReceiver).getValue()).floatValue();
    }

    @EventHandler
    public void onMove(MovePositionEvent event) {
        if (this.isTargetWithGliding()) {
            float yaw = mc.player.getYaw();
            double doubleValue = Math.toRadians(yaw);
            double doubleValueValue = 0.01;
            double doubleValueResult = -Math.sin(doubleValue) * doubleValueValue;
            double doubleValueItem = Math.cos(doubleValue) * doubleValueValue;
            double doubleValueEntry;
            if (this.mode.is("GroundedGrim")) {
                if (this.boostCooldownTicks > 0) {
                    this.motionTicks++;
                    double doubleValueCopy = this.cachedCoordinate != 0.0 ? this.cachedCoordinate / this.boostCooldownTicks : 0.0;
                    doubleValueEntry = doubleValueCopy + ((this.motionTicks & 1) == 0 ? 0.0015 : -0.0015);
                } else {
                    this.cachedCoordinate -= 0.04;
                    doubleValueResult = 0.0;
                    doubleValueItem = 0.0;
                    doubleValueEntry = 0.0;
                }
            } else {
                doubleValueEntry = -1.0E-4;
            }
            event.setMovePos(new Vec3d(doubleValueResult, doubleValueEntry, doubleValueItem));
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && this.mode.is("GroundedGrim")) {
            if (eventPacket.getType() == Type.RECEIVE) {
                if (eventPacket.getPacket() instanceof PlayerPositionLookS2CPacket) {
                    this.boostCooldownTicks = 6;
                    this.stateFlag = true;
                    this.cachedCoordinate = 0.0;
                }
            } else if (eventPacket.getType() == Type.SEND && eventPacket.getPacket() instanceof PlayerMoveC2SPacket) {
                if (this.isTargetWithGliding() && this.boostCooldownTicks == 0 && !this.stateFlag) {
                    PacketUtil.sendSilentPacket(new OnGroundOnly(mc.player.isOnGround(), mc.player.horizontalCollision));
                    eventPacket.cancel();
                }
                this.stateFlag = false;
            }
        }
    }

    @EventHandler
    public void onTick(ClientTickEndEvent event) {
        if (this.boostCooldownTicks > 0) {
            this.boostCooldownTicks--;
        }
    }

    @Override
    public void onDisable() {
        this.motionTicks = 0;
        this.boostCooldownTicks = 0;
        this.stateFlag = false;
        this.cachedCoordinate = 0.0;
        super.onDisable();
    }
}
