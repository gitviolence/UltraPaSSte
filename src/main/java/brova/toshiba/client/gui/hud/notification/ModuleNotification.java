package brova.toshiba.client.gui.hud.notification;

public class ModuleNotification {

    public final String moduleName;

    public final String categoryIcon;

    public final boolean enabled;

    public final String customText;

    public final long startTime;

    public ModuleNotification(String moduleName, String categoryIcon, boolean enabled, String customText, long startTime) {
        this.moduleName = moduleName;
        this.categoryIcon = categoryIcon;
        this.enabled = enabled;
        this.customText = customText;
        this.startTime = startTime;
    }

    public boolean isCustom() {
        return this.customText != null && !this.customText.isEmpty();
    }
}
