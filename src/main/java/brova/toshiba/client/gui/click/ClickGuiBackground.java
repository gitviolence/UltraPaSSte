package brova.toshiba.client.gui.click;

import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public final class ClickGuiBackground {

    private ClickGuiBackground() {
    }

    public static void render(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        floatValueCopy = MathHelper.clamp(floatValueCopy, 0.0F, 1.0F);
        if (!(floatValueCopy <= 0.001F)) {
            int intValue = Math.round(90.0F * floatValueCopy);
            if (intValue > 0) {
                Renderer2D.drawShadow(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, 3.5F, ColorMathUtil.rgba(0, 0, 0, intValue));
            }
            Renderer2D.drawBlurWithOpacity(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, 5.0F, ColorUtil.rgba(20, 20, 20, Math.round(60.0F * floatValueCopy)), floatValueCopy);
            int intValueValue = Math.round(255.0F * floatValueCopy);
            Renderer2D.drawDiagonalGradientRect(matrices, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, ColorUtil.rgba(25, 25, 25, intValueValue), ColorUtil.rgba(28, 28, 28, intValueValue));
        }
    }
}
