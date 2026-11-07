package brova.toshiba.client.modules;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.gui.hud.notification.NotificationManager;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.SettingBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Module implements MinecraftAccess {

    private String name;

    private String description;

    private int key;

    private ModuleCategory category;

    private boolean open;

    private boolean enable;

    private final List<SettingBase> settings = new ArrayList<>();

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = -1;
    }

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.description = "NULLABLE";
        this.category = category;
        this.key = -1;
    }

    public void onEnable() {
        this.enable = true;
        EventBus.register(this);
        NotificationManager.push(this.name, this.category.getIcons(), true);
    }

    public void onDisable() {
        this.enable = false;
        EventBus.unregister(this);
        NotificationManager.push(this.name, this.category.getIcons(), false);
    }

    public void toggle() {
        if (PanicController.isActive()) {
            this.enable = false;
            EventBus.unregister(this);
        } else {
            this.enable = !this.enable;
            if (this.enable) {
                this.onEnable();
            } else {
                this.onDisable();
            }
        }
    }

    public void setEnabled(boolean enable) {
        if (enable && PanicController.isActive()) {
            this.enable = false;
            EventBus.unregister(this);
        } else {
            boolean enableValue = this.enable;
            this.enable = enable;
            try {
                if (enable) {
                    this.onEnable();
                } else if (enableValue) {
                    this.onDisable();
                }
            } catch (Exception failure) {
                this.enable = false;
                this.onDisable();
            }
        }
    }

    public void addSettings(SettingBase... setting) {
        if (setting != null && setting.length != 0) {
            Arrays.stream(setting).filter(Objects::nonNull).forEach(this.settings::add);
        }
    }

    public String getDisplayName() {
        return this.name;
    }

    public String getDisplayDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getKey() {
        return this.key;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public List<SettingBase> getSettings() {
        return this.settings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setCategory(ModuleCategory category) {
        this.category = category;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public enum ModuleCategory {

        COMBAT("Combat", "a"),
        MOVEMENT("Movement", "c"),
        RENDER("Render", "b"),
        PLAYER("Player", "c"),
        MISC("Misc", "f"),
        GHOST("Ghost", "f"),
        CONFIG("Configs", "e"),
        FRIENDS("Friends", "w");

        private final String name;

        private final String icons;

        private ModuleCategory(String nullxx, String nullxxx) {
            this.name = nullxx;
            this.icons = nullxxx;
        }

        public String getName() {
            return this.name;
        }

        public String getIcons() {
            return this.icons;
        }
    }
}
