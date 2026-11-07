package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.settings.impl.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public final class AutoTool extends Module {

    public static AutoTool INSTANCE;

    private final BooleanSetting packetMode = new BooleanSetting("Пакетный", false);

    private final BooleanSetting silentSwitch = new BooleanSetting("Видно только для других людей", false).visible(this.packetMode::isState);

    private int previousToolSlot = -1;

    private int cachedCountForUpdateItemWithStack = -1;

    private int swappedInventorySlot = -1;

    private int cachedCountForEvent;

    private Item item;

    private boolean stateFlagForUpdateItemWithStack;

    private boolean stateFlagForUpdatePushPause;

    private boolean stateFlagForApplyPressedWithSprinting;

    public AutoTool() {
        super("AutoTool", "При ломание блоки берет предмет для ломания блока", ModuleCategory.PLAYER);
        INSTANCE = this;
        this.addSettings(this.packetMode, this.silentSwitch);
    }

    @EventHandler
    public void onEvent(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.player.isCreative()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((AutoTool) accessReceiver).updateLocalState();
            });
        } else if (this.cachedCountForEvent > 0) {
            this.applyPressedWithSprinting();
            this.cachedCountForEvent--;
            if (this.cachedCountForEvent == 1) {
                this.restoreToolSwapState();
            }
            if (this.cachedCountForEvent == 0) {
                this.applyPressedWithKeyPressed();
            }
        } else if (!mc.interactionManager.isBreakingBlock() && !mc.options.attackKey.isPressed() && this.previousToolSlot != -1) {
            this.restorePreviousTool();
        }
    }

    @EventHandler
    public void onMoveInput(DirectionalInput directionalInput) {
        if (this.cachedCountForEvent > 0) {
            directionalInput.setForward(0.0F);
            directionalInput.setStrafe(0.0F);
            boolean flag = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(directionalInput, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                ((DirectionalInput) accessReceiver).setJump(((boolean) accessArguments[0]));
            });
            directionalInput.setSneak(false);
        }
    }

    public boolean handleBlockBreaking(BlockPos position) {
        if (mc.player == null || mc.world == null || mc.player.isCreative()) {
            return false;
        } else if (this.cachedCountForEvent <= 0 && this.swappedInventorySlot == -1 && !this.stateFlagForUpdateItemWithStack) {
            if (this.cachedCountForUpdateItemWithStack != -1 && this.item != null && mc.player.getInventory().getStack(this.previousToolSlot).getItem() != this.item) {
                this.cachedCountForUpdateItemWithStack = -1;
                this.item = null;
            }
            BlockState blockState = mc.world.getBlockState(position);
            int swappedInventorySlot = this.calculateInventoryWithMiningSpeedMultiplier(blockState);
            if (swappedInventorySlot != -1) {
                if (this.previousToolSlot == -1) {
                    this.previousToolSlot = mc.player.getInventory().selectedSlot;
                }
                if (swappedInventorySlot < 9) {
                    this.sendNetworkHandlerWithInventory(swappedInventorySlot);
                } else if (this.cachedCountForUpdateItemWithStack == -1 && this.swappedInventorySlot == -1) {
                    this.swappedInventorySlot = swappedInventorySlot;
                    this.updatePushPause();
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private void sendNetworkHandlerWithInventory(int selectedSlot) {
        if (selectedSlot >= 0 && selectedSlot <= 8) {
            boolean flag = this.packetMode.isState() && this.silentSwitch.isState();
            if (flag || mc.player.getInventory().selectedSlot != selectedSlot) {
                if (this.packetMode.isState()) {
                    if (!flag) {
                        mc.player.getInventory().selectedSlot = selectedSlot;
                    }
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                    }
                } else {
                    mc.player.getInventory().selectedSlot = selectedSlot;
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                    }
                }
            }
        }
    }

    private void restorePreviousTool() {
        if (this.swappedInventorySlot != -1) {
            this.swappedInventorySlot = -1;
            this.previousToolSlot = -1;
            this.applyPressedWithKeyPressed();
        } else if (this.cachedCountForUpdateItemWithStack != -1) {
            if (this.item != null && mc.player.getInventory().getStack(this.previousToolSlot).getItem() == this.item) {
                this.stateFlagForUpdateItemWithStack = true;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((AutoTool) accessReceiver).updatePushPause();
                });
            } else {
                this.cachedCountForUpdateItemWithStack = -1;
                Object value = null;
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                    accessReceiver.item = (Item) accessValue;
                });
                this.previousToolSlot = -1;
            }
        } else {
            if (mc.player != null) {
                this.sendNetworkHandlerWithInventory(this.previousToolSlot);
            }
            this.previousToolSlot = -1;
        }
    }

    private void updatePushPause() {
        this.cachedCountForEvent = 2;
        if (!this.stateFlagForUpdatePushPause) {
            Sprint.pushPause(1000L);
            this.stateFlagForUpdatePushPause = true;
        }
        this.applyPressedWithSprinting();
    }

    private void restoreToolSwapState() {
        if (mc.player != null && mc.interactionManager != null) {
            if (this.swappedInventorySlot != -1) {
                Item item = mc.player.getInventory().getStack(this.swappedInventorySlot).getItem();
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, item, (accessReceiver, accessValue) -> {
                    accessReceiver.item = accessValue;
                });
                this.sendNetworkHandlerWithClickSlot(this.swappedInventorySlot, this.previousToolSlot);
                this.cachedCountForUpdateItemWithStack = this.swappedInventorySlot;
                this.swappedInventorySlot = -1;
            } else if (this.stateFlagForUpdateItemWithStack && this.cachedCountForUpdateItemWithStack != -1) {
                this.sendNetworkHandlerWithClickSlot(this.cachedCountForUpdateItemWithStack, this.previousToolSlot);
                this.cachedCountForUpdateItemWithStack = -1;
                this.item = null;
                this.stateFlagForUpdateItemWithStack = false;
                this.previousToolSlot = -1;
            }
        }
    }

    private void sendNetworkHandlerWithClickSlot(int intValue, int intValueValue) {
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, intValue, intValueValue, SlotActionType.SWAP, mc.player);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
        }
    }

    private void applyPressedWithSprinting() {
        mc.player.setSprinting(false);
        if (mc.player.input != null) {
            mc.player.input.movementForward = 0.0F;
            mc.player.input.movementSideways = 0.0F;
        }
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        this.stateFlagForApplyPressedWithSprinting = true;
    }

    private void applyPressedWithKeyPressed() {
        if (this.stateFlagForApplyPressedWithSprinting) {
            long handle = mc.getWindow().getHandle();
            mc.options.forwardKey.setPressed(InputUtil.isKeyPressed(handle, mc.options.forwardKey.getDefaultKey().getCode()));
            mc.options.backKey.setPressed(InputUtil.isKeyPressed(handle, mc.options.backKey.getDefaultKey().getCode()));
            mc.options.leftKey.setPressed(InputUtil.isKeyPressed(handle, mc.options.leftKey.getDefaultKey().getCode()));
            mc.options.rightKey.setPressed(InputUtil.isKeyPressed(handle, mc.options.rightKey.getDefaultKey().getCode()));
            mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(handle, mc.options.jumpKey.getDefaultKey().getCode()));
            this.stateFlagForApplyPressedWithSprinting = false;
        }
        if (this.stateFlagForUpdatePushPause) {
            Sprint.popPause();
            this.stateFlagForUpdatePushPause = false;
        }
    }

    private void updateLocalState() {
        this.previousToolSlot = -1;
        this.cachedCountForUpdateItemWithStack = -1;
        this.swappedInventorySlot = -1;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.item = (Item) accessValue;
        });
        this.stateFlagForUpdateItemWithStack = false;
        this.cachedCountForEvent = 0;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AutoTool) accessReceiver).applyPressedWithKeyPressed();
        });
    }

    private int calculateInventoryWithMiningSpeedMultiplier(BlockState blockState) {
        int intValue = -1;
        float floatValue = 1.0F;
        for (int index = 0; index < mc.player.getInventory().main.size(); index++) {
            float miningSpeedMultiplier = mc.player.getInventory().getStack(index).getMiningSpeedMultiplier(blockState);
            if (miningSpeedMultiplier > floatValue) {
                floatValue = miningSpeedMultiplier;
                intValue = index;
            }
        }
        return intValue;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.interactionManager != null) {
            if (this.cachedCountForUpdateItemWithStack != -1 && this.item != null && mc.player.getInventory().getStack(this.previousToolSlot).getItem() == this.item) {
                this.sendNetworkHandlerWithClickSlot(this.cachedCountForUpdateItemWithStack, this.previousToolSlot);
            } else if (this.previousToolSlot != -1 && this.swappedInventorySlot == -1) {
                this.sendNetworkHandlerWithInventory(this.previousToolSlot);
            }
        }
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AutoTool) accessReceiver).updateLocalState();
        });
        super.onDisable();
    }
}
