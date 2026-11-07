package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.BindListWidget.BindEntry;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.ServerHelper;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.utils.input.KeyBindUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class BindListWidget extends HudWidgetBase {

    private float width = 77.5F;

    private float height = 62.5F;

    private float cachedScalarForHudRender = 0.0F;

    private final Map<Object, Float> entryAlpha = new HashMap<>();

    private final Map<Object, Float> entryVerticalPositions = new HashMap<>();

    private final Map<Object, BindEntry> ifAbsentBindListWidgetBindEntryEntries = new HashMap<>();

    private final List<BindEntry> visibleEntries = new ArrayList<>();

    private final List<BindEntry> renderEntries = new ArrayList<>();

    private final Set<Object> activeEntryIds = new HashSet<>();

    private final Set<Object> knownEntryIds = new HashSet<>();

    private final Map<Integer, String> ifAbsentStringEntries = new HashMap<>();

    public BindListWidget(Draggable draggable) {
        super(draggable);
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            this.onHudRender(event);
            super.onRender(event);
        }
    }

    private float calculateFloat(float floatValue, float floatValueValue, float floatValueResult) {
        float floatValueItem = floatValueValue - floatValue;
        return floatValue + floatValueItem * floatValueResult;
    }

    private String createTextFromModuleModuleCategory(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT ->
                "C";
            case MOVEMENT ->
                "S";
            case RENDER ->
                "i";
            case PLAYER ->
                "p";
            case MISC ->
                "k";
            case GHOST, CONFIG ->
                "k";
            case FRIENDS ->
                "k";
        };
    }

    private int getCategoryIconSize(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case MOVEMENT ->
                10;
            case PLAYER ->
                17;
            default ->
                13;
        };
    }

    private float getCategoryIconOffsetX(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT ->
                -0.2F;
            case MOVEMENT ->
                0.0F;
            case RENDER ->
                -0.2F;
            case PLAYER ->
                0.2F;
            case MISC ->
                -0.1F;
            default ->
                0.0F;
        };
    }

    private float getCategoryIconOffsetY(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case MOVEMENT ->
                1.5F;
            default ->
                0.8F;
            case PLAYER ->
                -0.3F;
            case MISC ->
                0.8F;
        };
    }

    private int calculateThemeColor() {
        return ColorMathUtil.getThemeColor();
    }

    private String createTextFromEqualsIgnoreCase(String text) {
        return !text.equalsIgnoreCase("GRAVE ACCENT") && !text.equalsIgnoreCase("GRAVE") ? text : "~";
    }

    private String createTextFromIfAbsentWithBindName(int intValue) {
        return this.ifAbsentStringEntries.computeIfAbsent(intValue, input -> this.createTextFromEqualsIgnoreCase(KeyBindUtil.getBindName(input)));
    }

    private void onHudRender(HudRenderEvent event) {
        MatrixStack matrices = event.getContext().getMatrices();
        this.visibleEntries.clear();
        this.activeEntryIds.clear();
        var iterator = ModuleRegistry.INSTANCE.getObject().iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            if (module.isEnable()) {
                if (module.getKey() != -1) {
                    BindEntry bindEntry = this.ifAbsentBindListWidgetBindEntryEntries.computeIfAbsent(module, input -> new BindEntry(input, module, null));
                    this.visibleEntries.add(bindEntry);
                    this.activeEntryIds.add(module);
                }
                if (!ModuleRegistry.interfaceModule.hideSettingBinds.isState()) {
                    for (SettingBase setting : module.getSettings()) {
                        if (!(module instanceof ServerHelper) && setting instanceof BindSetting bindSetting && bindSetting.getKey() != -1 && setting.visible()) {
                            BindEntry bindEntryValue = this.ifAbsentBindListWidgetBindEntryEntries.computeIfAbsent(bindSetting, inputValue -> new BindEntry(inputValue, module, bindSetting));
                            this.visibleEntries.add(bindEntryValue);
                            this.activeEntryIds.add(bindSetting);
                        }
                    }
                }
            }
        }
        this.knownEntryIds.clear();
        this.knownEntryIds.addAll(this.entryAlpha.keySet());
        this.knownEntryIds.addAll(this.activeEntryIds);
        for (Object value : this.knownEntryIds) {
            boolean flag = this.activeEntryIds.contains(value);
            float orDefault = this.entryAlpha.getOrDefault(value, 0.0F);
            float floatValue = flag ? 1.0F : 0.0F;
            float floatValueValue = this.calculateFloat(orDefault, floatValue, 0.12F);
            if (!(floatValueValue > 0.01F) && !flag) {
                this.entryAlpha.remove(value);
                this.entryVerticalPositions.remove(value);
                this.ifAbsentBindListWidgetBindEntryEntries.remove(value);
            } else {
                this.entryAlpha.put(value, floatValueValue);
            }
        }
        boolean empty = this.visibleEntries.isEmpty();
        boolean flagValue = this.entryAlpha.values().stream().anyMatch(inputResult -> inputResult > 0.01F);
        boolean flagResult = empty && !flagValue && !(mc.currentScreen instanceof ChatScreen);
        float floatValueResult = flagResult ? 0.0F : 1.0F;
        this.cachedScalarForHudRender = this.calculateFloat(this.cachedScalarForHudRender, floatValueResult, 0.16F);
        if (flagResult && this.cachedScalarForHudRender <= 0.01F) {
            this.draggable.setWidth(85.25F);
            this.draggable.setHeight(68.75F);
        } else {
            int themeStaticColor = ColorMathUtil.getThemeStaticColor();
            int intValue = ColorMathUtil.darken(themeStaticColor, 0.55F);
            TextRenderer font = FontManager.getFont("roboto_flex_medium", 15);
            TextRenderer fontValue = FontManager.getFont("roboto_flex_semibold", 15);
            TextRenderer fontResult = FontManager.getFont("roboto_flex_medium", 15);
            float floatValueItem = 7.0F;
            float floatValueEntry = 18.0F;
            float floatValueCopy = 77.5F;
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            float width = this.width;
            float height = this.height;
            float floatValue2 = 1.1F;
            float floatValue3 = Math.max(0.0F, Math.min(1.0F, this.cachedScalarForHudRender));
            if (flagResult && floatValue3 <= 0.03F) {
                this.draggable.setWidth(85.25F);
                this.draggable.setHeight(68.75F);
            } else {
                float floatValue4 = flagResult ? (1.0F - floatValue3) * -6.0F : 0.0F;
                matrices.push();
                matrices.translate(x, y, 0.0F);
                matrices.scale(floatValue2, floatValue2, 1.0F);
                matrices.translate(-x, -y, 0.0F);
                matrices.translate(0.0F, floatValue4, 0.0F);
                this.renderEntries.clear();
                this.renderEntries.addAll(this.visibleEntries);
                for (var entry : this.entryAlpha.entrySet()) {
                    if (!this.activeEntryIds.contains(entry.getKey()) && entry.getValue() > 0.01F) {
                        BindEntry bindEntryResult = this.ifAbsentBindListWidgetBindEntryEntries.get(entry.getKey());
                        if (bindEntryResult != null) {
                            this.renderEntries.add(bindEntryResult);
                        }
                    }
                }
                float floatValue5 = 0.0F;
                for (BindEntry bindEntryItem : this.renderEntries) {
                    float orDefaultValue = this.entryAlpha.getOrDefault(bindEntryItem.id(), 0.0F);
                    if (orDefaultValue > 0.01F) {
                        floatValue5 += 10.5F * orDefaultValue;
                    }
                }
                float floatValue6 = 0.0F;
                float floatValue7 = floatValueEntry + floatValue5 + 6.5F;
                int intValueValue = BackgroundSettings.backgroundColor(18, 18, 18, floatValue3);
                int intValueResult = BackgroundSettings.overlayColor(0, 0, 0, 38, floatValue3);
                if (floatValue3 > 0.03F) {
                    Renderer2D.drawHudShadow(matrices, x, y, width, floatValue7, 7.0F, 2.0F, ColorMathUtil.rgba(0, 0, 0, (int) (90.0F * floatValue3)));
                    Renderer2D.drawHudBlur(matrices, x + 0.5F, y + 0.5F, width - 1.0F, floatValue7 - 1.0F, 6.5F, floatValue3);
                }
                Renderer2D.drawRoundedRect(matrices, x, y, width, floatValue7, 7.0F, intValueValue);
                Renderer2D.drawRoundedRect(matrices, x, y, width, floatValueEntry, 7.0F, 0.0F, 7.0F, 0.0F, intValueResult);
                int intValueItem = ColorMathUtil.applyAlpha(themeStaticColor, floatValue3);
                int intValueEntry = ColorMathUtil.applyAlpha(intValue, floatValue3);
                float floatValue8 = x + width - floatValueItem - 5.0F + this.getCategoryIconOffsetX(ModuleCategory.COMBAT);
                TextRenderer fontItem = FontManager.getFont("hud", 9);
                fontItem.drawGradientStringVertical(matrices, "K", floatValue8 - 2.5F, y + 9.0F + floatValue4, intValueItem, intValueEntry);
                float floatValue9 = x + floatValueItem;
                float floatValue10 = y + 6.2F;
                fontValue.drawString(matrices, "Binds", floatValue9, floatValue10, ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue3)));
                float floatValue11 = 0.0F;
                float floatValue12 = floatValueCopy;
                if (floatValue5 > 0.5F && !this.renderEntries.isEmpty()) {
                    ScissorStack.push();
                    ScissorStack.setFromComponentCoordinates((double) x, (double) (y + (floatValueEntry + floatValue6 + floatValue4) * 1.1F), (double) (width * 1.1F), (double) ((floatValue5 + 3.0F) * 1.1F));
                }
                for (BindEntry bindEntryEntry : this.renderEntries) {
                    float orDefaultResult = this.entryAlpha.getOrDefault(bindEntryEntry.id(), 0.0F);
                    if (!(orDefaultResult <= 0.01F)) {
                        String text = this.createTextFromIfAbsentWithBindName(bindEntryEntry.key());
                        float floatValue13 = y + floatValueEntry + floatValue6 + floatValue11 + 1.0F - (1.0F - orDefaultResult) * 5.0F;
                        float floatValue14 = orDefaultResult * floatValue3;
                        if (floatValue14 <= 0.03F) {
                            floatValue11 += 10.5F * orDefaultResult;
                        } else {
                            String textValue = bindEntryEntry.displayName();
                            int intValueCopy = ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue14));
                            float floatValue15 = floatValue13 + 7.0F;
                            float floatValue16 = x + floatValueItem;
                            matrices.push();
                            matrices.translate(floatValue16, floatValue15, 0.0F);
                            matrices.scale(1.0F, 1.0F, 1.0F);
                            matrices.translate(-floatValue16, -floatValue15, 0.0F);
                            fontResult.drawString(matrices, textValue, floatValue9, floatValue13 + 4.6F, intValueCopy);
                            matrices.pop();
                            ModuleCategory moduleCategory = bindEntryEntry.category();
                            String textResult = this.createTextFromModuleModuleCategory(moduleCategory);
                            TextRenderer fontEntry = FontManager.getFont("hud", this.getCategoryIconSize(moduleCategory));
                            float floatValue17 = 5.0F;
                            float renderWidth = font.getRenderWidth(text);
                            float renderWidthValue = fontEntry.getRenderWidth(textResult);
                            float floatValue18 = Math.max(10.5F, renderWidth + renderWidthValue + 5.4F);
                            float orDefaultItem = this.entryVerticalPositions.getOrDefault(bindEntryEntry.id(), floatValue18);
                            orDefaultItem = this.calculateFloat(orDefaultItem, floatValue18, 0.18F);
                            this.entryVerticalPositions.put(bindEntryEntry.id(), orDefaultItem);
                            float floatValue19 = x + width - floatValueItem - orDefaultItem;
                            float floatValue20 = floatValue19 + orDefaultItem + 1.0F;
                            matrices.push();
                            matrices.translate(floatValue20, floatValue15, 0.0F);
                            matrices.scale(1.0F, 1.0F, 1.0F);
                            matrices.translate(-floatValue20, -floatValue15, 0.0F);
                            int intValue2 = BackgroundSettings.overlayColor(255, 255, 255, 20, floatValue14);
                            if (floatValue14 > 0.08F) {
                                Renderer2D.drawRoundedRect(matrices, floatValue19 - 0.8F, floatValue13 + 2.0F, orDefaultItem + 2.0F, floatValue17 + 5.0F, 2.0F, intValue2);
                            }
                            int intValue3 = ColorMathUtil.applyAlpha(themeStaticColor, floatValue14);
                            int intValue4 = ColorMathUtil.applyAlpha(intValue, floatValue14);
                            float floatValue21 = floatValue19 + 1.5F;
                            font.drawString(matrices, text, floatValue21 + 0.5, (double) (floatValue13 + 5.0F), intValueCopy);
                            fontEntry.drawGradientStringVertical(matrices, textResult, floatValue21 + renderWidth + 2.5F + this.getCategoryIconOffsetX(moduleCategory), floatValue13 + 5.5F + this.getCategoryIconOffsetY(moduleCategory), intValue3, intValue4);
                            matrices.pop();
                            float floatValue22 = floatValueItem + fontResult.getStringWidth(textValue);
                            float floatValue23 = floatValue22 + orDefaultItem + floatValueItem * 2.0F;
                            if (floatValue23 > floatValue12) {
                                floatValue12 = floatValue23;
                            }
                            floatValue11 += 10.5F * orDefaultResult;
                        }
                    }
                }
                if (floatValue5 > 0.5F && !this.renderEntries.isEmpty()) {
                    ScissorStack.unset();
                    ScissorStack.pop();
                }
                matrices.pop();
                float floatValue24 = floatValueEntry + floatValue6 + floatValue11 + 6.5F;
                this.width = this.calculateFloat(this.width, Math.max(77.5F, floatValue12), 0.15F);
                this.height = this.calculateFloat(this.height, floatValue24, 0.15F);
                this.draggable.setWidth(this.width * 1.1F);
                this.draggable.setHeight(this.height * 1.1F);
            }
        }
    }

    public static record BindEntry(Object id, Module module, BindSetting setting) {

        public String displayName() {
            return this.setting != null ? this.setting.displayName() : this.module.getDisplayName();
        }

        public int key() {
            return this.setting != null ? this.setting.getKey() : this.module.getKey();
        }

        public ModuleCategory category() {
            return this.module.getCategory();
        }
    }
}
