package brova.toshiba.client.utils.player;

import net.minecraft.item.ItemStack;

public interface ItemStackPredicate {

    boolean isValid(ItemStack stack);
}
