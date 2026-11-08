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
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class ClickPearl extends Module {

    public static ClickPearl INSTANCE = new ClickPearl();

    private final BindSetting keyBind = new BindSetting("Кнопка", -1);

    private final BooleanSetting legitMode = new BooleanSetting("Легит", true);

    private boolean stateFlagForUpdateLocalState;

    private int cachedCount;

    private int pearlInventorySlot = -1;

    private int temporaryHotbarSlot = -1;

    private boolean stateFlagForUpdatePushPause;

    public ClickPearl() {
        super("ClickPearl", "Кидает перку по внутреннему бинду", ModuleCategory.MISC);
        this.addSettings(this.keyBind, this.legitMode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.updateLocalState();
    }

    @Override
    public void onDisable() {
        this.updateLocalState();
        super.onDisable();
    }

    @EventHandler
    public void onEvent(KeyBind keyBind) {
        if (mc.currentScreen == null && keyBind.getKey() == this.keyBind.getKey()) {
            this.stateFlagForUpdateLocalState = true;
        }
    }

    @EventHandler
    public void onInput(DirectionalInput directionalInput) {
        if (mc.player != null && this.legitMode.isState() && this.cachedCount > 0) {
            mc.player.setSprinting(false);
            directionalInput.setForward(0.0F);
            directionalInput.setStrafe(0.0F);
            directionalInput.setJump(false);
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onEvent(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            this.updateLocalState();
        } else if (this.pearlInventorySlot != -1) {
            this.sendClickSlotWithPacket();
        } else if (this.legitMode.isState() && this.cachedCount > 0) {
            mc.player.setSprinting(false);
            this.cachedCount += -1;
            if (this.cachedCount == 1) {
                this.sendInteractItemWithSwingHand();
            }
            if (this.cachedCount == 0) {
                this.updatePopPause();
            }
        } else if (this.stateFlagForUpdateLocalState) {
            this.stateFlagForUpdateLocalState = false;
            int intValue = this.calculateSlotWithToContainerSlot();
            if (intValue == -1) {
                return;
            }
            if (this.legitMode.isState()) {
                this.updatePushPause();
                this.cachedCount = 2;
            } else {
                this.sendInteractItemWithSwingHand();
            }
        }
    }

    private int calculateSlotWithToContainerSlot() {
        int slot = InventoryUtil.getSlot(Items.ENDER_PEARL);
        return slot == -1 ? -1 : InventoryUtil.toContainerSlot(slot);
    }

    private void sendInteractItemWithSwingHand() {
        if (mc.interactionManager != null) {
            if (mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
            } else {
                int pearlInventorySlot = this.calculateSlotWithToContainerSlot();
                if (pearlInventorySlot != -1) {
                    int temporaryHotbarSlot = mc.player.getInventory().selectedSlot;
                    int intValue = 36 + temporaryHotbarSlot;
                    if (pearlInventorySlot == intValue) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                    } else if (pearlInventorySlot >= 36 && pearlInventorySlot <= 44) {
                        int intValueValue = pearlInventorySlot + -36;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValueValue));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(temporaryHotbarSlot));
                    } else {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, pearlInventorySlot, temporaryHotbarSlot, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        this.pearlInventorySlot = pearlInventorySlot;
                        this.temporaryHotbarSlot = temporaryHotbarSlot;
                    }
                }
            }
        }
    }

    private void sendClickSlotWithPacket() {
        if (this.pearlInventorySlot != -1) {
            if (mc.player != null && mc.interactionManager != null) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, this.pearlInventorySlot, this.temporaryHotbarSlot, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                this.pearlInventorySlot = -1;
                this.temporaryHotbarSlot = -1;
            } else {
                this.pearlInventorySlot = -1;
                this.temporaryHotbarSlot = -1;
            }
        }
    }

    private void updatePushPause() {
        if (!this.stateFlagForUpdatePushPause) {
            Sprint.pushPause(1000L);
            this.stateFlagForUpdatePushPause = true;
        }
    }

    private void updatePopPause() {
        if (this.stateFlagForUpdatePushPause) {
            this.stateFlagForUpdatePushPause = false;
            Sprint.popPause();
        }
    }

    private void updateLocalState() {
        this.sendClickSlotWithPacket();
        this.stateFlagForUpdateLocalState = false;
        this.cachedCount = 0;
        this.updatePopPause();
    }
}
