package brova.toshiba.mixin;

import brova.toshiba.client.utils.time.TimerController;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dynamic.class)
public class RenderTickCounterDynamicMixin {

    @Shadow
    private float lastFrameDuration;

    @Shadow
    private float tickDelta;

    @Shadow
    private long prevTimeMillis;

    @Final
    @Shadow
    private float tickTime;

    @Inject(method = "beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
    private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        float timer = TimerController.getTimer();
        if (timer != 1.0F) {
            this.lastFrameDuration = (float) (timeMillis - this.prevTimeMillis) / this.tickTime * timer;
            this.prevTimeMillis = timeMillis;
            this.tickDelta = this.tickDelta + this.lastFrameDuration;
            int i = (int) this.tickDelta;
            this.tickDelta -= i;
            cir.setReturnValue(i);
        }
    }
}
