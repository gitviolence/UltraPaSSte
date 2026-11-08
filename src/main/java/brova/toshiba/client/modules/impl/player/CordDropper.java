package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;

public final class CordDropper extends Module {

    public static CordDropper INSTANCE = new CordDropper();

    private final BindSetting sendKey = new BindSetting("Клавиша отправки сообщения", -1);

    private final BooleanSetting localChat = new BooleanSetting("Локальный чат", false);

    public CordDropper() {
        super("CordDropper", "Отправляет координаты игрока в чат по бинду", ModuleCategory.PLAYER);
        this.addSettings(this.sendKey, this.localChat);
    }

    @EventHandler
    public void onBinding(KeyBind keyBind) {
        if (mc.currentScreen == null && mc.player != null && mc.world != null && this.sendKey.getKey() != -1 && keyBind.getKey() == this.sendKey.getKey()) {
            this.updateBlockXWithBlockY();
        }
    }

    private void updateBlockXWithBlockY() {
        if (mc.player != null && mc.player.networkHandler != null) {
            int blockX = mc.player.getBlockX();
            int blockY = mc.player.getBlockY();
            int blockZ = mc.player.getBlockZ();
            String text = (this.localChat.isState() ? "" : "!") + blockX + " " + blockY + " " + blockZ;
            mc.player.networkHandler.sendChatMessage(text);
        }
    }

    public BindSetting getSendKey() {
        return this.sendKey;
    }

    public BooleanSetting getLocalChat() {
        return this.localChat;
    }
}
