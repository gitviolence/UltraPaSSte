package brova.toshiba.client.gui.click;

import brova.toshiba.Toshiba;
import brova.toshiba.client.configs.CloudConfigSync;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.input.KeyBindUtil;
import brova.toshiba.client.utils.math.HoverUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.StyledFontRenderer;
import brova.toshiba.client.utils.render.font.SystemFontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class ClickGuiRenderer {

    private final ClickGuiController clickGuiController;

    private final SettingRenderer settingRenderer;

    private final ThemeSelector themeSelector;

    private final ProfilePanel profilePanel;

    private final Animation updateAnimationForUpdateRgbaWithWidth = new Animation(1.0F, 14.0F, PowerEasing.CUBIC_OUT);

    private final Animation primaryButtonHover = new Animation(0.0F, 12.0F, PowerEasing.CUBIC_OUT);

    private final Animation secondaryButtonHover = new Animation(0.0F, 12.0F, PowerEasing.CUBIC_OUT);

    public ClickGuiRenderer(ClickGuiController clickGuiController, SettingRenderer settingRenderer, ThemeSelector themeSelector, ProfilePanel profilePanel) {
        this.clickGuiController = clickGuiController;
        this.settingRenderer = settingRenderer;
        this.themeSelector = themeSelector;
        this.profilePanel = profilePanel;
    }

    public void render(DrawContext context, int intValue, int intValueValue, Window window, float floatValue) {
        if (window != null) {
            float clampedValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
            if (!(clampedValue <= 0.001F)) {
                int intValueResult = this.calculateThemeWithThemes();
                Object value = null;
                this.profilePanel.renderPanel(context, this.clickGuiController.getX(), this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY(), clampedValue, intValueResult, intValue, intValueValue);
                float floatValueValue = this.clickGuiController.updateContentTransition();
                value = this.createModuleFromRgbaWithDraw(context, intValue, intValueValue, this.clickGuiController.getX(), this.clickGuiController.getContentCategory(), intValueResult, clampedValue, floatValueValue);
                this.updateRgbaWithWidth(context, intValueResult, clampedValue);
                this.settingRenderer.renderColorPickerOverlay(context, intValue, intValueValue, this.clickGuiController, 255.0F * clampedValue);
                this.profilePanel.renderAvatarPreview(context, this.clickGuiController.getX(), this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY(), clampedValue, window);
            }
        }
    }

    private Module createModuleFromRgbaWithDraw(DrawContext context, int intValue, int intValueValue, float floatValue, ModuleCategory moduleCategory, int intValueResult, float floatValueValue, float floatValueResult) {
        float floatValueItem = this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY();
        ClickGuiBackground.render(context.getMatrices(), floatValue, floatValueItem, 500.0F, 350.0F, 10.0F, floatValueValue);
        int intValueItem = ColorUtil.rgba(28, 28, 28, this.calculateInteger(255, floatValueValue));
        int intValueEntry = ColorUtil.rgba(30, 30, 30, this.calculateInteger(255, floatValueValue));
        Renderer2D.drawDiagonalGradientRect(context.getMatrices(), floatValue + 3.0F, floatValueItem + 3.0F, 134.0F, 344.0F, 9.0F, intValueItem, intValueEntry);
        int intValueCopy = ColorUtil.rgba(255, 255, 255, this.calculateInteger(5, floatValueValue));
        Renderer2D.drawGradientRect(context.getMatrices(), floatValue + 140.0F, floatValueItem + 3.0F, 357.0F, 44.0F, 9.0F, intValueCopy, intValueCopy, intValueCopy, intValueCopy);
        float categoryAnimation = this.clickGuiController.getCategoryAnimation(ModuleCategory.COMBAT);
        float categoryAnimationValue = this.clickGuiController.getCategoryAnimation(ModuleCategory.MOVEMENT);
        if (categoryAnimation > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 63.5F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimation * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Combat", floatValue + 19.0F, floatValueItem + 72.5F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimation) * floatValueValue)));
        FontManager.getFont("hud", 15).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.COMBAT), floatValue + 116.0F, floatValueItem + 74.0F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimation), floatValueValue));
        if (categoryAnimationValue > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 90.5F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimationValue * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Movement", floatValue + 19.0F, floatValueItem + 99.5F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimationValue) * floatValueValue)));
        FontManager.getFont("hud", 12).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.MOVEMENT), floatValue + 116.0F, floatValueItem + 102.0F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimationValue), floatValueValue));
        float categoryAnimationResult = this.clickGuiController.getCategoryAnimation(ModuleCategory.PLAYER);
        if (categoryAnimationResult > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 117.5F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimationResult * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Player", floatValue + 19.0F, floatValueItem + 126.5F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimationResult) * floatValueValue)));
        FontManager.getFont("hud", 20).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.PLAYER), floatValue + 115.0F, floatValueItem + 126.5F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimationResult), floatValueValue));
        float categoryAnimationItem = this.clickGuiController.getCategoryAnimation(ModuleCategory.MISC);
        if (categoryAnimationItem > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 222.5F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimationItem * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Misc", floatValue + 19.0F, floatValueItem + 231.5F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimationItem) * floatValueValue)));
        FontManager.getFont("hud", 15).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.MISC), floatValue + 116.0F, floatValueItem + 233.0F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimationItem), floatValueValue));
        float categoryAnimationEntry = this.clickGuiController.getCategoryAnimation(ModuleCategory.GHOST);
        if (categoryAnimationEntry > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 249.5F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimationEntry * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Ghost", floatValue + 19.0F, floatValueItem + 258.5F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimationEntry) * floatValueValue)));
        FontManager.getFont("hud", 15).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.GHOST), floatValue + 116.0F, floatValueItem + 260.0F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimationEntry), floatValueValue));
        float categoryAnimationCopy = this.clickGuiController.getCategoryAnimation(ModuleCategory.CONFIG);
        if (categoryAnimationCopy > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 276.5F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimationCopy * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Configs", floatValue + 19.0F, floatValueItem + 285.5F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimationCopy) * floatValueValue)));
        FontManager.getFont("hud", 15).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.CONFIG), floatValue + 116.0F, floatValueItem + 287.0F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimationCopy), floatValueValue));
        float categoryAnimation2 = this.clickGuiController.getCategoryAnimation(ModuleCategory.RENDER);
        if (categoryAnimation2 > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 170.0F, 120.0F, 23.0F, 6.0F, ColorMathUtil.rgba(188, 207, 255, (int) (13.0F * categoryAnimation2 * floatValueValue)));
        }
        FontManager.getFont("suisse", 15).draw(context.getMatrices(), "Render", floatValue + 19.0F, floatValueItem + 179.0F, ColorUtil.rgba(255, 255, 255, (int) ((38.0F + 217.0F * categoryAnimation2) * floatValueValue)));
        FontManager.getFont("hud", 15).draw(context.getMatrices(), this.createTextFromModuleModuleCategory(ModuleCategory.RENDER), floatValue + 116.5F, floatValueItem + 180.0F, this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, 38), intValueResult, categoryAnimation2), floatValueValue));
        this.getLogoFont(40).drawCenteredString(context.getMatrices(), "A", floatValue + 73.0F, floatValueItem + 20.0F, this.calculateApplyAlpha(intValueResult, floatValueValue));
        int intValue2 = ColorUtil.rgba(255, 255, 255, (int) (13.0F * floatValueValue));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 47.875F, 5.0F, 1.75F, 0.0F, intValue2);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 36.0F, floatValueItem + 47.875F, 86.0F, 1.75F, 0.0F, intValue2);
        FontManager.getFont("suisse", 12).draw(context.getMatrices(), "MAIN", floatValue + 19.0F, floatValueItem + 47.9F, ColorUtil.rgba(255, 255, 255, (int) (51.0F * floatValueValue)));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 154.375F, 5.0F, 1.75F, 0.0F, intValue2);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 43.5F, floatValueItem + 154.375F, 78.5F, 1.75F, 0.0F, intValue2);
        FontManager.getFont("suisse", 12).draw(context.getMatrices(), "RENDER", floatValue + 19.0F, floatValueItem + 153.9F, ColorUtil.rgba(255, 255, 255, (int) (51.0F * floatValueValue)));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 10.0F, floatValueItem + 206.875F, 5.0F, 1.75F, 0.0F, intValue2);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 40.0F, floatValueItem + 206.875F, 82.0F, 1.75F, 0.0F, intValue2);
        FontManager.getFont("suisse", 12).draw(context.getMatrices(), "OTHER", floatValue + 19.0F, floatValueItem + 206.4F, ColorUtil.rgba(255, 255, 255, (int) (51.0F * floatValueValue)));
        this.profilePanel.render(context, floatValue, floatValueItem, floatValueValue);
        FontManager.getFont("toshiba", 25).draw(context.getMatrices(), "c", floatValue + 154.0F, floatValueItem + 21.0F, ColorUtil.rgba(255, 255, 255, (int) (64.0F * floatValueValue)));
        float contentY = ClickGuiLayout.getContentY(floatValueItem);
        float contentHeight = ClickGuiLayout.getContentHeight();
        float floatValueEntry = floatValueValue * floatValueResult;
        this.clickGuiController.clampScroll(moduleCategory, contentHeight);
        float scroll = this.clickGuiController.getScroll(moduleCategory);
        float[] values = new float[] { contentY + scroll, contentY + scroll };
        Module module = null;
        ScissorStack.push();
        ScissorStack.setFromComponentCoordinates((double) (floatValue + 140.0F), (double) contentY, 347.0, (double) contentHeight);
        if (moduleCategory == ModuleCategory.FRIENDS && !this.clickGuiController.hasSearchQuery()) {
            String text = "Coming soon...";
            TextRenderer font = FontManager.getFont("manrope", 24);
            float width = font.getWidth(text);
            float height = font.getHeight();
            float floatValueCopy = floatValue + (500.0F - width) / 2.0F;
            float floatValue2 = floatValueItem + (350.0F - height) / 2.0F + 8.0F;
            font.draw(context.getMatrices(), text, floatValueCopy, floatValue2, this.calculateApplyAlpha(-1, floatValueEntry));
        }
        if (moduleCategory == ModuleCategory.CONFIG && !this.clickGuiController.hasSearchQuery()) {
            this.updateRgbaWithDraw(context, intValue, intValueValue, floatValue, contentY, intValueResult, floatValueEntry);
        }
        for (Module moduleValue : this.clickGuiController.getModules(moduleCategory)) {
            float moduleOpenAnimation = this.clickGuiController.getModuleOpenAnimation(moduleValue);
            float moduleHeight = ClickGuiLayout.getModuleHeight(moduleValue, moduleOpenAnimation, this.clickGuiController);
            int moduleColumn = this.clickGuiController.getModuleColumn(moduleValue);
            float floatValue3 = floatValue + 140.0F + moduleColumn * 175.0F;
            float floatValue4 = values[moduleColumn];
            if (floatValue4 + moduleHeight + 6.0F >= contentY && floatValue4 <= contentY + contentHeight) {
                Module moduleResult = this.createModuleFromRgbaWithKey(context, intValue, intValueValue, floatValue3, floatValue4, moduleValue, moduleOpenAnimation, moduleHeight, intValueResult, floatValueEntry);
                if (moduleResult != null && HoverUtil.isHovered(intValue, intValueValue, floatValue + 140.0F, contentY, 347.0, contentHeight)) {
                    module = moduleResult;
                }
            }
            values[moduleColumn] += 6.0F + moduleHeight;
        }
        ScissorStack.pop();
        String textValue = module == null ? "Наведите на модуль, чтобы увидеть описание" : module.getDisplayDescription();
        if (textValue != null && !textValue.isBlank() && !"NULLABLE".equalsIgnoreCase(textValue) && !"desc".equalsIgnoreCase(textValue)) {
            TextRenderer fontValue = FontManager.getFont("suisse", 15);
            float floatValue5 = 222.0F;
            float floatValue6 = 9.0F;
            var valuesValue = this.createListFromAppendWithToString(fontValue, textValue, floatValue5);
            float floatValue7 = floatValueItem + 22.5F - (valuesValue.size() - 1) * floatValue6 * 0.5F;
            int intValue3 = ColorUtil.rgba(255, 255, 255, (int) (64.0F * floatValueEntry));
            for (int index = 0; index < valuesValue.size(); index++) {
                fontValue.drawStringWithFade(context.getMatrices(), valuesValue.get(index), floatValue + 174.0F, floatValue7 + index * floatValue6, floatValue5, intValue3);
            }
        }
        return module;
    }

    private void updateRgbaWithDraw(DrawContext context, int intValue, int intValueValue, float floatValue, float floatValueValue, int intValueResult, float floatValueResult) {
        float floatValueItem = floatValue + 140.0F + 94.5F;
        float floatValueEntry = 158.0F;
        TextRenderer font = FontManager.getFont("manrope", 20);
        TextRenderer fontValue = FontManager.getFont("suisse", 14);
        String text = "Облако конфигов";
        float floatValueCopy = floatValue + 140.0F + 173.5F;
        font.draw(context.getMatrices(), text, floatValueCopy - font.getWidth(text) / 2.0F, floatValueValue + 100.0F, this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 255), floatValueResult));
        String lastAppliedName = CloudConfigSync.getLastAppliedName();
        String textValue = lastAppliedName.isBlank() ? "Конфиг не применён" : "Применён: " + (lastAppliedName.length() > 27 ? lastAppliedName.substring(0, 27) + "…" : lastAppliedName);
        fontValue.draw(context.getMatrices(), textValue, floatValueCopy - fontValue.getWidth(textValue) / 2.0F, floatValueValue + 112.0F, ColorUtil.rgba(255, 255, 255, this.calculateInteger(125, floatValueResult)));
        fontValue.draw(context.getMatrices(), "Название конфига", floatValueItem + 3.0F, floatValueValue + 125.0F, this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 255), floatValueResult));
        float floatValue2 = floatValueValue + 134.0F;
        float floatValue3 = floatValueValue + 153.0F;
        this.primaryButtonHover.update(HoverUtil.isHovered(intValue, intValueValue, floatValueItem, floatValue2, floatValueEntry, 14.5) ? 1.0F : 0.0F);
        this.secondaryButtonHover.update(HoverUtil.isHovered(intValue, intValueValue, floatValueItem, floatValue3, floatValueEntry, 14.5) ? 1.0F : 0.0F);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueItem, floatValue2, floatValueEntry, 14.5F, 6.0F, ColorUtil.rgba(255, 255, 255, this.calculateInteger((int) (8.0F + 10.0F * Math.max(this.primaryButtonHover.getValue(), this.clickGuiController.isConfigNameActive() ? 1.0F : 0.0F)), floatValueResult)));
        String configName = this.clickGuiController.getConfigName();
        String textResult = configName;
        while (!textResult.isEmpty() && fontValue.getWidth(textResult) > floatValueEntry - 16.0F) {
            textResult = textResult.substring(1);
        }
        fontValue.draw(context.getMatrices(), textResult.isEmpty() ? "Type something..." : textResult, floatValueItem + 5.5F, floatValue2 + 5.0F, ColorUtil.rgba(255, 255, 255, this.calculateInteger(configName.isEmpty() ? 38 : 255, floatValueResult)));
        if (this.clickGuiController.isConfigNameActive() && System.currentTimeMillis() / 500L % 2L == 0L) {
            float floatValue4 = floatValueItem + 6.5F + fontValue.getWidth(textResult);
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue4, floatValue2 + 3.0F, 0.8F, 9.0F, 0.0F, this.calculateApplyAlpha(intValueResult, floatValueResult));
        }
        CloudConfigSync cloudConfigSync = Toshiba.INSTANCE.getCloudConfigSync();
        int intValueItem = cloudConfigSync != null && cloudConfigSync.isSaving() ? ColorUtil.rgba(115, 115, 125, this.calculateInteger(155, floatValueResult)) : this.calculateApplyAlpha(intValueResult, floatValueResult);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueItem, floatValue3, floatValueEntry, 14.5F, 6.0F, intValueItem);
        if (this.secondaryButtonHover.getValue() > 0.01F) {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValueItem, floatValue3, floatValueEntry, 14.5F, 6.0F, ColorUtil.rgba(255, 255, 255, this.calculateInteger((int) (30.0F * this.secondaryButtonHover.getValue()), floatValueResult)));
        }
        String textItem = cloudConfigSync != null && cloudConfigSync.isSaving() ? "Сохранение..." : "Сохранить конфиг";
        fontValue.draw(context.getMatrices(), textItem, floatValueItem + (floatValueEntry - fontValue.getWidth(textItem)) / 2.0F, floatValue3 + 5.0F, this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 255), floatValueResult));
        if (cloudConfigSync != null && !cloudConfigSync.getSaveStatus().isBlank()) {
            String saveStatus = cloudConfigSync.getSaveStatus();
            if (saveStatus.length() > 43) {
                saveStatus = saveStatus.substring(0, 43) + "…";
            }
            fontValue.draw(context.getMatrices(), saveStatus, floatValueCopy - fontValue.getWidth(saveStatus) / 2.0F, floatValueValue + 175.0F, ColorUtil.rgba(255, 255, 255, this.calculateInteger(155, floatValueResult)));
        }
    }

    private int calculateFloat(float[] values) {
        int intValue = 0;
        for (int index = 1; index < values.length; index++) {
            if (values[index] < values[intValue]) {
                intValue = index;
            }
        }
        return intValue;
    }

    private void updateRgbaWithStringWidth(DrawContext context, int intValue, float floatValue) {
        float floatValueValue = this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY() + 350.0F + 6.0F;
        float floatValueResult = floatValueValue + 4.0F - 2.0F;
        float floatValueItem = this.clickGuiController.getX() + 8.0F;
        Renderer2D.drawBlur(context.getMatrices(), floatValueItem - 8.0F, floatValueResult - 2.0F, 35.0F, 34.0F, 5.0F, 5.0F, ColorUtil.rgba(37, 37, 37, (int) (60.0F * floatValue)));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueItem - 4.0F, floatValueResult + 1.0F, 27.0F, 28.0F, 5.0F, ColorUtil.rgba(0, 0, 0, 127));
        this.getLogoFont(36).drawCenteredString(context.getMatrices(), "A", floatValueItem + 11.0F - 2.0F, floatValueResult + 10.0F, this.calculateApplyAlpha(intValue, floatValue));
        ModuleCategory[] moduleCategories = ModuleCategory.values();
        float floatValueEntry = this.clickGuiController.getX() + 8.0F + 22.0F + 15.0F;
        float floatValueCopy = this.calculateStringWidthWithCategoryAnimation(moduleCategories);
        Renderer2D.drawBlur(context.getMatrices(), floatValueEntry, floatValueResult - 2.0F, floatValueCopy + 12.0F, 34.0F, 5.0F, 5.0F, ColorUtil.rgba(37, 37, 37, (int) (60.0F * floatValue)));
        float floatValue2 = floatValueEntry;
        for (ModuleCategory moduleCategory : moduleCategories) {
            float categoryAnimation = this.clickGuiController.getCategoryAnimation(moduleCategory);
            String name = moduleCategory.getName();
            String icons = moduleCategory.getIcons();
            StyledFontRenderer styledFontRenderer = this.createStyledFontRendererFromModuleModuleCategory(moduleCategory, 14);
            float stringWidth = styledFontRenderer.getStringWidth(icons);
            StyledFontRenderer font = SystemFontManager.getFont("Manrope-Bold.ttf", 14.0F);
            float stringWidthValue = font.getStringWidth(name);
            boolean flag = this.clickGuiController.getSelectedCategory() == moduleCategory;
            float floatValue3 = 16.0F + stringWidth + 6.0F + stringWidthValue - 2.0F;
            float interpolatedValue = MathHelper.lerp(categoryAnimation, 22.0F, floatValue3);
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue2 + 5.0F, floatValueResult + 3.0F, interpolatedValue + 3.0F, 25.0F, 4.0F, ColorUtil.rgba(0, 0, 0, 127));
            int intValueValue = this.calculateApplyAlpha(ColorMathUtil.interpolateColor(ColorUtil.rgba(191, 191, 191, 255), intValue, categoryAnimation), floatValue);
            int intValueResult = this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, (int) (120.0F + 135.0F * categoryAnimation)), floatValue);
            float stringHeight = styledFontRenderer.getStringHeight(icons);
            float stringHeightValue = font.getStringHeight(name);
            float floatValue4 = floatValueResult + (22.0F - stringHeight) / 2.0F + 3.1F + this.getSelectedIconOffsetY(moduleCategory);
            float floatValue5 = floatValueResult + (22.0F - stringHeightValue) / 2.0F + 4.0F;
            if (flag) {
                StyledFontRenderer styledFontRendererValue = this.createStyledFontRendererFromModuleModuleCategory(moduleCategory, 23);
                float floatValue6 = floatValue2 + 8.0F + this.getSelectedIconOffsetX(moduleCategory);
                styledFontRendererValue.drawString(icons, floatValue6 + 2.0F, floatValue4, intValueValue);
                float floatValue7 = floatValue6 + stringWidth + 6.0F + (1.0F - categoryAnimation) * 4.0F;
                if (categoryAnimation > 0.02F) {
                    ScissorStack.push();
                    ScissorStack.setFromComponentCoordinates((double) (floatValue2 + 5.0F), (double) (floatValueResult + 3.0F), (double) (interpolatedValue + 3.0F), 25.0);
                    StyledFontRenderer fontValue = SystemFontManager.getFont("Manrope-Bold.ttf", 16.0F);
                    fontValue.drawString(name, floatValue7 + 3.0F, floatValue5 + 0.0F, intValueResult);
                    ScissorStack.pop();
                }
            } else {
                int collapsedIconSize = this.getCollapsedIconSize(moduleCategory);
                StyledFontRenderer styledFontRendererResult = this.createStyledFontRendererFromModuleModuleCategory(moduleCategory, collapsedIconSize);
                float stringWidthResult = styledFontRendererResult.getStringWidth(icons);
                float stringHeightResult = styledFontRendererResult.getStringHeight(icons);
                float floatValue8 = floatValue2 + (22.0F - stringWidthResult) / 2.5F + this.getCollapsedIconOffsetX(moduleCategory);
                float floatValue9 = floatValue4 + this.getCollapsedIconOffsetY(moduleCategory);
                styledFontRendererResult.drawString(icons, floatValue8, floatValue9, intValueValue);
            }
            floatValue2 += interpolatedValue + 7.0F;
        }
    }

    private Module createModuleFromRgbaWithKey(DrawContext context, int intValue, int intValueValue, float floatValue, float floatValueValue, Module module, float floatValueResult, float floatValueItem, int intValueResult, float floatValueEntry) {
        var settings = module.getSettings();
        this.updateRoundedRectWithRgba(context, floatValue, floatValueValue, floatValueItem, floatValueResult, module, floatValueEntry);
        String name = module.getName();
        String text = "";
        if (this.clickGuiController.getBindingModule() == module) {
            text = " ...";
        } else if (module.getKey() != -1) {
            text = " " + this.clickGuiController.toEnglish(KeyBindUtil.getBindName(module.getKey()));
        }
        int intValueItem = module.isEnable() ? this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 255), floatValueEntry) : this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 64), floatValueEntry);
        int intValueEntry = this.calculateApplyAlpha(ColorUtil.rgba(126, 124, 121, 255), floatValueEntry);
        float floatValueCopy = floatValueValue + 10.5F;
        this.getTextFont(18).draw(context.getMatrices(), name, floatValue + 8.5F, floatValueCopy, intValueItem);
        if (!text.isEmpty()) {
            float width = this.getTextFont(18).getWidth(name);
            this.getTextFont(18).draw(context.getMatrices(), text, floatValue + 8.5F + width, floatValueCopy, intValueEntry);
        }
        if (settings != null && !settings.isEmpty()) {
            StyledFontRenderer font = SystemFontManager.getFont("icon2.ttf", 14.0F);
            String textValue = "r";
            float stringHeight = font.getStringHeight(textValue);
            float floatValue2 = floatValue + 152.5F;
            float floatValue3 = floatValueValue + (30.0F - stringHeight) / 2.0F + 1.5F;
            font.drawRotatedGlyph(textValue.charAt(0), floatValue2, floatValue3, this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 180), floatValueEntry), 180.0F);
        }
        if (settings != null && !settings.isEmpty() && floatValueResult > 0.02F) {
            this.settingRenderer.render(context, module, floatValue, floatValueValue, floatValueResult, floatValueEntry, intValueResult, intValue, intValueValue, this.clickGuiController);
        }
        float moduleToggleAnimation = this.clickGuiController.getModuleToggleAnimation(module);
        float floatValue4 = floatValue + (settings != null && !settings.isEmpty() ? 128.0F : 146.0F);
        float floatValue5 = floatValueValue + 11.0F;
        int intValueCopy = this.calculateApplyAlpha(module.isEnable() ? ColorMathUtil.setAlphaColor(intValueResult, 255) : ColorUtil.rgba(255, 255, 255, 13), floatValueEntry);
        int intValue2 = this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, (int) MathHelper.lerp(moduleToggleAnimation, 64.0F, 255.0F)), floatValueEntry);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue4, floatValue5, 16.0F, 8.0F, module.isEnable() ? 3.5F : 4.0F, intValueCopy);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue4 + MathHelper.lerp(moduleToggleAnimation, 1.5F, 9.0F), floatValue5 + 1.0F, 6.0F, 6.0F, 3.0F, intValue2);
        return HoverUtil.isHovered(intValue, intValueValue, floatValue + 0.0F, floatValueValue, 172.0, 30.0) ? module : null;
    }

    private void updateRoundedRectWithRgba(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, Module module, float floatValueEntry) {
        int intValue = this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, module.isEnable() ? 10 : 5), floatValueEntry);
        if (floatValueItem > 0.001F) {
            int intValueValue = this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, module.isEnable() ? 5 : 3), floatValueEntry);
            float floatValueCopy = floatValueValue + 30.0F - 1.0F;
            float floatValue2 = floatValueResult - 30.0F + 1.0F;
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue, floatValueCopy, 172.0F, floatValue2, 0.0F, 9.0F, 0.0F, 9.0F, intValueValue);
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue, floatValueValue, 172.0F, 30.0F, 9.0F, 0.0F, 9.0F, 0.0F, intValue);
        } else {
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue, floatValueValue, 172.0F, 30.0F, 9.0F, intValue);
        }
        boolean flag = module.getSettings() != null && !module.getSettings().isEmpty();
        float floatValue3 = flag ? 41.0F : 24.0F;
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValue + 172.0F - 6.0F - floatValue3, floatValueValue + 6.0F, floatValue3, 18.0F, 6.0F, this.calculateApplyAlpha(ColorUtil.rgba(255, 255, 255, 5), floatValueEntry));
    }

    private int calculateThemeWithThemes() {
        return Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color[0];
    }

    private void updateRgbaWithWidth(DrawContext context, int intValue, float floatValue) {
        float floatValueValue = 23.0F;
        float floatValueResult = 80.0F;
        float searchX = ClickGuiLayout.getSearchX(this.clickGuiController.getX());
        float searchY = ClickGuiLayout.getSearchY(this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY());
        Renderer2D.drawRoundedRect(context.getMatrices(), searchX, searchY, floatValueResult, floatValueValue, 6.0F, ColorUtil.rgba(255, 255, 255, this.calculateInteger(this.clickGuiController.isSearchActive() ? 13 : 5, floatValue)));
        String searchText = this.clickGuiController.getSearchText();
        this.updateAnimationForUpdateRgbaWithWidth.update(searchText.isEmpty() ? 1.0F : 0.0F);
        float value = this.updateAnimationForUpdateRgbaWithWidth.getValue();
        float floatValueItem = searchX + 7.0F;
        TextRenderer font = FontManager.getFont("suisse", 14);
        float floatValueEntry = searchY + 9.0F;
        float width = font.getWidth(searchText.substring(0, Math.min(this.clickGuiController.getSearchCursor(), searchText.length())));
        float floatValueCopy = floatValueResult - 7.0F - 7.0F;
        float floatValue2 = Math.max(width - floatValueCopy, Math.min(width, this.clickGuiController.getSearchTextOffset()));
        floatValue2 = Math.min(floatValue2, Math.max(0.0F, font.getWidth(searchText) - floatValueCopy));
        this.clickGuiController.setSearchTextOffset(floatValue2);
        float floatValue3 = floatValueItem - this.clickGuiController.getSearchTextOffset();
        int intValueValue = (int) (76.0F * value * floatValue);
        if (intValueValue >= 4) {
            font.draw(context.getMatrices(), "Search...", floatValueItem, floatValueEntry, ColorUtil.rgba(255, 255, 255, intValueValue));
        }
        if (!searchText.isEmpty() || this.clickGuiController.isSearchActive()) {
            float floatValue4 = 3.0F;
            float floatValue5 = 1.5F;
            int intValueResult = (int) (230.0F * (1.0F - value) * floatValue);
            int intValueItem = ColorUtil.rgba(255, 255, 255, intValueResult);
            ScissorStack.push();
            ScissorStack.setFromComponentCoordinates((double) (floatValueItem - floatValue4), (double) searchY, (double) (floatValueResult - 9.0F), (double) floatValueValue);
            if (!searchText.isEmpty() && this.clickGuiController.hasSearchSelection()) {
                int searchSelectionStart = this.clickGuiController.getSearchSelectionStart();
                int searchSelectionEnd = this.clickGuiController.getSearchSelectionEnd();
                float floatValue6 = floatValue3 + font.getWidth(searchText.substring(0, searchSelectionStart)) - floatValue4;
                float floatValue7 = font.getWidth(searchText.substring(searchSelectionStart, searchSelectionEnd)) + floatValue4 + floatValue5;
                Renderer2D.drawRoundedRect(context.getMatrices(), floatValue6, searchY + 6.2F, floatValue7, 8.5F, 1.5F, this.calculateApplyAlpha(ColorMathUtil.rgba(42, 115, 255, 155), floatValue));
            }
            if (intValueResult >= 4) {
                font.draw(context.getMatrices(), searchText, floatValue3, floatValueEntry, intValueItem);
            }
            if (this.clickGuiController.isSearchActive() && System.currentTimeMillis() / 500L % 2L == 0L) {
                float floatValue8 = floatValue3 + width;
                Renderer2D.drawRoundedRect(context.getMatrices(), floatValue8 + 1.0F, searchY + 6.5F, 0.8F, 9.0F, 0.0F, this.calculateApplyAlpha(ColorMathUtil.applyAlpha(intValue, 0.9F), floatValue));
            }
            ScissorStack.pop();
        }
        if (intValueValue >= 4) {
            FontManager.getFont("toshiba", 16).draw(context.getMatrices(), "z", searchX + floatValueResult - 13.0F, searchY + 10.0F, ColorUtil.rgba(255, 255, 255, intValueValue));
        }
    }

    private List<String> createListFromAppendWithToString(TextRenderer textRenderer, String text, float floatValue) {
        ArrayList<String> values = new ArrayList<>(3);
        String[] texts = text.trim().split("\\s+");
        StringBuilder textBuilder = new StringBuilder();
        for (int index = 0; index < texts.length; index++) {
            String textValue = textBuilder.isEmpty() ? texts[index] : textBuilder + " " + texts[index];
            if (!textBuilder.isEmpty() && textRenderer.getRenderWidth(textValue) > floatValue) {
                values.add(textBuilder.toString());
                textBuilder.setLength(0);
                if (values.size() == 2) {
                    textBuilder.append(texts[index]);
                    for (int indexValue = index + 1; indexValue < texts.length; indexValue++) {
                        textBuilder.append(' ').append(texts[indexValue]);
                    }
                    break;
                }
            }
            if (textBuilder.isEmpty()) {
                textBuilder.append(texts[index]);
            } else {
                textBuilder.append(' ').append(texts[index]);
            }
        }
        if (!textBuilder.isEmpty() && values.size() < 3) {
            values.add(textBuilder.toString());
        }
        return values;
    }

    private float calculateStringWidthWithCategoryAnimation(ModuleCategory[] moduleCategories) {
        float floatValue = 0.0F;
        for (ModuleCategory moduleCategory : moduleCategories) {
            float categoryAnimation = this.clickGuiController.getCategoryAnimation(moduleCategory);
            StyledFontRenderer styledFontRenderer = this.createStyledFontRendererFromModuleModuleCategory(moduleCategory, 14);
            float stringWidth = styledFontRenderer.getStringWidth(moduleCategory.getIcons());
            float stringWidthValue = SystemFontManager.getFont("Manrope-Bold.ttf", 14.0F).getStringWidth(brova.toshiba.client.utils.runtime.AccessGuard.invoke(moduleCategory, new Object[] {}, (accessReceiver, accessArguments) -> ((ModuleCategory) accessReceiver).getName()));
            float floatValueValue = 16.0F + stringWidth + 6.0F + stringWidthValue - 2.0F;
            floatValue += MathHelper.lerp(categoryAnimation, 22.0F, floatValueValue) + 7.0F;
        }
        if (floatValue > 0.0F) {
            floatValue -= 7.0F;
        }
        return floatValue;
    }

    private StyledFontRenderer createStyledFontRendererFromModuleModuleCategory(ModuleCategory moduleCategory, int intValue) {
        String text = switch(moduleCategory) {
            case COMBAT ->
                "icon2.ttf";
            case MOVEMENT ->
                "rich_reg.ttf";
            case CONFIG ->
                "relic.ttf";
            case FRIENDS ->
                "rain2.ttf";
            default ->
                "rain.ttf";
        };
        return SystemFontManager.getFont(text, intValue);
    }

    private String createTextFromModuleModuleCategory(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT ->
                "C";
            case MOVEMENT ->
                "S";
            case CONFIG, FRIENDS, MISC, GHOST ->
                "k";
            case RENDER ->
                "i";
            case PLAYER ->
                "p";
        };
    }

    private float getSelectedIconOffsetX(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT ->
                0.0F;
            case MOVEMENT ->
                0.5F;
            case CONFIG, FRIENDS, GHOST ->
                0.5F;
            case MISC ->
                0.5F;
            case RENDER ->
                0.0F;
            case PLAYER ->
                0.0F;
            default ->
                0.0F;
        };
    }

    private float getSelectedIconOffsetY(ModuleCategory moduleCategory) {
        return 0.0F;
    }

    private int getCollapsedIconSize(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT, MOVEMENT, MISC, GHOST, RENDER, PLAYER ->
                25;
            case CONFIG, FRIENDS ->
                25;
            default ->
                20;
        };
    }

    private float getCollapsedIconOffsetX(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT ->
                7.0F;
            case MOVEMENT ->
                8.0F;
            case CONFIG, FRIENDS, GHOST ->
                7.5F;
            case MISC ->
                7.5F;
            case RENDER ->
                7.3F;
            case PLAYER ->
                7.0F;
            default ->
                0.0F;
        };
    }

    private float getCollapsedIconOffsetY(ModuleCategory moduleCategory) {
        return switch(moduleCategory) {
            case COMBAT, MOVEMENT, MISC, GHOST, RENDER, PLAYER ->
                -0.8F;
            case CONFIG, FRIENDS ->
                -0.8F;
            default ->
                -2.2F;
        };
    }

    private TextRenderer getTextFont(int intValue) {
        return FontManager.getFont("manrope", intValue);
    }

    private TextRenderer getPrimaryIconFont(int intValue) {
        return FontManager.getFont("icon", intValue);
    }

    private TextRenderer getSecondaryIconFont(int intValue) {
        return FontManager.getFont("icon1", intValue);
    }

    private TextRenderer getLogoFont(int intValue) {
        return FontManager.getFont("logo", intValue);
    }

    private int calculateApplyAlpha(int intValue, float floatValue) {
        int intValueValue = ColorMathUtil.applyAlpha(intValue, floatValue);
        return floatValue > 0.0F && (intValueValue >>> 24 & 0xFF) == 0 ? intValueValue & 16777215 | 16777216 : intValueValue;
    }

    private int calculateInteger(int intValue, float floatValue) {
        return MathHelper.clamp(Math.max(1, (int) (intValue * floatValue)), 1, intValue);
    }
}
