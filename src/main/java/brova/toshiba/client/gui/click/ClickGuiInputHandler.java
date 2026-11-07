package brova.toshiba.client.gui.click;

import brova.toshiba.Toshiba;
import brova.toshiba.client.configs.CloudConfigSync;
import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.modules.settings.implement.TextSetting;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.input.KeyBindUtil;
import brova.toshiba.client.utils.math.HoverUtil;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.StyledFontRenderer;
import brova.toshiba.client.utils.render.font.SystemFontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import java.util.List;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class ClickGuiInputHandler implements MinecraftAccess {

    private final ClickGuiController clickGuiController;

    private final ThemeSelector handleClickThemeSelector;

    public ClickGuiInputHandler(ClickGuiController clickGuiController, ThemeSelector handleClickThemeSelector) {
        this.clickGuiController = clickGuiController;
        this.handleClickThemeSelector = handleClickThemeSelector;
    }

    public boolean mouseClicked(double doubleValue, double doubleValueValue, int intValue, Window window) {
        ColorSetting activeColorSetting = this.clickGuiController.getActiveColorSetting();
        if (activeColorSetting != null) {
            float activeColorSettingX = this.clickGuiController.getActiveColorSettingX();
            float activeColorSettingY = this.clickGuiController.getActiveColorSettingY();
            float floatValue = 57.5F;
            float floatValueValue = activeColorSettingX + floatValue / 2.0F - 40.0F;
            float floatValueResult = activeColorSettingY + 17.5F;
            float floatValueItem = floatValueValue - 5.0F;
            float floatValueEntry = floatValueResult - 2.0F;
            float floatValueCopy = 90.0F;
            float floatValue2 = 78.0F;
            Animation colorSettingAnimation = this.clickGuiController.getColorSettingAnimation(activeColorSetting);
            float value = colorSettingAnimation.getValue();
            float floatValue3 = 0.85F + 0.15F * value;
            float floatValue4 = floatValueValue + 40.0F;
            float floatValue5 = floatValueResult + 37.0F;
            double doubleValueResult = floatValue4 + (doubleValue - floatValue4) / floatValue3;
            double doubleValueItem = floatValue5 + (doubleValueValue - floatValue5) / floatValue3;
            if (HoverUtil.isHovered(doubleValueResult, doubleValueItem, floatValueItem, floatValueEntry, floatValueCopy, floatValue2)) {
                if (intValue == 0) {
                    float floatValue6 = floatValueResult + 2.0F;
                    float floatValue7 = 80.0F;
                    float floatValue8 = 46.0F;
                    float floatValue9 = floatValue6 + floatValue8 + 4.0F;
                    float floatValue10 = 80.0F;
                    float floatValue11 = 6.0F;
                    float floatValue12 = floatValue9 + floatValue11 + 4.0F;
                    float floatValue13 = 80.0F;
                    float floatValue14 = 6.0F;
                    if (HoverUtil.isHovered(doubleValueResult, doubleValueItem, floatValueValue, floatValue6, floatValue7, floatValue8)) {
                        this.clickGuiController.setDraggingColorSettingSV(activeColorSetting);
                        float floatValue15 = (float) (doubleValueResult - floatValueValue);
                        float floatValue16 = (float) (doubleValueItem - floatValue6);
                        activeColorSetting.setSaturation(MathHelper.clamp(floatValue15 / floatValue7, 0.0F, 1.0F));
                        activeColorSetting.setBrightness(MathHelper.clamp(1.0F - floatValue16 / floatValue8, 0.0F, 1.0F));
                        return true;
                    }
                    if (HoverUtil.isHovered(doubleValueResult, doubleValueItem, floatValueValue, floatValue9, floatValue10, floatValue11)) {
                        this.clickGuiController.setDraggingColorSettingHue(activeColorSetting);
                        float floatValue17 = (float) (doubleValueResult - floatValueValue);
                        activeColorSetting.setHue(MathHelper.clamp(floatValue17 / floatValue10, 0.0F, 1.0F));
                        return true;
                    }
                    if (HoverUtil.isHovered(doubleValueResult, doubleValueItem, floatValueValue, floatValue12, floatValue13, floatValue14)) {
                        this.clickGuiController.setDraggingColorSettingAlpha(activeColorSetting);
                        float floatValue18 = (float) (doubleValueResult - floatValueValue);
                        activeColorSetting.setAlpha(MathHelper.clamp(floatValue18 / floatValue13, 0.0F, 1.0F));
                        return true;
                    }
                }
                return true;
            }
            if (!HoverUtil.isHovered(doubleValue, doubleValueValue, activeColorSettingX, activeColorSettingY, floatValue, 14.5)) {
                this.clickGuiController.closeActiveColorSetting();
            }
        }
        if (this.clickGuiController.getBindingModule() != null && intValue >= 2) {
            this.clickGuiController.getBindingModule().setKey(KeyBindUtil.createMouseBind(intValue));
            this.clickGuiController.setBindingModule(null);
            return true;
        } else if (this.clickGuiController.getBindingSetting() != null && intValue >= 2) {
            this.clickGuiController.getBindingSetting().setKey(KeyBindUtil.createMouseBind(intValue));
            this.clickGuiController.setBindingSetting(null);
            return true;
        } else {
            this.clickGuiController.setEditingTextSetting(null);
            float searchX = ClickGuiLayout.getSearchX(this.clickGuiController.getX());
            float searchY = ClickGuiLayout.getSearchY(this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY());
            if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, searchX, searchY, 80.0, 23.0)) {
                this.clickGuiController.setSearchActive(true);
                this.clickGuiController.setConfigNameActive(false);
                this.clickGuiController.setBindingModule(null);
                this.clickGuiController.setBindingSetting(null);
                this.clickGuiController.startSearchSelection(this.calculateWidthWithSubstring(doubleValue, searchX));
                return true;
            } else {
                this.clickGuiController.setSearchActive(false);
                this.clickGuiController.stopSearchSelection();
                if (this.isHoveredWithSelectedCategory(doubleValue, doubleValueValue, intValue)) {
                    return true;
                } else {
                    if (intValue == 0 && this.clickGuiController.getSelectedCategory() == ModuleCategory.CONFIG && this.clickGuiController.getContentCategory() == ModuleCategory.CONFIG && !this.clickGuiController.hasSearchQuery()) {
                        float floatValue19 = this.clickGuiController.getX() + 140.0F + 94.5F;
                        float contentY = ClickGuiLayout.getContentY(this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY());
                        if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue19, contentY + 134.0F, 158.0, 14.5)) {
                            this.clickGuiController.setConfigNameActive(true);
                            return true;
                        }
                        this.clickGuiController.setConfigNameActive(false);
                        if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue19, contentY + 153.0F, 158.0, 14.5)) {
                            CloudConfigSync cloudConfigSync = Toshiba.INSTANCE.getCloudConfigSync();
                            if (cloudConfigSync != null) {
                                cloudConfigSync.saveCurrent(this.clickGuiController.getConfigName());
                            }
                            return true;
                        }
                    }
                    if (this.handleClickThemeSelector.handleClick(window, doubleValue, doubleValueValue, intValue, this.clickGuiController.getRenderOffsetY())) {
                        return true;
                    } else {
                        ModuleCategory selectedCategory = this.clickGuiController.getSelectedCategory();
                        float x = this.clickGuiController.getX();
                        float contentYValue = ClickGuiLayout.getContentY(this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY());
                        float contentHeight = ClickGuiLayout.getContentHeight();
                        if (!HoverUtil.isHovered(doubleValue, doubleValueValue, x + 140.0F, contentYValue, 347.0, contentHeight)) {
                            return false;
                        } else if (this.clickGuiController.getContentCategory() != selectedCategory) {
                            return true;
                        } else {
                            float scroll = this.clickGuiController.getScroll(selectedCategory);
                            float[] values = new float[] { contentYValue + scroll, contentYValue + scroll };
                            for (Module module : this.clickGuiController.getModules(selectedCategory)) {
                                float moduleOpenAnimation = this.clickGuiController.getModuleOpenAnimation(module);
                                float moduleHeight = ClickGuiLayout.getModuleHeight(module, moduleOpenAnimation, this.clickGuiController);
                                int moduleColumn = this.clickGuiController.getModuleColumn(module);
                                float floatValue20 = x + 140.0F + moduleColumn * 175.0F;
                                float floatValue21 = values[moduleColumn];
                                if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue20, floatValue21, 172.0, 30.0)) {
                                    if (intValue == 1) {
                                        var settings = module.getSettings();
                                        if (settings != null && !settings.isEmpty()) {
                                            module.setOpen(!module.isOpen());
                                        }
                                        return true;
                                    }
                                    if (intValue != 0) {
                                        if (intValue == 2) {
                                            this.clickGuiController.setBindingModule(module);
                                            return true;
                                        }
                                        return true;
                                    }
                                    // Левая кнопка мыши включает/выключает модуль в любом месте его карточки,
                                    // а не только по переключателю справа.
                                    module.toggle();
                                    return true;
                                }
                                if (moduleOpenAnimation > 0.1F) {
                                    var settingsValue = module.getSettings();
                                    if (settingsValue != null && this.isHoveredWithVisible(doubleValue, doubleValueValue, intValue, floatValue20, floatValue21, settingsValue)) {
                                        return true;
                                    }
                                }
                                values[moduleColumn] += 6.0F + moduleHeight;
                            }
                            return false;
                        }
                    }
                }
            }
        }
    }

    public boolean mouseReleased(int intValue) {
        this.clickGuiController.stopSearchSelection();
        if (intValue == 0) {
            this.clickGuiController.setDraggingColorSettingSV(null);
            this.clickGuiController.setDraggingColorSettingHue(null);
            this.clickGuiController.setDraggingColorSettingAlpha(null);
            for (Module module : this.clickGuiController.getAllModules()) {
                var values = brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getSettings());
                if (values != null) {
                    for (SettingBase setting : values) {
                        if (setting instanceof FloatSetting settingValue) {
                            settingValue.setActive(false);
                            this.clickGuiController.endSliderDrag(settingValue);
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean mouseDragged(double doubleValue, double doubleValueValue, int intValue) {
        if (intValue == 0 && this.clickGuiController.isSearchDragging()) {
            float searchX = ClickGuiLayout.getSearchX(this.clickGuiController.getX());
            ClickGuiController clickGuiController = this.clickGuiController;
            int intValueValue = this.calculateWidthWithSubstring(doubleValue, searchX);
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(clickGuiController, new Object[] { intValueValue }, (accessReceiver, accessArguments) -> {
                ((ClickGuiController) accessReceiver).updateSearchSelection(((int) accessArguments[0]));
            });
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double doubleValue, double doubleValueValue, double doubleValueResult) {
        ModuleCategory selectedCategory = this.clickGuiController.getSelectedCategory();
        float contentY = ClickGuiLayout.getContentY(this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY());
        float contentHeight = ClickGuiLayout.getContentHeight();
        if (HoverUtil.isHovered(doubleValue, doubleValueValue, this.clickGuiController.getX() + 140.0F, contentY, 347.0, contentHeight)) {
            this.clickGuiController.addScroll(selectedCategory, doubleValueResult, contentHeight);
            return true;
        } else {
            return false;
        }
    }

    public boolean keyPressed(int intValue, int intValueValue) {
        if (this.clickGuiController.isSearchActive()) {
            boolean flag = (intValueValue & 2) != 0;
            boolean flagValue = (intValueValue & 1) != 0;
            if (flag) {
                switch(intValue) {
                    case 65:
                        this.clickGuiController.selectAllSearchText();
                        break;
                    case 67:
                        if (this.clickGuiController.hasSearchSelection()) {
                            mc.keyboard.setClipboard(this.clickGuiController.getSelectedSearchText());
                        }
                        break;
                    case 86:
                        this.clickGuiController.replaceSearchSelection(mc.keyboard.getClipboard());
                        break;
                    case 88:
                        if (this.clickGuiController.hasSearchSelection()) {
                            mc.keyboard.setClipboard(this.clickGuiController.getSelectedSearchText());
                            this.clickGuiController.replaceSearchSelection("");
                        }
                        break;
                    case 90:
                        this.clickGuiController.restoreSearchUndo();
                }
            }
            switch(intValue) {
                case 256:
                case 257:
                case 335:
                    this.clickGuiController.setSearchActive(false);
                    this.clickGuiController.stopSearchSelection();
                    break;
                case 259:
                    this.clickGuiController.removeLastSearchChar();
                    break;
                case 261:
                    this.clickGuiController.deleteSearchChar();
                    break;
                case 262:
                    this.clickGuiController.setSearchCursor(!flagValue && this.clickGuiController.hasSearchSelection() ? this.clickGuiController.getSearchSelectionEnd() : this.clickGuiController.getSearchCursor() + 1, flagValue);
                    break;
                case 263:
                    this.clickGuiController.setSearchCursor(!flagValue && this.clickGuiController.hasSearchSelection() ? this.clickGuiController.getSearchSelectionStart() : this.clickGuiController.getSearchCursor() - 1, flagValue);
                    break;
                case 268:
                    this.clickGuiController.setSearchCursor(0, flagValue);
                    break;
                case 269:
                    this.clickGuiController.setSearchCursor(this.clickGuiController.getSearchText().length(), flagValue);
            }
            return true;
        } else if (this.clickGuiController.getEditingTextSetting() != null) {
            TextSetting editingTextSetting = this.clickGuiController.getEditingTextSetting();
            if (intValue == 256 || intValue == 257 || intValue == 335) {
                this.clickGuiController.setEditingTextSetting(null);
                return true;
            } else if (intValue == 259) {
                String text = editingTextSetting.get();
                if (text != null && !text.isEmpty()) {
                    editingTextSetting.setText(text.substring(0, text.length() - 1));
                }
                return true;
            } else {
                return true;
            }
        } else if (this.clickGuiController.isConfigNameActive()) {
            if (intValue == 256 || intValue == 257 || intValue == 335) {
                this.clickGuiController.setConfigNameActive(false);
                return true;
            } else if (intValue == 259) {
                this.clickGuiController.backspaceConfigName();
                return true;
            } else {
                return true;
            }
        } else if (this.clickGuiController.getBindingModule() != null) {
            if (intValue == 256) {
                this.clickGuiController.setBindingModule(null);
            } else if (intValue != 261 && intValue != 259) {
                this.clickGuiController.getBindingModule().setKey(intValue);
                this.clickGuiController.setBindingModule(null);
            } else {
                this.clickGuiController.getBindingModule().setKey(-1);
                this.clickGuiController.setBindingModule(null);
            }
            return true;
        } else if (this.clickGuiController.getBindingSetting() == null) {
            return false;
        } else {
            if (intValue == 256) {
                this.clickGuiController.setBindingSetting(null);
            } else if (intValue != 261 && intValue != 259) {
                this.clickGuiController.getBindingSetting().setKey(intValue);
                this.clickGuiController.setBindingSetting(null);
            } else {
                this.clickGuiController.getBindingSetting().setKey(-1);
                this.clickGuiController.setBindingSetting(null);
            }
            return true;
        }
    }

    public boolean charTyped(char character) {
        if (this.clickGuiController.isSearchActive()) {
            ClickGuiController clickGuiController = this.clickGuiController;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(clickGuiController, new Object[] { character }, (accessReceiver, accessArguments) -> {
                ((ClickGuiController) accessReceiver).appendSearchChar(((char) accessArguments[0]));
            });
            return true;
        } else if (this.clickGuiController.getEditingTextSetting() != null) {
            if (!Character.isISOControl(character)) {
                TextSetting textSetting = brova.toshiba.client.utils.runtime.AccessGuard.invoke(this.clickGuiController, new Object[] {}, (accessReceiver, accessArguments) -> ((ClickGuiController) accessReceiver).getEditingTextSetting());
                textSetting.setText(textSetting.get() + character);
            }
            return true;
        } else if (!this.clickGuiController.isConfigNameActive()) {
            return false;
        } else {
            this.clickGuiController.appendConfigName(character);
            return true;
        }
    }

    private int calculateWidthWithSubstring(double doubleValue, float floatValue) {
        String text = brova.toshiba.client.utils.runtime.AccessGuard.invoke(this.clickGuiController, new Object[] {}, (accessReceiver, accessArguments) -> ((ClickGuiController) accessReceiver).getSearchText());
        float floatValueValue = floatValue + 7.0F;
        float floatValueResult = (float) doubleValue - floatValueValue + this.clickGuiController.getSearchTextOffset();
        if (!(floatValueResult <= 0.0F) && !text.isEmpty()) {
            for (int index = 1; index <= text.length(); index++) {
                float width = FontManager.getFont("suisse", 14).getWidth(text.substring(0, index - 1));
                float widthValue = FontManager.getFont("suisse", 14).getWidth(text.substring(0, index));
                float floatValueItem = width + (widthValue - width) * 0.5F;
                if (floatValueResult < floatValueItem) {
                    return index - 1;
                }
            }
            return text.length();
        } else {
            return 0;
        }
    }

    private boolean isHoveredWithVisible(double doubleValue, double doubleValueValue, int intValue, float floatValue, float floatValueValue, List<SettingBase> settings) {
        float floatValueResult = 30.0F;
        for (SettingBase setting : settings) {
            if (setting != null && setting.visible()) {
                float floatValueItem = floatValueValue + floatValueResult + 4.0F;
                if (setting instanceof BooleanSetting settingItem) {
                    float floatValueEntry = floatValue + 172.0F - 47.0F;
                    float floatValueCopy = floatValueItem + 1.0F;
                    if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValueEntry + 18.0F, floatValueCopy - 3.0F, 22.0, 12.0)) {
                        settingItem.setState(!settingItem.isState());
                        return true;
                    }
                    floatValueResult += 16.0F;
                } else if (setting instanceof TextSetting textSetting) {
                    float floatValue2 = floatValue + 7.0F;
                    float floatValue3 = floatValueItem + 13.0F;
                    float floatValue4 = 158.0F;
                    if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue2, floatValue3, floatValue4, 14.5)) {
                        this.clickGuiController.setSearchActive(false);
                        this.clickGuiController.stopSearchSelection();
                        this.clickGuiController.setEditingTextSetting(textSetting);
                        return true;
                    }
                    floatValueResult += 34.0F;
                } else if (setting instanceof FloatSetting settingEntry) {
                    float floatValue5 = floatValue + 10.0F;
                    if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue5, floatValueItem + 11.0F, ClickGuiLayout.getSliderWidth(), 8.0)) {
                        settingEntry.setActive(true);
                        settingEntry.setValue(this.clickGuiController.getSliderValue(settingEntry, floatValue5, doubleValue));
                        this.clickGuiController.beginSliderDrag(settingEntry, doubleValue);
                        return true;
                    }
                    floatValueResult += 24.0F;
                } else if (setting instanceof ModeSetting settingCopy) {
                    var mods = settingCopy.getMods();
                    if (mods.isEmpty()) {
                        floatValueResult += ClickGuiLayout.calculateModeSettingHeight(settingCopy);
                    } else {
                        float floatValue6 = floatValue + 7.0F;
                        float floatValue7 = 158.0F;
                        float floatValue8 = floatValueItem + 13.0F;
                        for (int index = 0; index < mods.size(); index++) {
                            int intValueValue = index / 5;
                            int intValueResult = index % 5;
                            float floatValue9 = floatValue7 / 5.0F;
                            float floatValue10 = floatValue8 + intValueValue * 14.5F;
                            if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue6 + floatValue9 * intValueResult, floatValue10, floatValue9, 15.5)) {
                                settingCopy.set(mods.get(index));
                                return true;
                            }
                        }
                        floatValueResult += ClickGuiLayout.calculateModeSettingHeight(settingCopy);
                    }
                } else if (!(setting instanceof MultiSetting multiSetting)) {
                    if (setting instanceof BindSetting bindSetting) {
                        if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue + 7.0F, floatValueItem + 13.0F, 158.0, 14.5)) {
                            this.clickGuiController.setBindingSetting(bindSetting);
                            return true;
                        }
                        floatValueResult += 34.0F;
                    } else if (setting instanceof ColorSetting colorSetting) {
                        float floatValue11 = 57.5F;
                        float floatValue12 = floatValue + 172.0F - 7.0F - floatValue11;
                        float floatValue13 = floatValueItem - 3.5F;
                        float floatValue14 = 14.5F;
                        if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue12, floatValue13, floatValue11, floatValue14)) {
                            this.clickGuiController.toggleColorSetting(colorSetting);
                            return true;
                        }
                        floatValueResult += 16.0F;
                    }
                } else {
                    int intValueItem = 0;
                    for (BooleanSetting settingValue : multiSetting.getSettings()) {
                        if (settingValue.visible()) {
                            intValueItem++;
                        }
                    }
                    if (intValueItem > 0) {
                        float floatValue15 = floatValue + 7.0F;
                        float floatValue16 = 158.0F;
                        float floatValue17 = floatValue16 / 5.0F;
                        int intValueEntry = 0;
                        for (BooleanSetting settingResult : multiSetting.getSettings()) {
                            if (settingResult.visible()) {
                                int intValueCopy = intValueEntry / 5;
                                int intValue2 = intValueEntry % 5;
                                if (intValue == 0 && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue15 + floatValue17 * intValue2, floatValueItem + 13.0F + intValueCopy * 14.5F, floatValue17, 15.5)) {
                                    settingResult.setState(!settingResult.isState());
                                    return true;
                                }
                                intValueEntry++;
                            }
                        }
                    }
                    floatValueResult += ClickGuiLayout.calculateListSettingHeight(multiSetting);
                }
            }
        }
        return false;
    }

    private boolean isHoveredWithSelectedCategory(double doubleValue, double doubleValueValue, int intValue) {
        if (intValue != 0) {
            return false;
        } else {
            float floatValue = this.clickGuiController.getX() + 10.0F;
            float floatValueValue = this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY();
            if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 63.5F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.COMBAT);
                return true;
            } else if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 90.5F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.MOVEMENT);
                return true;
            } else if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 117.5F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.PLAYER);
                return true;
            } else if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 222.5F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.MISC);
                return true;
            } else if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 249.5F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.GHOST);
                return true;
            } else if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 276.5F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.CONFIG);
                return true;
            } else if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue, floatValueValue + 170.0F, 120.0, 23.0)) {
                this.clickGuiController.setSelectedCategory(ModuleCategory.RENDER);
                return true;
            } else {
                float floatValueResult = this.clickGuiController.getY() + this.clickGuiController.getRenderOffsetY() + 350.0F + 6.0F;
                float floatValueItem = floatValueResult + 4.0F - 2.0F;
                float floatValueEntry = this.clickGuiController.getX() + 8.0F + 22.0F + 15.0F;
                for (ModuleCategory moduleCategory : ModuleCategory.values()) {
                    float categoryAnimation = this.clickGuiController.getCategoryAnimation(moduleCategory);
                    StyledFontRenderer styledFontRenderer = this.createStyledFontRendererFromModuleModuleCategory(moduleCategory, 14);
                    float stringWidth = styledFontRenderer.getStringWidth(moduleCategory.getIcons());
                    float stringWidthValue = SystemFontManager.getFont("Manrope-Bold.ttf", 14.0F).getStringWidth(moduleCategory.getName());
                    float floatValueCopy = 16.0F + stringWidth + 6.0F + stringWidthValue - 2.0F;
                    float interpolatedValue = MathHelper.lerp(categoryAnimation, 22.0F, floatValueCopy);
                    if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValueEntry + 5.0F, floatValueItem + 3.0F, interpolatedValue + 3.0F, 25.0)) {
                        this.clickGuiController.setSelectedCategory(moduleCategory);
                        return true;
                    }
                    floatValueEntry += interpolatedValue + 7.0F;
                }
                return false;
            }
        }
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

    private int calculateFloat(float[] values) {
        int intValue = 0;
        for (int index = 1; index < values.length; index++) {
            if (values[index] < values[intValue]) {
                intValue = index;
            }
        }
        return intValue;
    }

    private TextRenderer getBodyFont(int intValue) {
        return FontManager.getFont("manrope", intValue);
    }

    private TextRenderer getIconFont(int intValue) {
        return FontManager.getFont("icon", intValue);
    }
}
