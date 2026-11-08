package brova.toshiba.client.utils.render.font;

import brova.toshiba.client.modules.impl.misc.NameProtect;
import brova.toshiba.client.utils.render.transform.TransformStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.awt.Font;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class StyledFontRenderer extends FontRenderer {

    private final int[] integerValues = new int[32];

    public GlyphAtlasRegion[] boldChars = new GlyphAtlasRegion[1104];

    public GlyphAtlasRegion[] italicChars = new GlyphAtlasRegion[1104];

    public GlyphAtlasRegion[] boldItalicChars = new GlyphAtlasRegion[1104];

    public int texBold;

    public int texItalic;

    public int texItalicBold;

    public StyledFontRenderer(Font font, boolean flag, boolean flagValue) {
        super(font, flag, flagValue);
        this.updateDeriveFontWithGlTextureId();
        for (int index = 0; index < 32; index++) {
            int intValue = (index >> 3 & 1) * 85;
            int intValueValue = (index >> 2 & 1) * 170 + intValue;
            int intValueResult = (index >> 1 & 1) * 170 + intValue;
            int intValueItem = (index & 1) * 170 + intValue;
            if (index == 6) {
                intValueValue += 85;
            }
            if (index >= 16) {
                intValueValue /= 4;
                intValueResult /= 4;
                intValueItem /= 4;
            }
            this.integerValues[index] = (intValueValue & 0xFF) << 16 | (intValueResult & 0xFF) << 8 | intValueItem & 0xFF;
        }
    }

    public String patchText(String text) {
        if (text == null) {
            return null;
        } else {
            NameProtect INSTANCE = NameProtect.INSTANCE;
            return INSTANCE != null && INSTANCE.isEnable() ? INSTANCE.patchIncomingText(text) : text;
        }
    }

    public float drawStringWithShadow(String text, double doubleValue, double doubleValueValue, int intValue) {
        float floatValue = this.drawString(text, doubleValue + 0.5, doubleValueValue + 0.5, intValue, true);
        return Math.max(floatValue, this.drawString(text, doubleValue, doubleValueValue, intValue, false));
    }

    public float drawGradientString(String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        if (text == null) {
            return 0.0F;
        } else {
            text = this.patchText(text);
            floatValue--;
            if ((intValue & 0xFF000000) == 0) {
                intValue |= -16777216;
            }
            if ((intValueValue & 0xFF000000) == 0) {
                intValueValue |= -16777216;
            }
            float floatValueResult = (intValue >> 24 & 0xFF) / 255.0F;
            float floatValueItem = (intValue >> 16 & 0xFF) / 255.0F;
            float floatValueEntry = (intValue >> 8 & 0xFF) / 255.0F;
            float floatValueCopy = (intValue & 0xFF) / 255.0F;
            float floatValue2 = (intValueValue >> 24 & 0xFF) / 255.0F;
            float floatValue3 = (intValueValue >> 16 & 0xFF) / 255.0F;
            float floatValue4 = (intValueValue >> 8 & 0xFF) / 255.0F;
            float floatValue5 = (intValueValue & 0xFF) / 255.0F;
            double doubleValue = floatValue * 2.0;
            double doubleValueValue = (floatValueValue - 3.0) * 2.0;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Matrix4f matrix = TransformStack.matrix();
            matrix.scale(0.5F, 0.5F, 0.5F);
            GlyphAtlasRegion[] charData = this.charData;
            int length = text.length();
            for (int index = 0; index < length; index++) {
                char character = text.charAt(index);
                if (character < charData.length && charData[character] != null) {
                    RenderSystem.setShaderTexture(0, this.glTextureId);
                    RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                    Tessellator instance = Tessellator.getInstance();
                    BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    GlyphAtlasRegion glyphAtlasRegion = charData[character];
                    float storedX = glyphAtlasRegion.storedX;
                    float storedY = glyphAtlasRegion.storedY;
                    float width = glyphAtlasRegion.width;
                    float height = glyphAtlasRegion.height;
                    float floatValue6 = storedX / 512.0F;
                    float floatValue7 = storedY / 512.0F;
                    float floatValue8 = (storedX + width) / 512.0F;
                    float floatValue9 = (storedY + height) / 512.0F;
                    buffer.vertex(matrix, (float) doubleValue, (float) doubleValueValue, 0.0F).texture(floatValue6, floatValue7).color(floatValueItem, floatValueEntry, floatValueCopy, floatValueResult);
                    buffer.vertex(matrix, (float) doubleValue, (float) doubleValueValue + height, 0.0F).texture(floatValue6, floatValue9).color(floatValue3, floatValue4, floatValue5, floatValue2);
                    buffer.vertex(matrix, (float) doubleValue + width, (float) doubleValueValue + height, 0.0F).texture(floatValue8, floatValue9).color(floatValue3, floatValue4, floatValue5, floatValue2);
                    buffer.vertex(matrix, (float) doubleValue + width, (float) doubleValueValue, 0.0F).texture(floatValue8, floatValue7).color(floatValueItem, floatValueEntry, floatValueCopy, floatValueResult);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    doubleValue += glyphAtlasRegion.width - 8 + this.charOffset;
                } else if (character == ' ' || character == 160) {
                    doubleValue += 8.0;
                }
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return (float) doubleValue / 2.0F;
        }
    }

    public float drawGradientStringHorizontal(String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        if (text == null) {
            return 0.0F;
        } else {
            text = this.patchText(text);
            floatValue--;
            if ((intValue & 0xFF000000) == 0) {
                intValue |= -16777216;
            }
            if ((intValueValue & 0xFF000000) == 0) {
                intValueValue |= -16777216;
            }
            double doubleValue = floatValue * 2.0;
            double doubleValueValue = (floatValueValue - 3.0) * 2.0;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Matrix4f matrix = TransformStack.matrix();
            matrix.scale(0.5F, 0.5F, 0.5F);
            GlyphAtlasRegion[] charData = this.charData;
            int length = text.length();
            float floatValueResult = this.getStringWidth(text) * 2.0F;
            float floatValueItem = 0.0F;
            for (int index = 0; index < length; index++) {
                char character = text.charAt(index);
                if (character < charData.length && charData[character] != null) {
                    RenderSystem.setShaderTexture(0, this.glTextureId);
                    RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                    Tessellator instance = Tessellator.getInstance();
                    BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    GlyphAtlasRegion glyphAtlasRegion = charData[character];
                    float storedX = glyphAtlasRegion.storedX;
                    float storedY = glyphAtlasRegion.storedY;
                    float width = glyphAtlasRegion.width;
                    float height = glyphAtlasRegion.height;
                    float floatValueEntry = glyphAtlasRegion.width - 8 + this.charOffset;
                    float floatValueCopy = storedX / 512.0F;
                    float floatValue2 = storedY / 512.0F;
                    float floatValue3 = (storedX + width) / 512.0F;
                    float floatValue4 = (storedY + height) / 512.0F;
                    float floatValue5 = floatValueResult <= 0.0F ? 0.0F : floatValueItem / floatValueResult;
                    float floatValue6 = floatValueResult <= 0.0F ? 1.0F : (floatValueItem + floatValueEntry) / floatValueResult;
                    int intValueResult = this.interpolateRgb(intValue, intValueValue, floatValue5);
                    int intValueItem = this.interpolateRgb(intValue, intValueValue, floatValue6);
                    float floatValue7 = (intValueResult >> 24 & 0xFF) / 255.0F;
                    float floatValue8 = (intValueResult >> 16 & 0xFF) / 255.0F;
                    float floatValue9 = (intValueResult >> 8 & 0xFF) / 255.0F;
                    float floatValue10 = (intValueResult & 0xFF) / 255.0F;
                    float floatValue11 = (intValueItem >> 24 & 0xFF) / 255.0F;
                    float floatValue12 = (intValueItem >> 16 & 0xFF) / 255.0F;
                    float floatValue13 = (intValueItem >> 8 & 0xFF) / 255.0F;
                    float floatValue14 = (intValueItem & 0xFF) / 255.0F;
                    buffer.vertex(matrix, (float) doubleValue, (float) doubleValueValue, 0.0F).texture(floatValueCopy, floatValue2).color(floatValue8, floatValue9, floatValue10, floatValue7);
                    buffer.vertex(matrix, (float) doubleValue, (float) doubleValueValue + height, 0.0F).texture(floatValueCopy, floatValue4).color(floatValue8, floatValue9, floatValue10, floatValue7);
                    buffer.vertex(matrix, (float) doubleValue + width, (float) doubleValueValue + height, 0.0F).texture(floatValue3, floatValue4).color(floatValue12, floatValue13, floatValue14, floatValue11);
                    buffer.vertex(matrix, (float) doubleValue + width, (float) doubleValueValue, 0.0F).texture(floatValue3, floatValue2).color(floatValue12, floatValue13, floatValue14, floatValue11);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    doubleValue += floatValueEntry;
                    floatValueItem += floatValueEntry;
                } else if (character == ' ' || character == 160) {
                    doubleValue += 8.0;
                    floatValueItem += 8.0F;
                }
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return (float) doubleValue / 2.0F;
        }
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

    public float drawString(String text, float floatValue, float floatValueValue, int intValue) {
        return this.drawString(text, floatValue, floatValueValue, intValue, false);
    }

    public void drawRotatedGlyph(char character, float floatValue, float floatValueValue, int intValue, float floatValueResult) {
        if (character < this.charData.length && this.charData[character] != null) {
            floatValue = (floatValue - 1.0F) * 2.0F;
            floatValueValue = (floatValueValue - 3.0F) * 2.0F;
            float floatValueItem = floatValue + this.charData[character].width / 2.0F;
            float floatValueEntry = floatValueValue + this.charData[character].height / 2.0F;
            float floatValueCopy = (intValue >> 24 & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, floatValueCopy);
            RenderSystem.setShaderTexture(0, this.glTextureId);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
            Matrix4f matrix = TransformStack.matrix().scale(0.5F, 0.5F, 0.5F).translate(floatValueItem, floatValueEntry, 0.0F).rotateZ((float) Math.toRadians(floatValueResult)).translate(-floatValueItem, -floatValueEntry, 0.0F);
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);
            this.drawChar(this.charData, character, floatValue, floatValueValue, matrix, buffer);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.disableBlend();
        }
    }

    public float drawCenteredString(String text, float floatValue, float floatValueValue, int intValue) {
        return this.drawString(text, floatValue - this.getStringWidth(text) / 2.0F, floatValueValue, intValue);
    }

    public float drawCenteredStringWithShadow(String text, float floatValue, float floatValueValue, int intValue) {
        return this.drawStringWithShadow(text, floatValue - this.getStringWidth(text) / 2.0F, floatValueValue, intValue);
    }

    public float drawString(String text, double doubleValue, double doubleValueValue, int intValue, boolean flag) {
        doubleValue--;
        if (text == null) {
            return 0.0F;
        } else {
            text = this.patchText(text);
            if (intValue == 553648127) {
                intValue = 16777215;
            }
            if ((intValue & 0xFF000000) == 0) {
                intValue |= -16777216;
            }
            if (flag) {
                intValue = (intValue & 16579836) >> 2 | intValue & new Color(20, 20, 20, 200).getRGB();
            }
            GlyphAtlasRegion[] charData = this.charData;
            float floatValue = (intValue >> 24 & 0xFF) / 255.0F;
            boolean flagValue = false;
            boolean flagResult = false;
            boolean flagItem = false;
            boolean flagEntry = false;
            doubleValue *= 2.0;
            doubleValueValue = (doubleValueValue - 3.0) * 2.0;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, floatValue);
            Matrix4f matrix = TransformStack.matrix();
            matrix.scale(0.5F, 0.5F, 0.5F);
            int length = text.length();
            int glTextureId = this.glTextureId;
            RenderSystem.setShaderTexture(0, glTextureId);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
            for (int index = 0; index < length; index++) {
                char character = text.charAt(index);
                if (String.valueOf(character).equals("§") && index < length - 1) {
                    int intValueValue = 21;
                    try {
                        intValueValue = "0123456789abcdefklmnor".indexOf(text.charAt(index + 1));
                    } catch (Exception failure) {
                        System.err.println("[StyledFontRenderer] " + failure.getMessage());
                    }
                    if (intValueValue >= 16) {
                        if (intValueValue == 17) {
                            flagValue = true;
                            if (flagResult) {
                                glTextureId = this.texItalicBold;
                                charData = this.boldItalicChars;
                            } else {
                                glTextureId = this.texBold;
                                charData = this.boldChars;
                            }
                        } else if (intValueValue == 18) {
                            flagItem = true;
                        } else if (intValueValue == 19) {
                            flagEntry = true;
                        } else if (intValueValue == 20) {
                            flagResult = true;
                            if (flagValue) {
                                glTextureId = this.texItalicBold;
                                charData = this.boldItalicChars;
                            } else {
                                glTextureId = this.texItalic;
                                charData = this.italicChars;
                            }
                        } else if (intValueValue == 21) {
                            flagValue = false;
                            flagResult = false;
                            flagEntry = false;
                            flagItem = false;
                            RenderSystem.setShaderColor((intValue >> 16 & 0xFF) / 255.0F, (intValue >> 8 & 0xFF) / 255.0F, (intValue & 0xFF) / 255.0F, floatValue);
                            glTextureId = this.glTextureId;
                            charData = this.charData;
                        }
                    } else {
                        flagValue = false;
                        flagResult = false;
                        flagEntry = false;
                        flagItem = false;
                        glTextureId = this.glTextureId;
                        charData = this.charData;
                        if (intValueValue < 0 || intValueValue > 15) {
                            intValueValue = 15;
                        }
                        if (flag) {
                            intValueValue += 16;
                        }
                        int intValueResult = this.integerValues[intValueValue];
                        RenderSystem.setShaderColor((intValueResult >> 16 & 0xFF) / 255.0F, (intValueResult >> 8 & 0xFF) / 255.0F, (intValueResult & 0xFF) / 255.0F, floatValue);
                    }
                    index++;
                } else if (character < charData.length && charData[character] != null) {
                    RenderSystem.setShaderTexture(0, glTextureId);
                    Tessellator instance = Tessellator.getInstance();
                    BufferBuilder buffer = instance.begin(DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);
                    this.drawChar(charData, character, (float) doubleValue, (float) doubleValueValue, matrix, buffer);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    if (flagItem) {
                        this.renderVertexWithShader(doubleValue, doubleValueValue + charData[character].height / 2.0F, doubleValue + charData[character].width - 8.0, doubleValueValue + charData[character].height / 2.0F, 1.0F, matrix);
                    }
                    if (flagEntry) {
                        this.renderVertexWithShader(doubleValue, doubleValueValue + charData[character].height - 2.0, doubleValue + charData[character].width - 8.0, doubleValueValue + charData[character].height - 2.0, 1.0F, matrix);
                    }
                    doubleValue += charData[character].width - 8 + this.charOffset;
                }
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return (float) doubleValue / 2.0F;
        }
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            text = this.patchText(text);
            int intValue = 0;
            GlyphAtlasRegion[] charData = this.charData;
            boolean flag = false;
            boolean flagValue = false;
            int length = text.length();
            for (int index = 0; index < length; index++) {
                char character = text.charAt(index);
                if (String.valueOf(character).equals("§") && index < length - 1) {
                    int indexValue = "0123456789abcdefklmnor".indexOf(text.charAt(index + 1));
                    if (indexValue < 16) {
                        flag = false;
                        flagValue = false;
                    } else if (indexValue == 17) {
                        flag = true;
                        charData = flagValue ? this.boldItalicChars : this.boldChars;
                    } else if (indexValue == 20) {
                        flagValue = true;
                        charData = flag ? this.boldItalicChars : this.italicChars;
                    } else if (indexValue == 21) {
                        flag = false;
                        flagValue = false;
                        charData = this.charData;
                    }
                    index++;
                } else if (character < charData.length && charData[character] != null) {
                    intValue += charData[character].width - 8 + this.charOffset;
                }
            }
            return intValue / 2;
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        this.updateDeriveFontWithGlTextureId();
    }

    @Override
    public void setAntiAlias(boolean antiAlias) {
        super.setAntiAlias(antiAlias);
        this.updateDeriveFontWithGlTextureId();
    }

    @Override
    public void setFractionalMetrics(boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
        this.updateDeriveFontWithGlTextureId();
    }

    private void updateDeriveFontWithGlTextureId() {
        FontRenderer font = new FontRenderer(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics);
        this.texBold = font.getGlTextureId();
        this.boldChars = font.charData;
        FontRenderer fontValue = new FontRenderer(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics);
        this.texItalic = fontValue.getGlTextureId();
        this.italicChars = fontValue.charData;
        FontRenderer fontResult = new FontRenderer(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics);
        this.texItalicBold = fontResult.getGlTextureId();
        this.boldItalicChars = fontResult.charData;
    }

    private void renderVertexWithShader(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, float floatValue, Matrix4f matrix) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION);
        RenderSystem.lineWidth(floatValue);
        Tessellator instance = Tessellator.getInstance();
        BufferBuilder buffer = instance.begin(DrawMode.LINES, VertexFormats.POSITION);
        buffer.vertex(matrix, (float) doubleValue, (float) doubleValueValue, 0.0F);
        buffer.vertex(matrix, (float) doubleValueResult, (float) doubleValueItem, 0.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public void drawStringWithOutline(String text, double doubleValue, double doubleValueValue, int intValue) {
        this.drawString(text, doubleValue - 0.5, doubleValueValue, Color.BLACK.getRGB(), false);
        this.drawString(text, doubleValue + 0.5, doubleValueValue, Color.BLACK.getRGB(), false);
        this.drawString(text, doubleValue, doubleValueValue - 0.5, Color.BLACK.getRGB(), false);
        this.drawString(text, doubleValue, doubleValueValue + 0.5, Color.BLACK.getRGB(), false);
        this.drawString(text, doubleValue, doubleValueValue, intValue, false);
    }

    public void drawCenteredStringWithOutline(String text, float floatValue, float floatValueValue, int intValue) {
        this.drawCenteredString(text, floatValue - 0.5F, floatValueValue, Color.BLACK.getRGB());
        this.drawCenteredString(text, floatValue + 0.5F, floatValueValue, Color.BLACK.getRGB());
        this.drawCenteredString(text, floatValue, floatValueValue - 0.5F, Color.BLACK.getRGB());
        this.drawCenteredString(text, floatValue, floatValueValue + 0.5F, Color.BLACK.getRGB());
        this.drawCenteredString(text, floatValue, floatValueValue, intValue);
    }
}
