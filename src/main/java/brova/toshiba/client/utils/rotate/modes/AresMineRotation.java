package brova.toshiba.client.utils.rotate.modes;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import brova.toshiba.client.utils.rotate.RotationPointUtil;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.world.EntityRaycastUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class AresMineRotation extends RotationModeBase {

    private final AttackAura attackAura;

    private LivingEntity livingEntity;

    private float yaw;

    private float pitch;

    private float cachedScalar;

    private boolean stateFlag;

    private Vec3d position;

    private Vec3d subtractPosition;

    private long lastUpdateMillis;

    private Vec3d lerpPosition;

    public AresMineRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public void reset() {
        this.livingEntity = null;
        this.cachedScalar = 0.0F;
        this.stateFlag = false;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.position = (Vec3d) accessValue;
        });
        this.subtractPosition = null;
        this.lastUpdateMillis = 0L;
        value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.lerpPosition = (Vec3d) accessValue;
        });
        if (mc.player != null) {
            this.yaw = mc.player.getYaw();
            this.pitch = mc.player.getPitch();
        }
    }

    public void onAttack() {
        this.stateFlag = true;
        this.cachedScalar = Math.min(this.cachedScalar, 0.0F);
    }

    @Override
    public void updateRotations(LivingEntity livingEntity) {
        if (mc.player != null && livingEntity != null) {
            if (this.livingEntity != livingEntity) {
                this.livingEntity = livingEntity;
                this.yaw = mc.player.getYaw();
                this.pitch = mc.player.getPitch();
                this.cachedScalar = 0.0F;
                this.stateFlag = false;
                this.position = null;
                this.subtractPosition = null;
                this.lastUpdateMillis = 0L;
                this.lerpPosition = null;
            }
            Vec3d position = livingEntity.getPos();
            Vec3d subtractPosition = livingEntity.getVelocity();
            if (this.position != null && this.subtractPosition != null && subtractPosition.subtract(this.subtractPosition).lengthSquared() > 0.005) {
                this.lastUpdateMillis = System.currentTimeMillis();
                this.lerpPosition = this.position;
            }
            this.position = position;
            this.subtractPosition = subtractPosition;
            Vec3d positionValue = this.createCenterWithBoundingBox(livingEntity);
            if (this.lerpPosition != null) {
                long elapsedMillis = System.currentTimeMillis() - this.lastUpdateMillis;
                if (elapsedMillis < 20L) {
                    float floatValue = (float) elapsedMillis / 20.0F;
                    positionValue = this.lerpPosition.lerp(positionValue, floatValue * floatValue);
                } else {
                    this.lerpPosition = null;
                }
            }
            Vec2f rotations = RotationUtil.getRotations(positionValue);
            float x = rotations.x;
            float clampedValue = MathHelper.clamp(rotations.y, -90.0F, 90.0F);
            boolean flag = EntityRaycastUtil.rayTrace(mc.player.getRotationVector(this.pitch, this.yaw), 100.0, livingEntity.getBoundingBox().expand(-0.22));
            float floatValueValue = MathHelper.wrapDegrees(x - this.yaw);
            float floatValueResult = clampedValue - this.pitch;
            float yaw = this.yaw;
            float pitch = this.pitch;
            if (!flag) {
                if (this.stateFlag) {
                    float cachedScalar = Math.abs(floatValueValue) > 55.0F ? 0.01F : 0.001F;
                    if (this.cachedScalar >= 0.0F) {
                        this.cachedScalar -= cachedScalar;
                    }
                    if (this.cachedScalar <= 0.0F) {
                        this.stateFlag = false;
                    }
                } else {
                    this.cachedScalar += 0.005F;
                    if (this.cachedScalar >= 0.1F) {
                        this.stateFlag = true;
                    }
                }
                float clampedValueValue = MathHelper.clamp(Math.max(this.cachedScalar, 0.0F) * 2.5F, 0.0F, 1.0F);
                yaw = this.yaw + floatValueValue * clampedValueValue;
                pitch = this.pitch + floatValueResult * clampedValueValue;
            }
            yaw = this.calculateGCDValue(yaw, this.yaw);
            pitch = this.calculateGCDValue(pitch, this.pitch);
            Rotation rotation = new Rotation(yaw, pitch);
            RotationStorage.update(rotation, 360.0F, 360.0F, 13.0F, 18.0F, 1, 1, false);
            this.yaw = yaw;
            this.pitch = pitch;
            this.rotate = new Vec2f(yaw, pitch);
            this.attackAura.setCurrentRotations(this.rotate);
            this.attackAura.setTargetRotations(new Vec2f(yaw, pitch));
        }
    }

    private Vec3d createCenterWithBoundingBox(LivingEntity entity) {
        Vec3d position = this.attackAura.resolveRotationPoint(null);
        if (position != null) {
            return position;
        } else if (entity.isGliding() && ModuleRegistry.elytraTarget != null && ModuleRegistry.elytraTarget.isEnable()) {
            return entity.getBoundingBox().getCenter();
        } else {
            Vec3d point = RotationPointUtil.getPoint(entity);
            if (point == null) {
                point = entity.getPos().add(0.0, entity.getHeight() * 0.72, 0.0);
            }
            return point != null ? point : entity.getBoundingBox().getCenter();
        }
    }

    private float calculateGCDValue(float floatValue, float floatValueValue) {
        float gCDValue = MouseGcdUtil.getGCDValue();
        return gCDValue <= 0.0F ? floatValue : floatValueValue + Math.round((floatValue - floatValueValue) / gCDValue) * gCDValue;
    }
}
