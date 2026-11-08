package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.time.IntervalTimer;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoCodes extends Module {

    public static AutoCodes INSTANCE = new AutoCodes();

    private static final int[] integerValues = new int[] { 20, 22, 24 };

    private final ModeSetting server = new ModeSetting("Сервер", "WhiteRise", "WhiteRise");

    private final IntervalTimer intervalTimer = new IntervalTimer();

    private int cachedCount;

    private boolean loginCommandPending;

    private boolean registerCommandSent;

    public AutoCodes() {
        super("AutoCodes", "Автоматический сбор наград в /codes на сервер WhiteRise", ModuleCategory.MISC);
        this.addSettings(this.server);
    }

    @Override
    public void onEnable() {
        this.updateLocalState();
        this.intervalTimer.reset();
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand("codes");
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.updateLocalState();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        this.updateScreenHandlerWithNetworkHandler();
    }

    private void updateScreenHandlerWithNetworkHandler() {
        if (mc.player != null && mc.world != null) {
            if (this.cachedCount >= integerValues.length) {
                this.setEnabled(false);
            } else if (mc.currentScreen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getScreenHandler().getRows() == 5) {
                this.loginCommandPending = true;
                if (!this.registerCommandSent) {
                    ScreenHandler screenHandler = genericContainerScreen.getScreenHandler();
                    int intValue = integerValues[this.cachedCount];
                    if (intValue >= 0 && intValue < screenHandler.slots.size() && mc.interactionManager != null) {
                        Slot slot = screenHandler.slots.get(intValue);
                        if (slot.hasStack() && slot.getStack().getItem() == Items.CHEST_MINECART) {
                            mc.interactionManager.clickSlot(screenHandler.syncId, intValue, 0, SlotActionType.PICKUP, mc.player);
                            this.registerCommandSent = true;
                            this.cachedCount++;
                            this.intervalTimer.reset();
                        }
                    }
                }
            } else {
                this.registerCommandSent = false;
                if (this.loginCommandPending && this.intervalTimer.finished(50L) && mc.getNetworkHandler() != null) {
                    this.loginCommandPending = false;
                    mc.getNetworkHandler().sendChatCommand("codes");
                }
            }
        }
    }

    private void updateLocalState() {
        this.cachedCount = 0;
        this.loginCommandPending = false;
        this.registerCommandSent = false;
    }
}
