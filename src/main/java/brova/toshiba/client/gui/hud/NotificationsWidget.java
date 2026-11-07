package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.TotemPopEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.NotificationsWidget.ItemNotification;
import brova.toshiba.client.gui.hud.notification.ModuleNotification;
import brova.toshiba.client.gui.hud.notification.NotificationManager;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import brova.toshiba.client.modules.impl.render.Hud;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;

public final class NotificationsWidget extends HudWidgetBase {

    private final Map<ModuleNotification, Animation> ifAbsentAnimationByModuleNotification = new HashMap<>();

    private final Map<ModuleNotification, Float> keySetFloatByModuleNotification = new HashMap<>();

    private final Set<ModuleNotification> addAllModuleNotificationEntries = new HashSet<>();

    private long lastUpdateTimestamp = System.currentTimeMillis();

    private float cachedScalarForHudRender = 0.0F;

    private final Map<UUID, ItemNotification> ifAbsentNotificationsWidgetItemNotificationByUUID = new HashMap<>();

    private final Map<StatusEffect, Integer> entrySetIntegerByStatusEffect = new HashMap<>();

    public NotificationsWidget(Draggable draggable) {
        super(draggable);
        EventBus.register(this);
    }

    private boolean isItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            Item item = stack.getItem();
            return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE || item instanceof PotionItem || item == Items.MILK_BUCKET || item == Items.CHORUS_FRUIT;
        }
    }

    @EventHandler
    public void onPopTotem(TotemPopEvent event) {
        if (mc.player != null && mc.world != null && Hud.INSTANCE.hudModules.is("Уведомления") && Hud.INSTANCE.notificationActions.is("При потере тотема")) {
            PlayerEntity player = event.getPlayer();
            if (player != null && player != mc.player) {
                boolean flag = false;
                ItemStack mainHandStack = player.getMainHandStack();
                ItemStack offHandStack = player.getOffHandStack();
                if (mainHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
                    flag = mainHandStack.hasEnchantments();
                } else if (offHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
                    flag = offHandStack.hasEnchantments();
                }
                String text = flag ? "да" : "нет";
                String textValue = "Игрок " + player.getName().getString() + " потерял тотем, зачарованный: " + text;
                NotificationManager.pushCustom(textValue, "C");
            }
        }
    }

    @EventHandler
    @Override
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.world != null && mc.player != null) {
            if (Hud.INSTANCE.hudModules.is("Уведомления") && Hud.INSTANCE.notificationActions.is("Окончание эффекта")) {
                var statusEffects = mc.player.getStatusEffects();
                HashSet<net.minecraft.entity.effect.StatusEffect> values = new HashSet<>();
                for (StatusEffectInstance statusEffectInstance : statusEffects) {
                    StatusEffect statusEffect = statusEffectInstance.getEffectType().value();
                    values.add(statusEffect);
                    this.entrySetIntegerByStatusEffect.put(statusEffect, statusEffectInstance.getAmplifier());
                }
                var iterator = this.entrySetIntegerByStatusEffect.entrySet().iterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    StatusEffect key = entry.getKey();
                    if (!values.contains(key)) {
                        String string = key.getName().getString();
                        int intValue = entry.getValue() + 1;
                        String text = "Эффект " + string + " " + intValue + " закончился";
                        NotificationManager.pushCustom(text, "P");
                        iterator.remove();
                    }
                }
            } else {
                this.entrySetIntegerByStatusEffect.clear();
            }
            if (Hud.INSTANCE.hudModules.is("Уведомления") && Hud.INSTANCE.notificationActions.is("При использывание предмета")) {
                HashSet<java.util.UUID> valuesValue = new HashSet<>();
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (player != mc.player) {
                        UUID uuid = player.getUuid();
                        valuesValue.add(uuid);
                        if (player.isUsingItem()) {
                            ItemStack activeItem = player.getActiveItem();
                            if (!activeItem.isEmpty() && this.isItem(activeItem)) {
                                this.ifAbsentNotificationsWidgetItemNotificationByUUID.computeIfAbsent(uuid, input -> new ItemNotification(activeItem.copy())).cachedCount++;
                            } else {
                                this.ifAbsentNotificationsWidgetItemNotificationByUUID.remove(uuid);
                            }
                        } else {
                            ItemNotification itemNotification = this.ifAbsentNotificationsWidgetItemNotificationByUUID.remove(uuid);
                            if (itemNotification != null && itemNotification.cachedCount >= 25) {
                                Item item = itemNotification.itemStack.getItem();
                                String textValue = !(item instanceof PotionItem) && item != Items.MILK_BUCKET ? "съел" : "выпил";
                                String stringValue = itemNotification.itemStack.getName().getString();
                                String textResult = "Игрок " + player.getName().getString() + " " + textValue + " " + stringValue;
                                NotificationManager.pushCustom(textResult, "h");
                            }
                        }
                    }
                }
                this.ifAbsentNotificationsWidgetItemNotificationByUUID.keySet().retainAll(valuesValue);
            } else {
                this.ifAbsentNotificationsWidgetItemNotificationByUUID.clear();
            }
        } else {
            this.ifAbsentNotificationsWidgetItemNotificationByUUID.clear();
            this.entrySetIntegerByStatusEffect.clear();
        }
    }

    private TextRenderer getNotificationTextFont(int intValue) {
        return FontManager.getFont("manrope", 13);
    }

    private TextRenderer getNotificationIconFont(int intValue) {
        return FontManager.getFont("icon", intValue);
    }

    private TextRenderer getEffectIconFont(int intValue) {
        return FontManager.getFont("icon", intValue);
    }

    private TextRenderer getBrandIconFont(int intValue) {
        return FontManager.getFont("toshiba", intValue);
    }

    private TextRenderer getHudIconFont(int intValue) {
        return FontManager.getFont("hud", intValue);
    }

    private String createTextFromCustom(ModuleNotification moduleNotification) {
        if (moduleNotification.isCustom()) {
            return moduleNotification.customText;
        } else {
            String text = moduleNotification.enabled ? "Включен" : "выключен";
            return moduleNotification.moduleName + " " + text;
        }
    }

    private String createTextFromPatchIncomingText(String text) {
        if (text == null) {
            return null;
        } else if (ModuleRegistry.INSTANCE == null) {
            return text;
        } else {
            NameProtect nameProtect = ModuleRegistry.nameProtect;
            return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
        }
    }

    private float calculateWidthWithSize(TextRenderer textRenderer, String text) {
        return text != null && !text.isEmpty() ? textRenderer.getFont().getWidth(this.createTextFromPatchIncomingText(text), textRenderer.getSize() * 0.5F) : 0.0F;
    }

    private float calculateSubstringWithCustom(ModuleNotification moduleNotification) {
        String text = this.createTextFromCustom(moduleNotification);
        if (!moduleNotification.isCustom()) {
            String textValue = moduleNotification.moduleName + " ";
            String textResult = text.length() > textValue.length() ? text.substring(textValue.length()) : "";
            return this.calculateWidthWithSize(this.getNotificationTextFont(16), textValue) - 0.5F + this.calculateWidthWithSize(this.getNotificationTextFont(16), textResult);
        } else if (!text.contains(" не найдено") && !text.contains(" не найден")) {
            return this.calculateWidthWithSize(this.getNotificationTextFont(16), text);
        } else {
            String textItem = text.contains(" не найдено") ? " не найдено" : " не найден";
            int index = text.indexOf(textItem);
            String textEntry = text.substring(0, index);
            String textCopy = text.substring(index);
            return this.calculateWidthWithSize(this.getNotificationTextFont(16), textEntry) - 0.5F + this.calculateWidthWithSize(this.getNotificationTextFont(16), textCopy);
        }
    }

    private float calculateModuleNotification(ModuleNotification moduleNotification, float floatValue, float floatValueValue, float floatValueResult) {
        return 15.0F + this.calculateSubstringWithCustom(moduleNotification) + floatValue;
    }

    private float calculateText(String text, float floatValue, float floatValueValue, float floatValueResult) {
        float floatValueItem = this.calculateWidthWithSize(this.getNotificationTextFont(16), text);
        return 15.0F + floatValueItem + floatValue;
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            this.onHudRender(event);
            super.onRender(event);
        }
    }

    private void onHudRender(HudRenderEvent event) {
        if (mc != null) {
            long lastUpdateTimestamp = System.currentTimeMillis();
            float floatValue = (float) (lastUpdateTimestamp - this.lastUpdateTimestamp) / 1000.0F;
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            var active = NotificationManager.getActive();
            boolean flag = mc.currentScreen instanceof ChatScreen;
            boolean flagValue = !active.isEmpty() || flag;
            float floatValueValue = flag ? 1.0F : 0.0F;
            float floatValueResult = 8.0F;
            this.cachedScalarForHudRender = this.cachedScalarForHudRender + (floatValueValue - this.cachedScalarForHudRender) * Math.min(1.0F, floatValueResult * floatValue);
            if (!flagValue && this.cachedScalarForHudRender < 0.01F) {
                this.ifAbsentAnimationByModuleNotification.clear();
                this.keySetFloatByModuleNotification.clear();
                this.cachedScalarForHudRender = 0.0F;
            } else {
                float x = this.draggable.getX();
                float y = this.draggable.getY();
                MatrixStack matrices = event.getContext().getMatrices();
                matrices.push();
                matrices.translate(x, y, 0.0F);
                matrices.scale(1.1F, 1.1F, 1.0F);
                matrices.translate(-x, -y, 0.0F);
                int themeColor = ColorMathUtil.getThemeColor();
                long currentTimeMillis = System.currentTimeMillis();
                float floatValueItem = 16.0F;
                float floatValueEntry = 3.0F;
                float floatValueCopy = 12.0F;
                float floatValue2 = 5.0F;
                float floatValue3 = 1.5F;
                String text = "Пример ";
                String textValue = "вашего";
                String textResult = " уведомления";
                String textItem = text + textValue + textResult;
                String textEntry = "k";
                float floatValue4 = this.calculateText(textItem, floatValue2, floatValueItem, floatValue3);
                float floatValue5 = floatValue4;
                for (ModuleNotification moduleNotification : active) {
                    float floatValue6 = this.calculateModuleNotification(moduleNotification, floatValue2, floatValueItem, floatValue3);
                    if (floatValue6 > floatValue5) {
                        floatValue5 = floatValue6;
                    }
                }
                float floatValue7 = y;
                if (this.cachedScalarForHudRender > 0.01F) {
                    float cachedScalarForHudRender = this.cachedScalarForHudRender;
                    float floatValue8 = 0.86F + 0.14F * cachedScalarForHudRender;
                    float floatValue9 = x + floatValue4 * 0.5F;
                    float floatValue10 = y + floatValueItem * 0.5F;
                    MatrixStack matricesValue = event.getContext().getMatrices();
                    matricesValue.push();
                    matricesValue.translate(floatValue9, floatValue10, 0.0F);
                    matricesValue.scale(floatValue8, floatValue8, 1.0F);
                    matricesValue.translate(-floatValue9, -floatValue10, 0.0F);
                    int intValue = BackgroundSettings.backgroundColor(18, 18, 18, cachedScalarForHudRender);
                    if (cachedScalarForHudRender > 0.03F) {
                        Renderer2D.drawHudShadow(matricesValue, x, y, floatValue4, floatValueItem, 6.0F, 2.5F, ColorMathUtil.rgba(0, 0, 0, (int) (80.0F * cachedScalarForHudRender)));
                        Renderer2D.drawHudBlur(matricesValue, x + 0.5F, y + 0.5F, floatValue4 - 1.0F, floatValueItem - 1.0F, 5.5F, cachedScalarForHudRender);
                        Renderer2D.drawRoundedRect(matricesValue, x, y, floatValue4, floatValueItem, 6.0F, intValue);
                        int intValueValue = ColorUtil.rgba(255, 255, 255, (int) (255.0F * Math.min(1.0F, cachedScalarForHudRender / 0.7F)));
                        int themeStaticColor = ColorMathUtil.getThemeStaticColor();
                        int intValueResult = ColorMathUtil.darken(themeStaticColor, 0.55F);
                        int intValueItem = ColorMathUtil.setAlphaColor(themeStaticColor, (int) (255.0F * Math.min(1.0F, cachedScalarForHudRender / 0.7F)));
                        int intValueEntry = ColorMathUtil.setAlphaColor(intValueResult, (int) (255.0F * Math.min(1.0F, cachedScalarForHudRender / 0.7F)));
                        float floatValue11 = x + 5.5F;
                        this.getBrandIconFont(16).drawGradientStringVertical(matricesValue, textEntry, floatValue11, y + 6.5F, intValueItem, intValueEntry);
                        float floatValue12 = x + 15.0F;
                        TextRenderer notificationTextFont = this.getNotificationTextFont(16);
                        notificationTextFont.draw(matricesValue, text, floatValue12, y + 5.5F, intValueValue);
                        float floatValue13 = floatValue12 + this.calculateWidthWithSize(notificationTextFont, text);
                        notificationTextFont.drawGradientStringVertical(matricesValue, textValue, floatValue13, y + 5.5F, intValueItem, intValueEntry);
                        notificationTextFont.draw(matricesValue, textResult, floatValue13 + this.calculateWidthWithSize(notificationTextFont, textValue), y + 5.5F, intValueValue);
                    }
                    matricesValue.pop();
                    floatValue7 = y + (floatValueItem + floatValueEntry);
                }
                for (ModuleNotification moduleNotificationValue : active) {
                    Animation animation = this.ifAbsentAnimationByModuleNotification.computeIfAbsent(moduleNotificationValue, input -> new Animation(0.0F, 12.0F, PowerEasing.QUAD_OUT));
                    long longValue = currentTimeMillis - moduleNotificationValue.startTime;
                    animation.update(1.0F);
                    float value = animation.getValue();
                    float floatValue14 = value;
                    if (longValue > 2300L) {
                        floatValue14 = (1.0F - (float) (longValue - 2300L) / 200.0F) * value;
                    }
                    if (floatValue14 <= 0.0F) {
                        floatValue7 += floatValueItem + floatValueEntry;
                    } else {
                        Float valueValue = this.keySetFloatByModuleNotification.get(moduleNotificationValue);
                        if (valueValue == null) {
                            valueValue = floatValue7;
                        }
                        float floatValue15 = floatValue7 - valueValue;
                        if (Math.abs(floatValue15) > 0.01F) {
                            valueValue = valueValue + floatValue15 * Math.min(1.0F, floatValueCopy * floatValue);
                        } else {
                            valueValue = floatValue7;
                        }
                        this.keySetFloatByModuleNotification.put(moduleNotificationValue, valueValue);
                        String textCopy = this.createTextFromCustom(moduleNotificationValue);
                        String text2;
                        if (!moduleNotificationValue.isCustom()) {
                            text2 = moduleNotificationValue.enabled ? "+" : "-";
                        } else {
                            text2 = moduleNotificationValue.categoryIcon != null && !moduleNotificationValue.categoryIcon.isEmpty() ? moduleNotificationValue.categoryIcon : "?";
                            if (text2.equals("server")) {
                                text2 = "k";
                            }
                        }
                        float floatValue16 = this.calculateModuleNotification(moduleNotificationValue, floatValue2, floatValueItem, floatValue3);
                        float floatValue17 = x + (floatValue4 - floatValue16) * 0.5F;
                        float floatValue18 = 6.0F * (1.0F - value);
                        float floatValue19 = valueValue + floatValue18;
                        float floatValue20 = 0.86F + 0.14F * floatValue14;
                        float floatValue21 = floatValue17 + floatValue16 * 0.5F;
                        float floatValue22 = floatValue19 + floatValueItem * 0.5F;
                        MatrixStack matricesResult = event.getContext().getMatrices();
                        matricesResult.push();
                        matricesResult.translate(floatValue21, floatValue22, 0.0F);
                        matricesResult.scale(floatValue20, floatValue20, 1.0F);
                        matricesResult.translate(-floatValue21, -floatValue22, 0.0F);
                        int intValueCopy = BackgroundSettings.backgroundColor(18, 18, 18, floatValue14);
                        if (floatValue14 > 0.03F) {
                            Renderer2D.drawHudShadow(matricesResult, floatValue17, floatValue19, floatValue16, floatValueItem, 6.0F, 2.5F, ColorMathUtil.rgba(0, 0, 0, (int) (80.0F * floatValue14)));
                            Renderer2D.drawHudBlur(matricesResult, floatValue17 + 0.5F, floatValue19 + 0.5F, floatValue16 - 1.0F, floatValueItem - 1.0F, 5.5F, floatValue14);
                            Renderer2D.drawRoundedRect(matricesResult, floatValue17, floatValue19, floatValue16, floatValueItem, 6.0F, intValueCopy);
                            int intValue2 = ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue14));
                            int intValue3 = ColorMathUtil.setAlphaColor(themeColor, (int) (255.0F * floatValue14));
                            if (!moduleNotificationValue.isCustom() && !moduleNotificationValue.enabled) {
                                intValue3 = ColorUtil.rgba(255, 255, 255, (int) (123.0F * floatValue14));
                            }
                            float floatValue23 = floatValue17 + 5.5F;
                            float floatValue24 = floatValue19 + 6.5F;
                            byte byteValue = 16;
                            if (!moduleNotificationValue.isCustom() && !moduleNotificationValue.enabled) {
                                this.getHudIconFont(byteValue).draw(matricesResult, text2, floatValue23, floatValue24, intValue3);
                            } else {
                                int themeStaticColorValue = ColorMathUtil.getThemeStaticColor();
                                int intValue4 = ColorMathUtil.darken(themeStaticColorValue, 0.55F);
                                this.getHudIconFont(byteValue).drawGradientStringVertical(matricesResult, text2, floatValue23, floatValue24, ColorMathUtil.setAlphaColor(themeStaticColorValue, (int) (255.0F * floatValue14)), ColorMathUtil.setAlphaColor(intValue4, (int) (255.0F * floatValue14)));
                            }
                            float floatValue25 = floatValue17 + 15.0F;
                            if (!moduleNotificationValue.isCustom()) {
                                String text3 = moduleNotificationValue.moduleName + " ";
                                String text4 = textCopy.length() > text3.length() ? textCopy.substring(text3.length()) : "";
                                int intValue5 = moduleNotificationValue.enabled ? intValue2 : ColorUtil.rgba(255, 255, 255, (int) (123.0F * floatValue14));
                                this.getNotificationTextFont(16).draw(matricesResult, text3, floatValue25, floatValue19 + 5.5F, intValue2);
                                float floatValue26 = floatValue25 + this.calculateWidthWithSize(this.getNotificationTextFont(16), text3) - 0.5F;
                                if (moduleNotificationValue.enabled) {
                                    int themeStaticColorResult = ColorMathUtil.getThemeStaticColor();
                                    int intValue6 = ColorMathUtil.darken(themeStaticColorResult, 0.55F);
                                    this.getNotificationTextFont(16).drawGradientStringVertical(matricesResult, text4, floatValue26, floatValue19 + 5.5F, ColorMathUtil.setAlphaColor(themeStaticColorResult, (int) (255.0F * floatValue14)), ColorMathUtil.setAlphaColor(intValue6, (int) (255.0F * floatValue14)));
                                } else {
                                    this.getNotificationTextFont(16).draw(matricesResult, text4, floatValue26, floatValue19 + 5.5F, intValue5);
                                }
                            } else if (textCopy.contains(" потерял тотем, зачарованный: ")) {
                                int intValue7 = textCopy.indexOf(" зачарованный: ") + " зачарованный: ".length();
                                String text5 = textCopy.substring(0, intValue7);
                                String text6 = textCopy.substring(intValue7);
                                int intValue8 = text6.equalsIgnoreCase("да") ? ColorMathUtil.rgba(95, 255, 20, (int) (255.0F * floatValue14)) : ColorMathUtil.rgba(255, 41, 25, (int) (255.0F * floatValue14));
                                this.getNotificationTextFont(16).draw(matricesResult, text5, floatValue25, floatValue19 + 5.5F, intValue2);
                                this.getNotificationTextFont(16).draw(matricesResult, text6, floatValue25 + this.calculateWidthWithSize(this.getNotificationTextFont(16), text5) - 0.5F, floatValue19 + 5.5F, intValue8);
                            } else if (!textCopy.contains(" не найдено") && !textCopy.contains(" не найден")) {
                                this.getNotificationTextFont(16).draw(matricesResult, textCopy, floatValue25, floatValue19 + 5.5F, intValue2);
                            } else {
                                String text7 = textCopy.contains(" не найдено") ? " не найдено" : " не найден";
                                int index = textCopy.indexOf(text7);
                                String text8 = textCopy.substring(0, index);
                                String text9 = textCopy.substring(index);
                                int intValue9 = ColorMathUtil.rgba(255, 41, 25, (int) (255.0F * floatValue14));
                                this.getNotificationTextFont(16).draw(matricesResult, text8, floatValue25, floatValue19 + 5.5F, intValue2);
                                this.getNotificationTextFont(16).draw(matricesResult, text9, floatValue25 + this.calculateWidthWithSize(this.getNotificationTextFont(16), text8) - 0.5F, floatValue19 + 5.5F, intValue9);
                            }
                        }
                        matricesResult.pop();
                        floatValue7 += floatValueItem + floatValueEntry;
                    }
                }
                this.addAllModuleNotificationEntries.clear();
                this.addAllModuleNotificationEntries.addAll(active);
                this.ifAbsentAnimationByModuleNotification.keySet().removeIf(inputValue -> !this.addAllModuleNotificationEntries.contains(inputValue));
                this.keySetFloatByModuleNotification.keySet().removeIf(inputResult -> !this.addAllModuleNotificationEntries.contains(inputResult));
                matrices.pop();
                this.draggable.setWidth(floatValue5 * 1.1F);
                this.draggable.setHeight(Math.max(1.0F, floatValue7 - y) * 1.1F);
            }
        }
    }

    public static class ItemNotification {

        public final ItemStack itemStack;

        public int cachedCount;

        public ItemNotification(ItemStack itemStack) {
            this.itemStack = itemStack;
            this.cachedCount = 0;
        }
    }
}
