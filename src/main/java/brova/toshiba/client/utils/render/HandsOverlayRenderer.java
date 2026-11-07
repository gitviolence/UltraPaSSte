package brova.toshiba.client.utils.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.ShaderHands;
import brova.toshiba.client.utils.render.shader.HandsPostProcessor;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class HandsOverlayRenderer implements MinecraftAccess {

    private static HandsOverlayRenderer instance;

    private final long uniformTimestamp = System.nanoTime();

    private final HandsPostProcessor resetHandsPostProcessor = new HandsPostProcessor();

    private boolean glowEnabledLastFrame;

    private Framebuffer colorAttachmentFramebufferForCaptureBeforeHands;

    private Framebuffer sceneFramebuffer;

    private Framebuffer handMaskFramebuffer;

    private Framebuffer effectFramebuffer;

    private Framebuffer glowFramebuffer;

    private boolean stateFlagForUpdateDeleteWithClearColor = true;

    private final List<Framebuffer> framebufferEntries = new ArrayList<>();

    private int framebufferWidth = -1;

    private int framebufferHeight = -1;

    private boolean frameCaptured;

    private boolean stateFlagForCaptureAfterHands;

    private int effectWidth = -1;

    private int effectHeight = -1;

    public static HandsOverlayRenderer getInstance() {
        if (instance == null) {
            instance = new HandsOverlayRenderer();
        }
        return instance;
    }

    public void captureBeforeHands() {
        ShaderHands shaderHands = this.createShaderHands();
        if (!this.isShouldRenderFog(shaderHands)) {
            this.invalidateState();
        } else {
            boolean glowEnabledLastFrame = shaderHands.mode.is("Огонь");
            if (glowEnabledLastFrame != this.glowEnabledLastFrame) {
                this.resetTrail();
                this.glowEnabledLastFrame = glowEnabledLastFrame;
            }
            this.updateDeleteWithClearColor();
            if (this.colorAttachmentFramebufferForCaptureBeforeHands != null) {
                boolean flag = !shaderHands.mode.is("Туман");
                this.updateGlBindFramebufferWithGlGetInteger(this.colorAttachmentFramebufferForCaptureBeforeHands, flag);
                this.frameCaptured = true;
            }
        }
    }

    public void captureAfterHands() {
        ShaderHands shaderHands = this.createShaderHands();
        if (!this.isShouldRenderFog(shaderHands)) {
            this.invalidateState();
        } else {
            this.updateDeleteWithClearColor();
            if (this.colorAttachmentFramebufferForCaptureBeforeHands != null && this.sceneFramebuffer != null && this.handMaskFramebuffer != null && this.frameCaptured) {
                this.updateGlBindFramebufferWithGlGetInteger(this.sceneFramebuffer, true);
                this.stateFlagForCaptureAfterHands = true;
            }
        }
    }

    public void renderOverlayIfPending() {
        if (this.stateFlagForCaptureAfterHands) {
            this.updateDeleteWithClearColor();
            if (this.colorAttachmentFramebufferForCaptureBeforeHands != null && this.sceneFramebuffer != null && this.handMaskFramebuffer != null) {
                ShaderHands shaderHands = this.createShaderHands();
                if (!this.isShouldRenderFog(shaderHands)) {
                    this.invalidateState();
                } else if (shaderHands.mode.is("Туман")) {
                    this.renderShaderTextureWithUniform(shaderHands);
                    this.invalidateState();
                } else {
                    ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsMaskDiff);
                    if (orCreateProgram == null) {
                        this.invalidateState();
                    } else {
                        this.handMaskFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                        this.handMaskFramebuffer.clear();
                        this.handMaskFramebuffer.beginWrite(false);
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableBlend();
                        RenderSystem.setShader(ShaderPrograms.shaderHandsMaskDiff);
                        setUniform(orCreateProgram, "colorFallback", shaderHands.mode.is("Огонь") ? 1.0F : 0.0F);
                        RenderSystem.setShaderTexture(0, this.colorAttachmentFramebufferForCaptureBeforeHands.getColorAttachment());
                        RenderSystem.setShaderTexture(1, this.sceneFramebuffer.getColorAttachment());
                        int effectWidth = this.colorAttachmentFramebufferForCaptureBeforeHands.getDepthAttachment();
                        int effectHeight = this.sceneFramebuffer.getDepthAttachment();
                        if (effectWidth != 0 && effectWidth != this.effectWidth) {
                            this.updateGlTexParameteriWithBindTexture(effectWidth);
                            this.effectWidth = effectWidth;
                        }
                        if (effectHeight != 0 && effectHeight != this.effectHeight) {
                            this.updateGlTexParameteriWithBindTexture(effectHeight);
                            this.effectHeight = effectHeight;
                        }
                        RenderSystem.setShaderTexture(2, effectWidth);
                        RenderSystem.setShaderTexture(3, effectHeight);
                        drawFullscreenQuad();
                        RenderSystem.enableDepthTest();
                        if (shaderHands.mode.is("Огонь")) {
                            try {
                                this.resetHandsPostProcessor.render(shaderHands, this.sceneFramebuffer, this.handMaskFramebuffer, this.effectFramebuffer, this.glowFramebuffer);
                            } finally {
                                this.updateShaderTextureWithColorMask();
                                this.invalidateState();
                            }
                        } else {
                            boolean flag = shaderHands.mode.is("Плазма");
                            float floatValue = flag ? 0.0F : shaderHands.glow.get();
                            float floatValueValue = shaderHands.fill.get();
                            float floatValueResult = 1.0F;
                            float floatValueItem = flag ? 0.0F : shaderHands.outline.get();
                            boolean flagValue = floatValue > 0.001F;
                            boolean flagResult = floatValueValue > 0.001F;
                            int intValue = flag ? shaderHands.plasmaColorFirst.getColor() : Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color[0];
                            int intValueValue = flag ? shaderHands.plasmaColorSecond.getColor() : intValue;
                            if (shaderHands.mode.is("Жидкое стекло")) {
                                try {
                                    this.renderUniformWithShaderTexture(shaderHands, intValue);
                                } finally {
                                    this.updateShaderTextureWithColorMask();
                                    this.invalidateState();
                                }
                            } else if (shaderHands.mode.is("Красивый")) {
                                this.renderUniformWithDefaultBlendFunc(shaderHands, intValue, intValueValue, floatValue, floatValueValue, floatValueResult, floatValueItem);
                                this.invalidateState();
                            } else {
                                int intValueResult = 0;
                                if (flagValue) {
                                    int intValueItem;
                                    if (shaderHands.optimize.isState()) {
                                        intValueItem = Math.max(2, Math.min(4, 2 + Math.round(floatValue * 0.4F)));
                                    } else {
                                        intValueItem = Math.max(3, Math.min(8, 4 + Math.round(floatValue * 0.7F)));
                                    }
                                    intValueResult = this.calculateColorAttachment(intValueItem);
                                }
                                mc.getFramebuffer().beginWrite(true);
                                RenderSystem.enableBlend();
                                RenderSystem.colorMask(true, true, true, false);
                                RenderSystem.disableDepthTest();
                                ShaderProgram shaderProgram = flagValue ? mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsGlow) : null;
                                if (shaderProgram != null) {
                                    RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
                                    RenderSystem.setShader(ShaderPrograms.shaderHandsGlow);
                                    RenderSystem.setShaderTexture(0, intValueResult);
                                    RenderSystem.setShaderTexture(1, this.handMaskFramebuffer.getColorAttachment());
                                    setUniform(shaderProgram, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                    setUniform(shaderProgram, "color2", ColorMathUtil.redf(intValueValue), ColorMathUtil.greenf(intValueValue), ColorMathUtil.bluef(intValueValue));
                                    setUniform(shaderProgram, "exposure", 1.0F + floatValue * 1.8F);
                                    drawFullscreenQuad();
                                }
                                if (flagResult) {
                                    ShaderProgramKey shaderProgramKey = flag ? ShaderPrograms.shaderHandsPlasma : ShaderPrograms.shaderHandsOverlay;
                                    ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(shaderProgramKey);
                                    if (orCreateProgramValue == null) {
                                        this.updateShaderTextureWithColorMask();
                                        this.invalidateState();
                                        return;
                                    }
                                    RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                                    RenderSystem.setShader(shaderProgramKey);
                                    RenderSystem.setShaderTexture(0, this.handMaskFramebuffer.getColorAttachment());
                                    setUniform(orCreateProgramValue, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                    setUniform(orCreateProgramValue, "fill", floatValueValue);
                                    setUniform(orCreateProgramValue, "alpha", floatValueResult);
                                    if (flag) {
                                        setUniform(orCreateProgramValue, "color2", ColorMathUtil.redf(intValueValue), ColorMathUtil.greenf(intValueValue), ColorMathUtil.bluef(intValueValue));
                                        setUniform(orCreateProgramValue, "time", (float) (System.nanoTime() - this.uniformTimestamp) / 1.0E9F);
                                        setUniform(orCreateProgramValue, "speed", shaderHands.plasmaSpeed.get());
                                        setUniform(orCreateProgramValue, "scale", shaderHands.plasmaScale.get());
                                        setUniform(orCreateProgramValue, "resolution", this.framebufferWidth, this.framebufferHeight);
                                    }
                                    drawFullscreenQuad();
                                }
                                if (floatValueItem > 0.001F) {
                                    ShaderProgram orCreateProgramResult = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.blockOverlay);
                                    if (orCreateProgramResult != null) {
                                        RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                                        RenderSystem.setShader(ShaderPrograms.blockOverlay);
                                        RenderSystem.setShaderTexture(0, this.handMaskFramebuffer.getColorAttachment());
                                        setUniform(orCreateProgramResult, "texelSize", 1.0F / Math.max(1, mc.getWindow().getFramebufferWidth()), 1.0F / Math.max(1, mc.getWindow().getFramebufferHeight()));
                                        setUniform(orCreateProgramResult, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                        setUniform(orCreateProgramResult, "color2", ColorMathUtil.redf(intValueValue), ColorMathUtil.greenf(intValueValue), ColorMathUtil.bluef(intValueValue));
                                        setUniform(orCreateProgramResult, "outline", floatValueItem);
                                        setUniform(orCreateProgramResult, "alpha", Math.max(0.2F, floatValueResult));
                                        setUniform(orCreateProgramResult, "outlineOnly", 1.0F);
                                        setUniform(orCreateProgramResult, "optimize", shaderHands.optimize.isState() ? 1.0F : 0.0F);
                                        drawFullscreenQuad();
                                    }
                                }
                                this.updateShaderTextureWithColorMask();
                                this.invalidateState();
                            }
                        }
                    }
                }
            }
        }
    }

    public void invalidateState() {
        this.frameCaptured = false;
        this.stateFlagForCaptureAfterHands = false;
        this.effectWidth = -1;
        this.effectHeight = -1;
    }

    private int calculateColorAttachment(int intValue) {
        return this.calculateBeginWriteWithOrCreateProgram(this.handMaskFramebuffer.getColorAttachment(), intValue, 1.0F);
    }

    private int calculateBeginWriteWithOrCreateProgram(int intValue, int intValueValue, float floatValue) {
        this.updateDelete(intValueValue);
        if (this.framebufferEntries.isEmpty()) {
            return intValue;
        } else {
            int intValueResult = intValue;
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsKawaseDown);
            ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsKawaseUp);
            if (orCreateProgram != null && orCreateProgramValue != null) {
                for (int index = 0; index < intValueValue; index++) {
                    Framebuffer framebuffer = this.framebufferEntries.get(index);
                    framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    framebuffer.clear();
                    framebuffer.beginWrite(true);
                    RenderSystem.setShader(ShaderPrograms.shaderHandsKawaseDown);
                    RenderSystem.setShaderTexture(0, intValueResult);
                    this.updateUniform(orCreateProgram, framebuffer.textureWidth, framebuffer.textureHeight, (1.0F + index) * floatValue);
                    drawFullscreenQuad();
                    intValueResult = framebuffer.getColorAttachment();
                }
                for (int indexValue = intValueValue - 1; indexValue >= 1; indexValue--) {
                    Framebuffer framebufferValue = this.framebufferEntries.get(indexValue - 1);
                    framebufferValue.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    framebufferValue.clear();
                    framebufferValue.beginWrite(true);
                    RenderSystem.setShader(ShaderPrograms.shaderHandsKawaseUp);
                    RenderSystem.setShaderTexture(0, intValueResult);
                    this.updateUniform(orCreateProgramValue, framebufferValue.textureWidth, framebufferValue.textureHeight, (1.0F + indexValue) * floatValue);
                    setUniform(orCreateProgramValue, "color", 1.0F, 1.0F, 1.0F);
                    drawFullscreenQuad();
                    intValueResult = framebufferValue.getColorAttachment();
                }
                mc.getFramebuffer().beginWrite(true);
                return intValueResult;
            } else {
                return intValue;
            }
        }
    }

    private void updateGlBindFramebufferWithGlGetInteger(Framebuffer framebuffer, boolean flag) {
        int intValue = GL11.glGetInteger(36010);
        int intValueValue = GL11.glGetInteger(36006);
        GL30.glBindFramebuffer(36008, mc.getFramebuffer().fbo);
        GL30.glBindFramebuffer(36009, framebuffer.fbo);
        short shortValue = 16384;
        if (flag) {
            shortValue = (short) (shortValue | 256);
        }
        GL30.glBlitFramebuffer(0, 0, this.framebufferWidth, this.framebufferHeight, 0, 0, this.framebufferWidth, this.framebufferHeight, shortValue, 9728);
        GL30.glBindFramebuffer(36008, intValue);
        GL30.glBindFramebuffer(36009, intValueValue);
        mc.getFramebuffer().beginWrite(true);
    }

    private void updateGlTexParameteriWithBindTexture(int intValue) {
        RenderSystem.bindTexture(intValue);
        GL11.glTexParameteri(3553, 34892, 0);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        RenderSystem.bindTexture(0);
    }

    private void updateDeleteWithClearColor() {
        int framebufferWidth = mc.getWindow().getFramebufferWidth();
        int framebufferHeight = mc.getWindow().getFramebufferHeight();
        ShaderHands shaderHands = this.createShaderHands();
        int intValue = shaderHands != null && shaderHands.mode.is("Огонь") ? 1 : (shaderHands != null && shaderHands.optimize.isState() ? 4 : 2);
        int intValueValue = Math.max(2, framebufferWidth / intValue);
        int intValueResult = Math.max(2, framebufferHeight / intValue);
        if (framebufferWidth != this.framebufferWidth || framebufferHeight != this.framebufferHeight || this.colorAttachmentFramebufferForCaptureBeforeHands == null || this.sceneFramebuffer == null || this.handMaskFramebuffer == null || this.effectFramebuffer == null || this.glowFramebuffer == null || this.effectFramebuffer.textureWidth != intValueValue || this.effectFramebuffer.textureHeight != intValueResult) {
            if (this.colorAttachmentFramebufferForCaptureBeforeHands != null) {
                this.colorAttachmentFramebufferForCaptureBeforeHands.delete();
            }
            if (this.sceneFramebuffer != null) {
                this.sceneFramebuffer.delete();
            }
            if (this.handMaskFramebuffer != null) {
                this.handMaskFramebuffer.delete();
            }
            if (this.effectFramebuffer != null) {
                this.effectFramebuffer.delete();
            }
            if (this.glowFramebuffer != null) {
                this.glowFramebuffer.delete();
            }
            for (Framebuffer framebuffer : this.framebufferEntries) {
                framebuffer.delete();
            }
            this.framebufferEntries.clear();
            this.colorAttachmentFramebufferForCaptureBeforeHands = new SimpleFramebuffer(framebufferWidth, framebufferHeight, true);
            this.sceneFramebuffer = new SimpleFramebuffer(framebufferWidth, framebufferHeight, true);
            this.handMaskFramebuffer = new SimpleFramebuffer(framebufferWidth, framebufferHeight, true);
            this.effectFramebuffer = new SimpleFramebuffer(intValueValue, intValueResult, false);
            this.glowFramebuffer = new SimpleFramebuffer(intValueValue, intValueResult, false);
            this.updateGlTexParameteriWithBindTexture(this.colorAttachmentFramebufferForCaptureBeforeHands);
            this.updateGlTexParameteriWithBindTexture(this.sceneFramebuffer);
            if (shaderHands != null && shaderHands.mode.is("Огонь")) {
                this.updateGlTexParameteriWithBindTexture(this.handMaskFramebuffer);
            }
            this.updateGlTexParameteriWithBindTexture(this.effectFramebuffer);
            this.updateGlTexParameteriWithBindTexture(this.glowFramebuffer);
            this.effectFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.effectFramebuffer.clear();
            this.glowFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.glowFramebuffer.clear();
            this.stateFlagForUpdateDeleteWithClearColor = true;
            this.resetHandsPostProcessor.reset();
            this.framebufferWidth = framebufferWidth;
            this.framebufferHeight = framebufferHeight;
            this.effectWidth = -1;
            this.effectHeight = -1;
        }
    }

    private void updateDelete(int intValue) {
        while (this.framebufferEntries.size() > intValue) {
            int intValueValue = this.framebufferEntries.size() - 1;
            this.framebufferEntries.get(intValueValue).delete();
            this.framebufferEntries.remove(intValueValue);
        }
        ShaderHands shaderHands = this.createShaderHands();
        boolean flag = shaderHands != null && shaderHands.optimize.isState();
        for (int index = 0; index < intValue; index++) {
            int intValueResult = flag ? index + 2 : index + 1;
            int intValueItem = Math.max(2, this.framebufferWidth >> intValueResult);
            int intValueEntry = Math.max(2, this.framebufferHeight >> intValueResult);
            if (index >= this.framebufferEntries.size()) {
                SimpleFramebuffer framebuffer = new SimpleFramebuffer(intValueItem, intValueEntry, false);
                this.updateGlTexParameteriWithBindTexture(framebuffer);
                this.framebufferEntries.add(framebuffer);
            } else {
                Framebuffer framebufferValue = this.framebufferEntries.get(index);
                if (framebufferValue.textureWidth != intValueItem || framebufferValue.textureHeight != intValueEntry) {
                    framebufferValue.delete();
                    SimpleFramebuffer framebufferResult = new SimpleFramebuffer(intValueItem, intValueEntry, false);
                    this.updateGlTexParameteriWithBindTexture(framebufferResult);
                    this.framebufferEntries.set(index, framebufferResult);
                }
            }
        }
    }

    private void updateGlTexParameteriWithBindTexture(Framebuffer framebuffer) {
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        RenderSystem.bindTexture(0);
    }

    private ShaderHands createShaderHands() {
        return Toshiba.INSTANCE != null && ModuleRegistry.INSTANCE != null ? ModuleRegistry.shaderHands : null;
    }

    private void renderUniformWithShaderTexture(ShaderHands shaderHands, int intValue) {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsGlass);
        if (orCreateProgram != null) {
            float floatValue = shaderHands.glassBlur.isState() ? shaderHands.glassBlurIntensity.get() : 0.0F;
            int colorAttachment = this.colorAttachmentFramebufferForCaptureBeforeHands.getColorAttachment();
            int intValueValue = colorAttachment;
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.colorMask(true, true, true, true);
            if (floatValue > 0.001F) {
                intValueValue = this.calculateBeginWriteWithOrCreateProgram(colorAttachment, shaderHands.optimize.isState() ? 2 : 3, Math.max(0.1F, floatValue * 0.35F));
            }
            mc.getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.colorMask(true, true, true, false);
            RenderSystem.setShader(ShaderPrograms.shaderHandsGlass);
            RenderSystem.setShaderTexture(0, this.handMaskFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(1, colorAttachment);
            RenderSystem.setShaderTexture(2, this.sceneFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(3, intValueValue);
            setUniform(orCreateProgram, "resolution", this.framebufferWidth, this.framebufferHeight);
            setUniform(orCreateProgram, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
            setUniform(orCreateProgram, "time", (float) (System.nanoTime() - this.uniformTimestamp) / 1.0E9F);
            setUniform(orCreateProgram, "transparency", shaderHands.glassTransparency.get());
            setUniform(orCreateProgram, "distortion", shaderHands.glassDistortion.get());
            setUniform(orCreateProgram, "speed", shaderHands.glassSpeed.get());
            setUniform(orCreateProgram, "glow", shaderHands.glow.get());
            setUniform(orCreateProgram, "outline", shaderHands.outline.get());
            setUniform(orCreateProgram, "blurIntensity", floatValue);
            drawFullscreenQuad();
        }
    }

    private void renderUniformWithDefaultBlendFunc(ShaderHands shaderHands, int intValue, int intValueValue, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.blockOverlay);
        if (orCreateProgram != null) {
            mc.getFramebuffer().beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(ShaderPrograms.blockOverlay);
            RenderSystem.setShaderTexture(0, this.handMaskFramebuffer.getColorAttachment());
            setUniform(orCreateProgram, "texelSize", 1.0F / Math.max(1, mc.getWindow().getFramebufferWidth()), 1.0F / Math.max(1, mc.getWindow().getFramebufferHeight()));
            setUniform(orCreateProgram, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
            setUniform(orCreateProgram, "color2", ColorMathUtil.redf(intValueValue), ColorMathUtil.greenf(intValueValue), ColorMathUtil.bluef(intValueValue));
            setUniform(orCreateProgram, "time", (float) (System.currentTimeMillis() % 100000L) / 1000.0F);
            setUniform(orCreateProgram, "speed", shaderHands.waveSpeed.get());
            setUniform(orCreateProgram, "outline", floatValueItem);
            setUniform(orCreateProgram, "glow", floatValue);
            setUniform(orCreateProgram, "fill", floatValueValue);
            setUniform(orCreateProgram, "alpha", floatValueResult);
            setUniform(orCreateProgram, "outlineOnly", 0.0F);
            setUniform(orCreateProgram, "optimize", shaderHands.optimize.isState() ? 1.0F : 0.0F);
            drawFullscreenQuad();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            this.updateShaderTextureWithColorMask();
        }
    }

    private void renderShaderTextureWithUniform(ShaderHands shaderHands) {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsFlameTrail);
        ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsFlameComposite);
        if (orCreateProgram != null && orCreateProgramValue != null) {
            int effectHeight = this.sceneFramebuffer.getDepthAttachment();
            if (effectHeight != 0 && effectHeight != this.effectHeight) {
                this.updateGlTexParameteriWithBindTexture(effectHeight);
                this.effectHeight = effectHeight;
            }
            int intValue = shaderHands.resolveFogColor();
            float floatValue = ColorMathUtil.redf(intValue);
            float floatValueValue = ColorMathUtil.greenf(intValue);
            float floatValueResult = ColorMathUtil.bluef(intValue);
            float floatValueItem = ColorMathUtil.alphaf(intValue);
            float floatValueEntry = shaderHands.fogStrength.get();
            float floatValueCopy = shaderHands.fogRiseSpeed.get();
            float floatValue2 = shaderHands.fogWobble.get();
            float floatValue3 = shaderHands.fogLength.get();
            float floatValue4 = shaderHands.fogBrightness.get();
            float floatValue5 = (float) (System.nanoTime() % 180000000000L) / 1.0E9F;
            float floatValue6 = !shaderHands.fogColorMode.is("Предмет") && !shaderHands.fogColorMode.is("Item") ? 1.0F : 0.0F;
            float floatValue7 = shaderHands.fogOnlyItems.isState() ? 10.0F : 0.0F;
            float floatValue8 = floatValue6 + floatValue7;
            float framebufferWidth = this.framebufferWidth;
            float framebufferHeight = this.framebufferHeight;
            float floatValue9 = 1.0F / Math.max(1, this.framebufferWidth);
            float floatValue10 = 1.0F / Math.max(1, this.framebufferHeight);
            int textureWidth = this.effectFramebuffer.textureWidth;
            int textureHeight = this.effectFramebuffer.textureHeight;
            float floatValue11 = 1.0F / Math.max(1, textureWidth);
            float floatValue12 = 1.0F / Math.max(1, textureHeight);
            Framebuffer framebuffer = this.stateFlagForUpdateDeleteWithClearColor ? this.effectFramebuffer : this.glowFramebuffer;
            Framebuffer framebufferValue = this.stateFlagForUpdateDeleteWithClearColor ? this.glowFramebuffer : this.effectFramebuffer;
            framebufferValue.beginWrite(false);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.setShader(ShaderPrograms.shaderHandsFlameTrail);
            RenderSystem.setShaderTexture(0, this.colorAttachmentFramebufferForCaptureBeforeHands.getColorAttachment());
            RenderSystem.setShaderTexture(1, this.sceneFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(2, framebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(3, effectHeight);
            setUniform(orCreateProgram, "flameColor", floatValue, floatValueValue, floatValueResult, floatValueItem);
            setUniform(orCreateProgram, "params0", floatValueEntry, floatValueCopy, floatValue2, floatValue3);
            setUniform(orCreateProgram, "params1", floatValue4, floatValue5, floatValue8, floatValueItem);
            setUniform(orCreateProgram, "screen", textureWidth, textureHeight, floatValue11, floatValue12);
            drawFullscreenQuad();
            this.stateFlagForUpdateDeleteWithClearColor = !this.stateFlagForUpdateDeleteWithClearColor;
            mc.getFramebuffer().beginWrite(false);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.setShader(ShaderPrograms.shaderHandsFlameComposite);
            RenderSystem.setShaderTexture(0, this.sceneFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(1, this.colorAttachmentFramebufferForCaptureBeforeHands.getColorAttachment());
            RenderSystem.setShaderTexture(2, framebufferValue.getColorAttachment());
            RenderSystem.setShaderTexture(3, effectHeight);
            setUniform(orCreateProgramValue, "flameColor", floatValue, floatValueValue, floatValueResult, floatValueItem);
            setUniform(orCreateProgramValue, "params0", floatValueEntry, floatValueCopy, floatValue2, floatValue3);
            setUniform(orCreateProgramValue, "params1", floatValue4, floatValue5, floatValue8, floatValueItem);
            setUniform(orCreateProgramValue, "screen", framebufferWidth, framebufferHeight, floatValue9, floatValue10);
            drawFullscreenQuad();
            this.updateShaderTextureWithColorMask();
        }
    }

    public void resetTrail() {
        this.resetHandsPostProcessor.reset();
        if (this.effectFramebuffer != null) {
            this.effectFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.effectFramebuffer.clear();
        }
        if (this.glowFramebuffer != null) {
            this.glowFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.glowFramebuffer.clear();
        }
        this.stateFlagForUpdateDeleteWithClearColor = true;
    }

    private void updateShaderTextureWithColorMask() {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.setShaderTexture(1, 0);
        RenderSystem.setShaderTexture(2, 0);
        RenderSystem.setShaderTexture(3, 0);
        mc.getFramebuffer().beginWrite(true);
    }

    private boolean isShouldRenderFog(ShaderHands shaderHands) {
        if (shaderHands == null || !shaderHands.isEnable()) {
            return false;
        } else if (shaderHands.mode.is("Туман")) {
            return shaderHands.shouldRenderFog();
        } else if (shaderHands.mode.is("Жидкое стекло")) {
            return true;
        } else if (shaderHands.mode.is("Огонь")) {
            return true;
        } else if (shaderHands.mode.is("Плазма")) {
            return shaderHands.fill.get() > 0.001F;
        } else {
            boolean flag = shaderHands.glow.get() > 0.001F;
            boolean flagValue = shaderHands.fill.get() > 0.001F;
            return flag || flagValue;
        }
    }

    public static void setUniform(ShaderProgram shaderProgram, String text, float floatValue) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue);
        }
    }

    public static void setUniform(ShaderProgram shaderProgram, String text, float floatValue, float floatValueValue) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue);
        }
    }

    public static void setUniform(ShaderProgram shaderProgram, String text, float floatValue, float floatValueValue, float floatValueResult) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue, floatValueResult);
        }
    }

    public static void setUniform(ShaderProgram shaderProgram, String text, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue, floatValueResult, floatValueItem);
        }
    }

    private void updateUniform(ShaderProgram shaderProgram, int intValue, int intValueValue, float floatValue) {
        setUniform(shaderProgram, "uSize", Math.max(1, intValue), Math.max(1, intValueValue));
        setUniform(shaderProgram, "uOffset", floatValue, floatValue);
        setUniform(shaderProgram, "uHalfPixel", 0.5F / Math.max(1, intValue), 0.5F / Math.max(1, intValueValue));
    }

    public static void drawFullscreenQuad() {
        float floatValue = Math.max(mc.getWindow().getScaledWidth(), 1);
        float floatValueValue = Math.max(mc.getWindow().getScaledHeight(), 1);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(0.0F, floatValueValue, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, floatValueValue, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
