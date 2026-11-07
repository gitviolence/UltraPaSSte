package brova.toshiba.client.utils.text;

import brova.toshiba.client.utils.render.ColorMathUtil;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class ChatUtil {

    public static void sendMessage(Object value) {
        MinecraftClient instance = MinecraftClient.getInstance();
        if (instance.player == null) {
            System.err.println("[Toshiba] " + value);
        } else {
            MutableText mutableText = Text.literal("");
            String text = "ToshibaDLC";
            for (int index = 0; index < text.length(); index++) {
                mutableText.append(Text.literal(String.valueOf(text.charAt(index))).setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(ColorMathUtil.gradient(ColorMathUtil.getThemeColor(0), ColorMathUtil.getThemeColor(90), (float) index / text.length())))));
            }
            mutableText.append(Text.literal(" → ").setStyle(Style.EMPTY.withBold(false).withColor(TextColor.fromRgb(new Color(200, 200, 200).getRGB()))));
            mutableText.append(Text.literal(String.valueOf(value)).setStyle(Style.EMPTY.withBold(false).withColor(TextColor.fromRgb(new Color(200, 200, 200).getRGB()))));
            instance.player.sendMessage(mutableText, false);
        }
    }

    private ChatUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
