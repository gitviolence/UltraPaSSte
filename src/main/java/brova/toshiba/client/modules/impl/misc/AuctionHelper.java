package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.misc.AuctionHelper.AuctionListing;
import brova.toshiba.client.utils.render.ColorMathUtil;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class AuctionHelper extends Module {

    public static AuctionHelper INSTANCE = new AuctionHelper();

    private static final Pattern pattern = Pattern.compile("\\$\\s*([\\d\\s,.]+)");

    public AuctionHelper() {
        super("AuctionHelper", "Подсвечивает лучший лот на аукционе", ModuleCategory.MISC);
    }

    public void renderHandledScreen(DrawContext context, HandledScreen<?> handledScreen, Slot slot, int intValue, int intValueValue) {
        if (mc.player != null && handledScreen != null) {
            Slot slotValue = this.isStackWithPresent(slot, handledScreen.getScreenHandler()) ? slot : this.findBestSlot(handledScreen);
            if (slotValue != null) {
                this.updateFillWithReplAlpha(context, slotValue, intValue, intValueValue);
            }
        }
    }

    public Slot findBestSlot(HandledScreen<?> handledScreen) {
        AuctionListing auctionListing = null;
        for (Slot slot : handledScreen.getScreenHandler().slots) {
            if (this.isEnabled(slot)) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    OptionalLong optionalLong = this.readItemPrice(stack);
                    if (!optionalLong.isEmpty()) {
                        AuctionListing auctionListingValue = new AuctionListing(slot, stack, optionalLong.getAsLong());
                        if (auctionListingValue.compareTimestamp > 0L && (auctionListing == null || this.isCompareWithCount(auctionListingValue, auctionListing))) {
                            auctionListing = auctionListingValue;
                        }
                    }
                }
            }
        }
        return auctionListing == null ? null : auctionListing.slot;
    }

    private boolean isCompareWithCount(AuctionListing auctionListing, AuctionListing auctionListingValue) {
        int intValue = Double.compare(auctionListing.calculateCount(), auctionListingValue.calculateCount());
        if (intValue != 0) {
            return intValue < 0;
        } else {
            int intValueValue = Long.compare(auctionListing.compareTimestamp, auctionListingValue.compareTimestamp);
            return intValueValue != 0 ? intValueValue < 0 : auctionListing.itemStack.getCount() > brova.toshiba.client.utils.runtime.AccessGuard.read(auctionListingValue, accessReceiver -> accessReceiver.itemStack).getCount();
        }
    }

    private void updateFillWithReplAlpha(DrawContext context, Slot slot, int intValue, int intValueValue) {
        int intValueResult = intValue + slot.x;
        int intValueItem = intValueValue + slot.y;
        int intValueEntry = ColorMathUtil.replAlpha(ColorMathUtil.getThemeColor(), 235);
        int intValueCopy = ColorMathUtil.replAlpha(intValueEntry, 72);
        int intValue2 = ColorMathUtil.replAlpha(intValueEntry, 32);
        context.fill(intValueResult - 2, intValueItem - 2, intValueResult + 18, intValueItem + 18, intValue2);
        context.fill(intValueResult, intValueItem, intValueResult + 16, intValueItem + 16, intValueCopy);
        context.fill(intValueResult - 2, intValueItem - 2, intValueResult + 18, intValueItem - 1, intValueEntry);
        context.fill(intValueResult - 2, intValueItem + 17, intValueResult + 18, intValueItem + 18, intValueEntry);
        context.fill(intValueResult - 2, intValueItem - 1, intValueResult - 1, intValueItem + 17, intValueEntry);
        context.fill(intValueResult + 17, intValueItem - 1, intValueResult + 18, intValueItem + 17, intValueEntry);
        context.fill(intValueResult - 1, intValueItem - 1, intValueResult + 17, intValueItem, intValueEntry);
        context.fill(intValueResult - 1, intValueItem + 16, intValueResult + 17, intValueItem + 17, intValueEntry);
        context.fill(intValueResult - 1, intValueItem, intValueResult, intValueItem + 16, intValueEntry);
        context.fill(intValueResult + 16, intValueItem, intValueResult + 17, intValueItem + 16, intValueEntry);
    }

    private boolean isEnabled(Slot slot) {
        return slot.inventory != null && !(slot.inventory instanceof PlayerInventory) && slot.isEnabled() && slot.id >= 0;
    }

    private boolean isStackWithPresent(Slot slot, ScreenHandler screenHandler) {
        if (slot == null || screenHandler == null || !screenHandler.slots.contains(slot)) {
            return false;
        } else if (!this.isEnabled(slot)) {
            return false;
        } else {
            ItemStack stack = slot.getStack();
            return !stack.isEmpty() && this.readItemPrice(stack).isPresent();
        }
    }

    private OptionalLong readItemPrice(ItemStack stack) {
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return OptionalLong.empty();
        } else {
            long MAX_VALUE = Long.MAX_VALUE;
            for (Text text : loreComponent.lines()) {
                String string = text.getString();
                if (string != null && string.contains("$")) {
                    Matcher matcher = pattern.matcher(string);
                    while (matcher.find()) {
                        String textValue = matcher.group(1).replaceAll("[^\\d]", "");
                        if (!textValue.isEmpty()) {
                            try {
                                long longValue = Long.parseLong(textValue);
                                if (longValue > 0L) {
                                    MAX_VALUE = Math.min(MAX_VALUE, longValue);
                                }
                            } catch (NumberFormatException failure) {
                            }
                        }
                    }
                }
            }
            return MAX_VALUE == Long.MAX_VALUE ? OptionalLong.empty() : OptionalLong.of(MAX_VALUE);
        }
    }

    public final static class AuctionListing {

        final Slot slot;

        final ItemStack itemStack;

        final long compareTimestamp;

        AuctionListing(Slot slot, ItemStack itemStack, long compareTimestamp) {
            this.slot = slot;
            this.itemStack = itemStack;
            this.compareTimestamp = compareTimestamp;
        }

        double calculateCount() {
            return (double) this.compareTimestamp / Math.max(1, this.itemStack.getCount());
        }
    }
}
