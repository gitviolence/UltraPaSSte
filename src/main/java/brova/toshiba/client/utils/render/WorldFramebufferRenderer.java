package brova.toshiba.client.utils.render;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.ColorSaturation;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class WorldFramebufferRenderer implements MinecraftAccess {

    private static final WorldFramebufferRenderer INSTANCE = new WorldFramebufferRenderer();

    private Framebuffer deleteFramebuffer;

    private Framebuffer hudFramebuffer;

    private Framebuffer framebuffer;

    private WorldFramebufferRenderer() {
    }

    public static WorldFramebufferRenderer getInstance() {
        return INSTANCE;
    }

    public void renderWorld() {
        ColorSaturation colorSaturation = ModuleRegistry.colorSaturation;
        if (this.isFinite(colorSaturation)) {
            this.deleteFramebuffer = this.createGlBindFramebufferWithGlGetInteger(mc.getFramebuffer(), this.deleteFramebuffer);
            if (this.deleteFramebuffer != null) {
                this.renderGlIsEnabledWithGlGetInteger(colorSaturation.saturation.get(), this.deleteFramebuffer, false);
            }
        }
    }

    public Framebuffer getHudFramebuffer() {
        return this.framebuffer != null ? this.hudFramebuffer : null;
    }

    public void beginHud(DrawContext context) {
        ColorSaturation colorSaturation = ModuleRegistry.colorSaturation;
        if (this.framebuffer == null && this.isFinite(colorSaturation) && colorSaturation.vanillaHud.isState() && mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.colorSaturation) != null) {
            context.draw();
            Framebuffer framebuffer = mc.getFramebuffer();
            int textureWidth = framebuffer.textureWidth;
            int textureHeight = framebuffer.textureHeight;
            if (textureWidth >= 1 && textureHeight >= 1) {
                boolean flag = GL11.glIsEnabled(2929);
                boolean flagValue = GL11.glGetBoolean(2930);
                boolean flagResult = GL11.glIsEnabled(3089);
                MemoryStack memoryStack = MemoryStack.stackPush();
                try {
                    ByteBuffer buffer = memoryStack.malloc(4);
                    GL11.glGetBooleanv(3107, buffer);
                    try {
                        if (flagResult) {
                            GL11.glDisable(3089);
                        }
                        RenderSystem.colorMask(true, true, true, true);
                        RenderSystem.depthMask(true);
                        if (this.hudFramebuffer == null) {
                            this.hudFramebuffer = new SimpleFramebuffer(textureWidth, textureHeight, true);
                        } else if (this.hudFramebuffer.textureWidth != textureWidth || this.hudFramebuffer.textureHeight != textureHeight) {
                            this.hudFramebuffer.resize(textureWidth, textureHeight);
                        }
                        this.hudFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                        this.hudFramebuffer.clear();
                        this.framebuffer = framebuffer;
                    } finally {
                        RenderSystem.colorMask(buffer.get(0) != 0, buffer.get(1) != 0, buffer.get(2) != 0, buffer.get(3) != 0);
                        RenderSystem.depthMask(flagValue);
                        if (flag) {
                            RenderSystem.enableDepthTest();
                        } else {
                            RenderSystem.disableDepthTest();
                        }
                        if (flagResult) {
                            GL11.glEnable(3089);
                        }
                        mc.getFramebuffer().beginWrite(true);
                    }
                } catch (Throwable failure) {
                    if (memoryStack != null) {
                        try {
                            memoryStack.close();
                        } catch (Throwable failureValue) {
                            failure.addSuppressed(failureValue);
                        }
                    }
                    throw failure;
                }
                if (memoryStack != null) {
                    memoryStack.close();
                }
            }
        }
    }

    public void endHud(DrawContext context) {
        if (this.framebuffer != null) {
            Framebuffer framebuffer = this.framebuffer;
            try {
                context.draw();
            } finally {
                this.framebuffer = null;
                framebuffer.beginWrite(true);
            }
            ColorSaturation colorSaturation = ModuleRegistry.colorSaturation;
            float floatValue = this.isFinite(colorSaturation) && colorSaturation.vanillaHud.isState() ? colorSaturation.saturation.get() : 1.0F;
            this.renderGlIsEnabledWithGlGetInteger(floatValue, this.hudFramebuffer, true);
        }
    }

    public void release() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::release);
        } else {
            if (this.framebuffer != null) {
                Framebuffer framebuffer = this.framebuffer;
                this.framebuffer = null;
                framebuffer.beginWrite(true);
            }
            if (this.deleteFramebuffer != null || this.hudFramebuffer != null) {
                int intValue = GL11.glGetInteger(36010);
                int intValueValue = GL11.glGetInteger(36006);
                try {
                    if (this.deleteFramebuffer != null) {
                        this.deleteFramebuffer.delete();
                        this.deleteFramebuffer = null;
                    }
                    if (this.hudFramebuffer != null) {
                        this.hudFramebuffer.delete();
                        this.hudFramebuffer = null;
                    }
                } finally {
                    GL30.glBindFramebuffer(36008, intValue);
                    GL30.glBindFramebuffer(36009, intValueValue);
                }
            }
        }
    }

    private boolean isFinite(ColorSaturation colorSaturation) {
        return colorSaturation != null && colorSaturation.isEnable() && mc.world != null && Float.isFinite(colorSaturation.saturation.get()) && Math.abs(colorSaturation.saturation.get() - 1.0F) > 1.0E-4F;
    }

    private void renderGlIsEnabledWithGlGetInteger(float floatValue, Framebuffer framebuffer, boolean flag) {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.colorSaturation);
        if (orCreateProgram != null) {
            GlUniform uniform = orCreateProgram.getUniform("Saturation");
            GlUniform uniformValue = orCreateProgram.getUniform("HudPass");
            if (uniform != null && uniformValue != null) {
                Framebuffer framebufferValue = mc.getFramebuffer();
                ShaderProgram shader = RenderSystem.getShader();
                int shaderTexture = RenderSystem.getShaderTexture(0);
                boolean flagValue = GL11.glIsEnabled(2929);
                boolean flagResult = GL11.glGetBoolean(2930);
                boolean flagItem = GL11.glIsEnabled(3042);
                boolean flagEntry = GL11.glIsEnabled(2884);
                boolean flagCopy = GL11.glIsEnabled(3089);
                int intValue = GL11.glGetInteger(32969);
                int intValueValue = GL11.glGetInteger(32968);
                int intValueResult = GL11.glGetInteger(32971);
                int intValueItem = GL11.glGetInteger(32970);
                MemoryStack memoryStack = MemoryStack.stackPush();
                try {
                    ByteBuffer buffer = memoryStack.malloc(4);
                    GL11.glGetBooleanv(3107, buffer);
                    try {
                        framebufferValue.beginWrite(false);
                        RenderSystem.disableDepthTest();
                        RenderSystem.depthMask(false);
                        if (flag) {
                            RenderSystem.enableBlend();
                            RenderSystem.blendFuncSeparate(1, 771, 1, 771);
                        } else {
                            RenderSystem.disableBlend();
                        }
                        RenderSystem.disableCull();
                        if (flagCopy) {
                            GL11.glDisable(3089);
                        }
                        RenderSystem.colorMask(true, true, true, true);
                        RenderSystem.setShader(orCreateProgram);
                        RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
                        uniform.set(floatValue);
                        uniformValue.set(flag ? 1.0F : 0.0F);
                        this.renderTextureWithVertex();
                    } finally {
                        RenderSystem.setShaderTexture(0, shaderTexture);
                        if (shader == null) {
                            RenderSystem.clearShader();
                        } else {
                            RenderSystem.setShader(shader);
                        }
                        RenderSystem.colorMask(buffer.get(0) != 0, buffer.get(1) != 0, buffer.get(2) != 0, buffer.get(3) != 0);
                        RenderSystem.depthMask(flagResult);
                        RenderSystem.blendFuncSeparate(intValue, intValueValue, intValueResult, intValueItem);
                        if (flagValue) {
                            RenderSystem.enableDepthTest();
                        } else {
                            RenderSystem.disableDepthTest();
                        }
                        if (flagItem) {
                            RenderSystem.enableBlend();
                        } else {
                            RenderSystem.disableBlend();
                        }
                        if (flagEntry) {
                            RenderSystem.enableCull();
                        }
                        if (flagCopy) {
                            GL11.glEnable(3089);
                        }
                    }
                } catch (Throwable failure) {
                    if (memoryStack != null) {
                        try {
                            memoryStack.close();
                        } catch (Throwable failureValue) {
                            failure.addSuppressed(failureValue);
                        }
                    }
                    throw failure;
                }
                if (memoryStack != null) {
                    memoryStack.close();
                }
            }
        }
    }

    private Framebuffer createGlBindFramebufferWithGlGetInteger(Framebuffer framebuffer, Framebuffer framebufferValue) {
        int textureWidth = framebuffer.textureWidth;
        int textureHeight = framebuffer.textureHeight;
        if (textureWidth >= 1 && textureHeight >= 1) {
            int intValue = GL11.glGetInteger(36010);
            int intValueValue = GL11.glGetInteger(36006);
            boolean flag = GL11.glIsEnabled(2929);
            boolean flagValue = GL11.glIsEnabled(3089);
            Object value;
            try {
                if (flagValue) {
                    GL11.glDisable(3089);
                }
                if (framebufferValue == null) {
                    framebufferValue = new SimpleFramebuffer(textureWidth, textureHeight, false);
                } else if (framebufferValue.textureWidth != textureWidth || framebufferValue.textureHeight != textureHeight) {
                    framebufferValue.resize(textureWidth, textureHeight);
                }
                GL30.glBindFramebuffer(36008, framebuffer.fbo);
                GL30.glBindFramebuffer(36009, framebufferValue.fbo);
                GL30.glBlitFramebuffer(0, 0, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, 16384, 9728);
                value = framebufferValue;
            } finally {
                GL30.glBindFramebuffer(36008, intValue);
                GL30.glBindFramebuffer(36009, intValueValue);
                if (flag) {
                    RenderSystem.enableDepthTest();
                } else {
                    RenderSystem.disableDepthTest();
                }
                if (flagValue) {
                    GL11.glEnable(3089);
                }
            }
            return (Framebuffer) value;
        } else {
            return null;
        }
    }

    private void renderTextureWithVertex() {
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(-1.0F, -1.0F, 0.0F).texture(0.0F, 0.0F);
        buffer.vertex(1.0F, -1.0F, 0.0F).texture(1.0F, 0.0F);
        buffer.vertex(1.0F, 1.0F, 0.0F).texture(1.0F, 1.0F);
        buffer.vertex(-1.0F, 1.0F, 0.0F).texture(0.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
