package brova.toshiba.mixin;

import brova.toshiba.Toshiba;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

    @Inject(method = "main", at = @At("HEAD"))
    private static void onMain(String[] args, CallbackInfo ci) {
        if (Toshiba.INSTANCE.isServer) {
            try {
                Toshiba.INSTANCE.closeMinecraft();
            } catch (Exception failure) {
                System.err.println("[MainMixin] " + failure.getMessage());
            }
            Toshiba.INSTANCE.isServer = false;
        }
    }
}
