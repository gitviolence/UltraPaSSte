package brova.toshiba.client.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface MinecraftAccess {

    MinecraftClient mc = MinecraftClient.getInstance();

    Window mw = mc.getWindow();
}
