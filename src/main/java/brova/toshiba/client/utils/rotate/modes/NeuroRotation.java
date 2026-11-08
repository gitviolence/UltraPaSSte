package brova.toshiba.client.utils.rotate.modes;

import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.neuro.CombatContextFeatures;
import brova.toshiba.client.utils.neuro.NeuroManager;
import brova.toshiba.client.utils.neuro.RotationStep;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import brova.toshiba.client.utils.rotate.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class NeuroRotation extends RotationModeBase {

    private final AttackAura attackAura;

    private LivingEntity targetForReset;

    private LivingEntity targetForUpdateRotations;

    private int cachedCount = Integer.MIN_VALUE;

    private long lastUpdateTimestamp;

    private float yaw;

    private float pitch;

    public NeuroRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public void reset() {
        this.targetForReset = null;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.targetForUpdateRotations = (LivingEntity) accessValue;
        });
        this.cachedCount = Integer.MIN_VALUE;
        this.lastUpdateTimestamp = 0L;
        NeuroManager.getInstance().resetPlayback();
    }

    public void returnToCamera() {
        if (RotationStorage.instance != null && RotationStorage.instance.isRotating() && RotationStorage.instance.currentPriority() <= 1) {
            RotationStorage.instance.stopRotation(!this.attackAura.returnCameraToRotation.isState());
        }
        this.reset();
    }

    @Override
    public void updateRotations(LivingEntity targetForUpdateRotations) {
        if (mc.player != null && mc.world != null && targetForUpdateRotations != null && RotationStorage.instance != null) {
            NeuroManager instance = NeuroManager.getInstance();
            if (instance.getActiveModel() == null) {
                this.returnToCamera();
            } else if (!mc.player.isBlocking() && mc.currentScreen == null) {
                if (RotationStorage.instance.currentPriority() > 1) {
                    this.reset();
                } else {
                    long lastUpdateTimestamp = System.nanoTime();
                    if (this.targetForReset != mc.player || this.targetForUpdateRotations != targetForUpdateRotations || this.lastUpdateTimestamp != 0L && lastUpdateTimestamp - this.lastUpdateTimestamp > 250000000L) {
                        this.reset();
                        this.targetForReset = mc.player;
                        this.targetForUpdateRotations = targetForUpdateRotations;
                    }
                    if (this.cachedCount != mc.player.age) {
                        float yaw = mc.player.getYaw();
                        float pitch = mc.player.getPitch();
                        float floatValue = 0.05F;
                        if (this.lastUpdateTimestamp != 0L && (mc.player.age - this.cachedCount != 1 || Math.abs(MathHelper.wrapDegrees(yaw - this.yaw)) > 0.01F || Math.abs(pitch - this.pitch) > 0.01F)) {
                            instance.resetPlayback();
                        }
                        Box boundingBox = targetForUpdateRotations.getBoundingBox();
                        Vec3d position = CombatContextFeatures.defaultPoint(boundingBox);
                        RotationStep rotationStep = instance.nextMovement(targetForUpdateRotations, yaw, pitch, floatValue, boundingBox, position);
                        RotationStorage.updateExact(new Rotation(yaw + rotationStep.yaw(), MathHelper.clamp(pitch + rotationStep.pitch(), -90.0F, 90.0F)), 2, 1, false);
                        this.yaw = mc.player.getYaw();
                        this.pitch = mc.player.getPitch();
                        instance.movementApplied(targetForUpdateRotations, yaw, pitch);
                        this.rotate = new Vec2f(this.yaw, this.pitch);
                        this.attackAura.setCurrentRotations(this.rotate);
                        this.attackAura.setTargetRotations(RotationUtil.getRotations(position));
                        this.cachedCount = mc.player.age;
                        this.lastUpdateTimestamp = lastUpdateTimestamp;
                    }
                }
            } else {
                this.returnToCamera();
            }
        } else {
            this.reset();
        }
    }
}
