package brova.toshiba.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ButtonWidget.class)
public interface ButtonWidgetAccessor {

    @Accessor("onPress")
    PressAction getOnPress();

    @Accessor("onPress")
    @Mutable
    void setOnPress(PressAction onPress);
}
