package brova.toshiba.client.storages.implement;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;

public class ModuleStorage implements MinecraftAccess {

    public ModuleStorage() {
        this.updateInitialize();
    }

    private void updateInitialize() {
        ModuleRegistry.INSTANCE.initialize();
    }
}
