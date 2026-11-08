package brova.toshiba.client.utils.render.font;

import brova.toshiba.client.core.MinecraftAccess;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

public class MsdfFontBuilder {

    private String cachedText = "?";

    private Identifier texture;

    private Identifier resourceTexture;

    public MsdfFontBuilder name(String cachedText) {
        this.cachedText = cachedText;
        return this;
    }

    public MsdfFontBuilder data(String text) {
        this.texture = Identifier.of("toshiba", "msdf/" + text + "/font.json");
        return this;
    }

    public MsdfFontBuilder dataPath(String text) {
        this.texture = Identifier.of("toshiba", text);
        return this;
    }

    public MsdfFontBuilder atlas(String text) {
        this.resourceTexture = Identifier.of("toshiba", "msdf/" + text + "/font.png");
        return this;
    }

    public MsdfFontBuilder atlasPath(String text) {
        this.resourceTexture = Identifier.of("toshiba", text);
        return this;
    }

    public MsdfFont build() {
        String text = MsdfFont.createTextFromResourceWithPresent(this.texture);
        JsonObject asJsonObject = JsonParser.parseString(text).getAsJsonObject();
        JsonObject asJsonObjectValue = asJsonObject.getAsJsonObject("atlas");
        float asFloat = asJsonObjectValue.get("width").getAsFloat();
        float asFloatValue = asJsonObjectValue.get("height").getAsFloat();
        float asFloatResult = asJsonObjectValue.get("distanceRange").getAsFloat();
        JsonObject asJsonObjectResult = asJsonObject.getAsJsonObject("metrics");
        float asFloatItem = asJsonObjectResult.get("lineHeight").getAsFloat();
        float asFloatEntry = asJsonObjectResult.get("ascender").getAsFloat();
        float asFloatCopy = asJsonObjectResult.get("descender").getAsFloat();
        HashMap<Integer, FontGlyph> entries = new HashMap<>();
        for (JsonElement element : asJsonObject.getAsJsonArray("glyphs")) {
            JsonObject asJsonObjectItem = element.getAsJsonObject();
            int asInt = asJsonObjectItem.get("unicode").getAsInt();
            float asFloat2 = asJsonObjectItem.get("advance").getAsFloat();
            float floatValue = 0.0F;
            float floatValueValue = 0.0F;
            float floatValueResult = 0.0F;
            float floatValueItem = 0.0F;
            if (asJsonObjectItem.has("planeBounds") && !asJsonObjectItem.get("planeBounds").isJsonNull()) {
                JsonObject asJsonObjectEntry = asJsonObjectItem.getAsJsonObject("planeBounds");
                floatValue = asJsonObjectEntry.get("left").getAsFloat();
                floatValueValue = asJsonObjectEntry.get("top").getAsFloat();
                floatValueResult = asJsonObjectEntry.get("right").getAsFloat();
                floatValueItem = asJsonObjectEntry.get("bottom").getAsFloat();
            }
            float floatValueEntry = 0.0F;
            float floatValueCopy = 0.0F;
            float floatValue2 = 0.0F;
            float floatValue3 = 0.0F;
            if (asJsonObjectItem.has("atlasBounds") && !asJsonObjectItem.get("atlasBounds").isJsonNull()) {
                JsonObject asJsonObjectCopy = asJsonObjectItem.getAsJsonObject("atlasBounds");
                floatValueEntry = asJsonObjectCopy.get("left").getAsFloat();
                floatValueCopy = asJsonObjectCopy.get("top").getAsFloat();
                floatValue2 = asJsonObjectCopy.get("right").getAsFloat();
                floatValue3 = asJsonObjectCopy.get("bottom").getAsFloat();
            }
            FontGlyph fontGlyph = new FontGlyph(asInt, asFloat2, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, asFloat, asFloatValue);
            entries.put(asInt, fontGlyph);
        }
        Identifier resourceTexture = this.resourceTexture;
        if (MinecraftAccess.mc.getResourceManager() != null) {
            var resource = MinecraftAccess.mc.getResourceManager().getResource(this.resourceTexture);
            if (!resource.isPresent()) {
                String path = this.resourceTexture.getPath();
                if (path.startsWith("msdf/")) {
                    Identifier texture = Identifier.of(this.resourceTexture.getNamespace(), "fonts/" + path);
                    if (MinecraftAccess.mc.getResourceManager().getResource(texture).isPresent()) {
                        resourceTexture = texture;
                    }
                }
            }
        }
        AbstractTexture textureValue = MinecraftAccess.mc.getTextureManager().getTexture(resourceTexture);
        return new MsdfFont(this.cachedText, textureValue, asFloat, asFloatValue, asFloatResult, asFloatItem, asFloatEntry, asFloatCopy, entries);
    }
}
