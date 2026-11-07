package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.KeybindsWidget.BindRow;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.ServerHelper;
import brova.toshiba.client.modules.impl.misc.ServerHelper.BoundItem;
import brova.toshiba.client.modules.impl.misc.ServerHelper.ItemAction;
import brova.toshiba.client.utils.input.KeyBindUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class KeybindsWidget extends HudWidgetBase {

    private float width = 45.0F;

    private float height = 20.0F;

    private float cachedScalarForRender;

    private final List<BindRow> keybindsWidgetBindRowEntries = new ArrayList<>();

    private long lastUpdateTimestamp;

    private final Map<BoundItem, BindRow> keybindsWidgetBindRowByServerHelperBoundItem = new IdentityHashMap<>();

    private Object object;

    public KeybindsWidget(Draggable draggable) {
        super(draggable);
    }

    private float lerp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + (floatValueValue - floatValue) * floatValueResult;
    }

    private String createTextFromBindNameWithTrim(int intValue) {
        String bindName = KeyBindUtil.getBindName(intValue);
        if (bindName == null) {
            return "?";
        } else {
            bindName = bindName.toUpperCase().trim();
            if (bindName.startsWith("MOUSE")) {
                return "M" + bindName.substring(5);
            } else {
                return switch(bindName) {
                    case "GRAVE ACCENT", "GRAVE", "`" ->
                        "~";
                    case "ENTER", "NUMENTER" ->
                        "EN";
                    case "CAPS", "CAPS LOCK", "CAPSLOCK" ->
                        "CA";
                    case "SPACE" ->
                        "SP";
                    case "LSHIF;", "RSHIFT", "SHIFT" ->
                        "SH";
                    case "LCTR;", "RCTRL", "CONTROL", "CTRL" ->
                        "CT";
                    case "LAL;", "RALT", "ALT" ->
                        "AL";
                    case "TAB" ->
                        "TB";
                    case "ESC", "ESCAPE" ->
                        "ES";
                    case "BACKSPACE" ->
                        "BS";
                    case "DELETE" ->
                        "DL";
                    case "INSERT" ->
                        "IN";
                    case "HOME" ->
                        "HM";
                    case "END" ->
                        "ED";
                    case "PAGEUP" ->
                        "PU";
                    case "PAGEDOWN" ->
                        "PD";
                    case "LEF;" ->
                        "LT";
                    case "RIGHT" ->
                        "RT";
                    case "UP" ->
                        "UP";
                    case "DOWN" ->
                        "DN";
                    default ->
                        bindName;
                };
            }
        }
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            if (mc.player == null) {
                ModuleRegistry.serverHelper.refreshScreenBindInventory();
                this.keybindsWidgetBindRowEntries.clear();
                this.keybindsWidgetBindRowByServerHelperBoundItem.clear();
                this.object = null;
                this.lastUpdateTimestamp = 0L;
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis >= this.lastUpdateTimestamp || this.object != mc.player) {
                    ServerHelper serverHelper = ModuleRegistry.serverHelper;
                    serverHelper.refreshScreenBindInventory();
                    this.keybindsWidgetBindRowEntries.clear();
                    for (BoundItem boundItem : serverHelper.getKeyBindings()) {
                        if (boundItem.setting.visible() && boundItem.setting.getKey() != -1) {
                            ItemAction screenBindInfo = serverHelper.getScreenBindInfo(boundItem);
                            if (screenBindInfo != null) {
                                int key = boundItem.setting.getKey();
                                BindRow bindRow = this.keybindsWidgetBindRowByServerHelperBoundItem.get(boundItem);
                                if (bindRow == null || bindRow.cachedCount != key) {
                                    bindRow = new BindRow(key, this.createTextFromBindNameWithTrim(key));
                                    this.keybindsWidgetBindRowByServerHelperBoundItem.put(boundItem, bindRow);
                                }
                                bindRow.screenBindStack = serverHelper.getScreenBindStack(screenBindInfo);
                                this.keybindsWidgetBindRowEntries.add(bindRow);
                            }
                        }
                    }
                    this.object = mc.player;
                    this.lastUpdateTimestamp = currentTimeMillis + 250L;
                }
                List<BindRow> keybindsWidgetBindRowEntries = this.keybindsWidgetBindRowEntries;
                this.cachedScalarForRender = this.lerp(this.cachedScalarForRender, keybindsWidgetBindRowEntries.isEmpty() ? 0.0F : 1.0F, 0.16F);
                float floatValue = Math.max(0.0F, Math.min(1.0F, this.cachedScalarForRender));
                if (floatValue <= 0.03F) {
                    this.draggable.setWidth(45.0F);
                    this.draggable.setHeight(20.0F);
                } else {
                    TextRenderer textRenderer = FontManager.getFont("roboto_flex_semibold", 15);
                    TextRenderer font = FontManager.getFont("toshiba", 13);
                    int themeStaticColor = ColorMathUtil.getThemeStaticColor();
                    int intValue = ColorMathUtil.darken(themeStaticColor, 0.55F);
                    int intValueValue = ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue));
                    int intValueResult = ColorMathUtil.applyAlpha(themeStaticColor, floatValue);
                    int intValueItem = ColorMathUtil.applyAlpha(intValue, floatValue);
                    float x = this.draggable.getX();
                    float y = this.draggable.getY();
                    int intValueEntry = Math.max(1, (keybindsWidgetBindRowEntries.size() + 3) / 4);
                    float[] values = new float[intValueEntry];
                    float floatValueValue = 0.0F;
                    for (int index = 0; index < keybindsWidgetBindRowEntries.size(); index++) {
                        BindRow bindRowValue = keybindsWidgetBindRowEntries.get(index);
                        if (bindRowValue.textRenderer != textRenderer) {
                            bindRowValue.textWidth = textRenderer.getRenderWidth(bindRowValue.createTextFromText());
                            bindRowValue.textRenderer = textRenderer;
                        }
                        bindRowValue.entryWidth = 45.0F + Math.max(0.0F, bindRowValue.textWidth - 5.0F);
                        int intValueCopy = index / 4;
                        if (index % 4 != 0) {
                            values[intValueCopy] += 2.0F;
                        }
                        values[intValueCopy] += bindRowValue.entryWidth;
                        floatValueValue = Math.max(floatValueValue, values[intValueCopy]);
                    }
                    MatrixStack matrices = event.getContext().getMatrices();
                    matrices.push();
                    int intValue2 = 0;
                    float floatValueResult = x;
                    for (BindRow bindRowResult : keybindsWidgetBindRowEntries) {
                        int intValue3 = intValue2 % 4;
                        int intValue4 = intValue2 / 4;
                        if (intValue3 == 0) {
                            floatValueResult = x + (floatValueValue - values[intValue4]) * 0.5F;
                        }
                        float floatValueItem = y + intValue4 * 22.0F;
                        this.updateOverlayColorWithRoundedRect(matrices, floatValueResult, floatValueItem, bindRowResult.entryWidth, floatValue);
                        ItemStack itemStack = bindRowResult.getItemStack();
                        matrices.push();
                        matrices.translate(floatValueResult + 5.0F, floatValueItem + 5.5F, 0.0F);
                        matrices.scale(0.6F, 0.6F, 1.0F);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, floatValue);
                        event.getContext().drawItem(itemStack, 0, 0);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        matrices.pop();
                        float floatValueEntry = floatValueResult + 27.0F;
                        float floatValueCopy = floatValueItem + 2.0F;
                        float floatValue2 = bindRowResult.entryWidth - 45.0F;
                        textRenderer.drawString(matrices, bindRowResult.createTextFromText(), floatValueResult + 23.0F, floatValueCopy + 6.0F, intValueValue);
                        font.drawGradientStringVertical(matrices, "k", floatValueEntry + 3.5F + floatValue2, floatValueCopy + 7.5F, intValueResult, intValueItem);
                        floatValueResult += bindRowResult.entryWidth + 2.0F;
                        intValue2++;
                    }
                    matrices.pop();
                    this.width = this.lerp(this.width, floatValueValue, 0.15F);
                    this.height = this.lerp(this.height, intValueEntry * 20.0F + Math.max(0, intValueEntry - 1) * 2.0F, 0.15F);
                    this.draggable.setWidth(this.width);
                    this.draggable.setHeight(this.height);
                }
            }
        }
    }

    private void updateOverlayColorWithRoundedRect(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        int intValue = BackgroundSettings.backgroundColor(18, 18, 18, floatValueItem);
        int intValueValue = ColorMathUtil.rgba(0, 0, 0, (int) (90.0F * floatValueItem));
        int intValueResult = BackgroundSettings.overlayColor(255, 255, 255, 13, floatValueItem);
        int intValueItem = BackgroundSettings.overlayColor(255, 255, 255, 5, floatValueItem);
        Renderer2D.drawHudShadow(matrices, floatValue, floatValueValue, floatValueResult, 20.0F, 7.0F, 2.0F, intValueValue);
        Renderer2D.drawHudBlur(matrices, floatValue + 0.5F, floatValueValue + 0.5F, floatValueResult - 1.0F, 19.0F, 6.5F, floatValueItem);
        Renderer2D.drawRoundedRect(matrices, floatValue, floatValueValue, floatValueResult, 20.0F, 7.0F, intValue);
        Renderer2D.drawPixelRect(matrices, floatValue + 18.0F, floatValueValue + 5.0F, 0.5F, 10.0F, intValueResult);
        Renderer2D.drawRoundedRect(matrices, floatValue + 20.0F, floatValueValue + 3.3F, floatValueResult - 24.0F, 13.0F, 6.0F, intValueItem);
    }

    public final static class BindRow {

        ItemStack screenBindStack;

        private final String cachedText;

        final int cachedCount;

        TextRenderer textRenderer;

        float textWidth;

        float entryWidth;

        BindRow(int cachedCount, String cachedText) {
            this.cachedCount = cachedCount;
            this.cachedText = cachedText;
        }

        String createTextFromText() {
            return this.cachedText;
        }

        ItemStack getItemStack() {
            return this.screenBindStack;
        }
    }
}
