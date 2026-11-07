package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.text.ChatUtil;
import java.util.Set;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public final class ElytraSwap extends Module {

    public static ElytraSwap INSTANCE = new ElytraSwap();

    private final BindSetting swapElytraBind = new BindSetting("Свапнуть элитру", -1);

    private final BindSetting useFirework = new BindSetting("Использовать фейерверк", -1);

    private final BooleanSetting grimBypass = new BooleanSetting("Обходить Grim", true);

    private boolean stateFlag;

    private boolean stateFlagForDisable;

    private int chestSwapTicks;

    private int fireworkUseTicks;

    private boolean stateFlagForUpdatePushPause;

    private int cachedCountForDisable;

    private int pendingSlotRestoreTicks;

    private int previousSelectedSlot;

    private int clickSlot;

    private int rocketUseDelayTicks;

    public ElytraSwap() {
        super("ElytraSwap", "Автоматический свап элитр", ModuleCategory.MISC);
        this.addSettings(this.swapElytraBind, this.useFirework, this.grimBypass);
    }

    @EventHandler
    public void onInput(DirectionalInput directionalInput) {
        if (mc.player != null && this.grimBypass.isState() && (this.chestSwapTicks > 0 || this.fireworkUseTicks > 0)) {
            mc.player.setSprinting(false);
            directionalInput.setForward(0.0F);
            directionalInput.setStrafe(0.0F);
            directionalInput.setJump(false);
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onEvent(PlayerUpdateEvent event) {
        if (mc.player != null) {
            if (this.cachedCountForDisable > 0) {
                this.cachedCountForDisable += -1;
            }
            if (this.grimBypass.isState() && (this.chestSwapTicks > 0 || this.fireworkUseTicks > 0)) {
                this.applyPressed();
            }
            if (this.fireworkUseTicks > 0) {
                mc.player.setSprinting(false);
                this.fireworkUseTicks += -1;
                if (this.fireworkUseTicks == 0) {
                    this.updatePopPause();
                }
            }
            if (this.chestSwapTicks > 0) {
                mc.player.setSprinting(false);
                this.chestSwapTicks += -1;
                if (this.chestSwapTicks == 1) {
                    this.sendEquippedStackWithMessage();
                }
                if (this.chestSwapTicks == 0) {
                    this.updatePopPause();
                }
            } else {
                if (this.stateFlag) {
                    if (this.cachedCountForDisable > 0) {
                        this.stateFlag = false;
                        return;
                    }
                    if (this.grimBypass.isState()) {
                        this.updatePushPause();
                        this.chestSwapTicks = 3;
                        this.cachedCountForDisable = 1;
                    } else {
                        this.sendEquippedStackWithMessage();
                        this.cachedCountForDisable = 1;
                    }
                    this.stateFlag = false;
                }
                if (this.pendingSlotRestoreTicks > 0) {
                    if (this.rocketUseDelayTicks > 0) {
                        this.rocketUseDelayTicks += -1;
                    }
                    if (this.rocketUseDelayTicks == 0) {
                        if (this.pendingSlotRestoreTicks == 1) {
                            if (mc.interactionManager != null) {
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            }
                            this.pendingSlotRestoreTicks = 2;
                            this.rocketUseDelayTicks = 1;
                        } else if (this.pendingSlotRestoreTicks == 2) {
                            mc.player.getInventory().selectedSlot = this.previousSelectedSlot;
                            this.pendingSlotRestoreTicks = 0;
                        } else if (this.pendingSlotRestoreTicks == 10) {
                            if (mc.interactionManager != null) {
                                mc.interactionManager.clickSlot(0, this.clickSlot, 40, SlotActionType.SWAP, mc.player);
                                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                                mc.interactionManager.clickSlot(0, this.clickSlot, 40, SlotActionType.SWAP, mc.player);
                                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
                            }
                            this.pendingSlotRestoreTicks = 0;
                        }
                    }
                }
                if (this.stateFlagForDisable) {
                    this.stateFlagForDisable = false;
                    this.sendInteractItemWithInventory();
                }
            }
        }
    }

    private void sendEquippedStackWithMessage() {
        int intValue = InventoryUtil.findBestElytraSlot();
        int intValueValue = InventoryUtil.findBestChestplateSlot();
        boolean flag = mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) || mc.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty() || !Set.of(Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.LEATHER_CHESTPLATE).contains(mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem());
        if (flag) {
            if (intValueValue == -1) {
                ChatUtil.sendMessage("" + Formatting.RED + Formatting.BOLD + "Нет нагрудника!");
                this.chestSwapTicks = 0;
                this.updatePopPause();
                return;
            }
            ItemStack stack = mc.player.playerScreenHandler.getSlot(intValueValue).getStack();
            this.updateClickSlot(intValueValue);
        } else {
            if (intValue == -1) {
                ChatUtil.sendMessage("" + Formatting.RED + Formatting.BOLD + "Нет элитры!");
                this.chestSwapTicks = 0;
                this.updatePopPause();
                return;
            }
            ItemStack stackValue = mc.player.playerScreenHandler.getSlot(intValue).getStack();
            this.updateClickSlot(intValue);
        }
        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
    }

    private void updateClickSlot(int intValue) {
        if (intValue >= 0 && intValue < 9) {
            mc.interactionManager.clickSlot(0, 6, intValue, SlotActionType.SWAP, mc.player);
        } else {
            mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.SWAP, mc.player);
            mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.SWAP, mc.player);
            mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.SWAP, mc.player);
        }
    }

    private void sendInteractItemWithInventory() {
        if (mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        } else {
            int clickSlot = this.calculateOfWithStack();
            if (clickSlot != -1 && mc.interactionManager != null) {
                if (clickSlot == mc.player.getInventory().selectedSlot) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                } else if (clickSlot < 9) {
                    if (this.grimBypass.isState()) {
                        this.updatePushPause();
                        this.fireworkUseTicks = 4;
                    }
                    this.previousSelectedSlot = mc.player.getInventory().selectedSlot;
                    this.clickSlot = clickSlot;
                    mc.player.getInventory().selectedSlot = clickSlot;
                    this.pendingSlotRestoreTicks = 1;
                    this.rocketUseDelayTicks = 1;
                } else {
                    this.clickSlot = clickSlot;
                    if (this.grimBypass.isState()) {
                        this.updatePushPause();
                        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                        this.fireworkUseTicks = 2;
                        this.pendingSlotRestoreTicks = 10;
                        this.rocketUseDelayTicks = 1;
                    } else {
                        mc.interactionManager.clickSlot(0, clickSlot, 40, SlotActionType.SWAP, mc.player);
                        mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                        mc.interactionManager.clickSlot(0, clickSlot, 40, SlotActionType.SWAP, mc.player);
                    }
                }
            }
        }
    }

    private int calculateOfWithStack() {
        for (int index = 0; index < 36; index++) {
            if (mc.player.getInventory().getStack(index).isOf(Items.FIREWORK_ROCKET)) {
                return index;
            }
        }
        return -1;
    }

    private void updatePushPause() {
        if (!this.stateFlagForUpdatePushPause) {
            Sprint.pushPause(1000L);
            this.stateFlagForUpdatePushPause = true;
        }
    }

    private void updatePopPause() {
        if (this.stateFlagForUpdatePushPause && this.chestSwapTicks <= 0 && this.fireworkUseTicks <= 0) {
            this.stateFlagForUpdatePushPause = false;
            Sprint.popPause();
        }
    }

    private boolean isDamageWithMaxDamage(ItemStack stack) {
        return stack.getDamage() < stack.getMaxDamage() + -1;
    }

    @EventHandler
    public void onEvent(KeyBind keyBind) {
        if (keyBind.getKey() == this.swapElytraBind.getKey()) {
            this.stateFlag = true;
        }
        if (keyBind.getKey() == this.useFirework.getKey()) {
            this.stateFlagForDisable = true;
        }
    }

    private void applyPressed() {
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.jumpKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
        if (this.pendingSlotRestoreTicks > 0 && mc.player != null && (this.pendingSlotRestoreTicks == 1 || this.pendingSlotRestoreTicks == 2)) {
            mc.player.getInventory().selectedSlot = this.previousSelectedSlot;
        }
        this.chestSwapTicks = 0;
        this.cachedCountForDisable = 0;
        this.stateFlagForDisable = false;
        this.fireworkUseTicks = 0;
        this.pendingSlotRestoreTicks = 0;
        this.updatePopPause();
        super.onDisable();
    }
}
