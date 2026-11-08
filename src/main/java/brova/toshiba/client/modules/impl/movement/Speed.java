package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.PostTravelEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.modules.impl.movement.Speed.BoostState;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.network.PacketUtil;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.time.TimerController;
import brova.toshiba.mixin.IClientPlayerInteractionManager;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class Speed extends Module {

    public static Speed INSTANCE = new Speed();

    private final ModeSetting mode = new ModeSetting("Режим", "Grim Timer", "Grim", "AresMine", "Grim Timer", "Elytra AirStuck");

    private final FloatSetting speed = new FloatSetting("Скорость", 0.55F, 0.1F, 1.5F, 0.01F).visible(() -> this.mode.is("Grim"));

    private final BooleanSetting prediction = new BooleanSetting("Предикт", true).visible(() -> this.mode.is("Grim"));

    private final FloatSetting aresMineSpeed = new FloatSetting("Скорость AresMine", 1.05F, 1.0F, 1.6F, 0.1F).visible(() -> this.mode.is("AresMine"));

    private final FloatSetting aresMineRadius = new FloatSetting("Радиус AresMine", 1.0F, 0.01F, 3.0F, 0.1F).visible(() -> this.mode.is("AresMine"));

    private final FloatSetting aresMinePrediction = new FloatSetting("Предикт AresMine", 1.0F, 0.0F, 5.0F, 0.1F).visible(() -> this.mode.is("AresMine"));

    private BoostState speedBoostState = BoostState.CHARGING;

    private boolean stateFlagForPacket;

    private boolean stateFlagForUpdateNanoTimeWithTimer;

    private int boostWarmupTicks;

    private int groundedTicks;

    private int boostTicks;

    private int pendingSlotRestoreTicks;

    private boolean stateFlagForEnable;

    private long lastUpdateNanos;

    private int previousSelectedSlot = -1;

    private final Queue<CommonPongC2SPacket> pollCommonPongC2SPacketEntries = new ConcurrentLinkedQueue<>();

    public Speed() {
        super("Speed", "Позволяет увеличивать скорость движения", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode, this.speed, this.prediction, this.aresMineSpeed, this.aresMineRadius, this.aresMinePrediction);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.updateReset();
        this.previousSelectedSlot = -1;
        this.boostTicks = 0;
        this.pendingSlotRestoreTicks = 0;
        this.stateFlagForEnable = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && this.previousSelectedSlot != -1) {
            mc.player.getInventory().selectedSlot = this.previousSelectedSlot;
        }
        this.previousSelectedSlot = -1;
        super.onDisable();
        this.updateReset();
        TimerController.reset();
    }

    @EventHandler
    private void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnable() && mc.player != null && mc.world != null) {
            String current = this.mode.getCurrent();
            switch(current) {
                case "G籜im":
                    this.applyBoundingBoxWithGliding();
                    break;
                case "AresMine":
                    this.applyBoundingBoxWithDistanceTo();
                    break;
                case "Grim Timer":
                    this.updateNanoTimeWithOnGround();
                    break;
                case "FunTime Elytra":
                    this.updateInventoryWithSilentPacket();
                    break;
                case "Elytra AirSt爓ck":
                    this.sendPacketWithYaw();
            }
        }
    }

    @EventHandler
    private void updateOnGroundWithSprinting(DirectionalInput directionalInput) {
        if (this.isEnable() && mc.player != null && this.mode.is("Grim Timer")) {
            if (!this.isLocalState()) {
                this.groundedTicks = 0;
                this.updateReset();
                return;
            }
            if (this.speedBoostState == BoostState.BOOSTING) {
                this.groundedTicks = !mc.player.verticalCollision && !mc.player.isOnGround() ? 0 : this.groundedTicks + 1;
                mc.player.setSprinting(true);
                if (this.groundedTicks > 0) {
                    directionalInput.setJump(true);
                }
            }
        }
    }

    @EventHandler
    private void onPacket(EventPacket eventPacket) {
        if (this.isEnable() && mc.player != null && this.mode.is("Grim Timer") && this.stateFlagForPacket) {
            if (eventPacket.getType() == Type.SEND && eventPacket.getPacket() instanceof CommonPongC2SPacket commonPongC2SPacket) {
                this.pollCommonPongC2SPacketEntries.add(commonPongC2SPacket);
                eventPacket.cancel();
                return;
            }
            if (eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof PlayerPositionLookS2CPacket) {
                this.startCharging();
            }
        }
    }

    private void updateNanoTimeWithOnGround() {
        if (mc.player != null) {
            if (!this.isLocalState()) {
                this.updateReset();
            } else {
                long currentTimeNanos = System.nanoTime();
                if (!this.stateFlagForPacket) {
                    this.stateFlagForPacket = true;
                    this.startCharging();
                } else {
                    if (this.speedBoostState == BoostState.BOOSTING) {
                        if (!mc.player.verticalCollision && !mc.player.isOnGround()) {
                            this.stateFlagForUpdateNanoTimeWithTimer = true;
                            if (this.boostWarmupTicks >= 4 && mc.player.getVelocity().y <= 0.0) {
                                this.startCharging();
                                return;
                            }
                        } else if (this.stateFlagForUpdateNanoTimeWithTimer) {
                            this.stateFlagForUpdateNanoTimeWithTimer = false;
                            this.boostWarmupTicks++;
                        }
                    }
                    if (this.speedBoostState == BoostState.CHARGING && currentTimeNanos + -this.lastUpdateNanos >= 1250000000L) {
                        this.startBoosting();
                    } else if (this.speedBoostState == BoostState.BOOSTING && currentTimeNanos + -this.lastUpdateNanos >= 2400000000L) {
                        this.startCharging();
                    }
                }
            }
        }
    }

    private void startCharging() {
        this.updatePollWithSilentPacket();
        this.speedBoostState = BoostState.CHARGING;
        this.stateFlagForUpdateNanoTimeWithTimer = false;
        this.boostWarmupTicks = 0;
        this.groundedTicks = 0;
        this.lastUpdateNanos = System.nanoTime();
        TimerController.setTimer(0.05F);
    }

    private void startBoosting() {
        this.speedBoostState = BoostState.BOOSTING;
        this.stateFlagForUpdateNanoTimeWithTimer = false;
        this.boostWarmupTicks = 0;
        this.groundedTicks = 0;
        this.lastUpdateNanos = System.nanoTime();
        TimerController.setTimer(1.7F);
    }

    private void updateReset() {
        this.updatePollWithSilentPacket();
        this.stateFlagForPacket = false;
        this.speedBoostState = BoostState.CHARGING;
        this.stateFlagForUpdateNanoTimeWithTimer = false;
        this.boostWarmupTicks = 0;
        this.groundedTicks = 0;
        this.lastUpdateNanos = 0L;
        TimerController.reset();
    }

    private void updatePollWithSilentPacket() {
        CommonPongC2SPacket commonPongC2SPacket;
        while ((commonPongC2SPacket = this.pollCommonPongC2SPacketEntries.poll()) != null) {
            PacketUtil.sendSilentPacket(commonPongC2SPacket);
        }
    }

    private void applyBoundingBoxWithGliding() {
        AttackAura aura = ModuleRegistry.aura;
        if (aura != null && aura.isEnable() && (this.prediction.isState() || aura.getMoveFix().is("Таргетированная"))) {
            LivingEntity target = aura.getTarget();
            if (target != null && target != mc.player) {
                Box box = mc.player.getBoundingBox().expand(1.0);
                boolean flag = false;
                if (mc.player.isGliding() || target.getBoundingBox().intersects(box)) {
                    if (mc.player.isGliding()) {
                        double doubleValue = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
                        if (doubleValue <= 2.5) {
                            flag = true;
                        }
                    } else {
                        flag = true;
                    }
                }
                if (flag) {
                    Vec3d directStrafeVelocity = this.getDirectStrafeVelocity(target);
                    mc.player.setVelocity(directStrafeVelocity);
                }
            }
        }
    }

    private void applyBoundingBoxWithDistanceTo() {
        AttackAura aura = ModuleRegistry.aura;
        if (aura != null && aura.isEnable()) {
            LivingEntity target = aura.getTarget();
            if (target != null && target != mc.player) {
                Box box = mc.player.getBoundingBox().expand(this.aresMineRadius.get());
                boolean flag = target.getBoundingBox().intersects(box);
                if (mc.player.isGliding()) {
                    Vec3d position = this.createPositionFromTarget(target);
                    double doubleValue = mc.player.getEyePos().distanceTo(position);
                    double doubleValueValue = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
                    flag = doubleValue <= 2.5 || doubleValueValue <= 2.5;
                }
                if (flag) {
                    mc.player.setVelocity(this.getPredictedStrafeVelocity(target));
                }
            }
        }
    }

    @NotNull
    private Vec3d getPredictedStrafeVelocity(LivingEntity entity) {
        Vec3d position = this.createPositionFromTarget(entity);
        double doubleValue = position.x + -mc.player.getX();
        double doubleValueValue = position.z + -mc.player.getZ();
        float floatValue = (float) (Math.toDegrees(Math.atan2(doubleValueValue, doubleValue)) + -90.0);
        double doubleValueResult = Math.toRadians(floatValue);
        double doubleValueItem = 0.072 * this.aresMineSpeed.get();
        Vec3d velocity = mc.player.getVelocity();
        return new Vec3d(velocity.x + -Math.sin(doubleValueResult) * doubleValueItem, velocity.y, velocity.z + Math.cos(doubleValueResult) * doubleValueItem);
    }

    @NotNull
    private Vec3d createPositionFromTarget(LivingEntity entity) {
        double doubleValue = this.aresMinePrediction.get();
        if (doubleValue <= 0.001) {
            return entity.getPos();
        } else {
            double doubleValueValue = entity.getX() + -entity.prevX;
            double doubleValueResult = entity.getZ() + -entity.prevZ;
            return entity.getPos().add(doubleValueValue * doubleValue, 0.0, doubleValueResult * doubleValue);
        }
    }

    private void updateInventoryWithSilentPacket() {
        int selectedSlot = InventoryUtil.find(Items.ELYTRA, 0, 8);
        if (selectedSlot != -1) {
            float yaw = mc.gameRenderer.getCamera().getYaw();
            if (mc.options.getPerspective().isFrontView()) {
                yaw -= 180.0F;
            }
            RotationStorage.update(new Rotation(yaw, 55.0F), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, false);
            if (this.previousSelectedSlot == -1) {
                this.previousSelectedSlot = mc.player.getInventory().selectedSlot;
            }
            if (mc.player.getInventory().selectedSlot != selectedSlot) {
                mc.player.getInventory().selectedSlot = selectedSlot;
            } else if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                PacketUtil.sendSilentPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                ((IClientPlayerInteractionManager) mc.interactionManager).invokeSendSequencedPacket(mc.world, input -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, input, mc.player.getYaw(), mc.player.getPitch()));
                PacketUtil.sendSilentPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
                mc.player.startGliding();
                PacketUtil.sendSilentPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            }
        }
    }

    private void sendPacketWithYaw() {
        if (this.pendingSlotRestoreTicks > 0) {
            this.pendingSlotRestoreTicks += -1;
        } else {
            if (++this.boostTicks >= 40) {
                this.boostTicks = 0;
                this.pendingSlotRestoreTicks = this.stateFlagForEnable ? 1 : 1;
                this.stateFlagForEnable = !this.stateFlagForEnable;
            }
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
            mc.player.startGliding();
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
        }
    }

    @EventHandler
    private void onPostTravel(PostTravelEvent event) {
        if (this.isEnable() && this.mode.is("FunTime Elytra") && mc.player != null && !mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            mc.player.stopGliding();
        }
    }

    @NotNull
    private Vec3d getDirectStrafeVelocity(LivingEntity entity) {
        double doubleValue = entity.getX() + -mc.player.getX();
        double doubleValueValue = entity.getZ() + -mc.player.getZ();
        float floatValue = (float) (Math.toDegrees(Math.atan2(doubleValueValue, doubleValue)) + -90.0);
        double doubleValueResult = Math.toRadians(floatValue);
        double doubleValueItem = 0.072 * this.speed.get();
        Vec3d velocity = mc.player.getVelocity();
        return new Vec3d(velocity.x + -Math.sin(doubleValueResult) * doubleValueItem, velocity.y, velocity.z + Math.cos(doubleValueResult) * doubleValueItem);
    }

    private boolean isLocalState() {
        return mc.player != null && (mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F);
    }

    public enum BoostState {

        CHARGING, BOOSTING
    }
}
