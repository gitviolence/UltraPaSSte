package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.SprintKeyEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.impl.player.AutoEat;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.player.CriticalHitUtil;
import java.util.Optional;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class TriggerBot extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Умные криты", "Умные криты", "Только криты");

    private final MultiSetting targetTypes = new MultiSetting("Таргеты", new BooleanSetting("Игроки", true), new BooleanSetting("Голые", true), new BooleanSetting("Инвизки", true), new BooleanSetting("Мобы", true), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false));

    private final BooleanSetting weaponOnly = new BooleanSetting("Только с оружием", false);

    private final BooleanSetting throughGrass = new BooleanSetting("Через траву", true);

    private final FloatSetting distance = new FloatSetting("Дистанция", 3.0F, 0.5F, 3.0F, 0.1F);

    private final BooleanSetting attackThroughEntities = new BooleanSetting("Бить через энтити", false);

    private LivingEntity target;

    private int groundTicks;

    private int attackDelayTicks;

    private int sprintResetTicks;

    private int attackPhaseTicks;

    private long lastUpdateMillis;

    private boolean stateFlag;

    public TriggerBot() {
        super("TriggerBot", "Автоматически бьет игроков при наводке на их ХитБокс", ModuleCategory.COMBAT);
        this.addSettings(this.mode, this.targetTypes, this.weaponOnly, this.throughGrass, this.distance, this.attackThroughEntities);
    }

    @EventHandler(priority = -200)
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            this.groundTicks = mc.player.isOnGround() ? this.groundTicks + 1 : 0;
            if (this.attackDelayTicks > 0 && --this.attackDelayTicks == 0) {
                this.updatePopPause();
            }
            if (this.stateFlag) {
                mc.player.setSprinting(false);
            }
            this.target = null;
            if (!this.isBreakingBlockWithShouldSuppressCombat()) {
                this.attackPhaseTicks = 0;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((TriggerBot) accessReceiver).updatePopPause();
                });
            } else if (this.sprintResetTicks > 0) {
                this.sprintResetTicks--;
            } else {
                this.target = this.findTargetFromRaycastWithMultiply();
                if (this.target == null) {
                    this.attackPhaseTicks = 0;
                } else {
                    this.attackPhaseTicks = mc.player.getAttackCooldownProgress(0.0F) > 0.9F ? this.attackPhaseTicks + 1 : 0;
                    if (this.isOnGroundWithVelocityAndPressed()) {
                        if (this.isOnGroundWithVelocityAndStatusEffect()) {
                            if (mc.player.isSprinting()) {
                                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                                    ((TriggerBot) accessReceiver).updatePushPauseWithSprinting();
                                });
                                return;
                            }
                            if (!this.isInstanceWithItem(MaceItem.class) && mc.player.getAttackCooldownProgress(0.5F) <= 0.9F) {
                                return;
                            }
                        }
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                            ((TriggerBot) accessReceiver).updateAttackEntityWithSwingHand();
                        });
                    }
                }
            }
        } else {
            this.updateLocalState();
        }
    }

    private boolean isBreakingBlockWithShouldSuppressCombat() {
        // Бьёт и с открытым инвентарём/меню: экран — чисто клиентское состояние,
        // пакеты атаки сервер принимает как обычно.
        return !mc.interactionManager.isBreakingBlock() && !AutoEat.shouldSuppressCombat() && !this.isInstanceWithItem(CrossbowItem.class) && (!this.weaponOnly.isState() || this.isItem(mc.player.getMainHandStack()) || this.isItem(mc.player.getOffHandStack()));
    }

    private void updatePushPauseWithSprinting() {
        if (!this.stateFlag) {
            Sprint.pushPause(0L);
            this.stateFlag = true;
        }
        this.attackDelayTicks = 2;
        mc.player.setSprinting(false);
    }

    private void updatePopPause() {
        if (this.stateFlag) {
            Sprint.popPause();
            this.stateFlag = false;
        }
        this.attackDelayTicks = 0;
    }

    private void updateAttackEntityWithSwingHand() {
        mc.interactionManager.attackEntity(mc.player, this.target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.resetLastAttackedTicks();
        this.sprintResetTicks = 1;
        this.attackPhaseTicks = 0;
        this.lastUpdateMillis = System.currentTimeMillis() + this.calculateAdjustedCooldown();
    }

    @EventHandler
    public void onSprint(SprintKeyEvent event) {
        if (this.attackDelayTicks > 0) {
            event.cancel();
        }
    }

    private LivingEntity findTargetFromRaycastWithMultiply() {
        Vec3d cameraPosVec = mc.player.getCameraPosVec(1.0F);
        Vec3d rotationVec = mc.player.getRotationVec(1.0F);
        double doubleValue = this.distance.get();
        Vec3d position = cameraPosVec.add(rotationVec.multiply(doubleValue));
        BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(cameraPosVec, position, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
        double doubleValueValue = doubleValue;
        if (blockHitResult.getType() == Type.BLOCK && (!this.throughGrass.isState() || !(mc.world.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof PlantBlock))) {
            doubleValueValue = cameraPosVec.distanceTo(blockHitResult.getPos());
        }
        Box box = mc.player.getBoundingBox().stretch(rotationVec.multiply(doubleValue)).expand(1.0);
        if (!this.attackThroughEntities.isState()) {
            EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, cameraPosVec, position, box, input -> input != mc.player && !input.isSpectator() && input.canHit(), doubleValue * doubleValue);
            return entityHitResult != null && cameraPosVec.distanceTo(entityHitResult.getPos()) <= doubleValueValue + 0.1 && entityHitResult.getEntity() instanceof LivingEntity entityResult && this.isValidTarget(entityResult) ? entityResult : null;
        } else {
            LivingEntity entity = null;
            double doubleValueResult = Math.min(doubleValue * doubleValue, (doubleValueValue + 0.1) * (doubleValueValue + 0.1));
            for (LivingEntity entityValue : mc.world.getEntitiesByClass(LivingEntity.class, box, this::isValidTarget)) {
                Box boxValue = entityValue.getBoundingBox().expand(entityValue.getTargetingMargin());
                var optional = boxValue.raycast(cameraPosVec, position);
                if (boxValue.contains(cameraPosVec) || !optional.isEmpty()) {
                    double doubleValueItem = boxValue.contains(cameraPosVec) ? 0.0 : cameraPosVec.squaredDistanceTo(optional.get());
                    if (doubleValueItem <= doubleValueResult) {
                        doubleValueResult = doubleValueItem;
                        entity = entityValue;
                    }
                }
            }
            return entity;
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == mc.player || !entity.isAlive() || entity.isSpectator() || !entity.canHit()) {
            return false;
        } else if (entity instanceof PlayerEntity player) {
            if (!this.targetTypes.is("Игроки")) {
                return false;
            } else if (this.isUnarmored(player) && !this.targetTypes.is("Голые")) {
                return false;
            } else {
                return player.isInvisible() && !this.targetTypes.is("Инвизки") ? false : !Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString()) || this.targetTypes.is("Друзья");
            }
        } else {
            return !(entity instanceof PassiveEntity) && !(entity instanceof CodEntity) ? entity instanceof HostileEntity && this.targetTypes.is("Мобы") : this.targetTypes.is("Животные");
        }
    }

    private boolean isUnarmored(PlayerEntity player) {
        for (ItemStack stack : player.getArmorItems()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof MaceItem;
    }

    private boolean isInstanceWithItem(Class<? extends Item> type) {
        return type.isInstance(mc.player.getMainHandStack().getItem()) || type.isInstance(mc.player.getOffHandStack().getItem());
    }

    private boolean isInFluid() {
        return mc.player.isTouchingWater() || mc.player.isSwimming() || mc.player.isGliding() || mc.player.isClimbing() || mc.player.isInsideWall() || mc.player.isInLava() || mc.player.hasVehicle() || mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || mc.player.hasStatusEffect(StatusEffects.LEVITATION) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || mc.player.getAbilities().flying;
    }

    private boolean isOnGroundWithVelocityAndStatusEffect() {
        return !mc.player.isOnGround() && mc.player.fallDistance > 0.0F && mc.player.getVelocity().y < -0.01 && !this.isInFluid() && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !mc.player.getAbilities().flying;
    }

    private boolean isStatusEffect() {
        return mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || mc.player.hasStatusEffect(StatusEffects.LEVITATION) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) || mc.player.hasStatusEffect(StatusEffects.HASTE);
    }

    private long calculateAdjustedCooldown() {
        long longValue = 467L;
        TpsSync tpsSync = ModuleRegistry.tpsSync;
        if (tpsSync != null && tpsSync.isEnable()) {
            longValue = (long) ((float) tpsSync.getAdjustedCooldown(longValue) * 1.1F);
        }
        return longValue;
    }

    private boolean isOnGroundWithVelocityAndPressed() {
        boolean inFluid = this.isInFluid();
        boolean onGroundWithVelocityAndStatusEffect = this.isOnGroundWithVelocityAndStatusEffect();
        boolean flag = this.mode.is("Умные криты") && this.attackPhaseTicks >= 10;
        if (!inFluid && !flag && mc.player.getVelocity().y > 0.0) {
            return false;
        } else if (!mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming() || flag || !mc.options.jumpKey.isPressed() || !mc.player.isOnGround() && !(mc.player.getVelocity().y > 0.0)) {
            Item item = mc.player.getMainHandStack().getItem();
            boolean flagValue = item instanceof AxeItem || item instanceof TridentItem || item instanceof MaceItem;
            float floatValue = !mc.player.isOnGround() && !inFluid ? (flagValue ? 0.85F : 0.75F) : (flagValue ? 0.95F : 0.8F);
            if (this.isInstanceWithItem(MaceItem.class)) {
                floatValue = 0.4F;
            }
            float floatValueValue = 0.0F;
            if (this.isStatusEffect()) {
                if (System.currentTimeMillis() < this.lastUpdateMillis) {
                    return false;
                }
                floatValue = CriticalHitUtil.getAICooldown();
                floatValueValue = 1.5F;
            }
            if (mc.player.getAttackCooldownProgress(floatValueValue) < floatValue) {
                return false;
            } else if (this.mode.is("Только криты")) {
                return onGroundWithVelocityAndStatusEffect || inFluid;
            } else {
                return this.isInstanceWithItem(MaceItem.class) ? onGroundWithVelocityAndStatusEffect || inFluid || flag : onGroundWithVelocityAndStatusEffect || inFluid || flag || !mc.options.jumpKey.isPressed() && mc.player.isOnGround() && this.groundTicks >= 5;
            }
        } else {
            return false;
        }
    }

    private void updateLocalState() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((TriggerBot) accessReceiver).updatePopPause();
        });
        this.target = null;
        this.groundTicks = 0;
        this.attackDelayTicks = 0;
        this.sprintResetTicks = 0;
        this.attackPhaseTicks = 0;
        this.lastUpdateMillis = 0L;
    }

    @Override
    public void onEnable() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((TriggerBot) accessReceiver).updateLocalState();
        });
        super.onEnable();
    }

    @Override
    public void onDisable() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((TriggerBot) accessReceiver).updateLocalState();
        });
        super.onDisable();
    }

    public LivingEntity getTarget() {
        return this.target;
    }
}
