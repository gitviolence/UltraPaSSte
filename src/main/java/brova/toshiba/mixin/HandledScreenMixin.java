package brova.toshiba.mixin;

import brova.toshiba.client.modules.impl.misc.AuctionHelper;
import brova.toshiba.client.modules.impl.player.ItemScroller;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    @Nullable
    protected abstract Slot getSlotAt(double doubleValue, double doubleValueValue);

    @Shadow
    protected abstract void onMouseClick(@Nullable Slot slot, int intValue, int intValueValue, SlotActionType slotActionType);

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemScroller itemScroller = ItemScroller.INSTANCE;
        if (itemScroller.isEnable() && mc.player != null && mc.interactionManager != null) {
            long window = mc.getWindow().getHandle();
            boolean leftMousePressed = GLFW.glfwGetMouseButton(window, 0) == 1;
            boolean shiftPressed = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
            if (leftMousePressed && shiftPressed) {
                Slot slot = this.getSlotAt(mouseX, mouseY);
                if (slot != null && slot.hasStack()) {
                    if (itemScroller.canQuickMove()) {
                        this.onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
                    }
                }
            } else {
                itemScroller.resetTimer();
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void toshiba$renderAuctionHelper(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AuctionHelper m = AuctionHelper.INSTANCE;
        if (m != null && m.isEnable()) {
            Slot slot = this.getSlotAt(mouseX, mouseY);
            m.renderHandledScreen(context, (HandledScreen<?>) (Object) this, slot, this.x, this.y);
        }
    }
}
