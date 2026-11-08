package brova.toshiba.client.utils.render.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class FontRenderer {

    public static final int IMG_SIZE = 512;

    public GlyphAtlasRegion[] charData = new GlyphAtlasRegion[1104];

    public Font font;

    public boolean antiAlias;

    public boolean fractionalMetrics;

    public int fontHeight = -1;

    public int charOffset = 0;

    public Identifier textureId;

    public int glTextureId;

    private static int cachedCount = 0;

    public FontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        this.setupTexture(font, antiAlias, fractionalMetrics, this.charData);
    }

    public final void setupTexture(Font font, boolean flag, boolean flagValue, GlyphAtlasRegion[] glyphAtlasRegions) {
        BufferedImage bufferedImage = this.generateFontImage(font, flag, flagValue, glyphAtlasRegions);
        try {
            NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            for (int index = 0; index < bufferedImage.getHeight(); index++) {
                for (int indexValue = 0; indexValue < bufferedImage.getWidth(); indexValue++) {
                    int rGB = bufferedImage.getRGB(indexValue, index);
                    int intValue = rGB >> 24 & 0xFF;
                    int intValueValue = rGB >> 16 & 0xFF;
                    int intValueResult = rGB >> 8 & 0xFF;
                    int intValueItem = rGB & 0xFF;
                    nativeImage.setColorArgb(indexValue, index, intValue << 24 | intValueValue << 16 | intValueResult << 8 | intValueItem);
                }
            }
            NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
            this.glTextureId = nativeImageBackedTexture.getGlId();
            String text = "cfont_" + cachedCount++;
            this.textureId = Identifier.of("customfont", text);
            MinecraftClient.getInstance().getTextureManager().registerTexture(this.textureId, nativeImageBackedTexture);
        } catch (Exception failure) {
            System.err.println("[FontRenderer] " + failure.getMessage());
        }
    }

    public final BufferedImage generateFontImage(Font font, boolean flag, boolean flagValue, GlyphAtlasRegion[] glyphAtlasRegions) {
        BufferedImage bufferedImage = new BufferedImage(512, 512, 2);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, 512, 512);
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, flagValue ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, flag ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, flag ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int intValue = 0;
        int storedX = 0;
        int storedY = 1;
        for (int index = 0; index < glyphAtlasRegions.length; index++) {
            char character = (char) index;
            if (character > 1039 && character < 1104 || character < 256) {
                GlyphAtlasRegion glyphAtlasRegion = new GlyphAtlasRegion();
                Rectangle2D stringBounds = fontMetrics.getStringBounds(String.valueOf(character), graphics);
                glyphAtlasRegion.width = stringBounds.getBounds().width + 8;
                glyphAtlasRegion.height = stringBounds.getBounds().height;
                if (storedX + glyphAtlasRegion.width >= 512) {
                    storedX = 0;
                    storedY += intValue;
                    intValue = 0;
                }
                if (glyphAtlasRegion.height > intValue) {
                    intValue = glyphAtlasRegion.height;
                }
                glyphAtlasRegion.storedX = storedX;
                glyphAtlasRegion.storedY = storedY;
                if (glyphAtlasRegion.height > this.fontHeight) {
                    this.fontHeight = glyphAtlasRegion.height;
                }
                glyphAtlasRegions[index] = glyphAtlasRegion;
                graphics.drawString(String.valueOf(character), storedX + 2, storedY + fontMetrics.getAscent());
                storedX += glyphAtlasRegion.width;
            }
        }
        return bufferedImage;
    }

    public void drawChar(GlyphAtlasRegion[] glyphAtlasRegions, char character, float floatValue, float floatValueValue, Matrix4f matrix, BufferBuilder buffer) {
        try {
            if (glyphAtlasRegions[character] == null) {
                return;
            }
            this.drawQuad(floatValue, floatValueValue, glyphAtlasRegions[character].width, glyphAtlasRegions[character].height, glyphAtlasRegions[character].storedX, glyphAtlasRegions[character].storedY, glyphAtlasRegions[character].width, glyphAtlasRegions[character].height, matrix, buffer);
        } catch (Exception failure) {
        }
    }

    public void drawQuad(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, Matrix4f matrix, BufferBuilder buffer) {
        float floatValue4 = floatValueEntry / 512.0F;
        float floatValue5 = floatValueCopy / 512.0F;
        float floatValue6 = floatValue2 / 512.0F;
        float floatValue7 = floatValue3 / 512.0F;
        buffer.vertex(matrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue4 + floatValue6, floatValue5);
        buffer.vertex(matrix, floatValue, floatValueValue, 0.0F).texture(floatValue4, floatValue5);
        buffer.vertex(matrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValue4, floatValue5 + floatValue7);
        buffer.vertex(matrix, floatValue, floatValueValue + floatValueItem, 0.0F).texture(floatValue4, floatValue5 + floatValue7);
        buffer.vertex(matrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).texture(floatValue4 + floatValue6, floatValue5 + floatValue7);
        buffer.vertex(matrix, floatValue + floatValueResult, floatValueValue, 0.0F).texture(floatValue4 + floatValue6, floatValue5);
    }

    public int getStringHeight(String text) {
        return this.getFontHeight();
    }

    public int getFontHeight() {
        return (this.fontHeight - 8) / 2;
    }

    public int getStringWidth(String text) {
        int intValue = 0;
        for (char character : text.toCharArray()) {
            if (character < this.charData.length && this.charData[character] != null) {
                intValue += this.charData[character].width - 8 + this.charOffset;
            }
        }
        return intValue / 2;
    }

    public void setAntiAlias(boolean antiAlias) {
        if (this.antiAlias != antiAlias) {
            this.antiAlias = antiAlias;
            this.setupTexture(this.font, antiAlias, this.fractionalMetrics, this.charData);
        }
    }

    public void setFractionalMetrics(boolean fractionalMetrics) {
        if (this.fractionalMetrics != fractionalMetrics) {
            this.fractionalMetrics = fractionalMetrics;
            this.setupTexture(this.font, this.antiAlias, fractionalMetrics, this.charData);
        }
    }

    public void setFont(Font font) {
        this.font = font;
        this.setupTexture(font, this.antiAlias, this.fractionalMetrics, this.charData);
    }

    public Font getFont() {
        return this.font;
    }

    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    public boolean isFractionalMetrics() {
        return this.fractionalMetrics;
    }

    public Identifier getTextureId() {
        return this.textureId;
    }

    public int getGlTextureId() {
        return this.glTextureId;
    }
}
