package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.text.ChatUtil;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

public class PvPSave extends Module {

    private boolean stateFlag;

    public PvPSave() {
        super("PvPSave", "Предотвращает выход во время PvP", ModuleCategory.PLAYER);
    }

    public boolean shouldBlockHub(String text) {
        if (!this.isEnable() || !this.isTrimWithStartsWith(text)) {
            return false;
        } else if (!isCombatActive()) {
            this.stateFlag = false;
            return false;
        } else if (this.stateFlag) {
            this.stateFlag = false;
            return false;
        } else {
            this.stateFlag = true;
            ChatUtil.sendMessage("Режим боя активен. Чтобы выйти на хаб, повторите команду /hub.");
            return true;
        }
    }

    public boolean shouldConfirmDisconnect() {
        return this.isEnable() && isCombatActive();
    }

    @Override
    public void onDisable() {
        this.stateFlag = false;
        super.onDisable();
    }

    public static boolean isCombatActive() {
        if (mc.inGameHud == null) {
            return false;
        } else {
            try {
                BossBarHud bossBarHud = mc.inGameHud.getBossBarHud();
                for (Field field : bossBarHud.getClass().getDeclaredFields()) {
                    if (Map.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Map<?, ?> entries = (Map<?, ?>) field.get(bossBarHud);
                        return entries.values().stream().filter(ClientBossBar.class::isInstance).map(ClientBossBar.class::cast).map(input -> input.getName().getString().toLowerCase(Locale.ROOT)).anyMatch(inputValue -> inputValue.contains("pvp") || inputValue.contains("пвп") || inputValue.contains("боя") || inputValue.contains("бой"));
                    }
                }
            } catch (ReflectiveOperationException failure) {
            }
            return false;
        }
    }

    private boolean isTrimWithStartsWith(String text) {
        String textValue = text.trim();
        if (textValue.startsWith("/")) {
            textValue = textValue.substring(1);
        }
        return textValue.equalsIgnoreCase("hub");
    }
}
