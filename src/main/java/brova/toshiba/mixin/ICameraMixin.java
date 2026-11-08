package brova.toshiba.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface ICameraMixin {

    @Invoker("setRotation")
    void setCustomRotation(float floatValue, float floatValueValue);

    @Invoker("clipToSpace")
    float setClipToSpace(float clipToSpace);

    @Invoker("moveBy")
    void setCustomMoveBy(float floatValue, float floatValueValue, float floatValueResult);

    @Invoker("setPos")
    void setCustomPos(double doubleValue, double doubleValueValue, double doubleValueResult);
}
