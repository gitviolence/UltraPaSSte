package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.CustomSize;
import brova.toshiba.client.modules.impl.render.SpinBot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    private void toshiba$updateSpinBotState(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            SpinBot spinBot = ModuleRegistry.spinBot;
            if (spinBot != null) {
                spinBot.applyRotation(player, state, tickDelta);
            }
        }
    }

    @Inject(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At("HEAD"))
    private void toshiba$setupTransforms(PlayerEntityRenderState state, MatrixStack matrices, float animationProgress, float bodyYaw, CallbackInfo ci) {
        CustomSize m = CustomSize.INSTANCE;
        if (m != null && m.isEnable()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.world != null) {
                if (mc.world.getEntityById(state.id) instanceof PlayerEntity p) {
                    if (m.shouldScale(p.getName().getString())) {
                        float s = m.getScale();
                        matrices.scale(s, s, s);
                    }
                }
            }
        }
    }
}
