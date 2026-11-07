package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

public final class InventorySearchUtil implements MinecraftAccess {

    private static int switchTo = -1;

    public static int getItemCount(Item item) {
        if (mc.player == null) {
            return 0;
        } else {
            int intValue = 0;
            for (int index = 0; index < mc.player.getInventory().size(); index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.isOf(item)) {
                    intValue += stack.getCount();
                }
            }
            return intValue;
        }
    }

    public static SlotSearchResult getAxe() {
        return createSlotSearchResultFromNotFoundWithStack(input -> input.getItem() instanceof AxeItem, false);
    }

    public static SlotSearchResult getAxeHotBar() {
        return createSlotSearchResultFromNotFoundWithStack(input -> input.getItem() instanceof AxeItem, true);
    }

    public static SlotSearchResult getPickAxe() {
        return createSlotSearchResultFromNotFoundWithStack(input -> input.getItem() instanceof PickaxeItem, false);
    }

    public static SlotSearchResult getPickAxeHotbar() {
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> InventorySearchUtil.getPickAxeHotBar());
    }

    public static SlotSearchResult getPickAxeHotBar() {
        return createSlotSearchResultFromNotFoundWithStack(input -> input.getItem() instanceof PickaxeItem, true);
    }

    public static SlotSearchResult getSword() {
        return createSlotSearchResultFromNotFoundWithStack(input -> input.getItem() instanceof SwordItem, false);
    }

    public static SlotSearchResult getSwordHotBar() {
        return createSlotSearchResultFromNotFoundWithStack(input -> input.getItem() instanceof SwordItem, true);
    }

    public static SlotSearchResult getSkull() {
        return findInHotBar(input -> input.isOf(Items.SKELETON_SKULL) || input.isOf(Items.WITHER_SKELETON_SKULL) || input.isOf(Items.CREEPER_HEAD) || input.isOf(Items.PLAYER_HEAD) || input.isOf(Items.ZOMBIE_HEAD));
    }

    public static int getElytra() {
        if (mc.player == null) {
            return -1;
        } else {
            for (ItemStack stack : mc.player.getInventory().armor) {
                if (stack.isOf(Items.ELYTRA) && stack.getDamage() < stack.getMaxDamage() - 1) {
                    return -2;
                }
            }
            for (int index = 0; index < 36; index++) {
                ItemStack stackValue = mc.player.getInventory().getStack(index);
                if (stackValue.isOf(Items.ELYTRA) && stackValue.getDamage() < stackValue.getMaxDamage() - 1) {
                    return index < 9 ? index + 36 : index;
                }
            }
            return -1;
        }
    }

    public static SlotSearchResult findInHotBar(ItemStackPredicate itemStackPredicate) {
        if (mc.player != null) {
            if (itemStackPredicate.isValid(mc.player.getOffHandStack())) {
                return SlotSearchResult.inOffhand(mc.player.getOffHandStack());
            }
            for (int index = 0; index < 9; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (itemStackPredicate.isValid(stack)) {
                    return new SlotSearchResult(index, true, stack);
                }
            }
        }
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> SlotSearchResult.notFound());
    }

    public static SlotSearchResult findItemInHotBar(List<Item> items) {
        return findInHotBar(input -> items.contains(input.getItem()));
    }

    public static SlotSearchResult findItemInHotBar(Item... item) {
        return findItemInHotBar(Arrays.asList(item));
    }

    public static SlotSearchResult findInInventory(ItemStackPredicate itemStackPredicate) {
        if (mc.player != null) {
            for (int index = 35; index >= 0; index--) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (itemStackPredicate.isValid(stack)) {
                    return new SlotSearchResult(index, true, stack);
                }
            }
        }
        return SlotSearchResult.notFound();
    }

    public static SlotSearchResult findItemInInventory(List<Item> items) {
        return findInInventory(input -> items.contains(input.getItem()));
    }

    public static SlotSearchResult findItemInInventory(Item... item) {
        return findItemInInventory(Arrays.asList(item));
    }

    public static SlotSearchResult findBlockInHotBar(@NotNull List<Block> blocks) {
        return findItemInHotBar(blocks.stream().map(Block::asItem).toList());
    }

    public static SlotSearchResult findBlockInHotBar(Block... block) {
        return findItemInHotBar(Arrays.stream(block).map(Block::asItem).toList());
    }

    public static SlotSearchResult findBlockInInventory(@NotNull List<Block> blocks) {
        return findItemInInventory(blocks.stream().map(Block::asItem).toList());
    }

    public static SlotSearchResult findBlockInInventory(Block... block) {
        return findItemInInventory(Arrays.stream(block).map(Block::asItem).toList());
    }

    public static void saveSlot() {
        if (mc.player != null) {
            switchTo = mc.player.getInventory().selectedSlot;
        }
    }

    public static void returnSlot() {
        if (switchTo != -1) {
            switchTo(switchTo);
        }
        switchTo = -1;
    }

    public static void saveAndSwitchTo(int intValue) {
        saveSlot();
        switchTo(intValue);
    }

    public static void switchTo(int selectedSlot) {
        if (mc.player != null && mc.getNetworkHandler() != null && selectedSlot >= 0 && selectedSlot <= 8 && mc.player.getInventory().selectedSlot != selectedSlot) {
            mc.player.getInventory().selectedSlot = selectedSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
        }
    }

    public static void switchToSilent(int intValue) {
        if (mc.player != null && mc.getNetworkHandler() != null && intValue >= 0 && intValue <= 8) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(intValue));
        }
    }

    public static SlotSearchResult getAntiWeaknessItem() {
        if (mc.player == null) {
            return SlotSearchResult.notFound();
        } else {
            Item item = mc.player.getMainHandStack().getItem();
            return !(item instanceof SwordItem) && !(item instanceof PickaxeItem) && !(item instanceof AxeItem) && !(item instanceof ShovelItem) ? findInHotBar(input -> input.getItem() instanceof SwordItem || input.getItem() instanceof PickaxeItem || input.getItem() instanceof AxeItem || input.getItem() instanceof ShovelItem) : new SlotSearchResult(mc.player.getInventory().selectedSlot, true, mc.player.getMainHandStack());
        }
    }

    public static float getHitDamage(@NotNull ItemStack stack, PlayerEntity player) {
        if (mc.player != null && mc.world != null) {
            float floatValue = calculateOrDefaultWithModifiers(stack);
            if (mc.player.fallDistance > 0.0F) {
                floatValue += floatValue / 2.0F;
            }
            if (mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                int intValue = Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
                floatValue += 3.0F * intValue;
            }
            return DamageUtil.getDamageLeft(player, floatValue, mc.world.getDamageSources().generic(), player.getArmor(), (float) player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
        } else {
            return 0.0F;
        }
    }

    public static SlotSearchResult findBedInHotBar() {
        return findInHotBar(input -> input.getItem() instanceof BedItem);
    }

    public static SlotSearchResult findBed() {
        return findInInventory(input -> input.getItem() instanceof BedItem);
    }

    public static Item getItem(String text) {
        if (text == null) {
            return Items.AIR;
        } else {
            String normalizedText = text.toLowerCase();
            for (Block block : Registries.BLOCK) {
                if (block.getTranslationKey().replace("block.minecraft.", "").equals(normalizedText)) {
                    return block.asItem();
                }
            }
            for (Item item : Registries.ITEM) {
                if (item.getTranslationKey().replace("item.minecraft.", "").equals(normalizedText)) {
                    return item;
                }
            }
            return Items.DIRT;
        }
    }

    public static int getBedsCount() {
        if (mc.player == null) {
            return 0;
        } else {
            int intValue = 0;
            for (int index = 0; index < mc.player.getInventory().size(); index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.getItem() instanceof BedItem) {
                    intValue += stack.getCount();
                }
            }
            return intValue;
        }
    }

    private static SlotSearchResult createSlotSearchResultFromNotFoundWithStack(ItemStackPredicate itemStackPredicate, boolean flag) {
        if (mc.player == null) {
            return SlotSearchResult.notFound();
        } else {
            int intValue = -1;
            float floatValue = 0.0F;
            int intValueValue = flag ? 8 : 35;
            for (int index = 0; index <= intValueValue; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (itemStackPredicate.isValid(stack)) {
                    float floatValueValue = calculateOrDefaultWithModifiers(stack);
                    if (floatValueValue > floatValue) {
                        floatValue = floatValueValue;
                        intValue = index;
                    }
                }
            }
            return intValue == -1 ? SlotSearchResult.notFound() : new SlotSearchResult(intValue, true, mc.player.getInventory().getStack(intValue));
        }
    }

    private static float calculateOrDefaultWithModifiers(ItemStack stack) {
        AttributeModifiersComponent orDefault = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        double doubleValue = 1.0;
        for (Entry entry : orDefault.modifiers()) {
            if (entry.attribute().equals(EntityAttributes.ATTACK_DAMAGE)) {
                doubleValue += entry.modifier().value();
            }
        }
        return (float) doubleValue;
    }

    public static boolean isHolding(Item item) {
        return mc.player != null && (mc.player.getMainHandStack().isOf(item) || mc.player.getOffHandStack().isOf(item));
    }

    public static Hand getHand(Item item) {
        if (mc.player == null) {
            return null;
        } else if (mc.player.getOffHandStack().isOf(item)) {
            return Hand.OFF_HAND;
        } else {
            return mc.player.getMainHandStack().isOf(item) ? Hand.MAIN_HAND : null;
        }
    }

    private InventorySearchUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
