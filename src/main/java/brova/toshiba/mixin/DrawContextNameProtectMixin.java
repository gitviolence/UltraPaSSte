package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DrawContext.class)
public class DrawContextNameProtectMixin {

    @ModifyVariable(method = "drawTextWithShadow", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private String toshiba$patchStringShadow(String text) {
        return this.patch(text);
    }

    @ModifyVariable(method = "drawText", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private String toshiba$patchString(String text) {
        return this.patch(text);
    }

    private String patch(String text) {
        if (ModuleRegistry.INSTANCE == null) {
            return text;
        } else {
            NameProtect nameProtect = ModuleRegistry.nameProtect;
            return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
        }
    }
}
