package brova.toshiba.client.utils.render.font;

import java.util.HashMap;
import net.minecraft.client.util.math.MatrixStack;

public class FontManager {

    private static final HashMap<String, MsdfFont> msdfFontEntries = new HashMap<>();

    private static final HashMap<String, TextRenderer[]> ifAbsentTextRendererEntries = new HashMap<>();

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            initialized = true;
            updateBuildWithData("sf_regular");
            updateBuildWithData("icon");
            updateBuildWithData("icon1");
            updateBuildWithData("iconnew");
            updateBuildWithData("suisse");
            updateBuildWithData("rain");
            updateBuildWithData("topgorob");
            updateBuildWithData("logo");
            updateBuildWithData("manrope");
            updateBuildWithData("roboto_flex_medium");
            updateBuildWithData("roboto_flex_semibold");
            updateBuildWithData("watermark");
            updateBuildWithData("cooldown");
            updateBuildWithDataPath("hud", "msdf/hud/icons.png", "msdf/hud/icons.json");
            updateBuildWithDataPath("toshiba", "msdf/toshiba/icons.png", "msdf/toshiba/icons.json");
        }
    }

    private static void updateBuildWithData(String text) {
        try {
            MsdfFont font = MsdfFont.builder().atlas(text).data(text).build();
            msdfFontEntries.put(text, font);
            TextRenderer[] textRenderers = new TextRenderer[100];
            for (int index = 8; index < 100; index++) {
                textRenderers[index] = new TextRenderer(font, (float) index);
            }
            ifAbsentTextRendererEntries.put(text, textRenderers);
        } catch (Exception failure) {
            System.err.println("[Fonts] Failed to load " + text + ": " + failure.getMessage());
        }
    }

    private static void updateBuildWithDataPath(String text, String textValue, String textResult) {
        try {
            MsdfFont font = MsdfFont.builder().atlasPath(textValue).dataPath(textResult).build();
            msdfFontEntries.put(text, font);
            TextRenderer[] textRenderers = new TextRenderer[100];
            for (int index = 8; index < 100; index++) {
                textRenderers[index] = new TextRenderer(font, (float) index);
            }
            ifAbsentTextRendererEntries.put(text, textRenderers);
        } catch (Exception failure) {
            System.err.println("[Fonts] Failed to load " + text + ": " + failure.getMessage());
        }
    }

    public static TextRenderer getFont(String text, int intValue) {
        if (!initialized) {
            init();
        }
        String textValue = text.endsWith(".ttf") ? text.substring(0, text.length() - 4) : text;
        if (intValue < 8) {
            intValue = 8;
        }
        if (intValue >= 100) {
            intValue = 99;
        }
        TextRenderer[] textRenderers = ifAbsentTextRendererEntries.get(textValue);
        if (textRenderers != null && textRenderers[intValue] != null) {
            return textRenderers[intValue];
        } else {
            MsdfFont font = msdfFontEntries.get(textValue);
            if (font == null) {
                updateBuildWithData(textValue);
                font = msdfFontEntries.get(textValue);
            }
            if (font == null) {
                return null;
            } else {
                TextRenderer textRenderer = new TextRenderer(font, (float) intValue);
                textRenderers = ifAbsentTextRendererEntries.computeIfAbsent(textValue, input -> new TextRenderer[100]);
                textRenderers[intValue] = textRenderer;
                return textRenderer;
            }
        }
    }

    public static void drawText(MatrixStack matrices, String text, int intValue, String textValue, float floatValue, float floatValueValue, int intValueValue) {
        TextRenderer font = getFont(text, intValue);
        if (font != null) {
            font.draw(matrices, textValue, floatValue, floatValueValue, intValueValue);
        }
    }

    public static float getTextWidth(String text, int intValue, String textValue) {
        TextRenderer font = getFont(text, intValue);
        return font != null ? font.getWidth(textValue) : 0.0F;
    }

    public static float getTextHeight(String text, int intValue) {
        TextRenderer font = getFont(text, intValue);
        return font != null ? font.getHeight() : 0.0F;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
