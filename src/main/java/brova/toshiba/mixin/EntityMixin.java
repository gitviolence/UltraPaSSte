package brova.toshiba.mixin;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.MovementSpeedEvent;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.player.NoPush;
import brova.toshiba.client.modules.impl.render.SeeInvisibles;
import brova.toshiba.client.modules.impl.render.ShaderESP;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements MinecraftAccess {

    @Shadow
    private Box boundingBox;

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void toshiba$expandHitBox(CallbackInfoReturnable<Box> cir) {
        if (ModuleRegistry.hitBox != null) {
            cir.setReturnValue(ModuleRegistry.hitBox.expand((Entity) (Object) this, this.boundingBox));
        }
    }

    @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z"))
    private boolean fixFallDistanceCalculation(boolean original) {
        return (Object) this == mc.player ? false : original;
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void pushAwayFrom(CallbackInfo ci) {
        if ((Object) this == mc.player && ModuleRegistry.INSTANCE != null) {
            NoPush noPush = ModuleRegistry.noPush;
            if (noPush != null && noPush.isEnable() && noPush.getCollisionList().is("Игроки")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "isPushedByFluids", at = @At("RETURN"), cancellable = true)
    public void isPushedByFluids(CallbackInfoReturnable<Boolean> ci) {
        if ((Object) this == mc.player && ModuleRegistry.INSTANCE != null) {
            NoPush noPush = ModuleRegistry.noPush;
            if (noPush != null && noPush.isEnable() && noPush.getCollisionList().is("Вода")) {
                ci.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void toshiba$getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        if (ModuleRegistry.INSTANCE != null) {
            ShaderESP shaderEsp = ModuleRegistry.shaderEsp;
            if (shaderEsp != null && shaderEsp.shouldOutline((Entity) (Object) this)) {
                cir.setReturnValue(shaderEsp.getOutlineColor());
            }
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void toshiba$allowSeeInvisibles(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity target && ModuleRegistry.INSTANCE != null) {
            SeeInvisibles seeInvisibles = ModuleRegistry.seeInvisibles;
            if (seeInvisibles != null && seeInvisibles.shouldRenderInvisible(target)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "updateVelocity", at = @At("TAIL"))
    private void inject2(float speed, Vec3d movementInput, CallbackInfo ci) {
        if (mc.player != null && (Entity) (Object) this == mc.player && EventBus.hasListeners(MovementSpeedEvent.class)) {
            new MovementSpeedEvent(speed, movementInput).call();
        }
    }
}
