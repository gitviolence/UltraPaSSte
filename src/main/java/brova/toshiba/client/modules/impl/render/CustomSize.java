package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;

public final class CustomSize extends Module {

    public static CustomSize INSTANCE = new CustomSize();

    public final FloatSetting scale = new FloatSetting("Размер", 0.5F, 0.1F, 2.0F, 0.05F);

    public final BooleanSetting friends = new BooleanSetting("Для друзей", false);

    public CustomSize() {
        super("CustomSize", "Уменьшает размер игрока", ModuleCategory.RENDER);
        this.addSettings(this.scale, this.friends);
    }

    public float getScale() {
        return this.scale.get();
    }

    public boolean shouldScale(String text) {
        if (mc.player != null && text != null) {
            return text.equals(mc.player.getName().getString()) ? true : this.friends.isState() && Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(text);
        } else {
            return false;
        }
    }
}
