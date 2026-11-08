package brova.toshiba.client.utils.render;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL30;

public class BlurFramebufferManager implements MinecraftAccess {

    private static BlurFramebufferManager worldInstance;

    private static BlurFramebufferManager hudInstance;

    private final boolean hudMode;

    private Framebuffer buffer1;

    private Framebuffer hudBuffer;

    private int bufferWidth = -1;

    private int bufferHeight = -1;

    private boolean blurRequested = true;

    private Framebuffer[] blurLevels = new Framebuffer[0];

    private final float[] inverseLevelWidths = new float[3];

    private final float[] inverseLevelHeights = new float[3];

    private VertexBuffer fullscreenQuad;

    private int quadWidth = -1;

    private int quadHeight = -1;

    private float blurOffset = 5.0F;

    private ShaderProgram downsampleProgram;

    private GlUniform downsampleResolutionUniform;

    private GlUniform downsampleOffsetUniform;

    private GlUniform downsampleSaturationUniform;

    private GlUniform downsampleTintIntensityUniform;

    private GlUniform downsampleTintColorUniform;

    private ShaderProgram upsampleProgram;

    private GlUniform upsampleResolutionUniform;

    private GlUniform upsampleOffsetUniform;

    private GlUniform upsampleSaturationUniform;

    private GlUniform upsampleTintIntensityUniform;

    private GlUniform upsampleTintColorUniform;

    private BlurFramebufferManager(boolean hudMode) {
        this.hudMode = hudMode;
    }

    public static BlurFramebufferManager getInstance() {
        if (worldInstance == null) {
            worldInstance = new BlurFramebufferManager(false);
        }
        return worldInstance;
    }

    public static BlurFramebufferManager getHudInstance() {
        if (hudInstance == null) {
            hudInstance = new BlurFramebufferManager(true);
        }
        return hudInstance;
    }

    public static Framebuffer getBuffer1() {
        return getInstance().buffer1;
    }

    public static Framebuffer getBuffer2() {
        return getInstance().hudBuffer;
    }

    public static Framebuffer getHudBuffer() {
        return getHudInstance().hudBuffer;
    }

    public void beginFrame() {
        boolean blurRequested = this.blurRequested;
        this.blurRequested = false;
        if (blurRequested && (!this.hudMode || BackgroundSettings.isBlurEnabled())) {
            this.renderGlIsEnabledWithFramebuffer();
        }
    }

    public void request() {
        this.blurRequested = true;
    }

    private void renderGlIsEnabledWithFramebuffer() {
        int intValue = this.hudMode ? 2 : 3;
        int intValueValue = Math.max(1, mc.getWindow().getFramebufferWidth() / intValue);
        int intValueResult = Math.max(1, mc.getWindow().getFramebufferHeight() / intValue);
        if (this.blurLevels.length == 0 || this.bufferWidth != intValueValue || this.bufferHeight != intValueResult) {
            this.updateDeleteWithArraycopy(intValueValue, intValueResult);
        }
        if (this.blurLevels.length != 0) {
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.kawaseDown);
            ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.kawaseUp);
            if (orCreateProgram != null && orCreateProgramValue != null) {
                this.updateUniform(orCreateProgram, orCreateProgramValue);
                this.renderColorWithTexture(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
                int intValueItem = this.calculateInteger();
                float floatValue = this.hudMode ? 2.0F * Math.clamp(BackgroundSettings.BLUR_STRENGTH.get(), 0.0F, 5.0F) : 1.3F;
                boolean flag = GL30.glIsEnabled(2929);
                boolean flagValue = GL30.glIsEnabled(3089);
                boolean flagResult = GL30.glIsEnabled(3042);
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                if (flagValue) {
                    GL30.glDisable(3089);
                }
                BufferRenderer.reset();
                this.fullscreenQuad.bind();
                try {
                    RenderSystem.setShader(orCreateProgram);
                    if (this.downsampleOffsetUniform != null) {
                        this.downsampleOffsetUniform.set(floatValue);
                    }
                    if (this.downsampleSaturationUniform != null) {
                        this.downsampleSaturationUniform.set(1.0F);
                    }
                    if (this.downsampleTintIntensityUniform != null) {
                        this.downsampleTintIntensityUniform.set(0.0F);
                    }
                    if (this.downsampleTintColorUniform != null) {
                        this.downsampleTintColorUniform.set(1.0F, 1.0F, 1.0F);
                    }
                    this.updateBeginWriteWithShaderTexture(orCreateProgram, this.downsampleResolutionUniform, mc.getFramebuffer(), 0);
                    for (int index = 1; index < intValueItem; index++) {
                        this.updateBeginWriteWithShaderTexture(orCreateProgram, this.downsampleResolutionUniform, this.blurLevels[index - 1], index);
                    }
                    if (intValueItem > 1) {
                        RenderSystem.setShader(orCreateProgramValue);
                        if (this.upsampleOffsetUniform != null) {
                            this.upsampleOffsetUniform.set(floatValue);
                        }
                        if (this.upsampleSaturationUniform != null) {
                            this.upsampleSaturationUniform.set(1.0F);
                        }
                        if (this.upsampleTintIntensityUniform != null) {
                            this.upsampleTintIntensityUniform.set(0.0F);
                        }
                        if (this.upsampleTintColorUniform != null) {
                            this.upsampleTintColorUniform.set(1.0F, 1.0F, 1.0F);
                        }
                        for (int indexValue = intValueItem - 1; indexValue > 0; indexValue--) {
                            this.updateBeginWriteWithShaderTexture(orCreateProgramValue, this.upsampleResolutionUniform, this.blurLevels[indexValue], indexValue - 1);
                        }
                    }
                } finally {
                    VertexBuffer.unbind();
                    mc.getFramebuffer().beginWrite(true);
                    RenderSystem.setShaderTexture(0, 0);
                    if (flag) {
                        RenderSystem.enableDepthTest();
                    }
                    if (flagValue) {
                        GL30.glEnable(3089);
                    }
                    if (flagResult) {
                        RenderSystem.enableBlend();
                    }
                }
            } else {
                mc.getFramebuffer().beginWrite(true);
            }
        }
    }

    private void updateUniform(ShaderProgram downsampleProgram, ShaderProgram upsampleProgram) {
        if (this.downsampleProgram != downsampleProgram && downsampleProgram != null) {
            this.downsampleProgram = downsampleProgram;
            this.downsampleResolutionUniform = downsampleProgram.getUniform("Resolution");
            this.downsampleOffsetUniform = downsampleProgram.getUniform("Offset");
            this.downsampleSaturationUniform = downsampleProgram.getUniform("Saturation");
            this.downsampleTintIntensityUniform = downsampleProgram.getUniform("TintIntensity");
            this.downsampleTintColorUniform = downsampleProgram.getUniform("TintColor");
        }
        if (this.upsampleProgram != upsampleProgram && upsampleProgram != null) {
            this.upsampleProgram = upsampleProgram;
            this.upsampleResolutionUniform = upsampleProgram.getUniform("Resolution");
            this.upsampleOffsetUniform = upsampleProgram.getUniform("Offset");
            this.upsampleSaturationUniform = upsampleProgram.getUniform("Saturation");
            this.upsampleTintIntensityUniform = upsampleProgram.getUniform("TintIntensity");
            this.upsampleTintColorUniform = upsampleProgram.getUniform("TintColor");
        }
    }

    private int calculateInteger() {
        if (this.hudMode) {
            return this.blurLevels.length;
        } else {
            int roundedUp = (int) Math.ceil(this.blurOffset * 0.38F);
            return Math.min(this.blurLevels.length, Math.max(2, roundedUp));
        }
    }

    private void updateDeleteWithArraycopy(int bufferWidth, int bufferHeight) {
        for (Framebuffer framebuffer : this.blurLevels) {
            if (framebuffer != null) {
                framebuffer.delete();
            }
        }
        Framebuffer[] framebuffers = new Framebuffer[3];
        int intValue = 0;
        int intValueValue = bufferWidth;
        int intValueResult = bufferHeight;
        for (int index = 0; index < 3 && (this.hudMode || index <= 0 || intValueValue >= 128 && intValueResult >= 128); index++) {
            if (!this.hudMode && index > 0) {
                intValueValue = Math.max(64, intValueValue / 2);
                intValueResult = Math.max(64, intValueResult / 2);
            }
            this.inverseLevelWidths[intValue] = 1.0F / intValueValue;
            this.inverseLevelHeights[intValue] = 1.0F / intValueResult;
            int intValueItem = this.hudMode ? 1 : 2;
            SimpleFramebuffer framebufferValue = new SimpleFramebuffer(Math.max(1, intValueValue / intValueItem), Math.max(1, intValueResult / intValueItem), false);
            this.updateGlTexParameteriWithBindTexture(framebufferValue);
            framebuffers[intValue++] = framebufferValue;
        }
        this.blurLevels = new Framebuffer[intValue];
        System.arraycopy(framebuffers, 0, this.blurLevels, 0, intValue);
        this.buffer1 = intValue > 0 ? this.blurLevels[0] : null;
        this.hudBuffer = this.buffer1;
        this.bufferWidth = bufferWidth;
        this.bufferHeight = bufferHeight;
    }

    private void updateBeginWriteWithShaderTexture(ShaderProgram shaderProgram, GlUniform glUniform, Framebuffer framebuffer, int intValue) {
        this.blurLevels[intValue].beginWrite(true);
        RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
        if (glUniform != null) {
            if (this.hudMode) {
                glUniform.set(1.0F / framebuffer.textureWidth, 1.0F / framebuffer.textureHeight);
            } else {
                glUniform.set(this.inverseLevelWidths[intValue], this.inverseLevelHeights[intValue]);
            }
        }
        this.fullscreenQuad.draw(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), shaderProgram);
    }

    private void updateGlTexParameteriWithBindTexture(Framebuffer framebuffer) {
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        GL30.glTexParameteri(3553, 10241, 9729);
        GL30.glTexParameteri(3553, 10240, 9729);
        GL30.glTexParameteri(3553, 10242, 33071);
        GL30.glTexParameteri(3553, 10243, 33071);
        RenderSystem.bindTexture(0);
    }

    private void renderColorWithTexture(int quadWidth, int quadHeight) {
        if (this.fullscreenQuad == null || this.quadWidth != quadWidth || this.quadHeight != quadHeight) {
            BufferRenderer.reset();
            if (this.fullscreenQuad != null) {
                this.fullscreenQuad.close();
            }
            this.fullscreenQuad = VertexBuffer.createAndUpload(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR, input -> {
                input.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                input.vertex(0.0F, quadHeight, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                input.vertex(quadWidth, quadHeight, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                input.vertex(quadWidth, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            });
            this.quadWidth = quadWidth;
            this.quadHeight = quadHeight;
        }
    }

    public static int getTexture() {
        Framebuffer buffer2 = getBuffer2();
        return buffer2 != null ? buffer2.getColorAttachment() : 0;
    }

    public static int getHudTexture() {
        Framebuffer hudBuffer = getHudBuffer();
        return hudBuffer != null ? hudBuffer.getColorAttachment() : 0;
    }

    public void setBlurOffset(float blurOffset) {
        this.blurOffset = blurOffset;
    }
}
