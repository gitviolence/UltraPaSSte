package brova.toshiba.client.settings.impl;

import brova.toshiba.client.settings.SettingBase;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends SettingBase {

    private List<String> mods;

    private String current;

    private int index;

    public ModeSetting(String text, String textValue, String... textResult) {
        super(text);
        this.mods = Arrays.asList(textResult);
        this.index = this.mods.indexOf(textValue);
        if (this.index < 0) {
            this.index = 0;
        }
        this.current = this.mods.get(this.index);
    }

    public void set(String text) {
        int index = this.mods.indexOf(text);
        if (index >= 0) {
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                accessReceiver.current = accessValue;
            });
            this.index = index;
        }
    }

    public boolean is(String text) {
        return this.current.equals(text);
    }

    public String displayMode(String text) {
        return text;
    }

    public String displayCurrent() {
        return this.displayMode(this.current);
    }

    public ModeSetting visible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }

    public List<String> getMods() {
        return this.mods;
    }

    public String getCurrent() {
        return this.current;
    }

    public int getIndex() {
        return this.index;
    }

    public void setMods(List<String> mods) {
        this.mods = mods;
    }

    public void setCurrent(String current) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, current, (accessReceiver, accessValue) -> {
            accessReceiver.current = accessValue;
        });
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
