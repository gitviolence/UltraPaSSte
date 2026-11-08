package brova.toshiba.client.utils.rotate.modes;

import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import brova.toshiba.client.utils.rotate.RotationPointUtil;
import brova.toshiba.client.utils.rotate.RotationUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class FunTimeRotation extends RotationModeBase {

    private final AttackAura attackAura;

    private LivingEntity livingEntity;

    private float x;

    private float y;

    private float valueForReset;

    private boolean rotationInitialized;

    private boolean hadPlayer;

    private float cachedScalarForUpdateRotations;

    private int sin;

    private float cachedScalarForReset = 1.0F;

    private int rotationAgeTicks;

    private int rotationDelayTicks;

    private float wrapDegrees;

    private float valueForAttack;

    public FunTimeRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public void reset() {
        this.livingEntity = null;
        this.valueForReset = 0.0F;
        this.rotationInitialized = false;
        this.cachedScalarForReset = Math.max(0.0F, this.cachedScalarForReset - 0.08F);
        this.sin = 0;
        this.hadPlayer = mc.player != null;
        if (mc.player != null) {
            this.x = mc.player.getYaw();
            this.y = mc.player.getPitch();
        } else {
            this.x = 0.0F;
            this.y = 0.0F;
        }
    }

    public void onAttack() {
        if (mc.player != null) {
            ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
            this.rotationAgeTicks = Math.min(this.rotationAgeTicks + 1, 30);
            this.rotationDelayTicks = 120;
            float floatValue = 1.0F + Math.min(0.05F * this.rotationAgeTicks, 1.0F);
            float floatValueValue = 1.0F + this.rotationAgeTicks / 30.0F * 0.8F;
            float floatValueResult = (32.0F + threadLocalRandom.nextFloat() * 14.0F * floatValueValue) * floatValue;
            float floatValueItem = (4.5F + threadLocalRandom.nextFloat() * 2.0F * floatValueValue) * floatValue;
            float floatValueEntry = threadLocalRandom.nextBoolean() ? 1.0F : -1.0F;
            int intValue = threadLocalRandom.nextInt(100);
            float floatValueCopy;
            if (intValue < 12) {
                floatValueCopy = -2.6F;
            } else if (intValue < 28) {
                floatValueCopy = -1.3F;
            } else {
                floatValueCopy = 1.0F;
            }
            this.wrapDegrees = floatValueEntry * floatValueResult;
            this.valueForAttack = floatValueCopy * floatValueItem;
        }
    }

    @Override
    public void updateRotations(LivingEntity livingEntity) {
        if (mc.player != null && livingEntity != null) {
            if (mc.player.isBlocking()) {
                this.rotate = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
                this.x = this.rotate.x;
                this.y = this.rotate.y;
            } else {
                if (!this.hadPlayer) {
                    this.x = mc.player.getYaw();
                    this.y = mc.player.getPitch();
                    this.hadPlayer = true;
                }
                if (this.livingEntity != livingEntity) {
                    this.livingEntity = livingEntity;
                    this.valueForReset = 0.0F;
                    this.rotationInitialized = false;
                    this.sin = 0;
                    this.cachedScalarForReset = 1.0F;
                }
                this.sin++;
                float floatValue = (float) Math.sin(this.sin * 0.2F) * 0.13F;
                float floatValueValue = (float) Math.cos(this.sin * 0.032F) * 0.075F;
                float floatValueResult = (ThreadLocalRandom.current().nextFloat() - 0.5F) * 0.008F;
                this.cachedScalarForReset = Math.min(1.0F, this.cachedScalarForReset + 0.08F);
                this.cachedScalarForUpdateRotations = (floatValue + floatValueValue + floatValueResult) * this.cachedScalarForReset;
                Vec3d position = this.attackAura.resolveRotationPoint(RotationPointUtil.getMultipoint(livingEntity, 128.0));
                Vec2f rotations = RotationUtil.getRotations(position);
                float x = rotations.x;
                float y = rotations.y;
                float magnitude = Math.abs(MathHelper.wrapDegrees(x - this.x));
                boolean flag = mc.player.getAttackCooldownProgress(1.0F) > 0.9F && this.attackAura.getWhiteRiseTicksToAttack() <= 1;
                if (!this.rotationInitialized) {
                    float floatValueItem = 0.0041F;
                    if (magnitude > 60.0F) {
                        floatValueItem += 0.02F;
                    } else if (magnitude > 30.0F) {
                        floatValueItem += 0.01F;
                    } else {
                        floatValueItem += 0.0034F;
                    }
                    if (flag) {
                        floatValueItem += 0.01F;
                    }
                    this.valueForReset = this.valueForReset + floatValueItem * (1.6F + this.cachedScalarForUpdateRotations);
                    if (this.valueForReset >= 0.22F) {
                        this.rotationInitialized = true;
                    }
                } else {
                    float floatValueEntry = flag ? 0.032F : 0.007F;
                    this.valueForReset = this.valueForReset - floatValueEntry * (2.1F + this.cachedScalarForUpdateRotations);
                    if (this.valueForReset <= -0.04F) {
                        this.rotationInitialized = false;
                    }
                }
                float clampedValue = MathHelper.clamp(this.valueForReset, 0.0F, mc.player.isGliding() ? 0.34F : 0.22F);
                if (flag) {
                    clampedValue = Math.min(clampedValue + 0.06F, mc.player.isGliding() ? 0.4F : 0.27F);
                }
                clampedValue += this.cachedScalarForUpdateRotations * 0.5F;
                if (this.sin % 7 == 0) {
                    clampedValue += 0.03F;
                }
                float floatValueCopy = mc.player.isGliding() ? 38.0F : (flag ? 24.0F : 17.0F);
                float floatValue2 = mc.player.isGliding() ? 10.0F : (flag ? 3.8F : 2.3F);
                float clampedValueValue = MathHelper.clamp(MathHelper.wrapDegrees(x - this.x), -floatValueCopy, floatValueCopy);
                float clampedValueResult = MathHelper.clamp(y - this.y, -floatValue2, floatValue2);
                float floatValue3 = this.cachedScalarForUpdateRotations * 0.47F;
                float floatValue4 = this.cachedScalarForUpdateRotations * 0.05F;
                float xValue = this.x + clampedValueValue * clampedValue * (0.84F + floatValue3);
                float yValue = this.y + clampedValueResult * clampedValue * 0.67F + floatValue4;
                float gCDValue = MouseGcdUtil.getGCDValue();
                if (gCDValue > 0.0F) {
                    xValue = this.x + Math.round((xValue - this.x) / gCDValue) * gCDValue;
                    yValue = this.y + Math.round((yValue - this.y) / gCDValue) * gCDValue;
                }
                yValue = MathHelper.clamp(yValue, -89.0F, 89.0F);
                if (this.rotationDelayTicks > 0) {
                    float floatValue5 = (120 - this.rotationDelayTicks) / 120.0F;
                    float floatValue6 = (120 - this.rotationDelayTicks - 1) / 120.0F;
                    float floatValue7 = (float) Math.sin(Math.PI * floatValue5);
                    float floatValue8 = (float) Math.sin(Math.PI * Math.max(0.0F, floatValue6));
                    float floatValue9 = floatValue7 - floatValue8;
                    xValue = MathHelper.wrapDegrees(xValue + this.wrapDegrees * floatValue9);
                    yValue = MathHelper.clamp(yValue + this.valueForAttack * floatValue9, -89.0F, 89.0F);
                    this.rotationDelayTicks--;
                }
                this.x = xValue;
                this.y = yValue;
                Rotation rotation = new Rotation(xValue, yValue);
                float floatValue10 = mc.player.isGliding() && livingEntity.isGliding() ? 360.0F : 45.0F;
                RotationStorage.update(rotation, floatValue10, floatValue10, floatValue10, floatValue10, 0, 1, false);
                this.rotate = new Vec2f(xValue, yValue);
            }
        }
    }
}
