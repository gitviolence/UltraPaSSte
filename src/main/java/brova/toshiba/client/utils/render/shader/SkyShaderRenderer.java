package brova.toshiba.client.utils.render.shader;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class SkyShaderRenderer {

    private static final Matrix4f projectionMatrixTransform = new Matrix4f();

    private static final Matrix4f identityTransform = new Matrix4f();

    private static Framebuffer framebuffer;

    private static int positionX;

    private static int positionY;

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private static void updateGlTexParameteriWithGlBindTexture(int intValue) {
        int intValueValue = Math.max(minecraftClient.getWindow().getFramebufferWidth() / intValue, 1);
        int intValueResult = Math.max(minecraftClient.getWindow().getFramebufferHeight() / intValue, 1);
        if (framebuffer == null || positionX != intValueValue || positionY != intValueResult) {
            if (framebuffer != null) {
                framebuffer.delete();
            }
            framebuffer = new SimpleFramebuffer(intValueValue, intValueResult, false);
            int intValueItem = GL11.glGetInteger(32873);
            GL11.glBindTexture(3553, framebuffer.getColorAttachment());
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
            GL11.glTexParameteri(3553, 10242, 33071);
            GL11.glTexParameteri(3553, 10243, 33071);
            GL11.glBindTexture(3553, intValueItem);
            positionX = intValueValue;
            positionY = intValueResult;
        }
    }

    public static void render(SkyShaderMode skyShaderMode) {
        render(skyShaderMode, null, null, 1.0F, 1.35F, false);
    }

    public static void render(SkyShaderMode skyShaderMode, boolean flag) {
        render(skyShaderMode, null, null, 1.0F, 1.35F, flag);
    }

    public static void render(SkyShaderMode skyShaderMode, float[] values, float[] valuesValue, float floatValue, float floatValueValue) {
        render(skyShaderMode, values, valuesValue, floatValue, floatValueValue, false);
    }

    public static void render(SkyShaderMode skyShaderMode, float[] values, float[] valuesValue, float floatValue, float floatValueValue, boolean flag) {
        if (minecraftClient.getWindow() != null) {
            ShaderProgramKey shaderProgramKey;
            byte byteValue = switch(skyShaderMode) {
                case CAUSTIC ->
                    {
                        shaderProgramKey = ShaderPrograms.skyCaustic;
                        yield 3;
                    }
                case SUNSET ->
                    {
                        shaderProgramKey = ShaderPrograms.skySunset;
                        yield 3;
                    }
                case NEBULA ->
                    {
                        shaderProgramKey = ShaderPrograms.skyNebula;
                        yield 3;
                    }
                case SEVERNOESIYANIE ->
                    {
                        shaderProgramKey = ShaderPrograms.skySevernoesiyanie;
                        yield 3;
                    }
                default ->
                    {
                        shaderProgramKey = ShaderPrograms.skyFog;
                        yield 3;
                    }
            };
            ShaderProgram orCreateProgram = minecraftClient.getShaderLoader().getOrCreateProgram(shaderProgramKey);
            if (orCreateProgram != null) {
                updateGlTexParameteriWithGlBindTexture(byteValue);
                Camera camera = minecraftClient.gameRenderer.getCamera();
                float floatValueResult = (float) Math.toRadians(-camera.getYaw());
                float floatValueItem = (float) Math.toRadians(camera.getPitch());
                float floatValueEntry = minecraftClient.options.getFov().getValue().floatValue();
                int framebufferWidth = minecraftClient.getWindow().getFramebufferWidth();
                int framebufferHeight = minecraftClient.getWindow().getFramebufferHeight();
                float floatValueCopy = (float) (System.currentTimeMillis() % 100000L) / 1000.0F;
                GlUniform glUniform;
                if ((glUniform = orCreateProgram.getUniform("Time")) != null) {
                    glUniform.set(floatValueCopy * floatValue);
                }
                if ((glUniform = orCreateProgram.getUniform("uTime")) != null) {
                    glUniform.set(floatValueCopy * floatValue);
                }
                if ((glUniform = orCreateProgram.getUniform("Resolution")) != null) {
                    glUniform.set((float) positionX, (float) positionY);
                }
                if ((glUniform = orCreateProgram.getUniform("uResolution")) != null) {
                    glUniform.set((float) positionX, (float) positionY);
                }
                if ((glUniform = orCreateProgram.getUniform("CameraDir")) != null) {
                    glUniform.set(floatValueResult, floatValueItem);
                }
                if ((glUniform = orCreateProgram.getUniform("uCameraDir")) != null) {
                    glUniform.set(floatValueResult, floatValueItem);
                }
                if ((glUniform = orCreateProgram.getUniform("Fov")) != null) {
                    glUniform.set(floatValueEntry);
                }
                if ((glUniform = orCreateProgram.getUniform("uFov")) != null) {
                    glUniform.set(floatValueEntry);
                }
                if (values != null && (glUniform = orCreateProgram.getUniform("Accent")) != null) {
                    glUniform.set(values[0], values[1], values[2]);
                }
                if ((glUniform = orCreateProgram.getUniform("uColor")) != null) {
                    if (values != null) {
                        glUniform.set(values[0], values[1], values[2]);
                    } else {
                        glUniform.set(0.03F, 0.85F, 0.55F);
                    }
                }
                if ((glUniform = orCreateProgram.getUniform("uColor2")) != null) {
                    if (valuesValue != null) {
                        glUniform.set(valuesValue[0], valuesValue[1], valuesValue[2]);
                    } else {
                        glUniform.set(0.65F, 0.15F, 0.85F);
                    }
                }
                if ((glUniform = orCreateProgram.getUniform("uAlpha")) != null) {
                    glUniform.set(1.0F);
                }
                if ((glUniform = orCreateProgram.getUniform("uSpeed")) != null) {
                    glUniform.set(floatValue);
                }
                if ((glUniform = orCreateProgram.getUniform("uScale")) != null) {
                    glUniform.set(floatValueValue <= 1.0F ? 5.8F : floatValueValue * 4.3F);
                }
                if ((glUniform = orCreateProgram.getUniform("uIntensity")) != null) {
                    glUniform.set(0.03F);
                }
                if ((glUniform = orCreateProgram.getUniform("Blur")) != null) {
                    glUniform.set(flag ? 1.0F : 0.0F);
                }
                if ((glUniform = orCreateProgram.getUniform("uBlur")) != null) {
                    glUniform.set(flag ? 1.0F : 0.0F);
                }
                if ((glUniform = orCreateProgram.getUniform("ColorModulator")) != null) {
                    glUniform.set(1.0F, 1.0F, 1.0F, 1.0F);
                }
                projectionMatrixTransform.set(RenderSystem.getProjectionMatrix());
                ProjectionType projectionType = RenderSystem.getProjectionType();
                int intValue = GL11.glGetInteger(36006);
                Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
                modelViewStack.pushMatrix();
                modelViewStack.identity();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableBlend();
                RenderSystem.disableCull();
                GL30.glBindFramebuffer(36160, framebuffer.fbo);
                GL11.glViewport(0, 0, positionX, positionY);
                RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.clear(16384);
                identityTransform.identity().setOrtho(0.0F, positionX, positionY, 0.0F, -1.0F, 1.0F);
                RenderSystem.setProjectionMatrix(identityTransform, ProjectionType.ORTHOGRAPHIC);
                RenderSystem.setShader(shaderProgramKey);
                BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                buffer.vertex(0.0F, positionY, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                buffer.vertex(positionX, positionY, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                buffer.vertex(positionX, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
                GL30.glBindFramebuffer(36160, intValue);
                GL11.glViewport(0, 0, framebufferWidth, framebufferHeight);
                identityTransform.identity().setOrtho(0.0F, framebufferWidth, framebufferHeight, 0.0F, -1.0F, 1.0F);
                RenderSystem.setProjectionMatrix(identityTransform, ProjectionType.ORTHOGRAPHIC);
                RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
                BufferBuilder bufferValue = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                bufferValue.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                bufferValue.vertex(0.0F, framebufferHeight, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                bufferValue.vertex(framebufferWidth, framebufferHeight, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                bufferValue.vertex(framebufferWidth, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
                BufferRenderer.drawWithGlobalProgram(bufferValue.end());
                RenderSystem.setShaderTexture(0, 0);
                RenderSystem.enableCull();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                modelViewStack.popMatrix();
                RenderSystem.setProjectionMatrix(projectionMatrixTransform, projectionType);
            }
        }
    }

    private SkyShaderRenderer() {
        throw new UnsupportedOperationException("Utility class");
    }
}
