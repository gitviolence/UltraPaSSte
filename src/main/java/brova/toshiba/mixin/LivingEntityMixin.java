package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.PostTravelEvent;
import brova.toshiba.client.event.impl.VelocityEvent;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.SwingAnimations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            if (ModuleRegistry.INSTANCE != null) {
                SwingAnimations tweaks = ModuleRegistry.swingAnimations;
                if (tweaks != null && tweaks.isEnable() && tweaks.smoothEnabled.isState()) {
                    cir.setReturnValue((int) tweaks.slowAnimationSpeed.get());
                }
            }
        }
    }

    @Inject(method = "calcGlidingVelocity", at = @At("RETURN"), cancellable = true)
    private void onCalcGlidingVelocity(Vec3d oldVelocity, CallbackInfoReturnable<Vec3d> cir) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            VelocityEvent event = new VelocityEvent(cir.getReturnValue());
            event.call();
            cir.setReturnValue(event.getVelocity());
        }
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void onTravel(CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().player && EventBus.hasListeners(PostTravelEvent.class)) {
            new PostTravelEvent().call();
        }
    }
}
