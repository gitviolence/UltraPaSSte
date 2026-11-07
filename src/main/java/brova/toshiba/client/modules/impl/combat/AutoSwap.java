package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.gui.swap.SwapScreen;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.profile.Profiled;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

@Profiled
public final class AutoSwap extends Module {

    public static AutoSwap INSTANCE = new AutoSwap();

    private final ModeSetting moveMode = new ModeSetting("Режим перемещения", "Двойной", "Двойной", "Тройной");

    private final BooleanSetting legitMode = new BooleanSetting("Легит", true);

    private final ModeSetting primaryItem = new ModeSetting("Первый предмет", "Шар", "Шар", "Тотем", "Гепл", "Щит").visible(() -> this.moveMode.is("Двойной"));

    private final ModeSetting secondaryItem = new ModeSetting("Второй предмет", "Тотем", "Шар", "Тотем", "Гепл", "Щит").visible(() -> this.moveMode.is("Двойной"));

    private final BindSetting swapKey = new BindSetting("Кнопка свапа", -1);

    private final SwapScreen swapScreen = new SwapScreen();

    private long lastUpdateMillis;

    private int legitSwapInProgress = -1;

    private boolean stateFlag;

    public AutoSwap() {
        super("AutoSwap", "Быстрая смена предметов в левой руке", ModuleCategory.COMBAT);
        this.addSettings(this.moveMode, this.legitMode, this.primaryItem, this.secondaryItem, this.swapKey);
    }

    @Override
    public void onEnable() {
        this.legitSwapInProgress = -1;
        this.lastUpdateMillis = 0L;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.legitSwapInProgress = -1;
        this.lastUpdateMillis = 0L;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AutoSwap) accessReceiver).updatePopPause();
        });
        super.onDisable();
    }

    @EventHandler
    public void onBinding(KeyBind keyBind) {
        if (mc.player != null && mc.world != null && keyBind.getKey() == this.swapKey.getKey()) {
            if (this.moveMode.is("Тройной")) {
                if (mc.currentScreen == null) {
                    mc.setScreen(this.swapScreen);
                }
            } else if (mc.currentScreen == null && this.legitSwapInProgress == -1) {
                this.legitSwapInProgress = this.calculateCurrentWithItem();
                if (this.legitSwapInProgress != -1) {
                    if (this.legitMode.isState()) {
                        this.updateLocalState();
                        this.lastUpdateMillis = System.currentTimeMillis() + 50L;
                    } else {
                        this.sendPacket();
                        this.legitSwapInProgress = -1;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInput(DirectionalInput directionalInput) {
        if (this.legitMode.isState() && this.legitSwapInProgress != -1) {
            directionalInput.setForward(0.0F);
            directionalInput.setStrafe(0.0F);
            directionalInput.setJump(false);
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && this.legitSwapInProgress != -1 && this.legitMode.isState()) {
            mc.player.setSprinting(false);
            if (System.currentTimeMillis() >= this.lastUpdateMillis) {
                this.sendPacket();
                this.legitSwapInProgress = -1;
                this.updatePopPause();
            }
        }
    }

    public void swapToItem(Item item) {
        if (mc.player != null && item != null) {
            int intValue = this.calculateStackWithSlot(item);
            this.updateCurrentTimeMillis(intValue);
        }
    }

    public void swapToItem(ItemStack stack) {
        if (mc.player != null && stack != null && !stack.isEmpty()) {
            int intValue = this.calculateItemWithStack(stack);
            this.updateCurrentTimeMillis(intValue);
        }
    }

    private void updateCurrentTimeMillis(int legitSwapInProgress) {
        if (legitSwapInProgress != -1) {
            this.legitSwapInProgress = legitSwapInProgress;
            if (this.legitMode.isState()) {
                this.updateLocalState();
                this.lastUpdateMillis = System.currentTimeMillis() + 50L;
            } else {
                this.sendPacket();
                this.legitSwapInProgress = -1;
            }
        }
    }

    private void sendPacket() {
        if (this.legitSwapInProgress != -1 && mc.player != null && mc.interactionManager != null) {
            this.clickSlot(this.legitSwapInProgress);
            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }
    }

    private int calculateCurrentWithItem() {
        int intValue = this.calculateStackWithSlot(this.createItemFromText(this.primaryItem.getCurrent()));
        int intValueValue = this.calculateStackWithSlot(this.createItemFromText(this.secondaryItem.getCurrent()));
        return intValue != -1 && mc.player.getOffHandStack().getItem() != mc.player.playerScreenHandler.getSlot(intValue).getStack().getItem() ? intValue : intValueValue;
    }

    private void clickSlot(int intValue) {
        mc.interactionManager.clickSlot(0, intValue, 40, SlotActionType.SWAP, mc.player);
    }

    private int calculateStackWithSlot(Item item) {
        int intValue = -1;
        for (int index = 9; index < 45; index++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(index).getStack();
            if (stack.getItem() == item) {
                if (stack.hasEnchantments()) {
                    return index;
                }
                if (intValue == -1) {
                    intValue = index;
                }
            }
        }
        return intValue;
    }

    private int calculateItemWithStack(ItemStack stack) {
        int intValue = -1;
        for (int index = 9; index < 45; index++) {
            ItemStack stackValue = mc.player.playerScreenHandler.getSlot(index).getStack();
            if (ItemStack.areItemsAndComponentsEqual(stackValue, stack)) {
                return index;
            }
            if (intValue == -1 && stackValue.getItem() == stack.getItem()) {
                intValue = index;
            }
        }
        return intValue;
    }

    private Item createItemFromText(String text) {
        return switch(text) {
            case "Шар" ->
                Items.PLAYER_HEAD;
            case "Тотем" ->
                Items.TOTEM_OF_UNDYING;
            case "Гепл" ->
                Items.GOLDEN_APPLE;
            case "Щит" ->
                Items.SHIELD;
            default ->
                Items.AIR;
        };
    }

    private void updateLocalState() {
        if (!this.stateFlag) {
            long longValue = 1000L;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { longValue }, (accessReceiver, accessArguments) -> {
                Sprint.pushPause(((long) accessArguments[0]));
            });
            this.stateFlag = true;
        }
    }

    private void updatePopPause() {
        if (this.stateFlag) {
            this.stateFlag = false;
            Sprint.popPause();
        }
    }

    public boolean isLegitSwapInProgress() {
        return this.legitMode.isState() && this.legitSwapInProgress != -1;
    }

    public BindSetting getSwapKey() {
        return this.swapKey;
    }
}
