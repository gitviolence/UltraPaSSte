package brova.toshiba.client.utils.time;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.util.math.MathHelper;

public final class FpsTracker implements MinecraftAccess {

    private static int currentFPS;

    public static void updateFPS() {
        int currentFps = mc.getCurrentFps();
        currentFPS = MathHelper.lerp(0.5F, currentFps, currentFPS);
    }

    private FpsTracker() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int getCurrentFPS() {
        return currentFPS;
    }
}
