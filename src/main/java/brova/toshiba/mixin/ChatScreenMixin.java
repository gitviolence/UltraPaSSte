package brova.toshiba.mixin;

import brova.toshiba.client.gui.hud.Draggable;
import brova.toshiba.client.gui.hud.DraggableStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Unique
    private boolean toshiba$leftPressed;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (Draggable draggable : DraggableStorage.draggables.values()) {
            if (draggable.getModule().isEnable() && draggable.onClick(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();
        boolean leftPressed = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 0) == 1;
        if (this.toshiba$leftPressed && !leftPressed) {
            for (Draggable draggable : DraggableStorage.draggables.values()) {
                draggable.onRelease(0);
            }
        }
        this.toshiba$leftPressed = leftPressed;
        for (Draggable draggable : DraggableStorage.draggables.values()) {
            if (draggable.getModule().isEnable()) {
                draggable.onDraw(mouseX, mouseY, window, context.getMatrices());
            }
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        this.toshiba$leftPressed = false;
        for (Draggable draggable : DraggableStorage.draggables.values()) {
            draggable.onRelease(0);
        }
    }
}
