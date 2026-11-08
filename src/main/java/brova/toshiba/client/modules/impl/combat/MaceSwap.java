package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class MaceSwap extends Module {

    private final BindSetting swapMaceBind = new BindSetting("Свапнуть булаву", -1);

    private final BooleanSetting legitMode = new BooleanSetting("Легит", true);

    private int clickSlot = -1;

    private int cachedCountForMoveInput;

    private int cachedCountForBinding;

    private boolean stateFlagForBinding;

    private boolean stateFlagForUpdateLocalState;

    private boolean stateFlagForApplyPressedWithSprinting;

    public MaceSwap() {
        super("MaceSwap", "Меняет предмет в руке на булаву", ModuleCategory.COMBAT);
        this.addSettings(this.swapMaceBind, this.legitMode);
    }

    @EventHandler
    public void onBinding(KeyBind keyBind) {
        if (mc.currentScreen == null && mc.player != null && mc.world != null && this.swapMaceBind.getKey() != -1 && keyBind.getKey() == this.swapMaceBind.getKey() && this.cachedCountForBinding == 0) {
            this.stateFlagForBinding = true;
        }
    }

    @EventHandler
    public void onMoveInput(DirectionalInput directionalInput) {
        if (this.cachedCountForMoveInput > 0) {
            directionalInput.setForward(0.0F);
            float floatValue = 0.0F;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(directionalInput, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((DirectionalInput) accessReceiver).setStrafe(((float) accessArguments[0]));
            });
            directionalInput.setJump(false);
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            if (this.cachedCountForBinding > 0) {
                this.cachedCountForBinding--;
            }
            if (this.cachedCountForMoveInput > 0) {
                this.applyPressedWithSprinting();
                this.cachedCountForMoveInput--;
                if (this.cachedCountForMoveInput == 1) {
                    this.updateClickSlotWithInventory();
                }
                if (this.cachedCountForMoveInput == 0) {
                    this.applyPressedWithKeyPressed();
                }
            } else if (this.stateFlagForBinding) {
                this.stateFlagForBinding = false;
                if (this.isOfWithMainHandStack()) {
                    this.cachedCountForBinding = 2;
                    if (this.legitMode.isState()) {
                        this.cachedCountForMoveInput = 2;
                        this.pauseSprint();
                        this.applyPressedWithSprinting();
                    } else {
                        this.updateClickSlotWithInventory();
                    }
                }
            }
        } else {
            this.resetSwapSequence();
        }
    }

    private boolean isOfWithMainHandStack() {
        if (!mc.player.getMainHandStack().isOf(Items.MACE)) {
            this.clickSlot = this.calculateStackWithSlot();
            return this.clickSlot != -1;
        } else {
            return this.clickSlot >= 9 && this.clickSlot < 36;
        }
    }

    private void updateClickSlotWithInventory() {
        if (this.clickSlot >= 9 && this.clickSlot < 36) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, this.clickSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        }
    }

    private int calculateStackWithSlot() {
        for (int index = 9; index < 36; index++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(index).getStack();
            if (stack.isOf(Items.MACE)) {
                return index;
            }
        }
        return -1;
    }

    private void applyPressedWithSprinting() {
        mc.player.setSprinting(false);
        mc.player.input.movementForward = 0.0F;
        mc.player.input.movementSideways = 0.0F;
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
        this.resumeSprint();
    }

    private void pauseSprint() {
        if (!this.stateFlagForUpdateLocalState) {
            long longValue = 1000L;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { longValue }, (accessReceiver, accessArguments) -> {
                Sprint.pushPause(((long) accessArguments[0]));
            });
            this.stateFlagForUpdateLocalState = true;
        }
    }

    private void resumeSprint() {
        if (this.stateFlagForUpdateLocalState) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
                Sprint.popPause();
            });
            this.stateFlagForUpdateLocalState = false;
        }
    }

    private void resetSwapSequence() {
        this.stateFlagForBinding = false;
        this.cachedCountForMoveInput = 0;
        this.cachedCountForBinding = 0;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((MaceSwap) accessReceiver).applyPressedWithKeyPressed();
        });
    }

    @Override
    public void onDisable() {
        this.resetSwapSequence();
        super.onDisable();
    }
}
