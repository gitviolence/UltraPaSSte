package brova.toshiba.client.utils.render.shader;

import brova.toshiba.client.core.MinecraftAccess;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class WorldPostProcessor implements MinecraftAccess {

    private Framebuffer[] colorAttachmentFramebufferValues = new Framebuffer[0];

    private SimpleFramebuffer simpleFramebuffer;

    private int cachedPrimaryColor = -1;

    private int cachedSecondaryColor = -1;

    private long lastUpdateTimestamp;

    public void apply(MinecraftClient client, float floatValue, float floatValueValue, boolean flag) {
        Framebuffer framebuffer = client.getFramebuffer();
        if (framebuffer != null) {
            int textureWidth = framebuffer.textureWidth;
            int textureHeight = framebuffer.textureHeight;
            if (textureWidth > 0 && textureHeight > 0) {
                int intValue = Math.max(1, textureWidth / 2);
                int intValueValue = Math.max(1, textureHeight / 2);
                if (this.colorAttachmentFramebufferValues.length == 0 || this.cachedPrimaryColor != intValue || this.cachedSecondaryColor != intValueValue) {
                    this.updateArraycopy(intValue, intValueValue, textureWidth, textureHeight);
                }
                if (this.colorAttachmentFramebufferValues.length != 0) {
                    GL30.glBindFramebuffer(36008, framebuffer.fbo);
                    GL30.glBindFramebuffer(36009, this.simpleFramebuffer.fbo);
                    GL30.glBlitFramebuffer(0, 0, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, 256, 9728);
                    GL30.glBindFramebuffer(36160, framebuffer.fbo);
                    Matrix4f matrix = new Matrix4f(RenderSystem.getProjectionMatrix());
                    ProjectionType projectionType = RenderSystem.getProjectionType();
                    Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
                    modelViewStack.pushMatrix();
                    modelViewStack.identity();
                    float scaledWidth = mc.getWindow().getScaledWidth();
                    float scaledHeight = mc.getWindow().getScaledHeight();
                    RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, scaledWidth, scaledHeight, 0.0F, -1000.0F, 1000.0F), ProjectionType.ORTHOGRAPHIC);
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.disableBlend();
                    ShaderProgram orCreateProgram = client.getShaderLoader().getOrCreateProgram(ShaderPrograms.kawaseDown);
                    ShaderProgram orCreateProgramValue = client.getShaderLoader().getOrCreateProgram(ShaderPrograms.kawaseUp);
                    int intValueResult = this.calculateFloat(floatValue);
                    int intValueItem = Math.max(0, Math.min(1, intValueResult - 2));
                    this.renderClearColorWithBeginWrite(ShaderPrograms.kawaseDown, orCreateProgram, framebuffer, this.colorAttachmentFramebufferValues[0], scaledWidth, scaledHeight);
                    for (int index = 1; index <= intValueItem; index++) {
                        this.renderClearColorWithBeginWrite(ShaderPrograms.kawaseDown, orCreateProgram, this.colorAttachmentFramebufferValues[index - 1], this.colorAttachmentFramebufferValues[index], scaledWidth, scaledHeight);
                    }
                    for (int indexValue = 0; indexValue < 7; indexValue++) {
                        for (int indexResult = intValueItem + 1; indexResult < intValueResult; indexResult++) {
                            this.renderClearColorWithBeginWrite(ShaderPrograms.kawaseDown, orCreateProgram, this.colorAttachmentFramebufferValues[indexResult - 1], this.colorAttachmentFramebufferValues[indexResult], scaledWidth, scaledHeight);
                        }
                        for (int indexItem = intValueResult - 1; indexItem > intValueItem; indexItem--) {
                            this.renderClearColorWithBeginWrite(ShaderPrograms.kawaseUp, orCreateProgramValue, this.colorAttachmentFramebufferValues[indexItem], this.colorAttachmentFramebufferValues[indexItem - 1], scaledWidth, scaledHeight);
                        }
                    }
                    for (int indexEntry = intValueItem; indexEntry > 0; indexEntry--) {
                        this.renderClearColorWithBeginWrite(ShaderPrograms.kawaseUp, orCreateProgramValue, this.colorAttachmentFramebufferValues[indexEntry], this.colorAttachmentFramebufferValues[indexEntry - 1], scaledWidth, scaledHeight);
                    }
                    float floatValueResult = 0.05F;
                    float farPlaneDistance = client.gameRenderer.getFarPlaneDistance();
                    float floatValueItem = 0.1F * floatValueValue / 100.0F;
                    float floatValueEntry = 0.28F * floatValueValue / 100.0F;
                    if (floatValueEntry <= floatValueItem) {
                        floatValueEntry = floatValueItem + 0.001F;
                    }
                    framebuffer.beginWrite(true);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShader(ShaderPrograms.fogBlurComposite);
                    RenderSystem.setShaderTexture(0, this.colorAttachmentFramebufferValues[0].getColorAttachment());
                    int depthAttachment = this.simpleFramebuffer.getDepthAttachment();
                    RenderSystem.setShaderTexture(1, depthAttachment);
                    int intValueEntry = GL11.glGetInteger(32873);
                    GL11.glBindTexture(3553, depthAttachment);
                    GL11.glTexParameteri(3553, 34892, 0);
                    GL11.glTexParameteri(3553, 10241, 9728);
                    GL11.glTexParameteri(3553, 10240, 9728);
                    GL11.glBindTexture(3553, intValueEntry);
                    ShaderProgram orCreateProgramResult = client.getShaderLoader().getOrCreateProgram(ShaderPrograms.fogBlurComposite);
                    if (orCreateProgramResult != null) {
                        GlUniform uniform = orCreateProgramResult.getUniform("DepthParams");
                        if (uniform != null) {
                            uniform.set(floatValueResult, farPlaneDistance, floatValueItem, floatValueEntry);
                        }
                    }
                    this.renderColorWithTexture(scaledWidth, scaledHeight);
                    RenderSystem.setShaderTexture(0, 0);
                    RenderSystem.setShaderTexture(1, 0);
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(true);
                    modelViewStack.popMatrix();
                    RenderSystem.setProjectionMatrix(matrix, projectionType);
                    framebuffer.beginWrite(false);
                }
            }
        }
    }

    private int calculateFloat(float floatValue) {
        return Math.max(2, Math.min(this.colorAttachmentFramebufferValues.length, Math.round(floatValue * 0.8F)));
    }

    private void renderClearColorWithBeginWrite(ShaderProgramKey shaderProgramKey, ShaderProgram shaderProgram, Framebuffer framebuffer, Framebuffer framebufferValue, float floatValue, float floatValueValue) {
        framebufferValue.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        framebufferValue.clear();
        framebufferValue.beginWrite(true);
        RenderSystem.setShader(shaderProgramKey);
        framebuffer.beginRead();
        RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
        this.updateUniform(shaderProgram, framebufferValue.textureWidth, framebufferValue.textureHeight);
        this.renderColorWithTexture(floatValue, floatValueValue);
        framebuffer.endRead();
        framebufferValue.endWrite();
    }

    private void updateUniform(ShaderProgram shaderProgram, int intValue, int intValueValue) {
        GlUniform uniform = shaderProgram.getUniform("Resolution");
        GlUniform uniformValue = shaderProgram.getUniform("Offset");
        GlUniform uniformResult = shaderProgram.getUniform("Saturation");
        GlUniform uniformItem = shaderProgram.getUniform("TintIntensity");
        GlUniform uniformEntry = shaderProgram.getUniform("TintColor");
        if (uniform != null) {
            uniform.set(1.0F / intValue, 1.0F / intValueValue);
        }
        if (uniformValue != null) {
            uniformValue.set(1.7F);
        }
        if (uniformResult != null) {
            uniformResult.set(1.0F);
        }
        if (uniformItem != null) {
            uniformItem.set(0.0F);
        }
        if (uniformEntry != null) {
            uniformEntry.set(1.0F, 1.0F, 1.0F);
        }
    }

    private void renderColorWithTexture(float floatValue, float floatValueValue) {
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(0.0F, floatValueValue, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, floatValueValue, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void updateArraycopy(int cachedPrimaryColor, int cachedSecondaryColor, int intValue, int intValueValue) {
        this.updateDelete();
        Framebuffer[] framebuffers = new Framebuffer[5];
        int intValueResult = 0;
        int intValueItem = cachedPrimaryColor;
        int intValueEntry = cachedSecondaryColor;
        for (int index = 0; index < 5 && (index <= 0 || intValueItem >= 64 && intValueEntry >= 64); index++) {
            if (index > 0) {
                intValueItem = Math.max(32, intValueItem / 2);
                intValueEntry = Math.max(32, intValueEntry / 2);
            }
            SimpleFramebuffer framebuffer = new SimpleFramebuffer(intValueItem, intValueEntry, false);
            this.updateGlTexParameteriWithBindTexture(framebuffer);
            framebuffers[intValueResult++] = framebuffer;
        }
        this.colorAttachmentFramebufferValues = new Framebuffer[intValueResult];
        System.arraycopy(framebuffers, 0, this.colorAttachmentFramebufferValues, 0, intValueResult);
        this.simpleFramebuffer = new SimpleFramebuffer(intValue, intValueValue, true);
        this.cachedPrimaryColor = cachedPrimaryColor;
        this.cachedSecondaryColor = cachedSecondaryColor;
    }

    private void updateGlTexParameteriWithBindTexture(Framebuffer framebuffer) {
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        GL30.glTexParameteri(3553, 10241, 9729);
        GL30.glTexParameteri(3553, 10240, 9729);
        GL30.glTexParameteri(3553, 10242, 33071);
        GL30.glTexParameteri(3553, 10243, 33071);
        RenderSystem.bindTexture(0);
    }

    private void updateDelete() {
        for (Framebuffer framebuffer : this.colorAttachmentFramebufferValues) {
            if (framebuffer != null) {
                framebuffer.delete();
            }
        }
        this.colorAttachmentFramebufferValues = new Framebuffer[0];
        if (this.simpleFramebuffer != null) {
            this.simpleFramebuffer.delete();
            this.simpleFramebuffer = null;
        }
        this.cachedPrimaryColor = this.cachedSecondaryColor = -1;
    }

    public void close() {
        this.updateDelete();
    }
}
