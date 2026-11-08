package brova.toshiba.client.modules.settings.implement;

import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.utils.input.KeyBindUtil;
import java.util.function.Supplier;

public class BindSetting extends SettingBase {

    private int key;

    public BindSetting(String text, int key) {
        super(text);
        this.key = key;
    }

    public BindSetting visible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }

    public boolean isPressed() {
        return this.key != -1 && KeyBindUtil.isBindHeld(this.key);
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
