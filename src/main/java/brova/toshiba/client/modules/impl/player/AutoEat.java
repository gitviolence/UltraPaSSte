package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class AutoEat extends Module {

    public static final AutoEat INSTANCE = new AutoEat();

    private final FloatSetting hungerThreshold = new FloatSetting("Количество голода", 5.0F, 1.0F, 10.0F, 1.0F);

    private boolean stateFlagForShouldSuppressCombat;

    private boolean stateFlagForApplyPressedWithUsingItem;

    private boolean stateFlagForMainHandStackWithInventory;

    private int cachedCountForUpdate = -1;

    private int cachedCountForMainHandStackWithInventory = -1;

    public AutoEat() {
        super("AutoEat", "Автоматически ест при низком голоде", ModuleCategory.PLAYER);
        this.addSettings(this.hungerThreshold);
    }

    public static boolean shouldSuppressCombat() {
        return INSTANCE != null && INSTANCE.isEnable() && INSTANCE.stateFlagForShouldSuppressCombat;
    }

    @Override
    public void onDisable() {
        this.applyPressed();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            this.applyPressed();
        } else if (mc.currentScreen != null) {
            this.applyPressed();
        } else if (!mc.player.getAbilities().creativeMode && !mc.player.isSpectator()) {
            if (!this.stateFlagForShouldSuppressCombat) {
                if (!this.isUsingItemWithOffHandStack()) {
                    return;
                }
                this.stateFlagForShouldSuppressCombat = true;
                this.cachedCountForUpdate = mc.player.getInventory().selectedSlot;
            }
            this.applyPressedWithUsingItem();
        } else {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((AutoEat) accessReceiver).applyPressed();
            });
        }
    }

    private void applyPressedWithUsingItem() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            this.applyPressed();
        } else {
            this.pauseBaritonePathing();
            if (!this.stateFlagForApplyPressedWithUsingItem) {
                Sprint.pushPause(0L);
                this.stateFlagForApplyPressedWithUsingItem = true;
            }
            mc.options.attackKey.setPressed(false);
            if (!this.isFoodLevelWithHungerManager()) {
                if (!player.isUsingItem()) {
                    this.applyPressed();
                }
            } else if (!this.isMainHandStackWithInventory()) {
                this.applyPressed();
            } else {
                Hand hand = this.createOffHandStackWithMainHandStack(player);
                if (hand == null) {
                    this.applyPressed();
                } else {
                    mc.options.useKey.setPressed(true);
                    if (!player.isUsingItem() || player.getActiveHand() != hand) {
                        mc.interactionManager.interactItem(player, hand);
                    }
                }
            }
        }
    }

    private boolean isUsingItemWithOffHandStack() {
        return this.isFoodLevelWithHungerManager() && !mc.player.isUsingItem() && (this.isOfWithUseAction(mc.player.getOffHandStack()) || this.calculateInventoryWithStack() != -1);
    }

    private boolean isFoodLevelWithHungerManager() {
        return mc.player != null && mc.player.getHungerManager().getFoodLevel() < 20 && mc.player.getHungerManager().getFoodLevel() <= this.calculateInteger();
    }

    private int calculateInteger() {
        return Math.round(this.hungerThreshold.get()) * 2;
    }

    private boolean isMainHandStackWithInventory() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return false;
        } else if (this.isOfWithUseAction(player.getOffHandStack())) {
            return true;
        } else if (this.isOfWithUseAction(player.getMainHandStack())) {
            return true;
        } else {
            int cachedCountForMainHandStackWithInventory = this.calculateInventoryWithStack();
            if (cachedCountForMainHandStackWithInventory == -1) {
                return false;
            } else if (cachedCountForMainHandStackWithInventory < 9) {
                this.stateFlagForMainHandStackWithInventory = false;
                this.cachedCountForMainHandStackWithInventory = -1;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { cachedCountForMainHandStackWithInventory }, (accessReceiver, accessArguments) -> {
                    ((AutoEat) accessReceiver).selectHotbarSlot(((int) accessArguments[0]));
                });
                return this.isOfWithUseAction(player.getMainHandStack());
            } else {
                this.selectHotbarSlot(this.cachedCountForUpdate == -1 ? player.getInventory().selectedSlot : this.cachedCountForUpdate);
                this.sendNetworkHandlerWithClickSlot(cachedCountForMainHandStackWithInventory, player.getInventory().selectedSlot);
                this.stateFlagForMainHandStackWithInventory = true;
                this.cachedCountForMainHandStackWithInventory = cachedCountForMainHandStackWithInventory;
                return this.isOfWithUseAction(player.getMainHandStack());
            }
        }
    }

    private Hand createOffHandStackWithMainHandStack(ClientPlayerEntity player) {
        if (player == null) {
            return null;
        } else if (this.isOfWithUseAction(player.getOffHandStack())) {
            return Hand.OFF_HAND;
        } else {
            return this.isOfWithUseAction(player.getMainHandStack()) ? Hand.MAIN_HAND : null;
        }
    }

    private int calculateInventoryWithStack() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return -1;
        } else {
            int selectedSlot = player.getInventory().selectedSlot;
            if (this.isOfWithUseAction(player.getInventory().getStack(selectedSlot))) {
                return selectedSlot;
            } else {
                for (int index = 0; index < 9; index++) {
                    if (index != selectedSlot && this.isOfWithUseAction(player.getInventory().getStack(index))) {
                        return index;
                    }
                }
                for (int indexValue = 9; indexValue < 36; indexValue++) {
                    if (this.isOfWithUseAction(player.getInventory().getStack(indexValue))) {
                        return indexValue;
                    }
                }
                return -1;
            }
        }
    }

    private boolean isOfWithUseAction(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            return !stack.isOf(Items.GOLDEN_APPLE) && !stack.isOf(Items.ENCHANTED_GOLDEN_APPLE) && !stack.isOf(Items.CHORUS_FRUIT) ? stack.getUseAction() == UseAction.EAT : false;
        } else {
            return false;
        }
    }

    private void selectHotbarSlot(int selectedSlot) {
        if (mc.player != null && selectedSlot >= 0 && selectedSlot <= 8 && mc.player.getInventory().selectedSlot != selectedSlot) {
            mc.player.getInventory().selectedSlot = selectedSlot;
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            }
        }
    }

    private void sendNetworkHandlerWithClickSlot(int intValue, int intValueValue) {
        if (mc.player != null && mc.interactionManager != null && intValue >= 9 && intValue <= 35 && intValueValue >= 0 && intValueValue <= 8) {
            mc.interactionManager.clickSlot(0, intValue, intValueValue, SlotActionType.SWAP, mc.player);
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }
    }

    private void applyPressed() {
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
        if (this.stateFlagForApplyPressedWithUsingItem) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
                Sprint.popPause();
            });
            this.stateFlagForApplyPressedWithUsingItem = false;
        }
        this.updateInventory();
        this.stateFlagForShouldSuppressCombat = false;
    }

    private void updateInventory() {
        if (mc.player != null && mc.interactionManager != null) {
            if (this.stateFlagForMainHandStackWithInventory && this.cachedCountForMainHandStackWithInventory != -1) {
                int intValue = this.cachedCountForUpdate == -1 ? mc.player.getInventory().selectedSlot : this.cachedCountForUpdate;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { intValue }, (accessReceiver, accessArguments) -> {
                    ((AutoEat) accessReceiver).selectHotbarSlot(((int) accessArguments[0]));
                });
                this.sendNetworkHandlerWithClickSlot(this.cachedCountForMainHandStackWithInventory, intValue);
            }
            if (this.cachedCountForUpdate != -1) {
                this.selectHotbarSlot(this.cachedCountForUpdate);
            }
            this.updateLocalState();
        } else {
            this.updateLocalState();
        }
    }

    private void updateLocalState() {
        this.stateFlagForMainHandStackWithInventory = false;
        this.cachedCountForMainHandStackWithInventory = -1;
        this.cachedCountForUpdate = -1;
    }

    private void pauseBaritonePathing() {
        try {
            Object value = findPrimaryBaritone();
            if (value == null) {
                this.updateCancelBlockBreaking();
                return;
            }
            Object valueValue = invokeNoArgMethod(value, "getPathingBehavior");
            if (valueValue == null || !Boolean.TRUE.equals(invokeNoArgMethod(valueValue, "hasPath"))) {
                this.updateCancelBlockBreaking();
                return;
            }
            Object valueResult = invokeNoArgMethod(value, "getInputOverrideHandler");
            if (valueResult != null) {
                valueResult.getClass().getMethod("clearAllKeys").invoke(valueResult);
                Object valueItem = valueResult.getClass().getMethod("getBlockBreakHelper").invoke(valueResult);
                if (valueItem != null) {
                    valueItem.getClass().getMethod("stopBreakingBlock").invoke(valueItem);
                }
            }
            valueValue.getClass().getMethod("requestPause").invoke(valueValue);
            this.updateCancelBlockBreaking();
        } catch (Throwable failure) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((AutoEat) accessReceiver).updateCancelBlockBreaking();
            });
        }
    }

    private void updateCancelBlockBreaking() {
        try {
            if (mc.interactionManager != null) {
                mc.interactionManager.cancelBlockBreaking();
            }
        } catch (Throwable failure) {
        }
    }

    private static Object findPrimaryBaritone() throws ReflectiveOperationException {
        var type = Class.forName("baritone.api.BaritoneAPI");
        Object value = type.getMethod("getProvider").invoke(null);
        return value == null ? null : value.getClass().getMethod("getPrimaryBaritone").invoke(value);
    }

    private static Object invokeNoArgMethod(Object value, String text) throws ReflectiveOperationException {
        return value.getClass().getMethod(text).invoke(value);
    }
}
