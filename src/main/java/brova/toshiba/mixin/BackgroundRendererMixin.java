package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.CustomWorld;
import brova.toshiba.client.modules.impl.render.NoRender;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(method = "getFogModifier", at = @At("HEAD"), cancellable = true)
    private static void toshiba$getFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> cir) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Плохие эффекты")) {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void toshiba$applyFog(Camera camera, FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (ModuleRegistry.INSTANCE != null) {
            CustomWorld tweaks = ModuleRegistry.worldTweaks;
            if (tweaks != null && tweaks.isFogEnabled()) {
                cir.setReturnValue(new Fog(2.0F, tweaks.getFogDistance(), FogShape.CYLINDER, color.x, color.y, color.z, color.w));
            }
        }
    }
}
