package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TextVisitFactory.class)
public class TextVisitFactoryMixin {

    @ModifyArg(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), index = 0)
    private static String toshiba$patchVisitedText(String text) {
        if (ModuleRegistry.INSTANCE == null) {
            return text;
        } else {
            NameProtect nameProtect = ModuleRegistry.nameProtect;
            return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
        }
    }
}
