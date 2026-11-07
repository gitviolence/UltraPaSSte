package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void toshiba$onRender(DrawContext context, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("БоссБар")) {
                ci.cancel();
            }
        }
    }
}
