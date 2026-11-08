package brova.toshiba.client.utils.render.font;

import brova.toshiba.client.modules.impl.misc.NameProtect;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class SystemFontManager {

    private static final Map<String, Map<Float, StyledFontRenderer>> ifAbsentStyledFontRendererEntries = new HashMap<>();

    private static final Map<String, Map<Float, GradientFontRenderer>> ifAbsentGradientFontRendererEntries = new HashMap<>();

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            initialized = true;
        }
    }

    public static StyledFontRenderer getFont(String text, float floatValue) {
        ifAbsentStyledFontRendererEntries.computeIfAbsent(text, input -> new HashMap<>());
        var entries = ifAbsentStyledFontRendererEntries.get(text);
        if (entries.containsKey(floatValue)) {
            return entries.get(floatValue);
        } else {
            Font fontFromTTF = FontLoader.getFontFromTTF(text, floatValue, 0);
            if (fontFromTTF == null) {
                fontFromTTF = new Font("Arial", 0, (int) floatValue);
            }
            StyledFontRenderer styledFontRenderer = new StyledFontRenderer(fontFromTTF, true, true);
            entries.put(floatValue, styledFontRenderer);
            return styledFontRenderer;
        }
    }

    public static GradientFontRenderer getGradientFont(String text, float floatValue) {
        ifAbsentGradientFontRendererEntries.computeIfAbsent(text, input -> new HashMap<>());
        var entries = ifAbsentGradientFontRendererEntries.get(text);
        if (entries.containsKey(floatValue)) {
            return entries.get(floatValue);
        } else {
            Font fontFromTTF = FontLoader.getFontFromTTF(text, floatValue, 0);
            if (fontFromTTF == null) {
                fontFromTTF = new Font("Arial", 0, (int) floatValue);
            }
            GradientFontRenderer gradientFontRenderer = new GradientFontRenderer(fontFromTTF, true, true);
            entries.put(floatValue, gradientFontRenderer);
            return gradientFontRenderer;
        }
    }

    private static String createTextFromPatchIncomingText(String text) {
        if (text == null) {
            return null;
        } else {
            NameProtect INSTANCE = NameProtect.INSTANCE;
            return INSTANCE != null && INSTANCE.isEnable() ? INSTANCE.patchIncomingText(text) : text;
        }
    }

    public static void drawStringWithFade(StyledFontRenderer styledFontRenderer, String text, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        if (text != null && !text.isEmpty() && !(floatValueResult <= 0.0F)) {
            text = createTextFromPatchIncomingText(text);
            float floatValueItem = floatValue;
            float floatValueEntry = Math.min(22.0F, Math.max(8.0F, floatValueResult * 0.35F));
            if ((intValue >>> 24 & 0xFF) > 4 && text.indexOf(167) < 0 && styledFontRenderer.getStringWidth(text) <= floatValueResult - floatValueEntry) {
                styledFontRenderer.drawString(text, floatValue, floatValueValue, intValue);
            } else {
                float floatValueCopy = floatValue + floatValueResult - floatValueEntry;
                int intValueValue = intValue >> 24 & 0xFF;
                for (int index = 0; index < text.length(); index++) {
                    String textValue = String.valueOf(text.charAt(index));
                    float stringWidth = styledFontRenderer.getStringWidth(textValue);
                    if (floatValueItem > floatValue + floatValueResult && index > 0) {
                        break;
                    }
                    int intValueResult = intValue;
                    if (floatValueItem > floatValueCopy) {
                        float floatValue2 = (floatValueItem - floatValueCopy) / floatValueEntry;
                        floatValue2 = Math.max(0.0F, Math.min(1.0F, floatValue2));
                        float floatValue3 = (float) Math.cos(floatValue2 * Math.PI / 2.0);
                        int intValueItem = (int) (intValueValue * floatValue3);
                        intValueResult = intValue & 16777215 | intValueItem << 24;
                    }
                    if ((intValueResult >> 24 & 0xFF) > 4) {
                        styledFontRenderer.drawString(textValue, floatValueItem, floatValueValue, intValueResult);
                    }
                    floatValueItem += stringWidth;
                }
            }
        }
    }

    public static StyledFontRenderer getSystemFont(String text, float floatValue) {
        String textValue = "system_" + text;
        ifAbsentStyledFontRendererEntries.computeIfAbsent(textValue, input -> new HashMap<>());
        var entries = ifAbsentStyledFontRendererEntries.get(textValue);
        if (entries.containsKey(floatValue)) {
            return entries.get(floatValue);
        } else {
            Font font = new Font(text, 0, (int) floatValue);
            StyledFontRenderer styledFontRenderer = new StyledFontRenderer(font, true, true);
            entries.put(floatValue, styledFontRenderer);
            return styledFontRenderer;
        }
    }

    public static StyledFontRenderer getSystemFont(String text, float floatValue, int intValue) {
        String textValue = "system_" + text + "_" + intValue;
        ifAbsentStyledFontRendererEntries.computeIfAbsent(textValue, input -> new HashMap<>());
        var entries = ifAbsentStyledFontRendererEntries.get(textValue);
        if (entries.containsKey(floatValue)) {
            return entries.get(floatValue);
        } else {
            Font font = new Font(text, intValue, (int) floatValue);
            StyledFontRenderer styledFontRenderer = new StyledFontRenderer(font, true, true);
            entries.put(floatValue, styledFontRenderer);
            return styledFontRenderer;
        }
    }

    public static void clearCache() {
        ifAbsentStyledFontRendererEntries.clear();
        ifAbsentGradientFontRendererEntries.clear();
        initialized = false;
    }

    public static void clearCache(String text) {
        ifAbsentStyledFontRendererEntries.remove(text);
        ifAbsentGradientFontRendererEntries.remove(text);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
