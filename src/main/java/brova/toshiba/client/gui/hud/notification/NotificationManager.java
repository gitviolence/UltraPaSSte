package brova.toshiba.client.gui.hud.notification;

import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.modules.impl.misc.ClientSounds;
import brova.toshiba.client.utils.audio.SoundUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager {

    public static final long DURATION_MS = 2500L;

    private static final long lastUpdateTimestampForUpdateCurrentTimeMillisWithEquals = System.currentTimeMillis();

    private static final List<ModuleNotification> iteratorModuleNotificationEntries = new ArrayList<>();

    public static void push(String text, String textValue, boolean flag) {
        if (!PanicController.isActive() && text != null && !text.isEmpty()) {
            iteratorModuleNotificationEntries.add(new ModuleNotification(text, textValue, flag, null, System.currentTimeMillis()));
            updateCurrentTimeMillisWithEquals(text, flag);
        }
    }

    public static void pushCustom(String text, String textValue) {
        if (!PanicController.isActive() && text != null && !text.isEmpty()) {
            iteratorModuleNotificationEntries.add(new ModuleNotification(text, textValue, false, text, System.currentTimeMillis()));
        }
    }

    public static List<ModuleNotification> getActive() {
        long currentTimeMillis = System.currentTimeMillis();
        var iterator = iteratorModuleNotificationEntries.iterator();
        while (iterator.hasNext()) {
            ModuleNotification moduleNotification = iterator.next();
            if (currentTimeMillis - moduleNotification.startTime > 2500L) {
                iterator.remove();
            }
        }
        return iteratorModuleNotificationEntries;
    }

    public static void clear() {
        iteratorModuleNotificationEntries.clear();
    }

    private static void updateCurrentTimeMillisWithEquals(String text, boolean flag) {
        if (System.currentTimeMillis() - lastUpdateTimestampForUpdateCurrentTimeMillisWithEquals >= 4000L) {
            ClientSounds INSTANCE = ClientSounds.INSTANCE;
            if (INSTANCE != null && INSTANCE.isEnable() && !"ClientSounds".equals(text)) {
                float floatValue = 1.0F;
                SoundUtil.playSound(flag ? "module_enable.wav" : "module_disable.wav", INSTANCE.volume.get() / INSTANCE.volume.getMax(), floatValue);
            }
        }
    }
}
