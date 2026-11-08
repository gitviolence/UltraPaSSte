package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.NoRender;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void toshiba$renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Огонь")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void toshiba$renderInWallOverlay(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Фон в блоке")) {
                ci.cancel();
            }
        }
    }
}
