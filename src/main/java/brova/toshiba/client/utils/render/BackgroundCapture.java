package brova.toshiba.client.utils.render;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class BackgroundCapture implements MinecraftAccess {

    private final int[] viewportIntegerValues = new int[4];

    private Framebuffer framebuffer;

    private ShaderProgram orCreateProgram;

    private GlUniform opacityUniform;

    public boolean captureBackground(DrawContext context, float floatValue) {
        if (floatValue >= 1.0F) {
            return false;
        } else {
            this.orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.guiFade);
            if (this.orCreateProgram == null) {
                return false;
            } else {
                this.opacityUniform = this.orCreateProgram.getUniform("Opacity");
                if (this.opacityUniform == null) {
                    return false;
                } else {
                    context.draw();
                    Framebuffer main = mc.getFramebuffer();
                    int textureWidth = main.textureWidth;
                    int textureHeight = main.textureHeight;
                    if (textureWidth > 0 && textureHeight > 0) {
                        int intValue = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
                        int intValueValue = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
                        boolean flag = GL11.glIsEnabled(2929);
                        boolean flagValue = GL11.glIsEnabled(3089);
                        GL11.glGetIntegerv(2978, this.viewportIntegerValues);
                        try {
                            if (flagValue) {
                                GL11.glDisable(3089);
                            }
                            this.updateInteger(textureWidth, textureHeight);
                            // Источник и приёмник задаём явно: берём всегда основной фреймбуфер.
                            // Раньше источником был "текущий" draw framebuffer, а его могут оставить
                            // переключённым другие модули (shader esp) - тогда в кадр попадал мусор и gui мигал белым.
                            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.fbo);
                            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.framebuffer.fbo);
                            GL30.glBlitFramebuffer(0, 0, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, 16384, 9728);
                        } finally {
                            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, intValueValue);
                            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, intValue);
                            main.beginWrite(false);
                            RenderSystem.viewport(this.viewportIntegerValues[0], this.viewportIntegerValues[1], this.viewportIntegerValues[2], this.viewportIntegerValues[3]);
                            if (flag) {
                                RenderSystem.enableDepthTest();
                            } else {
                                RenderSystem.disableDepthTest();
                            }
                            if (flagValue) {
                                GL11.glEnable(3089);
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    public void render(DrawContext context, float floatValue) {
        context.draw();
        boolean flag = GL11.glIsEnabled(2929);
        boolean flagValue = GL11.glIsEnabled(3042);
        boolean flagResult = GL11.glIsEnabled(3089);
        int intValue = GL11.glGetInteger(32969);
        int intValueValue = GL11.glGetInteger(32968);
        int intValueResult = GL11.glGetInteger(32971);
        int intValueItem = GL11.glGetInteger(32970);
        int intValueEntry = GL11.glGetInteger(32777);
        int shaderTexture = RenderSystem.getShaderTexture(0);
        ShaderProgram shader = RenderSystem.getShader();
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendEquation(32774);
            if (flagResult) {
                GL11.glDisable(3089);
            }
            this.opacityUniform.set(1.0F - MathHelper.clamp(floatValue, 0.0F, 1.0F));
            RenderSystem.setShader(this.orCreateProgram);
            RenderSystem.setShaderTexture(0, this.framebuffer.getColorAttachment());
            float scaledWindowWidth = context.getScaledWindowWidth();
            float scaledWindowHeight = context.getScaledWindowHeight();
            Matrix4f matrix = new Matrix4f();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(-1);
            buffer.vertex(matrix, 0.0F, scaledWindowHeight, 0.0F).texture(0.0F, 0.0F).color(-1);
            buffer.vertex(matrix, scaledWindowWidth, scaledWindowHeight, 0.0F).texture(1.0F, 0.0F).color(-1);
            buffer.vertex(matrix, scaledWindowWidth, 0.0F, 0.0F).texture(1.0F, 1.0F).color(-1);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            RenderSystem.setShaderTexture(0, shaderTexture);
            RenderSystem.setShader(shader);
            RenderSystem.blendFuncSeparate(intValue, intValueValue, intValueResult, intValueItem);
            RenderSystem.blendEquation(intValueEntry);
            if (!flagValue) {
                RenderSystem.disableBlend();
            }
            if (flag) {
                RenderSystem.enableDepthTest();
            }
            if (flagResult) {
                GL11.glEnable(3089);
            }
        }
    }

    public void close() {
        if (this.framebuffer != null) {
            int intValue = GL11.glGetInteger(36010);
            int intValueValue = GL11.glGetInteger(36006);
            try {
                this.framebuffer.delete();
                this.framebuffer = null;
            } finally {
                GlStateManager._glBindFramebuffer(36008, intValue);
                GlStateManager._glBindFramebuffer(36009, intValueValue);
            }
        }
    }

    private void updateInteger(int intValue, int intValueValue) {
        if (this.framebuffer == null || this.framebuffer.textureWidth != intValue || this.framebuffer.textureHeight != intValueValue) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((BackgroundCapture) accessReceiver).close();
            });
            this.framebuffer = new SimpleFramebuffer(intValue, intValueValue, false);
        }
    }
}
