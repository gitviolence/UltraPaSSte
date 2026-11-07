package brova.toshiba.client.utils.render.font;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class TextRenderer implements MinecraftAccess {

    private static final char formattingMarker = '§';

    private final MsdfFont font;

    private final float size;

    public TextRenderer(MsdfFont font, float size) {
        this.font = font;
        this.size = size;
    }

    public TextRenderer(String text, float size) {
        this.font = MsdfFont.builder().atlas(text).data(text).build();
        this.size = size;
    }

    public void drawString(MatrixStack matrices, String text, double doubleValue, double doubleValueValue, int intValue) {
        this.draw(matrices, text, (float) doubleValue, (float) doubleValueValue, intValue);
    }

    public void drawString(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue) {
        this.draw(matrices, text, floatValue, floatValueValue, intValue);
    }

    public void drawString(String text, float floatValue, float floatValueValue, int intValue) {
        MatrixStack matrices = new MatrixStack();
        this.draw(matrices, text, floatValue, floatValueValue, intValue);
    }

    public void drawCenteredString(MatrixStack matrices, String text, double doubleValue, double doubleValueValue, int intValue) {
        this.draw(matrices, text, (float) (doubleValue - this.getStringWidth(text) / 2.0), (float) doubleValueValue, intValue);
    }

    public void drawCenteredString(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue) {
        this.draw(matrices, text, floatValue - this.getStringWidth(text) / 2.0F, floatValueValue, intValue);
    }

    public void drawRight(MatrixStack matrices, String text, double doubleValue, double doubleValueValue, int intValue) {
        this.draw(matrices, text, (float) (doubleValue - this.getStringWidth(text)), (float) doubleValueValue, intValue);
    }

    public void drawRight(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue) {
        this.draw(matrices, text, floatValue - this.getStringWidth(text), floatValueValue, intValue);
    }

    public void draw(MatrixStack matrices, String text, double doubleValue, double doubleValueValue, int intValue) {
        this.draw(matrices, text, (float) doubleValue, (float) doubleValueValue, intValue);
    }

    public void draw(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue) {
        this.drawWithWeight(matrices, text, floatValue, floatValueValue, intValue, 0.0F);
    }

    public void drawWithWeight(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, float floatValueResult) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float floatValueItem = this.size * 0.5F;
            if (this.isWidth(text, floatValueItem)) {
                floatValueValue--;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.fontsMsdf);
                if (orCreateProgram != null) {
                    this.updateUniformWithAtlasWidth(orCreateProgram, intValue);
                    GlUniform uniform = orCreateProgram.getUniform("Weight");
                    if (uniform != null) {
                        uniform.set(Math.max(0.0F, Math.min(0.12F, floatValueResult)));
                    }
                    RenderSystem.setShaderTexture(0, this.font.getTextureId());
                    this.font.setFiltered();
                    Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                    BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    this.font.applyGlyphs(positionMatrix, buffer, floatValueItem, text, 0.0F, floatValue, floatValueValue + this.font.getBaselineHeight() * floatValueItem, 0.0F, 255, 255, 255, 255);
                    RenderSystem.setShader(ShaderPrograms.fontsMsdf);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    if (uniform != null) {
                        uniform.set(0.0F);
                    }
                    RenderSystem.setShaderTexture(0, 0);
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                }
            }
        }
    }

    public void drawGradientStringHorizontal(String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        MatrixStack matrices = new MatrixStack();
        this.drawGradientStringHorizontal(matrices, text, floatValue, floatValueValue, intValue, intValueValue);
    }

    public void drawGradientStringHorizontal(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float stringWidth = this.getStringWidth(text);
            float floatValueResult = floatValue;
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                String textValue = String.valueOf(character);
                float stringWidthValue = this.getStringWidth(textValue);
                float floatValueItem = stringWidth > 0.0F ? (floatValueResult - floatValue) / stringWidth : 0.0F;
                int intValueResult = interpolateColor(intValue, intValueValue, floatValueItem);
                this.draw(matrices, textValue, floatValueResult, floatValueValue, intValueResult);
                floatValueResult += stringWidthValue;
            }
        }
    }

    public void drawGradientStringHorizontal(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float stringWidth = this.getStringWidth(text);
            float floatValueResult = floatValue;
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                String textValue = String.valueOf(character);
                float stringWidthValue = this.getStringWidth(textValue);
                float floatValueItem = stringWidth > 0.0F ? (floatValueResult - floatValue) / stringWidth : 0.0F;
                int intValueEntry = interpolateColor(intValue, intValueValue, floatValueItem);
                int intValueCopy = interpolateColor(intValueResult, intValueItem, floatValueItem);
                int intValue2 = interpolateColor(intValueEntry, intValueCopy, 0.5F);
                this.draw(matrices, textValue, floatValueResult, floatValueValue, intValue2);
                floatValueResult += stringWidthValue;
            }
        }
    }

    public void drawGradientStringVertical(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float floatValueResult = this.size * 0.5F;
            if (this.isWidth(text, floatValueResult)) {
                floatValueValue--;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.watermarkFontsMsdf);
                if (orCreateProgram != null) {
                    this.updateUniformWithAtlasWidth(orCreateProgram, -1);
                    RenderSystem.setShaderTexture(0, this.font.getTextureId());
                    this.font.setFiltered();
                    Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                    BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    this.font.applyGlyphsVerticalGradient(positionMatrix, buffer, floatValueResult, text, 0.0F, floatValue, floatValueValue + this.font.getBaselineHeight() * floatValueResult, 0.0F, intValue >> 16 & 0xFF, intValue >> 8 & 0xFF, intValue & 0xFF, intValue >>> 24 & 0xFF, intValueValue >> 16 & 0xFF, intValueValue >> 8 & 0xFF, intValueValue & 0xFF, intValueValue >>> 24 & 0xFF);
                    RenderSystem.setShader(ShaderPrograms.watermarkFontsMsdf);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    RenderSystem.setShaderTexture(0, 0);
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                }
            }
        }
    }

    public void drawGradientGlyphHorizontal(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float floatValueResult = this.size * 0.5F;
            if (this.isWidth(text, floatValueResult)) {
                floatValueValue--;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.watermarkFontsMsdf);
                if (orCreateProgram != null) {
                    this.updateUniformWithAtlasWidth(orCreateProgram, -1);
                    RenderSystem.setShaderTexture(0, this.font.getTextureId());
                    this.font.setFiltered();
                    Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                    BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    this.font.applyGlyphsHorizontalGradient(positionMatrix, buffer, floatValueResult, text, 0.0F, floatValue, floatValueValue + this.font.getBaselineHeight() * floatValueResult, 0.0F, intValue >> 16 & 0xFF, intValue >> 8 & 0xFF, intValue & 0xFF, intValue >>> 24 & 0xFF, intValueValue >> 16 & 0xFF, intValueValue >> 8 & 0xFF, intValueValue & 0xFF, intValueValue >>> 24 & 0xFF);
                    RenderSystem.setShader(ShaderPrograms.watermarkFontsMsdf);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    RenderSystem.setShaderTexture(0, 0);
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                }
            }
        }
    }

    public void drawStringWithFade(MatrixStack matrices, String text, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            if (!(floatValueResult <= 1.0F)) {
                int intValueValue = intValue >>> 24 & 0xFF;
                if (intValueValue > 4) {
                    float floatValueItem = this.size * 0.5F;
                    float floatValueEntry = 25.0F;
                    if ((intValue >>> 24 & 0xFF) > 4 && text.indexOf(167) < 0 && this.getStringWidth(text) <= floatValueResult - floatValueEntry) {
                        this.draw(matrices, text, floatValue, floatValueValue, intValue);
                    } else {
                        floatValueValue--;
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.disableCull();
                        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.fontsMsdf);
                        if (orCreateProgram != null) {
                            GlUniform uniform = orCreateProgram.getUniform("TextureSize");
                            GlUniform uniformValue = orCreateProgram.getUniform("Range");
                            GlUniform uniformResult = orCreateProgram.getUniform("Color");
                            if (uniform != null) {
                                uniform.set(this.font.getAtlasWidth(), this.font.getAtlasHeight());
                            }
                            if (uniformValue != null) {
                                uniformValue.set(this.font.getRange());
                            }
                            RenderSystem.setShaderTexture(0, this.font.getTextureId());
                            this.font.setFiltered();
                            float floatValueCopy = floatValue;
                            float floatValue2 = floatValue + floatValueResult - floatValueEntry;
                            for (int index = 0; index < text.length(); index++) {
                                String textValue = String.valueOf(text.charAt(index));
                                float stringWidth = this.getStringWidth(textValue);
                                if (Character.isWhitespace(text.charAt(index))) {
                                    floatValueCopy += stringWidth;
                                } else if (!this.isWidth(textValue, floatValueItem)) {
                                    floatValueCopy += stringWidth;
                                } else {
                                    if (floatValueCopy > floatValue + floatValueResult && index > 0) {
                                        break;
                                    }
                                    int intValueResult = intValue;
                                    if (floatValueCopy > floatValue2) {
                                        float floatValue3 = (floatValueCopy - floatValue2) / floatValueEntry;
                                        floatValue3 = Math.max(0.0F, Math.min(1.0F, floatValue3));
                                        float floatValue4 = (float) Math.cos(floatValue3 * Math.PI / 2.0);
                                        int intValueItem = (int) (intValueValue * floatValue4);
                                        intValueResult = intValue & 16777215 | intValueItem << 24;
                                    }
                                    if ((intValueResult >>> 24 & 0xFF) > 4) {
                                        float[] values = this.createFloatFromInteger(intValueResult);
                                        if (uniformResult != null) {
                                            uniformResult.set(values[0], values[1], values[2], values[3]);
                                        }
                                        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                                        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                                        this.font.applyGlyphs(positionMatrix, buffer, floatValueItem, textValue, 0.0F, floatValueCopy, floatValueValue + this.font.getBaselineHeight() * floatValueItem, 0.0F, 255, 255, 255, 255);
                                        RenderSystem.setShader(ShaderPrograms.fontsMsdf);
                                        BufferRenderer.drawWithGlobalProgram(buffer.end());
                                    }
                                    floatValueCopy += stringWidth;
                                }
                            }
                            RenderSystem.setShaderTexture(0, 0);
                            RenderSystem.enableCull();
                            RenderSystem.disableBlend();
                        }
                    }
                }
            }
        }
    }

    public void drawAnimatedGradientStringHorizontal(String text, float floatValue, float floatValueValue, int intValue, int intValueValue, float floatValueResult) {
        MatrixStack matrices = new MatrixStack();
        this.drawAnimatedGradientStringHorizontal(matrices, text, floatValue, floatValueValue, intValue, intValueValue, floatValueResult, 1.15F);
    }

    public void drawAnimatedGradientStringHorizontal(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue, float floatValueResult) {
        this.drawAnimatedGradientStringHorizontal(matrices, text, floatValue, floatValueValue, intValue, intValueValue, floatValueResult, 1.15F);
    }

    public void drawAnimatedGradientStringHorizontal(String text, float floatValue, float floatValueValue, int intValue, int intValueValue, float floatValueResult, float floatValueItem) {
        MatrixStack matrices = new MatrixStack();
        this.drawAnimatedGradientStringHorizontal(matrices, text, floatValue, floatValueValue, intValue, intValueValue, floatValueResult, floatValueItem);
    }

    public void drawAnimatedGradientStringHorizontal(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue, float floatValueResult, float floatValueItem) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float stringWidth = this.getStringWidth(text);
            float floatValueEntry = floatValue;
            double doubleValue = System.currentTimeMillis() * 0.001 * Math.max(0.01F, floatValueResult) % 2.0;
            float floatValueCopy = Math.max(0.01F, floatValueItem);
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                String textValue = String.valueOf(character);
                float stringWidthValue = this.getStringWidth(textValue);
                float floatValue2 = stringWidth > 0.0F ? (floatValueEntry - floatValue) / stringWidth : 0.0F;
                float floatValue3 = calculateFloat(floatValue2 * floatValueCopy + (float) doubleValue);
                int intValueResult = interpolateColor(intValue, intValueValue, floatValue3);
                this.draw(matrices, textValue, floatValueEntry, floatValueValue, intValueResult);
                floatValueEntry += stringWidthValue;
            }
        }
    }

    public void drawStringWithOutline(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            this.draw(matrices, text, floatValue - 1.0F, floatValueValue, intValueValue);
            this.draw(matrices, text, floatValue + 1.0F, floatValueValue, intValueValue);
            this.draw(matrices, text, floatValue, floatValueValue - 1.0F, intValueValue);
            this.draw(matrices, text, floatValue, floatValueValue + 1.0F, intValueValue);
            this.draw(matrices, text, floatValue, floatValueValue, intValue);
        }
    }

    public void drawStringWithShadow(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            int intValueValue = 1426063360;
            this.draw(matrices, text, floatValue + 1.0F, floatValueValue + 1.0F, intValueValue);
            this.draw(matrices, text, floatValue, floatValueValue, intValue);
        }
    }

    public void drawParagraph(MatrixStack matrices, String text, double doubleValue, double doubleValueValue, int intValue) {
        this.drawParagraph(matrices, text, (float) doubleValue, (float) doubleValueValue, intValue);
    }

    public void drawParagraph(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue) {
        if (text != null && !text.isEmpty()) {
            text = this.createTextFromPatchIncomingText(text);
            float floatValueResult = this.size * 0.5F;
            floatValueValue--;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.fontsMsdf);
            if (orCreateProgram != null) {
                GlUniform uniform = orCreateProgram.getUniform("TextureSize");
                GlUniform uniformValue = orCreateProgram.getUniform("Range");
                GlUniform uniformResult = orCreateProgram.getUniform("Color");
                if (uniform != null) {
                    uniform.set(this.font.getAtlasWidth(), this.font.getAtlasHeight());
                }
                if (uniformValue != null) {
                    uniformValue.set(this.font.getRange());
                }
                RenderSystem.setShaderTexture(0, this.font.getTextureId());
                this.font.setFiltered();
                float floatValueItem = floatValue;
                int intValueValue = intValue;
                StringBuilder textBuilder = new StringBuilder();
                for (int index = 0; index < text.length(); index++) {
                    char character = text.charAt(index);
                    if (character == 167 && index + 1 < text.length()) {
                        if (!textBuilder.isEmpty()) {
                            this.renderBeginWithApplyGlyphs(matrices, uniformResult, textBuilder.toString(), floatValueItem, floatValueValue + this.font.getBaselineHeight() * floatValueResult, floatValueResult, intValueValue);
                            floatValueItem += this.getStringWidth(textBuilder.toString());
                            textBuilder.setLength(0);
                        }
                        char characterValue = text.charAt(index + 1);
                        int intValueResult = this.calculateChar(characterValue, intValue);
                        if (intValueResult != -1) {
                            intValueValue = intValueResult;
                        }
                        index++;
                    } else {
                        textBuilder.append(character);
                    }
                }
                if (!textBuilder.isEmpty()) {
                    this.renderBeginWithApplyGlyphs(matrices, uniformResult, textBuilder.toString(), floatValueItem, floatValueValue + this.font.getBaselineHeight() * floatValueResult, floatValueResult, intValueValue);
                }
                RenderSystem.setShaderTexture(0, 0);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderBeginWithApplyGlyphs(MatrixStack matrices, GlUniform glUniform, String text, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        if (this.isWidth(text, floatValueResult)) {
            float[] values = this.createFloatFromInteger(intValue);
            if (glUniform != null) {
                glUniform.set(values[0], values[1], values[2], values[3]);
            }
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            this.font.applyGlyphs(positionMatrix, buffer, floatValueResult, text, 0.0F, floatValue, floatValueValue, 0.0F, 255, 255, 255, 255);
            RenderSystem.setShader(ShaderPrograms.fontsMsdf);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

    private boolean isWidth(String text, float floatValue) {
        return text != null && !text.isEmpty() && this.font.getWidth(text, floatValue) > 0.0F;
    }

    private void updateUniformWithAtlasWidth(ShaderProgram shaderProgram, int intValue) {
        GlUniform uniform = shaderProgram.getUniform("TextureSize");
        GlUniform uniformValue = shaderProgram.getUniform("Range");
        GlUniform uniformResult = shaderProgram.getUniform("Color");
        if (uniform != null) {
            uniform.set(this.font.getAtlasWidth(), this.font.getAtlasHeight());
        }
        if (uniformValue != null) {
            uniformValue.set(this.font.getRange());
        }
        float[] values = this.createFloatFromInteger(intValue);
        if (uniformResult != null) {
            uniformResult.set(values[0], values[1], values[2], values[3]);
        }
    }

    private int calculateChar(char character, int intValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        return switch(character) {
            case '0' ->
                intValueValue << 24 | 0;
            case '1' ->
                intValueValue << 24 | 170;
            case '2' ->
                intValueValue << 24 | 43520;
            case '3' ->
                intValueValue << 24 | 43690;
            case '4' ->
                intValueValue << 24 | 11141120;
            case '5' ->
                intValueValue << 24 | 11141290;
            case '6' ->
                intValueValue << 24 | 16755200;
            case '7' ->
                intValueValue << 24 | 11184810;
            case '8' ->
                intValueValue << 24 | 5592405;
            case '9' ->
                intValueValue << 24 | 5592575;
            default ->
                -1;
            case 'A', 'a' ->
                intValueValue << 24 | 5635925;
            case 'B', 'b' ->
                intValueValue << 24 | 5636095;
            case 'C', 'c' ->
                intValueValue << 24 | 16733525;
            case 'D', 'd' ->
                intValueValue << 24 | 16733695;
            case 'E', 'e' ->
                intValueValue << 24 | 16777045;
            case 'F', 'f' ->
                intValueValue << 24 | 16777215;
            case 'R', 'r' ->
                intValue;
        };
    }

    private float[] createFloatFromInteger(int intValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        return new float[] { intValueResult / 255.0F, intValueItem / 255.0F, intValueEntry / 255.0F, intValueValue / 255.0F };
    }

    public static int interpolateColor(int intValue, int intValueValue, float floatValue) {
        floatValue = Math.max(0.0F, Math.min(1.0F, floatValue));
        int intValueResult = intValue >> 24 & 0xFF;
        int intValueItem = intValue >> 16 & 0xFF;
        int intValueEntry = intValue >> 8 & 0xFF;
        int intValueCopy = intValue & 0xFF;
        int intValue2 = intValueValue >> 24 & 0xFF;
        int intValue3 = intValueValue >> 16 & 0xFF;
        int intValue4 = intValueValue >> 8 & 0xFF;
        int intValue5 = intValueValue & 0xFF;
        if (intValueResult == 0) {
            intValueResult = 255;
        }
        if (intValue2 == 0) {
            intValue2 = 255;
        }
        int intValue6 = (int) (intValueResult + (intValue2 - intValueResult) * floatValue);
        int intValue7 = (int) (intValueItem + (intValue3 - intValueItem) * floatValue);
        int intValue8 = (int) (intValueEntry + (intValue4 - intValueEntry) * floatValue);
        int intValue9 = (int) (intValueCopy + (intValue5 - intValueCopy) * floatValue);
        return intValue6 << 24 | intValue7 << 16 | intValue8 << 8 | intValue9;
    }

    private static float calculateFloat(float floatValue) {
        float floatValueValue = floatValue % 2.0F;
        if (floatValueValue < 0.0F) {
            floatValueValue += 2.0F;
        }
        return floatValueValue > 1.0F ? 2.0F - floatValueValue : floatValueValue;
    }

    private String createTextFromPatchIncomingText(String text) {
        if (text == null) {
            return null;
        } else if (ModuleRegistry.INSTANCE == null) {
            return text;
        } else {
            NameProtect nameProtect = ModuleRegistry.nameProtect;
            return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
        }
    }

    public float getStringWidth(String text) {
        if (text == null) {
            return 0.0F;
        } else {
            text = this.createTextFromPatchIncomingText(text);
            return this.font.getWidth(this.createTextFromIndexOfWithCharAt(text), this.size) / 2.0F;
        }
    }

    public float getWidth(String text) {
        return this.getStringWidth(text);
    }

    public float getRenderWidth(String text) {
        if (text == null) {
            return 0.0F;
        } else {
            text = this.createTextFromPatchIncomingText(text);
            return this.font.getWidth(this.createTextFromIndexOfWithCharAt(text), this.size * 0.5F);
        }
    }

    public float getHeight() {
        return this.size;
    }

    public float getFontHeight() {
        return this.size;
    }

    public MsdfFont getFont() {
        return this.font;
    }

    public float getSize() {
        return this.size;
    }

    private String createTextFromIndexOfWithCharAt(String text) {
        if (text != null && text.indexOf(167) >= 0) {
            StringBuilder textBuilder = new StringBuilder(text.length());
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                if (character == 167 && index + 1 < text.length()) {
                    index++;
                } else {
                    textBuilder.append(character);
                }
            }
            return textBuilder.toString();
        } else {
            return text;
        }
    }
}
