package brova.toshiba.mixin;

import brova.toshiba.client.gui.click.ClickGuiScreen;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.SystemFontManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class FontInitMixin {

    @Inject(method = "onFinishedLoading", at = @At("TAIL"))
    private void onFinishedLoading(CallbackInfo ci) {
        SystemFontManager.init();
        FontManager.init();
        ClickGuiScreen.preloadResources();
    }
}
