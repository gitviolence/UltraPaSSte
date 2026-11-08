package brova.toshiba.client.utils.render.font;

import java.awt.Font;
import java.io.InputStream;

public class FontLoader {

    public static Font getFontFromTTF(String text, float floatValue, int intValue) {
        try (InputStream resourceAsStream = openFontStream(text)) {
            if (resourceAsStream == null) {
                return null;
            }
            Font font = Font.createFont(intValue, resourceAsStream);
            return font.deriveFont(floatValue);
        } catch (Exception failure) {
            System.err.println("[Fonts] Failed to load " + text + ": " + failure.getMessage());
            return null;
        }
    }

    private static InputStream openFontStream(String text) {
        InputStream stream = FontLoader.class.getResourceAsStream("/assets/toshiba/ttf/" + text);
        if (stream == null) {
            stream = FontLoader.class.getResourceAsStream("/assets/toshiba/fonts/ttf/" + text);
        }
        return stream;
    }
}
