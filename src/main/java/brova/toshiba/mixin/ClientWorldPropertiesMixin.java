package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.CustomWorld;
import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Properties.class)
public class ClientWorldPropertiesMixin {

    @Shadow
    private long timeOfDay;

    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void toshiba$setTimeOfDay(long timeOfDay, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            CustomWorld tweaks = ModuleRegistry.worldTweaks;
            if (tweaks != null && tweaks.isTimeEnabled()) {
                this.timeOfDay = tweaks.getForcedTime();
                ci.cancel();
            }
        }
    }
}
