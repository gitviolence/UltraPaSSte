package brova.toshiba.mixin;

import java.util.function.Function;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DrawContext.class)
public interface DrawContextInvoker {

    @Invoker("drawTexturedQuad")
    void toshiba$drawTexturedQuad(Function<Identifier, RenderLayer> function, Identifier texture, int intValue, int intValueValue, int intValueResult, int intValueItem, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValueEntry);
}
