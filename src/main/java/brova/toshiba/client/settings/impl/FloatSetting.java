package brova.toshiba.client.settings.impl;

import brova.toshiba.client.settings.SettingBase;
import java.util.function.Supplier;
import net.minecraft.util.math.MathHelper;

public class FloatSetting extends SettingBase {

    private float value;

    private final float min;

    private final float max;

    private final float increment;

    private boolean active;

    public FloatSetting(String text, float value, float min, float max, float increment) {
        super(text);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public Number getValue() {
        return MathHelper.clamp(this.value, this.getMin(), this.getMax());
    }

    public void setValue(float value) {
        this.value = MathHelper.clamp(value, this.getMin(), this.getMax());
    }

    public float get() {
        return this.getValue().floatValue();
    }

    public FloatSetting visible(Supplier<Boolean> supplier) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, supplier, (accessReceiver, accessValue) -> {
            accessReceiver.visible = accessValue;
        });
        return this;
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    public float getIncrement() {
        return this.increment;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
