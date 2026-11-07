package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;

public final class ItemScroller extends Module {

    public static ItemScroller INSTANCE = new ItemScroller();

    public final FloatSetting delay = new FloatSetting("Задержка", 50.0F, 0.0F, 200.0F, 1.0F);

    private long lastUpdateTimestamp;

    public ItemScroller() {
        super("ItemScroller", "Убирает задержку на действия в инвентаре", ModuleCategory.PLAYER);
        this.addSettings(this.delay);
    }

    public boolean canQuickMove() {
        long lastUpdateTimestamp = System.currentTimeMillis();
        if (lastUpdateTimestamp - this.lastUpdateTimestamp < (long) this.delay.get()) {
            return false;
        } else {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            return true;
        }
    }

    public void resetTimer() {
        this.lastUpdateTimestamp = 0L;
    }

    @Override
    public void onDisable() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((ItemScroller) accessReceiver).resetTimer();
        });
        super.onDisable();
    }
}
