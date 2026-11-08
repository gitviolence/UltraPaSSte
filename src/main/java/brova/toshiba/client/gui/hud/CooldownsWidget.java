package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.CooldownsWidget.CooldownEntry;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import brova.toshiba.mixin.ItemCooldownEntryAccessor;
import brova.toshiba.mixin.ItemCooldownManagerAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CooldownsWidget extends HudWidgetBase {

    private static final Identifier identifier = Identifier.of("toshiba", "cooldown_preview");

    private float width = 77.5F;

    private float height = 62.5F;

    private float cachedScalarForUpdateItemCooldownManager;

    private ItemCooldownManager itemCooldownManager;

    private int cachedCount = Integer.MIN_VALUE;

    private final Map<Identifier, ItemStack> itemStackByIdentifier = new HashMap<>();

    private final Map<Identifier, Float> cooldownAlpha = new HashMap<>();

    private final Map<Identifier, Float> cooldownVerticalPositions = new HashMap<>();

    private final Map<Identifier, CooldownEntry> cooldownsWidgetCooldownEntryByIdentifier = new HashMap<>();

    private final List<CooldownEntry> sortCooldownsWidgetCooldownEntryEntries = new ArrayList<>();

    private final List<CooldownEntry> addAllCooldownsWidgetCooldownEntryEntries = new ArrayList<>();

    private final Set<Identifier> activeCooldownIds = new HashSet<>();

    private final Set<Identifier> previousCooldownIds = new HashSet<>();

    public CooldownsWidget(Draggable draggable) {
        super(draggable);
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            ItemCooldownManager itemCooldownManager = mc != null && mc.player != null ? mc.player.getItemCooldownManager() : null;
            if (this.itemCooldownManager != itemCooldownManager) {
                this.updateItemCooldownManager(itemCooldownManager);
            }
            if (this.itemCooldownManager == null) {
                this.draggable.setWidth(0.0F);
                this.draggable.setHeight(0.0F);
            } else {
                this.updateInventoryWithToshiba$getEndTick();
                this.onHudRender(event);
                super.onRender(event);
            }
        }
    }

    private void updateItemCooldownManager(ItemCooldownManager itemCooldownManager) {
        this.itemCooldownManager = itemCooldownManager;
        this.cachedCount = Integer.MIN_VALUE;
        this.itemStackByIdentifier.clear();
        this.cooldownAlpha.clear();
        this.cooldownVerticalPositions.clear();
        this.cooldownsWidgetCooldownEntryByIdentifier.clear();
        this.sortCooldownsWidgetCooldownEntryEntries.clear();
        this.addAllCooldownsWidgetCooldownEntryEntries.clear();
        this.activeCooldownIds.clear();
        this.previousCooldownIds.clear();
        this.width = 77.5F;
        this.height = 62.5F;
        this.cachedScalarForUpdateItemCooldownManager = 0.0F;
    }

    private void updateInventoryWithToshiba$getEndTick() {
        this.sortCooldownsWidgetCooldownEntryEntries.clear();
        this.activeCooldownIds.clear();
        ItemCooldownManagerAccessor itemCooldownManager = (ItemCooldownManagerAccessor) this.itemCooldownManager;
        int cachedCount = itemCooldownManager.toshiba$getTick();
        if (this.cachedCount != cachedCount) {
            for (int index = 0; index < mc.player.getInventory().size(); index++) {
                this.updateGroupWithAreItemsAndComponentsEqual(mc.player.getInventory().getStack(index));
            }
            this.updateGroupWithAreItemsAndComponentsEqual(mc.player.getOffHandStack());
            this.updateGroupWithAreItemsAndComponentsEqual(mc.player.getMainHandStack());
            this.cachedCount = cachedCount;
        }
        for (var entry : itemCooldownManager.toshiba$getEntries().entrySet()) {
            Identifier key = entry.getKey();
            ItemCooldownEntryAccessor value = (ItemCooldownEntryAccessor) entry.getValue();
            int intValue = value.toshiba$getEndTick() - cachedCount;
            if (intValue > 0) {
                int intValueValue = value.toshiba$getStartTick();
                CooldownEntry cooldownEntry = this.cooldownsWidgetCooldownEntryByIdentifier.get(key);
                ItemStack stack = cooldownEntry != null && cooldownEntry.startTick() == intValueValue ? cooldownEntry.stack() : this.itemStackByIdentifier.get(key);
                if (stack == null) {
                    stack = Registries.ITEM.getOptionalValue(key).map(input -> input.getDefaultStack()).orElse(ItemStack.EMPTY);
                }
                if (!stack.isEmpty()) {
                    int intValueResult = Math.max(1, value.toshiba$getEndTick() - intValueValue);
                    CooldownEntry cooldownEntryValue = new CooldownEntry(key, stack, stack.getName().getString(), this.formatTextFromFormat(intValue), intValue, intValueResult, intValueValue);
                    this.cooldownsWidgetCooldownEntryByIdentifier.put(key, cooldownEntryValue);
                    this.sortCooldownsWidgetCooldownEntryEntries.add(cooldownEntryValue);
                    this.activeCooldownIds.add(key);
                }
            }
        }
        this.sortCooldownsWidgetCooldownEntryEntries.sort(Comparator.comparing(inputValue -> inputValue.group().toString()));
        if (this.sortCooldownsWidgetCooldownEntryEntries.isEmpty() && mc.currentScreen instanceof ChatScreen) {
            ItemStack defaultStack = Items.ENDER_PEARL.getDefaultStack();
            CooldownEntry cooldownEntryResult = new CooldownEntry(identifier, defaultStack, defaultStack.getName().getString(), "0:15", 300, 300, 0);
            this.cooldownsWidgetCooldownEntryByIdentifier.put(identifier, cooldownEntryResult);
            this.sortCooldownsWidgetCooldownEntryEntries.add(cooldownEntryResult);
            this.activeCooldownIds.add(identifier);
        }
    }

    private void updateGroupWithAreItemsAndComponentsEqual(ItemStack stack) {
        if (!stack.isEmpty()) {
            Identifier group = this.itemCooldownManager.getGroup(stack);
            ItemStack stackValue = this.itemStackByIdentifier.get(group);
            if (stackValue == null || !ItemStack.areItemsAndComponentsEqual(stackValue, stack)) {
                this.itemStackByIdentifier.put(group, stack.copyWithCount(1));
            }
        }
    }

    private String formatTextFromFormat(int intValue) {
        int roundedUp = (int) Math.ceil(intValue / 20.0);
        return roundedUp / 60 + ":" + String.format(Locale.ROOT, "%02d", roundedUp % 60);
    }

    private float lerp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + (floatValueValue - floatValue) * floatValueResult;
    }

    private void onHudRender(HudRenderEvent event) {
        MatrixStack matrices = event.getContext().getMatrices();
        this.previousCooldownIds.clear();
        this.previousCooldownIds.addAll(this.cooldownAlpha.keySet());
        this.previousCooldownIds.addAll(this.activeCooldownIds);
        for (Identifier texture : this.previousCooldownIds) {
            boolean flag = this.activeCooldownIds.contains(texture);
            float interpolatedValue = this.lerp(this.cooldownAlpha.getOrDefault(texture, 0.0F), flag ? 1.0F : 0.0F, 0.12F);
            if (!(interpolatedValue > 0.01F) && !flag) {
                this.cooldownAlpha.remove(texture);
                this.cooldownVerticalPositions.remove(texture);
                this.cooldownsWidgetCooldownEntryByIdentifier.remove(texture);
            } else {
                this.cooldownAlpha.put(texture, interpolatedValue);
            }
        }
        boolean flagValue = this.cooldownAlpha.values().stream().anyMatch(input -> input > 0.01F);
        boolean flagResult = this.sortCooldownsWidgetCooldownEntryEntries.isEmpty() && !flagValue && !(mc.currentScreen instanceof ChatScreen);
        this.cachedScalarForUpdateItemCooldownManager = this.lerp(this.cachedScalarForUpdateItemCooldownManager, flagResult ? 0.0F : 1.0F, 0.16F);
        float floatValue = Math.max(0.0F, Math.min(1.0F, this.cachedScalarForUpdateItemCooldownManager));
        if (flagResult && floatValue <= 0.01F) {
            this.draggable.setWidth(85.25F);
            this.draggable.setHeight(68.75F);
        } else {
            this.addAllCooldownsWidgetCooldownEntryEntries.clear();
            this.addAllCooldownsWidgetCooldownEntryEntries.addAll(this.sortCooldownsWidgetCooldownEntryEntries);
            for (var entry : this.cooldownAlpha.entrySet()) {
                if (!this.activeCooldownIds.contains(entry.getKey()) && entry.getValue() > 0.01F) {
                    CooldownEntry cooldownEntry = this.cooldownsWidgetCooldownEntryByIdentifier.get(entry.getKey());
                    if (cooldownEntry != null) {
                        this.addAllCooldownsWidgetCooldownEntryEntries.add(cooldownEntry);
                    }
                }
            }
            float floatValueValue = 0.0F;
            for (CooldownEntry cooldownEntryValue : this.addAllCooldownsWidgetCooldownEntryEntries) {
                floatValueValue += 10.5F * this.cooldownAlpha.getOrDefault(cooldownEntryValue.group(), 0.0F);
            }
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            float width = this.width;
            float floatValueResult = 18.0F + floatValueValue + 6.5F;
            float floatValueItem = flagResult ? (1.0F - floatValue) * -6.0F : 0.0F;
            int themeStaticColor = ColorMathUtil.getThemeStaticColor();
            int intValue = ColorMathUtil.darken(themeStaticColor, 0.55F);
            TextRenderer font = FontManager.getFont("roboto_flex_semibold", 15);
            TextRenderer fontValue = FontManager.getFont("roboto_flex_medium", 15);
            TextRenderer fontResult = FontManager.getFont("roboto_flex_medium", 15);
            matrices.push();
            matrices.translate(x, y, 0.0F);
            matrices.scale(1.1F, 1.1F, 1.0F);
            matrices.translate(-x, -y, 0.0F);
            matrices.translate(0.0F, floatValueItem, 0.0F);
            int intValueValue = BackgroundSettings.backgroundColor(18, 18, 18, floatValue);
            int intValueResult = BackgroundSettings.overlayColor(0, 0, 0, 38, floatValue);
            if (floatValue > 0.03F) {
                Renderer2D.drawHudShadow(matrices, x, y, width, floatValueResult, 7.0F, 2.0F, ColorMathUtil.rgba(0, 0, 0, (int) (90.0F * floatValue)));
                Renderer2D.drawHudBlur(matrices, x + 0.5F, y + 0.5F, width - 1.0F, floatValueResult - 1.0F, 6.5F, floatValue);
            }
            Renderer2D.drawRoundedRect(matrices, x, y, width, floatValueResult, 7.0F, intValueValue);
            Renderer2D.drawRoundedRect(matrices, x, y, width, 18.0F, 7.0F, 0.0F, 7.0F, 0.0F, intValueResult);
            TextRenderer fontItem = FontManager.getFont("cooldown", 14);
            fontItem.drawGradientStringVertical(matrices, "C", x + width - 7.0F - 8.0F, y + 8.0F, ColorMathUtil.applyAlpha(themeStaticColor, floatValue), ColorMathUtil.applyAlpha(intValue, floatValue));
            font.drawString(matrices, "Cooldowns", x + 7.0F, y + 6.2F, ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue)));
            boolean flagItem = floatValueValue > 0.5F && !this.addAllCooldownsWidgetCooldownEntryEntries.isEmpty();
            if (flagItem) {
                ScissorStack.push();
                ScissorStack.setFromComponentCoordinates((double) x, (double) (y + (18.0F + floatValueItem) * 1.1F), (double) (width * 1.1F), (double) ((floatValueValue + 3.0F) * 1.1F));
            }
            float floatValueEntry = 0.0F;
            float floatValueCopy = 77.5F;
            for (CooldownEntry cooldownEntryResult : this.addAllCooldownsWidgetCooldownEntryEntries) {
                float orDefault = this.cooldownAlpha.getOrDefault(cooldownEntryResult.group(), 0.0F);
                if (!(orDefault <= 0.01F)) {
                    float floatValue2 = orDefault * floatValue;
                    float floatValue3 = y + 18.0F + floatValueEntry + 1.0F - (1.0F - orDefault) * 5.0F;
                    if (floatValue2 <= 0.03F) {
                        floatValueEntry += 10.5F * orDefault;
                    } else {
                        float floatValue4 = floatValue3 + 7.0F;
                        int intValueItem = this.calculateCosWithCurrentTimeMillis(cooldownEntryResult.durationTicks(), floatValue2);
                        this.onHudRender(event, cooldownEntryResult.stack(), x + 7.0F, floatValue3 + 3.5F, intValueItem / 255.0F);
                        fontValue.drawString(matrices, cooldownEntryResult.name(), x + 7.0F + 11.0F, floatValue3 + 5.0F, ColorUtil.rgba(255, 255, 255, intValueItem));
                        float renderWidth = fontResult.getRenderWidth(cooldownEntryResult.duration());
                        float floatValue5 = Math.max(24.0F, renderWidth + 12.0F);
                        float interpolatedValueValue = this.lerp(this.cooldownVerticalPositions.getOrDefault(cooldownEntryResult.group(), floatValue5), floatValue5, 0.18F);
                        this.cooldownVerticalPositions.put(cooldownEntryResult.group(), interpolatedValueValue);
                        float floatValue6 = x + width - 7.0F - interpolatedValueValue;
                        int intValueEntry = BackgroundSettings.overlayColor(255, 255, 255, 20, floatValue2);
                        Renderer2D.drawRoundedRect(matrices, floatValue6 - 1.0F, floatValue3 + 2.0F, interpolatedValueValue + 2.5F, 10.0F, 3.0F, intValueEntry);
                        fontResult.drawString(matrices, cooldownEntryResult.duration(), floatValue6 + 2.0F, floatValue3 + 5.0F, ColorUtil.rgba(255, 255, 255, intValueItem));
                        float floatValue7 = 7.8F;
                        float floatValue8 = floatValue6 + interpolatedValueValue - floatValue7 - 1.0F;
                        float floatValue9 = floatValue4 - 4.0F;
                        Renderer2D.drawRingArc(matrices, floatValue8, floatValue9, floatValue7, 1.7F, -90.0F, 269.9F, ColorUtil.rgba(255, 255, 255, (int) (8.0F * floatValue2)));
                        float floatValue10 = Math.max(0.0F, Math.min(1.0F, (float) cooldownEntryResult.durationTicks() / cooldownEntryResult.maximumDurationTicks()));
                        if (floatValue10 > 0.002F) {
                            Renderer2D.drawRingArc(matrices, floatValue8, floatValue9, floatValue7, 1.7F, -90.0F, -90.0F + 360.0F * floatValue10, ColorMathUtil.applyAlpha(themeStaticColor, floatValue2));
                        }
                        float floatValue11 = 23.5F + fontValue.getStringWidth(cooldownEntryResult.name());
                        floatValueCopy = Math.max(floatValueCopy, floatValue11 + interpolatedValueValue + 14.0F);
                        floatValueEntry += 10.5F * orDefault;
                    }
                }
            }
            if (flagItem) {
                ScissorStack.unset();
                ScissorStack.pop();
            }
            matrices.pop();
            this.width = this.lerp(this.width, Math.max(77.5F, floatValueCopy), 0.15F);
            this.height = this.lerp(this.height, 18.0F + floatValueEntry + 6.5F, 0.15F);
            this.draggable.setWidth(this.width * 1.1F);
            this.draggable.setHeight(this.height * 1.1F);
        }
    }

    private void onHudRender(HudRenderEvent event, ItemStack stack, float floatValue, float floatValueValue, float floatValueResult) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, floatValueResult);
        try {
            Renderer2D.drawHudItem(event.getContext(), stack, floatValue, floatValueValue, 0.46875F, 0.0F);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(false);
        }
    }

    private int calculateCosWithCurrentTimeMillis(int intValue, float floatValue) {
        int intValueValue = 255;
        if (intValue <= 200 && intValue > 0) {
            double doubleValue = 0.5 + 0.5 * Math.cos((Math.PI * 2) * (System.currentTimeMillis() % 700L) / 700.0);
            intValueValue = (int) (100.0 + 155.0 * doubleValue);
        } else if (intValue == 0) {
            intValueValue = 0;
        }
        return (int) (intValueValue * floatValue);
    }

    public static record CooldownEntry(Identifier group, ItemStack stack, String name, String duration, int durationTicks, int maximumDurationTicks, int startTick) {
    }
}
