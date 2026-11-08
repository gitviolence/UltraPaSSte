package brova.toshiba.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {

    @Invoker("addFeature")
    boolean toshiba$addFeature(FeatureRenderer<?, ?> featureRenderer);

    @Invoker("setupTransforms")
    void toshiba$setupTransforms(LivingEntityRenderState livingEntityRenderState, MatrixStack matrices, float floatValue, float floatValueValue);

    @Invoker("scale")
    void toshiba$scale(LivingEntityRenderState livingEntityRenderState, MatrixStack matrices);
}
