package brova.toshiba.client.utils.render.font;

import brova.toshiba.client.core.MinecraftAccess;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class MsdfFont implements MinecraftAccess {

    private final String name;

    private final AbstractTexture textureId;

    private final float atlasWidth;

    private final float atlasHeight;

    private final float range;

    private final float lineHeight;

    private final float cachedScalar;

    private final float baselineHeight;

    private final HashMap<Integer, FontGlyph> fontGlyphEntries;

    MsdfFont(String name, AbstractTexture textureId, float atlasWidth, float atlasHeight, float range, float lineHeight, float cachedScalar, float baselineHeight, HashMap<Integer, FontGlyph> fontGlyphEntries) {
        this.name = name;
        this.textureId = textureId;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.range = range;
        this.lineHeight = lineHeight;
        this.cachedScalar = cachedScalar;
        this.baselineHeight = baselineHeight;
        this.fontGlyphEntries = fontGlyphEntries;
    }

    public void setFiltered() {
        this.textureId.setFilter(true, false);
    }

    public int getTextureId() {
        return this.textureId.getGlId();
    }

    public float getAtlasWidth() {
        return this.atlasWidth;
    }

    public float getAtlasHeight() {
        return this.atlasHeight;
    }

    public float getRange() {
        return this.range;
    }

    public float getLineHeight() {
        return this.lineHeight;
    }

    public float getBaselineHeight() {
        return this.lineHeight + this.baselineHeight;
    }

    public String getName() {
        return this.name;
    }

    public void applyGlyphs(Matrix4f matrix, VertexConsumer vertices, float floatValue, String text, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        text = createTextFromReplace(text);
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == 167 && index + 1 < text.length()) {
                index++;
            } else {
                FontGlyph fontGlyph = this.fontGlyphEntries.get(Integer.valueOf(character));
                if (fontGlyph != null) {
                    floatValueResult += fontGlyph.apply(matrix, vertices, floatValue, floatValueResult, floatValueItem, floatValueEntry, intValue, intValueValue, intValueResult, intValueItem) + floatValueValue;
                }
            }
        }
    }

    public void applyGlyphsVerticalGradient(Matrix4f matrix, VertexConsumer vertices, float floatValue, String text, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy, int intValue2, int intValue3) {
        text = createTextFromReplace(text);
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == 167 && index + 1 < text.length()) {
                index++;
            } else {
                FontGlyph fontGlyph = this.fontGlyphEntries.get(Integer.valueOf(character));
                if (fontGlyph != null) {
                    floatValueResult += fontGlyph.applyVerticalGradient(matrix, vertices, floatValue, floatValueResult, floatValueItem, floatValueEntry, intValue, intValueValue, intValueResult, intValueItem, intValueEntry, intValueCopy, intValue2, intValue3) + floatValueValue;
                }
            }
        }
    }

    public void applyGlyphsHorizontalGradient(Matrix4f matrix, VertexConsumer vertices, float floatValue, String text, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy, int intValue2, int intValue3) {
        text = createTextFromReplace(text);
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == 167 && index + 1 < text.length()) {
                index++;
            } else {
                FontGlyph fontGlyph = this.fontGlyphEntries.get(Integer.valueOf(character));
                if (fontGlyph != null) {
                    floatValueResult += fontGlyph.applyHorizontalGradient(matrix, vertices, floatValue, floatValueResult, floatValueItem, floatValueEntry, intValue, intValueValue, intValueResult, intValueItem, intValueEntry, intValueCopy, intValue2, intValue3) + floatValueValue;
                }
            }
        }
    }

    public float getWidth(String text, float floatValue) {
        text = createTextFromReplace(text);
        float floatValueValue = 0.0F;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == 167 && index + 1 < text.length()) {
                index++;
            } else {
                FontGlyph fontGlyph = this.fontGlyphEntries.get(Integer.valueOf(character));
                if (fontGlyph != null) {
                    floatValueValue += fontGlyph.getWidth(floatValue);
                }
            }
        }
        return floatValueValue;
    }

    private static String createTextFromReplace(String text) {
        return text == null ? "" : text.replace("ᴀ", "A").replace("ʙ", "B").replace("ᴄ", "C").replace("ᴅ", "D").replace("ᴇ", "E").replace("ғ", "F").replace("ɢ", "G").replace("ʜ", "H").replace("ɪ", "I").replace("ᴊ", "J").replace("ᴋ", "K").replace("ʟ", "L").replace("ᴍ", "M").replace("ɴ", "N").replace("ᴏ", "O").replace("ᴘ", "P").replace("ǫ", "Q").replace("ʀ", "R").replace("ꜱ", "S").replace("ᴛ", "T").replace("ᴜ", "U").replace("ᴠ", "V").replace("ᴡ", "W").replace("ʏ", "Y").replace("ᴢ", "Z").replace("ꜰ", "F");
    }

    static String createTextFromResourceWithPresent(Identifier texture) {
        try {
            ResourceManager resourceManager = mc.getResourceManager();
            var resource = resourceManager.getResource(texture);
            if (!resource.isPresent()) {
                String path = texture.getPath();
                if (path.startsWith("msdf/")) {
                    Identifier textureValue = Identifier.of(texture.getNamespace(), "fonts/" + path);
                    resource = resourceManager.getResource(textureValue);
                }
            }
            if (resource.isPresent()) {
                InputStream inputStream = resource.get().getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String text = bufferedReader.lines().collect(Collectors.joining("\n"));
                bufferedReader.close();
                inputStream.close();
                return text;
            } else {
                throw new FileNotFoundException("Resource not found: " + texture);
            }
        } catch (Exception failure) {
            throw new RuntimeException("Failed to read resource: " + texture, failure);
        }
    }

    public static MsdfFontBuilder builder() {
        return new MsdfFontBuilder();
    }
}
