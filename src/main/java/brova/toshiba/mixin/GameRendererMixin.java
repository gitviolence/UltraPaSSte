package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.AspectRatio;
import brova.toshiba.client.modules.impl.render.NoRender;
import brova.toshiba.client.utils.render.WorldFramebufferRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V", ordinal = 0))
    private void toshiba$renderColorSaturation(RenderTickCounter tickCounter, boolean renderWorld, CallbackInfo ci) {
        if (renderWorld && MinecraftClient.getInstance().isFinishedLoading()) {
            WorldFramebufferRenderer.getInstance().renderWorld();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void toshiba$releaseColorSaturation(CallbackInfo ci) {
        WorldFramebufferRenderer.getInstance().release();
    }

    @ModifyArg(method = "getBasicProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"), index = 1)
    private float toshiba$projectionAspect(float original) {
        AspectRatio aspectRatio = ModuleRegistry.aspectRatio;
        return aspectRatio != null && aspectRatio.isEnable() ? aspectRatio.getProjectionAspect(original) : original;
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void toshiba$hideTotemAnimation(ItemStack stack, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null && stack != null && stack.isOf(Items.TOTEM_OF_UNDYING)) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isTotemAnimationDisabled()) {
                ci.cancel();
            }
        }
    }
}
