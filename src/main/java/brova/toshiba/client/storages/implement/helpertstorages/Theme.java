package brova.toshiba.client.storages.implement.helpertstorages;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.render.ColorMathUtil;

public class Theme implements MinecraftAccess {

    private String name;

    public int[] color;

    public Theme(String name, int... color) {
        this.name = name;
        this.color = color;
    }

    public int getColor(int intValue) {
        return ColorMathUtil.gradient(5, intValue, this.color);
    }

    public String getName() {
        return this.name;
    }

    public int[] getColor() {
        return this.color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(int[] color) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, color, (accessReceiver, accessValue) -> {
            accessReceiver.color = accessValue;
        });
    }
}
