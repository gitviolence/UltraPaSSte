package brova.toshiba.client.settings.impl;

import brova.toshiba.client.settings.SettingBase;
import java.util.function.Supplier;

public class BooleanSetting extends SettingBase {

    private boolean state;

    public BooleanSetting(String text, boolean state) {
        super(text);
        this.state = state;
    }

    public static BooleanSetting of(String text, boolean flag) {
        return new BooleanSetting(text, flag);
    }

    public BooleanSetting visible(Supplier<Boolean> supplier) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, supplier, (accessReceiver, accessValue) -> {
            accessReceiver.visible = accessValue;
        });
        return this;
    }

    public boolean isState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
