package brova.toshiba.client.utils.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class GlowRenderer {

    private static GlowRenderer instance;

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private Framebuffer framebuffer;

    private int previousPrimaryColor;

    private int previousSecondaryColor;

    private float glowRadius = 10.0F;

    private float glowAlpha = 1.0F;

    private Color white = Color.WHITE;

    private Matrix4f matrix4f;

    private int glBindFramebuffer;

    public static GlowRenderer getInstance() {
        if (instance == null) {
            instance = new GlowRenderer();
        }
        return instance;
    }

    private void updateFramebufferWidthWithFramebufferHeight() {
        int previousPrimaryColor = minecraftClient.getWindow().getFramebufferWidth();
        int previousSecondaryColor = minecraftClient.getWindow().getFramebufferHeight();
        if (this.framebuffer == null || this.previousPrimaryColor != previousPrimaryColor || this.previousSecondaryColor != previousSecondaryColor) {
            if (this.framebuffer != null) {
                this.framebuffer.delete();
            }
            this.framebuffer = new SimpleFramebuffer(previousPrimaryColor, previousSecondaryColor, false);
            this.previousPrimaryColor = previousPrimaryColor;
            this.previousSecondaryColor = previousSecondaryColor;
        }
    }

    public void begin(float floatValue, Color color) {
        this.begin(floatValue, 1.0F, color);
    }

    public void begin(float glowRadius, float glowAlpha, Color white) {
        this.updateFramebufferWidthWithFramebufferHeight();
        this.glowRadius = glowRadius;
        this.glowAlpha = glowAlpha;
        this.white = white;
        this.matrix4f = new Matrix4f(RenderSystem.getProjectionMatrix());
        this.glBindFramebuffer = GL11.glGetInteger(36006);
        GL30.glBindFramebuffer(36160, this.framebuffer.fbo);
        GL11.glViewport(0, 0, this.previousPrimaryColor, this.previousSecondaryColor);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(16384);
        RenderSystem.setProjectionMatrix(this.matrix4f, ProjectionType.ORTHOGRAPHIC);
    }

    public void end(MatrixStack matrices, RenderAction renderAction) {
        GL30.glBindFramebuffer(36160, this.glBindFramebuffer);
        GL11.glViewport(0, 0, minecraftClient.getWindow().getFramebufferWidth(), minecraftClient.getWindow().getFramebufferHeight());
        RenderSystem.setProjectionMatrix(this.matrix4f, ProjectionType.ORTHOGRAPHIC);
        this.renderColorWithTexture(matrices);
        if (renderAction != null) {
            renderAction.render();
        }
    }

    private float calculateExp(float floatValue, float floatValueValue) {
        return (float) Math.exp(-(floatValue * floatValue) / (2.0F * floatValueValue * floatValueValue));
    }

    private void renderColorWithTexture(MatrixStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.disableDepthTest();
        int scaledWidth = minecraftClient.getWindow().getScaledWidth();
        int scaledHeight = minecraftClient.getWindow().getScaledHeight();
        RenderSystem.setShaderTexture(0, this.framebuffer.getColorAttachment());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float floatValue = this.white.getRed() / 255.0F;
        float floatValueValue = this.white.getGreen() / 255.0F;
        float floatValueResult = this.white.getBlue() / 255.0F;
        float floatValueItem = this.white.getAlpha() / 255.0F * this.glowAlpha;
        float floatValueEntry = this.glowRadius * 0.4F;
        float[] values = new float[6];
        float floatValueCopy = 0.0F;
        for (int index = 0; index < 6; index++) {
            float floatValue2 = this.glowRadius * (index + 1) / 6.0F;
            values[index] = this.calculateExp(floatValue2, floatValueEntry);
            floatValueCopy += values[index];
        }
        for (int indexValue = 0; indexValue < 6; indexValue++) {
            values[indexValue] /= floatValueCopy;
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (int indexResult = 0; indexResult < 6; indexResult++) {
            float floatValue3 = this.glowRadius * (indexResult + 1) / 6.0F;
            float floatValue4 = floatValueItem * values[indexResult] * 0.7F;
            if (!(floatValue4 < 0.001F)) {
                floatValue4 = Math.min(floatValue4, 1.0F);
                for (int indexItem = 0; indexItem < 12; indexItem++) {
                    float floatValue5 = (float) (indexItem * 2.0 * Math.PI) / 12.0F;
                    float floatValue6 = (float) Math.cos(floatValue5) * floatValue3;
                    float floatValue7 = (float) Math.sin(floatValue5) * floatValue3;
                    buffer.vertex(positionMatrix, floatValue6, floatValue7, 0.0F).texture(0.0F, 1.0F).color(floatValue, floatValueValue, floatValueResult, floatValue4);
                    buffer.vertex(positionMatrix, floatValue6, scaledHeight + floatValue7, 0.0F).texture(0.0F, 0.0F).color(floatValue, floatValueValue, floatValueResult, floatValue4);
                    buffer.vertex(positionMatrix, scaledWidth + floatValue6, scaledHeight + floatValue7, 0.0F).texture(1.0F, 0.0F).color(floatValue, floatValueValue, floatValueResult, floatValue4);
                    buffer.vertex(positionMatrix, scaledWidth + floatValue6, floatValue7, 0.0F).texture(1.0F, 1.0F).color(floatValue, floatValueValue, floatValueResult, floatValue4);
                    if (indexResult > 0) {
                        float floatValue8 = (float) ((indexItem + 0.5) * 2.0 * Math.PI) / 12.0F;
                        float floatValue9 = floatValue3 * 0.6F;
                        float floatValue10 = (float) Math.cos(floatValue8) * floatValue9;
                        float floatValue11 = (float) Math.sin(floatValue8) * floatValue9;
                        float floatValue12 = floatValue4 * 0.5F;
                        buffer.vertex(positionMatrix, floatValue10, floatValue11, 0.0F).texture(0.0F, 1.0F).color(floatValue, floatValueValue, floatValueResult, floatValue12);
                        buffer.vertex(positionMatrix, floatValue10, scaledHeight + floatValue11, 0.0F).texture(0.0F, 0.0F).color(floatValue, floatValueValue, floatValueResult, floatValue12);
                        buffer.vertex(positionMatrix, scaledWidth + floatValue10, scaledHeight + floatValue11, 0.0F).texture(1.0F, 0.0F).color(floatValue, floatValueValue, floatValueResult, floatValue12);
                        buffer.vertex(positionMatrix, scaledWidth + floatValue10, floatValue11, 0.0F).texture(1.0F, 1.0F).color(floatValue, floatValueValue, floatValueResult, floatValue12);
                    }
                }
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void startGlow(float floatValue, int intValue, RenderAction renderAction, MatrixStack matrices) {
        startGlow(floatValue, 1.0F, intValue, renderAction, matrices);
    }

    public static void startGlow(float floatValue, float floatValueValue, int intValue, RenderAction renderAction, MatrixStack matrices) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        if (intValueValue == 0) {
            intValueValue = 255;
        }
        GlowRenderer instance = getInstance();
        instance.begin(floatValue, floatValueValue, new Color(intValueResult, intValueItem, intValueEntry, intValueValue));
        renderAction.render();
        instance.end(matrices, renderAction);
    }

    public void cleanup() {
        if (this.framebuffer != null) {
            this.framebuffer.delete();
            this.framebuffer = null;
        }
    }
}
