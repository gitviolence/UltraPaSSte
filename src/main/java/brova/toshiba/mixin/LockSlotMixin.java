package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.DropItemEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class LockSlotMixin {

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (EventBus.hasListeners(DropItemEvent.class)) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            DropItemEvent event = new DropItemEvent(player.getInventory().selectedSlot);
            event.call();
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }
}
