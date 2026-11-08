package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Unique
    private ButtonWidget toshiba$confirmationButton;

    @Unique
    private TextWidget toshiba$confirmationText;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void toshiba$init(CallbackInfo ci) {
        this.children().stream().filter(ButtonWidget.class::isInstance).map(ButtonWidget.class::cast).filter(button -> button.getMessage().getString().equals(Text.translatable("menu.disconnect").getString())).findFirst().ifPresent(this::toshiba$protectDisconnect);
    }

    @Unique
    private void toshiba$protectDisconnect(ButtonWidget button) {
        ButtonWidgetAccessor accessor = (ButtonWidgetAccessor) button;
        PressAction original = accessor.getOnPress();
        accessor.setOnPress(widget -> {
            if (ModuleRegistry.pvpSave.shouldConfirmDisconnect()) {
                this.toshiba$showConfirmation(button, original);
            } else {
                original.onPress(widget);
            }
        });
    }

    @Unique
    private void toshiba$showConfirmation(ButtonWidget disconnectButton, PressAction original) {
        if (this.toshiba$confirmationButton == null) {
            int x = disconnectButton.getX();
            int textY = disconnectButton.getY() + disconnectButton.getHeight() + 2;
            this.toshiba$confirmationText = this.addDrawableChild(new TextWidget(x, textY, disconnectButton.getWidth(), 9, Text.literal("Режим PvP активен"), this.textRenderer).alignCenter());
            this.toshiba$confirmationButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Отключиться").withColor(16742263), widget -> original.onPress(disconnectButton)).dimensions(x, textY + 12, disconnectButton.getWidth(), disconnectButton.getHeight()).build());
        }
    }
}
