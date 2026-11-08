package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.utils.math.MathUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class CoordinatesWidget extends HudWidgetBase {

    private TextRenderer robotoFlexMediumFont14;

    private TextRenderer hudFont12;

    private TextRenderer toshibaFont15;

    public CoordinatesWidget(Draggable draggable) {
        super(draggable);
    }

    private void updateLocalState() {
        this.robotoFlexMediumFont14 = FontManager.getFont("roboto_flex_medium", 14);
        this.hudFont12 = FontManager.getFont("hud", 12);
        this.toshibaFont15 = FontManager.getFont("toshiba", 15);
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            this.DefaultStyle(event);
            super.onRender(event);
        }
    }

    public void DefaultStyle(HudRenderEvent event) {
        if (mc != null && mc.player != null) {
            this.updateLocalState();
            TextRenderer robotoFlexMediumFont14 = this.robotoFlexMediumFont14;
            if (robotoFlexMediumFont14 != null && this.hudFont12 != null && this.toshibaFont15 != null) {
                MatrixStack matrices = event.getContext().getMatrices();
                float x = this.draggable.getX();
                float y = this.draggable.getY();
                int roundedDown = (int) Math.floor(mc.player.getX());
                int roundedDownValue = (int) Math.floor(mc.player.getY());
                int roundedDownResult = (int) Math.floor(mc.player.getZ());
                double bps = MathUtil.getBps(mc.player);
                String text = this.createTextFromDouble(bps);
                String textValue = "bps";
                String textResult = String.valueOf(roundedDown);
                String textItem = String.valueOf(roundedDownValue);
                String textEntry = String.valueOf(roundedDownResult);
                float floatValue = 1.1F;
                float floatValueValue = 18.0F;
                float floatValueResult = 7.0F;
                float floatValueItem = x + 5.5F;
                float floatValueEntry = x + 15.5F;
                float floatValueCopy = floatValueEntry + robotoFlexMediumFont14.getStringWidth(text + textValue);
                float floatValue2 = floatValueCopy + 4.5F;
                float floatValue3 = floatValue2 + 7.0F;
                float floatValue4 = floatValue3 + 9.5F;
                float floatValue5 = floatValue4 + robotoFlexMediumFont14.getStringWidth(textResult) + robotoFlexMediumFont14.getStringWidth("x ") + robotoFlexMediumFont14.getStringWidth(textItem) + robotoFlexMediumFont14.getStringWidth("y ") + robotoFlexMediumFont14.getStringWidth(textEntry) + robotoFlexMediumFont14.getStringWidth("z");
                float floatValue6 = floatValue5 - x + 5.0F;
                int intValue = BackgroundSettings.backgroundColor(18, 18, 18, 1.0F);
                int intValueValue = ColorMathUtil.rgba(0, 0, 0, 80);
                int intValueResult = ColorUtil.rgba(255, 255, 255, 255);
                int intValueItem = ColorUtil.rgba(255, 255, 255, 102);
                int intValueEntry = ColorUtil.rgba(255, 255, 255, 38);
                int themeStaticColor = ColorMathUtil.getThemeStaticColor();
                int intValueCopy = ColorMathUtil.darken(themeStaticColor, 0.55F);
                matrices.push();
                matrices.translate(x, y, 0.0F);
                matrices.scale(floatValue, floatValue, 1.0F);
                matrices.translate(-x, -y, 0.0F);
                Renderer2D.drawHudShadow(matrices, x, y, floatValue6, floatValueValue, floatValueResult, 2.5F, intValueValue);
                Renderer2D.drawHudBlur(matrices, x + 0.5F, y + 0.5F, floatValue6 - 1.0F, floatValueValue - 1.0F, floatValueResult - 0.5F, 1.0F);
                Renderer2D.drawRoundedRect(matrices, x, y, floatValue6, floatValueValue, floatValueResult, intValue);
                this.hudFont12.drawGradientStringVertical(matrices, "S", floatValueItem, y + 8.6F, themeStaticColor, intValueCopy);
                float floatValue7 = y + 7.5F;
                robotoFlexMediumFont14.drawString(matrices, text, floatValueEntry, floatValue7, intValueResult);
                robotoFlexMediumFont14.drawString(matrices, textValue, floatValueEntry + robotoFlexMediumFont14.getStringWidth(text), floatValue7, intValueItem);
                Renderer2D.drawPixelRect(matrices, floatValue2, y + 5.0F, 0.5F, 8.0F, intValueEntry);
                this.toshibaFont15.drawGradientStringVertical(matrices, "E", floatValue3, y + 8.0F, themeStaticColor, intValueCopy);
                robotoFlexMediumFont14.drawString(matrices, textResult, floatValue4, floatValue7, intValueResult);
                float floatValue8 = floatValue4 + robotoFlexMediumFont14.getStringWidth(textResult);
                robotoFlexMediumFont14.drawString(matrices, "x", floatValue8, floatValue7, intValueItem);
                floatValue8 += robotoFlexMediumFont14.getStringWidth("x ");
                robotoFlexMediumFont14.drawString(matrices, textItem, floatValue8, floatValue7, intValueResult);
                floatValue8 += robotoFlexMediumFont14.getStringWidth(textItem);
                robotoFlexMediumFont14.drawString(matrices, "y", floatValue8, floatValue7, intValueItem);
                floatValue8 += robotoFlexMediumFont14.getStringWidth("y ");
                robotoFlexMediumFont14.drawString(matrices, textEntry, floatValue8, floatValue7, intValueResult);
                floatValue8 += robotoFlexMediumFont14.getStringWidth(textEntry);
                robotoFlexMediumFont14.drawString(matrices, "z", floatValue8, floatValue7, intValueItem);
                matrices.pop();
                this.draggable.setWidth(floatValue6 * floatValue);
                this.draggable.setHeight(floatValueValue * floatValue);
            }
        }
    }

    private String createTextFromDouble(double doubleValue) {
        int intValue = (int) Math.round(doubleValue * 100.0);
        int magnitude = Math.abs(intValue % 100);
        return intValue / 100 + "." + (magnitude < 10 ? "0" : "") + magnitude;
    }
}
