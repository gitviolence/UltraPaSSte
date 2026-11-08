package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.JumpCircle.Circle;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class JumpCircle extends Module {

    public static JumpCircle INSTANCE = new JumpCircle();

    private final FloatSetting size = new FloatSetting("Размер", 1.0F, 0.5F, 2.0F, 0.1F);

    private final FloatSetting speed = new FloatSetting("Скорость", 2.0F, 1.0F, 3.0F, 0.1F);

    private final FloatSetting lifetime = new FloatSetting("Время жизни", 1.0F, 0.5F, 1.5F, 0.1F);

    private final List<Circle> iteratorJumpCircleCircleEntries = new ArrayList<>();

    private final Identifier circleTexture = Identifier.of("toshiba", "featrues/jumpcircle/circle.png");

    private boolean onGround = true;

    public JumpCircle() {
        super("JumpCircle", "Красивый настраевымый круг при прыжке.", ModuleCategory.RENDER);
        this.addSettings(this.size, this.speed, this.lifetime);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            this.onGround = mc.player.isOnGround();
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.iteratorJumpCircleCircleEntries.clear();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            boolean onGround = mc.player.isOnGround();
            if (this.onGround && !onGround) {
                Vec3d position = new Vec3d(mc.player.getX(), Math.floor(mc.player.getY()) + 0.001, mc.player.getZ());
                this.iteratorJumpCircleCircleEntries.add(new Circle(position, System.currentTimeMillis()));
                while (this.iteratorJumpCircleCircleEntries.size() > 8) {
                    this.iteratorJumpCircleCircleEntries.remove(0);
                }
            }
            this.onGround = onGround;
            long currentTimeMillis = System.currentTimeMillis();
            float floatValue = this.calculateFloat();
            var iterator = this.iteratorJumpCircleCircleEntries.iterator();
            while (iterator.hasNext()) {
                Circle circle = iterator.next();
                if (currentTimeMillis - circle.startTimeMs() > (long) floatValue) {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (!this.iteratorJumpCircleCircleEntries.isEmpty()) {
            long currentTimeMillis = System.currentTimeMillis();
            Vec3d pos = event.getCamera().getPos();
            MatrixStack matrices = event.getMatrices();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, this.circleTexture);
            for (Circle circle : this.iteratorJumpCircleCircleEntries) {
                float floatValue = this.calculateStartTimeMs(currentTimeMillis, circle);
                if (!(floatValue >= 1.0F)) {
                    float floatValueValue = this.calculateFloat(floatValue);
                    if (!(floatValueValue <= 0.01F)) {
                        this.renderAlphaColorWithPos(matrices, pos, circle, floatValue, floatValueValue, currentTimeMillis);
                    }
                }
            }
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private float calculateFloat() {
        return 1850.0F / Math.max(0.25F, this.speed.get());
    }

    private float calculateStartTimeMs(long longValue, Circle circle) {
        return (float) (longValue - circle.startTimeMs()) / this.calculateFloat();
    }

    private float calculateFloat(float floatValue) {
        float clampedValue = MathHelper.clamp(floatValue * this.lifetime.get(), 0.0F, 1.0F);
        return 1.0F - clampedValue;
    }

    private void renderAlphaColorWithPos(MatrixStack matrices, Vec3d position, Circle circle, float floatValue, float floatValueValue, long longValue) {
        float floatValueResult = (float) (longValue - circle.startTimeMs()) / 1000.0F;
        float floatValueItem = easeOutCubic(floatValue);
        float floatValueEntry = Math.min(floatValueItem * this.size.get(), this.size.get());
        float floatValueCopy = floatValueResult * 120.0F * this.speed.get();
        floatValueCopy += (float) Math.sin(floatValue * Math.PI * 2.0) * 30.0F;
        float floatValue2 = (float) Math.sin(floatValueResult * 7.0F * this.speed.get());
        float floatValue3 = 1.0F + floatValue2 * 0.06F;
        float clampedValue = MathHelper.clamp(floatValueValue * (1.0F + floatValue2 * 0.12F), 0.0F, 1.0F);
        float clampedValueValue = MathHelper.clamp(clampedValue * 1.25F, 0.0F, 1.0F);
        float floatValue4 = floatValueEntry * floatValue3;
        int jumpCircleColor = this.getJumpCircleColor();
        int jumpCircleGlowColor = this.getJumpCircleGlowColor();
        int intValue = ColorMathUtil.setAlphaColor(jumpCircleColor, (int) (255.0F * clampedValueValue));
        int intValueValue = ColorMathUtil.setAlphaColor(jumpCircleGlowColor, (int) (255.0F * clampedValueValue));
        int intValueResult = ColorMathUtil.setAlphaColor(ColorMathUtil.darken(jumpCircleColor, 0.65F), (int) (255.0F * MathHelper.clamp(clampedValueValue * 0.9F, 0.0F, 1.0F)));
        int intValueItem = ColorMathUtil.setAlphaColor(ColorMathUtil.darken(jumpCircleGlowColor, 0.65F), (int) (255.0F * MathHelper.clamp(clampedValueValue * 0.9F, 0.0F, 1.0F)));
        matrices.push();
        matrices.translate(circle.pos().x - position.x, circle.pos().y - position.y, circle.pos().z - position.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(floatValueCopy));
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float floatValue5 = floatValue4 * 0.5F;
        float floatValue6 = floatValue4 * 1.08F;
        float floatValue7 = floatValue6 * 0.5F;
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        this.renderColorWithTexture(buffer, positionMatrix, -floatValue5, -floatValue5, floatValue5, floatValue5, intValue, intValueValue);
        this.renderColorWithTexture(buffer, positionMatrix, -floatValue7, -floatValue7, floatValue7, floatValue7, intValueResult, intValueItem);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        matrices.pop();
    }

    private void renderColorWithTexture(BufferBuilder buffer, Matrix4f matrix, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue) {
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        int intValueCopy = intValue >> 24 & 0xFF;
        int intValue2 = intValueValue >> 16 & 0xFF;
        int intValue3 = intValueValue >> 8 & 0xFF;
        int intValue4 = intValueValue & 0xFF;
        int intValue5 = intValueValue >> 24 & 0xFF;
        buffer.vertex(matrix, floatValue, floatValueValue, 0.0F).texture(0.0F, 1.0F).color(intValueResult, intValueItem, intValueEntry, intValueCopy);
        buffer.vertex(matrix, floatValue, floatValueItem, 0.0F).texture(0.0F, 0.0F).color(intValue2, intValue3, intValue4, intValue5);
        buffer.vertex(matrix, floatValueResult, floatValueItem, 0.0F).texture(1.0F, 0.0F).color(intValue2, intValue3, intValue4, intValue5);
        buffer.vertex(matrix, floatValueResult, floatValueValue, 0.0F).texture(1.0F, 1.0F).color(intValueResult, intValueItem, intValueEntry, intValueCopy);
    }

    private int getJumpCircleColor() {
        return Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color[0];
    }

    private int getJumpCircleGlowColor() {
        return Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color[0];
    }

    private static float easeOutCubic(float floatValue) {
        float floatValueValue = 1.0F - floatValue;
        return 1.0F - floatValueValue * floatValueValue * floatValueValue;
    }

    public static record Circle(Vec3d pos, long startTimeMs) {
    }
}
