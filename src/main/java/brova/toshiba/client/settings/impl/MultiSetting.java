package brova.toshiba.client.settings.impl;

import brova.toshiba.client.settings.SettingBase;
import java.util.List;
import java.util.function.Supplier;

public class MultiSetting extends SettingBase {

    public List<BooleanSetting> settings;

    public MultiSetting(String text, BooleanSetting... setting) {
        super(text);
        this.settings = List.of(setting);
    }

    public MultiSetting of(String text, BooleanSetting... setting) {
        return new MultiSetting(text, setting);
    }

    public boolean is(String text) {
        BooleanSetting setting = this.createBooleanSettingFromEqualsIgnoreCaseWithName(text);
        return setting != null && setting.isState();
    }

    public void set(String text, boolean flag) {
        BooleanSetting setting = this.createBooleanSettingFromEqualsIgnoreCaseWithName(text);
        if (setting != null) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(setting, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                ((BooleanSetting) accessReceiver).setState(((boolean) accessArguments[0]));
            });
        }
    }

    public MultiSetting visible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }

    private BooleanSetting createBooleanSettingFromEqualsIgnoreCaseWithName(String text) {
        for (BooleanSetting setting : this.settings) {
            if (setting.name().equalsIgnoreCase(text)) {
                return setting;
            }
        }
        return null;
    }

    public List<BooleanSetting> getSettings() {
        return this.settings;
    }

    public void setSettings(List<BooleanSetting> settings) {
        this.settings = settings;
    }
}
