package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.modules.impl.render.TargetESP.TargetAnimation;
import brova.toshiba.client.modules.impl.render.TargetESP.TargetParticle;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class TargetESP extends Module {

    public static TargetESP INSTANCE = new TargetESP();

    private static final float piRadians = (float) Math.PI;

    private static final float fullCircleRadians = (float) (Math.PI * 2);

    private static final Identifier captureTexture = Identifier.of("toshiba", "featrues/targetesp/capture.png");

    private static final Identifier targetBloomTexture = Identifier.of("toshiba", "featrues/targetesp/bloom.png");

    private static final Identifier particleBloomTexture = Identifier.of("toshiba", "featrues/particle/bloom.png");

    private final ModeSetting mode = new ModeSetting("Режим", "Ромб", "Ромб", "Кольцо", "Души", "Молнии");

    private final ColorSetting color = new ColorSetting("Цвет", -10184961);

    private final FloatSetting size = new FloatSetting("Размер", 1.15F, 0.6F, 2.5F, 0.05F);

    private final FloatSetting ringRadius = new FloatSetting("Радиус кольца", 0.5F, 0.3F, 1.5F, 0.05F);

    private final FloatSetting ringSpeed = new FloatSetting("Скорость кольца", 1.0F, 0.3F, 3.0F, 0.1F);

    private final FloatSetting rotationSpeed = new FloatSetting("Скорость вращения", 1.2F, 0.2F, 4.0F, 0.05F);

    private final BooleanSetting redOnHurt = new BooleanSetting("Краснеть при ударе", true);

    private float visibilityAlpha = 0.0F;

    private float markerScale = 0.0F;

    private float ease = 0.0F;

    private float startValue = -280.0F;

    private float targetValue = 280.0F;

    private long lastUpdateTimestampForWorldRender = System.currentTimeMillis();

    private LivingEntity currentTarget = null;

    private LivingEntity previousTarget = null;

    private Vec3d targetPosition = null;

    private float targetHeight = 1.8F;

    private float targetWidth = 0.6F;

    private float soulsVisibilityAlpha = 0.0F;

    private long lastVisibilityUpdateMillis = 0L;

    private final Random random = new Random();

    private final float[] floatValues = new float[3];

    private final TargetAnimation[] targetESPTargetAnimationValues = new TargetAnimation[7];

    private BufferBuilder bufferBuilder;

    private Matrix4f positionMatrix;

    private boolean stateFlag;

    private float billboardRightX;

    private float billboardRightY;

    private float billboardRightZ;

    private float billboardUpX;

    private float billboardUpY;

    private float billboardUpZ;

    public TargetESP() {
        super("TargetESP", "Отображения таргета", ModuleCategory.RENDER);
        for (int index = 0; index < 7; index++) {
            this.targetESPTargetAnimationValues[index] = new TargetAnimation();
        }
        this.size.visible(() -> this.mode.is("Ромб") || this.mode.is("Молнии"));
        this.rotationSpeed.visible(() -> this.mode.is("Ромб") || this.mode.is("Молнии"));
        this.ringRadius.visible(() -> this.mode.is("Кольцо"));
        this.ringSpeed.visible(() -> this.mode.is("Кольцо"));
        this.addSettings(this.mode, this.color, this.size, this.rotationSpeed, this.redOnHurt, this.ringRadius, this.ringSpeed);
    }

    @Override
    public void onDisable() {
        this.visibilityAlpha = 0.0F;
        this.markerScale = 0.0F;
        this.currentTarget = null;
        this.previousTarget = null;
        this.targetPosition = null;
        this.soulsVisibilityAlpha = 0.0F;
        this.lastVisibilityUpdateMillis = 0L;
        this.ease = 0.0F;
        this.startValue = -280.0F;
        this.targetValue = 280.0F;
        for (TargetAnimation targetAnimation : this.targetESPTargetAnimationValues) {
            targetAnimation.creationNanos = 0L;
        }
        super.onDisable();
    }

    private boolean isLocalState() {
        return this.mode.is("Ромб");
    }

    private Identifier getCaptureTexture() {
        return captureTexture;
    }

    private Identifier getBloomTexture() {
        return targetBloomTexture;
    }

    private int calculateColor() {
        return this.color.getColor();
    }

    private float calculateFloat(float floatValue, float floatValueValue, float floatValueResult) {
        if (floatValue < floatValueValue) {
            floatValue = Math.min(floatValue + floatValueResult, floatValueValue);
        } else if (floatValue > floatValueValue) {
            floatValue = Math.max(floatValue - floatValueResult, floatValueValue);
        }
        return floatValue;
    }

    private float calculateSqrt(Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult) {
        double doubleValueItem = doubleValue - position.x;
        double doubleValueEntry = doubleValueValue - position.y;
        double doubleValueCopy = doubleValueResult - position.z;
        double squareRoot = Math.sqrt(doubleValueItem * doubleValueItem + doubleValueEntry * doubleValueEntry + doubleValueCopy * doubleValueCopy);
        return (float) Math.max(0.1, squareRoot * 0.007F);
    }

    private LivingEntity createTargetFromTargetWithAlive() {
        AttackAura aura = ModuleRegistry.aura;
        boolean flag = aura != null && (aura.isEnable() || ModuleRegistry.maceAura != null && ModuleRegistry.maceAura.isEnable());
        LivingEntity entity = flag ? aura.getTarget() : null;
        if (entity != null && entity.isAlive() && !entity.isRemoved()) {
            return entity;
        } else {
            return ModuleRegistry.aimAssist != null ? ModuleRegistry.aimAssist.getEspTarget() : null;
        }
    }

    @EventHandler(priority = -100)
    public void onRender3D(WorldRenderEvent event) {
        if (mc != null && mc.player != null && mc.world != null) {
            LivingEntity currentTarget = this.createTargetFromTargetWithAlive();
            boolean flag = currentTarget != null && currentTarget.isAlive();
            float floatValue = 0.05F;
            this.visibilityAlpha = this.calculateFloat(this.visibilityAlpha, flag ? 1.0F : 0.0F, floatValue);
            this.markerScale = this.calculateFloat(this.markerScale, flag ? 1.0F : 0.5F, floatValue);
            long lastVisibilityUpdateMillis = System.currentTimeMillis();
            if (this.lastVisibilityUpdateMillis == 0L) {
                this.lastVisibilityUpdateMillis = lastVisibilityUpdateMillis;
            }
            if (flag) {
                if (this.previousTarget != currentTarget) {
                    this.soulsVisibilityAlpha = 0.0F;
                    this.lastVisibilityUpdateMillis = lastVisibilityUpdateMillis;
                }
                this.currentTarget = currentTarget;
                this.previousTarget = currentTarget;
            }
            float floatValueValue = Math.max(0.001F, (float) (lastVisibilityUpdateMillis - this.lastVisibilityUpdateMillis) / 400.0F);
            this.lastVisibilityUpdateMillis = lastVisibilityUpdateMillis;
            this.soulsVisibilityAlpha = this.calculateFloat(this.soulsVisibilityAlpha, flag ? 1.0F : 0.0F, floatValueValue);
            if (this.visibilityAlpha <= 0.001F && this.soulsVisibilityAlpha <= 0.001F && !flag) {
                this.currentTarget = null;
                this.previousTarget = null;
                this.targetPosition = null;
                this.lastVisibilityUpdateMillis = 0L;
            } else {
                if (flag && currentTarget != null) {
                    float tickDelta = event.getTickDelta();
                    this.targetPosition = new Vec3d(MathHelper.lerp((double) tickDelta, currentTarget.lastRenderX, currentTarget.getX()), MathHelper.lerp((double) tickDelta, currentTarget.lastRenderY, currentTarget.getY()), MathHelper.lerp((double) tickDelta, currentTarget.lastRenderZ, currentTarget.getZ()));
                    this.targetHeight = currentTarget.getHeight();
                    this.targetWidth = currentTarget.getWidth();
                }
                if (this.targetPosition != null) {
                    if (this.isLocalState()) {
                        this.renderTargetMarker(event);
                    }
                    if (this.mode.is("Души")) {
                        this.renderTargetSouls(event);
                    }
                    if (this.mode.is("Кольцо")) {
                        this.renderTargetRing(event);
                    }
                    if (this.mode.is("Молнии")) {
                        this.renderTargetLightning(event);
                    }
                }
            }
        }
    }

    private void renderTargetRing(WorldRenderEvent event) {
        if (!(this.visibilityAlpha <= 0.001F) && this.targetPosition != null) {
            float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
            LivingEntity currentTarget = this.currentTarget;
            Vec3d position;
            float floatValue;
            if (currentTarget != null && currentTarget.isAlive()) {
                position = new Vec3d(MathHelper.lerp((double) tickDelta, currentTarget.lastRenderX, currentTarget.getX()), MathHelper.lerp((double) tickDelta, currentTarget.lastRenderY, currentTarget.getY()), MathHelper.lerp((double) tickDelta, currentTarget.lastRenderZ, currentTarget.getZ()));
                floatValue = currentTarget.getHeight();
            } else {
                position = this.targetPosition;
                floatValue = this.targetHeight;
            }
            Vec3d pos = mc.gameRenderer.getCamera().getPos();
            double doubleValue = position.x - pos.x;
            double doubleValueValue = position.y - pos.y;
            double doubleValueResult = position.z - pos.z;
            double doubleValueItem = 2000.0 / this.ringSpeed.get();
            double doubleValueEntry = System.currentTimeMillis() % (long) doubleValueItem;
            boolean flag = doubleValueEntry > doubleValueItem / 2.0;
            double doubleValueCopy = doubleValueEntry / (doubleValueItem / 2.0);
            if (flag) {
                doubleValueCopy--;
            } else {
                doubleValueCopy = 1.0 - doubleValueCopy;
            }
            doubleValueCopy = doubleValueCopy < 0.5 ? 2.0 * doubleValueCopy * doubleValueCopy : 1.0 - Math.pow(-2.0 * doubleValueCopy + 2.0, 2.0) / 2.0;
            double doubleValue2 = floatValue / 1.2 * (doubleValueCopy > 0.5 ? 1.0 - doubleValueCopy : doubleValueCopy) * (flag ? -1 : 1);
            int intValue = this.calculateColor();
            float floatValueValue = this.calculateTarget(currentTarget);
            int intValueValue = this.calculateInteger(ColorMathUtil.rgb(255, 3, 3), intValue >>> 24);
            int intValueResult = this.overCol(intValue, intValueValue, floatValueValue);
            int intValueItem = this.calculateInteger(intValueResult, 0.88235295F * this.visibilityAlpha);
            int intValueEntry = this.calculateInteger(intValueResult, 0.003921569F * this.visibilityAlpha);
            int intValueCopy = this.calculateInteger(intValueResult, this.visibilityAlpha);
            double doubleValue3 = this.ringRadius.get();
            MatrixStack matrices = event.getMatrices();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.disableCull();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int index = 0; index <= 360; index++) {
                double doubleValue4 = Math.toRadians(index);
                float floatValueResult = (float) (doubleValue + Math.cos(doubleValue4) * doubleValue3);
                float floatValueItem = (float) (doubleValueResult + Math.sin(doubleValue4) * doubleValue3);
                float floatValueEntry = (float) (doubleValueValue + floatValue * doubleValueCopy);
                float floatValueCopy = (float) (doubleValueValue + floatValue * doubleValueCopy + doubleValue2);
                buffer.vertex(positionMatrix, floatValueResult, floatValueEntry, floatValueItem).color(intValueItem);
                buffer.vertex(positionMatrix, floatValueResult, floatValueCopy, floatValueItem).color(intValueEntry);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.lineWidth(1.5F);
            BufferBuilder bufferValue = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int indexValue = 0; indexValue <= 360; indexValue++) {
                double doubleValue5 = Math.toRadians(indexValue);
                float floatValue2 = (float) (doubleValue + Math.cos(doubleValue5) * doubleValue3);
                float floatValue3 = (float) (doubleValueResult + Math.sin(doubleValue5) * doubleValue3);
                float floatValue4 = (float) (doubleValueValue + floatValue * doubleValueCopy);
                bufferValue.vertex(positionMatrix, floatValue2, floatValue4, floatValue3).color(intValueCopy);
            }
            BufferRenderer.drawWithGlobalProgram(bufferValue.end());
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    private int calculateInteger(int intValue, float floatValue) {
        int intValueValue = (int) ((intValue >> 24 & 0xFF) * floatValue);
        intValueValue = Math.max(0, Math.min(255, intValueValue));
        return intValueValue << 24 | intValue & 16777215;
    }

    private int calculateInteger(int intValue, int intValueValue) {
        intValueValue = Math.max(0, Math.min(255, intValueValue));
        return intValueValue << 24 | intValue & 16777215;
    }

    public int overCol(int intValue, int intValueValue, float floatValue) {
        floatValue = Math.max(0.0F, Math.min(1.0F, floatValue));
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        int intValueCopy = intValue >> 24 & 0xFF;
        int intValue2 = intValueValue >> 16 & 0xFF;
        int intValue3 = intValueValue >> 8 & 0xFF;
        int intValue4 = intValueValue & 0xFF;
        int intValue5 = intValueValue >> 24 & 0xFF;
        int intValue6 = (int) (intValueResult + (intValue2 - intValueResult) * floatValue);
        int intValue7 = (int) (intValueItem + (intValue3 - intValueItem) * floatValue);
        int intValue8 = (int) (intValueEntry + (intValue4 - intValueEntry) * floatValue);
        int intValue9 = (int) (intValueCopy + (intValue5 - intValueCopy) * floatValue);
        return intValue9 << 24 | intValue6 << 16 | intValue7 << 8 | intValue8;
    }

    private float calculateTarget(LivingEntity entity) {
        if (this.redOnHurt.isState() && entity != null) {
            float floatValue = mc != null ? mc.getRenderTickCounter().getTickDelta(true) : 0.0F;
            float clampedValue = MathHelper.clamp(entity.hurtTime - floatValue, 0.0F, 10.0F);
            float floatValueValue = clampedValue / 10.0F;
            return floatValueValue * floatValueValue * (3.0F - 2.0F * floatValueValue);
        } else {
            return 0.0F;
        }
    }

    private void renderDistanceScaledBillboard(MatrixStack matrices, Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, int intValue, float floatValueValue) {
        float floatValueResult = this.calculateSqrt(position, doubleValue, doubleValueValue, doubleValueResult);
        float floatValueItem = floatValue * floatValueResult * 0.5F;
        this.renderColorWithTexture(matrices, position, doubleValue, doubleValueValue, doubleValueResult, floatValueItem, intValue, floatValueValue);
    }

    private void renderFixedSizeBillboard(MatrixStack matrices, Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, int intValue, float floatValueValue) {
        float floatValueResult = floatValue * 0.5F;
        this.renderColorWithTexture(matrices, position, doubleValue, doubleValueValue, doubleValueResult, floatValueResult, intValue, floatValueValue);
    }

    private void renderColorWithTexture(BufferBuilder buffer, MatrixStack matrices, Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, int intValue, float floatValueValue) {
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        int intValueEntry = intValue >> 24 & 0xFF;
        if (intValueEntry > 0) {
            float floatValueResult = floatValue * 0.5F;
            matrices.push();
            matrices.translate(doubleValue - position.x, doubleValueValue - position.y, doubleValueResult - position.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
            if (floatValueValue != 0.0F) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(floatValueValue));
            }
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            buffer.vertex(positionMatrix, -floatValueResult, -floatValueResult, 0.0F).texture(0.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(positionMatrix, -floatValueResult, floatValueResult, 0.0F).texture(0.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(positionMatrix, floatValueResult, floatValueResult, 0.0F).texture(1.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(positionMatrix, floatValueResult, -floatValueResult, 0.0F).texture(1.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            matrices.pop();
        }
    }

    private float easeOutCubic(int intValue) {
        float floatValue = intValue / 17.0F * 0.16F;
        float clampedValue = MathHelper.clamp((this.soulsVisibilityAlpha - floatValue) / (1.0F - floatValue), 0.0F, 1.0F);
        float floatValueValue = 1.0F - clampedValue;
        return 1.0F - floatValueValue * floatValueValue * floatValueValue;
    }

    private Vec3d createSinWithToRadians(double doubleValue, double doubleValueValue, double doubleValueResult, int intValue, int intValueValue, long longValue) {
        double doubleValueItem = Math.toRadians(intValue * 120.0 + intValueValue * 27.0);
        double doubleValueEntry = longValue / 430.0;
        double doubleValueCopy = 1.05F * (0.78 + intValueValue % 5 * 0.07);
        double doubleValue2 = doubleValueEntry + doubleValueItem;
        double doubleValue3 = Math.sin(doubleValueEntry * 0.82 + doubleValueItem * 1.6) * 1.2F;
        return new Vec3d(doubleValue + Math.cos(doubleValue2) * doubleValueCopy, doubleValueValue + doubleValue3, doubleValueResult + Math.sin(doubleValue2) * doubleValueCopy);
    }

    private Vec3d createDouble(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry, double doubleValueCopy, int intValue, int intValueValue, float floatValue, long longValue) {
        Vec3d position = this.createSinWithToRadians(doubleValue, doubleValueValue, doubleValueResult, intValue, intValueValue, longValue);
        return new Vec3d(position.x + (doubleValueItem - position.x) * floatValue, position.y + (doubleValueEntry - position.y) * floatValue, position.z + (doubleValueCopy - position.z) * floatValue);
    }

    private void renderColorWithTexture(MatrixStack matrices, Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, int intValue, float floatValueValue) {
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        int intValueEntry = intValue >> 24 & 0xFF;
        if (intValueEntry > 0) {
            matrices.push();
            matrices.translate(doubleValue - position.x, doubleValueValue - position.y, doubleValueResult - position.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
            if (floatValueValue != 0.0F) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(floatValueValue));
            }
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(positionMatrix, -floatValue, -floatValue, 0.0F).texture(0.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(positionMatrix, -floatValue, floatValue, 0.0F).texture(0.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(positionMatrix, floatValue, floatValue, 0.0F).texture(1.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(positionMatrix, floatValue, -floatValue, 0.0F).texture(1.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            matrices.pop();
        }
    }

    private void renderTargetMarker(WorldRenderEvent event) {
        if (this.targetPosition != null && !(this.visibilityAlpha <= 0.001F)) {
            Vec3d pos = mc.gameRenderer.getCamera().getPos();
            double x = this.targetPosition.x;
            double doubleValue = this.targetPosition.y + (this.targetHeight + 0.4F) * 0.5F;
            double z = this.targetPosition.z;
            float floatValue = this.size.getValue().floatValue() * 12.0F;
            float floatValueValue = floatValue * this.markerScale;
            long lastUpdateTimestampForWorldRender = System.currentTimeMillis();
            float floatValueResult = Math.max(0.001F, (float) (lastUpdateTimestampForWorldRender - this.lastUpdateTimestampForWorldRender) / 1000.0F);
            this.lastUpdateTimestampForWorldRender = lastUpdateTimestampForWorldRender;
            float floatValueItem = Math.max(0.35F, 2.2F / this.rotationSpeed.getValue().floatValue());
            for (this.ease += floatValueResult / floatValueItem; this.ease >= 1.0F; this.targetValue = this.targetValue > 0.0F ? -280.0F : 280.0F) {
                this.ease--;
                this.startValue = this.targetValue;
            }
            float floatValueEntry = (float) PowerEasing.SINE_IN_OUT.ease(this.ease);
            float interpolatedValue = MathHelper.lerp(floatValueEntry, this.startValue, this.targetValue);
            float floatValueCopy = this.calculateTarget(this.currentTarget);
            int intValue = this.calculateInteger(this.calculateColor(), this.visibilityAlpha);
            int intValueValue = this.calculateInteger(ColorMathUtil.rgb(255, 3, 3), intValue >>> 24);
            int intValueResult = this.overCol(intValue, intValueValue, floatValueCopy);
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, this.getCaptureTexture());
            this.renderDistanceScaledBillboard(event.getMatrices(), pos, x, doubleValue, z, floatValueValue, intValueResult, interpolatedValue);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void renderTargetSouls(WorldRenderEvent event) {
        if (!(this.soulsVisibilityAlpha <= 0.001F) && this.targetPosition != null) {
            float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
            LivingEntity currentTarget = this.currentTarget;
            Vec3d position;
            float floatValue;
            if (currentTarget != null && currentTarget.isAlive()) {
                position = new Vec3d(MathHelper.lerp((double) tickDelta, currentTarget.lastRenderX, currentTarget.getX()), MathHelper.lerp((double) tickDelta, currentTarget.lastRenderY, currentTarget.getY()), MathHelper.lerp((double) tickDelta, currentTarget.lastRenderZ, currentTarget.getZ()));
                floatValue = currentTarget.getHeight();
            } else {
                position = this.targetPosition;
                floatValue = this.targetHeight;
            }
            Vec3d pos = mc.gameRenderer.getCamera().getPos();
            double x = position.x;
            double doubleValue = position.y + floatValue / 2.0F;
            double z = position.z;
            double doubleValueValue = 0.7;
            float floatValueValue = 3.4F;
            long currentTimeMillis = System.currentTimeMillis();
            float floatValueResult = this.calculateTarget(currentTarget);
            int intValue = this.calculateColor();
            int intValueValue = this.calculateInteger(ColorMathUtil.rgb(255, 3, 3), intValue >>> 24);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, this.getBloomTexture());
            MatrixStack matrices = event.getMatrices();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (int index = 0; index < 17; index++) {
                float floatValueItem = 1.0F - index / 17.0F * 0.7F;
                double doubleValueResult = 0.15 * (currentTimeMillis - index * 10.0) / 25.0;
                double doubleValueItem = Math.sin(doubleValueResult) * doubleValueValue;
                double doubleValueEntry = Math.cos(doubleValueResult) * doubleValueValue;
                float floatValueEntry = this.easeOutCubic(index);
                double doubleValueCopy = x + doubleValueItem;
                double doubleValue2 = doubleValue + doubleValueEntry;
                double doubleValue3 = z - doubleValueEntry;
                Vec3d positionValue = this.createDouble(x, doubleValue, z, doubleValueCopy, doubleValue2, doubleValue3, 0, index, floatValueEntry, currentTimeMillis);
                double xValue = positionValue.x;
                double y = positionValue.y;
                double zValue = positionValue.z;
                float floatValueCopy = floatValueValue * floatValueItem * MathHelper.lerp(floatValueEntry, 1.45F, 1.0F);
                float floatValue2 = this.soulsVisibilityAlpha * 0.6F;
                int intValueResult = this.calculateInteger(intValue, floatValue2 * this.soulsVisibilityAlpha);
                int intValueItem = this.calculateInteger(intValueValue, floatValue2 * this.soulsVisibilityAlpha);
                int intValueEntry = this.overCol(intValueResult, intValueItem, floatValueResult);
                this.renderColorWithTexture(buffer, matrices, pos, xValue, y, zValue, floatValueCopy * 0.12F, intValueEntry, 0.0F);
                int intValueCopy = this.calculateInteger(intValueEntry, 0.45F);
                this.renderColorWithTexture(buffer, matrices, pos, xValue, y, zValue, floatValueCopy * 0.21F, intValueCopy, 0.0F);
            }
            for (int indexValue = 0; indexValue < 17; indexValue++) {
                float floatValue3 = 1.0F - indexValue / 17.0F * 0.7F;
                double doubleValue4 = 0.15 * (currentTimeMillis - indexValue * 10.0) / 25.0;
                double doubleValue5 = Math.sin(doubleValue4) * doubleValueValue;
                double doubleValue6 = Math.cos(doubleValue4) * doubleValueValue;
                float floatValue4 = this.easeOutCubic(indexValue);
                double doubleValue7 = x - doubleValue5;
                double doubleValue8 = doubleValue + doubleValue5;
                double doubleValue9 = z - doubleValue6;
                Vec3d positionResult = this.createDouble(x, doubleValue, z, doubleValue7, doubleValue8, doubleValue9, 1, indexValue, floatValue4, currentTimeMillis);
                double xResult = positionResult.x;
                double yValue = positionResult.y;
                double zResult = positionResult.z;
                float floatValue5 = floatValueValue * floatValue3 * MathHelper.lerp(floatValue4, 1.45F, 1.0F);
                float floatValue6 = this.soulsVisibilityAlpha * 0.6F;
                int intValue2 = this.calculateInteger(intValue, floatValue6 * this.soulsVisibilityAlpha);
                int intValue3 = this.calculateInteger(this.calculateInteger(ColorMathUtil.rgb(235, 7, 7), intValue >>> 24), floatValue6 * this.soulsVisibilityAlpha);
                int intValue4 = this.overCol(intValue2, intValue3, floatValueResult);
                this.renderColorWithTexture(buffer, matrices, pos, xResult, yValue, zResult, floatValue5 * 0.12F, intValue4, 0.0F);
                int intValue5 = this.calculateInteger(intValue4, 0.45F);
                this.renderColorWithTexture(buffer, matrices, pos, xResult, yValue, zResult, floatValue5 * 0.21F, intValue5, 0.0F);
            }
            for (int indexResult = 0; indexResult < 17; indexResult++) {
                float floatValue7 = 1.0F - indexResult / 17.0F * 0.7F;
                double doubleValue10 = 0.15 * (currentTimeMillis - indexResult * 10.0) / 25.0;
                double doubleValue11 = Math.sin(doubleValue10) * doubleValueValue;
                double doubleValue12 = Math.cos(doubleValue10) * doubleValueValue;
                float floatValue8 = this.easeOutCubic(indexResult);
                double doubleValue13 = x - doubleValue11;
                double doubleValue14 = doubleValue - doubleValue11;
                double doubleValue15 = z + doubleValue12;
                Vec3d positionItem = this.createDouble(x, doubleValue, z, doubleValue13, doubleValue14, doubleValue15, 2, indexResult, floatValue8, currentTimeMillis);
                double xItem = positionItem.x;
                double yResult = positionItem.y;
                double zItem = positionItem.z;
                float floatValue9 = floatValueValue * floatValue7 * MathHelper.lerp(floatValue8, 1.45F, 1.0F);
                float floatValue10 = this.soulsVisibilityAlpha * 0.6F;
                int intValue6 = this.calculateInteger(intValue, floatValue10 * this.soulsVisibilityAlpha);
                int intValue7 = this.calculateInteger(intValueValue, floatValue10 * this.soulsVisibilityAlpha);
                int intValue8 = this.overCol(intValue6, intValue7, floatValueResult);
                this.renderColorWithTexture(buffer, matrices, pos, xItem, yResult, zItem, floatValue9 * 0.12F, intValue8, 0.0F);
                int intValue9 = this.calculateInteger(intValue8, 0.45F);
                this.renderColorWithTexture(buffer, matrices, pos, xItem, yResult, zItem, floatValue9 * 0.21F, intValue9, 0.0F);
            }
            BuiltBuffer bufferValue = buffer.endNullable();
            if (bufferValue != null) {
                BufferRenderer.drawWithGlobalProgram(bufferValue);
            }
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }
    }

    private Vec3d createPositionFromLerpWithAlive(float floatValue) {
        LivingEntity currentTarget = this.currentTarget;
        if (currentTarget != null && currentTarget.isAlive()) {
            this.targetHeight = currentTarget.getHeight();
            this.targetWidth = currentTarget.getWidth();
            return new Vec3d(MathHelper.lerp((double) floatValue, currentTarget.lastRenderX, currentTarget.getX()), MathHelper.lerp((double) floatValue, currentTarget.lastRenderY, currentTarget.getY()), MathHelper.lerp((double) floatValue, currentTarget.lastRenderZ, currentTarget.getZ()));
        } else {
            return this.targetPosition;
        }
    }

    private int calculateInteger(int intValue) {
        return this.calculateInteger(interpolateRgb(intValue, -1, 0.18F), intValue >>> 24);
    }

    private int calculateOverColWithRgb(int intValue, float floatValue) {
        return floatValue <= 0.001F ? intValue : this.overCol(intValue, this.calculateInteger(ColorMathUtil.rgb(255, 25, 25), intValue >>> 24), floatValue);
    }

    private static int interpolateRgb(int intValue, int intValueValue, float floatValue) {
        int intValueResult = (int) ((intValue >> 16 & 0xFF) + ((intValueValue >> 16 & 0xFF) - (intValue >> 16 & 0xFF)) * floatValue);
        int intValueItem = (int) ((intValue >> 8 & 0xFF) + ((intValueValue >> 8 & 0xFF) - (intValue >> 8 & 0xFF)) * floatValue);
        int intValueEntry = (int) ((intValue & 0xFF) + ((intValueValue & 0xFF) - (intValue & 0xFF)) * floatValue);
        return 0xFF000000 | intValueResult << 16 | intValueItem << 8 | intValueEntry;
    }

    private static float clampUnit(float floatValue) {
        return floatValue < 0.0F ? 0.0F : Math.min(floatValue, 1.0F);
    }

    private static float inverseVectorLength(float floatValue, float floatValueValue, float floatValueResult) {
        float squareRoot = (float) Math.sqrt(floatValue * floatValue + floatValueValue * floatValueValue + floatValueResult * floatValueResult);
        return squareRoot < 1.0E-6F ? 0.0F : 1.0F / squareRoot;
    }

    private float randomFloatBetween(float floatValue, float floatValueValue) {
        return floatValue + this.random.nextFloat() * (floatValueValue - floatValue);
    }

    private void updateNextFloatWithSqrt() {
        float floatValue = this.randomFloatBetween(-1.0F, 1.0F);
        float floatValueValue = this.random.nextFloat() * (float) (Math.PI * 2);
        float squareRoot = (float) Math.sqrt(1.0F - floatValue * floatValue);
        this.floatValues[0] = squareRoot * MathHelper.cos(floatValueValue);
        this.floatValues[1] = squareRoot * MathHelper.sin(floatValueValue);
        this.floatValues[2] = floatValue;
    }

    private void beginBillboardBatch(WorldRenderEvent event) {
        Camera camera = event.getCamera();
        Vector3f horizontalPlane = camera.getHorizontalPlane();
        Vector3f verticalPlane = camera.getVerticalPlane();
        this.billboardRightX = horizontalPlane.y * verticalPlane.z - horizontalPlane.z * verticalPlane.y;
        this.billboardRightY = horizontalPlane.z * verticalPlane.x - horizontalPlane.x * verticalPlane.z;
        this.billboardRightZ = horizontalPlane.x * verticalPlane.y - horizontalPlane.y * verticalPlane.x;
        this.billboardUpX = verticalPlane.x;
        this.billboardUpY = verticalPlane.y;
        this.billboardUpZ = verticalPlane.z;
        this.positionMatrix = event.getMatrices().peek().getPositionMatrix();
        this.bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        this.stateFlag = false;
    }

    private void renderDepthMaskWithEnableDepthTest() {
        BuiltBuffer buffer = this.bufferBuilder != null ? this.bufferBuilder.endNullable() : null;
        this.bufferBuilder = null;
        this.positionMatrix = null;
        if (buffer != null && this.stateFlag) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            AbstractTexture texture = mc.getTextureManager().getTexture(particleBloomTexture);
            if (texture != null) {
                texture.setFilter(true, false);
            }
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, particleBloomTexture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BufferRenderer.drawWithGlobalProgram(buffer);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        } else if (buffer != null) {
            buffer.close();
        }
    }

    private void renderColorWithTexture(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        this.bufferBuilder.vertex(this.positionMatrix, floatValue, floatValueValue, floatValueResult).texture(floatValueItem, floatValueEntry).color(intValue, intValueValue, intValueResult, intValueItem);
    }

    private int calculateFloat(float floatValue) {
        return MathHelper.clamp((int) (floatValue * 255.0F), 0, 255);
    }

    private void updateFloat(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, float floatValueEntry) {
        int intValueValue = this.calculateFloat(floatValueEntry);
        if (intValueValue > 0 && !(floatValueItem <= 5.0E-4F)) {
            int intValueResult = intValue >> 16 & 0xFF;
            int intValueItem = intValue >> 8 & 0xFF;
            int intValueEntry = intValue & 0xFF;
            float floatValueCopy = floatValueItem * 0.5F;
            float floatValue2 = this.billboardRightX * floatValueCopy;
            float floatValue3 = this.billboardRightY * floatValueCopy;
            float floatValue4 = this.billboardRightZ * floatValueCopy;
            float floatValue5 = this.billboardUpX * floatValueCopy;
            float floatValue6 = this.billboardUpY * floatValueCopy;
            float floatValue7 = this.billboardUpZ * floatValueCopy;
            this.renderColorWithTexture(floatValue - floatValue2 - floatValue5, floatValueValue - floatValue3 - floatValue6, floatValueResult - floatValue4 - floatValue7, 0.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.renderColorWithTexture(floatValue - floatValue2 + floatValue5, floatValueValue - floatValue3 + floatValue6, floatValueResult - floatValue4 + floatValue7, 0.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.renderColorWithTexture(floatValue + floatValue2 + floatValue5, floatValueValue + floatValue3 + floatValue6, floatValueResult + floatValue4 + floatValue7, 1.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.renderColorWithTexture(floatValue + floatValue2 - floatValue5, floatValueValue + floatValue3 - floatValue6, floatValueResult + floatValue4 - floatValue7, 1.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.stateFlag = true;
        }
    }

    private void updateSqrt(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, int intValue, float floatValue3) {
        int intValueValue = this.calculateFloat(floatValue3);
        if (intValueValue > 0 && !(floatValue2 <= 1.0E-4F)) {
            float floatValue4 = floatValue - floatValueItem;
            float floatValue5 = floatValueValue - floatValueEntry;
            float floatValue6 = floatValueResult - floatValueCopy;
            float squareRoot = (float) Math.sqrt(floatValue4 * floatValue4 + floatValue5 * floatValue5 + floatValue6 * floatValue6);
            if (squareRoot < 1.0E-4F) {
                this.updateFloat(floatValue, floatValueValue, floatValueResult, floatValue2 * 2.0F, intValue, floatValue3);
            } else {
                float floatValue7 = (floatValue + floatValueItem) * 0.5F;
                float floatValue8 = (floatValueValue + floatValueEntry) * 0.5F;
                float floatValue9 = (floatValueResult + floatValueCopy) * 0.5F;
                float floatValue10 = floatValue5 * floatValue9 - floatValue6 * floatValue8;
                float floatValue11 = floatValue6 * floatValue7 - floatValue4 * floatValue9;
                float floatValue12 = floatValue4 * floatValue8 - floatValue5 * floatValue7;
                float squareRootValue = (float) Math.sqrt(floatValue10 * floatValue10 + floatValue11 * floatValue11 + floatValue12 * floatValue12);
                if (!(squareRootValue < 1.0E-6F)) {
                    float floatValue13 = floatValue2 / squareRootValue;
                    floatValue10 *= floatValue13;
                    floatValue11 *= floatValue13;
                    floatValue12 *= floatValue13;
                    float floatValue14 = floatValue2 / squareRoot;
                    float floatValue15 = floatValue4 * floatValue14;
                    float floatValue16 = floatValue5 * floatValue14;
                    float floatValue17 = floatValue6 * floatValue14;
                    float floatValue18 = floatValue + floatValue15;
                    float floatValue19 = floatValueValue + floatValue16;
                    float floatValue20 = floatValueResult + floatValue17;
                    float floatValue21 = floatValueItem - floatValue15;
                    float floatValue22 = floatValueEntry - floatValue16;
                    float floatValue23 = floatValueCopy - floatValue17;
                    int intValueResult = intValue >> 16 & 0xFF;
                    int intValueItem = intValue >> 8 & 0xFF;
                    int intValueEntry = intValue & 0xFF;
                    this.renderColorWithTexture(floatValue21 - floatValue10, floatValue22 - floatValue11, floatValue23 - floatValue12, 0.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.renderColorWithTexture(floatValue21 + floatValue10, floatValue22 + floatValue11, floatValue23 + floatValue12, 0.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.renderColorWithTexture(floatValue18 + floatValue10, floatValue19 + floatValue11, floatValue20 + floatValue12, 1.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.renderColorWithTexture(floatValue18 - floatValue10, floatValue19 - floatValue11, floatValue20 - floatValue12, 1.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.stateFlag = true;
                }
            }
        }
    }

    private void updateSqrtWithLerp(float[] values, int intValue, int intValueValue, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValueResult, float floatValueCopy) {
        int intValueItem = this.calculateFloat(floatValueCopy);
        if (intValueItem > 0 && intValue >= 2) {
            int intValueEntry = intValueResult >> 16 & 0xFF;
            int intValueCopy = intValueResult >> 8 & 0xFF;
            int intValue2 = intValueResult & 0xFF;
            float floatValue2 = 0.0F;
            float floatValue3 = 0.0F;
            float floatValue4 = 0.0F;
            float floatValue5 = 0.0F;
            float floatValue6 = 0.0F;
            float floatValue7 = 0.0F;
            for (int index = 0; index < intValue; index++) {
                int intValue3 = index * 3;
                float floatValue8 = floatValue + values[intValue3];
                float floatValue9 = floatValueValue + values[intValue3 + 1];
                float floatValue10 = floatValueResult + values[intValue3 + 2];
                int intValue4 = Math.max(0, index - 1) * 3;
                int intValue5 = Math.min(intValue - 1, index + 1) * 3;
                float floatValue11 = values[intValue5] - values[intValue4];
                float floatValue12 = values[intValue5 + 1] - values[intValue4 + 1];
                float floatValue13 = values[intValue5 + 2] - values[intValue4 + 2];
                float floatValue14 = floatValue12 * floatValue10 - floatValue13 * floatValue9;
                float floatValue15 = floatValue13 * floatValue8 - floatValue11 * floatValue10;
                float floatValue16 = floatValue11 * floatValue9 - floatValue12 * floatValue8;
                float squareRoot = (float) Math.sqrt(floatValue14 * floatValue14 + floatValue15 * floatValue15 + floatValue16 * floatValue16);
                if (squareRoot > 1.0E-6F) {
                    float floatValue17 = MathHelper.lerp((float) index / intValueValue, floatValueItem, floatValueEntry) / squareRoot;
                    floatValue14 *= floatValue17;
                    floatValue15 *= floatValue17;
                    floatValue16 *= floatValue17;
                } else {
                    floatValue14 = floatValue5;
                    floatValue15 = floatValue6;
                    floatValue16 = floatValue7;
                }
                if (index > 0) {
                    this.renderColorWithTexture(floatValue2 - floatValue5, floatValue3 - floatValue6, floatValue4 - floatValue7, 0.5F, 0.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                    this.renderColorWithTexture(floatValue2 + floatValue5, floatValue3 + floatValue6, floatValue4 + floatValue7, 0.5F, 1.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                    this.renderColorWithTexture(floatValue8 + floatValue14, floatValue9 + floatValue15, floatValue10 + floatValue16, 0.5F, 1.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                    this.renderColorWithTexture(floatValue8 - floatValue14, floatValue9 - floatValue15, floatValue10 - floatValue16, 0.5F, 0.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                }
                floatValue2 = floatValue8;
                floatValue3 = floatValue9;
                floatValue4 = floatValue10;
                floatValue5 = floatValue14;
                floatValue6 = floatValue15;
                floatValue7 = floatValue16;
            }
            this.stateFlag = true;
        }
    }

    private void updateFloat(float[] values, int intValue, int intValueValue, float floatValue) {
        if (intValueValue - intValue >= 2) {
            int intValueResult = intValue + intValueValue >>> 1;
            int intValueItem = intValue * 3;
            int intValueEntry = intValueValue * 3;
            int intValueCopy = intValueResult * 3;
            float floatValueValue = values[intValueEntry] - values[intValueItem];
            float floatValueResult = values[intValueEntry + 1] - values[intValueItem + 1];
            float floatValueItem = values[intValueEntry + 2] - values[intValueItem + 2];
            float floatValueEntry = this.randomFloatBetween(-1.0F, 1.0F);
            float floatValueCopy = this.randomFloatBetween(-1.0F, 1.0F);
            float floatValue2 = this.randomFloatBetween(-1.0F, 1.0F);
            float floatValue3 = floatValueValue * floatValueValue + floatValueResult * floatValueResult + floatValueItem * floatValueItem;
            if (floatValue3 > 1.0E-8F) {
                float floatValue4 = (floatValueEntry * floatValueValue + floatValueCopy * floatValueResult + floatValue2 * floatValueItem) / floatValue3;
                floatValueEntry -= floatValueValue * floatValue4;
                floatValueCopy -= floatValueResult * floatValue4;
                floatValue2 -= floatValueItem * floatValue4;
            }
            values[intValueCopy] = (values[intValueItem] + values[intValueEntry]) * 0.5F + floatValueEntry * floatValue;
            values[intValueCopy + 1] = (values[intValueItem + 1] + values[intValueEntry + 1]) * 0.5F + floatValueCopy * floatValue;
            values[intValueCopy + 2] = (values[intValueItem + 2] + values[intValueEntry + 2]) * 0.5F + floatValue2 * floatValue;
            this.updateFloat(values, intValue, intValueResult, floatValue * 0.55F);
            this.updateFloat(values, intValueResult, intValueValue, floatValue * 0.55F);
        }
    }

    private void renderTargetLightning(WorldRenderEvent event) {
        if (!(this.visibilityAlpha <= 0.001F) && this.targetPosition != null) {
            float tickDelta = event.getTickDelta();
            Vec3d position = this.createPositionFromLerpWithAlive(tickDelta);
            if (position != null) {
                Vec3d pos = event.getCamera().getPos();
                float floatValue = (float) (position.x - pos.x);
                float floatValueValue = (float) (position.y - pos.y);
                float floatValueResult = (float) (position.z - pos.z);
                float targetHeight = this.targetHeight;
                float floatValueItem = Math.max(0.5F, this.targetWidth);
                float floatValueEntry = this.markerScale * this.size.get();
                float floatValueCopy = this.rotationSpeed.get();
                long currentTimeNanos = System.nanoTime();
                int intValue = this.calculateColor();
                float floatValue2 = this.visibilityAlpha * (intValue >>> 24) / 255.0F;
                int intValueValue = this.calculateInteger(intValue);
                float floatValue3 = this.calculateTarget(this.currentTarget);
                int intValueResult = this.calculateOverColWithRgb(intValue, floatValue3);
                int intValueItem = this.calculateOverColWithRgb(intValueValue, floatValue3);
                int intValueEntry = interpolateRgb(intValueResult, -1, 0.8F);
                int intValueCopy = interpolateRgb(intValueItem, -1, 0.8F);
                this.beginBillboardBatch(event);
                for (int index = 0; index < 7; index++) {
                    TargetAnimation targetAnimation = this.targetESPTargetAnimationValues[index];
                    float floatValue4 = (float) (currentTimeNanos - targetAnimation.creationNanos) * 1.0E-9F;
                    if (targetAnimation.creationNanos == 0L || floatValue4 >= targetAnimation.lifetimeSeconds || floatValue4 < 0.0F) {
                        boolean flag = targetAnimation.creationNanos == 0L;
                        this.updateCosWithSin(targetAnimation, index, currentTimeNanos, targetHeight, floatValueItem, floatValueEntry, floatValueCopy, intValueResult, intValueItem);
                        if (flag) {
                            long longValue = (long) (index / 7.0F * targetAnimation.lifetimeSeconds * 1.0E9F);
                            targetAnimation.creationNanos = currentTimeNanos - longValue;
                            targetAnimation.nextPathRefreshNanos = currentTimeNanos + 45000000L;
                            floatValue4 = (float) (currentTimeNanos - targetAnimation.creationNanos) * 1.0E-9F;
                        } else {
                            floatValue4 = 0.001F;
                        }
                    }
                    if (currentTimeNanos >= targetAnimation.nextPathRefreshNanos) {
                        this.updateTargetESPTargetAnimation(targetAnimation);
                        targetAnimation.flickerAlpha = 0.55F + 0.45F * this.random.nextFloat();
                        targetAnimation.nextPathRefreshNanos = currentTimeNanos + 45000000L;
                    }
                    float floatValue5 = clampUnit(floatValue4 / targetAnimation.lifetimeSeconds);
                    int intValue2 = (index & 1) == 0 ? intValueResult : intValueItem;
                    int intValue3 = (index & 1) == 0 ? intValueEntry : intValueCopy;
                    float floatValue6 = (float) Math.pow(1.0F - floatValue5, 1.3F) * floatValue2;
                    float floatValue7 = Math.max(0.0F, 1.0F - floatValue5 / 0.45F);
                    floatValue7 *= floatValue7 * floatValue2;
                    if (floatValue7 > 0.003F) {
                        this.updateFloat(floatValue + targetAnimation.startX, floatValueValue + targetAnimation.startY, floatValueResult + targetAnimation.startZ, floatValueEntry * 0.85F, intValue2, floatValue7 * 0.48F * targetAnimation.flickerAlpha);
                        this.updateFloat(floatValue + targetAnimation.startX, floatValueValue + targetAnimation.startY, floatValueResult + targetAnimation.startZ, floatValueEntry * 0.28F, intValue3, floatValue7 * 0.9F);
                    }
                    float floatValue8 = clampUnit(floatValue4 / 0.07F);
                    int intValue4 = Math.min(17, Math.max(2, Math.round(floatValue8 * 16.0F) + 1));
                    float floatValue9 = floatValue6 * targetAnimation.flickerAlpha;
                    this.updateSqrtWithLerp(targetAnimation.mainPathVertices, intValue4, 16, floatValue, floatValueValue, floatValueResult, floatValueEntry * 0.085F, floatValueEntry * 0.03F, intValue2, floatValue9 * 0.45F);
                    this.updateSqrtWithLerp(targetAnimation.mainPathVertices, intValue4, 16, floatValue, floatValueValue, floatValueResult, floatValueEntry * 0.022F, floatValueEntry * 0.008F, intValue3, floatValue9);
                    if (intValue4 > targetAnimation.branchDepth + 1) {
                        int intValue5 = Math.min(9, Math.max(2, intValue4 - targetAnimation.branchDepth));
                        this.updateSqrtWithLerp(targetAnimation.jitteredPathVertices, intValue5, 8, floatValue, floatValueValue, floatValueResult, floatValueEntry * 0.05F, floatValueEntry * 0.015F, intValue2, floatValue9 * 0.35F);
                        this.updateSqrtWithLerp(targetAnimation.jitteredPathVertices, intValue5, 8, floatValue, floatValueValue, floatValueResult, floatValueEntry * 0.014F, floatValueEntry * 0.005F, intValue3, floatValue9 * 0.8F);
                    }
                    if (floatValue8 >= 1.0F) {
                        this.updateSqrtWithLerp(targetAnimation.smoothedPathVertices, 9, 8, floatValue, floatValueValue, floatValueResult, floatValueEntry * 0.042F, floatValueEntry * 0.012F, intValueItem, floatValue9 * 0.32F);
                        this.updateSqrtWithLerp(targetAnimation.smoothedPathVertices, 9, 8, floatValue, floatValueValue, floatValueResult, floatValueEntry * 0.012F, floatValueEntry * 0.004F, intValueCopy, floatValue9 * 0.75F);
                        this.updateFloat(floatValue + targetAnimation.endX, floatValueValue + targetAnimation.endY, floatValueResult + targetAnimation.endZ, floatValueEntry * 0.32F, intValue2, floatValue9 * 0.6F);
                    }
                    for (int indexValue = 0; indexValue < 3; indexValue++) {
                        TargetParticle targetParticle = targetAnimation.sparks[indexValue];
                        float floatValue10 = floatValue4 - targetParticle.spawnDelay;
                        if (!(floatValue10 <= 0.0F) && !(floatValue10 >= targetParticle.lifetime)) {
                            float floatValue11 = floatValue10 / targetParticle.lifetime;
                            float floatValue12 = (float) Math.exp(-targetParticle.drag * floatValue10);
                            float floatValue13 = (1.0F - floatValue12) / targetParticle.drag;
                            float floatValue14 = 0.5F * targetParticle.gravity * floatValue10 * floatValue10;
                            float floatValue15 = targetAnimation.startX + targetParticle.velocityX * floatValue13;
                            float floatValue16 = targetAnimation.startY + targetParticle.velocityY * floatValue13 - floatValue14;
                            float floatValue17 = targetAnimation.startZ + targetParticle.velocityZ * floatValue13;
                            float floatValue18 = (float) Math.pow(1.0F - floatValue11, 1.4F) * floatValue2;
                            float floatValue19 = targetParticle.velocityX * floatValue12 * targetParticle.trailDuration;
                            float floatValue20 = (targetParticle.velocityY * floatValue12 - targetParticle.gravity * floatValue10) * targetParticle.trailDuration;
                            float floatValue21 = targetParticle.velocityZ * floatValue12 * targetParticle.trailDuration;
                            float floatValue22 = floatValue15 - targetAnimation.startX;
                            float floatValue23 = floatValue16 - targetAnimation.startY;
                            float floatValue24 = floatValue17 - targetAnimation.startZ;
                            float floatValue25 = floatValue22 * floatValue22 + floatValue23 * floatValue23 + floatValue24 * floatValue24;
                            float floatValue26 = floatValue19 * floatValue19 + floatValue20 * floatValue20 + floatValue21 * floatValue21;
                            if (floatValue26 > floatValue25 && floatValue26 > 1.0E-8F) {
                                float squareRoot = (float) Math.sqrt(floatValue25 / floatValue26);
                                floatValue19 *= squareRoot;
                                floatValue20 *= squareRoot;
                                floatValue21 *= squareRoot;
                            }
                            float floatValue27 = targetParticle.size * (1.0F - 0.4F * floatValue11);
                            float floatValue28 = floatValue + floatValue15;
                            float floatValue29 = floatValueValue + floatValue16;
                            float floatValue30 = floatValueResult + floatValue17;
                            this.updateSqrt(floatValue28, floatValue29, floatValue30, floatValue28 - floatValue19, floatValue29 - floatValue20, floatValue30 - floatValue21, floatValue27 * 2.6F, targetParticle.color, floatValue18 * 0.4F);
                            this.updateSqrt(floatValue28, floatValue29, floatValue30, floatValue28 - floatValue19, floatValue29 - floatValue20, floatValue30 - floatValue21, floatValue27, targetParticle.glowColor, floatValue18);
                        }
                    }
                }
                this.renderDepthMaskWithEnableDepthTest();
            }
        }
    }

    private void updateCosWithSin(TargetAnimation targetAnimation, int intValue, long creationNanos, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValueValue, int intValueResult) {
        targetAnimation.creationNanos = creationNanos;
        targetAnimation.lifetimeSeconds = this.randomFloatBetween(0.28F, 0.44F) / Math.max(0.4F, floatValueItem * 0.85F);
        targetAnimation.nextPathRefreshNanos = creationNanos + 45000000L;
        targetAnimation.branchDepth = 3 + this.random.nextInt(5);
        targetAnimation.segmentCount = 8 + this.random.nextInt(5);
        targetAnimation.flickerAlpha = 0.7F + 0.3F * this.random.nextFloat();
        float floatValueEntry = (floatValueValue * 0.58F + 0.12F) * floatValueResult;
        float floatValueCopy = (float) creationNanos * 1.0E-9F * floatValueItem * 2.2F;
        float floatValue2 = floatValueCopy + intValue * (float) (Math.PI * 2.0 / 7.0) + this.randomFloatBetween(-0.35F, 0.35F);
        int intValueItem = intValue % 3;
        if (intValueItem == 0) {
            float floatValue3 = floatValueEntry * this.randomFloatBetween(0.85F, 1.1F);
            float floatValue4 = floatValueEntry * this.randomFloatBetween(0.85F, 1.15F);
            float floatValue5 = this.random.nextBoolean() ? 1.0F : -1.0F;
            float floatValue6 = floatValue5 * this.randomFloatBetween(1.35F, 2.35F);
            float floatValue7 = floatValue2 + floatValue6;
            float floatValue8 = floatValue2 + floatValue6 * 0.5F;
            float floatValue9 = Math.max(floatValue3, floatValue4) * this.randomFloatBetween(1.12F, 1.35F);
            float startY = floatValue * this.randomFloatBetween(0.12F, 0.9F);
            float endY = MathHelper.clamp(startY + this.randomFloatBetween(-0.55F, 0.55F) * floatValue, floatValue * 0.06F, floatValue * 0.98F);
            float controlY = (startY + endY) * 0.5F + this.randomFloatBetween(-0.18F, 0.18F) * floatValue;
            targetAnimation.startX = MathHelper.cos(floatValue2) * floatValue3;
            targetAnimation.startY = startY;
            targetAnimation.startZ = MathHelper.sin(floatValue2) * floatValue3;
            targetAnimation.controlX = MathHelper.cos(floatValue8) * floatValue9;
            targetAnimation.controlY = controlY;
            targetAnimation.controlZ = MathHelper.sin(floatValue8) * floatValue9;
            targetAnimation.endX = MathHelper.cos(floatValue7) * floatValue4;
            targetAnimation.endY = endY;
            targetAnimation.endZ = MathHelper.sin(floatValue7) * floatValue4;
        } else if (intValueItem == 1) {
            boolean flag = this.random.nextBoolean();
            float startYValue = floatValue * (flag ? this.randomFloatBetween(0.78F, 1.05F) : this.randomFloatBetween(0.05F, 0.3F));
            float endYValue = floatValue * (flag ? this.randomFloatBetween(0.06F, 0.42F) : this.randomFloatBetween(0.68F, 1.02F));
            float floatValue10 = floatValue2 + this.randomFloatBetween(-1.2F, 1.2F);
            float floatValue11 = (floatValue2 + floatValue10) * 0.5F + this.randomFloatBetween(-0.35F, 0.35F);
            float floatValue12 = floatValueEntry * this.randomFloatBetween(0.75F, 1.05F);
            float floatValue13 = floatValueEntry * this.randomFloatBetween(0.85F, 1.2F);
            float floatValue14 = floatValueEntry * this.randomFloatBetween(1.15F, 1.42F);
            targetAnimation.startX = MathHelper.cos(floatValue2) * floatValue12;
            targetAnimation.startY = startYValue;
            targetAnimation.startZ = MathHelper.sin(floatValue2) * floatValue12;
            targetAnimation.controlX = MathHelper.cos(floatValue11) * floatValue14;
            targetAnimation.controlY = (startYValue + endYValue) * 0.5F;
            targetAnimation.controlZ = MathHelper.sin(floatValue11) * floatValue14;
            targetAnimation.endX = MathHelper.cos(floatValue10) * floatValue13;
            targetAnimation.endY = endYValue;
            targetAnimation.endZ = MathHelper.sin(floatValue10) * floatValue13;
        } else {
            float floatValue15 = floatValueEntry * this.randomFloatBetween(0.65F, 0.9F);
            float floatValue16 = floatValueEntry * this.randomFloatBetween(1.35F, 1.85F);
            float floatValue17 = floatValue2 + this.randomFloatBetween(-0.85F, 0.85F);
            float floatValue18 = (floatValue2 + floatValue17) * 0.5F + this.randomFloatBetween(-0.25F, 0.25F);
            float startYResult = floatValue * this.randomFloatBetween(0.22F, 0.88F);
            float endYResult = MathHelper.clamp(startYResult + this.randomFloatBetween(-0.45F, 0.45F) * floatValue, floatValue * 0.05F, floatValue * 1.08F);
            targetAnimation.startX = MathHelper.cos(floatValue2) * floatValue15;
            targetAnimation.startY = startYResult;
            targetAnimation.startZ = MathHelper.sin(floatValue2) * floatValue15;
            targetAnimation.controlX = MathHelper.cos(floatValue18) * ((floatValue15 + floatValue16) * 0.52F);
            targetAnimation.controlY = (startYResult + endYResult) * 0.5F + this.randomFloatBetween(-0.12F, 0.12F) * floatValue;
            targetAnimation.controlZ = MathHelper.sin(floatValue18) * ((floatValue15 + floatValue16) * 0.52F);
            targetAnimation.endX = MathHelper.cos(floatValue17) * floatValue16;
            targetAnimation.endY = endYResult;
            targetAnimation.endZ = MathHelper.sin(floatValue17) * floatValue16;
        }
        float floatValue19 = targetAnimation.controlX - targetAnimation.startX;
        float floatValue20 = targetAnimation.controlY - targetAnimation.startY;
        float floatValue21 = targetAnimation.controlZ - targetAnimation.startZ;
        float floatValue22 = targetAnimation.endX - targetAnimation.controlX;
        float floatValue23 = targetAnimation.endY - targetAnimation.controlY;
        float floatValue24 = targetAnimation.endZ - targetAnimation.controlZ;
        targetAnimation.pathLength = (float) (Math.sqrt(floatValue19 * floatValue19 + floatValue20 * floatValue20 + floatValue21 * floatValue21) + Math.sqrt(floatValue22 * floatValue22 + floatValue23 * floatValue23 + floatValue24 * floatValue24));
        this.updateTargetESPTargetAnimation(targetAnimation);
        int intValueEntry = (intValue & 1) == 0 ? intValueValue : intValueResult;
        for (int index = 0; index < 3; index++) {
            TargetParticle targetParticle = targetAnimation.sparks[index];
            this.updateNextFloatWithSqrt();
            float floatValue25 = floatValueResult * this.randomFloatBetween(3.5F, 6.8F);
            targetParticle.velocityX = this.floatValues[0] * floatValue25;
            targetParticle.velocityY = this.floatValues[1] * floatValue25;
            targetParticle.velocityZ = this.floatValues[2] * floatValue25;
            targetParticle.drag = 8.0F;
            targetParticle.gravity = 3.0F * floatValueResult;
            targetParticle.spawnDelay = this.randomFloatBetween(0.0F, 0.08F);
            targetParticle.lifetime = this.randomFloatBetween(0.14F, 0.26F);
            targetParticle.size = floatValueResult * this.randomFloatBetween(0.016F, 0.025F);
            targetParticle.trailDuration = 0.03F;
            targetParticle.color = interpolateRgb(intValueEntry, -1, this.randomFloatBetween(0.45F, 0.8F));
            targetParticle.glowColor = interpolateRgb(targetParticle.color, -1, 0.7F);
        }
    }

    private void updateTargetESPTargetAnimation(TargetAnimation targetAnimation) {
        float[] mainPathVertices = targetAnimation.mainPathVertices;
        mainPathVertices[0] = targetAnimation.startX;
        mainPathVertices[1] = targetAnimation.startY;
        mainPathVertices[2] = targetAnimation.startZ;
        byte byteValue = 8;
        int intValue = byteValue * 3;
        mainPathVertices[intValue] = targetAnimation.controlX + this.randomFloatBetween(-0.06F, 0.06F) * targetAnimation.pathLength;
        mainPathVertices[intValue + 1] = targetAnimation.controlY + this.randomFloatBetween(-0.06F, 0.06F) * targetAnimation.pathLength;
        mainPathVertices[intValue + 2] = targetAnimation.controlZ + this.randomFloatBetween(-0.06F, 0.06F) * targetAnimation.pathLength;
        byte byteValueValue = 48;
        mainPathVertices[byteValueValue] = targetAnimation.endX;
        mainPathVertices[byteValueValue + 1] = targetAnimation.endY;
        mainPathVertices[byteValueValue + 2] = targetAnimation.endZ;
        this.updateFloat(mainPathVertices, 0, byteValue, targetAnimation.pathLength * 0.16F);
        this.updateFloat(mainPathVertices, byteValue, 16, targetAnimation.pathLength * 0.16F);
        this.updateFloat(mainPathVertices, targetAnimation.branchDepth, targetAnimation.jitteredPathVertices, targetAnimation.pathLength * 0.46F);
        this.updateFloat(mainPathVertices, targetAnimation.segmentCount, targetAnimation.smoothedPathVertices, targetAnimation.pathLength * 0.36F);
    }

    private void updateFloat(float[] values, int intValue, float[] valuesValue, float floatValue) {
        int intValueValue = MathHelper.clamp(intValue, 1, 14) * 3;
        float floatValueValue = values[intValueValue + 3] - values[intValueValue];
        float floatValueResult = values[intValueValue + 4] - values[intValueValue + 1];
        float floatValueItem = values[intValueValue + 5] - values[intValueValue + 2];
        float floatValueEntry = inverseVectorLength(floatValueValue, floatValueResult, floatValueItem);
        this.updateNextFloatWithSqrt();
        floatValueValue = floatValueValue * floatValueEntry + this.floatValues[0] * 0.9F;
        floatValueResult = floatValueResult * floatValueEntry + this.floatValues[1] * 0.9F;
        floatValueItem = floatValueItem * floatValueEntry + this.floatValues[2] * 0.9F;
        floatValueEntry = inverseVectorLength(floatValueValue, floatValueResult, floatValueItem) * floatValue;
        valuesValue[0] = values[intValueValue];
        valuesValue[1] = values[intValueValue + 1];
        valuesValue[2] = values[intValueValue + 2];
        byte byteValue = 24;
        valuesValue[byteValue] = valuesValue[0] + floatValueValue * floatValueEntry;
        valuesValue[byteValue + 1] = valuesValue[1] + floatValueResult * floatValueEntry;
        valuesValue[byteValue + 2] = valuesValue[2] + floatValueItem * floatValueEntry;
        this.updateFloat(valuesValue, 0, 8, floatValue * 0.25F);
    }

    public final static class TargetAnimation {

        public float startX;

        public float startY;

        public float startZ;

        public float controlX;

        public float controlY;

        public float controlZ;

        public float endX;

        public float endY;

        public float endZ;

        public float pathLength;

        public long creationNanos;

        public float lifetimeSeconds = 0.35F;

        public long nextPathRefreshNanos;

        public int branchDepth = 4;

        public int segmentCount = 10;

        public float flickerAlpha = 1.0F;

        public final float[] mainPathVertices = new float[51];

        public final float[] jitteredPathVertices = new float[27];

        public final float[] smoothedPathVertices = new float[27];

        public final TargetParticle[] sparks = new TargetParticle[3];

        public TargetAnimation() {
            for (int index = 0; index < 3; index++) {
                this.sparks[index] = new TargetParticle();
            }
        }
    }

    public final static class TargetParticle {

        public float velocityX;

        public float velocityY;

        public float velocityZ;

        public float drag = 8.0F;

        public float gravity;

        public float spawnDelay;

        public float lifetime = 0.2F;

        public float size;

        public float trailDuration;

        public int color;

        public int glowColor;

        TargetParticle() {
        }
    }
}
