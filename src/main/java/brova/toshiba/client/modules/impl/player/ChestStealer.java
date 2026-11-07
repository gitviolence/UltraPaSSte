package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

public final class ChestStealer extends Module {

    public static ChestStealer INSTANCE = new ChestStealer();

    private final FloatSetting delay = new FloatSetting("Задержка", 100.0F, 0.0F, 1000.0F, 1.0F);

    private final BooleanSetting randomizeSlots = new BooleanSetting("Брать по рандому", false);

    private long ifPresentTimestamp = 0L;

    public ChestStealer() {
        super("ChestStealer", "Автоматически лутает предметы с сундуков", ModuleCategory.PLAYER);
        this.addSettings(this.delay, this.randomizeSlots);
    }

    @EventHandler
    private void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.interactionManager != null) {
            ScreenHandler currentScreenHandler = mc.player.currentScreenHandler;
            if (currentScreenHandler != null && currentScreenHandler != mc.player.playerScreenHandler && (currentScreenHandler instanceof GenericContainerScreenHandler || currentScreenHandler instanceof HopperScreenHandler)) {
                long ifPresentTimestamp = System.currentTimeMillis();
                long longValue = (long) this.delay.get();
                if (ifPresentTimestamp - this.ifPresentTimestamp >= longValue) {
                    DefaultedList<Slot> slots = currentScreenHandler.slots;
                    this.createOptionalFromStackWithEmpty(slots, currentScreenHandler).ifPresent(input -> {
                        if (mc.player.currentScreenHandler == currentScreenHandler) {
                            mc.interactionManager.clickSlot(currentScreenHandler.syncId, input.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                            this.ifPresentTimestamp = ifPresentTimestamp;
                        }
                    });
                }
            }
        }
    }

    private Optional<Slot> createOptionalFromStackWithEmpty(List<Slot> slots, ScreenHandler screenHandler) {
        int intValue = this.calculateInventory(screenHandler);
        if (intValue > 0 && intValue <= slots.size()) {
            var values = slots.subList(0, intValue);
            ArrayList<net.minecraft.screen.slot.Slot> valuesValue = new ArrayList<>();
            for (Slot slot : values) {
                if (slot.hasStack() && !slot.getStack().isEmpty() && !mc.player.getItemCooldownManager().isCoolingDown(slot.getStack())) {
                    valuesValue.add(slot);
                }
            }
            if (valuesValue.isEmpty()) {
                return Optional.empty();
            } else if (this.randomizeSlots.isState()) {
                int intValueValue = ThreadLocalRandom.current().nextInt(valuesValue.size());
                return Optional.of(valuesValue.get(intValueValue));
            } else {
                return Optional.of(valuesValue.get(0));
            }
        } else {
            return Optional.empty();
        }
    }

    private int calculateInventory(ScreenHandler screenHandler) {
        if (screenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            Inventory inventory = genericContainerScreenHandler.getInventory();
            return inventory.size();
        } else {
            return screenHandler instanceof HopperScreenHandler ? 5 : 0;
        }
    }

    @Override
    public void onDisable() {
        this.ifPresentTimestamp = 0L;
        super.onDisable();
    }
}
