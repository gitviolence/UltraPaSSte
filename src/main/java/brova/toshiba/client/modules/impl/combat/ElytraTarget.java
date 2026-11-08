package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.movement.SuperFirework;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.time.IntervalTimer;
import brova.toshiba.client.utils.world.EntityPrediction;
import brova.toshiba.client.utils.world.EntityPrediction.EntityMotion;
import brova.toshiba.mixin.FireworkRocketEntityAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ElytraTarget extends Module {

    public static ElytraTarget INSTANCE = new ElytraTarget();

    public final FloatSetting elytraDistance = new FloatSetting("Дистанция", 30.0F, 5.0F, 100.0F, 5.0F);

    public final FloatSetting elytraAimRange = new FloatSetting("Дистанция на элитрах", 50.0F, 10.0F, 100.0F, 0.05F);

    public final FloatSetting forward = new FloatSetting("Перегон", 3.0F, 0.0F, 10.0F, 0.1F);

    public final FloatSetting forwardValue = this.forward;

    private final ModeSetting vanillaMode = new ModeSetting("Режимы", "Резкий/HvH", "Резкий/HvH", "Точный", "Легитный", "Ванильный");

    private final BooleanSetting prediction = new BooleanSetting("Предикт", false);

    private final BooleanSetting visualReverse = new BooleanSetting("Visual Reverse", false);

    private final FloatSetting predictionStrength = new FloatSetting("Сила предикта", 2.7F, 1.0F, 5.0F, 0.1F);

    private static boolean predictCondition = false;

    private static int cachedCount = 0;

    private static boolean stateFlagForResetPredictState = false;

    private static final IntervalTimer intervalTimer = new IntervalTimer();

    private Vec3d zero = Vec3d.ZERO;

    private boolean gliding = false;

    private double predictedDistance = 0.0;

    private boolean stateFlagForReverseActive = false;

    private float value = -10.0F;

    private boolean stateFlagForUpdateVanillaMode = true;

    public ElytraTarget() {
        super("Elytra Target", "Элитра перегон", ModuleCategory.COMBAT);
        this.addSettings(this.vanillaMode, this.prediction, this.visualReverse, this.predictionStrength, this.elytraDistance, this.elytraAimRange, this.forward);
    }

    public boolean isPredictionActive() {
        return mc.player != null && this.isEnable() && mc.player.isGliding();
    }

    public boolean isAuraActive() {
        return mc.player != null && this.isEnable() && mc.player.isGliding();
    }

    public int getForwardTicks() {
        return Math.max(0, Math.round(this.forward.getValue().floatValue()));
    }

    public boolean shouldSyncTargetFlight(LivingEntity entity) {
        if (this.isAuraActive() && entity != null && entity.isAlive() && entity.isGliding()) {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d eyePosValue = entity.getEyePos();
            double doubleValue = Math.hypot(eyePosValue.x + -eyePos.x, eyePosValue.z + -eyePos.z);
            double doubleValueValue = eyePosValue.y + -eyePos.y;
            double clampedValue = MathHelper.clamp(this.forward.getValue().doubleValue() * 1.8, 4.0, 7.5);
            if (doubleValue > clampedValue) {
                return false;
            } else {
                return doubleValueValue > 2.25 && doubleValue > 2.0 ? false : Math.abs(doubleValueValue) <= 4.5;
            }
        } else {
            return false;
        }
    }

    public Vec3d getPredictedPoint(LivingEntity entity, Vec3d position) {
        if (entity == null) {
            return position;
        } else {
            Vec3d positionValue;
            if (this.shouldSyncTargetFlight(entity)) {
                Vec3d velocity = entity.getVelocity();
                int forwardTicks = this.getForwardTicks();
                positionValue = entity.getBoundingBox().getCenter().add(velocity.multiply(forwardTicks));
            } else if (this.hasChasePosition()) {
                Vec3d velocityValue = entity.getVelocity();
                int forwardTicksValue = this.getForwardTicks();
                Vec3d positionResult = entity.getBoundingBox().getCenter().add(velocityValue.multiply(forwardTicksValue));
                positionValue = this.createExpandWithBoundingBox(entity, positionResult, 0.6);
            } else {
                positionValue = EntityPrediction.predict(entity, position, this.getForwardTicks());
            }
            return this.createVanillaModeWithEyePos(positionValue);
        }
    }

    private Vec3d createExpandWithBoundingBox(LivingEntity entity, Vec3d position, double doubleValue) {
        Box box = entity.getBoundingBox().expand(doubleValue);
        double clampedValue = MathHelper.clamp(position.x, box.minX, box.maxX);
        double clampedValueValue = MathHelper.clamp(position.z, box.minZ, box.maxZ);
        double doubleValueValue = box.maxY + -box.minY;
        double doubleValueResult = box.minY + doubleValueValue * 0.75;
        double clampedValueResult = MathHelper.clamp(position.y, doubleValueResult + -0.15, box.maxY + -0.05);
        return new Vec3d(clampedValue, clampedValueResult, clampedValueValue);
    }

    public Vec3d getPredictedCenter(LivingEntity entity) {
        if (entity == null) {
            return null;
        } else {
            Vec3d predictedPoint = this.getPredictedPoint(entity, entity.getBoundingBox().getCenter());
            if (!this.isVanillaMode() && mc.player != null && predictedPoint != null) {
                Vec3d eyePos = mc.player.getEyePos();
                double y = entity.getEyePos().y;
                if (eyePos.y > y + -0.3) {
                    double doubleValue = MathHelper.clamp(eyePos.y + -y, 0.0, 1.2) * 0.35;
                    predictedPoint = new Vec3d(predictedPoint.x, y + -0.25 + -doubleValue, predictedPoint.z);
                }
            }
            return predictedPoint;
        }
    }

    public boolean isVanillaMode() {
        return this.vanillaMode.is("Ванильный");
    }

    private Vec3d createVanillaModeWithEyePos(Vec3d position) {
        if (this.isVanillaMode() && mc.player != null && position != null) {
            Vec3d eyePos = mc.player.getEyePos();
            double doubleValue = Math.hypot(position.x + -eyePos.x, position.z + -eyePos.z);
            if (doubleValue < 1.0E-4) {
                return position;
            } else {
                float floatValue = (float) (-Math.toDegrees(Math.atan2(position.y + -eyePos.y, doubleValue)));
                float clampedValue = MathHelper.clamp(floatValue + this.value, -89.0F, 89.0F);
                double doubleValueValue = eyePos.y + -(Math.tan(Math.toRadians(clampedValue)) * doubleValue);
                return new Vec3d(position.x, doubleValueValue, position.z);
            }
        } else {
            return position;
        }
    }

    public Vec3d getAimPoint(LivingEntity entity) {
        if (mc.player == null || entity == null) {
            return null;
        } else if (!this.hasChasePosition()) {
            return this.getFollowAnchor(entity);
        } else {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d eyePosValue = entity.getEyePos();
            Vec3d zero = this.zero;
            Vec3d position = this.createLengthSquaredWithNormalize(entity, eyePos, eyePosValue);
            if (position.lengthSquared() > 1.0E-4) {
                zero = zero.add(position.normalize().multiply(2.0));
            }
            if (eyePos.y > eyePosValue.y + 0.35) {
                zero = new Vec3d(zero.x, Math.min(zero.y, eyePosValue.y + 0.25), zero.z);
            }
            if (this.isLengthSquaredWithNormalize(eyePos, eyePosValue, position)) {
                double clampedValue = MathHelper.clamp(zero.y, eyePosValue.y + -0.35, eyePosValue.y + 0.25);
                zero = new Vec3d(eyePosValue.x, clampedValue, eyePosValue.z);
            }
            if (!this.isEntitiesWithAlive() && zero.y < eyePos.y && mc.player.getVelocity().y < 0.03) {
                double clampedValueValue = MathHelper.clamp(0.45 + Math.hypot(zero.x + -eyePos.x, zero.z + -eyePos.z) * 0.08, 0.45, 0.95);
                double doubleValue = eyePos.y + -clampedValueValue;
                if (zero.y < doubleValue) {
                    zero = new Vec3d(zero.x, MathHelper.lerp(0.2, zero.y, doubleValue), zero.z);
                }
            }
            return zero;
        }
    }

    public Vec3d getAimVector(LivingEntity entity) {
        if (mc.player == null) {
            return Vec3d.ZERO;
        } else {
            Vec3d aimPoint = this.getAimPoint(entity);
            return aimPoint == null ? Vec3d.ZERO : aimPoint.subtract(mc.player.getEyePos());
        }
    }

    public boolean shouldTarget(LivingEntity entity) {
        if (this.isEnable() && entity != null && mc.player != null) {
            return this.prediction.isState() && mc.player.isGliding() ? entity.isGliding() : false;
        } else {
            return false;
        }
    }

    public boolean isReverseActive() {
        if (this.isEnable() && this.prediction.isState() && this.visualReverse.isState() && mc.player != null) {
            AttackAura aura = ModuleRegistry.aura;
            LivingEntity entity = aura != null && aura.isEnable() ? aura.getTarget() : null;
            if (entity != null && this.shouldTarget(entity)) {
                float floatValue = mc.player.distanceTo(entity);
                float floatValueValue = 2.5F;
                float floatValueResult = Math.max(floatValueValue, this.predictionStrength.getValue().floatValue());
                float floatValueItem = floatValueResult + 3.0F;
                if (!this.stateFlagForReverseActive && floatValue <= floatValueValue) {
                    this.stateFlagForReverseActive = true;
                }
                if (this.stateFlagForReverseActive && floatValue >= floatValueItem) {
                    this.stateFlagForReverseActive = false;
                }
                return this.stateFlagForReverseActive;
            } else {
                this.stateFlagForReverseActive = false;
                return false;
            }
        } else {
            this.stateFlagForReverseActive = false;
            return false;
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (mc.player != null && mc.world != null) {
            AttackAura aura = ModuleRegistry.aura;
            LivingEntity entity = aura != null && aura.isEnable() ? aura.getTarget() : null;
            if (entity != null && entity.isGliding() && this.prediction.isState() && this.shouldTarget(entity)) {
                Vec3d predictedCenter = this.getPredictedCenter(entity);
                if (predictedCenter != null) {
                    Vec3d pos = event.getCamera().getPos();
                    Box box = new Box(predictedCenter.x + -0.35 + -pos.x, predictedCenter.y + -0.35 + -pos.y, predictedCenter.z + -0.35 + -pos.z, predictedCenter.x + 0.35 + -pos.x, predictedCenter.y + 0.35 + -pos.y, predictedCenter.z + 0.35 + -pos.z);
                    int themeColor = ColorMathUtil.getThemeColor();
                    int intValue = ColorMathUtil.setAlphaColor(themeColor, 45);
                    int intValueValue = ColorMathUtil.setAlphaColor(themeColor, 255);
                    this.onWorldRender(event, box, intValue, intValueValue);
                }
            }
        }
    }

    private void onWorldRender(WorldRenderEvent event, Box box, int intValue, int intValueValue) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(2.0F);
        Tessellator instance = Tessellator.getInstance();
        if ((intValue >>> 24 & 0xFF) > 0) {
            this.renderFilledPredictionBox(instance, event, box, intValue);
        }
        this.renderPredictionBoxOutline(instance, event, box, intValueValue);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderFilledPredictionBox(Tessellator tessellator, WorldRenderEvent event, Box box, int intValue) {
        BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        float floatValue = ColorMathUtil.redf(intValue);
        float floatValueValue = ColorMathUtil.greenf(intValue);
        float floatValueResult = ColorMathUtil.bluef(intValue);
        float floatValueItem = ColorMathUtil.alphaf(intValue);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void renderPredictionBoxOutline(Tessellator tessellator, WorldRenderEvent event, Box box, int intValue) {
        BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        float floatValue = ColorMathUtil.redf(intValue);
        float floatValueValue = ColorMathUtil.greenf(intValue);
        float floatValueResult = ColorMathUtil.bluef(intValue);
        float floatValueItem = ColorMathUtil.alphaf(intValue);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(event.getMatrices().peek().getPositionMatrix(), minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static boolean isPredictCondition() {
        return predictCondition;
    }

    public static void resetPredictState() {
        predictCondition = false;
        cachedCount = 0;
        stateFlagForResetPredictState = false;
    }

    public void resetChase() {
        this.gliding = false;
        this.zero = Vec3d.ZERO;
        this.predictedDistance = 0.0;
    }

    public void updateChase(LivingEntity entity, boolean flag) {
        this.resetChase();
        if (this.isAuraActive() && entity != null) {
            this.gliding = flag && entity.isGliding();
            if (this.gliding) {
                double doubleValue = this.forward.getValue().doubleValue();
                double doubleValueValue = doubleValue < 4.0 ? 0.0 : doubleValue + -3.0;
                Vec3d eyePos = mc.player.getEyePos();
                Vec3d eyePosValue = entity.getEyePos();
                Vec3d position = this.createLengthSquaredWithNormalize(entity, eyePos, eyePosValue);
                if (position.lengthSquared() <= 1.0E-4) {
                    this.gliding = false;
                } else {
                    this.zero = eyePosValue.add(position.normalize().multiply(doubleValue));
                    Vec3d positionValue = eyePosValue.add(position.normalize().multiply(doubleValueValue));
                    this.predictedDistance = positionValue.distanceTo(eyePos);
                }
            }
        }
    }

    public boolean hasChasePosition() {
        return this.gliding && this.zero != null && this.zero.lengthSquared() > 1.0E-4;
    }

    public double getPredictedDistance() {
        return this.predictedDistance;
    }

    public Vec3d getFollowAnchor(LivingEntity entity) {
        if (entity == null) {
            return Vec3d.ZERO;
        } else if (this.hasChasePosition()) {
            return this.zero;
        } else {
            Vec3d eyePos = entity.getEyePos();
            Vec3d position = this.createLengthSquaredWithData(entity);
            if (position.lengthSquared() < 1.0E-4) {
                return eyePos;
            } else {
                double clampedValue = MathHelper.clamp(this.forward.getValue().doubleValue() * 0.75, 1.5, 4.0);
                return eyePos.add(position.normalize().multiply(clampedValue));
            }
        }
    }

    public void syncTargetFlightSpeed(LivingEntity entity) {
        if (!this.isVanillaMode() && this.shouldSyncTargetFlight(entity) && this.isEntitiesWithAlive()) {
            Vec3d followAnchor = this.getFollowAnchor(entity);
            if (followAnchor != null && !(followAnchor.lengthSquared() < 1.0E-4)) {
                Vec3d eyePos = mc.player.getEyePos();
                Vec3d velocity = mc.player.getVelocity();
                Vec3d velocityValue = entity.getVelocity();
                Vec3d position = followAnchor.subtract(eyePos);
                Vec3d positionValue = new Vec3d(position.x, 0.0, position.z);
                double y = position.y;
                double doubleValue = Math.hypot(position.x, position.z);
                if (!(doubleValue > MathHelper.clamp(this.forward.getValue().doubleValue() * 1.8, 4.0, 7.0)) && (!(y > 2.0) || !(doubleValue > 2.0)) && !(Math.abs(y) > 4.5)) {
                    if (positionValue.lengthSquared() < 1.0E-4) {
                        positionValue = new Vec3d(velocityValue.x, 0.0, velocityValue.z);
                    }
                    if (positionValue.lengthSquared() < 1.0E-4) {
                        Vec3d positionResult = this.createLengthSquaredWithData(entity);
                        positionValue = new Vec3d(positionResult.x, 0.0, positionResult.z);
                    }
                    if (!(positionValue.lengthSquared() < 1.0E-4)) {
                        positionValue = positionValue.normalize();
                        double doubleValueValue = Math.hypot(velocityValue.x, velocityValue.z);
                        double doubleValueResult = Math.hypot(velocity.x, velocity.z);
                        SuperFirework INSTANCE = SuperFirework.INSTANCE;
                        boolean flag = INSTANCE != null && INSTANCE.isEnable();
                        double clampedValue = MathHelper.clamp((doubleValue + -1.8) * 0.04, 0.0, flag ? 0.28 : 0.18);
                        double doubleValueItem = Math.max(doubleValueValue, 0.05) + clampedValue;
                        if (flag && doubleValueResult > doubleValueItem && doubleValue > 1.5) {
                            doubleValueItem += (doubleValueResult + -doubleValueItem) * 0.2;
                        }
                        double doubleValueEntry = velocityValue.y + MathHelper.clamp(y * 0.045, -0.12, 0.12);
                        double doubleValueCopy = flag ? 0.12 : 0.18;
                        double doubleValue2 = positionValue.x * doubleValueItem;
                        double doubleValue3 = positionValue.z * doubleValueItem;
                        mc.player.setVelocity(velocity.x + (doubleValue2 + -velocity.x) * doubleValueCopy, velocity.y + (doubleValueEntry + -velocity.y) * doubleValueCopy, velocity.z + (doubleValue3 + -velocity.z) * doubleValueCopy);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            AttackAura aura = ModuleRegistry.aura;
            LivingEntity entity = aura != null && aura.isEnable() ? aura.getTarget() : null;
            if (entity != null) {
                EntityPrediction.updateEntity(entity);
            }
            this.updateVanillaMode();
            smartPredict(entity);
            this.updateChase(entity, predictCondition);
            this.syncTargetFlightSpeed(entity);
        } else {
            resetPredictState();
            this.resetChase();
        }
    }

    public static void smartPredict(LivingEntity entity) {
        if (intervalTimer.finished(50L)) {
            if (entity != null) {
                double doubleValue = entity.getX() + -entity.prevX;
                double doubleValueValue = entity.getY() + -entity.prevY;
                double doubleValueResult = entity.getZ() + -entity.prevZ;
                float floatValue = (float) Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult) * 20.0F;
                if (floatValue > 20.0F) {
                    cachedCount = 0;
                    stateFlagForResetPredictState = false;
                    predictCondition = true;
                } else {
                    cachedCount++;
                    if (predictCondition) {
                        stateFlagForResetPredictState = true;
                    }
                    if (cachedCount >= 3) {
                        predictCondition = false;
                        cachedCount = 0;
                        stateFlagForResetPredictState = false;
                    } else {
                        predictCondition = stateFlagForResetPredictState;
                    }
                }
            }
            intervalTimer.reset();
        }
    }

    private boolean isLengthSquaredWithNormalize(Vec3d position, Vec3d positionValue, Vec3d positionResult) {
        double doubleValue = position.y - positionValue.y;
        if (doubleValue < 3.0) {
            return false;
        } else {
            Vec3d positionItem = new Vec3d(positionResult.x, 0.0, positionResult.z);
            Vec3d positionEntry = new Vec3d(position.x - positionValue.x, 0.0, position.z - positionValue.z);
            if (!(positionItem.lengthSquared() <= 1.0E-4) && !(positionEntry.lengthSquared() <= 1.0E-4)) {
                double doubleValueValue = positionItem.normalize().dotProduct(positionEntry.normalize());
                return doubleValueValue < -0.15;
            } else {
                return false;
            }
        }
    }

    private Vec3d createLengthSquaredWithNormalize(LivingEntity entity, Vec3d position, Vec3d positionValue) {
        Vec3d positionResult = this.createLengthSquaredWithData(entity);
        if (positionResult.lengthSquared() <= 1.0E-4) {
            return Vec3d.ZERO;
        } else {
            positionResult = positionResult.normalize();
            if (position.y > positionValue.y + 0.35 && positionResult.y > 0.0) {
                Vec3d positionItem = new Vec3d(positionResult.x, 0.0, positionResult.z);
                if (positionItem.lengthSquared() > 1.0E-4) {
                    return positionItem.normalize();
                }
            }
            return positionResult;
        }
    }

    private Vec3d createLengthSquaredWithData(LivingEntity entity) {
        EntityMotion data = EntityPrediction.getData(entity);
        if (data != null) {
            Vec3d resolvedForward = data.getResolvedForward();
            if (resolvedForward.lengthSquared() > 1.0E-4) {
                return resolvedForward;
            }
        }
        Vec3d velocity = entity.getVelocity();
        Vec3d position = new Vec3d(velocity.x, 0.0, velocity.z);
        return position.lengthSquared() > 1.0E-4 ? velocity : entity.getRotationVector();
    }

    private boolean isEntitiesWithAlive() {
        if (mc.world != null && mc.player != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof FireworkRocketEntity fireworkRocketEntity && fireworkRocketEntity.isAlive()) {
                    LivingEntity entityValue = ((FireworkRocketEntityAccessor) fireworkRocketEntity).elysium$getShooter();
                    if (entityValue == mc.player) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public void onDisable() {
        resetPredictState();
        this.resetChase();
        super.onDisable();
    }

    private void updateVanillaMode() {
        if (!this.isVanillaMode()) {
            this.value = -10.0F;
            this.stateFlagForUpdateVanillaMode = true;
        } else {
            this.value = this.value + (this.stateFlagForUpdateVanillaMode ? 0.5F : -0.5F);
            if (this.value >= 10.0F) {
                this.value = 10.0F;
                this.stateFlagForUpdateVanillaMode = false;
            } else if (this.value <= -10.0F) {
                this.value = -10.0F;
                this.stateFlagForUpdateVanillaMode = true;
            }
        }
    }

    public float getPredictionMultiplier(LivingEntity entity) {
        return entity != null && entity.isGliding() ? this.forward.getValue().floatValue() : 0.0F;
    }
}
