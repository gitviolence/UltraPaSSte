package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class SaturationEatHudMixin {

    @Inject(method = "renderFood", at = @At("HEAD"))
    private void renderFoodUnderlay(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        ModuleRegistry.saturationEat.renderFoodUnderlay(context, player, top, right);
    }

    @Inject(method = "renderFood", at = @At("TAIL"))
    private void renderFoodOverlays(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        ModuleRegistry.saturationEat.renderFoodOverlays(context, player, top, right, player.age);
    }
}
