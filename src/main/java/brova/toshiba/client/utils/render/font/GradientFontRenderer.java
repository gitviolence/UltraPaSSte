package brova.toshiba.client.utils.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Font;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class GradientFontRenderer extends StyledFontRenderer {

    public GradientFontRenderer(Font font, boolean flag, boolean flagValue) {
        super(font, flag, flagValue);
    }

    public int drawGradientString(String text, float floatValue, float floatValueValue, int intValue, int intValueValue, boolean flag, boolean flagValue) {
        text = this.patchText(text);
        int intValueResult;
        if (flag) {
            intValueResult = this.calculateText(text, floatValue + 1.0F, floatValueValue + 1.0F, intValue, intValueValue, true, flagValue);
            intValueResult = Math.max(intValueResult, this.calculateText(text, floatValue, floatValueValue, intValue, intValueValue, false, flagValue));
        } else {
            intValueResult = this.calculateText(text, floatValue, floatValueValue, intValue, intValueValue, false, flagValue);
        }
        return intValueResult;
    }

    private int calculateText(String text, float floatValue, float floatValueValue, int intValue, int intValueValue, boolean flag, boolean flagValue) {
        if (text == null) {
            return 0;
        } else {
            if ((intValue & -67108864) == 0) {
                intValue |= -16777216;
            }
            if ((intValueValue & -67108864) == 0) {
                intValueValue |= -16777216;
            }
            if (flag) {
                intValue = (intValue & 16579836) >> 2 | intValue & 0xFF000000;
                intValueValue = (intValueValue & 16579836) >> 2 | intValueValue & 0xFF000000;
            }
            return this.calculateStringWidthWithEnableBlend(text, floatValue, floatValueValue, flag, intValue, intValueValue, flagValue);
        }
    }

    private int calculateStringWidthWithEnableBlend(String text, float floatValue, float floatValueValue, boolean flag, int intValue, int intValueValue, boolean flagValue) {
        float stringWidth = this.getStringWidth(text);
        float floatValueResult = 0.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = new Matrix4f();
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == ' ' || character == 160) {
                floatValue += 4.0F;
            } else if (character < this.charData.length && this.charData[character] != null) {
                float floatValueItem = this.charData[character].width - 8 + this.charOffset;
                if (flagValue) {
                    float floatValueEntry = floatValueResult / stringWidth;
                    float floatValueCopy = (floatValueResult + floatValueItem) / stringWidth;
                    int intValueResult = this.interpolateRgb(intValue, intValueValue, floatValueEntry);
                    int intValueItem = this.interpolateRgb(intValue, intValueValue, floatValueCopy);
                    this.renderColorWithTexture(character, floatValue, floatValueValue, intValueResult, intValueItem, true, matrix);
                    floatValueResult += floatValueItem;
                } else {
                    this.renderColorWithTexture(character, floatValue, floatValueValue, intValue, intValueValue, false, matrix);
                }
                floatValue += floatValueItem;
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        return (int) floatValue;
    }

    private int interpolateRgb(int intValue, int intValueValue, float floatValue) {
        float floatValueValue = (intValue >> 24 & 0xFF) / 255.0F;
        float floatValueResult = (intValue >> 16 & 0xFF) / 255.0F;
        float floatValueItem = (intValue >> 8 & 0xFF) / 255.0F;
        float floatValueEntry = (intValue & 0xFF) / 255.0F;
        float floatValueCopy = (intValueValue >> 24 & 0xFF) / 255.0F;
        float floatValue2 = (intValueValue >> 16 & 0xFF) / 255.0F;
        float floatValue3 = (intValueValue >> 8 & 0xFF) / 255.0F;
        float floatValue4 = (intValueValue & 0xFF) / 255.0F;
        int intValueResult = (int) (((1.0F - floatValue) * floatValueValue + floatValue * floatValueCopy) * 255.0F);
        int intValueItem = (int) (((1.0F - floatValue) * floatValueResult + floatValue * floatValue2) * 255.0F);
        int intValueEntry = (int) (((1.0F - floatValue) * floatValueItem + floatValue * floatValue3) * 255.0F);
        int intValueCopy = (int) (((1.0F - floatValue) * floatValueEntry + floatValue * floatValue4) * 255.0F);
        return intValueResult << 24 | intValueItem << 16 | intValueEntry << 8 | intValueCopy;
    }

    private void renderColorWithTexture(char character, float floatValue, float floatValueValue, int intValue, int intValueValue, boolean flag, Matrix4f matrix) {
        if (character < this.charData.length && this.charData[character] != null) {
            float floatValueResult = (intValue >> 24 & 0xFF) / 255.0F;
            float floatValueItem = (intValue >> 16 & 0xFF) / 255.0F;
            float floatValueEntry = (intValue >> 8 & 0xFF) / 255.0F;
            float floatValueCopy = (intValue & 0xFF) / 255.0F;
            float floatValue2 = (intValueValue >> 24 & 0xFF) / 255.0F;
            float floatValue3 = (intValueValue >> 16 & 0xFF) / 255.0F;
            float floatValue4 = (intValueValue >> 8 & 0xFF) / 255.0F;
            float floatValue5 = (intValueValue & 0xFF) / 255.0F;
            GlyphAtlasRegion glyphAtlasRegion = this.charData[character];
            float storedX = glyphAtlasRegion.storedX;
            float storedY = glyphAtlasRegion.storedY;
            int width = glyphAtlasRegion.width;
            float floatValue6 = width - 0.01F;
            float floatValue7 = storedX / 512.0F;
            float floatValue8 = storedY / 512.0F;
            float floatValue9 = (storedX + floatValue6 - 1.0F) / 512.0F;
            float floatValue10 = (storedY + 7.99F) / 512.0F;
            RenderSystem.setShaderTexture(0, this.glTextureId);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            Tessellator instance = Tessellator.getInstance();
            BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            if (flag) {
                buffer.vertex(matrix, floatValue, floatValueValue, 0.0F).texture(floatValue7, floatValue8).color(floatValueItem, floatValueEntry, floatValueCopy, floatValueResult);
                buffer.vertex(matrix, floatValue, floatValueValue + 7.99F, 0.0F).texture(floatValue7, floatValue10).color(floatValueItem, floatValueEntry, floatValueCopy, floatValueResult);
                buffer.vertex(matrix, floatValue + floatValue6 - 1.0F, floatValueValue + 7.99F, 0.0F).texture(floatValue9, floatValue10).color(floatValue3, floatValue4, floatValue5, floatValue2);
                buffer.vertex(matrix, floatValue + floatValue6 - 1.0F, floatValueValue, 0.0F).texture(floatValue9, floatValue8).color(floatValue3, floatValue4, floatValue5, floatValue2);
            } else {
                buffer.vertex(matrix, floatValue, floatValueValue, 0.0F).texture(floatValue7, floatValue8).color(floatValueItem, floatValueEntry, floatValueCopy, floatValueResult);
                buffer.vertex(matrix, floatValue, floatValueValue + 7.99F, 0.0F).texture(floatValue7, floatValue10).color(floatValue3, floatValue4, floatValue5, floatValue2);
                buffer.vertex(matrix, floatValue + floatValue6 - 1.0F, floatValueValue + 7.99F, 0.0F).texture(floatValue9, floatValue10).color(floatValue3, floatValue4, floatValue5, floatValue2);
                buffer.vertex(matrix, floatValue + floatValue6 - 1.0F, floatValueValue, 0.0F).texture(floatValue9, floatValue8).color(floatValueItem, floatValueEntry, floatValueCopy, floatValueResult);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }
}
