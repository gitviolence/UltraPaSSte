package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.RotationEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AimAssist.RotationDelta;
import brova.toshiba.client.modules.impl.combat.AimAssist.RotationError;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import brova.toshiba.client.storages.implement.RotationState;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.math.KalmanFilter;
import brova.toshiba.client.utils.profile.Profiled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@Profiled
public final class AimAssist extends Module {

    public static final AimAssist INSTANCE = new AimAssist();

    private final ModeSetting priority = new ModeSetting("Приоритет", "Угол", "Угол", "Дистанция", "Здоровье");

    private final FloatSetting yawSpeed = new FloatSetting("Yaw Speed", 90.0F, 0.0F, 360.0F, 1.0F);

    private final FloatSetting pitchSpeed = new FloatSetting("Pitch Speed", 70.0F, 0.0F, 360.0F, 1.0F);

    private final FloatSetting fov = new FloatSetting("FOV", 90.0F, 1.0F, 360.0F, 1.0F);

    private final FloatSetting aimRange = new FloatSetting("Дистанция наводки", 3.0F, 0.5F, 7.0F, 0.1F);

    private final MultiSetting targetTypes = new MultiSetting("Таргеты", new BooleanSetting("Игроки", true), new BooleanSetting("Инвизки", true), new BooleanSetting("Мобы", true), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false));

    private final BooleanSetting pauseWhenLookingAtHitbox = new BooleanSetting("Не работать когда камера на хитбоксе", true);

    private final BooleanSetting onlyWhileMovingMouse = new BooleanSetting("Только при движение мышки", true);

    private final BooleanSetting showTargetEsp = new BooleanSetting("Отображать TargertESP", true);

    private LivingEntity target;

    private long lastUpdateTimestamp;

    private final KalmanFilter pauseKalmanFilter = new KalmanFilter();

    private final KalmanFilter correctKalmanFilter = new KalmanFilter(0.35, 1.2, 1.5);

    public AimAssist() {
        super("AimAssist", "Плавная коррекция прицела на выбранную цель.", ModuleCategory.COMBAT);
        this.addSettings(this.priority, this.yawSpeed, this.pitchSpeed, this.fov, this.aimRange, this.targetTypes, this.pauseWhenLookingAtHitbox, this.onlyWhileMovingMouse, this.showTargetEsp);
    }

    public LivingEntity getEspTarget() {
        if (this.isEnable() && this.showTargetEsp.isState() && this.canAim() && this.target != null && !this.target.isRemoved()) {
            return this.isTargetWithinAimRange(this.target, mc.player.getYaw(), mc.player.getPitch()) ? this.target : null;
        } else {
            return null;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.clearTarget();
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AimAssist) accessReceiver).applyCameraAim();
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.clearTarget();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        this.applyCameraAim();
    }

    @EventHandler(priority = 200)
    public void onLook(RotationEvent event) {
        if (event.isCancelled()) {
            this.clearTarget();
        } else if (!this.canAim()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((AimAssist) accessReceiver).clearTarget();
            });
        } else {
            double frameDeltaSeconds = this.getFrameDeltaSeconds();
            double doubleValue = event.getYaw() * 0.15;
            double doubleValueValue = event.getPitch() * 0.15;
            if (Double.isFinite(doubleValue) && Double.isFinite(doubleValueValue)) {
                boolean state = this.onlyWhileMovingMouse.isState();
                if (state && !event.isPhysicalMouseMoved()) {
                    this.pauseKalmanFilter.pause(frameDeltaSeconds);
                    KalmanFilter correctKalmanFilter = this.correctKalmanFilter;
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(correctKalmanFilter, new Object[] { frameDeltaSeconds }, (accessReceiver, accessArguments) -> {
                        ((KalmanFilter) accessReceiver).pause(((double) accessArguments[0]));
                    });
                } else {
                    if (state) {
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                            ((AimAssist) accessReceiver).applyMouseAim();
                        });
                    }
                    if (!this.isRotationBusy()) {
                        this.resetSmoothing();
                    } else {
                        float floatValue = (float) (mc.player.getYaw() + doubleValue);
                        float clampedValue = (float) MathHelper.clamp(mc.player.getPitch() + doubleValueValue, -90.0, 90.0);
                        LivingEntity target = this.selectTarget(floatValue, clampedValue);
                        if (target != this.target) {
                            this.target = target;
                            this.resetSmoothing();
                        }
                        if (this.target == null) {
                            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                                ((AimAssist) accessReceiver).resetSmoothing();
                            });
                        } else if (this.pauseWhenLookingAtHitbox.isState() && this.isCrosshairInsideTarget(this.target, floatValue, clampedValue)) {
                            this.resetSmoothing();
                        } else {
                            RotationError rotationError = this.calculateRotationError(this.target, floatValue, clampedValue);
                            double doubleValueResult = this.calculateMouseSensitivityStep();
                            RotationDelta rotationDelta = new RotationDelta(this.pauseKalmanFilter.correct(rotationError.yawError(), doubleValue, frameDeltaSeconds, this.yawSpeed.get(), doubleValueResult, state, this.yawSpeed.get() / 360.0F), this.correctKalmanFilter.correct(this.scoreTarget(this.target, floatValue, clampedValue), doubleValueValue, frameDeltaSeconds, this.pitchSpeed.get(), doubleValueResult, state, this.pitchSpeed.get() / 360.0F));
                            if (!rotationDelta.isZero()) {
                                this.applyQuantizedRotation(event, rotationDelta, doubleValueResult);
                            }
                        }
                    }
                }
            } else {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((AimAssist) accessReceiver).clearTarget();
                });
            }
        }
    }

    private boolean canAim() {
        return mc.player != null && mc.world != null && mc.currentScreen == null && mc.mouse != null && mc.mouse.isCursorLocked();
    }

    private boolean isRotationBusy() {
        if (FreeLookStorage.isActive()) {
            return false;
        } else {
            return RotationStorage.instance != null && RotationStorage.instance.isRotating() ? false : !this.isAuraControllingRotation();
        }
    }

    private void applyMouseAim() {
        if (!this.isAuraControllingRotation()) {
            RotationStorage instance = RotationStorage.instance;
            if (instance == null || !instance.isRotating() && !FreeLookStorage.isActive()) {
                if (FreeLookStorage.isActive()) {
                    mc.player.setYaw(FreeLookStorage.getFreeYaw());
                    mc.player.setPitch(FreeLookStorage.getFreePitch());
                    FreeLookStorage.setActive(false);
                }
            } else {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(instance, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((RotationStorage) accessReceiver).stopRotation();
                });
            }
        }
    }

    private void applyCameraAim() {
        if (mc.player != null && !this.isAuraControllingRotation()) {
            RotationStorage instance = RotationStorage.instance;
            if (instance != null && instance.currentTask() == RotationState.RESET) {
                instance.stopRotation(false);
            } else if (FreeLookStorage.isActive() && (instance == null || !instance.isRotating())) {
                FreeLookStorage.setFreeYaw(mc.player.getYaw());
                FreeLookStorage.setFreePitch(mc.player.getPitch());
                boolean flag = false;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                    FreeLookStorage.setActive(((boolean) accessArguments[0]));
                });
            }
        }
    }

    private boolean isAuraControllingRotation() {
        return ModuleRegistry.aura != null && ModuleRegistry.aura.isEnable() && ModuleRegistry.aura.getTarget() != null && !brova.toshiba.client.utils.runtime.AccessGuard.read(ModuleRegistry.aura, accessReceiver -> accessReceiver.rotationType).is("БезРотации");
    }

    private double calculateMouseSensitivityStep() {
        double doubleValue = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double doubleValueValue = mc.options.getPerspective().isFirstPerson() && mc.player.isUsingSpyglass() && !mc.options.smoothCameraEnabled ? 1.0 : 8.0;
        return doubleValue * doubleValue * doubleValue * doubleValueValue * 0.15;
    }

    private void applyQuantizedRotation(RotationEvent event, RotationDelta rotationDelta, double doubleValue) {
        int intValue = (int) Math.round(rotationDelta.pitch() / doubleValue);
        double clampedValue = MathHelper.clamp(mc.player.getPitch() + event.getPitch() * 0.15, -90.0, 90.0);
        int roundedDown = (int) Math.floor((90.0 - clampedValue) / doubleValue);
        int roundedUp = (int) Math.ceil((-90.0 - clampedValue) / doubleValue);
        int clampedValueValue = MathHelper.clamp(intValue, roundedUp, roundedDown);
        if (clampedValueValue != intValue) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this.correctKalmanFilter, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((KalmanFilter) accessReceiver).reset();
            });
        }
        double doubleValueValue = event.getYaw() + rotationDelta.yaw() / 0.15;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(event, new Object[] { doubleValueValue }, (accessReceiver, accessArguments) -> {
            ((RotationEvent) accessReceiver).setYaw(((double) accessArguments[0]));
        });
        event.setPitch(event.getPitch() + clampedValueValue * doubleValue / 0.15);
    }

    private LivingEntity selectTarget(float floatValue, float floatValueValue) {
        if (this.target != null && this.isTargetWithinAimRange(this.target, floatValue, floatValueValue)) {
            return this.target;
        } else {
            double doubleValue = this.aimRange.get() * this.aimRange.get();
            LivingEntity entity = null;
            double MAX_VALUE = Double.MAX_VALUE;
            for (LivingEntity entityValue : mc.world.getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(this.aimRange.get()), this::isValidTarget)) {
                double doubleValueValue = mc.player.squaredDistanceTo(entityValue);
                if (!(doubleValueValue > doubleValue)) {
                    RotationError rotationError = this.calculateRotationError(entityValue, floatValue, floatValueValue);
                    if (!(rotationError.magnitude() > this.fov.get())) {
                        double doubleValueResult = this.priority.is("Дистанция") ? Math.sqrt(doubleValueValue) : (this.priority.is("Здоровье") ? entityValue.getHealth() : rotationError.magnitude());
                        if (doubleValueResult < MAX_VALUE) {
                            entity = entityValue;
                            MAX_VALUE = doubleValueResult;
                        }
                    }
                }
            }
            return entity;
        }
    }

    private boolean isTargetWithinAimRange(LivingEntity entity, float floatValue, float floatValueValue) {
        return this.isValidTarget(entity) && mc.player.squaredDistanceTo(entity) <= this.aimRange.get() * this.aimRange.get() && this.calculateRotationError(entity, floatValue, floatValueValue).magnitude() <= this.fov.get();
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == mc.player || !entity.isAlive() || entity.getHealth() <= 0.0F || entity.isSpectator()) {
            return false;
        } else if (entity instanceof ArmorStandEntity || entity instanceof IronGolemEntity || entity instanceof BatEntity) {
            return false;
        } else if (AntiBot.checkBot(entity)) {
            return false;
        } else {
            if (entity instanceof PlayerEntity player) {
                if (!this.targetTypes.is("Игроки") || player.isSleeping()) {
                    return false;
                }
                if (player.hasStatusEffect(StatusEffects.INVISIBILITY) && !this.targetTypes.is("Инвизки")) {
                    return false;
                }
                if (Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString()) && !this.targetTypes.is("Друзья")) {
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
            return mc.player.canSee(entity);
        }
    }

    private boolean isCrosshairInsideTarget(LivingEntity entity, float floatValue, float floatValueValue) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d rotationVector = mc.player.getRotationVector(floatValueValue, floatValue);
        return this.getInterpolatedBoundingBox(entity).raycast(eyePos, eyePos.add(rotationVector.multiply(this.aimRange.get()))).isPresent();
    }

    private RotationError calculateRotationError(LivingEntity entity, float floatValue, float floatValueValue) {
        Vec2f rotation = this.calculateRotations(this.findAimPoint(entity, floatValue, floatValueValue));
        float floatValueResult = MathHelper.wrapDegrees(rotation.x - floatValue);
        float floatValueItem = rotation.y - floatValueValue;
        return new RotationError(floatValueResult, floatValueItem);
    }

    private double scoreTarget(LivingEntity entity, float floatValue, float floatValueValue) {
        Box interpolatedBoundingBox = this.getInterpolatedBoundingBox(entity);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d position = this.findAimPoint(entity, floatValue, floatValueValue);
        double doubleValue = Math.hypot(position.x - eyePos.x, position.z - eyePos.z);
        double doubleValueValue = Math.min(interpolatedBoundingBox.getLengthY() * 0.05, 0.08);
        double doubleValueResult = -Math.toDegrees(Math.atan2(interpolatedBoundingBox.maxY - doubleValueValue - eyePos.y, doubleValue));
        double doubleValueItem = -Math.toDegrees(Math.atan2(interpolatedBoundingBox.minY + doubleValueValue - eyePos.y, doubleValue));
        return MathHelper.clamp((double) floatValueValue, doubleValueResult, doubleValueItem) - floatValueValue;
    }

    private Box getInterpolatedBoundingBox(LivingEntity entity) {
        Vec3d position = entity.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)).subtract(entity.getPos());
        return entity.getBoundingBox().offset(position);
    }

    private Vec3d findAimPoint(LivingEntity entity, float floatValue, float floatValueValue) {
        Box interpolatedBoundingBox = this.getInterpolatedBoundingBox(entity);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d rotationVector = mc.player.getRotationVector(floatValueValue, floatValue);
        double doubleValue = Math.max(0.0, interpolatedBoundingBox.getCenter().subtract(eyePos).dotProduct(rotationVector));
        Vec3d position = eyePos.add(rotationVector.multiply(doubleValue));
        return new Vec3d(MathHelper.clamp(position.x, interpolatedBoundingBox.minX + interpolatedBoundingBox.getLengthX() * 0.2, interpolatedBoundingBox.maxX - interpolatedBoundingBox.getLengthX() * 0.2), MathHelper.clamp(position.y, interpolatedBoundingBox.minY + interpolatedBoundingBox.getLengthY() * 0.55, interpolatedBoundingBox.minY + interpolatedBoundingBox.getLengthY() * 0.85), MathHelper.clamp(position.z, interpolatedBoundingBox.minZ + interpolatedBoundingBox.getLengthZ() * 0.2, interpolatedBoundingBox.maxZ - interpolatedBoundingBox.getLengthZ() * 0.2));
    }

    private Vec2f calculateRotations(Vec3d position) {
        Vec3d positionValue = position.subtract(mc.player.getEyePos());
        float floatValue = (float) Math.toDegrees(Math.atan2(positionValue.z, positionValue.x)) - 90.0F;
        float floatValueValue = (float) (-Math.toDegrees(Math.atan2(positionValue.y, Math.hypot(positionValue.x, positionValue.z))));
        return new Vec2f(floatValue, floatValueValue);
    }

    private void resetSmoothing() {
        this.pauseKalmanFilter.reset();
        this.correctKalmanFilter.reset();
    }

    private void clearTarget() {
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.target = (LivingEntity) accessValue;
        });
        this.lastUpdateTimestamp = 0L;
        this.resetSmoothing();
    }

    private double getFrameDeltaSeconds() {
        long lastUpdateTimestamp = System.nanoTime();
        double doubleValue = this.lastUpdateTimestamp == 0L ? 0.016666666666666666 : (lastUpdateTimestamp - this.lastUpdateTimestamp) / 1.0E9;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        return MathHelper.clamp(doubleValue, 1.0E-6, 0.05);
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public static record RotationDelta(double yaw, double pitch) {

        boolean isZero() {
            return this.yaw == 0.0 && this.pitch == 0.0;
        }
    }

    public static record RotationError(float yawError, float pitchError) {

        float magnitude() {
            return (float) Math.hypot(this.yawError, this.pitchError);
        }
    }
}
