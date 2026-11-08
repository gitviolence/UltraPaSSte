package brova.toshiba.client.gui.click;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.modules.settings.implement.TextSetting;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.input.KeyBindUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SettingRenderer {

    public void render(DrawContext context, Module module, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, double doubleValue, double doubleValueValue, ClickGuiController clickGuiController) {
        var settings = module.getSettings();
        if (settings != null && !settings.isEmpty() && !(floatValueResult <= 0.01F)) {
            float floatValueEntry = ClickGuiLayout.calculateSettingsHeight(module, clickGuiController);
            float floatValueCopy = floatValueValue + 30.0F;
            float floatValue2 = floatValueEntry * floatValueResult;
            ScissorStack.push();
            ScissorStack.setFromComponentCoordinates((double) (floatValue + 0.0F), (double) floatValueCopy, 172.0, (double) floatValue2);
            float floatValue3 = 30.0F;
            for (SettingBase setting : settings) {
                if (setting != null && setting.visible()) {
                    float floatValue4 = floatValueValue + floatValue3 + 4.0F;
                    int intValueValue = Math.round(255.0F * floatValueResult * MathHelper.clamp(floatValueItem, 0.0F, 1.0F));
                    if (setting instanceof BooleanSetting settingValue) {
                        this.updateBooleanSetting(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, settingValue, clickGuiController);
                        floatValue3 += 16.0F;
                    } else if (setting instanceof TextSetting textSetting) {
                        this.updateRgbaWithDraw(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, textSetting, clickGuiController);
                        floatValue3 += 34.0F;
                    } else if (setting instanceof FloatSetting settingResult) {
                        this.updateActiveWithValue(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, settingResult, clickGuiController);
                        floatValue3 += 24.0F;
                    } else if (setting instanceof ModeSetting settingItem) {
                        this.updateModeSetting(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, settingItem, clickGuiController);
                        floatValue3 += ClickGuiLayout.calculateModeSettingHeight(settingItem);
                    } else if (setting instanceof MultiSetting multiSetting) {
                        this.updateRgbaWithRoundedRect(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, multiSetting, clickGuiController);
                        floatValue3 += ClickGuiLayout.calculateListSettingHeight(multiSetting);
                    } else if (setting instanceof BindSetting bindSetting) {
                        this.updateRgbaWithDraw(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, bindSetting, clickGuiController);
                        floatValue3 += 34.0F;
                    } else if (setting instanceof ColorSetting colorSetting) {
                        this.updateRgbaWithDraw(context, floatValue, floatValue4, intValueValue, intValue, doubleValue, doubleValueValue, colorSetting, clickGuiController);
                        floatValue3 += 16.0F;
                    }
                }
            }
            ScissorStack.pop();
        }
    }

    private void updateBooleanSetting(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, BooleanSetting setting, ClickGuiController clickGuiController) {
        this.renderBooleanSetting(context, floatValue + 10.0F, floatValueValue, 155.0F, intValue, intValueValue, setting, clickGuiController);
    }

    public void renderBooleanSetting(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue, BooleanSetting setting, ClickGuiController clickGuiController) {
        if (intValue > 0) {
            Animation booleanCircleAnimation = clickGuiController.getBooleanCircleAnimation(setting);
            booleanCircleAnimation.update(setting.isState() ? 1.0F : 0.0F);
            float value = booleanCircleAnimation.getValue();
            TextRenderer font = FontManager.getFont("suisse", 14);
            font.draw(context.getMatrices(), setting.name(), floatValue, floatValueValue + 2.0F, ColorUtil.rgba(255, 255, 255, intValue));
            float floatValueItem = floatValue + floatValueResult - 22.0F;
            float floatValueEntry = floatValueItem + 3.0F;
            int intValueResult = ColorMathUtil.interpolateColor(ColorUtil.rgba(255, 255, 255, (int) (13.0F * (intValue / 255.0F))), ColorMathUtil.setAlphaColor(intValueValue, intValue), value);
            int intValueItem = ColorMathUtil.interpolateColor(ColorUtil.rgba(0, 0, 0, (int) (64.0F * (intValue / 255.0F))), ColorUtil.rgba(255, 255, 255, intValue), value);
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValueItem, floatValueValue - 2.0F, 22.0F, 12.0F, 5.0F, ColorUtil.rgba(255, 255, 255, (int) (5.0F * (intValue / 255.0F))));
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValueEntry, floatValueValue, 16.0F, 8.0F, setting.isState() ? 3.5F : 4.0F, intValueResult);
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValueEntry + MathHelper.lerp(value, 1.5F, 9.0F), floatValueValue + 1.0F, 6.0F, 6.0F, 3.0F, intValueItem);
        }
    }

    private void updateActiveWithValue(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, FloatSetting setting, ClickGuiController clickGuiController) {
        if (setting.isActive()) {
            setting.setValue(clickGuiController.updateActiveSliderValue(setting, doubleValue));
        }
        this.renderFloatSetting(context, floatValue + 10.0F, floatValueValue, ClickGuiLayout.getSliderWidth(), intValue, intValueValue, setting, clickGuiController);
    }

    public void renderFloatSetting(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue, FloatSetting setting, ClickGuiController clickGuiController) {
        if (intValue > 0) {
            Animation sliderAnimation = clickGuiController.getSliderAnimation(setting);
            sliderAnimation.update(clickGuiController.getSliderPos(setting));
            float floatValueItem = Math.max(0.0F, Math.min(1.0F, sliderAnimation.getValue()));
            TextRenderer font = FontManager.getFont("suisse", 14);
            TextRenderer fontValue = FontManager.getFont("suisse", 14);
            String text;
            if (setting.getIncrement() >= 1.0F) {
                text = String.valueOf((int) setting.get());
            } else {
                text = String.format(Locale.US, "%.2f", setting.get()).replaceAll("\\.?0+$", "");
            }
            int intValueResult = ColorUtil.rgba(255, 255, 255, intValue);
            int intValueItem = ColorMathUtil.setAlphaColor(intValueValue, intValue);
            font.draw(context.getMatrices(), setting.displayName(), floatValue, floatValueValue + 2.0F, intValueResult);
            float width = fontValue.getWidth(text);
            float floatValueEntry = floatValue + floatValueResult - width;
            fontValue.draw(context.getMatrices(), text, floatValueEntry - 2.0F, floatValueValue + 2.0F, intValueItem);
            float floatValueCopy = floatValue + floatValueResult;
            float floatValue2 = floatValueValue + 13.0F;
            float floatValue3 = 4.0F;
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue, floatValue2, floatValueResult, floatValue3, 1.5F, ColorMathUtil.rgba(55, 60, 66, (int) (200.0F * (intValue / 255.0F))));
            float floatValue4 = floatValueResult * floatValueItem;
            if (floatValue4 > 0.5F) {
                Renderer2D.drawRoundedRect(context.getMatrices(), floatValue, floatValue2, floatValue4, floatValue3, 1.5F, ColorMathUtil.setAlphaColor(intValueValue, intValue));
            }
            float floatValue5 = 5.0F;
            float floatValue6 = floatValue + floatValueResult * floatValueItem;
            float floatValue7 = Math.max(floatValue, Math.min(floatValueCopy - floatValue5, floatValue6 - floatValue5 / 2.0F));
            float floatValue8 = floatValue2 - (floatValue5 - floatValue3) / 2.0F;
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue7, floatValue8, floatValue5, floatValue5, floatValue5 / 2.0F, ColorUtil.rgba(255, 255, 255, intValue));
        }
    }

    private void updateRgbaWithDraw(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, TextSetting textSetting, ClickGuiController clickGuiController) {
        String text = textSetting.get();
        boolean flag = clickGuiController.getEditingTextSetting() == textSetting;
        boolean flagValue = text == null || text.isEmpty();
        String textValue = flagValue ? "Type something..." : text;
        String textResult = flag ? textValue + "_" : textValue;
        float floatValueResult = floatValue + 7.0F;
        float floatValueItem = floatValueValue + 13.0F;
        float floatValueEntry = 158.0F;
        float floatValueCopy = 14.5F;
        TextRenderer font = FontManager.getFont("suisse", 14);
        font.draw(context.getMatrices(), textSetting.name(), floatValue + 10.0F, floatValueValue + 2.0F, ColorUtil.rgba(255, 255, 255, intValue));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, 6.0F, ColorUtil.rgba(255, 255, 255, (int) (8.0F * (intValue / 255.0F))));
        ScissorStack.push();
        ScissorStack.setFromComponentCoordinates((double) (floatValueResult + 4.0F), (double) floatValueItem, (double) (floatValueEntry - 8.0F), (double) floatValueCopy);
        font.draw(context.getMatrices(), textResult, floatValueResult + 5.5F, floatValueItem + 5.0F, ColorUtil.rgba(255, 255, 255, (int) ((flagValue ? 38.0F : 255.0F) * (intValue / 255.0F))));
        ScissorStack.pop();
    }

    private void updateModeSetting(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, ModeSetting setting, ClickGuiController clickGuiController) {
        this.renderModeSetting(context, floatValue + 7.0F, floatValueValue, 158.0F, 5, intValue, intValueValue, doubleValue, doubleValueValue, setting, clickGuiController);
    }

    public void renderModeSetting(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue, int intValueResult, double doubleValue, double doubleValueValue, ModeSetting setting, ClickGuiController clickGuiController) {
        TextRenderer font = FontManager.getFont("suisse", 14);
        String current = setting.getCurrent();
        float width = font.getWidth(current);
        int intValueItem = ColorUtil.rgba(255, 255, 255, intValueValue);
        font.draw(context.getMatrices(), setting.name(), floatValue + 2.5F, floatValueValue + 2.0F, intValueItem);
        font.draw(context.getMatrices(), current, (float) Math.round(floatValue + floatValueResult - width), floatValueValue + 2.0F, ColorMathUtil.setAlphaColor(intValueResult, intValueValue));
        var mods = setting.getMods();
        if (!mods.isEmpty()) {
            float floatValueItem = floatValue;
            float floatValueEntry = floatValueValue + 13.0F;
            float floatValueCopy = floatValueResult;
            float floatValue2 = 15.5F;
            for (int index = 0; index < mods.size(); index++) {
                String text = mods.get(index);
                boolean flag = setting.getCurrent().equals(text);
                int intValueEntry = index / intValue;
                int intValueCopy = index % intValue;
                int intValue2 = Math.min(intValue, mods.size() - intValueEntry * intValue);
                float floatValue3 = floatValueCopy / intValue;
                float floatValue4 = floatValueEntry + intValueEntry * 14.5F;
                if (intValueCopy == 0) {
                    boolean flagValue = intValueEntry > 0;
                    boolean flagResult = (intValueEntry + 1) * intValue < mods.size();
                    int intValue3 = Math.min(intValue, mods.size() - intValueEntry * intValue);
                    int intValue4 = flagResult ? Math.min(intValue, mods.size() - (intValueEntry + 1) * intValue) : 0;
                    float floatValue5 = flagValue ? 0.0F : 6.0F;
                    float floatValue6 = flagResult ? 0.0F : 6.0F;
                    float floatValue7 = flagValue ? 0.0F : 6.0F;
                    float floatValue8 = flagResult && intValue4 >= intValue3 ? 0.0F : 6.0F;
                    Renderer2D.drawRoundedRect(context.getMatrices(), floatValueItem, floatValue4, floatValue3 * intValue2, floatValue2, floatValue5, floatValue6, floatValue7, floatValue8, ColorUtil.rgba(255, 255, 255, (int) (8.0F * (intValueValue / 255.0F))));
                }
                float floatValue9 = Math.round(floatValueItem + floatValue3 * intValueCopy);
                float value = clickGuiController.getModeAnimation(this.createTextFromIdentityHashCode(setting, text), flag).getValue();
                if (value > 0.001F) {
                    Renderer2D.drawRoundedRect(context.getMatrices(), floatValue9, floatValue4, floatValue3, floatValue2, 6.0F, ColorMathUtil.setAlphaColor(intValueResult, (int) (intValueValue * value)));
                }
                int intValue5 = flag ? ColorUtil.rgba(255, 255, 255, intValueValue) : ColorUtil.rgba(255, 255, 255, (int) (38.0F * (intValueValue / 255.0F)));
                this.updateWidthWithDraw(font, context.getMatrices(), text, floatValue9, floatValue4 + 5.0F, floatValue3, floatValue2, intValue5, doubleValue, doubleValueValue, clickGuiController, this.createTextFromIdentityHashCode(setting, text));
            }
        }
    }

    public void renderSlidingModeSetting(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, int intValue, int intValueValue, double doubleValue, double doubleValueValue, ModeSetting setting, ClickGuiController clickGuiController) {
        TextRenderer font = FontManager.getFont("suisse", 14);
        int intValueResult = ColorUtil.rgba(255, 255, 255, intValue);
        font.draw(context.getMatrices(), setting.name(), floatValue + 2.5F, floatValueValue + 2.0F, intValueResult);
        String current = setting.getCurrent();
        font.draw(context.getMatrices(), current, (float) Math.round(floatValue + floatValueResult - font.getWidth(current)), floatValueValue + 2.0F, ColorMathUtil.setAlphaColor(intValueValue, intValue));
        var mods = setting.getMods();
        if (!mods.isEmpty()) {
            float floatValueItem = floatValueValue + 13.0F;
            float floatValueEntry = 15.5F;
            float floatValueCopy = floatValueResult / mods.size();
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue, floatValueItem, floatValueResult, floatValueEntry, 6.0F, ColorUtil.rgba(255, 255, 255, Math.round(8.0F * intValue / 255.0F)));
            float floatValue2 = floatValue + floatValueCopy * clickGuiController.getModeSelectorAnimation(setting);
            Renderer2D.drawRoundedRect(context.getMatrices(), floatValue2, floatValueItem, floatValueCopy, floatValueEntry, 6.0F, ColorMathUtil.setAlphaColor(intValueValue, intValue));
            for (int index = 0; index < mods.size(); index++) {
                String text = mods.get(index);
                String textValue = this.createTextFromIdentityHashCode(setting, text);
                float value = clickGuiController.getModeAnimation(textValue, setting.is(text)).getValue();
                int intValueItem = Math.round(MathHelper.lerp(value, 38.0F, 255.0F) * intValue / 255.0F);
                this.updateWidthWithDraw(font, context.getMatrices(), text, floatValue + floatValueCopy * index, floatValueItem + 5.0F, floatValueCopy, floatValueEntry, ColorUtil.rgba(255, 255, 255, intValueItem), doubleValue, doubleValueValue, clickGuiController, textValue);
            }
        }
    }

    private void updateRgbaWithRoundedRect(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, MultiSetting multiSetting, ClickGuiController clickGuiController) {
        TextRenderer font = FontManager.getFont("suisse", 14);
        int intValueResult = 0;
        int intValueItem = 0;
        for (BooleanSetting setting : multiSetting.getSettings()) {
            if (setting.visible()) {
                intValueResult++;
                if (setting.isState()) {
                    intValueItem++;
                }
            }
        }
        if (intValueResult != 0) {
            font.draw(context.getMatrices(), multiSetting.name(), floatValue + 10.0F, floatValueValue + 2.0F, ColorUtil.rgba(255, 255, 255, intValue));
            String text = intValueItem + " / " + intValueResult;
            float width = font.getWidth(text);
            font.draw(context.getMatrices(), text, (float) Math.round(floatValue + 172.0F - width - 7.0F), floatValueValue + 2.0F, ColorMathUtil.setAlphaColor(intValueValue, intValue));
            float floatValueResult = floatValue + 7.0F;
            float floatValueItem = floatValueValue + 13.0F;
            float floatValueEntry = 158.0F;
            float floatValueCopy = 15.5F;
            float floatValue2 = floatValueEntry / 5.0F;
            for (int index = 0; index < intValueResult; index++) {
                BooleanSetting settingValue = this.createSettingsWithVisible(multiSetting, index);
                if (settingValue != null) {
                    int intValueEntry = index / 5;
                    int intValueCopy = index % 5;
                    int intValue2 = Math.min(5, intValueResult - intValueEntry * 5);
                    float floatValue3 = floatValueItem + intValueEntry * 14.5F;
                    if (intValueCopy == 0) {
                        boolean flag = intValueEntry > 0;
                        boolean flagValue = (intValueEntry + 1) * 5 < intValueResult;
                        int intValue3 = Math.min(5, intValueResult - intValueEntry * 5);
                        int intValue4 = flagValue ? Math.min(5, intValueResult - (intValueEntry + 1) * 5) : 0;
                        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueResult, floatValue3, floatValue2 * intValue2, floatValueCopy, flag ? 0.0F : 6.0F, flagValue ? 0.0F : 6.0F, flag ? 0.0F : 6.0F, flagValue && intValue4 >= intValue3 ? 0.0F : 6.0F, ColorUtil.rgba(255, 255, 255, (int) (8.0F * (intValue / 255.0F))));
                        int intValue5 = intValueEntry * 5;
                        int intValue6 = intValue5 + intValue2;
                        int intValue7 = intValue5;
                        while (intValue7 < intValue6) {
                            BooleanSetting settingResult = this.createSettingsWithVisible(multiSetting, intValue7);
                            if (settingResult != null && settingResult.isState()) {
                                int intValue8 = intValue7;
                                do {
                                    intValue7++;
                                    settingResult = intValue7 < intValue6 ? this.createSettingsWithVisible(multiSetting, intValue7) : null;
                                } while (intValue7 < intValue6 && settingResult != null && settingResult.isState());
                                int intValue9 = intValue8 - intValue5;
                                int intValue10 = intValue7 - intValue5;
                                float floatValue4 = intValue9 == 0 ? (flag ? 0.0F : 6.0F) : 6.0F;
                                float floatValue5 = intValue9 == 0 ? (flagValue ? 0.0F : 6.0F) : 6.0F;
                                float floatValue6 = intValue10 == intValue2 ? (flag ? 0.0F : 6.0F) : 6.0F;
                                float floatValue7 = intValue10 == intValue2 ? (flagValue && intValue4 >= intValue3 ? 0.0F : 6.0F) : 6.0F;
                                Renderer2D.drawRoundedRect(context.getMatrices(), floatValueResult + floatValue2 * (intValue8 - intValue5), floatValue3, floatValue2 * (intValue7 - intValue8), floatValueCopy, floatValue4, floatValue5, floatValue6, floatValue7, ColorMathUtil.setAlphaColor(intValueValue, intValue));
                                for (int indexValue = intValue8 + 1; indexValue < intValue7; indexValue++) {
                                    float floatValue8 = Math.round(floatValueResult + floatValue2 * (indexValue - intValue5)) - 1.0F;
                                    Renderer2D.drawRoundedRect(context.getMatrices(), floatValue8, floatValue3 + 4.75F, 2.0F, 5.0F, 1.0F, ColorUtil.rgba(255, 255, 255, (int) (64.0F * (intValue / 255.0F))));
                                }
                            } else {
                                intValue7++;
                            }
                        }
                    }
                    float floatValue9 = Math.round(floatValueResult + floatValue2 * intValueCopy);
                    int intValue11 = settingValue.isState() ? ColorUtil.rgba(255, 255, 255, intValue) : ColorUtil.rgba(255, 255, 255, (int) (38.0F * (intValue / 255.0F)));
                    this.updateWidthWithDraw(font, context.getMatrices(), settingValue.name(), floatValue9, floatValue3 + 5.0F, floatValue2, floatValueCopy, intValue11, doubleValue, doubleValueValue, clickGuiController, this.createTextFromHCodeWithName(multiSetting, settingValue));
                }
            }
        }
    }

    private BooleanSetting createSettingsWithVisible(MultiSetting multiSetting, int intValue) {
        int intValueValue = 0;
        for (BooleanSetting setting : multiSetting.getSettings()) {
            if (setting.visible()) {
                if (intValueValue == intValue) {
                    return setting;
                }
                intValueValue++;
            }
        }
        return null;
    }

    private void updateWidthWithDraw(TextRenderer textRenderer, MatrixStack matrices, String text, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, double doubleValue, double doubleValueValue, ClickGuiController clickGuiController, String textValue) {
        float floatValueEntry = floatValue + 3.0F;
        float floatValueCopy = floatValueResult - 6.0F;
        float width = textRenderer.getWidth(text);
        float floatValue2 = width - floatValueCopy;
        float floatValue3 = Math.round(floatValue + (floatValueResult - width) / 2.0F);
        if (floatValue2 <= 0.0F) {
            textRenderer.draw(matrices, text, floatValue3, floatValueValue, intValue);
        } else {
            boolean floatValue4 = this.isFloat(floatValue, floatValueValue - 5.0F, floatValueResult, floatValueItem, doubleValue, doubleValueValue);
            float floatValue5 = clickGuiController.advanceTextScrollPhase(textValue, floatValue4);
            boolean textScrollActive = clickGuiController.isTextScrollActive(textValue, floatValue4);
            Animation textHoverAnimation = clickGuiController.getTextHoverAnimation(textValue, textScrollActive);
            if (textScrollActive) {
                textHoverAnimation.update(1.0F);
            } else {
                textHoverAnimation.setValue(0.0F);
            }
            float floatValue6 = this.calculateFloat(floatValue2, floatValue5) * textHoverAnimation.getValue();
            float floatValue7 = Math.min(12.0F, floatValueCopy * 0.5F);
            float floatValue8 = floatValueEntry + floatValueCopy - floatValue7;
            int intValueValue = intValue >>> 24 & 0xFF;
            float floatValue9 = floatValueEntry - floatValue6;
            ScissorStack.push();
            ScissorStack.setFromComponentCoordinates((double) floatValueEntry, (double) (floatValueValue - 5.0F), (double) floatValueCopy, (double) floatValueItem);
            for (int index = 0; index < text.length(); index++) {
                String textResult = String.valueOf(text.charAt(index));
                float widthValue = textRenderer.getWidth(textResult);
                if (floatValue9 + widthValue >= floatValueEntry && floatValue9 <= floatValueEntry + floatValueCopy) {
                    float floatValue10 = floatValue6 > 0.1F ? this.calculateFloat(floatValue9, floatValueEntry, floatValue7) : 1.0F;
                    float floatValue11 = floatValue6 > 0.1F ? this.calculateFloat(floatValue9 + widthValue, floatValueEntry, floatValue7) : 1.0F;
                    float floatValue12 = this.calculateFloat(floatValue9, floatValue8, floatValueEntry + floatValueCopy, floatValue7);
                    float floatValue13 = this.calculateFloat(floatValue9 + widthValue, floatValue8, floatValueEntry + floatValueCopy, floatValue7);
                    textRenderer.drawGradientStringHorizontal(matrices, textResult, floatValue9, floatValueValue, this.calculateInteger(intValue, intValueValue, floatValue10 * floatValue12), this.calculateInteger(intValue, intValueValue, floatValue11 * floatValue13));
                }
                floatValue9 += widthValue;
            }
            ScissorStack.pop();
        }
    }

    private float calculateFloat(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        return floatValue <= floatValueValue ? 1.0F : Math.max(0.0F, Math.min(1.0F, (floatValueResult - floatValue) / floatValueItem));
    }

    private float calculateFloat(float floatValue, float floatValueValue, float floatValueResult) {
        return Math.max(0.0F, Math.min(1.0F, (floatValue - floatValueValue) / floatValueResult));
    }

    private int calculateInteger(int intValue, int intValueValue, float floatValue) {
        int intValueResult = Math.max(4, Math.round(intValueValue * floatValue));
        return intValue & 16777215 | intValueResult << 24;
    }

    private void updateRgbaWithDraw(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, BindSetting bindSetting, ClickGuiController clickGuiController) {
        boolean flag = clickGuiController.getBindingSetting() == bindSetting;
        Animation bindAnimation = clickGuiController.getBindAnimation(this.createTextFromHCode(bindSetting), flag);
        bindAnimation.update(flag ? 1.0F : 0.0F);
        float value = bindAnimation.getValue();
        TextRenderer font = FontManager.getFont("suisse", 14);
        String text = bindSetting.getKey() == -1 ? "Press" : clickGuiController.toEnglish(KeyBindUtil.getBindName(bindSetting.getKey()));
        float width = font.getWidth(text);
        float widthValue = font.getWidth("...");
        float floatValueResult = floatValue + 7.0F;
        float floatValueItem = floatValueValue + 13.0F;
        float floatValueEntry = 158.0F;
        float floatValueCopy = 14.5F;
        font.draw(context.getMatrices(), bindSetting.name(), floatValue + 10.0F, floatValueValue + 2.0F, ColorUtil.rgba(255, 255, 255, intValue));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, 6.0F, ColorMathUtil.setAlphaColor(intValueValue, intValue));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueResult, floatValueItem, floatValueEntry, floatValueCopy, 6.0F, ColorUtil.rgba(255, 255, 255, (int) (34.0F * value * (intValue / 255.0F))));
        float floatValue2 = Math.max(0.0F, 1.0F - value * 2.0F);
        float floatValue3 = Math.max(0.0F, value * 2.0F - 1.0F);
        int intValueResult = ColorUtil.rgba(255, 255, 255, (int) (intValue * floatValue2));
        int intValueItem = ColorUtil.rgba(255, 255, 255, (int) (intValue * floatValue3));
        if (floatValue2 > 0.001F) {
            font.draw(context.getMatrices(), text, (float) Math.round(floatValueResult + (floatValueEntry - width) / 2.0F), floatValueItem + 5.0F, intValueResult);
        }
        if (floatValue3 > 0.001F) {
            font.draw(context.getMatrices(), "...", (float) Math.round(floatValueResult + (floatValueEntry - widthValue) / 2.0F), floatValueItem + 5.0F, intValueItem);
        }
    }

    private String createTextFromIdentityHashCode(ModeSetting setting, String text) {
        return System.identityHashCode(setting) + "_mode_" + text;
    }

    private String createTextFromHCodeWithName(MultiSetting multiSetting, BooleanSetting setting) {
        return multiSetting.hashCode() + "_list_" + setting.name();
    }

    private String createTextFromHCode(BindSetting bindSetting) {
        return bindSetting.hashCode() + "_bind";
    }

    private String formatTextFromFormatWithIncrement(FloatSetting setting) {
        float floatValue = setting.get();
        float increment = setting.getIncrement();
        if (increment >= 1.0F) {
            return String.valueOf((int) floatValue);
        } else {
            return increment >= 0.1F ? String.format("%.1f", floatValue) : String.format("%.2f", floatValue);
        }
    }

    private void updateDrawWithHeight(TextRenderer textRenderer, MatrixStack matrices, String text, float floatValue, float floatValueValue, float floatValueResult, int intValue, double doubleValue, double doubleValueValue, ClickGuiController clickGuiController, String textValue) {
        if (text != null && !text.isEmpty() && !(floatValueResult <= 0.0F)) {
            float width = textRenderer.getWidth(text);
            float floatValueItem = width - floatValueResult;
            if (floatValueItem <= 6.0F) {
                textRenderer.draw(matrices, text, floatValue, floatValueValue, intValue);
            } else {
                boolean floatValueEntry = this.isFloat(floatValue, floatValueValue, floatValueResult, textRenderer.getHeight(), doubleValue, doubleValueValue);
                float floatValueCopy = clickGuiController.advanceTextScrollPhase(textValue, floatValueEntry);
                boolean textScrollActive = clickGuiController.isTextScrollActive(textValue, floatValueEntry);
                Animation textHoverAnimation = clickGuiController.getTextHoverAnimation(textValue, textScrollActive);
                textHoverAnimation.update(textScrollActive ? 1.0F : 0.0F);
                float value = textHoverAnimation.getValue();
                float floatValue2 = this.calculateFloat(floatValueItem, floatValueCopy) * value;
                ScissorStack.push();
                ScissorStack.setFromComponentCoordinates((double) floatValue, (double) (floatValueValue - 2.0F), (double) floatValueResult, (double) (textRenderer.getHeight() + 4.0F));
                textRenderer.draw(matrices, text, floatValue - floatValue2, floatValueValue, intValue);
                ScissorStack.pop();
            }
        }
    }

    private int getPrimaryTextColor(int intValue) {
        return ColorUtil.rgba(245, 245, 248, intValue);
    }

    private int getSecondaryTextColor(int intValue) {
        return ColorUtil.rgba(186, 186, 194, intValue);
    }

    private boolean isFloat(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, double doubleValue, double doubleValueValue) {
        return doubleValue >= floatValue && doubleValue <= floatValue + floatValueResult && doubleValueValue >= floatValueValue - 2.0F && doubleValueValue <= floatValueValue + floatValueItem + 2.0F;
    }

    private float calculateFloat(float floatValue, float floatValueValue) {
        if (floatValue <= 0.0F) {
            return 0.0F;
        } else {
            float floatValueResult = floatValueValue < 0.5F ? floatValueValue * 2.0F : 2.0F - floatValueValue * 2.0F;
            float floatValueItem = floatValueResult * floatValueResult * (3.0F - 2.0F * floatValueResult);
            return floatValue * floatValueItem;
        }
    }

    private String createTextFromIdentityHashCode(SettingBase setting) {
        return "setting_text_" + System.identityHashCode(setting);
    }

    private TextRenderer createTextRendererFromInteger(int intValue) {
        return FontManager.getFont("manrope", intValue);
    }

    private void updateRgbaWithDraw(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, double doubleValue, double doubleValueValue, ColorSetting colorSetting, ClickGuiController clickGuiController) {
        TextRenderer font = FontManager.getFont("suisse", 14);
        int intValueResult = ColorUtil.rgba(255, 255, 255, intValue);
        String text = String.format("#%06X", colorSetting.getColor() & 16777215);
        float floatValueResult = 57.5F;
        float floatValueItem = 14.5F;
        float floatValueEntry = floatValue + 172.0F - 7.0F - floatValueResult;
        float floatValueCopy = floatValueValue - 3.5F;
        font.draw(context.getMatrices(), colorSetting.name(), floatValue + 9.5F, floatValueValue + 2.0F, intValueResult);
        int intValueItem = ColorMathUtil.setAlphaColor(intValueValue, intValue);
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueEntry, floatValueCopy, floatValueResult, floatValueItem, 6.0F, intValueItem);
        TextRenderer fontValue = FontManager.getFont("toshiba", 14);
        float floatValue2 = floatValueResult - 42.0F;
        float floatValue3 = floatValueEntry + (floatValue2 - fontValue.getWidth("b")) * 0.5F + 2.0F;
        fontValue.draw(context.getMatrices(), "b", floatValue3, floatValueValue + 2.8F, ColorUtil.rgba(255, 255, 255, intValue));
        Renderer2D.drawRoundedRect(context.getMatrices(), floatValueEntry + floatValueResult - 42.0F, floatValueCopy + 4.25F, 1.0F, 6.0F, 0.5F, ColorUtil.rgba(255, 255, 255, (int) (64.0F * (intValue / 255.0F))));
        font.draw(context.getMatrices(), text, floatValueEntry + floatValueResult - 34.5F, floatValueValue + 2.0F, ColorUtil.rgba(255, 255, 255, intValue));
        Animation colorSettingAnimation = clickGuiController.getColorSettingAnimation(colorSetting);
        colorSettingAnimation.update(clickGuiController.isColorSettingOpened(colorSetting) ? 1.0F : 0.0F);
        if (clickGuiController.getActiveColorSetting() == colorSetting) {
            clickGuiController.setActiveColorSettingPosition(floatValueEntry, floatValueCopy);
        }
    }

    public void renderColorPickerOverlay(DrawContext context, double doubleValue, double doubleValueValue, ClickGuiController clickGuiController, float floatValue) {
        ColorSetting activeColorSetting = clickGuiController.getActiveColorSetting();
        if (activeColorSetting != null) {
            Animation colorSettingAnimation = clickGuiController.getColorSettingAnimation(activeColorSetting);
            colorSettingAnimation.update(clickGuiController.isColorSettingOpened(activeColorSetting) ? 1.0F : 0.0F);
            float value = colorSettingAnimation.getValue();
            if (!(value <= 0.01F)) {
                float activeColorSettingX = clickGuiController.getActiveColorSettingX();
                float activeColorSettingY = clickGuiController.getActiveColorSettingY();
                float floatValueValue = 57.5F;
                float floatValueResult = value * (floatValue / 255.0F);
                int clampedValue = MathHelper.clamp((int) (255.0F * floatValueResult), 1, 255);
                float floatValueItem = activeColorSettingX + floatValueValue / 2.0F - 40.0F;
                float floatValueEntry = activeColorSettingY + 17.5F;
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                float floatValueCopy = 0.85F + 0.15F * value;
                float floatValue2 = floatValueItem + 40.0F;
                float floatValue3 = floatValueEntry + 37.0F;
                matrices.translate(floatValue2, floatValue3, 0.0F);
                matrices.scale(floatValueCopy, floatValueCopy, 1.0F);
                matrices.translate(-floatValue2, -floatValue3, 0.0F);
                int intValue = ColorUtil.rgba(0, 0, 0, MathHelper.clamp((int) (160.0F * floatValueResult), 1, 255));
                Renderer2D.drawRoundedRect(matrices, floatValueItem - 5.0F, floatValueEntry - 2.0F, 90.0F, 78.0F, 3.0F, intValue);
                float floatValue4 = floatValueEntry + 2.0F;
                float floatValue5 = 80.0F;
                float floatValue6 = 46.0F;
                double doubleValueResult = floatValue2 + (doubleValue - floatValue2) / floatValueCopy;
                double doubleValueItem = floatValue3 + (doubleValueValue - floatValue3) / floatValueCopy;
                if (clickGuiController.getDraggingColorSettingSV() == activeColorSetting) {
                    float floatValue7 = (float) (doubleValueResult - floatValueItem);
                    float floatValue8 = (float) (doubleValueItem - floatValue4);
                    activeColorSetting.setSaturation(MathHelper.clamp(floatValue7 / floatValue5, 0.0F, 1.0F));
                    activeColorSetting.setBrightness(MathHelper.clamp(1.0F - floatValue8 / floatValue6, 0.0F, 1.0F));
                }
                int intValueValue = ColorMathUtil.rgba(255, 255, 255, clampedValue);
                int intValueResult = Color.HSBtoRGB(activeColorSetting.getHue(), 1.0F, 1.0F) & 16777215 | clampedValue << 24;
                int intValueItem = ColorMathUtil.rgba(0, 0, 0, clampedValue);
                int intValueEntry = ColorMathUtil.rgba(0, 0, 0, clampedValue);
                Renderer2D.drawGradientRect(matrices, floatValueItem, floatValue4, floatValue5, floatValue6, 2.0F, intValueValue, intValueResult, intValueItem, intValueEntry);
                float floatValue9 = floatValueItem + activeColorSetting.getSaturation() * floatValue5;
                float floatValue10 = floatValue4 + (1.0F - activeColorSetting.getBrightness()) * floatValue6;
                Renderer2D.drawRoundedRect(matrices, floatValue9 - 2.5F, floatValue10 - 2.5F, 5.0F, 5.0F, 2.5F, ColorMathUtil.rgba(0, 0, 0, MathHelper.clamp((int) (180.0F * floatValueResult), 1, 255)));
                Renderer2D.drawRoundedRect(matrices, floatValue9 - 1.5F, floatValue10 - 1.5F, 3.0F, 3.0F, 1.5F, ColorMathUtil.rgba(255, 255, 255, clampedValue));
                float floatValue11 = floatValue4 + floatValue6 + 4.0F;
                float floatValue12 = 80.0F;
                float floatValue13 = 6.0F;
                if (clickGuiController.getDraggingColorSettingHue() == activeColorSetting) {
                    float floatValue14 = (float) (doubleValueResult - floatValueItem);
                    activeColorSetting.setHue(MathHelper.clamp(floatValue14 / floatValue12, 0.0F, 1.0F));
                }
                Renderer2D.drawImage(matrices, "toshiba", "featrues/gui/sliderhue.png", floatValueItem, floatValue11, floatValue12, floatValue13, ColorMathUtil.rgba(255, 255, 255, clampedValue));
                float floatValue15 = floatValueItem + activeColorSetting.getHue() * floatValue12;
                Renderer2D.drawRoundedRect(matrices, floatValue15 - 2.5F, floatValue11 - 0.5F, 5.0F, 7.0F, 2.0F, ColorMathUtil.rgba(0, 0, 0, MathHelper.clamp((int) (180.0F * floatValueResult), 1, 255)));
                Renderer2D.drawRoundedRect(matrices, floatValue15 - 1.5F, floatValue11 + 0.5F, 3.0F, 5.0F, 1.0F, ColorMathUtil.rgba(255, 255, 255, clampedValue));
                float floatValue16 = floatValue11 + floatValue13 + 4.0F;
                float floatValue17 = 80.0F;
                float floatValue18 = 6.0F;
                if (clickGuiController.getDraggingColorSettingAlpha() == activeColorSetting) {
                    float floatValue19 = (float) (doubleValueResult - floatValueItem);
                    activeColorSetting.setAlpha(MathHelper.clamp(floatValue19 / floatValue17, 0.0F, 1.0F));
                }
                Renderer2D.drawImage(matrices, "toshiba", "featrues/gui/alphabar.png", floatValueItem, floatValue16, floatValue17, floatValue18, ColorMathUtil.rgba(255, 255, 255, clampedValue));
                int intValueCopy = activeColorSetting.getColorWithAlpha() & 16777215 | MathHelper.clamp((int) (1.0F * value), 1, 255) << 24;
                int intValue2 = activeColorSetting.getColorWithAlpha() & 16777215 | clampedValue << 24;
                Renderer2D.drawGradientRect(matrices, floatValueItem, floatValue16, floatValue17, floatValue18, 2.0F, intValueCopy, intValue2, intValueCopy, intValue2);
                float floatValue20 = floatValueItem + activeColorSetting.getAlpha() * floatValue17;
                Renderer2D.drawRoundedRect(matrices, floatValue20 - 2.5F, floatValue16 - 0.5F, 5.0F, 7.0F, 2.0F, ColorMathUtil.rgba(0, 0, 0, MathHelper.clamp((int) (180.0F * floatValueResult), 1, 255)));
                Renderer2D.drawRoundedRect(matrices, floatValue20 - 1.5F, floatValue16 + 0.5F, 3.0F, 5.0F, 1.0F, ColorMathUtil.rgba(255, 255, 255, clampedValue));
                matrices.pop();
            }
        }
    }
}
