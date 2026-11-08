package brova.toshiba.client.utils.render;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Renderer2D implements MinecraftAccess {

    private static final ConcurrentHashMap<String, Identifier> containsKeyIdentifierEntries = new ConcurrentHashMap<>();

    private static final UUID uUID = new UUID(0L, 0L);

    private static ShaderProgram shaderProgram;

    private static GlUniform sizeUniform;

    private static GlUniform radiusUniform;

    private static GlUniform smoothnessUniform;

    private static GlUniform colorModulatorUniform;

    public static void drawHudItem(DrawContext context, ItemStack stack, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        if (context != null && stack != null && !stack.isEmpty()) {
            MatrixStack matrices = context.getMatrices();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            matrices.push();
            matrices.translate(floatValue, floatValueValue, floatValueItem);
            matrices.scale(floatValueResult, floatValueResult, 1.0F);
            context.drawItem(stack, 0, 0);
            matrices.pop();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(true);
        }
    }

    public static void drawGradient6Rect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.gradient6Rect);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        GlUniform uniform = orCreateProgram.getUniform("Size");
        GlUniform uniformValue = orCreateProgram.getUniform("Radius");
        GlUniform uniformResult = orCreateProgram.getUniform("Smoothness");
        GlUniform uniformItem = orCreateProgram.getUniform("LeftTopColor");
        GlUniform uniformEntry = orCreateProgram.getUniform("LeftBottomColor");
        GlUniform uniformCopy = orCreateProgram.getUniform("CenterTopColor");
        GlUniform uniform2 = orCreateProgram.getUniform("CenterBottomColor");
        GlUniform uniform3 = orCreateProgram.getUniform("RightTopColor");
        GlUniform uniform4 = orCreateProgram.getUniform("RightBottomColor");
        if (uniform != null) {
            uniform.set(floatValueResult, floatValueItem);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry);
        }
        if (uniformResult != null) {
            uniformResult.set(1.0F);
        }
        if (uniformItem != null) {
            int intValue2 = intValue >> 24 & 0xFF;
            if (intValue2 == 0) {
                intValue2 = 255;
            }
            uniformItem.set((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, intValue2 / 255.0F);
        }
        if (uniformEntry != null) {
            int intValue3 = intValueValue >> 24 & 0xFF;
            if (intValue3 == 0) {
                intValue3 = 255;
            }
            uniformEntry.set((intValueValue >> 16 & 0xFF) / 255.0F, (intValueValue >> 8 & 0xFF) / 255.0F, (intValueValue & 0xFF) / 255.0F, intValue3 / 255.0F);
        }
        if (uniformCopy != null) {
            int intValue4 = intValueResult >> 24 & 0xFF;
            if (intValue4 == 0) {
                intValue4 = 255;
            }
            uniformCopy.set((intValueResult >> 16 & 0xFF) / 255.0F, (intValueResult >> 8 & 0xFF) / 255.0F, (intValueResult & 0xFF) / 255.0F, intValue4 / 255.0F);
        }
        if (uniform2 != null) {
            int intValue5 = intValueItem >> 24 & 0xFF;
            if (intValue5 == 0) {
                intValue5 = 255;
            }
            uniform2.set((intValueItem >> 16 & 0xFF) / 255.0F, (intValueItem >> 8 & 0xFF) / 255.0F, (intValueItem & 0xFF) / 255.0F, intValue5 / 255.0F);
        }
        if (uniform3 != null) {
            int intValue6 = intValueEntry >> 24 & 0xFF;
            if (intValue6 == 0) {
                intValue6 = 255;
            }
            uniform3.set((intValueEntry >> 16 & 0xFF) / 255.0F, (intValueEntry >> 8 & 0xFF) / 255.0F, (intValueEntry & 0xFF) / 255.0F, intValue6 / 255.0F);
        }
        if (uniform4 != null) {
            int intValue7 = intValueCopy >> 24 & 0xFF;
            if (intValue7 == 0) {
                intValue7 = 255;
            }
            uniform4.set((intValueCopy >> 16 & 0xFF) / 255.0F, (intValueCopy >> 8 & 0xFF) / 255.0F, (intValueCopy & 0xFF) / 255.0F, intValue7 / 255.0F);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(ShaderPrograms.gradient6Rect);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shadowRect);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float floatValue2 = floatValueResult + floatValueCopy * 2.0F;
        float floatValue3 = floatValueItem + floatValueCopy * 2.0F;
        float floatValue4 = floatValue - floatValueCopy;
        float floatValue5 = floatValueValue - floatValueCopy;
        GlUniform uniform = orCreateProgram.getUniform("Size");
        GlUniform uniformValue = orCreateProgram.getUniform("Softness");
        GlUniform uniformResult = orCreateProgram.getUniform("Radius");
        GlUniform uniformItem = orCreateProgram.getUniform("TopLeftColor");
        GlUniform uniformEntry = orCreateProgram.getUniform("TopRightColor");
        GlUniform uniformCopy = orCreateProgram.getUniform("BottomLeftColor");
        GlUniform uniform2 = orCreateProgram.getUniform("BottomRightColor");
        if (uniform != null) {
            uniform.set(floatValue2, floatValue3);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueCopy);
        }
        if (uniformResult != null) {
            uniformResult.set(floatValueEntry);
        }
        if (uniformItem != null) {
            int intValueEntry = intValue >> 24 & 0xFF;
            if (intValueEntry == 0) {
                intValueEntry = 255;
            }
            uniformItem.set((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, intValueEntry / 255.0F);
        }
        if (uniformEntry != null) {
            int intValueCopy = intValueValue >> 24 & 0xFF;
            if (intValueCopy == 0) {
                intValueCopy = 255;
            }
            uniformEntry.set((intValueValue >> 16 & 0xFF) / 255.0F, (intValueValue >> 8 & 0xFF) / 255.0F, (intValueValue & 0xFF) / 255.0F, intValueCopy / 255.0F);
        }
        if (uniformCopy != null) {
            int intValue2 = intValueResult >> 24 & 0xFF;
            if (intValue2 == 0) {
                intValue2 = 255;
            }
            uniformCopy.set((intValueResult >> 16 & 0xFF) / 255.0F, (intValueResult >> 8 & 0xFF) / 255.0F, (intValueResult & 0xFF) / 255.0F, intValue2 / 255.0F);
        }
        if (uniform2 != null) {
            int intValue3 = intValueItem >> 24 & 0xFF;
            if (intValue3 == 0) {
                intValue3 = 255;
            }
            uniform2.set((intValueItem >> 16 & 0xFF) / 255.0F, (intValueItem >> 8 & 0xFF) / 255.0F, (intValueItem & 0xFF) / 255.0F, intValue3 / 255.0F);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(positionMatrix, floatValue4, floatValue5, 0.0F).texture(0.0F, 0.0F);
        buffer.vertex(positionMatrix, floatValue4, floatValue5 + floatValue3, 0.0F).texture(0.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue4 + floatValue2, floatValue5 + floatValue3, 0.0F).texture(1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue4 + floatValue2, floatValue5, 0.0F).texture(1.0F, 0.0F);
        RenderSystem.setShader(ShaderPrograms.shadowRect);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, 10.0F, intValue, intValueValue, intValueResult, intValueItem);
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue) {
        drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, intValue, intValue, intValue, intValue);
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue) {
        drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, 10.0F, intValue, intValue, intValue, intValue);
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, 0.0F, 10.0F, intValue, intValue, intValue, intValue);
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue) {
        drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, intValue, intValue, intValueValue, intValueValue);
    }

    public static void drawShadowHorizontal(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue) {
        drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, intValue, intValueValue, intValue, intValueValue);
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue) {
        drawShadow(matrices, floatValue + floatValue2, floatValueValue + floatValue3, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, intValue, intValue, intValue, intValue);
    }

    public static void drawShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        drawShadow(matrices, floatValue + floatValue2, floatValueValue + floatValue3, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, intValue, intValueValue, intValueResult, intValueItem);
    }

    public static void drawShadow6(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shadow6Rect);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float floatValue2 = floatValueResult + floatValueCopy * 2.0F;
        float floatValue3 = floatValueItem + floatValueCopy * 2.0F;
        float floatValue4 = floatValue - floatValueCopy;
        float floatValue5 = floatValueValue - floatValueCopy;
        GlUniform uniform = orCreateProgram.getUniform("Size");
        GlUniform uniformValue = orCreateProgram.getUniform("Softness");
        GlUniform uniformResult = orCreateProgram.getUniform("Radius");
        GlUniform uniformItem = orCreateProgram.getUniform("LeftTopColor");
        GlUniform uniformEntry = orCreateProgram.getUniform("LeftBottomColor");
        GlUniform uniformCopy = orCreateProgram.getUniform("CenterTopColor");
        GlUniform uniform2 = orCreateProgram.getUniform("CenterBottomColor");
        GlUniform uniform3 = orCreateProgram.getUniform("RightTopColor");
        GlUniform uniform4 = orCreateProgram.getUniform("RightBottomColor");
        if (uniform != null) {
            uniform.set(floatValue2, floatValue3);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueCopy);
        }
        if (uniformResult != null) {
            uniformResult.set(floatValueEntry);
        }
        if (uniformItem != null) {
            int intValue2 = intValue >> 24 & 0xFF;
            if (intValue2 == 0) {
                intValue2 = 255;
            }
            uniformItem.set((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, intValue2 / 255.0F);
        }
        if (uniformEntry != null) {
            int intValue3 = intValueValue >> 24 & 0xFF;
            if (intValue3 == 0) {
                intValue3 = 255;
            }
            uniformEntry.set((intValueValue >> 16 & 0xFF) / 255.0F, (intValueValue >> 8 & 0xFF) / 255.0F, (intValueValue & 0xFF) / 255.0F, intValue3 / 255.0F);
        }
        if (uniformCopy != null) {
            int intValue4 = intValueResult >> 24 & 0xFF;
            if (intValue4 == 0) {
                intValue4 = 255;
            }
            uniformCopy.set((intValueResult >> 16 & 0xFF) / 255.0F, (intValueResult >> 8 & 0xFF) / 255.0F, (intValueResult & 0xFF) / 255.0F, intValue4 / 255.0F);
        }
        if (uniform2 != null) {
            int intValue5 = intValueItem >> 24 & 0xFF;
            if (intValue5 == 0) {
                intValue5 = 255;
            }
            uniform2.set((intValueItem >> 16 & 0xFF) / 255.0F, (intValueItem >> 8 & 0xFF) / 255.0F, (intValueItem & 0xFF) / 255.0F, intValue5 / 255.0F);
        }
        if (uniform3 != null) {
            int intValue6 = intValueEntry >> 24 & 0xFF;
            if (intValue6 == 0) {
                intValue6 = 255;
            }
            uniform3.set((intValueEntry >> 16 & 0xFF) / 255.0F, (intValueEntry >> 8 & 0xFF) / 255.0F, (intValueEntry & 0xFF) / 255.0F, intValue6 / 255.0F);
        }
        if (uniform4 != null) {
            int intValue7 = intValueCopy >> 24 & 0xFF;
            if (intValue7 == 0) {
                intValue7 = 255;
            }
            uniform4.set((intValueCopy >> 16 & 0xFF) / 255.0F, (intValueCopy >> 8 & 0xFF) / 255.0F, (intValueCopy & 0xFF) / 255.0F, intValue7 / 255.0F);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(positionMatrix, floatValue4, floatValue5, 0.0F).texture(0.0F, 0.0F);
        buffer.vertex(positionMatrix, floatValue4, floatValue5 + floatValue3, 0.0F).texture(0.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue4 + floatValue2, floatValue5 + floatValue3, 0.0F).texture(1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue4 + floatValue2, floatValue5, 0.0F).texture(1.0F, 0.0F);
        RenderSystem.setShader(ShaderPrograms.shadow6Rect);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawTexture(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        int intValueValue = intValue >> 24 & 0xFF;
        float floatValue4 = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValue5 = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValue6 = (intValue & 0xFF) / 255.0F;
        float floatValue7 = intValueValue / 255.0F;
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(floatValueEntry, floatValueCopy).color(floatValue4, floatValue5, floatValue6, floatValue7);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValueEntry, floatValue3).color(floatValue4, floatValue5, floatValue6, floatValue7);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(floatValue2, floatValue3).color(floatValue4, floatValue5, floatValue6, floatValue7);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue2, floatValueCopy).color(floatValue4, floatValue5, floatValue6, floatValue7);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
    }

    public static void drawImage(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        drawTexture(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, 0.0F, 0.0F, 1.0F, 1.0F, intValue);
    }

    public static void drawImage(MatrixStack matrices, String text, String textValue, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        drawImage(matrices, Identifier.of(text, textValue), floatValue, floatValueValue, floatValueResult, floatValueItem, intValue);
    }

    public static void drawRoundedImage(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        if (texture != null && !(floatValueResult <= 0.0F) && !(floatValueItem <= 0.0F) && !(floatValueCopy <= 0.0F)) {
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.roundedTextureOpacity);
            if (orCreateProgram != null) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderTexture(0, texture);
                GlUniform uniform = orCreateProgram.getUniform("Size");
                GlUniform uniformValue = orCreateProgram.getUniform("Radius");
                GlUniform uniformResult = orCreateProgram.getUniform("Smoothness");
                GlUniform uniformItem = orCreateProgram.getUniform("ColorModulator");
                GlUniform uniformEntry = orCreateProgram.getUniform("Opacity");
                GlUniform uniformCopy = orCreateProgram.getUniform("ExactBounds");
                if (uniform != null) {
                    uniform.set(floatValueResult, floatValueItem);
                }
                if (uniformValue != null) {
                    uniformValue.set(floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry);
                }
                if (uniformResult != null) {
                    uniformResult.set(1.0F / (float) mc.getWindow().getScaleFactor());
                }
                if (uniformItem != null) {
                    uniformItem.set(1.0F, 1.0F, 1.0F, 1.0F);
                }
                if (uniformEntry != null) {
                    uniformEntry.set(MathHelper.clamp(floatValueCopy, 0.0F, 1.0F));
                }
                if (uniformCopy != null) {
                    uniformCopy.set(1.0F);
                }
                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(0.0F, 0.0F).color(255, 255, 255, 0);
                buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(0.0F, 1.0F).color(255, 255, 255, 0);
                buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(1.0F, 1.0F).color(255, 255, 255, 0);
                buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(1.0F, 0.0F).color(255, 255, 255, 0);
                RenderSystem.setShader(ShaderPrograms.roundedTextureOpacity);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
                if (uniformCopy != null) {
                    uniformCopy.set(0.0F);
                }
                RenderSystem.setShaderTexture(0, 0);
                RenderSystem.disableBlend();
            }
        }
    }

    public static void drawSprite(MatrixStack matrices, Sprite sprite, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        drawTexture(matrices, sprite.getAtlasId(), floatValue, floatValueValue, floatValueResult, floatValueResult, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), intValue);
    }

    public static void drawPlayerHead(MatrixStack matrices, PlayerEntity player, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry) {
        drawPlayerHead(matrices, player, floatValue, floatValueValue, floatValueResult, floatValueItem, 1.0F, floatValueEntry);
    }

    public static void drawPlayerHead(MatrixStack matrices, PlayerEntity player, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        if (player != null) {
            Identifier texture = createTextureFromSkinTexturesWithTexture(player);
            renderRoundedPlayerFace(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueItem, floatValueItem, floatValueItem, floatValueEntry, floatValueCopy);
        }
    }

    public static void drawPlayerHead(MatrixStack matrices, String text, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        drawPlayerHead(matrices, text, floatValue, floatValueValue, floatValueResult, floatValueItem, 1.0F, 0.0F);
    }

    public static void drawPlayerHead(MatrixStack matrices, String text, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        if (text != null && !text.isEmpty()) {
            Identifier texture = createTextureFromSkinTexturesWithName(text);
            updateMatrixStack(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy);
        }
    }

    public static void drawPlayerHead(MatrixStack matrices, UUID uuid, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        drawPlayerHead(matrices, uuid, floatValue, floatValueValue, floatValueResult, floatValueItem, 1.0F, 0.0F);
    }

    public static void drawPlayerHead(MatrixStack matrices, UUID uuid, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        drawPlayerHead(matrices, uuid, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueItem, floatValueItem, floatValueItem, floatValueEntry, floatValueCopy);
    }

    public static void drawPlayerHead(MatrixStack matrices, UUID uuid, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4) {
        if (uuid != null) {
            Identifier texture = createTextureFromSkinTexturesWithTexture(uuid);
            renderRoundedPlayerFace(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, floatValue4);
        }
    }

    public static void drawPlayerHead(MatrixStack matrices, PlayerListEntry playerListEntry, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        drawPlayerHead(matrices, playerListEntry, floatValue, floatValueValue, floatValueResult, floatValueItem, 1.0F, 0.0F);
    }

    public static void drawPlayerHead(MatrixStack matrices, PlayerListEntry playerListEntry, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        if (playerListEntry != null) {
            Identifier texture = null;
            try {
                if (playerListEntry.getSkinTextures() != null) {
                    texture = playerListEntry.getSkinTextures().texture();
                }
            } catch (Exception failure) {
            }
            if (texture == null) {
                try {
                    texture = DefaultSkinHelper.getSkinTextures(playerListEntry.getProfile().getId()).texture();
                } catch (Exception failureValue) {
                }
            }
            if (texture == null) {
                texture = DefaultSkinHelper.getSkinTextures(uUID).texture();
            }
            renderRoundedPlayerFace(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueItem, floatValueItem, floatValueItem, floatValueEntry, floatValueCopy);
        }
    }

    public static void drawPlayerHead(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        renderRoundedPlayerFace(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueItem, floatValueItem, floatValueItem, 1.0F, 0.0F);
    }

    private static void updateMatrixStack(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        renderRoundedPlayerFace(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueItem, floatValueItem, floatValueItem, floatValueEntry, floatValueCopy);
    }

    private static void renderRoundedPlayerFace(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4) {
        if (texture == null) {
            texture = DefaultSkinHelper.getSkinTextures(uUID).texture();
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, texture);
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.face);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        GlUniform uniform = orCreateProgram.getUniform("location");
        GlUniform uniformValue = orCreateProgram.getUniform("size");
        GlUniform uniformResult = orCreateProgram.getUniform("radius");
        GlUniform uniformItem = orCreateProgram.getUniform("alpha");
        GlUniform uniformEntry = orCreateProgram.getUniform("u");
        GlUniform uniformCopy = orCreateProgram.getUniform("v");
        GlUniform uniform2 = orCreateProgram.getUniform("w");
        GlUniform uniform3 = orCreateProgram.getUniform("h");
        GlUniform uniform4 = orCreateProgram.getUniform("hurtPercent");
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueResult, floatValueResult);
        }
        if (uniformResult != null) {
            uniformResult.set(floatValueItem, floatValueEntry, floatValueCopy, floatValue2);
        }
        if (uniformItem != null) {
            uniformItem.set(floatValue3);
        }
        if (uniformEntry != null) {
            uniformEntry.set(0.125F);
        }
        if (uniformCopy != null) {
            uniformCopy.set(0.125F);
        }
        if (uniform2 != null) {
            uniform2.set(0.125F);
        }
        if (uniform3 != null) {
            uniform3.set(0.125F);
        }
        if (uniform4 != null) {
            uniform4.set(floatValue4);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(0.0F, 0.0F);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueResult, 0.0F).texture(0.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueResult, 0.0F).texture(1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(1.0F, 0.0F);
        RenderSystem.setShader(ShaderPrograms.face);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        renderFaceOverlay(matrices, texture, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, floatValue4);
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
    }

    private static void renderFaceOverlay(MatrixStack matrices, Identifier texture, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4) {
        RenderSystem.setShaderTexture(0, texture);
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.face);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        GlUniform uniform = orCreateProgram.getUniform("location");
        GlUniform uniformValue = orCreateProgram.getUniform("size");
        GlUniform uniformResult = orCreateProgram.getUniform("radius");
        GlUniform uniformItem = orCreateProgram.getUniform("alpha");
        GlUniform uniformEntry = orCreateProgram.getUniform("u");
        GlUniform uniformCopy = orCreateProgram.getUniform("v");
        GlUniform uniform2 = orCreateProgram.getUniform("w");
        GlUniform uniform3 = orCreateProgram.getUniform("h");
        GlUniform uniform4 = orCreateProgram.getUniform("hurtPercent");
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueResult, floatValueResult);
        }
        if (uniformResult != null) {
            uniformResult.set(floatValueItem, floatValueEntry, floatValueCopy, floatValue2);
        }
        if (uniformItem != null) {
            uniformItem.set(floatValue3);
        }
        if (uniformEntry != null) {
            uniformEntry.set(0.625F);
        }
        if (uniformCopy != null) {
            uniformCopy.set(0.125F);
        }
        if (uniform2 != null) {
            uniform2.set(0.125F);
        }
        if (uniform3 != null) {
            uniform3.set(0.125F);
        }
        if (uniform4 != null) {
            uniform4.set(floatValue4);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(0.0F, 0.0F);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueResult, 0.0F).texture(0.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueResult, 0.0F).texture(1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(1.0F, 0.0F);
        RenderSystem.setShader(ShaderPrograms.face);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static Identifier createTextureFromSkinTexturesWithTexture(PlayerEntity player) {
        if (player == null) {
            return DefaultSkinHelper.getSkinTextures(uUID).texture();
        } else {
            if (player instanceof AbstractClientPlayerEntity playerValue) {
                try {
                    Identifier texture = playerValue.getSkinTextures().texture();
                    if (texture != null) {
                        return texture;
                    }
                } catch (Exception failure) {
                }
            }
            try {
                if (mc.getNetworkHandler() != null) {
                    PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
                    if (playerListEntry != null && playerListEntry.getSkinTextures() != null) {
                        Identifier textureValue = playerListEntry.getSkinTextures().texture();
                        if (textureValue != null) {
                            return textureValue;
                        }
                    }
                }
            } catch (Exception failureValue) {
            }
            try {
                return DefaultSkinHelper.getSkinTextures(player.getUuid()).texture();
            } catch (Exception failureResult) {
                return DefaultSkinHelper.getSkinTextures(uUID).texture();
            }
        }
    }

    private static Identifier createTextureFromSkinTexturesWithName(String text) {
        if (text != null && !text.isEmpty()) {
            String normalizedText = text.toLowerCase(Locale.ROOT);
            Identifier texture = containsKeyIdentifierEntries.get(normalizedText);
            if (texture != null) {
                return texture;
            } else {
                try {
                    if (mc.getNetworkHandler() != null) {
                        for (PlayerListEntry playerListEntry : mc.getNetworkHandler().getPlayerList()) {
                            if (playerListEntry != null && playerListEntry.getProfile() != null && playerListEntry.getProfile().getName() != null && playerListEntry.getProfile().getName().equalsIgnoreCase(text) && playerListEntry.getSkinTextures() != null) {
                                Identifier textureValue = playerListEntry.getSkinTextures().texture();
                                if (textureValue != null) {
                                    containsKeyIdentifierEntries.put(normalizedText, textureValue);
                                    return textureValue;
                                }
                            }
                        }
                    }
                } catch (Exception failure) {
                }
                try {
                    if (mc.world != null) {
                        for (PlayerEntity player : mc.world.getPlayers()) {
                            if (player != null && player.getName() != null && player.getName().getString().equalsIgnoreCase(text)) {
                                Identifier textureResult = createTextureFromSkinTexturesWithTexture(player);
                                if (textureResult != null) {
                                    containsKeyIdentifierEntries.put(normalizedText, textureResult);
                                    return textureResult;
                                }
                            }
                        }
                    }
                } catch (Exception failureValue) {
                }
                try {
                    Identifier textureItem = DefaultSkinHelper.getSkinTextures(UUID.nameUUIDFromBytes(text.getBytes())).texture();
                    if (textureItem != null) {
                        containsKeyIdentifierEntries.put(normalizedText, textureItem);
                        return textureItem;
                    }
                } catch (Exception failureResult) {
                }
                return DefaultSkinHelper.getSkinTextures(uUID).texture();
            }
        } else {
            return DefaultSkinHelper.getSkinTextures(uUID).texture();
        }
    }

    private static Identifier createTextureFromSkinTexturesWithTexture(UUID uuid) {
        if (uuid == null) {
            return DefaultSkinHelper.getSkinTextures(uUID).texture();
        } else {
            String text = uuid.toString();
            if (containsKeyIdentifierEntries.containsKey(text)) {
                Identifier texture = containsKeyIdentifierEntries.get(text);
                if (texture != null) {
                    return texture;
                }
            }
            try {
                if (mc.getNetworkHandler() != null) {
                    PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(uuid);
                    if (playerListEntry != null && playerListEntry.getSkinTextures() != null) {
                        Identifier textureValue = playerListEntry.getSkinTextures().texture();
                        if (textureValue != null) {
                            containsKeyIdentifierEntries.put(text, textureValue);
                            return textureValue;
                        }
                    }
                }
            } catch (Exception failure) {
            }
            try {
                if (mc.world != null) {
                    PlayerEntity playerByUuid = mc.world.getPlayerByUuid(uuid);
                    if (playerByUuid != null) {
                        Identifier textureResult = createTextureFromSkinTexturesWithTexture(playerByUuid);
                        if (textureResult != null) {
                            containsKeyIdentifierEntries.put(text, textureResult);
                            return textureResult;
                        }
                    }
                }
            } catch (Exception failureValue) {
            }
            try {
                Identifier textureItem = DefaultSkinHelper.getSkinTextures(uuid).texture();
                if (textureItem != null) {
                    containsKeyIdentifierEntries.put(text, textureItem);
                    return textureItem;
                }
            } catch (Exception failureResult) {
            }
            return DefaultSkinHelper.getSkinTextures(uUID).texture();
        }
    }

    public static void clearSkinCache() {
        containsKeyIdentifierEntries.clear();
    }

    public static void removeSkinFromCache(String text) {
        containsKeyIdentifierEntries.remove(text.toLowerCase(Locale.ROOT));
    }

    public static void drawRoundedRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue) {
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue);
    }

    public static void drawDefaultHudElementRects(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        drawDefaultHudThemedPanel(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, 3.0F, 3.5F, intValue);
        drawRoundedRect(matrices, floatValue + floatValueResult - 14.5F, floatValueValue + 3.0F, 10.0F, 10.0F, 2.0F, ColorMathUtil.darken(intValue, 0.4F));
    }

    public static void drawDefaultHudInfoBox(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue) {
        drawRoundedRect(matrices, floatValue - 0.25F, floatValueValue - 1.25F, floatValueResult + 0.5F, 9.0F, 1.3F, intValue);
        drawRoundedRect(matrices, floatValue, floatValueValue - 1.0F, floatValueResult, 8.5F, 1.0F, intValueValue);
    }

    public static void drawDefaultHudPanel(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue, int intValueResult) {
        drawRoundedRect(matrices, floatValue - 0.5F, floatValueValue - 0.5F, floatValueResult + 1.0F, floatValueItem + 1.0F, floatValueCopy, intValue);
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, intValueValue, intValueResult);
    }

    public static void drawDefaultHudThemedPanel(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue) {
        int intValueValue = ColorMathUtil.alpha(intValue);
        drawDefaultHudPanel(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, ColorMathUtil.rgba(50, 50, 50, intValueValue), ColorMathUtil.darken(intValue, 0.15F), ColorMathUtil.darken(intValue, 0.05F));
    }

    public static void drawWaveHudHeader(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy) {
        drawShadow6(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueCopy, floatValue2, intValue, intValueValue, intValueResult, intValueItem, intValueEntry, intValueCopy);
        drawGradient6Rect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, intValue, intValueValue, intValueResult, intValueItem, intValueEntry, intValueCopy);
    }

    public static void drawWaveHudPanel(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy, int intValue2) {
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, 0.0F, intValue);
        drawWaveHudHeader(matrices, floatValue, floatValueValue, floatValueResult, floatValueEntry, floatValueCopy, floatValue2, floatValue3, intValueValue, intValueResult, intValueItem, intValueEntry, intValueCopy, intValue2);
    }

    public static void drawTargetHudWaveFrame(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2) {
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, 0.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(40, 40, 40, 255), floatValue2));
        drawRoundedRect(matrices, floatValue + floatValueEntry, floatValueValue + floatValueEntry, floatValueResult - floatValueEntry * 2.0F, floatValueItem - floatValueEntry * 2.0F, 0.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(20, 20, 20, 255), floatValue2));
        drawRoundedRect(matrices, floatValue + floatValueEntry + 2.0F, floatValueValue + floatValueEntry + 2.0F, floatValueCopy, floatValueCopy, 0.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(40, 40, 40, 255), floatValue2));
        drawRoundedRect(matrices, floatValue + floatValueEntry + 3.0F, floatValueValue + floatValueEntry + 3.0F, floatValueCopy - 2.0F, floatValueCopy - 2.0F, 0.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(25, 25, 25, 255), floatValue2));
    }

    public static void drawTargetHudDefaultPlaceholder(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult) {
        drawRoundedRect(matrices, floatValue - 1.0F, floatValueValue - 1.0F, 22.0F, 22.0F, 1.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(21, 21, 21, 255), floatValueResult));
    }

    public static void drawTargetHudHealthBars(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, float floatValueCopy) {
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult, 5.5F, 1.25F, ColorMathUtil.applyAlpha(ColorMathUtil.darken(intValue, 0.5F), floatValueCopy * 0.8F));
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult * floatValueItem, 5.5F, 1.25F, ColorMathUtil.applyAlpha(ColorMathUtil.darken(intValue, 0.8F), floatValueCopy * 0.8F));
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult * floatValueEntry, 5.5F, 1.25F, ColorMathUtil.applyAlpha(intValue, floatValueCopy), ColorMathUtil.applyAlpha(intValueValue, floatValueCopy), true);
    }

    public static void drawTargetHudGoldenBars(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3) {
        int intValue = ColorMathUtil.rgba(255, 215, 0, 255);
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult * floatValueEntry, floatValueItem, 1.25F, ColorMathUtil.applyAlpha(ColorMathUtil.darken(intValue, 0.65F), floatValue2 * floatValue3 * 0.8F));
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult * floatValueCopy, floatValueItem, 1.25F, ColorMathUtil.applyAlpha(ColorMathUtil.darken(intValue, 0.55F), floatValue2 * floatValue3), ColorMathUtil.applyAlpha(intValue, floatValue2 * floatValue3), true);
    }

    public static void drawTargetHudHeartBase(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult) {
        drawRoundedRect(matrices, floatValue, floatValueValue, 6.2F, 4.5F, 0.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(0, 0, 0, 255), floatValueResult));
    }

    public static void drawTargetHudHeartFill(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue) {
        drawShadow(matrices, floatValue + 1.0F, floatValueValue + 1.0F, floatValueResult, 2.0F, 0.0F, 8.0F, intValueValue);
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult + 1.2F, 4.5F, 0.0F, intValue);
    }

    public static void drawKeyStrokeRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue) {
        drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, intValue);
    }

    public static void drawRoundedRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.roundedRect);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        GlUniform uniform = orCreateProgram.getUniform("Size");
        GlUniform uniformValue = orCreateProgram.getUniform("Radius");
        if (uniform != null) {
            uniform.set(floatValueResult, floatValueItem);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueEntry, floatValueCopy, floatValue2, floatValue3);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int intValueValue = intValue >> 24 & 0xFF;
        float floatValue4 = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValue5 = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValue6 = (intValue & 0xFF) / 255.0F;
        float floatValue7 = intValueValue / 255.0F;
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(floatValue4, floatValue5, floatValue6, floatValue7);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).color(floatValue4, floatValue5, floatValue6, floatValue7);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).color(floatValue4, floatValue5, floatValue6, floatValue7);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(floatValue4, floatValue5, floatValue6, floatValue7);
        RenderSystem.setShader(ShaderPrograms.roundedRect);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawPixelRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        double doubleValue = Math.max(1.0, mc.getWindow().getScaleFactor());
        float floatValueEntry = (float) (1.0 / doubleValue);
        float floatValueCopy = (float) (Math.round(floatValue * doubleValue) / doubleValue);
        float floatValue2 = (float) (Math.round(floatValueValue * doubleValue) / doubleValue);
        float floatValue3 = Math.max(floatValueEntry, (float) (Math.round(floatValueResult * doubleValue) / doubleValue));
        float floatValue4 = Math.max(floatValueEntry, (float) (Math.round(floatValueItem * doubleValue) / doubleValue));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int intValueValue = intValue >> 24 & 0xFF;
        float floatValue5 = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValue6 = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValue7 = (intValue & 0xFF) / 255.0F;
        float floatValue8 = intValueValue / 255.0F;
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, floatValueCopy, floatValue2, 0.0F).color(floatValue5, floatValue6, floatValue7, floatValue8);
        buffer.vertex(positionMatrix, floatValueCopy, floatValue2 + floatValue4, 0.0F).color(floatValue5, floatValue6, floatValue7, floatValue8);
        buffer.vertex(positionMatrix, floatValueCopy + floatValue3, floatValue2 + floatValue4, 0.0F).color(floatValue5, floatValue6, floatValue7, floatValue8);
        buffer.vertex(positionMatrix, floatValueCopy + floatValue3, floatValue2, 0.0F).color(floatValue5, floatValue6, floatValue7, floatValue8);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawRoundCircle(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        drawRoundedRect(matrices, floatValue - floatValueResult / 2.0F, floatValueValue - floatValueResult / 2.0F, floatValueResult, floatValueResult, floatValueResult / 2.0F - 0.5F, intValue);
    }

    public static void drawRingArc(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue) {
        if (!(floatValueResult <= 0.0F) && !(floatValueItem <= 0.0F)) {
            float floatValue2 = floatValueResult / 2.0F;
            float floatValue3 = (float) Math.toRadians(floatValueEntry);
            float floatValue4 = (float) Math.toRadians(floatValueCopy);
            float floatValue5 = (float) (Math.PI * 2);
            if (floatValue3 < 0.0F) {
                floatValue3 += floatValue5;
            }
            if (floatValue4 < 0.0F) {
                floatValue4 += floatValue5;
            }
            while (floatValue4 < floatValue3) {
                floatValue4 += floatValue5;
            }
            if (floatValue4 - floatValue3 <= 1.0E-4F) {
                floatValue4 = floatValue3 + floatValue5;
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.ringArc);
            GlUniform uniform = orCreateProgram.getUniform("Size");
            GlUniform uniformValue = orCreateProgram.getUniform("Radius");
            GlUniform uniformResult = orCreateProgram.getUniform("Thickness");
            GlUniform uniformItem = orCreateProgram.getUniform("StartAngle");
            GlUniform uniformEntry = orCreateProgram.getUniform("EndAngle");
            GlUniform uniformCopy = orCreateProgram.getUniform("Smoothness");
            GlUniform uniform2 = orCreateProgram.getUniform("ColorModulator");
            if (uniform != null) {
                uniform.set(floatValueResult, floatValueResult);
            }
            if (uniformValue != null) {
                uniformValue.set(floatValue2);
            }
            if (uniformResult != null) {
                uniformResult.set(floatValueItem);
            }
            if (uniformItem != null) {
                uniformItem.set(floatValue3);
            }
            if (uniformEntry != null) {
                uniformEntry.set(floatValue4);
            }
            if (uniformCopy != null) {
                uniformCopy.set(Math.min(0.55F, floatValueItem * 0.25F));
            }
            if (uniform2 != null) {
                uniform2.set(1.0F, 1.0F, 1.0F, 1.0F);
            }
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            int intValueValue = intValue >> 24 & 0xFF;
            float floatValue6 = (intValue >> 16 & 0xFF) / 255.0F;
            float floatValue7 = (intValue >> 8 & 0xFF) / 255.0F;
            float floatValue8 = (intValue & 0xFF) / 255.0F;
            float floatValue9 = intValueValue / 255.0F;
            buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(floatValue6, floatValue7, floatValue8, floatValue9);
            buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueResult, 0.0F).color(floatValue6, floatValue7, floatValue8, floatValue9);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueResult, 0.0F).color(floatValue6, floatValue7, floatValue8, floatValue9);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(floatValue6, floatValue7, floatValue8, floatValue9);
            RenderSystem.setShader(ShaderPrograms.ringArc);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.disableBlend();
        }
    }

    public static void drawGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, intValue, intValueValue, intValueResult, intValueItem, false);
    }

    public static void drawGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue, int intValueValue, int intValueResult, int intValueItem, boolean flag) {
        renderUniformWithColor(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, intValue, intValueValue, intValueResult, intValueItem, flag, false);
    }

    public static void drawDiagonalGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue) {
        renderUniformWithColor(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue, intValue, intValueValue, intValueValue, true, true);
    }

    private static void renderUniformWithColor(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue, int intValueValue, int intValueResult, int intValueItem, boolean flag, boolean flagValue) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.gradientRect);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        GlUniform uniform = orCreateProgram.getUniform("Size");
        GlUniform uniformValue = orCreateProgram.getUniform("Radius");
        GlUniform uniformResult = orCreateProgram.getUniform("Smoothness");
        GlUniform uniformItem = orCreateProgram.getUniform("ColorModulator");
        GlUniform uniformEntry = orCreateProgram.getUniform("TopLeftColor");
        GlUniform uniformCopy = orCreateProgram.getUniform("BottomLeftColor");
        GlUniform uniform2 = orCreateProgram.getUniform("TopRightColor");
        GlUniform uniform3 = orCreateProgram.getUniform("BottomRightColor");
        GlUniform uniform4 = orCreateProgram.getUniform("Diagonal");
        if (uniform4 != null) {
            uniform4.set(flagValue ? 1.0F : 0.0F);
        }
        if (uniform != null) {
            uniform.set(floatValueResult, floatValueItem);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueEntry, floatValueCopy, floatValue2, floatValue3);
        }
        if (uniformResult != null) {
            uniformResult.set(1.0F);
        }
        if (uniformItem != null) {
            uniformItem.set(1.0F, 1.0F, 1.0F, 1.0F);
        }
        int intValueEntry = intValue >> 24 & 0xFF;
        if (intValueEntry == 0 && !flag) {
            intValueEntry = 255;
        }
        if (uniformEntry != null) {
            uniformEntry.set((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, intValueEntry / 255.0F);
        }
        int intValueCopy = intValueResult >> 24 & 0xFF;
        if (intValueCopy == 0 && !flag) {
            intValueCopy = 255;
        }
        if (uniformCopy != null) {
            uniformCopy.set((intValueResult >> 16 & 0xFF) / 255.0F, (intValueResult >> 8 & 0xFF) / 255.0F, (intValueResult & 0xFF) / 255.0F, intValueCopy / 255.0F);
        }
        int intValue2 = intValueValue >> 24 & 0xFF;
        if (intValue2 == 0 && !flag) {
            intValue2 = 255;
        }
        if (uniform2 != null) {
            uniform2.set((intValueValue >> 16 & 0xFF) / 255.0F, (intValueValue >> 8 & 0xFF) / 255.0F, (intValueValue & 0xFF) / 255.0F, intValue2 / 255.0F);
        }
        int intValue3 = intValueItem >> 24 & 0xFF;
        if (intValue3 == 0 && !flag) {
            intValue3 = 255;
        }
        if (uniform3 != null) {
            uniform3.set((intValueItem >> 16 & 0xFF) / 255.0F, (intValueItem >> 8 & 0xFF) / 255.0F, (intValueItem & 0xFF) / 255.0F, intValue3 / 255.0F);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(ShaderPrograms.gradientRect);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue, intValueValue, intValueResult, intValueItem);
    }

    public static void drawGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue) {
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue, intValue, intValueValue, intValueValue);
    }

    public static void drawGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue) {
        drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, 0.0F, 0.0F, 0.0F, 0.0F, intValue, intValue, intValueValue, intValueValue);
    }

    public static void drawGradientRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, boolean flag) {
        if (flag) {
            drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue, intValueValue, intValue, intValueValue);
        } else {
            drawGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue, intValue, intValueValue, intValueValue);
        }
    }

    public static void drawRoundedRectOutline(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4, int intValue) {
        drawRoundedRectOutline(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, floatValue4, intValue, intValue, intValue, intValue);
    }

    public static void drawRoundedRectOutline(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        drawRoundedRectOutline(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, floatValueCopy, intValue, intValueValue, intValueResult, intValueItem);
    }

    public static void drawRoundedRectOutline(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        renderUniformWithColor(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, floatValue4, intValue, intValueValue, intValueResult, intValueItem, false);
    }

    public static void drawRoundedRectOutline(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, boolean flag) {
        renderUniformWithColor(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, floatValueCopy, intValue, intValue, intValue, intValue, flag);
    }

    private static void renderUniformWithColor(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4, int intValue, int intValueValue, int intValueResult, int intValueItem, boolean flag) {
        if (!(floatValue4 <= 0.0F)) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.roundedRectOutline);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            GlUniform uniform = orCreateProgram.getUniform("Size");
            GlUniform uniformValue = orCreateProgram.getUniform("Radius");
            GlUniform uniformResult = orCreateProgram.getUniform("Smoothness");
            GlUniform uniformItem = orCreateProgram.getUniform("ColorModulator");
            GlUniform uniformEntry = orCreateProgram.getUniform("Outline");
            GlUniform uniformCopy = orCreateProgram.getUniform("ExactBounds");
            GlUniform uniform2 = orCreateProgram.getUniform("TopLeftColor");
            GlUniform uniform3 = orCreateProgram.getUniform("BottomLeftColor");
            GlUniform uniform4 = orCreateProgram.getUniform("TopRightColor");
            GlUniform uniform5 = orCreateProgram.getUniform("BottomRightColor");
            if (uniform != null) {
                uniform.set(floatValueResult, floatValueItem);
            }
            if (uniformValue != null) {
                uniformValue.set(floatValueEntry, floatValueCopy, floatValue2, floatValue3);
            }
            if (uniformResult != null) {
                uniformResult.set(flag ? 1.0F / (float) mc.getWindow().getScaleFactor() : 1.0F);
            }
            if (uniformItem != null) {
                uniformItem.set(1.0F, 1.0F, 1.0F, 1.0F);
            }
            if (uniformEntry != null) {
                uniformEntry.set(floatValue4);
            }
            if (uniformCopy != null) {
                uniformCopy.set(flag ? 1.0F : 0.0F);
            }
            if (uniform2 != null) {
                int intValueEntry = intValue >> 24 & 0xFF;
                if (intValueEntry == 0) {
                    intValueEntry = 255;
                }
                uniform2.set((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, intValueEntry / 255.0F);
            }
            if (uniform3 != null) {
                int intValueCopy = intValueResult >> 24 & 0xFF;
                if (intValueCopy == 0) {
                    intValueCopy = 255;
                }
                uniform3.set((intValueResult >> 16 & 0xFF) / 255.0F, (intValueResult >> 8 & 0xFF) / 255.0F, (intValueResult & 0xFF) / 255.0F, intValueCopy / 255.0F);
            }
            if (uniform4 != null) {
                int intValue2 = intValueValue >> 24 & 0xFF;
                if (intValue2 == 0) {
                    intValue2 = 255;
                }
                uniform4.set((intValueValue >> 16 & 0xFF) / 255.0F, (intValueValue >> 8 & 0xFF) / 255.0F, (intValueValue & 0xFF) / 255.0F, intValue2 / 255.0F);
            }
            if (uniform5 != null) {
                int intValue3 = intValueItem >> 24 & 0xFF;
                if (intValue3 == 0) {
                    intValue3 = 255;
                }
                uniform5.set((intValueItem >> 16 & 0xFF) / 255.0F, (intValueItem >> 8 & 0xFF) / 255.0F, (intValueItem & 0xFF) / 255.0F, intValue3 / 255.0F);
            }
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(ShaderPrograms.roundedRectOutline);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            if (uniformCopy != null) {
                uniformCopy.set(0.0F);
            }
            RenderSystem.disableBlend();
        }
    }

    private static ShaderProgram createUniformWithOrCreateProgram() {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.roundedTexture);
        if (shaderProgram != orCreateProgram && orCreateProgram != null) {
            shaderProgram = orCreateProgram;
            sizeUniform = orCreateProgram.getUniform("Size");
            radiusUniform = orCreateProgram.getUniform("Radius");
            smoothnessUniform = orCreateProgram.getUniform("Smoothness");
            colorModulatorUniform = orCreateProgram.getUniform("ColorModulator");
        }
        return orCreateProgram;
    }

    public static void drawBlur(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue) {
        BlurFramebufferManager.getInstance().request();
        if (BlurFramebufferManager.getBuffer2() != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            createUniformWithOrCreateProgram();
            if (sizeUniform != null) {
                sizeUniform.set(floatValueResult, floatValueItem);
            }
            if (radiusUniform != null) {
                radiusUniform.set(floatValueEntry, floatValueCopy, floatValue2, floatValue3);
            }
            if (smoothnessUniform != null) {
                smoothnessUniform.set(0.5F);
            }
            if (colorModulatorUniform != null) {
                colorModulatorUniform.set(1.0F, 1.0F, 1.0F, 1.0F);
            }
            RenderSystem.setShaderTexture(0, BlurFramebufferManager.getTexture());
            RenderSystem.setShader(ShaderPrograms.roundedTexture);
            int scaledWidth = mc.getWindow().getScaledWidth();
            int scaledHeight = mc.getWindow().getScaledHeight();
            float floatValue4 = floatValue / scaledWidth;
            float floatValue5 = (scaledHeight - floatValueValue) / scaledHeight;
            float floatValue6 = (floatValue + floatValueResult) / scaledWidth;
            float floatValue7 = (scaledHeight - floatValueValue - floatValueItem) / scaledHeight;
            int intValueValue = intValue >> 24 & 0xFF;
            float floatValue8 = (intValue >> 16 & 0xFF) / 255.0F;
            float floatValue9 = (intValue >> 8 & 0xFF) / 255.0F;
            float floatValue10 = (intValue & 0xFF) / 255.0F;
            float floatValue11 = intValueValue / 255.0F;
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(floatValue4, floatValue5).color(floatValue8, floatValue9, floatValue10, floatValue11);
            buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValue4, floatValue7).color(floatValue8, floatValue9, floatValue10, floatValue11);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(floatValue6, floatValue7).color(floatValue8, floatValue9, floatValue10, floatValue11);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue6, floatValue5).color(floatValue8, floatValue9, floatValue10, floatValue11);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.disableBlend();
        }
    }

    public static void drawBlur(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue) {
        drawBlur(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, intValue);
    }

    public static void startGlow(float floatValue, int intValue, RenderAction renderAction, MatrixStack matrices) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        if (intValueValue == 0) {
            intValueValue = 255;
        }
        GlowRenderer.getInstance().begin(floatValue, new Color(intValueResult, intValueItem, intValueEntry, intValueValue));
        renderAction.render();
        GlowRenderer.getInstance().end(matrices, renderAction);
    }

    public static void startGlow(float floatValue, float floatValueValue, int intValue, RenderAction renderAction, MatrixStack matrices) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        if (intValueValue == 0) {
            intValueValue = 255;
        }
        GlowRenderer.getInstance().begin(floatValue, floatValueValue, new Color(intValueResult, intValueItem, intValueEntry, intValueValue));
        renderAction.render();
        GlowRenderer.getInstance().end(matrices, renderAction);
    }

    public static void drawBlur(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4, int intValue) {
        BlurFramebufferManager.getInstance().request();
        if (BlurFramebufferManager.getBuffer2() != null) {
            BlurFramebufferManager.getInstance().setBlurOffset(floatValue4);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            createUniformWithOrCreateProgram();
            if (sizeUniform != null) {
                sizeUniform.set(floatValueResult, floatValueItem);
            }
            if (radiusUniform != null) {
                radiusUniform.set(floatValueEntry, floatValueCopy, floatValue2, floatValue3);
            }
            if (smoothnessUniform != null) {
                smoothnessUniform.set(0.5F);
            }
            if (colorModulatorUniform != null) {
                colorModulatorUniform.set(1.0F, 1.0F, 1.0F, 1.0F);
            }
            RenderSystem.setShaderTexture(0, BlurFramebufferManager.getTexture());
            RenderSystem.setShader(ShaderPrograms.roundedTexture);
            int scaledWidth = mc.getWindow().getScaledWidth();
            int scaledHeight = mc.getWindow().getScaledHeight();
            float floatValue5 = floatValue / scaledWidth;
            float floatValue6 = (scaledHeight - floatValueValue) / scaledHeight;
            float floatValue7 = (floatValue + floatValueResult) / scaledWidth;
            float floatValue8 = (scaledHeight - floatValueValue - floatValueItem) / scaledHeight;
            int intValueValue = intValue >> 24 & 0xFF;
            float floatValue9 = (intValue >> 16 & 0xFF) / 255.0F;
            float floatValue10 = (intValue >> 8 & 0xFF) / 255.0F;
            float floatValue11 = (intValue & 0xFF) / 255.0F;
            float floatValue12 = intValueValue / 255.0F;
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(floatValue5, floatValue6).color(floatValue9, floatValue10, floatValue11, floatValue12);
            buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValue5, floatValue8).color(floatValue9, floatValue10, floatValue11, floatValue12);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(floatValue7, floatValue8).color(floatValue9, floatValue10, floatValue11, floatValue12);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue7, floatValue6).color(floatValue9, floatValue10, floatValue11, floatValue12);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.disableBlend();
        }
    }

    public static void drawBlur(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue) {
        drawBlur(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry, floatValueCopy, intValue);
    }

    public static void drawBlurWithOpacity(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, float floatValue2) {
        if (!(floatValue2 <= 0.0F)) {
            BlurFramebufferManager.getInstance().request();
            if (BlurFramebufferManager.getBuffer2() != null) {
                BlurFramebufferManager.getInstance().setBlurOffset(floatValueCopy);
                renderUniformWithColor(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, intValue, floatValue2, BlurFramebufferManager.getTexture());
            }
        }
    }

    public static void drawHudBlur(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        drawHudGlow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy);
        if (BackgroundSettings.isBlurEnabled() && !(floatValueCopy <= 0.0F)) {
            BlurFramebufferManager.getHudInstance().request();
            if (BlurFramebufferManager.getHudBuffer() != null) {
                renderUniformWithColor(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, 0, floatValueCopy, BlurFramebufferManager.getHudTexture());
            }
        }
    }

    public static void drawHudGlow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        float clampedValue = MathHelper.clamp(BackgroundSettings.GLOW_STRENGTH.get(), 0.0F, 5.0F);
        if (BackgroundSettings.GLOW.isState() && !(clampedValue <= 0.0F) && !(floatValueCopy <= 0.0F) && !(floatValueResult <= 0.0F) && !(floatValueItem <= 0.0F)) {
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.hudGlow);
            if (orCreateProgram != null) {
                float floatValue2 = 0.5F + clampedValue * 2.0F;
                float floatValue3 = floatValueResult + floatValue2 * 2.0F;
                float floatValue4 = floatValueItem + floatValue2 * 2.0F;
                GlUniform uniform = orCreateProgram.getUniform("Size");
                GlUniform uniformValue = orCreateProgram.getUniform("Softness");
                GlUniform uniformResult = orCreateProgram.getUniform("Radius");
                GlUniform uniformItem = orCreateProgram.getUniform("GlowColor");
                if (uniform != null) {
                    uniform.set(floatValue3, floatValue4);
                }
                if (uniformValue != null) {
                    uniformValue.set(floatValue2);
                }
                if (uniformResult != null) {
                    uniformResult.set(MathHelper.clamp(floatValueEntry, 0.0F, Math.min(floatValueResult, floatValueItem) * 0.5F));
                }
                if (uniformItem != null) {
                    uniformItem.set(0.0F, 0.0F, 0.0F, MathHelper.clamp(floatValueCopy, 0.0F, 1.0F) * (0.15F + clampedValue * 0.1F));
                }
                float floatValue5 = floatValue - floatValue2;
                float floatValue6 = floatValueValue - floatValue2;
                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(orCreateProgram);
                BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                buffer.vertex(positionMatrix, floatValue5, floatValue6, 0.0F).texture(0.0F, 0.0F);
                buffer.vertex(positionMatrix, floatValue5, floatValue6 + floatValue4, 0.0F).texture(0.0F, 1.0F);
                buffer.vertex(positionMatrix, floatValue5 + floatValue3, floatValue6 + floatValue4, 0.0F).texture(1.0F, 1.0F);
                buffer.vertex(positionMatrix, floatValue5 + floatValue3, floatValue6, 0.0F).texture(1.0F, 0.0F);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
                RenderSystem.disableBlend();
            }
        }
    }

    public static void drawHudShadow(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue) {
        int intValueValue = Math.round((intValue >>> 24 & 0xFF) * BackgroundSettings.getOverlayOpacity());
        if (intValueValue > 0) {
            drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, ColorMathUtil.setAlphaColor(intValue, intValueValue));
        }
    }

    private static void renderUniformWithColor(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, float floatValueCopy, int intValueValue) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.roundedTextureOpacity);
        GlUniform uniform = orCreateProgram.getUniform("Size");
        GlUniform uniformValue = orCreateProgram.getUniform("Radius");
        GlUniform uniformResult = orCreateProgram.getUniform("Smoothness");
        GlUniform uniformItem = orCreateProgram.getUniform("ColorModulator");
        GlUniform uniformEntry = orCreateProgram.getUniform("Opacity");
        if (uniform != null) {
            uniform.set(floatValueResult, floatValueItem);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueEntry, floatValueEntry, floatValueEntry, floatValueEntry);
        }
        if (uniformResult != null) {
            uniformResult.set(0.5F);
        }
        if (uniformItem != null) {
            uniformItem.set(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (uniformEntry != null) {
            uniformEntry.set(MathHelper.clamp(floatValueCopy, 0.0F, 1.0F));
        }
        int scaledWidth = mc.getWindow().getScaledWidth();
        int scaledHeight = mc.getWindow().getScaledHeight();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Vector3f vector3f = positionMatrix.transformPosition(new Vector3f(floatValue, floatValueValue, 0.0F));
        Vector3f vector3fValue = positionMatrix.transformPosition(new Vector3f(floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F));
        float floatValue2 = vector3f.x / scaledWidth;
        float floatValue3 = (scaledHeight - vector3f.y) / scaledHeight;
        float floatValue4 = vector3fValue.x / scaledWidth;
        float floatValue5 = (scaledHeight - vector3fValue.y) / scaledHeight;
        int intValueResult = intValue >> 24 & 0xFF;
        float floatValue6 = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValue7 = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValue8 = (intValue & 0xFF) / 255.0F;
        float floatValue9 = intValueResult / 255.0F;
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(floatValue2, floatValue3).color(floatValue6, floatValue7, floatValue8, floatValue9);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValue2, floatValue5).color(floatValue6, floatValue7, floatValue8, floatValue9);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(floatValue4, floatValue5).color(floatValue6, floatValue7, floatValue8, floatValue9);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue4, floatValue3).color(floatValue6, floatValue7, floatValue8, floatValue9);
        RenderSystem.setShaderTexture(0, intValueValue);
        RenderSystem.setShader(ShaderPrograms.roundedTextureOpacity);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
    }

    public static void drawRingArcGradient(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue) {
        if (!(floatValueResult <= 0.0F) && !(floatValueItem <= 0.0F)) {
            float floatValue2 = floatValueResult / 2.0F;
            float floatValue3 = (float) Math.toRadians(floatValueEntry);
            float floatValue4 = (float) Math.toRadians(floatValueCopy);
            float floatValue5 = (float) (Math.PI * 2);
            if (floatValue3 < 0.0F) {
                floatValue3 += floatValue5;
            }
            if (floatValue4 < 0.0F) {
                floatValue4 += floatValue5;
            }
            while (floatValue4 < floatValue3) {
                floatValue4 += floatValue5;
            }
            if (floatValue4 - floatValue3 <= 1.0E-4F) {
                floatValue4 = floatValue3 + floatValue5;
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.ringArc);
            GlUniform uniform = orCreateProgram.getUniform("Size");
            GlUniform uniformValue = orCreateProgram.getUniform("Radius");
            GlUniform uniformResult = orCreateProgram.getUniform("Thickness");
            GlUniform uniformItem = orCreateProgram.getUniform("StartAngle");
            GlUniform uniformEntry = orCreateProgram.getUniform("EndAngle");
            GlUniform uniformCopy = orCreateProgram.getUniform("Smoothness");
            GlUniform uniform2 = orCreateProgram.getUniform("ColorModulator");
            if (uniform != null) {
                uniform.set(floatValueResult, floatValueResult);
            }
            if (uniformValue != null) {
                uniformValue.set(floatValue2);
            }
            if (uniformResult != null) {
                uniformResult.set(floatValueItem);
            }
            if (uniformItem != null) {
                uniformItem.set(floatValue3);
            }
            if (uniformEntry != null) {
                uniformEntry.set(floatValue4);
            }
            if (uniformCopy != null) {
                uniformCopy.set(Math.min(0.55F, floatValueItem * 0.25F));
            }
            if (uniform2 != null) {
                uniform2.set(1.0F, 1.0F, 1.0F, 1.0F);
            }
            float floatValue6 = (intValue >> 16 & 0xFF) / 255.0F;
            float floatValue7 = (intValue >> 8 & 0xFF) / 255.0F;
            float floatValue8 = (intValue & 0xFF) / 255.0F;
            float floatValue9 = (intValue >> 24 & 0xFF) / 255.0F;
            float floatValue10 = (intValueValue >> 16 & 0xFF) / 255.0F;
            float floatValue11 = (intValueValue >> 8 & 0xFF) / 255.0F;
            float floatValue12 = (intValueValue & 0xFF) / 255.0F;
            float floatValue13 = (intValueValue >> 24 & 0xFF) / 255.0F;
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(floatValue6, floatValue7, floatValue8, floatValue9);
            buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueResult, 0.0F).color(floatValue10, floatValue11, floatValue12, floatValue13);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueResult, 0.0F).color(floatValue10, floatValue11, floatValue12, floatValue13);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(floatValue6, floatValue7, floatValue8, floatValue9);
            RenderSystem.setShader(ShaderPrograms.ringArc);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.disableBlend();
        }
    }

    public static void drawLiquidGlass(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue, float floatValue4, float floatValue5, int intValueValue, float floatValue6, boolean flag, float floatValue7, float floatValue8, float floatValue9, boolean flagValue) {
        int intValueResult;
        if (flagValue) {
            intValueResult = mc.getFramebuffer().getColorAttachment();
        } else {
            BlurFramebufferManager.getInstance().request();
            if (BlurFramebufferManager.getBuffer1() == null) {
                return;
            }
            intValueResult = BlurFramebufferManager.getTexture();
            if (intValueResult == 0) {
                return;
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, intValueResult);
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.liquidGlass);
        GlUniform uniform = orCreateProgram.getUniform("GlobalAlpha");
        GlUniform uniformValue = orCreateProgram.getUniform("Size");
        GlUniform uniformResult = orCreateProgram.getUniform("Radius");
        GlUniform uniformItem = orCreateProgram.getUniform("Smoothness");
        GlUniform uniformEntry = orCreateProgram.getUniform("FresnelPower");
        GlUniform uniformCopy = orCreateProgram.getUniform("FresnelColor");
        GlUniform uniform2 = orCreateProgram.getUniform("FresnelAlpha");
        GlUniform uniform3 = orCreateProgram.getUniform("BaseAlpha");
        GlUniform uniform4 = orCreateProgram.getUniform("FresnelInvert");
        GlUniform uniform5 = orCreateProgram.getUniform("FresnelMix");
        GlUniform uniform6 = orCreateProgram.getUniform("DistortStrength");
        GlUniform uniform7 = orCreateProgram.getUniform("CornerSmoothness");
        if (uniform != null) {
            uniform.set(floatValue4);
        }
        if (uniformValue != null) {
            uniformValue.set(floatValueResult, floatValueItem);
        }
        if (uniformResult != null) {
            uniformResult.set(floatValueEntry, floatValueCopy, floatValue2, floatValue3);
        }
        if (uniformItem != null) {
            uniformItem.set(0.5F);
        }
        if (uniformEntry != null) {
            uniformEntry.set(floatValue5);
        }
        int intValueItem = intValueValue >> 24 & 0xFF;
        if (intValueItem == 0) {
            intValueItem = 255;
        }
        if (uniformCopy != null) {
            uniformCopy.set((intValueValue >> 16 & 0xFF) / 255.0F, (intValueValue >> 8 & 0xFF) / 255.0F, (intValueValue & 0xFF) / 255.0F);
        }
        if (uniform2 != null) {
            uniform2.set(intValueItem / 255.0F);
        }
        if (uniform3 != null) {
            uniform3.set(floatValue6);
        }
        if (uniform4 != null) {
            uniform4.set(flag ? 1 : 0);
        }
        if (uniform5 != null) {
            uniform5.set(floatValue7);
        }
        if (uniform6 != null) {
            uniform6.set(floatValue8);
        }
        if (uniform7 != null) {
            uniform7.set(floatValue9);
        }
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float framebufferWidth = mc.getWindow().getFramebufferWidth();
        float framebufferHeight = mc.getWindow().getFramebufferHeight();
        float scaleFactor = (float) mc.getWindow().getScaleFactor();
        float floatValue10 = floatValue * scaleFactor;
        float floatValue11 = floatValueValue * scaleFactor;
        float floatValue12 = floatValueResult * scaleFactor;
        float floatValue13 = floatValueItem * scaleFactor;
        float floatValue14 = floatValue10 / framebufferWidth;
        float floatValue15 = 1.0F - floatValue11 / framebufferHeight;
        float floatValue16 = (floatValue10 + floatValue12) / framebufferWidth;
        float floatValue17 = 1.0F - (floatValue11 + floatValue13) / framebufferHeight;
        int intValueEntry = intValue >> 24 & 0xFF;
        float floatValue18 = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValue19 = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValue20 = (intValue & 0xFF) / 255.0F;
        float floatValue21 = intValueEntry / 255.0F;
        RenderSystem.setShader(ShaderPrograms.liquidGlass);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).texture(floatValue14, floatValue15).color(floatValue18, floatValue19, floatValue20, floatValue21);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValue14, floatValue17).color(floatValue18, floatValue19, floatValue20, floatValue21);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(floatValue16, floatValue17).color(floatValue18, floatValue19, floatValue20, floatValue21);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue16, floatValue15).color(floatValue18, floatValue19, floatValue20, floatValue21);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private Renderer2D() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
