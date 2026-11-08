package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.GameUpdateEvent;
import brova.toshiba.client.event.impl.MovementInputEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.PostPlayerTickEvent;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura.AttackState;
import brova.toshiba.client.modules.impl.combat.AttackAura.BlockIgnoringRaycastContext;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.impl.player.AutoEat;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.storages.implement.ServerStorage;
import brova.toshiba.client.utils.movement.MovementUtil;
import brova.toshiba.client.utils.player.CriticalHitUtil;
import brova.toshiba.client.utils.player.HealthUtil;
import brova.toshiba.client.utils.player.InventorySearchUtil;
import brova.toshiba.client.utils.player.SlotSearchResult;
import brova.toshiba.client.utils.profile.Profiled;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationPointUtil;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.rotate.modes.AresMineRotation;
import brova.toshiba.client.utils.rotate.modes.FunTimeRotation;
import brova.toshiba.client.utils.rotate.modes.NeuroRotation;
import brova.toshiba.client.utils.rotate.modes.ReallyWorldRotation;
import brova.toshiba.client.utils.rotate.modes.SpookyTimeRotation;
import brova.toshiba.client.utils.rotate.modes.shard.ShardRotation;
import brova.toshiba.client.utils.rotate.modes.SlimeRotation;
import brova.toshiba.client.utils.time.IntervalTimer;
import brova.toshiba.client.utils.world.EntityPrediction;
import brova.toshiba.client.utils.world.NearestPointUtil;
import brova.toshiba.mixin.ICameraMixin;
import brova.toshiba.mixin.IClientPlayerInteractionManager;
import brova.toshiba.mixin.ILivingEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@Profiled
public final class AttackAura extends Module {

    public static AttackAura INSTANCE = new AttackAura();

    public final ModeSetting rotationType = new ModeSetting("Ротация", "Shard", "ReallyWorld", "Shard", "Slime", "Neuro", "SpookyTime", "БезРотации");

    private final MultiSetting targetTypes = new MultiSetting("Таргеты", new BooleanSetting("Игроки", true), new BooleanSetting("Голые", true), new BooleanSetting("Инвизки", true), new BooleanSetting("Мобы", true), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false));

    private final FloatSetting range = new FloatSetting("Дистанция атаки", 3.0F, 0.0F, 6.0F, 0.05F);

    private final FloatSetting extraRange = new FloatSetting("Доп. дистанция", 3.0F, 0.0F, 6.0F, 0.05F);

    public final BooleanSetting onlyCrits = new BooleanSetting("Только криты", true);

    // Умные криты включены по умолчанию: на земле без прыжка удары идут полным зарядом
    // (без крестьянского молчания), в прыжке — честный крит-чек как был.
    public final BooleanSetting smartCrit = new BooleanSetting("Умные криты", true);

    private final ModeSetting sprintResetMode = new ModeSetting("Сброс спринта", "Легит", "Легит", "Пакетный");

    public final BooleanSetting throughWalls = new BooleanSetting("Бить через стены", true);

    private final BooleanSetting wallCheckBypass = new BooleanSetting("Обходить ач", false).visible(this.throughWalls::isState);

    private final BooleanSetting releaseShield = new BooleanSetting("Отжимать щит", false);

    private final BooleanSetting breakShields = new BooleanSetting("Ломать щит", true);

    private final BooleanSetting pauseWhileEating = new BooleanSetting("Не бить когда ешь", true);

    private final BooleanSetting pauseInInventory = new BooleanSetting("Не бить в инвентаре", true);

    private final BooleanSetting swordOnly = new BooleanSetting("Бить только с мечом", false);

    public final BooleanSetting returnCameraToRotation = new BooleanSetting("Возращать камеру к ротации", false);

    private final ModeSetting moveFix = new ModeSetting("Корекция движения", "Свободная", "Свободная", "Сфокусированная", "Таргетированная", "Таргет2");

    private final ModeSetting targetPriority = new ModeSetting("Приоритет", "Дистанция", "Дистанция", "Здоровье", "Угол");

    private LivingEntity target;

    private static LivingEntity lastTarget;

    private Vec2f currentRotations = new Vec2f(0.0F, 0.0F);

    private Vec2f targetRotations = new Vec2f(0.0F, 0.0F);

    private Vec3d externalRotationPoint;

    private final IntervalTimer attackTimer = new IntervalTimer();

    private final ReallyWorldRotation resetReallyWorldRotation = new ReallyWorldRotation(this);

    private final FunTimeRotation resetFunTimeRotation = new FunTimeRotation(this);

    private final SpookyTimeRotation resetSpookyTimeRotation = new SpookyTimeRotation(this);

    private final ShardRotation resetShardRotation = new ShardRotation(this);

    private final SlimeRotation resetSlimeRotation = new SlimeRotation(this);

    private final NeuroRotation resetNeuroRotation = new NeuroRotation(this);

    private String previousRotationMode = "Shard";

    private final AresMineRotation resetAresMineRotation = new AresMineRotation(this);

    private final IntervalTimer rotationTimer = new IntervalTimer();

    private TpsSync tpsSync;

    public long cps = 0L;

    private int legitSprintResetTicks = 0;

    private boolean waitingForServerSprintStop = false;

    private boolean sprintResetPending = false;

    private int wTapTicks = 0;

    private boolean stateFlagForSendResetWithCurrentTimeMillis = false;

    private int cachedCount = 0;

    private int whiteRiseTicksToAttack = 0;

    // Человеческая задержка реакции: цель, только что наведённая (первый вход в таргет),
    // 1–3 тика не атакуется — мгновенный удар в тик наведения = сигнатура киллы.
    private int targetAcquiredAge = -1000;

    private int acquireLagTicks = 2;

    private float attackCooldownThreshold = 0.97F + (float) Math.random() * 0.03F;

    // Порог «поймать дугу»: в реальной падающей дуге сервер бьёт критом (×1.5) в любом тике дуги —
    // ждать полной зарядки до посадки бессмысленно, окно конечное. Порог 0.72–0.82 заряда:
    // 0.72×1.5 = 1.08 урона от базы — каждый удар в дуге сильнее полного заряда на земле (×1.0),
    // окно дуги ловится максимально рано. Пол ниже 0.667 (=1.0× ÷ крит) бессмысленен.
    private float attackArcThreshold = 0.72F + (float) Math.random() * 0.10F;

    // Надёжность reach-чека: удар ровно на границе 3.0 улетает в лаг-комп сервера по дистанции
    // и сервер отменяет свинг без урона. Чуть сдвигаем границу внутрь (рероллится с остальными
    // порогами после удара) — краевые свинги не отменяются, дальние удары доходят без мисс.
    private float attackReachMargin = 0.05F + (float) Math.random() * 0.13F;

    // Тики подряд, когда удар заблокирован только ожиданием докрутки Slime. Счётчик
    // обслуживает предохранитель гейта готовности — вечное ожидание невозможно.
    private int aimConvergenceWaitTicks = 0;

    private BlockPos blockPos;

    private float cachedYaw = Float.NaN;

    private float cachedPitch = Float.NaN;

    private double cachedPositionX = Double.NaN;

    private double cachedPositionY = Double.NaN;

    private double cachedPositionZ = Double.NaN;

    private double cachedEyeHeight;

    private boolean stateFlagForRwWallPacket;

    private ClientWorld previousWorld;

    private PlayerEntity previousPlayer;

    private float cachedScalarForPlayerTick = 0.0F;

    private float cachedScalar = 0.0F;

    private float lastObservedYaw = Float.NaN;

    private float lastObservedPitch = Float.NaN;

    public static float adjYaw;

    public static float adjPitch;

    public static float otvodkaYaw;

    public static float otvodkaPitch;

    public boolean isRotated;

    public void setCurrentRotations(Vec2f currentRotations) {
        this.currentRotations = currentRotations;
    }

    public void setTargetRotations(Vec2f targetRotations) {
        this.targetRotations = targetRotations;
    }

    public void setExternalTarget(LivingEntity target) {
        if (!this.isEnable()) {
            this.target = target;
        }
    }

    public void setExternalRotationPoint(Vec3d externalRotationPoint) {
        this.externalRotationPoint = externalRotationPoint;
    }

    public Vec3d resolveRotationPoint(Vec3d position) {
        return this.externalRotationPoint != null ? this.externalRotationPoint : position;
    }

    public int getLegitSprintResetTicks() {
        return this.legitSprintResetTicks;
    }

    public boolean isWaitingForServerSprintStop() {
        return this.waitingForServerSprintStop;
    }

    public boolean isSprintResetPending() {
        return this.sprintResetPending;
    }

    public AttackAura() {
        super("AttackAura", "Автоматически наводиться и бьёт протививника.", ModuleCategory.COMBAT);
        this.addSettings(this.rotationType, this.targetTypes, this.range, this.extraRange, this.onlyCrits, this.smartCrit, this.sprintResetMode, this.pauseWhileEating, this.pauseInInventory, this.swordOnly, this.throughWalls, this.wallCheckBypass, this.releaseShield, this.breakShields, this.returnCameraToRotation, this.moveFix, this.targetPriority);
    }

    public boolean usesShardRotation() {
        return this.rotationType.is("Shard");
    }

    public boolean usesSlimeRotation() {
        return this.rotationType.is("Slime");
    }

    private void updateEqualsWithReturnToCamera() {
        String previousRotationMode = brova.toshiba.client.utils.runtime.AccessGuard.invoke(this.rotationType, new Object[] {}, (accessReceiver, accessArguments) -> ((ModeSetting) accessReceiver).getCurrent());
        if (!previousRotationMode.equals(this.previousRotationMode)) {
            if (previousRotationMode.equals("Neuro") || this.previousRotationMode.equals("Neuro")) {
                this.resetNeuroRotation.returnToCamera();
                this.resetShardRotation.reset();
                this.resetSlimeRotation.reset();
            }
            if (this.previousRotationMode.equals("Slime") && !previousRotationMode.equals("Slime")) {
                this.resetSlimeRotation.returnToCamera();
            }
            this.previousRotationMode = previousRotationMode;
        }
    }

    @EventHandler
    public void onPlayerTick(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            this.updateEqualsWithReturnToCamera();
            if (this.tpsSync == null && Toshiba.INSTANCE != null && Toshiba.INSTANCE.moduleStorage != null) {
                this.tpsSync = ModuleRegistry.tpsSync;
            }
            this.cachedScalarForPlayerTick++;
            this.refreshTarget();
        }
    }

    @EventHandler
    public void onAttackEntity(TargetPlayerEvent event) {
        if (mc.player != null && mc.world != null && event.getPlayer() == mc.player && event.getTarget() instanceof LivingEntity entity && this.target == entity && this.isValidTarget(entity)) {
            lastTarget = entity;
            if (this.usesShardRotation()) {
                this.resetShardRotation.onAttack();
            }
            if (this.usesSlimeRotation()) {
                this.resetSlimeRotation.onAttack();
            }
        }
    }

    @EventHandler
    public void onMoveInput(DirectionalInput directionalInput) {
        this.updateWrapDegreesWithToDegrees(directionalInput);
    }

    private void updateWrapDegreesWithToDegrees(DirectionalInput directionalInput) {
        if (mc.player != null && this.target != null && !this.rotationType.is("БезРотации") && (directionalInput.getForward() != 0.0F || directionalInput.getStrafe() != 0.0F) && (ModuleRegistry.autoSwap == null || !ModuleRegistry.autoSwap.isLegitSwapInProgress())) {
            if (this.moveFix.is("Свободная")) {
                MovementUtil.fixMovementFree(directionalInput);
            } else if (this.moveFix.is("Таргет2")) {
                this.updateTarget2MovementCorrections(directionalInput);
            } else if (this.moveFix.is("Таргетированная")) {
                double doubleValue = this.target.getX() + -mc.player.getX();
                double doubleValueValue = this.target.getZ() + -mc.player.getZ();
                float floatValue = (float) (Math.toDegrees(Math.atan2(doubleValueValue, doubleValue)) + -90.0);
                float floatValueValue = 0.0F;
                float floatValueResult = 0.0F;
                float MAX_VALUE = Float.MAX_VALUE;
                double doubleValueResult = MathHelper.wrapDegrees(floatValue);
                for (float index = -1.0F; index <= 1.0F; index++) {
                    for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                        if (index != 0.0F || indexValue != 0.0F) {
                            double doubleValueItem = MathHelper.wrapDegrees(Math.toDegrees(MovementUtil.direction(mc.player.getYaw(), index, indexValue)));
                            double magnitude = Math.abs(MathHelper.wrapDegrees((float) (doubleValueResult + -doubleValueItem)));
                            if (magnitude < MAX_VALUE) {
                                MAX_VALUE = (float) magnitude;
                                floatValueValue = index;
                                floatValueResult = indexValue;
                            }
                        }
                    }
                }
                directionalInput.setForward(floatValueValue);
                directionalInput.setStrafe(floatValueResult);
            }
        }
    }

    private void updateTarget2MovementCorrections(DirectionalInput directionalInput) {
        // Таргет2 — максимум вжиманья, движением управляет юзер (WASD): курс ведём не в центр цели,
        // а в БЛИЖАЙШУЮ точку хитбокса + упреждение по скорости цели (0.5 тика вперёд) —
        // на стрейфе заходим на зарез и держим притык. За игрока сами НЕ ходим: двигаем только
        // перенаправлением удерживаемых клавиш, скорость строго ванильная.
        LivingEntity livingEntity = this.target;
        if (livingEntity == null || mc.world == null || mc.player.isGliding() || mc.player.getAbilities().flying || mc.player.hasVehicle()) {
            return;
        }
        Vec3d vel = livingEntity.getVelocity();
        Box box = livingEntity.getBoundingBox().offset(vel.x * 0.5, vel.y * 0.5, vel.z * 0.5);
        Vec3d eye = mc.player.getEyePos();
        Vec3d closest = new Vec3d(MathHelper.clamp(eye.x, box.minX, box.maxX), MathHelper.clamp(eye.y, box.minY, box.maxY), MathHelper.clamp(eye.z, box.minZ, box.maxZ));
        double doubleValue = closest.x - mc.player.getX();
        double doubleValueValue = closest.z - mc.player.getZ();
        if (doubleValue * doubleValue + doubleValueValue * doubleValueValue < 0.01) {
            doubleValue = livingEntity.getX() - mc.player.getX();
            doubleValueValue = livingEntity.getZ() - mc.player.getZ();
        }
        float desiredYaw = MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(doubleValueValue, doubleValue)) + -90.0));
        // Лучший из 8 WASD-комбинаций под курс — та же математика, что в «Таргетированной».
        float bestForward = 0.0F;
        float bestStrafe = 0.0F;
        float MAX_VALUE = Float.MAX_VALUE;
        for (float index = -1.0F; index <= 1.0F; index++) {
            for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                if (index != 0.0F || indexValue != 0.0F) {
                    double doubleValueItem = MathHelper.wrapDegrees(Math.toDegrees(MovementUtil.direction(mc.player.getYaw(), index, indexValue)));
                    double magnitude = Math.abs(MathHelper.wrapDegrees((float) (desiredYaw + -doubleValueItem)));
                    if (magnitude < MAX_VALUE) {
                        MAX_VALUE = (float) magnitude;
                        bestForward = index;
                        bestStrafe = indexValue;
                    }
                }
            }
        }
        directionalInput.setForward(bestForward);
        directionalInput.setStrafe(bestStrafe);
    }

    @EventHandler
    public void onKeyboardInput(MovementInputEvent event) {
        if (mc.player != null && mc.world != null && this.target != null && !this.rotationType.is("БезРотации")) {
            float floatValue = this.calculateYawWithTargetRotation();
            if (this.moveFix.is("Сфокусированная")) {
                event.setYaw(floatValue, mc.player.getYaw());
            }
        }
    }

    private float calculateYawWithTargetRotation() {
        return RotationStorage.instance != null && RotationStorage.instance.targetRotation() != null ? brova.toshiba.client.utils.runtime.AccessGuard.invoke(RotationStorage.instance, new Object[] {}, (accessReceiver, accessArguments) -> ((RotationStorage) accessReceiver).targetRotation()).getYaw() : mc.player.getYaw();
    }

    private float calculateRotationsWithCenter() {
        return RotationUtil.getRotations(this.target.getBoundingBox().getCenter()).x;
    }

    private void updateWrapDegreesWithToDegrees(DirectionalInput directionalInput, float floatValue, float floatValueValue) {
        float forward = directionalInput.getForward();
        float strafe = directionalInput.getStrafe();
        if (forward != 0.0F || strafe != 0.0F) {
            double doubleValue = MathHelper.wrapDegrees(Math.toDegrees(MovementUtil.direction(floatValueValue, forward, strafe)));
            float floatValueResult = 0.0F;
            float floatValueItem = 0.0F;
            float MAX_VALUE = Float.MAX_VALUE;
            for (float index = -1.0F; index <= 1.0F; index++) {
                for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                    if (index != 0.0F || indexValue != 0.0F) {
                        double doubleValueValue = MathHelper.wrapDegrees(Math.toDegrees(MovementUtil.direction(floatValue, index, indexValue)));
                        double magnitude = Math.abs(doubleValue - doubleValueValue);
                        if (magnitude < MAX_VALUE) {
                            MAX_VALUE = (float) magnitude;
                            floatValueResult = index;
                            floatValueItem = indexValue;
                        }
                    }
                }
            }
            directionalInput.setForward(floatValueResult);
            directionalInput.setStrafe(floatValueItem);
        }
    }

    @EventHandler
    private void onGameUpdate(GameUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.target == null) {
                if (this.returnCameraToRotation.isState() && FreeLookStorage.isActive()) {
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                        ((AttackAura) accessReceiver).updateResetWithCamera();
                    });
                }
            } else {
                lastTarget = this.target;
                if (!this.rotationType.is("Neuro")) {
                    this.updateTargetRotations();
                }
            }
        }
    }

    @EventHandler
    public void onTick(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.previousWorld != mc.world || this.previousPlayer != mc.player) {
                this.updateYawWithPitch();
            }
            if (this.whiteRiseTicksToAttack > 0) {
                this.whiteRiseTicksToAttack--;
            }
            if (this.wTapTicks > 0) {
                this.wTapTicks--;
            }
            if (this.sprintResetPending && this.legitSprintResetTicks > 0) {
                mc.options.sprintKey.setPressed(false);
                mc.player.setSprinting(false);
                this.legitSprintResetTicks--;
            }
            if (this.sprintResetPending && this.legitSprintResetTicks <= 0) {
                this.sprintResetPending = false;
                this.waitingForServerSprintStop = false;
            }
            boolean flag = ModuleRegistry.packetCriticals.isEnable() && mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);
            boolean flagValue = mc.player.fallDistance > 0.0F && mc.player.fallDistance < 1.0F;
            if (!flag || this.isWallBypassEnabled() && flagValue) {
                this.sendResetWithCurrentTimeMillis();
            }
        }
    }

    @EventHandler
    public void onPost(PostPlayerTickEvent event) {
        if (mc.player != null && mc.world != null) {
            boolean flag = ModuleRegistry.packetCriticals.isEnable() && mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);
            if (!this.isWallBypassEnabled() && flag && mc.player.fallDistance > 0.0F && mc.player.fallDistance < 1.0F) {
                this.sendResetWithCurrentTimeMillis();
            }
            if (this.rotationType.is("Neuro") && this.target != null) {
                this.resetNeuroRotation.updateRotations(this.target);
            }
        }
    }

    private void sendResetWithCurrentTimeMillis() {
        this.refreshTarget();
        if (this.target != null) {
            // Свежий тик ротации ДО гейта: рейкаст смотрится по применённой ротации.
            // Без этого удар проверяется по вчерашнему углу (наведение из прошлого тика)
            // и в кадре свинга прицел отстаёт — отсюда «не доводится».
            this.updateTargetRotations();
            if (this.isGlidingWithAttackCooldownProgress() && this.cps <= System.currentTimeMillis()) {
                if (this.pauseWhileEating.isState() && (this.isUsingItem() || AutoEat.shouldSuppressCombat())) {
                    return;
                }
                if (this.pauseInInventory.isState() && mc.currentScreen != null) {
                    return;
                }
                if (this.swordOnly.isState() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                    return;
                }
                if (!this.isConstantState()) {
                    return;
                }
                if (this.sprintResetMode.is("Легит")) {
                    if (this.sprintResetPending) {
                        return;
                    }
                    if (mc.player.isSprinting() && !this.isTouchingWaterWithSubmergedInWater()) {
                        mc.options.sprintKey.setPressed(false);
                        mc.player.setSprinting(false);
                        if (Sprint.INSTANCE != null && Sprint.INSTANCE.isEnable()) {
                            Sprint.tickStop = 1;
                        }
                        this.legitSprintResetTicks = 1;
                        this.waitingForServerSprintStop = true;
                        this.sprintResetPending = true;
                        return;
                    }
                }
                if (this.sprintResetMode.is("Пакетный") && mc.player.isSprinting() && !this.isTouchingWaterWithSubmergedInWater()) {
                    mc.player.setSprinting(false);
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                    if (Sprint.INSTANCE != null && Sprint.INSTANCE.isEnable()) {
                        Sprint.tickStop = 2;
                    }
                }
                if ((this.rotationType.is("Sloth/Shard") || this.rotationType.is("Sloth Pro/Flash 1")) && this.isNaNWithWrapDegrees()) {
                    return;
                }
                this.updateOnAttackWithStopUsingItem();
                this.stateFlagForSendResetWithCurrentTimeMillis = false;
                this.cachedCount = 0;
            }
        } else {
            this.cps = System.currentTimeMillis();
            this.rotationTimer.reset();
            adjPitch = 0.0F;
            adjYaw = 0.0F;
            if (this.returnCameraToRotation.isState()) {
                this.updateResetWithCamera();
            } else if (this.usesShardRotation()) {
                this.resetShardRotation.returnToCamera();
            } else if (this.usesSlimeRotation()) {
                this.resetSlimeRotation.returnToCamera();
            } else if (RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
                RotationStorage.instance.stopRotation();
            }
            this.resetReallyWorldRotation.reset();
            this.resetFunTimeRotation.reset();
            this.resetSpookyTimeRotation.reset();
            this.resetNeuroRotation.reset();
            if (!this.usesShardRotation()) {
                this.resetShardRotation.reset();
            }
            if (!this.usesSlimeRotation()) {
                this.resetSlimeRotation.reset();
            }
            this.resetAresMineRotation.reset();
            this.stateFlagForSendResetWithCurrentTimeMillis = false;
            this.cachedCount = 0;
            this.legitSprintResetTicks = 0;
            this.waitingForServerSprintStop = false;
            this.sprintResetPending = false;
            this.wTapTicks = 0;
            this.whiteRiseTicksToAttack = 0;
            this.lastObservedYaw = Float.NaN;
            this.lastObservedPitch = Float.NaN;
        }
    }

    public void Rotate() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AttackAura) accessReceiver).updateTargetRotations();
        });
    }

    public void rotateTo(LivingEntity entity) {
        if (entity != null) {
            this.updateRotationsWithRotating(entity);
        }
    }

    public void rotateToPoint(LivingEntity entity, Vec3d externalRotationPoint) {
        if (entity != null && externalRotationPoint != null) {
            Vec3d externalRotationPointValue = this.externalRotationPoint;
            this.externalRotationPoint = externalRotationPoint;
            try {
                this.updateRotationsWithRotating(entity);
            } finally {
                this.externalRotationPoint = externalRotationPointValue;
            }
        }
    }

    public void returnToCamera() {
        if (this.returnCameraToRotation.isState()) {
            this.updateResetWithCamera();
        } else if (this.usesShardRotation()) {
            this.resetShardRotation.returnToCamera();
        } else if (this.usesSlimeRotation()) {
            this.resetSlimeRotation.returnToCamera();
        } else if (this.rotationType.is("Neuro")) {
            this.resetNeuroRotation.returnToCamera();
        } else if (RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(RotationStorage.instance, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((RotationStorage) accessReceiver).stopRotation();
            });
        }
    }

    private void updateResetWithCamera() {
        if (RotationStorage.instance != null) {
            RotationStorage.instance.stopRotation(false);
        } else if (mc.player != null && FreeLookStorage.isActive()) {
            float prevYaw = mc.player.getYaw();
            float prevPitch = mc.player.getPitch();
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { prevYaw }, (accessReceiver, accessArguments) -> {
                FreeLookStorage.setFreeYaw(((float) accessArguments[0]));
            });
            FreeLookStorage.setFreePitch(prevPitch);
            mc.player.prevYaw = prevYaw;
            mc.player.prevPitch = prevPitch;
            mc.player.bodyYaw = prevYaw;
            mc.player.prevBodyYaw = prevYaw;
            mc.player.headYaw = prevYaw;
            mc.player.prevHeadYaw = prevYaw;
            if (mc.gameRenderer != null && mc.gameRenderer.getCamera() != null) {
                ((ICameraMixin) mc.gameRenderer.getCamera()).setCustomRotation(prevYaw, prevPitch);
            }
            boolean flag = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                FreeLookStorage.setActive(((boolean) accessArguments[0]));
            });
        }
        this.resetReallyWorldRotation.reset();
        this.resetFunTimeRotation.reset();
        this.resetSpookyTimeRotation.reset();
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this.resetShardRotation, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((ShardRotation) accessReceiver).reset();
        });
        this.resetSlimeRotation.reset();
        this.resetNeuroRotation.reset();
        this.resetAresMineRotation.reset();
    }

    private void updateTargetRotations() {
        if (this.target != null) {
            this.updateRotationsWithRotating(this.target);
        }
    }

    private void updateRotationsWithRotating(LivingEntity entity) {
        this.updateEqualsWithReturnToCamera();
        if (this.rotationType.is("БезРотации")) {
            if (RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
                RotationStorage.instance.stopRotation();
            }
        } else if (this.rotationType.is("Shard")) {
            this.resetShardRotation.updateRotations(entity);
        } else if (this.rotationType.is("Slime")) {
            this.resetSlimeRotation.updateRotations(entity);
        } else if (this.rotationType.is("Neuro")) {
            this.resetNeuroRotation.updateRotations(entity);
        } else if (this.rotationType.is("AresMine")) {
            this.resetAresMineRotation.updateRotations(entity);
        } else if (this.rotationType.is("ReallyWorld")) {
            this.resetReallyWorldRotation.updateRotations(entity);
        } else if (this.rotationType.is("FunTime")) {
            this.resetFunTimeRotation.updateRotations(entity);
        } else if (this.rotationType.is("SpookyTime")) {
            this.resetSpookyTimeRotation.updateRotations(entity);
        }
    }

    private void applySubtractWithEyePos(LivingEntity entity) {
        Vec3d position = this.resolveRotationPoint(null);
        if (position != null) {
            Vec3d positionValue = position.subtract(mc.player.getEyePos());
            float floatValue = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(positionValue.z, positionValue.x)) - 90.0);
            float floatValueValue = (float) (-Math.toDegrees(Math.atan2(positionValue.y, Math.hypot(positionValue.x, positionValue.z))));
            RotationStorage.update(new Rotation(floatValue, floatValueValue), 360.0F, 360.0F, 360.0F, 360.0F, 1, 1, false);
            mc.player.setYaw(floatValue);
            mc.player.setPitch(MathHelper.clamp(floatValueValue, -90.0F, 90.0F));
        } else {
            Vec3d positionResult;
            if (!mc.player.isGliding()) {
                positionResult = entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0).subtract(mc.player.getEyePos());
            } else {
                Vec3d positionItem = Vec3d.fromPolar(entity.getLerpTargetPitch(), entity.getLerpTargetYaw());
                Vec3d rotationVector = entity.getRotationVector();
                positionResult = entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0).subtract(mc.player.getEyePos());
                Vec3d interpolatedValue = positionItem.normalize().lerp(rotationVector, positionItem.length());
                if (mc.player.isGliding() && entity.isGliding() && ModuleRegistry.elytraTarget.isEnable()) {
                    if (ModuleRegistry.elytraTarget.isVanillaMode()) {
                        positionResult = ModuleRegistry.elytraTarget.getPredictedPoint(entity, entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0)).subtract(mc.player.getEyePos());
                    } else {
                        positionResult = positionResult.add(interpolatedValue.normalize().multiply(ModuleRegistry.elytraTarget.getPredictionMultiplier(entity)));
                    }
                }
            }
            float floatValueResult = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(positionResult.z, positionResult.x)) - 90.0);
            float floatValueItem = (float) (-Math.toDegrees(Math.atan2(positionResult.y, Math.hypot(positionResult.x, positionResult.z))));
            RotationStorage.update(new Rotation(floatValueResult, floatValueItem), 360.0F, 360.0F, 360.0F, 360.0F, 1, 1, false);
            mc.player.setYaw(floatValueResult);
            mc.player.setPitch(MathHelper.clamp(floatValueItem, -90.0F, 90.0F));
        }
    }

    private void updateResolveRotationPointWithRotations(LivingEntity entity) {
        Vec3d position = this.resolveRotationPoint(null);
        Vec2f rotations = RotationUtil.getRotations(position != null ? position : this.getPredictedRotationPoint(entity, entity.getLeashPos(1.0F)));
        this.currentRotations = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
        RotationStorage.update(new Rotation(rotations.x, rotations.y), 360.0F, 360.0F, 360.0F, 360.0F, 1, 1, false);
    }

    private boolean isTouchingWaterWithSubmergedInWater() {
        return mc.player != null && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) && Sprint.INSTANCE != null && Sprint.INSTANCE.shouldKeepSprintInWater();
    }

    private boolean isServerSprinting() {
        return Toshiba.INSTANCE != null && brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.serverStorage) != null && brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.serverStorage).isServerSprinting();
    }

    private boolean canRaycastWithEyePos() {
        if (mc.player != null && mc.world != null && this.target != null) {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d position = mc.player.getRotationVector(mc.player.getPitch(), mc.player.getYaw()).normalize();
            // Reach гейта идёт с анти-мисс запасом: удар ровно на границе сервер может не признать
            // по лаг-комп дистанции и отменить свинг без урона — запас держит краевые удары валидными.
            double doubleValue = Math.max(2.0, this.range.getValue().doubleValue() - this.attackReachMargin);
            Vec3d positionValue = eyePos.add(position.multiply(doubleValue));
            Box boundingBox = this.target.getBoundingBox();
            if (boundingBox.contains(eyePos)) {
                return true;
            } else {
                var optional = boundingBox.raycast(eyePos, positionValue);
                if (optional.isEmpty()) {
                    return false;
                } else if (eyePos.squaredDistanceTo(optional.get()) > doubleValue * doubleValue) {
                    return false;
                } else if (this.throughWalls.isState()) {
                    return true;
                } else {
                    BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(eyePos, optional.get(), ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
                    return blockHitResult.getType() != HitResult.Type.BLOCK;
                }
            }
        } else {
            return false;
        }
    }

    public Vec3d getPredictedRotationPoint(LivingEntity entity, Vec3d position) {
        return mc.player != null && entity != null && mc.player.isGliding() && entity.isGliding() && ModuleRegistry.elytraTarget != null && ModuleRegistry.elytraTarget.isEnable() ? ModuleRegistry.elytraTarget.getPredictedPoint(entity, position) : position;
    }

    public Vec3d getStableBodyPoint(LivingEntity entity) {
        Box boundingBox = entity.getBoundingBox();
        return new Vec3d(boundingBox.getCenter().x, boundingBox.minY + boundingBox.getLengthY() * 0.72, boundingBox.getCenter().z);
    }

    private LivingEntity findTargetFromSortWithComparingDouble() {
        ArrayList<LivingEntity> entities = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity entityValue && this.isValidTarget(entityValue)) {
                entities.add(entityValue);
            }
        }
        if (!entities.isEmpty() && this.isEnable()) {
            String current = this.targetPriority.getCurrent();
            switch(current) {
                case "Дистанция":
                    entities.sort(Comparator.comparingDouble(input -> input.getBoundingBox().getCenter().squaredDistanceTo(mc.player.getEyePos())));
                    break;
                case "Здоровье":
                    entities.sort(Comparator.comparingDouble(HealthUtil::getHealth));
                    break;
                case "Угол":
                    entities.sort(Comparator.comparingDouble(inputValue -> {
                        Vec2f rotations = RotationUtil.getRotations(inputValue.getBoundingBox().getCenter());
                        double magnitude = Math.abs(MathHelper.wrapDegrees(rotations.x - mc.player.getYaw()));
                        double magnitudeValue = Math.abs(MathHelper.wrapDegrees(rotations.y - mc.player.getPitch()));
                        return magnitude + magnitudeValue;
                    }));
                    break;
                case "Никакой":
            }
            return entities.isEmpty() ? null : entities.get(0);
        } else {
            return null;
        }
    }

    private void refreshTarget() {
        if (!this.isEnable()) {
            this.target = null;
        } else if (this.target == null || !this.isValidTarget(this.target)) {
            this.target = this.findTargetFromSortWithComparingDouble();
            if (this.target != null) {
                lastTarget = this.target;
                targetAcquiredAge = mc.player.age;
                acquireLagTicks = 1 + (int) (Math.random() * 2.0);
            }
        }
    }

    private void updateOnAttackWithStopUsingItem() {
        // Задержка реакции человека: только что наведённая цель первые 1–3 тика не получает удара —
        // человек не свингает в тот же тик, когда появился таргет.
        if (mc.player != null && mc.player.age - this.targetAcquiredAge < this.acquireLagTicks) {
            return;
        }
        if (this.canTypeWithSide()) {
            Hand hand = null;
            if (this.releaseShield.isState()) {
                hand = this.createBlockingWithActiveHand();
                if (hand != null) {
                    mc.interactionManager.stopUsingItem(mc.player);
                }
            }
            boolean flag = false;
            if (this.target instanceof PlayerEntity player && player.isBlocking() && this.breakShields.isState()) {
                flag = this.isSlotWithAxe(player);
            }
            if (!flag) {
                mc.interactionManager.attackEntity(mc.player, this.target);
            }
            mc.player.swingHand(Hand.MAIN_HAND);
            if (hand != null) {
                this.updateStackInHandWithUsingItem(hand);
            }
            if (this.rotationType.is("Shard")) {
                this.resetShardRotation.onAttack();
            }
            if (this.rotationType.is("Slime")) {
                this.resetSlimeRotation.onAttack();
            }
            if (this.rotationType.is("FunTime")) {
                this.resetFunTimeRotation.onAttack();
            }
            if (this.rotationType.is("SpookyTime")) {
                this.resetSpookyTimeRotation.onAttack();
            }
            if (this.rotationType.is("AresMine")) {
                this.resetAresMineRotation.onAttack();
            }
            // Идеальное КД + лучшая микро-рандомизация, без настроек:
            // порог кулдауна сам дрейфует каждый цикл (0.97–1.00 — полный заряд, урон не теряется),
            // а пауза между ударами идёт узким «человеческим» окном с редкими микро-паузами —
            // постоянного периода нет, статистика ачей периодичность не вытаскивает.
            this.attackCooldownThreshold = 0.97F + (float) Math.random() * 0.03F;
            this.attackArcThreshold = 0.72F + (float) Math.random() * 0.10F;
            this.attackReachMargin = 0.05F + (float) Math.random() * 0.13F;
            long longValue = (long) (430.0F * (0.92F + Math.random() * 0.14F));
            if (Math.random() < 0.10) {
                longValue += 60L + (long) (Math.random() * 200.0);
            }
            if (this.tpsSync != null && this.tpsSync.isEnable()) {
                longValue = (long) ((float) this.tpsSync.getAdjustedCooldown(longValue) * 1.1F);
            }
            this.cps = System.currentTimeMillis() + longValue;
            this.whiteRiseTicksToAttack = 8;
            this.attackTimer.reset();
            this.lastObservedYaw = mc.player.getYaw();
            this.lastObservedPitch = mc.player.getPitch();
        }
    }

    private boolean isNaNWithWrapDegrees() {
        if (mc.player != null && !Float.isNaN(this.lastObservedYaw)) {
            float magnitude = Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() + -this.lastObservedYaw));
            float magnitudeValue = Math.abs(mc.player.getPitch() + -this.lastObservedPitch);
            return magnitude < 0.15F && magnitudeValue < 0.15F;
        } else {
            return false;
        }
    }

    private boolean isSlotWithAxe(PlayerEntity player) {
        SlotSearchResult axe = InventorySearchUtil.getAxe();
        if (!axe.found()) {
            return false;
        } else {
            int selectedSlot = mc.player.getInventory().selectedSlot;
            if (axe.slot() == selectedSlot) {
                mc.interactionManager.attackEntity(mc.player, player);
                return true;
            } else if (axe.isInHotBar()) {
                return this.isPacketWithAttackEntity(player, axe.slot(), selectedSlot);
            } else if (mc.player.currentScreenHandler.syncId != 0) {
                return false;
            } else {
                int intValue = this.calculateInteger(selectedSlot);
                if (intValue == -1) {
                    return false;
                } else {
                    this.sendClickSlotWithPacket(axe.slot(), intValue);
                    boolean flag = false;
                    try {
                        flag = this.isPacketWithAttackEntity(player, intValue, selectedSlot);
                    } finally {
                        this.sendClickSlotWithPacket(axe.slot(), intValue);
                    }
                    return flag;
                }
            }
        }
    }

    private boolean isPacketWithAttackEntity(PlayerEntity player, int intValue, int intValueValue) {
        if (mc.player != null && mc.player.networkHandler != null && mc.interactionManager != null) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValue));
            boolean flag;
            try {
                mc.interactionManager.attackEntity(mc.player, player);
                flag = true;
            } finally {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValueValue));
            }
            return flag;
        } else {
            return false;
        }
    }

    private void sendClickSlotWithPacket(int intValue, int intValueValue) {
        if (mc.player != null && mc.interactionManager != null && mc.player.networkHandler != null) {
            int syncId = mc.player.currentScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, intValue, intValueValue, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
        }
    }

    private int calculateInteger(int intValue) {
        for (byte index = 8; index >= 0; index--) {
            if (index != intValue) {
                return index;
            }
        }
        return intValue >= 0 && intValue < 9 ? intValue : -1;
    }

    private Hand createBlockingWithActiveHand() {
        if (mc.player != null && mc.player.isBlocking()) {
            Hand activeHand = mc.player.getActiveHand();
            if (activeHand == null) {
                return null;
            } else {
                return this.isShield(mc.player.getStackInHand(activeHand)) ? activeHand : null;
            }
        } else {
            return null;
        }
    }

    private boolean isUsingItem() {
        return mc.player != null && mc.player.isUsingItem() ? !this.releaseShield.isState() || this.createBlockingWithActiveHand() == null : false;
    }

    private void updateStackInHandWithUsingItem(Hand hand) {
        if (mc.player != null && mc.interactionManager != null && hand != null && this.isShield(mc.player.getStackInHand(hand)) && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, hand);
        }
    }

    private boolean isShield(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ShieldItem;
    }

    private boolean isItemWithMainHandStack() {
        Item item = mc.player.getMainHandStack().getItem();
        return item != Items.AIR && (item instanceof SwordItem || item instanceof PickaxeItem || item instanceof AxeItem || item instanceof HoeItem || item instanceof ShovelItem || item instanceof MaceItem || item == Items.MACE);
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity != null && entity != mc.player) {
            if (!entity.isAlive() || entity.getHealth() <= 0.0F) {
                return false;
            } else if (entity instanceof ArmorStandEntity) {
                return false;
            } else if (entity instanceof IronGolemEntity || entity instanceof BatEntity) {
                return false;
            } else if (AntiBot.checkBot(entity)) {
                return false;
            } else {
                if (entity instanceof PlayerEntity player) {
                    if (!this.targetTypes.is("Игроки")) {
                        return false;
                    }
                    if (this.isUnarmored(player) && !this.targetTypes.is("Голые")) {
                        return false;
                    }
                    if (player.hasStatusEffect(StatusEffects.INVISIBILITY) && !this.targetTypes.is("Инвизки")) {
                        return false;
                    }
                    if (Toshiba.INSTANCE.friendStorage.isFriend(entity.getName().getString()) && !this.targetTypes.is("Друзья")) {
                        return false;
                    }
                } else if (!(entity instanceof PassiveEntity) && !(entity instanceof CodEntity)) {
                    if (!(entity instanceof HostileEntity)) {
                        return false;
                    }
                    if (!this.targetTypes.is("Мобы")) {
                        return false;
                    }
                } else if (!this.targetTypes.is("Животные")) {
                    return false;
                }
                Vec3d nearestPoint = RotationPointUtil.getNearestPoint(entity);
                if (nearestPoint == null) {
                    nearestPoint = NearestPointUtil.getClosestPoint(entity);
                }
                if (nearestPoint == null && entity != null) {
                    nearestPoint = entity.getBoundingBox().getCenter();
                }
                return nearestPoint != null && !(mc.player.getEyePos().distanceTo(nearestPoint) > this.calculateGliding()) ? this.throughWalls.isState() || mc.player.canSee(entity) : false;
            }
        } else {
            return false;
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

    private boolean isWallBypassEnabled() {
        return this.throughWalls.isState() && this.wallCheckBypass.isState();
    }

    @EventHandler(priority = -200)
    public void onRwWallPacket(EventPacket eventPacket) {
        if (eventPacket.getType() == EventPacket.Type.SEND && !eventPacket.isCancelled()) {
            if (eventPacket.getPacket() instanceof PlayerMoveC2SPacket playerMoveC2SPacket && mc.player != null) {
                if (this.previousWorld != mc.world || this.previousPlayer != mc.player) {
                    this.updateYawWithPitch();
                }
                this.cachedPositionX = playerMoveC2SPacket.getX(this.cachedPositionX);
                this.cachedPositionY = playerMoveC2SPacket.getY(this.cachedPositionY);
                this.cachedPositionZ = playerMoveC2SPacket.getZ(this.cachedPositionZ);
                this.cachedYaw = playerMoveC2SPacket.getYaw(this.cachedYaw);
                this.cachedPitch = playerMoveC2SPacket.getPitch(this.cachedPitch);
                this.cachedEyeHeight = mc.player.getEyeY() - mc.player.getY();
                this.stateFlagForRwWallPacket = true;
            } else if (eventPacket.getPacket() instanceof PlayerActionC2SPacket playerActionC2SPacket) {
                if (playerActionC2SPacket.getAction() == Action.ABORT_DESTROY_BLOCK) {
                    BlockPos position = playerActionC2SPacket.getPos().toImmutable();
                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, position, (accessReceiver, accessValue) -> {
                        accessReceiver.blockPos = accessValue;
                    });
                } else if (playerActionC2SPacket.getAction() == Action.START_DESTROY_BLOCK) {
                    Object value = null;
                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                        accessReceiver.blockPos = (BlockPos) accessValue;
                    });
                }
            }
        }
    }

    private boolean canTypeWithSide() {
        if (!this.isWallBypassEnabled()) {
            return true;
        } else if (this.target != null && mc.player != null && mc.world != null && mc.player.networkHandler != null && mc.interactionManager != null) {
            Vec3d eyePos = mc.player.getEyePos();
            // Тот же анти-мисс запас, что и в основном reach-гейте (attackReachMargin).
            double doubleValue = Math.max(2.0, Math.min(this.range.getValue().doubleValue(), mc.player.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE)) - this.attackReachMargin);
            Vec3d position = eyePos.add(mc.player.getRotationVector().multiply(doubleValue));
            Box boundingBox = this.target.getBoundingBox();
            if (boundingBox.contains(eyePos)) {
                return true;
            } else {
                var optional = boundingBox.raycast(eyePos, position);
                if (optional.isEmpty()) {
                    return false;
                } else if (this.isExpandWithBoundingBox(boundingBox)) {
                    return true;
                } else {
                    BlockHitResult blockHitResult = this.createBlockHitResultFromRaycast(eyePos, optional.get(), null);
                    if (blockHitResult.getType() != HitResult.Type.BLOCK) {
                        return true;
                    } else {
                        BlockPos positionValue = blockHitResult.getBlockPos().toImmutable();
                        BlockState blockState = mc.world.getBlockState(positionValue);
                        if (blockState.getHardness(mc.world, positionValue) < 0.0F) {
                            return false;
                        } else if (this.createBlockHitResultFromRaycast(eyePos, optional.get(), positionValue).getType() == HitResult.Type.BLOCK) {
                            return false;
                        } else if (mc.player.isUsingItem() || mc.interactionManager.isBreakingBlock()) {
                            return false;
                        } else if (!this.canMultiplyWithFromPolar(boundingBox, positionValue, doubleValue)) {
                            return false;
                        } else {
                            BlockPos positionResult = positionValue.offset(blockHitResult.getSide());
                            if (positionResult.equals(this.blockPos)) {
                                positionResult = positionValue.up();
                                if (positionResult.equals(this.blockPos)) {
                                    positionResult = positionValue.down();
                                }
                            }
                            IClientPlayerInteractionManager interactionManager = (IClientPlayerInteractionManager) mc.interactionManager;
                            interactionManager.invokeSyncSelectedSlot();
                            this.sendPacket(positionResult);
                            this.sendPacket(positionValue);
                            interactionManager.invokeSendSequencedPacket(mc.world, input -> new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, positionValue, blockHitResult.getSide(), input));
                            return true;
                        }
                    }
                }
            }
        } else {
            return false;
        }
    }

    private boolean canMultiplyWithFromPolar(Box box, BlockPos position, double doubleValue) {
        if (this.stateFlagForRwWallPacket && this.previousWorld == mc.world && this.previousPlayer == mc.player) {
            Vec3d positionValue = new Vec3d(this.cachedPositionX, this.cachedPositionY + this.cachedEyeHeight, this.cachedPositionZ);
            if (box.contains(positionValue)) {
                return true;
            } else {
                Vec3d positionResult = positionValue.add(Vec3d.fromPolar(this.cachedPitch, this.cachedYaw).multiply(doubleValue));
                var optional = box.raycast(positionValue, positionResult);
                return optional.isPresent() && this.createBlockHitResultFromRaycast(positionValue, optional.get(), position).getType() != HitResult.Type.BLOCK;
            }
        } else {
            return false;
        }
    }

    private void updateYawWithPitch() {
        this.previousWorld = mc.world;
        this.previousPlayer = mc.player;
        this.stateFlagForRwWallPacket = false;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.blockPos = (BlockPos) accessValue;
        });
        this.cachedPositionX = mc.player == null ? Double.NaN : mc.player.getX();
        this.cachedPositionY = mc.player == null ? Double.NaN : mc.player.getY();
        this.cachedPositionZ = mc.player == null ? Double.NaN : mc.player.getZ();
        this.cachedYaw = mc.player == null ? Float.NaN : mc.player.getYaw();
        this.cachedPitch = mc.player == null ? Float.NaN : mc.player.getPitch();
        this.cachedEyeHeight = mc.player == null ? 0.0 : mc.player.getEyeY() - mc.player.getY();
    }

    private void sendPacket(BlockPos blockPos) {
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, blockPos, Direction.DOWN, 0));
        this.blockPos = blockPos;
    }

    private BlockHitResult createBlockHitResultFromRaycast(Vec3d position, Vec3d positionValue, BlockPos positionResult) {
        return mc.world.raycast(new BlockIgnoringRaycastContext(this, position, positionValue, ShapeType.OUTLINE, FluidHandling.NONE, mc.player, positionResult));
    }

    private boolean isExpandWithBoundingBox(Box box) {
        Box boxValue = mc.player.getBoundingBox().expand(0.5);
        Box boxResult = new Box(Math.min(mc.player.getX(), box.minX), Math.min(mc.player.getY(), box.minY), Math.min(mc.player.getZ(), box.minZ), Math.max(mc.player.getX(), box.maxX), Math.max(mc.player.getEyeY(), box.maxY), Math.max(mc.player.getZ(), box.maxZ)).expand(0.5);
        for (Entity entity : mc.world.getEntities()) {
            if ((entity instanceof AbstractBoatEntity || entity instanceof ShulkerEntity) && entity != mc.player.getVehicle() && entity.isAlive()) {
                Box boundingBox = entity.getBoundingBox();
                if (boundingBox.intersects(boxValue) || boundingBox.intersects(boxResult)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGlidingWithAttackCooldownProgress() {
        if (mc.player != null && this.target != null) {
            // Полный заряд на КАЖДОМ ударе: реальный прогресс (без +1.5-тиковой заводки) —
            // никакого скрытого среза урона, сила удара 0.95–1.0 от полной.
            // Исключение — чистая падающая дуга (isVanillaArcFalling, сервер даст крит ×1.5):
            // удар по дуга-порогу (0.72–0.82 заряда) даёт 1.08–1.23 урона от базы — больше полной
            // зарядки на земле (×1.0), и конечное окно дуги больше не проскакивает зря.
            float progressValue = mc.player.getAttackCooldownProgress(0.0F);
            boolean flagArc = isVanillaArcFalling() && progressValue >= this.attackArcThreshold;
            if (progressValue < this.attackCooldownThreshold && !flagArc) {
                this.aimConvergenceWaitTicks = 0;
                return false;
            } else {
                boolean flag = mc.player.isGliding() && this.target.isGliding();
                if (!flag && !this.canRaycastWithEyePos()) {
                    this.aimConvergenceWaitTicks = 0;
                    return false;
                } else {
                    if (flag) {
                        Vec3d position = this.target.getPos().add(0.0, this.target.getHeight() / 2.0, 0.0);
                        int intValue = 0;
                        if (ModuleRegistry.elytraTarget != null && ModuleRegistry.elytraTarget.isEnable()) {
                            intValue = (int) ModuleRegistry.elytraTarget.getPredictionMultiplier(this.target);
                        }
                        Vec3d positionValue = EntityPrediction.predict(this.target, position, intValue);
                        if (mc.player.getEyePos().distanceTo(positionValue) > 5.0) {
                            this.aimConvergenceWaitTicks = 0;
                            return false;
                        }
                    }
                    // Навод обязан быть готов к КАЖДОМУ удару: пока Slime не довёл прицел до точки
                    // (ошибка ≤ 3.0° yaw / ≤ 2.5° pitch), свинг не уходит. Гейт стоит после кулдауна
                    // и reach — в обычном бою докрутка завершается задолго до готового КД.
                    if (this.usesSlimeRotation() && !this.isSlimeAimConverged()) {
                        // Предохранитель: при экстремальном стрейнже не залипаем в ожидании навечно —
                        // после 6 тиков готового КД свинг уходит (рейкаст уже внутри хитбокса).
                        if (this.aimConvergenceWaitTicks++ < 6) {
                            return false;
                        }
                    }
                    this.aimConvergenceWaitTicks = 0;
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    private boolean isGliding() {
        if (mc.player != null && this.target != null) {
            return mc.player.isGliding() && this.target.isGliding() ? true : this.canRaycastWithEyePos();
        } else {
            return false;
        }
    }

    private boolean isAttackReady() {
        return this.isGlidingWithAttackCooldownProgress();
    }

    private boolean isConstantState() {
        if (!this.onlyCrits.isState()) {
            return true;
        } else {
            return mc.player != null && this.target != null ? this.createAttackAuraAttackStateFromOnGroundWithGliding() != AttackState.WAIT : false;
        }
    }

    private AttackState createAttackAuraAttackStateFromOnGroundWithGliding() {
        if (this.isInFluid()) {
            return AttackState.NORMAL;
        } else if (mc.player.isGliding()) {
            return AttackState.NORMAL;
        } else if (CriticalHitUtil.isInCobweb()) {
            return AttackState.NORMAL;
        } else if (this.smartCrit.isState() && mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
            return AttackState.NORMAL;
        } else {
            // Канон-чек мода: CriticalHitUtil.canCritical — та же проверка у других боевых модов
            // (TriggerBot и т.д.), проверенная «работает и не банит». Внутри: падение (velY<0) +
            // fallDistance>0 ИЛИ окно после поднятия ударом (hurtTime>0 и падение), ветки NoDelay
            // («Прыжок») и PacketCriticals (паутина/медленное падение), и smartCrit на земле.
            return CriticalHitUtil.canCritical(this.target) ? AttackState.CRIT : AttackState.WAIT;
        }
    }

    private boolean isVanillaArcFalling() {
        // Чистый ванильный тик дуги крита: падение + fallDistance>0 и ни одного серверного
        // блокера (лестница/вода/лава/ транспорт/полёт/элитра/ливитация/медленное падение/слепота/паутина).
        // Именно в этих тиках сервер умножит урон на 1.5 — дуга-порог работает только здесь.
        return mc.player != null && !mc.player.isOnGround() && mc.player.getVelocity().y < 0.0 && mc.player.fallDistance > 0.0F && !mc.player.isClimbing() && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() && !mc.player.isInLava() && !mc.player.hasVehicle() && !mc.player.getAbilities().flying && !mc.player.isGliding() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION) && !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !CriticalHitUtil.isInCobweb();
    }

    private boolean isSlimeAimConverged() {
        // Навод готов, когда применённая (GCD-снапнутая) ротация дошла до расчётной цели наведения.
        // Расчётную точку (targetRotations) и применённый взгляд (currentRotations) Slime публикует
        // каждый тик из apply() — разница между ними и есть недоводка в градусах.
        if (this.targetRotations == null || this.currentRotations == null) {
            return true;
        } else {
            return Math.abs(MathHelper.wrapDegrees(this.targetRotations.x - this.currentRotations.x)) <= 3.0F && Math.abs(MathHelper.wrapDegrees(this.targetRotations.y - this.currentRotations.y)) <= 2.5F;
        }
    }

    private boolean isServerGrounded() {
        return Toshiba.INSTANCE != null && Toshiba.INSTANCE.serverStorage != null && Toshiba.INSTANCE.serverStorage.isServerOnGround();
    }

    private boolean isInFluid() {
        return mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava();
    }

    private boolean hasJumpClearance() {
        Box box = mc.player.getBoundingBox().offset(0.0, 0.42, 0.0);
        return mc.world.isSpaceEmpty(mc.player, box);
    }

    public boolean isReadyToAttack() {
        if (this.target == null) {
            return false;
        } else if (!this.pauseWhileEating.isState() || !this.isUsingItem() && !AutoEat.shouldSuppressCombat()) {
            if (this.pauseInInventory.isState() && mc.currentScreen != null) {
                return false;
            } else {
                return this.swordOnly.isState() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem) ? false : this.isConstantState();
            }
        } else {
            return false;
        }
    }

    public boolean isAttackImminent() {
        if (!this.isReadyToAttack()) {
            return false;
        } else {
            return this.cps > System.currentTimeMillis() + 50L ? false : mc.player.isGliding() && this.target.isGliding() || this.canRaycastWithEyePos();
        }
    }

    public int getWhiteRiseTicksToAttack() {
        return this.whiteRiseTicksToAttack;
    }

    public void setRotationState(Vec2f currentRotations, Vec2f targetRotations) {
        this.currentRotations = currentRotations;
        this.targetRotations = targetRotations;
    }

    public boolean isAboveWater() {
        BlockPos position = BlockPos.ofFloored(mc.player.getPos().add(0.0, -0.4, 0.0));
        return !mc.player.isSubmergedInWater() && mc.world.getBlockState(position).isOf(Blocks.WATER);
    }

    public float getAttackCooldown() {
        return MathHelper.clamp(((ILivingEntity) mc.player).getLastAttackedTicks() / this.getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

    public float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.ATTACK_SPEED) * 20.0);
    }

    private float calculateGliding() {
        return mc.player.isGliding() ? ElytraTarget.INSTANCE.elytraAimRange.getValue().floatValue() : this.range.getValue().floatValue() + this.extraRange.getValue().floatValue();
    }

    @Override
    public void onDisable() {
        this.updateYawWithPitch();
        super.onDisable();
        if (this.target != null) {
            this.rotationTimer.reset();
        }
        this.target = null;
        if (this.returnCameraToRotation.isState()) {
            this.updateResetWithCamera();
        } else if (this.usesShardRotation()) {
            this.resetShardRotation.returnToCamera();
        } else if (this.usesSlimeRotation()) {
            this.resetSlimeRotation.returnToCamera();
        } else if (RotationStorage.instance != null) {
            RotationStorage.instance.stopRotation();
        }
        this.resetReallyWorldRotation.reset();
        this.resetFunTimeRotation.reset();
        this.resetSpookyTimeRotation.reset();
        this.resetNeuroRotation.reset();
        if (!this.usesShardRotation()) {
            this.resetShardRotation.reset();
        }
        if (!this.usesSlimeRotation()) {
            this.resetSlimeRotation.reset();
        }
        this.resetAresMineRotation.reset();
        this.legitSprintResetTicks = 0;
        this.stateFlagForSendResetWithCurrentTimeMillis = false;
        this.cachedCount = 0;
        this.wTapTicks = 0;
        this.whiteRiseTicksToAttack = 0;
    }

    @Override
    public void onEnable() {
        this.updateYawWithPitch();
        super.onEnable();
        this.target = null;
        this.resetReallyWorldRotation.reset();
        this.resetFunTimeRotation.reset();
        this.resetSpookyTimeRotation.reset();
        this.resetShardRotation.reset();
        this.resetSlimeRotation.reset();
        this.resetNeuroRotation.reset();
        this.resetAresMineRotation.reset();
        this.legitSprintResetTicks = 0;
        this.stateFlagForSendResetWithCurrentTimeMillis = false;
        this.cachedCount = 0;
        this.wTapTicks = 0;
        this.whiteRiseTicksToAttack = 0;
        if (mc.player != null) {
            this.currentRotations = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
        }
    }

    public FloatSetting getRange() {
        return this.range;
    }

    public ModeSetting getMoveFix() {
        return this.moveFix;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public static LivingEntity getLastTarget() {
        return lastTarget;
    }

    public Vec2f getCurrentRotations() {
        return this.currentRotations;
    }

    public Vec2f getTargetRotations() {
        return this.targetRotations;
    }

    public IntervalTimer getAttackTimer() {
        return this.attackTimer;
    }

    public int getWTapTicks() {
        return this.wTapTicks;
    }

    public static class BlockIgnoringRaycastContext extends RaycastContext {

        public final BlockPos blockPos;

        public BlockIgnoringRaycastContext(AttackAura attackAura, Vec3d position, Vec3d positionValue, ShapeType shapeType, FluidHandling fluidHandling, Entity entity, BlockPos blockPos) {
            super(position, positionValue, shapeType, fluidHandling, entity);
            this.blockPos = blockPos;
        }

        @Override
        public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
            return !pos.equals(this.blockPos) && !(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof TrapdoorBlock) && !(state.getBlock() instanceof FenceGateBlock) && !(state.getBlock() instanceof SlabBlock) ? super.getBlockShape(state, world, pos) : VoxelShapes.empty();
        }
    }

    public enum AttackState {

        CRIT, NORMAL, WAIT
    }
}
