package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;

public final class InventoryUtil implements MinecraftAccess {

    public static int getItemSlot(Item item) {
        for (ItemStack stack : mc.player.getArmorItems()) {
            if (stack.getItem() == item) {
                return -2;
            }
        }
        int intValue = -1;
        for (int index = 0; index < 36; index++) {
            ItemStack stackValue = mc.player.getInventory().getStack(index);
            if (stackValue.getItem() == item) {
                intValue = index;
                break;
            }
        }
        if (intValue < 9 && intValue != -1) {
            intValue += 36;
        }
        return intValue;
    }

    public static int getItemCount(Item item) {
        if (mc.player == null) {
            return 0;
        } else {
            int intValue = 0;
            for (int index = 0; index < 36; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.getItem() == item) {
                    intValue += stack.getCount();
                }
            }
            return intValue;
        }
    }

    public static int findBestToolSlot(BlockState blockState) {
        if (mc.player != null && blockState != null) {
            int intValue = -1;
            float floatValue = 1.0F;
            for (int index = 0; index < 36; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                float miningSpeedMultiplier = stack.getMiningSpeedMultiplier(blockState);
                if (miningSpeedMultiplier > floatValue) {
                    floatValue = miningSpeedMultiplier;
                    intValue = index;
                }
            }
            return intValue;
        } else {
            return -1;
        }
    }

    public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> registryKey) {
        ItemEnchantmentsComponent orDefault = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (var registryEntry : orDefault.getEnchantments()) {
            if (registryEntry.matchesKey(registryKey)) {
                return orDefault.getLevel(registryEntry);
            }
        }
        return 0;
    }

    public static int findBestElytraSlot() {
        if (mc.player == null) {
            return -1;
        } else {
            int intValue = -1;
            double doubleValue = -1.0;
            for (int index = 0; index < 36; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.getItem() == Items.ELYTRA) {
                    int enchantmentLevel = getEnchantmentLevel(stack, Enchantments.PROTECTION);
                    int enchantmentLevelValue = getEnchantmentLevel(stack, Enchantments.UNBREAKING);
                    int enchantmentLevelResult = getEnchantmentLevel(stack, Enchantments.MENDING);
                    int maxDamage = stack.getMaxDamage();
                    int damage = stack.getDamage();
                    double doubleValueValue = (double) (maxDamage - damage) / maxDamage;
                    double doubleValueResult = enchantmentLevel * 100 + enchantmentLevelValue * 10 + (enchantmentLevelResult > 0 ? 1 : 0) + doubleValueValue * 10.0;
                    if (doubleValueResult > doubleValue) {
                        doubleValue = doubleValueResult;
                        intValue = index;
                    }
                }
            }
            return intValue;
        }
    }

    public static int findBestChestplateSlot() {
        if (mc.player == null) {
            return -1;
        } else {
            int intValue = -1;
            double doubleValue = -1.0;
            for (int index = 0; index < 36; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.getItem() instanceof ArmorItem armorItem) {
                    EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);
                    if (equippableComponent != null && equippableComponent.slot() == EquipmentSlot.CHEST) {
                        int enchantmentLevel = getEnchantmentLevel(stack, Enchantments.PROTECTION);
                        int enchantmentLevelValue = getEnchantmentLevel(stack, Enchantments.UNBREAKING);
                        int enchantmentLevelResult = getEnchantmentLevel(stack, Enchantments.MENDING);
                        int chestplatePriority = getChestplatePriority(armorItem);
                        int maxDamage = stack.getMaxDamage();
                        int damage = stack.getDamage();
                        double doubleValueValue = maxDamage == 0 ? 1.0 : (double) (maxDamage - damage) / maxDamage;
                        double doubleValueResult = chestplatePriority * 10000.0 + enchantmentLevel * 100.0 + enchantmentLevelValue * 10.0 + (enchantmentLevelResult > 0 ? 1 : 0) + doubleValueValue * 10.0;
                        if (doubleValueResult > doubleValue) {
                            doubleValue = doubleValueResult;
                            intValue = index;
                        }
                    }
                }
            }
            return intValue;
        }
    }

    public static int getChestplatePriority(Item item) {
        if (item == Items.NETHERITE_CHESTPLATE) {
            return 5;
        } else if (item == Items.DIAMOND_CHESTPLATE) {
            return 4;
        } else if (item == Items.IRON_CHESTPLATE) {
            return 3;
        } else if (item == Items.GOLDEN_CHESTPLATE) {
            return 2;
        } else if (item == Items.CHAINMAIL_CHESTPLATE) {
            return 2;
        } else {
            return item == Items.LEATHER_CHESTPLATE ? 1 : 0;
        }
    }

    public static int find(Item item, int intValue, int intValueValue) {
        if (mc.player != null) {
            for (int index = intValueValue; index >= intValue; index--) {
                if (mc.player.currentScreenHandler.syncId != 0 && mc.player.currentScreenHandler.getSlot(index).getStack().getItem() == item) {
                    return index;
                }
                if (mc.player.currentScreenHandler.syncId == 0 && mc.player.getInventory().getStack(index).getItem() == item) {
                    return index;
                }
            }
        }
        return -1;
    }

    public static void swapAndUseHvH(Item item) {
        int intValue = find(item, 9, 45);
        int intValueValue = find(item, 0, 8);
        int selectedSlot = mc.player.getInventory().selectedSlot;
        boolean usingItem = mc.player.isUsingItem();
        if (mc.player.getMainHandStack().getItem() == item) {
            if (!usingItem) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        } else if (mc.player.getOffHandStack().getItem() == item) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        } else if (usingItem) {
            if (intValueValue != -1) {
                mc.interactionManager.clickSlot(0, 36 + intValueValue, 40, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                mc.interactionManager.clickSlot(0, 36 + intValueValue, 40, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            } else if (intValue != -1) {
                mc.interactionManager.clickSlot(0, intValue, 40, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                mc.interactionManager.clickSlot(0, intValue, 40, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        } else if (intValueValue != -1) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValueValue));
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
        } else if (intValue != -1) {
            int intValueResult = -1;
            for (int index = 0; index < 8; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.isEmpty()) {
                    intValueResult = index;
                    break;
                }
                UseAction useAction = stack.getUseAction();
                if (useAction == UseAction.NONE) {
                    intValueResult = index;
                }
            }
            boolean flag = false;
            if (mc.player.isSprinting()) {
                mc.player.networkHandler.sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                mc.player.setSprinting(false);
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                if (!ModuleRegistry.sprint.isEnable()) {
                    mc.options.sprintKey.setPressed(false);
                }
                flag = true;
            }
            if (intValueResult == -1) {
                mc.interactionManager.clickSlot(0, intValue, 8, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(8));
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            } else {
                mc.interactionManager.clickSlot(0, intValue, intValueResult, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValueResult));
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                mc.interactionManager.clickSlot(0, intValue, intValueResult, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
            if (flag) {
                mc.player.networkHandler.sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
            }
        }
    }

    public static int getSlot(Item item) {
        if (mc.player == null) {
            return -1;
        } else {
            for (int index = 0; index < 36; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (!stack.isEmpty() && stack.getItem() == item) {
                    return index;
                }
            }
            return -1;
        }
    }

    public static int toContainerSlot(int intValue) {
        return intValue < 9 ? intValue + 36 : intValue;
    }

    public static void inventorySwapClick(Item item) {
        inventorySwapClick(item, false);
    }

    public static void inventorySwapClick(Item item, boolean flag) {
        if (mc.player != null && mc.interactionManager != null) {
            int slot = getSlot(item);
            if (slot != -1) {
                if (flag) {
                    sendClickSlotWithPacket(slot);
                } else {
                    int selectedSlot = mc.player.getInventory().selectedSlot;
                    boolean flagValue = slot < 9;
                    if (flagValue && slot == selectedSlot) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    } else if (flagValue) {
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                    } else {
                        int intValue = toContainerSlot(slot);
                        int intValueValue = calculateInteger(selectedSlot);
                        mc.interactionManager.clickSlot(0, intValue, intValueValue, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                        try {
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValueValue));
                            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                        } finally {
                            mc.interactionManager.clickSlot(0, intValue, intValueValue, SlotActionType.SWAP, mc.player);
                            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                        }
                    }
                }
            }
        }
    }

    private static void sendClickSlotWithPacket(int intValue) {
        if (mc.player != null && mc.interactionManager != null && intValue >= 0) {
            int intValueValue = toContainerSlot(intValue);
            mc.interactionManager.clickSlot(0, intValueValue, 40, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            try {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                mc.player.swingHand(Hand.OFF_HAND);
            } finally {
                mc.interactionManager.clickSlot(0, intValueValue, 40, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }
    }

    private static int calculateInteger(int intValue) {
        if (intValue <= 0) {
            return 1;
        } else {
            return intValue >= 8 ? 7 : intValue + 1;
        }
    }

    private InventoryUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
