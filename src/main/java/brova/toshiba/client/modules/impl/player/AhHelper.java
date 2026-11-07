package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ScreenEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.text.ChatUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class AhHelper extends Module {

    public static AhHelper INSTANCE = new AhHelper();

    public AhHelper() {
        super("AhHelper", "Помощник аукциона", ModuleCategory.PLAYER);
    }

    public static boolean isAuction(Screen screen) {
        if (!(screen instanceof HandledScreen<?> handledScreen)) {
            return false;
        } else {
            String string = handledScreen.getTitle().getString();
            String normalizedText = string.toLowerCase();
            return string.contains("Аукцион") || normalizedText.contains("0a2z") || normalizedText.contains("漢");
        }
    }

    @EventHandler
    public void onSetScreen(ScreenEvent event) {
        Screen screen = event.getScreen();
        if (screen != null) {
            boolean auction = isAuction(screen);
            ChatUtil.sendMessage("AhHelper -> " + (auction ? "§a" + auction : "§c" + auction));
        }
    }
}
