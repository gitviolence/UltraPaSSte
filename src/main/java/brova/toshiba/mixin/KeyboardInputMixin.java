package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.DirectionalInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        if (EventBus.hasListeners(DirectionalInput.class)) {
            DirectionalInput eventInput = new DirectionalInput(this.movementForward, this.movementSideways, this.playerInput.jump(), this.playerInput.sneak());
            eventInput.call();
            float forward = eventInput.getForward();
            float strafe = eventInput.getStrafe();
            this.playerInput = new PlayerInput(forward > 0.0F, forward < 0.0F, strafe > 0.0F, strafe < 0.0F, eventInput.isJump(), eventInput.isSneak(), this.playerInput.sprint());
            this.movementForward = forward;
            this.movementSideways = strafe;
        }
    }
}
