package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.RotationEvent;
import brova.toshiba.client.utils.input.KeyBindUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.Smoother;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private double cursorDeltaX;

    @Shadow
    private double cursorDeltaY;

    @Shadow
    private Smoother cursorXSmoother;

    @Shadow
    private Smoother cursorYSmoother;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = false)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        try {
            if (this.client.player == null) {
                return;
            }
            int actionId = action == 1 ? 1 : 0;
            KeyBindUtil.callMouse(button, actionId);
        } catch (Exception failure) {
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        try {
            if (this.client.player == null) {
                return;
            }
            if (!EventBus.hasListeners(RotationEvent.class)) {
                return;
            }
            double rawMouseX = this.cursorDeltaX;
            double rawMouseY = this.cursorDeltaY;
            double sensitivity = this.client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
            double scaled = sensitivity * sensitivity * sensitivity * 8.0;
            double i;
            double j;
            if (this.client.options.smoothCameraEnabled) {
                i = this.cursorXSmoother.smooth(this.cursorDeltaX * scaled, timeDelta * scaled);
                j = this.cursorYSmoother.smooth(this.cursorDeltaY * scaled, timeDelta * scaled);
            } else if (this.client.options.getPerspective().isFirstPerson() && this.client.player.isUsingSpyglass()) {
                this.cursorXSmoother.clear();
                this.cursorYSmoother.clear();
                i = this.cursorDeltaX * sensitivity * sensitivity * sensitivity;
                j = this.cursorDeltaY * sensitivity * sensitivity * sensitivity;
            } else {
                this.cursorXSmoother.clear();
                this.cursorYSmoother.clear();
                i = this.cursorDeltaX * scaled;
                j = this.cursorDeltaY * scaled;
            }
            int invert = this.client.options.getInvertYMouse().getValue() ? -1 : 1;
            RotationEvent event = new RotationEvent(i, j * invert, rawMouseX != 0.0 || rawMouseY != 0.0);
            EventBus.invoke(event);
            if (!event.isCancelled()) {
                this.client.getTutorialManager().onUpdateMouse(event.getYaw(), event.getPitch());
                this.client.player.changeLookDirection(event.getYaw(), event.getPitch());
            }
            this.cursorDeltaX = 0.0;
            this.cursorDeltaY = 0.0;
            ci.cancel();
        } catch (Exception failure) {
        }
    }
}
