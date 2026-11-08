package brova.toshiba.client.gui.click;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.modules.settings.implement.TextSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.util.Window;

public final class ClickGuiController {

    private static final Map<Character, Character> keyboardLayoutMap = new HashMap<>();

    private final Map<BooleanSetting, Animation> booleanToggleAnimations = new HashMap<>();

    private final Map<BooleanSetting, Animation> booleanHoverAnimations = new HashMap<>();

    private final Map<FloatSetting, Animation> sliderAnimations = new HashMap<>();

    private final Map<FloatSetting, Double> sliderDragValues = new HashMap<>();

    private final Map<FloatSetting, Double> sliderDisplayValues = new HashMap<>();

    private final Map<String, Animation> optionHoverAnimations = new HashMap<>();

    private final Map<ModeSetting, Animation> modeExpandAnimations = new IdentityHashMap<>();

    private final Map<String, Animation> modeHoverAnimations = new HashMap<>();

    private final Map<String, Animation> settingVisibilityAnimations = new HashMap<>();

    private final Map<ColorSetting, Boolean> colorPickerExpanded = new HashMap<>();

    private final Map<ColorSetting, Animation> colorPickerAnimations = new HashMap<>();

    private final Map<ModuleCategory, Animation> categoryAnimations = new EnumMap<>(ModuleCategory.class);

    private final Map<Module, Animation> moduleToggleAnimations = new IdentityHashMap<>();

    private final Map<Module, Animation> moduleHoverAnimations = new IdentityHashMap<>();

    private final Map<Module, Animation> moduleExpandAnimations = new IdentityHashMap<>();

    private final Map<String, Animation> sectionAnimations = new HashMap<>();

    private final Map<String, Float> sectionHeights = new HashMap<>();

    private final Map<String, Boolean> sectionExpanded = new HashMap<>();

    private final Map<String, Boolean> sectionHovered = new HashMap<>();

    private ColorSetting draggingColorSettingSV;

    private ColorSetting draggingColorSettingHue;

    private ColorSetting draggingColorSettingAlpha;

    private final Map<ModuleCategory, Float> categoryScrollTargets = new EnumMap<>(ModuleCategory.class);

    private final Map<ModuleCategory, Animation> categoryScrollAnimations = new EnumMap<>(ModuleCategory.class);

    private final Map<ModuleCategory, List<Module>> modulesByCategory = new EnumMap<>(ModuleCategory.class);

    private final List<Module> allModules = new ArrayList<>();

    private List<Module> filteredModules = List.of();

    private String lastSearchQuery;

    private float x;

    private float y;

    private BindSetting bindingSetting;

    private TextSetting editingTextSetting;

    private Module bindingModule;

    private float renderOffsetY;

    private boolean searchActive;

    private String selectedSearchText = "";

    private String previousSearchText = "";

    private int searchCursor = 0;

    private int searchSelectionStart = 0;

    private int searchSelectionEnd = 0;

    private boolean searchDragging;

    private float searchTextOffset;

    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;

    private ModuleCategory contentCategory = ModuleCategory.COMBAT;

    private final Animation categoryTransitionAnimation = new Animation(1.0F, 9.0F, PowerEasing.QUAD_OUT);

    private String configName = "";

    private boolean configNameActive;

    private ColorSetting activeColorSetting;

    private float activeColorSettingX;

    private float activeColorSettingY;

    public ClickGuiController() {
        this.refreshModules();
        for (Module module : this.allModules) {
            module.setOpen(false);
        }
    }

    public void refreshModules() {
        this.lastSearchQuery = null;
        this.allModules.clear();
        this.allModules.addAll(ModuleRegistry.INSTANCE.getObject());
        for (ModuleCategory moduleCategory : ModuleCategory.values()) {
            ArrayList<brova.toshiba.client.modules.Module> values = new ArrayList<>();
            for (Module module : this.allModules) {
                if (module.getCategory() == moduleCategory) {
                    values.add(module);
                }
            }
            this.modulesByCategory.put(moduleCategory, List.copyOf(values));
            this.categoryScrollTargets.putIfAbsent(moduleCategory, 0.0F);
            this.getScrollAnimation(moduleCategory);
        }
    }

    public void updatePosition(Window window, int intValue) {
        float totalCategoriesWidth = ClickGuiLayout.getTotalCategoriesWidth(intValue);
        this.x = window.getScaledWidth() / 2.0F - totalCategoriesWidth / 2.0F;
        this.y = window.getScaledHeight() / 2.0F - 175.0F;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getRenderOffsetY() {
        return this.renderOffsetY;
    }

    public void setRenderOffsetY(float renderOffsetY) {
        this.renderOffsetY = renderOffsetY;
    }

    public List<Module> getModules(ModuleCategory moduleCategory) {
        if (this.hasSearchQuery()) {
            if (!this.selectedSearchText.equals(this.lastSearchQuery)) {
                String normalizedText = this.selectedSearchText.strip().toLowerCase(Locale.ROOT);
                var values = this.allModules.stream().filter(input -> input.getName().toLowerCase(Locale.ROOT).contains(normalizedText) || input.getDisplayName().toLowerCase(Locale.ROOT).contains(normalizedText)).toList();
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, values, (accessReceiver, accessValue) -> {
                    accessReceiver.filteredModules = accessValue;
                });
                this.lastSearchQuery = this.selectedSearchText;
            }
            return this.filteredModules;
        } else {
            return this.modulesByCategory.getOrDefault(moduleCategory, List.of());
        }
    }

    public boolean hasSearchQuery() {
        return !this.selectedSearchText.isBlank();
    }

    public ModuleCategory getSelectedCategory() {
        return this.selectedCategory;
    }

    public void setSelectedCategory(ModuleCategory selectedCategory) {
        if (selectedCategory != null) {
            if (this.selectedCategory == selectedCategory) {
                this.resetCategoryScroll(selectedCategory);
            }
            if (!this.selectedSearchText.isEmpty()) {
                this.clearSearchText();
            }
            this.searchActive = false;
            this.stopSearchSelection();
            this.selectedCategory = selectedCategory;
            this.configNameActive = false;
        }
    }

    public ModuleCategory getContentCategory() {
        return this.contentCategory;
    }

    public float updateContentTransition() {
        this.categoryTransitionAnimation.update(this.contentCategory == this.selectedCategory ? 1.0F : 0.0F);
        if (this.contentCategory != this.selectedCategory && this.categoryTransitionAnimation.getValue() <= 0.02F) {
            this.contentCategory = this.selectedCategory;
            float floatValue = 0.0F;
            Animation categoryTransitionAnimation = this.categoryTransitionAnimation;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(categoryTransitionAnimation, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((Animation) accessReceiver).setValue(((float) accessArguments[0]));
            });
            this.categoryTransitionAnimation.update(1.0F);
        }
        return this.categoryTransitionAnimation.getValue();
    }

    public String getConfigName() {
        return this.configName;
    }

    public boolean isConfigNameActive() {
        return this.configNameActive;
    }

    public void setConfigNameActive(boolean configNameActive) {
        this.configNameActive = configNameActive;
    }

    public void appendConfigName(char character) {
        if (this.configName.length() < 64 && (Character.isLetterOrDigit(character) || character == '_' || character == '-')) {
            String text = this.configName + character;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                accessReceiver.configName = accessValue;
            });
        }
    }

    public void backspaceConfigName() {
        if (!this.configName.isEmpty()) {
            String text = this.configName.substring(0, this.configName.length() - 1);
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                accessReceiver.configName = accessValue;
            });
        }
    }

    public float getCategoryAnimation(ModuleCategory moduleCategory) {
        Animation animation = this.categoryAnimations.computeIfAbsent(moduleCategory, input -> new Animation(moduleCategory == this.selectedCategory ? 1.0F : 0.0F, 8.0F, PowerEasing.QUAD_OUT));
        animation.update(moduleCategory == this.selectedCategory ? 1.0F : 0.0F);
        return animation.getValue();
    }

    public float getModuleHoverAnimation(Module module, boolean flag) {
        Animation animation = this.moduleToggleAnimations.computeIfAbsent(module, input -> new Animation(0.0F, 12.0F, PowerEasing.CUBIC_OUT));
        animation.update(flag ? 1.0F : 0.0F);
        return animation.getValue();
    }

    public int getModuleColumn(Module module) {
        var modules = this.getModules(brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getCategory()));
        int index = modules.indexOf(module);
        return Math.max(index, 0) % 2;
    }

    public float getModuleToggleAnimation(Module module) {
        Animation animation = this.moduleHoverAnimations.computeIfAbsent(module, input -> new Animation(module.isEnable() ? 1.0F : 0.0F, 12.0F, PowerEasing.CUBIC_OUT));
        animation.update(module.isEnable() ? 1.0F : 0.0F);
        return animation.getValue();
    }

    public float getModuleOpenAnimation(Module module) {
        Animation animation = this.moduleExpandAnimations.computeIfAbsent(module, input -> new Animation(module.isOpen() ? 1.0F : 0.0F, 4.0F, PowerEasing.CUBIC_OUT));
        float floatValue = module.isOpen() ? 1.0F : 0.0F;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(animation, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
            ((Animation) accessReceiver).update(((float) accessArguments[0]));
        });
        return animation.getValue();
    }

    public List<Module> getAllModules() {
        return this.allModules;
    }

    public String toEnglish(String text) {
        StringBuilder textBuilder = new StringBuilder();
        for (char character : text.toCharArray()) {
            textBuilder.append(keyboardLayoutMap.getOrDefault(character, character));
        }
        return textBuilder.toString();
    }

    public float getSliderPos(FloatSetting setting) {
        float floatValue = setting.getMax() - setting.getMin();
        return (setting.get() - setting.getMin()) / floatValue;
    }

    public float getSliderValue(FloatSetting setting, float floatValue, double doubleValue) {
        float floatValueValue = setting.getMax() - setting.getMin();
        float floatValueResult = (float) doubleValue - floatValue;
        float floatValueItem = Math.max(0.0F, Math.min(1.0F, floatValueResult / ClickGuiLayout.getSliderWidth()));
        float floatValueEntry = setting.getMin() + floatValueValue * floatValueItem;
        float increment = setting.getIncrement();
        floatValueEntry = Math.round(floatValueEntry / increment) * increment;
        return Math.max(setting.getMin(), Math.min(setting.getMax(), floatValueEntry));
    }

    public void beginSliderDrag(FloatSetting setting, double doubleValue) {
        this.sliderDragValues.put(setting, doubleValue);
        this.sliderDisplayValues.put(setting, 0.0);
    }

    public void endSliderDrag(FloatSetting setting) {
        this.sliderDragValues.remove(setting);
        this.sliderDisplayValues.remove(setting);
    }

    public float updateActiveSliderValue(FloatSetting setting, double doubleValue) {
        double orDefault = this.sliderDragValues.getOrDefault(setting, doubleValue);
        this.sliderDragValues.put(setting, doubleValue);
        double doubleValueValue = doubleValue - orDefault;
        if (Math.abs(doubleValueValue) < 1.0E-4) {
            return setting.get();
        } else {
            float floatValue = setting.getMax() - setting.getMin();
            float increment = setting.getIncrement();
            if (!(floatValue <= 0.0F) && !(increment <= 0.0F)) {
                double doubleValueResult = floatValue / increment;
                if (doubleValueResult <= 0.0) {
                    return setting.get();
                } else {
                    double doubleValueItem = ClickGuiLayout.getSliderWidth() / doubleValueResult;
                    if (doubleValueItem <= 0.0) {
                        return setting.get();
                    } else {
                        double doubleValueEntry = this.sliderDisplayValues.getOrDefault(setting, 0.0) + doubleValueValue;
                        int intValue = (int) (doubleValueEntry / doubleValueItem);
                        if (intValue == 0) {
                            this.sliderDisplayValues.put(setting, doubleValueEntry);
                            return setting.get();
                        } else {
                            this.sliderDisplayValues.put(setting, doubleValueEntry - intValue * doubleValueItem);
                            float floatValueValue = setting.get() + intValue * increment;
                            floatValueValue = Math.round(floatValueValue / increment) * increment;
                            return Math.max(setting.getMin(), Math.min(setting.getMax(), floatValueValue));
                        }
                    }
                }
            } else {
                return setting.get();
            }
        }
    }

    public float getScroll(ModuleCategory moduleCategory) {
        Animation scrollAnimation = this.getScrollAnimation(moduleCategory);
        scrollAnimation.update(this.categoryScrollTargets.getOrDefault(moduleCategory, 0.0F));
        return scrollAnimation.getValue();
    }

    private Animation getScrollAnimation(ModuleCategory moduleCategory) {
        return this.categoryScrollAnimations.computeIfAbsent(moduleCategory, input -> new Animation(0.0F, 8.0F, PowerEasing.CUBIC_OUT));
    }

    private void resetCategoryScroll(ModuleCategory moduleCategory) {
        Animation scrollAnimation = this.getScrollAnimation(moduleCategory);
        scrollAnimation.setValue(scrollAnimation.getValue());
        scrollAnimation.setSpeed(14.0F);
        this.categoryScrollTargets.put(moduleCategory, 0.0F);
    }

    public void clampScroll(ModuleCategory moduleCategory, float floatValue) {
        float totalModulesHeight = this.getTotalModulesHeight(moduleCategory);
        float floatValueValue = Math.min(0.0F, floatValue - totalModulesHeight);
        float orDefault = this.categoryScrollTargets.getOrDefault(moduleCategory, 0.0F);
        if (orDefault < floatValueValue || orDefault > 0.0F) {
            this.categoryScrollTargets.put(moduleCategory, Math.max(floatValueValue, Math.min(0.0F, orDefault)));
        }
    }

    public void addScroll(ModuleCategory moduleCategory, double doubleValue, float floatValue) {
        float totalModulesHeight = this.getTotalModulesHeight(moduleCategory);
        float floatValueValue = Math.min(0.0F, floatValue - totalModulesHeight);
        float orDefault = this.categoryScrollTargets.getOrDefault(moduleCategory, 0.0F);
        float floatValueResult = Math.max(floatValueValue, Math.min(0.0F, orDefault + (float) (doubleValue * 20.0)));
        if (floatValueResult != orDefault) {
            this.getScrollAnimation(moduleCategory).setSpeed(8.0F);
            this.categoryScrollTargets.put(moduleCategory, floatValueResult);
        }
    }

    public float getTotalModulesHeight(ModuleCategory moduleCategory) {
        float[] values = new float[] { 0.0F, 0.0F };
        for (Module module : this.getModules(moduleCategory)) {
            int moduleColumn = this.getModuleColumn(module);
            values[moduleColumn] += ClickGuiLayout.getModuleHeight(module, this.getModuleOpenAnimation(module), this) + 6.0F;
        }
        return Math.max(values[0], values[1]);
    }

    private int indexOfMinimum(float[] values) {
        int intValue = 0;
        for (int index = 1; index < values.length; index++) {
            if (values[index] < values[intValue]) {
                intValue = index;
            }
        }
        return intValue;
    }

    public Animation getBooleanBackgroundAnimation(BooleanSetting setting) {
        return this.booleanToggleAnimations.computeIfAbsent(setting, input -> new Animation(setting.isState() ? 1.0F : 0.0F, 15.0F, PowerEasing.CUBIC_OUT));
    }

    public Animation getBooleanCircleAnimation(BooleanSetting setting) {
        return this.booleanHoverAnimations.computeIfAbsent(setting, input -> new Animation(setting.isState() ? 1.0F : 0.0F, 6.0F, PowerEasing.SINE_IN_OUT));
    }

    public Animation getSliderAnimation(FloatSetting setting) {
        return this.sliderAnimations.computeIfAbsent(setting, input -> new Animation(this.getSliderPos(setting), 12.0F, PowerEasing.CUBIC_OUT));
    }

    public Animation getModeAnimation(String text, boolean flag) {
        Animation animation = this.optionHoverAnimations.computeIfAbsent(text, input -> new Animation(flag ? 1.0F : 0.0F, 10.0F, PowerEasing.CUBIC_OUT));
        animation.update(flag ? 1.0F : 0.0F);
        return animation;
    }

    public float getModeSelectorAnimation(ModeSetting setting) {
        int intValue = Math.max(0, setting.getMods().indexOf(brova.toshiba.client.utils.runtime.AccessGuard.invoke(setting, new Object[] {}, (accessReceiver, accessArguments) -> ((ModeSetting) accessReceiver).getCurrent())));
        Animation animation = this.modeExpandAnimations.computeIfAbsent(setting, input -> new Animation(intValue, 8.0F, PowerEasing.CUBIC_OUT));
        animation.update(intValue);
        return animation.getValue();
    }

    public Animation getListAnimation(String text, boolean flag) {
        return this.modeHoverAnimations.computeIfAbsent(text, input -> new Animation(flag ? 1.0F : 0.0F, 10.0F, PowerEasing.CUBIC_OUT));
    }

    public Animation getBindAnimation(String text, boolean flag) {
        return this.settingVisibilityAnimations.computeIfAbsent(text, input -> new Animation(flag ? 1.0F : 0.0F, 10.0F, PowerEasing.CUBIC_OUT));
    }

    public Animation getTextHoverAnimation(String text, boolean flag) {
        return this.sectionAnimations.computeIfAbsent(text, input -> new Animation(flag ? 1.0F : 0.0F, 9.0F, PowerEasing.CUBIC_OUT));
    }

    public float advanceTextScrollPhase(String text, boolean flag) {
        float orDefault = this.sectionHeights.getOrDefault(text, 0.0F);
        if (flag) {
            orDefault += 0.004F;
            if (orDefault > 1.0F) {
                orDefault--;
            }
        } else {
            orDefault = 0.0F;
        }
        this.sectionHovered.put(text, flag);
        this.sectionExpanded.put(text, false);
        this.sectionHeights.put(text, orDefault);
        return orDefault;
    }

    public boolean isTextScrollActive(String text, boolean flag) {
        return flag;
    }

    public BindSetting getBindingSetting() {
        return this.bindingSetting;
    }

    public void setBindingSetting(BindSetting bindingSetting) {
        this.bindingSetting = bindingSetting;
    }

    public Module getBindingModule() {
        return this.bindingModule;
    }

    public void setBindingModule(Module bindingModule) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, bindingModule, (accessReceiver, accessValue) -> {
            accessReceiver.bindingModule = accessValue;
        });
    }

    public TextSetting getEditingTextSetting() {
        return this.editingTextSetting;
    }

    public void setEditingTextSetting(TextSetting editingTextSetting) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, editingTextSetting, (accessReceiver, accessValue) -> {
            accessReceiver.editingTextSetting = accessValue;
        });
    }

    public boolean isSearchActive() {
        return this.searchActive;
    }

    public void setSearchActive(boolean searchActive) {
        this.searchActive = searchActive;
    }

    public String getSearchText() {
        return this.selectedSearchText;
    }

    public float getSearchTextOffset() {
        return this.searchTextOffset;
    }

    public void setSearchTextOffset(float searchTextOffset) {
        this.searchTextOffset = Math.max(0.0F, searchTextOffset);
    }

    public void appendSearchChar(char character) {
        character = keyboardLayoutMap.getOrDefault(character, character);
        if (character >= ' ' && character <= '~' && (this.selectedSearchText.length() < 24 || this.hasSearchSelection())) {
            this.replaceSearchSelection(String.valueOf(character));
        }
    }

    public void removeLastSearchChar() {
        if (this.hasSearchSelection()) {
            this.replaceSearchSelection("");
        } else if (this.searchCursor > 0) {
            this.rememberSearchText();
            String text = this.selectedSearchText.substring(0, this.searchCursor - 1) + this.selectedSearchText.substring(this.searchCursor);
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                accessReceiver.selectedSearchText = accessValue;
            });
            this.searchCursor--;
            this.collapseSearchSelection();
        }
    }

    public void clearSearchText() {
        this.rememberSearchText();
        this.selectedSearchText = "";
        this.searchCursor = 0;
        this.searchTextOffset = 0.0F;
        this.collapseSearchSelection();
    }

    public void deleteSearchChar() {
        if (this.hasSearchSelection()) {
            this.replaceSearchSelection("");
        } else if (this.searchCursor < this.selectedSearchText.length()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((ClickGuiController) accessReceiver).rememberSearchText();
            });
            this.selectedSearchText = this.selectedSearchText.substring(0, this.searchCursor) + this.selectedSearchText.substring(this.searchCursor + 1);
            this.collapseSearchSelection();
        }
    }

    public void setSearchText(String searchText) {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((ClickGuiController) accessReceiver).rememberSearchText();
        });
        String text = this.filterSearchInput(searchText);
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
            accessReceiver.selectedSearchText = accessValue;
        });
        this.searchCursor = this.selectedSearchText.length();
        this.collapseSearchSelection();
    }

    public void restoreSearchUndo() {
        String previousSearchText = this.selectedSearchText;
        String text = this.previousSearchText == null ? "" : this.previousSearchText;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
            accessReceiver.selectedSearchText = accessValue;
        });
        this.previousSearchText = previousSearchText;
        this.searchCursor = this.selectedSearchText.length();
        this.collapseSearchSelection();
    }

    public int getSearchCursor() {
        return this.searchCursor;
    }

    public int getSearchSelectionStart() {
        return Math.min(this.searchSelectionStart, this.searchSelectionEnd);
    }

    public int getSearchSelectionEnd() {
        return Math.max(this.searchSelectionStart, this.searchSelectionEnd);
    }

    public boolean hasSearchSelection() {
        return this.getSearchSelectionStart() != this.getSearchSelectionEnd();
    }

    public String getSelectedSearchText() {
        return !this.hasSearchSelection() ? "" : this.selectedSearchText.substring(this.getSearchSelectionStart(), this.getSearchSelectionEnd());
    }

    public void selectAllSearchText() {
        this.searchSelectionStart = 0;
        this.searchSelectionEnd = this.selectedSearchText.length();
        this.searchCursor = this.selectedSearchText.length();
    }

    public void setSearchCursor(int intValue, boolean flag) {
        this.searchCursor = this.clampSearchCursor(intValue);
        if (flag) {
            this.searchSelectionEnd = this.searchCursor;
        } else {
            this.searchSelectionStart = this.searchCursor;
            this.searchSelectionEnd = this.searchCursor;
        }
    }

    public void startSearchSelection(int intValue) {
        this.searchCursor = this.clampSearchCursor(intValue);
        this.searchSelectionStart = this.searchCursor;
        this.searchSelectionEnd = this.searchCursor;
        this.searchDragging = true;
    }

    public void updateSearchSelection(int intValue) {
        if (this.searchDragging) {
            this.searchCursor = this.clampSearchCursor(intValue);
            this.searchSelectionEnd = this.searchCursor;
        }
    }

    public void stopSearchSelection() {
        this.searchDragging = false;
    }

    public boolean isSearchDragging() {
        return this.searchDragging;
    }

    public void replaceSearchSelection(String text) {
        this.rememberSearchText();
        String textValue = this.filterSearchInput(text);
        int searchSelectionStart = this.getSearchSelectionStart();
        int searchSelectionEnd = this.getSearchSelectionEnd();
        if (!this.hasSearchSelection()) {
            searchSelectionStart = this.searchCursor;
            searchSelectionEnd = this.searchCursor;
        }
        int intValue = Math.max(0, 24 - (this.selectedSearchText.length() - (searchSelectionEnd - searchSelectionStart)));
        if (textValue.length() > intValue) {
            textValue = textValue.substring(0, intValue);
        }
        String textResult = this.selectedSearchText.substring(0, searchSelectionStart) + textValue + this.selectedSearchText.substring(searchSelectionEnd);
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, textResult, (accessReceiver, accessValue) -> {
            accessReceiver.selectedSearchText = accessValue;
        });
        this.searchCursor = searchSelectionStart + textValue.length();
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((ClickGuiController) accessReceiver).collapseSearchSelection();
        });
    }

    private void collapseSearchSelection() {
        this.searchSelectionStart = this.searchCursor;
        this.searchSelectionEnd = this.searchCursor;
        this.searchDragging = false;
    }

    private int clampSearchCursor(int intValue) {
        return Math.max(0, Math.min(this.selectedSearchText.length(), intValue));
    }

    private void rememberSearchText() {
        this.previousSearchText = this.selectedSearchText;
    }

    private String filterSearchInput(String text) {
        if (text != null && !text.isEmpty()) {
            StringBuilder textBuilder = new StringBuilder();
            for (int index = 0; index < text.length() && textBuilder.length() < 24; index++) {
                char orDefault = keyboardLayoutMap.getOrDefault(text.charAt(index), text.charAt(index));
                if (orDefault >= ' ' && orDefault <= '~') {
                    textBuilder.append(orDefault);
                }
            }
            return textBuilder.toString();
        } else {
            return "";
        }
    }

    public boolean isColorSettingOpened(ColorSetting colorSetting) {
        return this.colorPickerExpanded.getOrDefault(colorSetting, false);
    }

    public void toggleColorSetting(ColorSetting colorSetting) {
        boolean colorSettingOpened = this.isColorSettingOpened(colorSetting);
        if (!colorSettingOpened) {
            if (this.activeColorSetting != null && this.activeColorSetting != colorSetting) {
                this.colorPickerExpanded.put(this.activeColorSetting, false);
                this.getColorSettingAnimation(this.activeColorSetting).setEasing(PowerEasing.BACK_IN);
            }
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, colorSetting, (accessReceiver, accessValue) -> {
                accessReceiver.activeColorSetting = accessValue;
            });
            this.colorPickerExpanded.put(colorSetting, true);
            this.getColorSettingAnimation(colorSetting).setEasing(PowerEasing.BACK_OUT);
        } else {
            this.colorPickerExpanded.put(colorSetting, false);
            this.getColorSettingAnimation(colorSetting).setEasing(PowerEasing.BACK_IN);
        }
    }

    public Animation getColorSettingAnimation(ColorSetting colorSetting) {
        Animation animation = this.colorPickerAnimations.get(colorSetting);
        if (animation == null) {
            animation = new Animation(0.0F, 3.5F, this.isColorSettingOpened(colorSetting) ? PowerEasing.BACK_OUT : PowerEasing.BACK_IN);
            this.colorPickerAnimations.put(colorSetting, animation);
        }
        return animation;
    }

    public ColorSetting getDraggingColorSettingSV() {
        return this.draggingColorSettingSV;
    }

    public void setDraggingColorSettingSV(ColorSetting draggingColorSettingSV) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, draggingColorSettingSV, (accessReceiver, accessValue) -> {
            accessReceiver.draggingColorSettingSV = accessValue;
        });
    }

    public ColorSetting getDraggingColorSettingHue() {
        return this.draggingColorSettingHue;
    }

    public void setDraggingColorSettingHue(ColorSetting draggingColorSettingHue) {
        this.draggingColorSettingHue = draggingColorSettingHue;
    }

    public ColorSetting getDraggingColorSettingAlpha() {
        return this.draggingColorSettingAlpha;
    }

    public void setDraggingColorSettingAlpha(ColorSetting draggingColorSettingAlpha) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, draggingColorSettingAlpha, (accessReceiver, accessValue) -> {
            accessReceiver.draggingColorSettingAlpha = accessValue;
        });
    }

    public ColorSetting getActiveColorSetting() {
        if (this.activeColorSetting != null) {
            Animation colorSettingAnimation = this.getColorSettingAnimation(this.activeColorSetting);
            if (colorSettingAnimation.getValue() <= 0.01F && !this.isColorSettingOpened(this.activeColorSetting)) {
                this.activeColorSetting = null;
            }
        }
        return this.activeColorSetting;
    }

    public float getActiveColorSettingX() {
        return this.activeColorSettingX;
    }

    public float getActiveColorSettingY() {
        return this.activeColorSettingY;
    }

    public void setActiveColorSettingPosition(float activeColorSettingX, float activeColorSettingY) {
        this.activeColorSettingX = activeColorSettingX;
        this.activeColorSettingY = activeColorSettingY;
    }

    public void closeActiveColorSetting() {
        if (this.activeColorSetting != null) {
            this.colorPickerExpanded.put(this.activeColorSetting, false);
            this.getColorSettingAnimation(this.activeColorSetting).setEasing(PowerEasing.BACK_IN);
        }
    }

    static {
        String text = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ";
        String textValue = "qwertyuiop[]asdfghjkl;'zxcvbnm,.QWERTYUIOP[]ASDFGHJKL;'ZXCVBNM,.";
        int intValue = Math.min(text.length(), textValue.length());
        for (int index = 0; index < intValue; index++) {
            keyboardLayoutMap.put(text.charAt(index), textValue.charAt(index));
        }
    }
}
