package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DropItemEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.misc.ServerHelper;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.text.ChatUtil;
import java.util.List;

public final class LockSlot extends Module {

    public static LockSlot INSTANCE = new LockSlot();

    private final MultiSetting lockedSlots = new MultiSetting("Заблокированные слоты", new BooleanSetting("1", false), new BooleanSetting("2", false), new BooleanSetting("3", false), new BooleanSetting("4", false), new BooleanSetting("5", false), new BooleanSetting("6", false), new BooleanSetting("7", false), new BooleanSetting("8", false), new BooleanSetting("9", false));

    private final BooleanSetting lockOnlyInPvp = new BooleanSetting("Блокировать только в PvP", true);

    public LockSlot() {
        super("LockSlot", "Запрещает выбрасывать предметы из выбранных слотов", ModuleCategory.PLAYER);
        this.addSettings(this.lockOnlyInPvp, this.lockedSlots);
    }

    @EventHandler
    public void onDropItem(DropItemEvent event) {
        int slot = event.getSlot();
        if (slot >= 0 && slot < ((List) brova.toshiba.client.utils.runtime.AccessGuard.invoke(this.lockedSlots, new Object[] {}, (accessReceiver, accessArguments) -> ((MultiSetting) accessReceiver).getSettings())).size() && (!this.lockOnlyInPvp.isState() || ServerHelper.isPvp()) && this.lockedSlots.getSettings().get(slot).isState()) {
            ChatUtil.sendMessage("Выброс из слота \"" + (slot + 1) + "\" отключен модулем LockSlot");
            event.cancel();
        }
    }
}
