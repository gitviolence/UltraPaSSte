package brova.toshiba.client.configs;

import brova.toshiba.Toshiba;
import brova.toshiba.client.data.Macro;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.Draggable;
import brova.toshiba.client.gui.hud.DraggableStorage;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.modules.settings.implement.TextSetting;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.storages.implement.MacroStorage;
import brova.toshiba.client.storages.implement.ThemeStorage;
import brova.toshiba.client.storages.implement.ThemeStorage.Themes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ConfigManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<SettingBase, Object> objectBySettingBase = new IdentityHashMap<>();

    private static Themes themeStorageThemes;

    private static boolean stateFlag;

    private ConfigManager() {
    }

    public static void captureDefaults() {
        if (!stateFlag) {
            var iterator = ModuleRegistry.INSTANCE.getObject().iterator();
            while (iterator.hasNext()) {
                Module module = iterator.next();
                for (SettingBase setting : module.getSettings()) {
                    updateCurrentWithKey(setting);
                }
            }
            for (SettingBase settingValue : BackgroundSettings.getSettings()) {
                updateCurrentWithKey(settingValue);
            }
            if (Toshiba.INSTANCE.themeStorage != null) {
                themeStorageThemes = brova.toshiba.client.utils.runtime.AccessGuard.invoke(brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage), new Object[] {}, (accessReceiver, accessArguments) -> ((ThemeStorage) accessReceiver).getThemes());
            }
            stateFlag = true;
        }
    }

    public static String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("version", 1);
        if (brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage) != null && brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage).getThemes() != null) {
            json.addProperty("theme", brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage).getThemes().name());
        }
        JsonObject jsonValue = new JsonObject();
        var iterator = ModuleRegistry.INSTANCE.getObject().iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            JsonObject jsonResult = new JsonObject();
            jsonResult.addProperty("enabled", module.isEnable());
            jsonResult.addProperty("toggled", module.isEnable());
            jsonResult.addProperty("bind", module.getKey());
            JsonObject jsonItem = new JsonObject();
            for (SettingBase setting : module.getSettings()) {
                jsonItem.add(setting.name(), createJsonElementFromCurrentWithKey(setting));
            }
            jsonResult.add("settings", jsonItem);
            jsonValue.add(module.getName(), jsonResult);
        }
        json.add("modules", jsonValue);
        JsonObject jsonEntry = new JsonObject();
        for (SettingBase settingValue : BackgroundSettings.getSettings()) {
            jsonEntry.add(settingValue.name(), createJsonElementFromCurrentWithKey(settingValue));
        }
        json.add("hudAppearance", jsonEntry);
        JsonObject jsonCopy = new JsonObject();
        for (var entry : DraggableStorage.draggables.entrySet()) {
            JsonObject json2 = new JsonObject();
            json2.addProperty("x", entry.getValue().getX());
            json2.addProperty("y", entry.getValue().getY());
            jsonCopy.add(entry.getKey(), json2);
        }
        json.add("draggables", jsonCopy);
        JsonArray array = new JsonArray();
        if (brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.macroStorage) != null) {
            Toshiba.INSTANCE.macroStorage.getMacros().forEach(input -> {
                JsonObject json3 = new JsonObject();
                json3.addProperty("name", input.getName());
                json3.addProperty("command", input.getCommand());
                json3.addProperty("bind", input.getBind() == null ? -1 : input.getBind().getKey());
                array.add(json3);
            });
        }
        json.add("macros", array);
        return gson.toJson(json);
    }

    public static void reset() {
        captureDefaults();
        var iterator = ModuleRegistry.INSTANCE.getObject().iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            try {
                boolean flag = false;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(module, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                    ((Module) accessReceiver).setEnabled(((boolean) accessArguments[0]));
                });
            } catch (Throwable failure) {
                module.setEnable(false);
                EventBus.unregister(module);
            }
            module.setKey(-1);
            module.setOpen(false);
            for (SettingBase setting : brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getSettings())) {
                updateValueWithText(setting);
            }
        }
        for (SettingBase settingValue : BackgroundSettings.getSettings()) {
            updateValueWithText(settingValue);
        }
        for (Draggable draggable : DraggableStorage.draggables.values()) {
            draggable.setX(draggable.initialXVal);
            draggable.setY(draggable.initialYVal);
        }
        if (themeStorageThemes != null && Toshiba.INSTANCE.themeStorage != null) {
            brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage).setThemes(themeStorageThemes);
        }
        if (brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.macroStorage) != null) {
            brova.toshiba.client.utils.runtime.AccessGuard.invoke(Toshiba.INSTANCE.macroStorage, new Object[] {}, (accessReceiver, accessArguments) -> ((MacroStorage) accessReceiver).getMacros()).forEach(input -> {
                if (input.getBind() != null) {
                    input.getBind().setKey(-1);
                }
            });
        }
    }

    public static ConfigResult apply(String text) {
        try {
            JsonElement element = JsonParser.parseString(text == null ? "" : text);
            if (!element.isJsonObject()) {
                return new ConfigResult(false, "Корень JSON должен быть объектом");
            } else {
                JsonObject asJsonObject = element.getAsJsonObject();
                updateHasWithJsonObject(asJsonObject);
                updateHasWithAsJsonObject(asJsonObject);
                return new ConfigResult(true, "Конфиг успешно запущен");
            }
        } catch (Exception failure) {
            String message = failure.getMessage();
            return new ConfigResult(false, message != null && !message.isBlank() ? message : "Некорректный JSON");
        }
    }

    private static void updateHasWithJsonObject(JsonObject json) {
        if (json.has("modules") && json.get("modules").isJsonObject()) {
            JsonObject asJsonObject = json.getAsJsonObject("modules");
            var iterator = ModuleRegistry.INSTANCE.getObject().iterator();
            while (iterator.hasNext()) {
                Module module = iterator.next();
                if (asJsonObject.has(module.getName())) {
                    JsonElement element = asJsonObject.get(module.getName());
                    if (!element.isJsonObject()) {
                        throw new IllegalArgumentException("Модуль " + module.getName() + " должен быть объектом");
                    }
                    JsonObject asJsonObjectValue = element.getAsJsonObject();
                    if (asJsonObjectValue.has("enabled") && !asJsonObjectValue.get("enabled").isJsonPrimitive()) {
                        throw new IllegalArgumentException("Некорректное состояние модуля " + module.getName());
                    }
                    if (asJsonObjectValue.has("toggled") && !asJsonObjectValue.get("toggled").isJsonPrimitive()) {
                        throw new IllegalArgumentException("Некорректное состояние модуля " + brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getName()));
                    }
                    if (asJsonObjectValue.has("bind") && !asJsonObjectValue.get("bind").isJsonPrimitive()) {
                        throw new IllegalArgumentException("Некорректный бинд модуля " + brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getName()));
                    }
                    if (asJsonObjectValue.has("enabled")) {
                        asJsonObjectValue.get("enabled").getAsBoolean();
                    }
                    if (asJsonObjectValue.has("toggled")) {
                        asJsonObjectValue.get("toggled").getAsBoolean();
                    }
                    if (asJsonObjectValue.has("bind")) {
                        asJsonObjectValue.get("bind").getAsInt();
                    }
                    if (asJsonObjectValue.has("settings") && !asJsonObjectValue.get("settings").isJsonObject()) {
                        throw new IllegalArgumentException("Настройки модуля " + module.getName() + " должны быть объектом");
                    }
                    if (asJsonObjectValue.has("settings")) {
                        updateNameWithHasAndAsBoolean(brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getSettings()), asJsonObjectValue.getAsJsonObject("settings"));
                    }
                }
            }
            if (json.has("hudAppearance")) {
                if (!json.get("hudAppearance").isJsonObject()) {
                    throw new IllegalArgumentException("hudAppearance должен быть объектом");
                }
                updateNameWithHasAndAsBoolean(BackgroundSettings.getSettings(), json.getAsJsonObject("hudAppearance"));
            }
            if (json.has("draggables") && !json.get("draggables").isJsonObject()) {
                throw new IllegalArgumentException("draggables должен быть объектом");
            } else {
                if (json.has("draggables")) {
                    for (var entry : json.getAsJsonObject("draggables").entrySet()) {
                        if (!entry.getValue().isJsonObject()) {
                            throw new IllegalArgumentException("Позиция " + entry.getKey() + " должна быть объектом");
                        }
                        JsonObject asJsonObjectResult = entry.getValue().getAsJsonObject();
                        if (asJsonObjectResult.has("x")) {
                            asJsonObjectResult.get("x").getAsFloat();
                        }
                        if (asJsonObjectResult.has("y")) {
                            asJsonObjectResult.get("y").getAsFloat();
                        }
                    }
                }
                if (json.has("macros") && !json.get("macros").isJsonArray()) {
                    throw new IllegalArgumentException("macros должен быть массивом");
                } else {
                    if (json.has("macros")) {
                        for (JsonElement elementValue : json.getAsJsonArray("macros")) {
                            if (!elementValue.isJsonObject()) {
                                throw new IllegalArgumentException("Каждый макрос должен быть объектом");
                            }
                            JsonObject asJsonObjectItem = elementValue.getAsJsonObject();
                            if (asJsonObjectItem.has("name")) {
                                asJsonObjectItem.get("name").getAsString();
                            }
                            if (asJsonObjectItem.has("command")) {
                                asJsonObjectItem.get("command").getAsString();
                            }
                            if (asJsonObjectItem.has("bind")) {
                                asJsonObjectItem.get("bind").getAsInt();
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("В JSON отсутствует объект modules");
        }
    }

    private static void updateNameWithHasAndAsBoolean(List<SettingBase> settings, JsonObject json) {
        for (SettingBase setting : settings) {
            if (json.has(setting.name())) {
                JsonElement element = json.get(setting.name());
                if (setting instanceof BooleanSetting) {
                    element.getAsBoolean();
                } else if (setting instanceof FloatSetting) {
                    element.getAsFloat();
                } else if (!(setting instanceof ModeSetting) && !(setting instanceof TextSetting)) {
                    if (!(setting instanceof BindSetting) && !(setting instanceof ColorSetting)) {
                        if (setting instanceof MultiSetting multiSetting) {
                            if (!element.isJsonObject()) {
                                throw new IllegalArgumentException("Настройка " + setting.name() + " должна быть объектом");
                            }
                            JsonObject asJsonObject = element.getAsJsonObject();
                            for (BooleanSetting settingValue : multiSetting.getSettings()) {
                                if (asJsonObject.has(settingValue.name())) {
                                    asJsonObject.get(settingValue.name()).getAsBoolean();
                                }
                            }
                        }
                    } else {
                        element.getAsInt();
                    }
                } else {
                    element.getAsString();
                }
            }
        }
    }

    private static void updateHasWithAsJsonObject(JsonObject json) {
        JsonObject asJsonObject = json.getAsJsonObject("modules");
        IdentityHashMap<Module, Boolean> identityHashMap = new IdentityHashMap<>();
        var iterator = ModuleRegistry.INSTANCE.getObject().iterator();
        while (iterator.hasNext()) {
            Module module = iterator.next();
            module.setOpen(false);
            JsonObject jsonValue = asJsonObject.has(brova.toshiba.client.utils.runtime.AccessGuard.invoke(module, new Object[] {}, (accessReceiver, accessArguments) -> ((Module) accessReceiver).getName())) ? asJsonObject.getAsJsonObject(module.getName()) : null;
            if (jsonValue == null) {
                module.setKey(-1);
                identityHashMap.put(module, false);
            } else {
                module.setKey(jsonValue.has("bind") ? jsonValue.get("bind").getAsInt() : -1);
                if (jsonValue.has("settings")) {
                    updateNameWithHasAndState(module.getSettings(), jsonValue.getAsJsonObject("settings"));
                }
                identityHashMap.put(module, jsonValue.has("enabled") ? jsonValue.get("enabled").getAsBoolean() : jsonValue.has("toggled") && jsonValue.get("toggled").getAsBoolean());
            }
        }
        if (json.has("hudAppearance")) {
            updateNameWithHasAndState(BackgroundSettings.getSettings(), json.getAsJsonObject("hudAppearance"));
        }
        if (json.has("theme") && Toshiba.INSTANCE.themeStorage != null) {
            String asString = json.get("theme").getAsString();
            for (Themes themes : brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> Themes.values())) {
                if (((Enum<?>) themes).name().equalsIgnoreCase(asString)) {
                    brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.themeStorage).setThemes(themes);
                    break;
                }
            }
        }
        if (json.has("draggables")) {
            JsonObject asJsonObjectValue = json.getAsJsonObject("draggables");
            for (var entry : DraggableStorage.draggables.entrySet()) {
                if (asJsonObjectValue.has(entry.getKey()) && asJsonObjectValue.get(entry.getKey()).isJsonObject()) {
                    JsonObject asJsonObjectResult = asJsonObjectValue.getAsJsonObject(entry.getKey());
                    if (asJsonObjectResult.has("x")) {
                        Draggable value = entry.getValue();
                        float asFloat = asJsonObjectResult.get("x").getAsFloat();
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(value, new Object[] { asFloat }, (accessReceiver, accessArguments) -> {
                            ((Draggable) accessReceiver).setX(((float) accessArguments[0]));
                        });
                    }
                    if (asJsonObjectResult.has("y")) {
                        Draggable valueValue = entry.getValue();
                        float asFloatValue = asJsonObjectResult.get("y").getAsFloat();
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(valueValue, new Object[] { asFloatValue }, (accessReceiver, accessArguments) -> {
                            ((Draggable) accessReceiver).setY(((float) accessArguments[0]));
                        });
                    }
                }
            }
        }
        if (json.has("macros") && brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.macroStorage) != null) {
            brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.macroStorage).clear();
            for (JsonElement element : json.getAsJsonArray("macros")) {
                if (element.isJsonObject()) {
                    JsonObject asJsonObjectItem = element.getAsJsonObject();
                    String text = asJsonObjectItem.has("name") ? asJsonObjectItem.get("name").getAsString() : "";
                    String textValue = asJsonObjectItem.has("command") ? asJsonObjectItem.get("command").getAsString() : "";
                    int intValue = asJsonObjectItem.has("bind") ? asJsonObjectItem.get("bind").getAsInt() : -1;
                    if (!text.isBlank()) {
                        Toshiba.INSTANCE.macroStorage.add(new Macro(text, textValue, new BindSetting("bind", intValue)));
                    }
                }
            }
        }
        for (var entryValue : identityHashMap.entrySet()) {
            Module module = entryValue.getKey();
            boolean desired = entryValue.getValue();
            // Меняем состояние только у тех модулей, которые реально отличаются от конфига:
            // иначе при автозагрузке сыпались бы уведомления и двойная регистрация событий.
            if (module.isEnable() != desired) {
                boolean flag = desired;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(module, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                    ((Module) accessReceiver).setEnabled(((boolean) accessArguments[0]));
                });
            }
        }
    }

    private static void updateNameWithHasAndState(List<SettingBase> settings, JsonObject json) {
        for (SettingBase setting : settings) {
            if (json.has(setting.name())) {
                JsonElement element = json.get(setting.name());
                if (setting instanceof BooleanSetting settingResult) {
                    settingResult.setState(element.getAsBoolean());
                } else if (setting instanceof FloatSetting settingItem) {
                    settingItem.setValue(element.getAsFloat());
                } else if (setting instanceof ModeSetting settingEntry) {
                    settingEntry.set(element.getAsString());
                } else if (setting instanceof TextSetting textSetting) {
                    textSetting.setText(element.getAsString());
                } else if (setting instanceof BindSetting bindSetting) {
                    bindSetting.setKey(element.getAsInt());
                } else if (setting instanceof ColorSetting colorSetting) {
                    colorSetting.setColor(element.getAsInt());
                } else if (setting instanceof MultiSetting multiSetting && element.isJsonObject()) {
                    JsonObject asJsonObject = element.getAsJsonObject();
                    for (BooleanSetting settingValue : brova.toshiba.client.utils.runtime.AccessGuard.invoke(multiSetting, new Object[] {}, (accessReceiver, accessArguments) -> ((MultiSetting) accessReceiver).getSettings())) {
                        if (asJsonObject.has(settingValue.name())) {
                            settingValue.setState(asJsonObject.get(settingValue.name()).getAsBoolean());
                        }
                    }
                }
            }
        }
    }

    private static void updateCurrentWithKey(SettingBase setting) {
        if (setting instanceof BooleanSetting settingResult) {
            objectBySettingBase.put(setting, settingResult.isState());
        } else if (setting instanceof FloatSetting settingItem) {
            objectBySettingBase.put(setting, settingItem.get());
        } else if (setting instanceof ModeSetting settingEntry) {
            objectBySettingBase.put(setting, settingEntry.getCurrent());
        } else if (setting instanceof TextSetting textSetting) {
            objectBySettingBase.put(setting, brova.toshiba.client.utils.runtime.AccessGuard.invoke(textSetting, new Object[] {}, (accessReceiver, accessArguments) -> ((TextSetting) accessReceiver).get()));
        } else if (setting instanceof BindSetting bindSetting) {
            objectBySettingBase.put(setting, bindSetting.getKey());
        } else if (setting instanceof ColorSetting colorSetting) {
            objectBySettingBase.put(setting, colorSetting.getColor());
        } else if (setting instanceof MultiSetting multiSetting) {
            for (BooleanSetting settingValue : multiSetting.getSettings()) {
                updateCurrentWithKey(settingValue);
            }
        }
    }

    private static void updateValueWithText(SettingBase setting) {
        Object value = objectBySettingBase.get(setting);
        if (setting instanceof BooleanSetting settingResult && value instanceof Boolean enabled) {
            boolean flag = enabled;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(settingResult, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                ((BooleanSetting) accessReceiver).setState(((boolean) accessArguments[0]));
            });
        } else if (setting instanceof FloatSetting settingItem && value instanceof Number number) {
            settingItem.setValue(number.floatValue());
            boolean flagValue = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(settingItem, new Object[] { flagValue }, (accessReceiver, accessArguments) -> {
                ((FloatSetting) accessReceiver).setActive(((boolean) accessArguments[0]));
            });
        } else if (setting instanceof ModeSetting settingEntry && value instanceof String text) {
            settingEntry.set(text);
        } else if (setting instanceof TextSetting textSetting && value instanceof String textValue) {
            textSetting.setText(textValue);
        } else if (setting instanceof BindSetting bindSetting) {
            bindSetting.setKey(-1);
        } else if (setting instanceof ColorSetting colorSetting && value instanceof Number numberValue) {
            colorSetting.setColor(numberValue.intValue());
        } else if (setting instanceof MultiSetting multiSetting) {
            for (BooleanSetting settingValue : multiSetting.getSettings()) {
                updateValueWithText(settingValue);
            }
        }
    }

    private static JsonElement createJsonElementFromCurrentWithKey(SettingBase setting) {
        if (setting instanceof BooleanSetting settingResult) {
            return new JsonPrimitive(settingResult.isState());
        } else if (setting instanceof FloatSetting settingItem) {
            return new JsonPrimitive(settingItem.get());
        } else if (setting instanceof ModeSetting settingEntry) {
            return new JsonPrimitive(settingEntry.getCurrent());
        } else if (setting instanceof TextSetting textSetting) {
            return new JsonPrimitive(brova.toshiba.client.utils.runtime.AccessGuard.<String>invoke(textSetting, new Object[] {}, (accessReceiver, accessArguments) -> ((TextSetting) accessReceiver).get()));
        } else if (setting instanceof BindSetting bindSetting) {
            return new JsonPrimitive(bindSetting.getKey());
        } else if (setting instanceof ColorSetting colorSetting) {
            return new JsonPrimitive(colorSetting.getColor());
        } else if (!(setting instanceof MultiSetting multiSetting)) {
            return new JsonPrimitive("");
        } else {
            JsonObject json = new JsonObject();
            for (BooleanSetting settingValue : multiSetting.getSettings()) {
                json.addProperty(settingValue.name(), settingValue.isState());
            }
            return json;
        }
    }
}
