package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.GameUpdateEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MaceAura extends Module {

    public static MaceAura INSTANCE = new MaceAura();

    private final FloatSetting horizontalRange = new FloatSetting("Горизонтальная дистанция", 5.0F, 1.0F, 12.0F, 0.1F);

    private final FloatSetting verticalRange = new FloatSetting("Вертикальная дистанция", 30.0F, 1.0F, 64.0F, 0.5F);

    private final FloatSetting attackRange = new FloatSetting("Дистанция удара", 3.0F, 1.0F, 6.0F, 0.1F);

    private final BooleanSetting requireLineOfSight = new BooleanSetting("Не наводиться через стены", false);

    private final BooleanSetting breakShields = new BooleanSetting("Ломать щит", true);

    private LivingEntity target;

    private long lastUpdateMillis;

    private boolean stateFlag;

    public MaceAura() {
        super("MaceAura", "Наводится и атакует булавой во время падения.", ModuleCategory.COMBAT);
        this.addSettings(this.horizontalRange, this.verticalRange, this.attackRange, this.requireLineOfSight, this.breakShields);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            if (!this.isVelocity()) {
                this.target = null;
                this.stateFlag = false;
                this.updateExternalTarget();
                this.updateReturnToCamera();
            } else {
                LivingEntity entity = this.findTargetFromLastTargetWithEntities();
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, entity, (accessReceiver, accessValue) -> {
                    accessReceiver.target = accessValue;
                });
                boolean of = mc.player.getMainHandStack().isOf(Items.MACE);
                boolean blockingWithItem = this.isBlockingWithItem(this.target);
                if (!of && !blockingWithItem) {
                    entity = null;
                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, entity, (accessReceiver, accessValue) -> {
                        accessReceiver.target = accessValue;
                    });
                    this.stateFlag = false;
                    this.updateExternalTarget();
                    this.updateReturnToCamera();
                } else {
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                        ((MaceAura) accessReceiver).updateExternalTarget();
                    });
                    if (this.target == null) {
                        this.stateFlag = false;
                        this.updateReturnToCamera();
                    } else if (this.calculateBoundingBoxWithDistanceTo(this.target) > this.attackRange.get()) {
                        this.stateFlag = false;
                    } else if (blockingWithItem) {
                        this.sendCurrentTimeMillisWithAttackEntity((PlayerEntity) this.target);
                    } else if (System.currentTimeMillis() - this.lastUpdateMillis >= 800L) {
                        mc.interactionManager.attackEntity(mc.player, this.target);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        this.lastUpdateMillis = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGameUpdate(GameUpdateEvent event) {
        if (this.target != null && ModuleRegistry.aura != null && !ModuleRegistry.aura.isEnable()) {
            ModuleRegistry.aura.Rotate();
        }
    }

    private boolean isBlockingWithItem(LivingEntity entity) {
        return this.breakShields.isState() && entity instanceof PlayerEntity player && player.isBlocking() && mc.player.getMainHandStack().getItem() instanceof AxeItem && this.calculateOfWithStack() != -1;
    }

    private void sendCurrentTimeMillisWithAttackEntity(PlayerEntity player) {
        if (!this.stateFlag) {
            mc.interactionManager.attackEntity(mc.player, player);
            mc.player.swingHand(Hand.MAIN_HAND);
            int selectedSlot = this.calculateOfWithStack();
            if (selectedSlot != -1) {
                mc.player.getInventory().selectedSlot = selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            }
            this.stateFlag = true;
            this.lastUpdateMillis = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - this.lastUpdateMillis >= 800L) {
            mc.interactionManager.attackEntity(mc.player, player);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.lastUpdateMillis = System.currentTimeMillis();
            this.stateFlag = false;
        }
    }

    private int calculateOfWithStack() {
        for (int index = 0; index < 9; index++) {
            if (mc.player.getInventory().getStack(index).isOf(Items.MACE)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isVelocity() {
        return mc.player.fallDistance > 0.1F && mc.player.getVelocity().y < -0.1;
    }

    private void updateExternalTarget() {
        if (ModuleRegistry.aura != null) {
            ModuleRegistry.aura.setExternalTarget(this.target);
        }
    }

    private void updateReturnToCamera() {
        if (ModuleRegistry.aura != null && !ModuleRegistry.aura.isEnable()) {
            ModuleRegistry.aura.returnToCamera();
        }
    }

    private LivingEntity findTargetFromLastTargetWithEntities() {
        LivingEntity lastTarget = AttackAura.getLastTarget();
        if (this.isValidTarget(lastTarget)) {
            return lastTarget;
        } else {
            LivingEntity entity = null;
            double MAX_VALUE = Double.MAX_VALUE;
            for (Entity entityValue : mc.world.getEntities()) {
                if (entityValue instanceof LivingEntity entityResult && this.isValidTarget(entityResult)) {
                    double doubleValue = this.calculateBoundingBoxWithDistanceTo(entityResult);
                    if (doubleValue < MAX_VALUE) {
                        entity = entityResult;
                        MAX_VALUE = doubleValue;
                    }
                }
            }
            return entity;
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player && entity != mc.player && entity.isAlive() && !(entity.getHealth() <= 0.0F))) {
            return false;
        } else if (Toshiba.INSTANCE != null && Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString())) {
            return false;
        } else {
            Vec3d pos = mc.player.getPos();
            Vec3d posValue = entity.getPos();
            double doubleValue = Math.hypot(posValue.x - pos.x, posValue.z - pos.z);
            if (!(doubleValue > this.horizontalRange.get()) && !(Math.abs(posValue.y - pos.y) > this.verticalRange.get())) {
                return this.requireLineOfSight.isState() && !mc.player.canSee(entity) ? false : posValue.y <= pos.y;
            } else {
                return false;
            }
        }
    }

    private double calculateBoundingBoxWithDistanceTo(LivingEntity entity) {
        Box boundingBox = entity.getBoundingBox();
        return mc.player.getEyePos().distanceTo(boundingBox.getCenter());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.target = null;
        this.lastUpdateMillis = 0L;
        this.updateExternalTarget();
        this.updateReturnToCamera();
    }

    public LivingEntity getTarget() {
        return this.target;
    }
}
