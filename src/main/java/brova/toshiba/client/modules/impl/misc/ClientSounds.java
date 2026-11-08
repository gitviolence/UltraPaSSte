package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.audio.SoundUtil;

public final class ClientSounds extends Module {

    public static ClientSounds INSTANCE = new ClientSounds();

    public final FloatSetting volume = new FloatSetting("Громкость", 50.0F, 1.0F, 100.0F, 0.5F);

    public ClientSounds() {
        super("ClientSounds", "Добавляет звуки клиента", ModuleCategory.MISC);
        this.addSettings(this.volume);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        SoundUtil.playSound("module_enable.wav", this.volume.get() / this.volume.getMax(), 1.0F);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SoundUtil.playSound("module_disable.wav", this.volume.get() / this.volume.getMax(), 1.0F);
    }
}
