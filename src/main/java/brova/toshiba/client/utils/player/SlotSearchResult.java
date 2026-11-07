package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record SlotSearchResult(int slot, boolean found, ItemStack stack) implements MinecraftAccess {

    private static final SlotSearchResult slotSearchResult = new SlotSearchResult(-1, false, ItemStack.EMPTY);

    public static SlotSearchResult notFound() {
        return slotSearchResult;
    }

    @NotNull
    public static SlotSearchResult inOffhand(ItemStack stack) {
        return new SlotSearchResult(999, true, stack);
    }

    public boolean isHolding() {
        return mc.player == null ? false : this.isOffhand() || mc.player.getInventory().selectedSlot == this.slot;
    }

    public boolean isOffhand() {
        return this.slot == 999;
    }

    public boolean isInHotBar() {
        return this.slot >= 0 && this.slot < 9;
    }

    public void switchTo() {
        if (this.found && this.isInHotBar()) {
            InventorySearchUtil.switchTo(this.slot);
        }
    }

    public void switchToSilent() {
        if (this.found && this.isInHotBar()) {
            InventorySearchUtil.switchToSilent(this.slot);
        }
    }
}
