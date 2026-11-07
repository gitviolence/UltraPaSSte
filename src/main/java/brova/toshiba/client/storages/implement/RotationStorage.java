package brova.toshiba.client.storages.implement;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.MovementInputEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.mixin.ICameraMixin;
import net.minecraft.util.math.MathHelper;

public final class RotationStorage implements MinecraftAccess {

    public static RotationStorage instance;

    private RotationState rotating = RotationState.IDLE;

    private float cachedScalarForCurrentYawSpeed;

    private float cachedScalarForCurrentPitchSpeed;

    private float cachedScalarForCurrentYawReturnSpeed;

    private float cachedScalarForCurrentPitchReturnSpeed;

    private int cachedCountForCurrentPriority;

    private int cachedCountForCurrentTimeout;

    private int cachedCountForIdleTicks;

    private Rotation rotation;

    public RotationStorage() {
        instance = this;
        EventBus.register(this);
    }

    public static double direction(float floatValue, float floatValueValue, float floatValueResult) {
        if (floatValueValue < 0.0F) {
            floatValue += 180.0F;
        }
        float floatValueItem = 1.0F;
        if (floatValueValue < 0.0F) {
            floatValueItem = -0.5F;
        }
        if (floatValueValue > 0.0F) {
            floatValueItem = 0.5F;
        }
        if (floatValueResult > 0.0F) {
            floatValue -= 90.0F * floatValueItem;
        }
        if (floatValueResult < 0.0F) {
            floatValue += 90.0F * floatValueItem;
        }
        return Math.toRadians(floatValue);
    }

    public static void fixMovement(MovementInputEvent event, float floatValue) {
        float movementForward = event.getMovementForward();
        float movementSideways = event.getMovementSideways();
        if (movementForward != 0.0F || movementSideways != 0.0F) {
            double doubleValue = MathHelper.wrapDegrees(Math.toDegrees(direction(floatValue, movementForward, movementSideways)));
            float floatValueValue = 0.0F;
            float floatValueResult = 0.0F;
            float MAX_VALUE = Float.MAX_VALUE;
            for (float index = -1.0F; index <= 1.0F; index++) {
                for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                    if (index != 0.0F || indexValue != 0.0F) {
                        double doubleValueValue = MathHelper.wrapDegrees(Math.toDegrees(direction(floatValue, index, indexValue)));
                        float magnitude = Math.abs(MathHelper.wrapDegrees((float) (doubleValue - doubleValueValue)));
                        if (magnitude < MAX_VALUE) {
                            MAX_VALUE = magnitude;
                            floatValueValue = index;
                            floatValueResult = indexValue;
                        }
                    }
                }
            }
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(event, new Object[] { floatValueValue }, (accessReceiver, accessArguments) -> {
                ((MovementInputEvent) accessReceiver).setMovementForward(((float) accessArguments[0]));
            });
            event.setMovementSideways(floatValueResult);
        }
    }

    @EventHandler
    public void onInput(MovementInputEvent event) {
        if (this.isRotating()) {
            fixMovement(event, MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw()));
        }
    }

    private void restoreCameraAfterAura() {
        if (ModuleRegistry.aura != null && ModuleRegistry.aura.isEnable() && brova.toshiba.client.utils.runtime.AccessGuard.read(ModuleRegistry.aura, accessReceiver -> accessReceiver.returnCameraToRotation).isState()) {
            boolean flag = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                ((RotationStorage) accessReceiver).stopRotation(((boolean) accessArguments[0]));
            });
        } else {
            Rotation rotation = new Rotation(FreeLookStorage.getFreeYaw(), FreeLookStorage.getFreePitch());
            if (this.isWithinRotationTolerance(rotation, this.currentYawReturnSpeed(), this.currentPitchReturnSpeed())) {
                this.stopRotation();
            }
        }
    }

    @EventHandler
    public void onEventTick(PlayerUpdateEvent event) {
        if (this.currentTask().equals(RotationState.AIM) && this.idleTicks() > this.currentTimeout()) {
            if (ModuleRegistry.aura != null && ModuleRegistry.aura.isEnable() && ModuleRegistry.aura.returnCameraToRotation.isState()) {
                this.stopRotation(false);
            } else {
                this.currentTask(RotationState.RESET);
            }
        }
        if (this.currentTask().equals(RotationState.RESET)) {
            this.restoreCameraAfterAura();
        }
        this.cachedCountForIdleTicks++;
    }

    public static void update(Rotation rotation, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue, boolean flag) {
        RotationStorage rotationStorage = instance;
        if (mc.player != null && rotationStorage.currentPriority() <= intValueValue) {
            if (rotationStorage.currentTask().equals(RotationState.IDLE) && !flag) {
                float yaw = mc.player.getYaw();
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { yaw }, (accessReceiver, accessArguments) -> {
                    FreeLookStorage.setFreeYaw(((float) accessArguments[0]));
                });
                FreeLookStorage.setFreePitch(mc.player.getPitch());
                FreeLookStorage.setActive(true);
            }
            rotationStorage.currentYawSpeed(floatValue);
            RotationStorage rotationStorageValue = brova.toshiba.client.utils.runtime.AccessGuard.invoke(rotationStorage, new Object[] { floatValueValue }, (accessReceiver, accessArguments) -> ((RotationStorage) accessReceiver).currentPitchSpeed(((float) accessArguments[0])));
            rotationStorage.currentYawReturnSpeed(floatValueResult);
            rotationStorage.currentPitchReturnSpeed(floatValueItem);
            rotationStorageValue = brova.toshiba.client.utils.runtime.AccessGuard.invoke(rotationStorage, new Object[] { intValue }, (accessReceiver, accessArguments) -> ((RotationStorage) accessReceiver).currentTimeout(((int) accessArguments[0])));
            rotationStorage.currentPriority(intValueValue);
            rotationStorage.currentTask(RotationState.AIM);
            rotationStorage.targetRotation(rotation);
            rotationStorage.idleTicks(0);
            rotationStorage.isWithinRotationTolerance(rotation, floatValue, floatValueValue);
        }
    }

    public static void update(Rotation rotation, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        update(rotation, floatValue, floatValue, floatValueValue, floatValueValue, intValue, intValueValue, false);
    }

    public static void update(Rotation rotation, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue) {
        update(rotation, floatValue, floatValueValue, floatValueResult, floatValueResult, intValue, intValueValue, false);
    }

    public static void updateExact(Rotation rotation, int intValue, int intValueValue, boolean flag) {
        RotationStorage rotationStorage = instance;
        if (mc.player != null && rotationStorage != null && rotationStorage.currentPriority() <= intValueValue) {
            if (rotationStorage.currentTask().equals(RotationState.IDLE) && !flag) {
                FreeLookStorage.setFreeYaw(mc.player.getYaw());
                float pitch = mc.player.getPitch();
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { pitch }, (accessReceiver, accessArguments) -> {
                    FreeLookStorage.setFreePitch(((float) accessArguments[0]));
                });
                FreeLookStorage.setActive(true);
            }
            rotationStorage.currentYawSpeed(360.0F);
            rotationStorage.currentPitchSpeed(360.0F);
            rotationStorage.currentYawReturnSpeed(360.0F);
            float floatValue = 360.0F;
            RotationStorage rotationStorageValue = brova.toshiba.client.utils.runtime.AccessGuard.invoke(rotationStorage, new Object[] { floatValue }, (accessReceiver, accessArguments) -> ((RotationStorage) accessReceiver).currentPitchReturnSpeed(((float) accessArguments[0])));
            rotationStorage.currentTimeout(intValue);
            rotationStorage.currentPriority(intValueValue);
            rotationStorage.currentTask(RotationState.AIM);
            rotationStorage.targetRotation(rotation);
            mc.player.setYaw(rotation.getYaw());
            mc.player.setPitch(MathHelper.clamp(rotation.getPitch(), -90.0F, 90.0F));
            rotationStorage.idleTicks(0);
        }
    }

    private boolean isWithinRotationTolerance(Rotation rotation, float floatValue, float floatValueValue) {
        if (mc.player == null) {
            return false;
        } else {
            float gCDValue = MouseGcdUtil.getGCDValue();
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            float floatValueResult = MathHelper.wrapDegrees(rotation.getYaw() - yaw);
            float floatValueItem = rotation.getPitch() - pitch;
            float floatValueEntry = Math.min(Math.abs(floatValueResult), floatValue);
            float floatValueCopy = Math.min(Math.abs(floatValueItem), floatValueValue);
            float clampedValue = MathHelper.clamp(floatValueResult, -floatValueEntry, floatValueEntry);
            float clampedValueValue = MathHelper.clamp(floatValueItem, -floatValueCopy, floatValueCopy);
            int intValue = Math.round(clampedValue / gCDValue);
            int intValueValue = Math.round(clampedValueValue / gCDValue);
            mc.player.setYaw(yaw + intValue * gCDValue);
            mc.player.setPitch(MathHelper.clamp(pitch + intValueValue * gCDValue, -90.0F, 90.0F));
            this.idleTicks(0);
            return new Rotation(mc.player).getDelta(rotation) < 1.0F;
        }
    }

    public void stopRotation() {
        this.stopRotation(true);
    }

    public void stopRotation(boolean flag) {
        if (mc.player != null && FreeLookStorage.isActive()) {
            if (flag) {
                mc.player.setYaw(FreeLookStorage.getFreeYaw());
                mc.player.setPitch(FreeLookStorage.getFreePitch());
            } else {
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
            }
        }
        this.currentTask(RotationState.IDLE);
        this.currentPriority(0);
        FreeLookStorage.setActive(false);
    }

    public boolean isRotating() {
        return !this.rotating.equals(RotationState.IDLE);
    }

    public RotationState currentTask() {
        return this.rotating;
    }

    public float currentYawSpeed() {
        return this.cachedScalarForCurrentYawSpeed;
    }

    public float currentPitchSpeed() {
        return this.cachedScalarForCurrentPitchSpeed;
    }

    public float currentYawReturnSpeed() {
        return this.cachedScalarForCurrentYawReturnSpeed;
    }

    public float currentPitchReturnSpeed() {
        return this.cachedScalarForCurrentPitchReturnSpeed;
    }

    public int currentPriority() {
        return this.cachedCountForCurrentPriority;
    }

    public int currentTimeout() {
        return this.cachedCountForCurrentTimeout;
    }

    public int idleTicks() {
        return this.cachedCountForIdleTicks;
    }

    public Rotation targetRotation() {
        return this.rotation;
    }

    public RotationStorage currentTask(RotationState rotating) {
        this.rotating = rotating;
        return this;
    }

    public RotationStorage currentYawSpeed(float cachedScalarForCurrentYawSpeed) {
        this.cachedScalarForCurrentYawSpeed = cachedScalarForCurrentYawSpeed;
        return this;
    }

    public RotationStorage currentPitchSpeed(float cachedScalarForCurrentPitchSpeed) {
        this.cachedScalarForCurrentPitchSpeed = cachedScalarForCurrentPitchSpeed;
        return this;
    }

    public RotationStorage currentYawReturnSpeed(float cachedScalarForCurrentYawReturnSpeed) {
        this.cachedScalarForCurrentYawReturnSpeed = cachedScalarForCurrentYawReturnSpeed;
        return this;
    }

    public RotationStorage currentPitchReturnSpeed(float cachedScalarForCurrentPitchReturnSpeed) {
        this.cachedScalarForCurrentPitchReturnSpeed = cachedScalarForCurrentPitchReturnSpeed;
        return this;
    }

    public RotationStorage currentPriority(int cachedCountForCurrentPriority) {
        this.cachedCountForCurrentPriority = cachedCountForCurrentPriority;
        return this;
    }

    public RotationStorage currentTimeout(int cachedCountForCurrentTimeout) {
        this.cachedCountForCurrentTimeout = cachedCountForCurrentTimeout;
        return this;
    }

    public RotationStorage idleTicks(int cachedCountForIdleTicks) {
        this.cachedCountForIdleTicks = cachedCountForIdleTicks;
        return this;
    }

    public RotationStorage targetRotation(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }
}
