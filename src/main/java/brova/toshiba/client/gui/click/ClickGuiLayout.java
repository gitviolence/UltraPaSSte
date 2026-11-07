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
import java.util.List;

public final class ClickGuiLayout {

    public static final float WIDTH = 500.0F;

    public static final float HEIGHT = 350.0F;

    public static final float TOP_PANEL_HEIGHT = 350.0F;

    public static final float PANEL_PADDING = 10.0F;

    public static final float MODULES_X = 140.0F;

    public static final float MODULES_Y = 50.0F;

    public static final float MODULES_WIDTH = 347.0F;

    public static final float MODULES_HEIGHT = 290.0F;

    public static final float CONFIG_CONTROL_WIDTH = 158.0F;

    public static final float CONFIG_CONTROL_HEIGHT = 14.5F;

    public static final float CONFIG_INPUT_Y = 134.0F;

    public static final float CONFIG_BUTTON_Y = 153.0F;

    public static final float BOTTOM_BAR_HEIGHT = 30.0F;

    public static final float BOTTOM_BAR_GAP = 6.0F;

    public static final float BOTTOM_BAR_PADDING = 8.0F;

    public static final float BOTTOM_SEARCH_WIDTH = 145.0F;

    public static final float BOTTOM_SEARCH_HEIGHT = 20.0F;

    public static final float BOTTOM_SELECTED_PADDING = 8.0F;

    public static final float BOTTOM_SELECTED_GAP = 6.0F;

    public static final float MODULE_CARD_WIDTH = 172.0F;

    public static final float MODULE_CARD_GAP = 3.0F;

    public static final float MODULE_CARD_SPACING_Y = 6.0F;

    public static final float CATEGORY_BUTTON_SIZE = 22.0F;

    public static final float CATEGORY_BUTTON_SPACING = 7.0F;

    public static final float CATEGORY_BAR_PADDING = 8.0F;

    public static final float PROFILE_X = 9.0F;

    public static final float PROFILE_Y = 309.0F;

    public static final float PROFILE_WIDTH = 122.0F;

    public static final float PROFILE_HEIGHT = 32.0F;

    public static final float PROFILE_RADIUS = 6.0F;

    public static final float PROFILE_IMAGE_X = 15.0F;

    public static final float PROFILE_IMAGE_Y = 315.0F;

    public static final float PROFILE_IMAGE_SIZE = 20.0F;

    public static final float PROFILE_IMAGE_RADIUS = 6.0F;

    public static final float PROFILE_IMAGE_BORDER = 1.0F;

    public static final float PROFILE_TEXT_X = 41.0F;

    public static final float PROFILE_USERNAME_Y = 319.5F;

    public static final float PROFILE_SUBSCRIPTION_Y = 327.0F;

    public static final float PROFILE_TEXT_RIGHT_PADDING = 6.0F;

    public static final int PROFILE_USERNAME_FONT_SIZE = 14;

    public static final int PROFILE_SUBSCRIPTION_FONT_SIZE = 10;

    public static final float PROFILE_PANEL_WIDTH = 123.0F;

    public static final float PROFILE_PANEL_RADIUS = 12.0F;

    public static final float PROFILE_PANEL_GAP = 14.0F;

    public static final float PROFILE_PANEL_SETTING_PADDING = 7.0F;

    public static final float PROFILE_PANEL_SETTING_TOP_PADDING = 10.0F;

    public static final float PROFILE_PANEL_SETTING_BOTTOM_PADDING = 3.5F;

    public static final float THEME_PANEL_Y = 100.0F;

    public static final float THEME_PANEL_H = 15.0F;

    public static final float THEME_BOX_SIZE = 8.0F;

    public static final float THEME_BOX_GAP = 4.0F;

    public static final float THEME_BOX_RADIUS = 2.0F;

    public static final float THEME_SIDE_PADDING = 4.0F;

    public static final float MODULE_PADDING = 0.0F;

    public static final float MODULE_GAP = 6.0F;

    public static final float MODULE_HEADER_HEIGHT = 30.0F;

    public static final float MODULE_INNER_WIDTH = 172.0F;

    public static final float SETTING_START_Y = 30.0F;

    public static final float SETTING_PADDING = 4.0F;

    public static final float SETTING_BOTTOM_PADDING = 3.0F;

    public static final float SETTING_LEFT = 10.0F;

    public static final float SETTING_RIGHT = 168.0F;

    public static float SLIDER_WIDTH = 152.0F;

    public static final float TEXT_SETTING_WIDTH = 70.0F;

    public static final float CLICKABLE_WIDTH = 158.0F;

    public static final int SEARCH_MAX_CHARS = 24;

    public static final float SEARCH_WIDTH = 80.0F;

    public static final float SEARCH_HEIGHT = 23.0F;

    public static final float SEARCH_GAP = 8.0F;

    public static final float SEARCH_ICON_X = 3.5F;

    public static final float SEARCH_TEXT_X = 7.0F;

    public static final float SEARCH_RIGHT_PADDING = 7.0F;

    public static float getSliderWidth() {
        return SLIDER_WIDTH;
    }

    private ClickGuiLayout() {
    }

    public static float getProfilePanelX(float floatValue) {
        return floatValue - 14.0F - 123.0F;
    }

    public static float getProfilePanelY(float floatValue, float floatValueValue) {
        return floatValue + 350.0F - floatValueValue;
    }

    public static float getProfileSettingHeight(SettingBase setting) {
        if (setting instanceof ModeSetting settingValue) {
            return calculateModeSettingHeight(settingValue);
        } else if (setting instanceof BooleanSetting) {
            return 16.0F;
        } else {
            return setting instanceof FloatSetting ? 24.0F : 0.0F;
        }
    }

    public static float getTotalCategoriesWidth(int intValue) {
        return 500.0F;
    }

    public static float getCategoryPanelX(float floatValue, int intValue) {
        return floatValue;
    }

    public static float getContentY(float floatValue) {
        return floatValue + 50.0F;
    }

    public static float getContentHeight() {
        return 290.0F;
    }

    public static float getSearchX(float floatValue) {
        return floatValue + 500.0F - 80.0F - 13.5F;
    }

    public static float getSearchX(float floatValue, int intValue) {
        return getSearchX(floatValue);
    }

    public static float getSearchX(float floatValue, int intValue, float floatValueValue) {
        return floatValue + 500.0F - floatValueValue - 13.5F;
    }

    public static float getSearchY(float floatValue) {
        return floatValue + 13.5F;
    }

    public static boolean hasVisibleSettings(List<SettingBase> settings) {
        for (SettingBase setting : settings) {
            if (setting != null && brova.toshiba.client.utils.runtime.AccessGuard.invoke(setting, new Object[] {}, (accessReceiver, accessArguments) -> ((SettingBase) accessReceiver).visible())) {
                return true;
            }
        }
        return false;
    }

    public static float calculateModeSettingHeight(ModeSetting setting) {
        int intValue = setting.getMods() == null ? 0 : ((List) brova.toshiba.client.utils.runtime.AccessGuard.invoke(setting, new Object[] {}, (accessReceiver, accessArguments) -> ((ModeSetting) accessReceiver).getMods())).size();
        int intValueValue = Math.max(1, (int) Math.ceil(intValue / 5.0));
        return 34.0F + (intValueValue - 1) * 14.5F;
    }

    public static float calculateListSettingHeight(MultiSetting multiSetting) {
        int intValue = 0;
        for (BooleanSetting setting : multiSetting.getSettings()) {
            if (setting.visible()) {
                intValue++;
            }
        }
        int intValueValue = Math.max(1, (int) Math.ceil(intValue / 5.0));
        return 34.0F + (intValueValue - 1) * 14.5F;
    }

    public static float calculateSettingsHeight(Module module) {
        return calculateSettingsHeight(module, null);
    }

    public static float calculateSettingsHeight(Module module, ClickGuiController clickGuiController) {
        float floatValue = 0.0F;
        var settings = module.getSettings();
        if (settings != null && !settings.isEmpty()) {
            boolean flag = false;
            for (SettingBase setting : settings) {
                if (setting != null && brova.toshiba.client.utils.runtime.AccessGuard.invoke(setting, new Object[] {}, (accessReceiver, accessArguments) -> ((SettingBase) accessReceiver).visible())) {
                    flag = true;
                    if (setting instanceof BooleanSetting) {
                        floatValue += 16.0F;
                    } else if (setting instanceof BindSetting) {
                        floatValue += 34.0F;
                    } else if (setting instanceof TextSetting) {
                        floatValue += 34.0F;
                    } else if (setting instanceof FloatSetting) {
                        floatValue += 24.0F;
                    } else if (setting instanceof ModeSetting settingValue) {
                        floatValue += calculateModeSettingHeight(settingValue);
                    } else if (setting instanceof MultiSetting multiSetting) {
                        floatValue += calculateListSettingHeight(multiSetting);
                    } else if (setting instanceof ColorSetting) {
                        floatValue += 16.0F;
                    }
                }
            }
            if (flag) {
                floatValue += 3.0F;
            }
            return floatValue;
        } else {
            return 0.0F;
        }
    }

    public static float getModuleHeight(Module module, float floatValue) {
        return getModuleHeight(module, floatValue, null);
    }

    public static float getModuleHeight(Module module, float floatValue, ClickGuiController clickGuiController) {
        return 30.0F + calculateSettingsHeight(module, clickGuiController) * floatValue;
    }
}
