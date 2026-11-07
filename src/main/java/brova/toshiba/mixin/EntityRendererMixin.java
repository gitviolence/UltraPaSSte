package brova.toshiba.mixin;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.core.OptimizationFlags;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.NameTags;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> implements MinecraftAccess {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void toshiba$renderLabelIfPresent(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            if (ModuleRegistry.wardenEsp != null && ModuleRegistry.wardenEsp.isEnable() && ModuleRegistry.wardenEsp.isHologramLabel(text.getString())) {
                ci.cancel();
            } else if (state instanceof PlayerEntityRenderState && state instanceof OptimizationFlags optimizationState && optimizationState.toshiba$hideVanillaNameTag()) {
                ci.cancel();
            } else {
                NameTags esp = ModuleRegistry.nameTags;
                if (esp != null) {
                    if (esp.shouldHideVanillaTags() && !(state instanceof ArmorStandEntityRenderState)) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
