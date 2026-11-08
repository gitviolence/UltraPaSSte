package brova.toshiba.mixin;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.data.SeeInvisiblesTarget;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.SeeInvisibles;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> implements MinecraftAccess {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void toshiba$updateSeeInvisiblesState(T entity, S state, float tickDelta, CallbackInfo ci) {
        boolean shouldRenderInvisible = this.toshiba$shouldRenderInvisible(entity);
        ((SeeInvisiblesTarget) state).toshiba$setSeeInvisiblesTarget(shouldRenderInvisible);
        if (shouldRenderInvisible) {
            state.invisible = true;
            state.invisibleToPlayer = false;
        }
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 654311423))
    private int toshiba$changeInvisibleAlpha(int original, S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (((SeeInvisiblesTarget) state).toshiba$isSeeInvisiblesTarget()) {
            SeeInvisibles seeInvisibles = ModuleRegistry.seeInvisibles;
            return seeInvisibles != null ? seeInvisibles.getInvisibleColor() : SeeInvisibles.INVISIBLE_COLOR;
        } else {
            return original;
        }
    }

    @Unique
    private boolean toshiba$shouldRenderInvisible(T entity) {
        if (entity instanceof PlayerEntity player && ModuleRegistry.INSTANCE != null) {
            SeeInvisibles seeInvisibles = ModuleRegistry.seeInvisibles;
            return seeInvisibles != null && seeInvisibles.shouldRenderInvisible(player);
        } else {
            return false;
        }
    }
}
