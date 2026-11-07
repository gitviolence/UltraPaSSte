package brova.toshiba.client.core;

import brova.toshiba.Toshiba;
import brova.toshiba.client.configs.ConfigManager;
import brova.toshiba.client.configs.ConfigResult;
import brova.toshiba.client.configs.LocalConfigStorage;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.storages.implement.CommandStorage;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import brova.toshiba.client.storages.implement.FriendStorage;
import brova.toshiba.client.storages.implement.MacroStorage;
import brova.toshiba.client.storages.implement.ModuleStorage;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.storages.implement.StaffStorage;
import brova.toshiba.client.storages.implement.ThemeStorage;
import brova.toshiba.client.storages.implement.WaypointStorage;
import brova.toshiba.client.utils.time.TpsCalculator;

public class Initializer implements MinecraftAccess {

    public void onInitialize() {
        EventBus.register(this);
        this.initStorages();
        this.loadLastSavedConfig();
    }

    /** При старте клиента подтягиваем последний сохранённый локальный конфиг. */
    private void loadLastSavedConfig() {
        LocalConfigStorage storage = Toshiba.INSTANCE.localConfigStorage;
        if (storage == null) {
            return;
        }
        try {
            String json = storage.loadLastSaved();
            if (json == null || json.isBlank()) {
                return;
            }
            ConfigResult result = ConfigManager.apply(json);
            if (!result.successful()) {
                System.err.println("[Toshiba] Last config was not applied: " + result.message());
            }
        } catch (Throwable failure) {
            System.err.println("[Toshiba] Failed to load the last config: " + failure.getMessage());
        }
    }

    public void initStorages() {
        Toshiba INSTANCE = Toshiba.INSTANCE;
        ModuleStorage moduleStorage = new ModuleStorage();
        brova.toshiba.client.utils.runtime.AccessGuard.write(INSTANCE, moduleStorage, (accessReceiver, accessValue) -> {
            accessReceiver.moduleStorage = accessValue;
        });
        Toshiba.INSTANCE.themeStorage = new ThemeStorage();
        Toshiba.INSTANCE.tpsCalc = new TpsCalculator();
        EventBus.register(Toshiba.INSTANCE.tpsCalc);
        Toshiba.INSTANCE.freeLookStorage = new FreeLookStorage();
        Toshiba.INSTANCE.rotationStorage = new RotationStorage();
        Toshiba.INSTANCE.friendStorage = new FriendStorage();
        INSTANCE = Toshiba.INSTANCE;
        MacroStorage macroStorage = new MacroStorage();
        brova.toshiba.client.utils.runtime.AccessGuard.write(INSTANCE, macroStorage, (accessReceiver, accessValue) -> {
            accessReceiver.macroStorage = accessValue;
        });
        INSTANCE = Toshiba.INSTANCE;
        StaffStorage staffStorage = new StaffStorage();
        brova.toshiba.client.utils.runtime.AccessGuard.write(INSTANCE, staffStorage, (accessReceiver, accessValue) -> {
            accessReceiver.staffStorage = accessValue;
        });
        Toshiba.INSTANCE.waypointStorage = new WaypointStorage();
        Toshiba.INSTANCE.commandStorage = new CommandStorage();
        Toshiba.INSTANCE.localConfigStorage = new LocalConfigStorage();
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
            ConfigManager.captureDefaults();
        });
    }
}
