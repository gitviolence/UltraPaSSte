package brova.toshiba.client.configs;

import brova.toshiba.Toshiba;
import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ClientTickEvent;
import brova.toshiba.client.utils.text.ChatUtil;

/**
 * Автозагрузка и автосохранение конфига клиента.
 *
 * При входе в мир клиент сам применяет активный конфиг (последний сохранённый),
 * при выходе из мира - сам сохраняет текущее состояние в слот auto, чтобы в следующий
 * раз зайти с теми же настройками. Вручную вводить ничего не нужно.
 */
public final class AutoConfigLoader implements MinecraftAccess {

    /** Слот автосохранения: текущее состояние клиента. */
    public static final String AUTO_CONFIG_NAME = "auto";

    private boolean wasInWorld;

    private boolean appliedInWorld;

    @EventHandler
    public void onTick(ClientTickEvent event) {
        try {
            boolean inWorld = mc.world != null && mc.player != null;
            if (inWorld) {
                if (!this.appliedInWorld) {
                    this.appliedInWorld = true;
                    this.applyActiveConfig();
                }
                this.wasInWorld = true;
            } else if (this.wasInWorld) {
                this.wasInWorld = false;
                this.appliedInWorld = false;
                AutoConfigLoader.saveCurrentState();
            }
        } catch (Throwable failure) {
            System.err.println("[AutoConfigLoader] " + failure.getMessage());
        }
    }

    /** Применяет активный конфиг. Вызывается при входе в мир. */
    private void applyActiveConfig() {
        LocalConfigStorage storage = Toshiba.INSTANCE.localConfigStorage;
        if (storage == null) {
            return;
        }
        String name = storage.getLastSavedName();
        if (name == null) {
            return;
        }
        String json = storage.read(name);
        if (json == null || json.isBlank()) {
            return;
        }
        ConfigResult result = ConfigManager.apply(json);
        if (result.successful()) {
            ChatUtil.sendMessage("Конфиг " + name + " загружен автоматически.");
        } else {
            System.err.println("[AutoConfigLoader] Не удалось применить конфиг " + name + ": " + result.message());
        }
    }

    /** Сохраняет текущее состояние в слот auto. Вызывается при выходе из мира и при закрытии игры. */
    public static void saveCurrentState() {
        LocalConfigStorage storage = Toshiba.INSTANCE.localConfigStorage;
        if (storage == null) {
            return;
        }
        try {
            String json = ConfigManager.toJson();
            if (storage.save(AUTO_CONFIG_NAME, json)) {
                System.out.println("[AutoConfigLoader] Конфиг auto сохранён.");
            }
        } catch (Throwable failure) {
            System.err.println("[AutoConfigLoader] Не удалось сохранить конфиг auto: " + failure.getMessage());
        }
    }
}
