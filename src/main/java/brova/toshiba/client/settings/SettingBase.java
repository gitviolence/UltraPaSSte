package brova.toshiba.client.settings;

import brova.toshiba.client.core.MinecraftAccess;
import java.awt.Color;
import java.util.function.Supplier;

public abstract class SettingBase implements MinecraftAccess {

    private final String cachedText;

    public Supplier<Boolean> visible = () -> true;

    public Color color = Color.WHITE;

    public SettingBase(String cachedText) {
        this.cachedText = cachedText;
    }

    public Boolean visible() {
        return this.visible.get();
    }

    public String displayName() {
        return this.cachedText;
    }

    public String name() {
        return this.cachedText;
    }

    public Color color() {
        return this.color;
    }
}
