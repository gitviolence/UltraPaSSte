package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.PotionsWidget.PotionEntry;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class PotionsWidget extends HudWidgetBase {

    private float width = 77.5F;

    private float height = 62.5F;

    private float cachedScalarForUpdateEffectEntries;

    private final Map<StatusEffect, Float> effectAlpha = new HashMap<>();

    private final Map<StatusEffect, Float> effectVerticalPositions = new HashMap<>();

    private final Map<StatusEffect, Integer> effectPreviousDurations = new HashMap<>();

    private final Map<StatusEffect, PotionEntry> entriesByEffect = new HashMap<>();

    private final List<PotionEntry> activeEntries = new ArrayList<>();

    private final List<PotionEntry> fadingEntries = new ArrayList<>();

    private final Set<StatusEffect> activeEffects = new HashSet<>();

    private final Set<StatusEffect> previousEffects = new HashSet<>();

    private long lastUpdateMillis;

    private RegistryEntry<StatusEffect> speedEffect = StatusEffects.SPEED;

    private static final List<RegistryEntry<StatusEffect>> negativeEffects = List.of( StatusEffects.POISON, StatusEffects.WITHER, StatusEffects.NAUSEA, StatusEffects.BLINDNESS, StatusEffects.HUNGER, StatusEffects.SLOWNESS, StatusEffects.MINING_FATIGUE, StatusEffects.INSTANT_DAMAGE, StatusEffects.WEAKNESS, StatusEffects.LEVITATION, StatusEffects.UNLUCK, StatusEffects.BAD_OMEN);

    public PotionsWidget(Draggable draggable) {
        super(draggable);
    }

    private boolean isNegativeEffect(RegistryEntry<StatusEffect> registryEntry) {
        for (var registryEntryValue : negativeEffects) {
            if (registryEntry == registryEntryValue) {
                return true;
            }
        }
        return false;
    }

    private String formatDuration(StatusEffectInstance statusEffectInstance) {
        int duration = statusEffectInstance.getDuration();
        int intValue = duration / 1200;
        return !statusEffectInstance.isInfinite() && intValue <= 60 ? intValue + ":" + String.format("%02d", duration % 1200 / 20) : "**:**";
    }

    private float lerp(float floatValue, float floatValueValue, float floatValueResult) {
        return floatValue + (floatValueValue - floatValue) * floatValueResult;
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            this.updateEffectEntries(event);
            super.onRender(event);
        }
    }

    private void updateEffectEntries(HudRenderEvent event) {
        MatrixStack matrices = event.getContext().getMatrices();
        this.activeEntries.clear();
        this.activeEffects.clear();
        for (StatusEffectInstance statusEffectInstance : mc != null && mc.player != null ? mc.player.getStatusEffects() : List.<StatusEffectInstance>of()) {
            StatusEffect statusEffect = statusEffectInstance.getEffectType().value();
            var effectType = statusEffectInstance.getEffectType();
            int duration = statusEffectInstance.getDuration();
            PotionEntry potionEntry = this.entriesByEffect.get(statusEffect);
            int orDefault = this.effectPreviousDurations.getOrDefault(statusEffect, duration);
            if (potionEntry == null || duration > potionEntry.durationTicks()) {
                orDefault = duration;
            }
            this.effectPreviousDurations.put(statusEffect, Math.max(1, orDefault));
            String text = statusEffectInstance.getAmplifier() > 0 ? " " + (statusEffectInstance.getAmplifier() + 1) : "";
            PotionEntry potionEntryValue = new PotionEntry(statusEffect, effectType, statusEffect.getName().getString(), text, this.formatDuration(statusEffectInstance), this.isNegativeEffect(effectType), duration, this.effectPreviousDurations.get(statusEffect));
            this.entriesByEffect.put(statusEffect, potionEntryValue);
            this.activeEntries.add(potionEntryValue);
            this.activeEffects.add(statusEffect);
        }
        if (this.activeEntries.isEmpty() && mc != null && mc.currentScreen instanceof ChatScreen) {
            long lastUpdateMillis = System.currentTimeMillis();
            if (lastUpdateMillis - this.lastUpdateMillis >= 1000L) {
                ArrayList<net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect>> values = new ArrayList<>();
                for (Identifier texture : Registries.STATUS_EFFECT.getIds()) {
                    Registries.STATUS_EFFECT.getEntry(texture).ifPresent(values::add);
                }
                if (!values.isEmpty()) {
                    this.speedEffect = values.get(new Random().nextInt(values.size()));
                }
                this.lastUpdateMillis = lastUpdateMillis;
            }
            StatusEffect statusEffectValue = this.speedEffect.value();
            PotionEntry potionEntryResult = new PotionEntry(statusEffectValue, this.speedEffect, "Сила", "", "**:**", false, 99999, 99999);
            this.entriesByEffect.put(statusEffectValue, potionEntryResult);
            this.effectPreviousDurations.put(statusEffectValue, 99999);
            this.activeEntries.add(potionEntryResult);
            this.activeEffects.add(statusEffectValue);
        }
        this.previousEffects.clear();
        this.previousEffects.addAll(this.effectAlpha.keySet());
        this.previousEffects.addAll(this.activeEffects);
        for (StatusEffect statusEffectResult : this.previousEffects) {
            boolean flag = this.activeEffects.contains(statusEffectResult);
            float interpolatedValue = this.lerp(this.effectAlpha.getOrDefault(statusEffectResult, 0.0F), flag ? 1.0F : 0.0F, 0.12F);
            if (!(interpolatedValue > 0.01F) && !flag) {
                this.effectAlpha.remove(statusEffectResult);
                this.effectVerticalPositions.remove(statusEffectResult);
                this.effectPreviousDurations.remove(statusEffectResult);
                this.entriesByEffect.remove(statusEffectResult);
            } else {
                this.effectAlpha.put(statusEffectResult, interpolatedValue);
            }
        }
        boolean empty = this.activeEntries.isEmpty();
        boolean flagValue = this.effectAlpha.values().stream().anyMatch(input -> input > 0.01F);
        boolean flagResult = empty && !flagValue && mc != null && !(mc.currentScreen instanceof ChatScreen);
        this.cachedScalarForUpdateEffectEntries = this.lerp(this.cachedScalarForUpdateEffectEntries, flagResult ? 0.0F : 1.0F, 0.16F);
        float floatValue = Math.max(0.0F, Math.min(1.0F, this.cachedScalarForUpdateEffectEntries));
        if (flagResult && floatValue <= 0.01F) {
            this.draggable.setWidth(85.25F);
            this.draggable.setHeight(68.75F);
        } else {
            this.fadingEntries.clear();
            this.fadingEntries.addAll(this.activeEntries);
            for (var entry : this.effectAlpha.entrySet()) {
                if (!this.activeEffects.contains(entry.getKey()) && entry.getValue() > 0.01F) {
                    PotionEntry potionEntryItem = this.entriesByEffect.get(entry.getKey());
                    if (potionEntryItem != null) {
                        this.fadingEntries.add(potionEntryItem);
                    }
                }
            }
            float floatValueValue = 0.0F;
            for (PotionEntry potionEntryEntry : this.fadingEntries) {
                floatValueValue += 10.5F * this.effectAlpha.getOrDefault(potionEntryEntry.effect(), 0.0F);
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
            int intValueItem = ColorMathUtil.applyAlpha(themeStaticColor, floatValue);
            int intValueEntry = ColorMathUtil.applyAlpha(intValue, floatValue);
            TextRenderer fontItem = FontManager.getFont("hud", 16);
            fontItem.drawGradientStringVertical(matrices, "P", x + width - 7.0F - 7.0F, y + 7.0F, intValueItem, intValueEntry);
            font.drawString(matrices, "Potions", x + 7.0F, y + 6.2F, ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue)));
            if (floatValueValue > 0.5F && !this.fadingEntries.isEmpty()) {
                ScissorStack.push();
                ScissorStack.setFromComponentCoordinates((double) x, (double) (y + (18.0F + floatValueItem) * 1.1F), (double) (width * 1.1F), (double) ((floatValueValue + 3.0F) * 1.1F));
            }
            float floatValueEntry = 0.0F;
            float floatValueCopy = 77.5F;
            for (PotionEntry potionEntryCopy : this.fadingEntries) {
                float orDefaultValue = this.effectAlpha.getOrDefault(potionEntryCopy.effect(), 0.0F);
                if (!(orDefaultValue <= 0.01F)) {
                    float floatValue2 = orDefaultValue * floatValue;
                    float floatValue3 = y + 18.0F + floatValueEntry + 1.0F - (1.0F - orDefaultValue) * 5.0F;
                    if (floatValue2 <= 0.03F) {
                        floatValueEntry += 10.5F * orDefaultValue;
                    } else {
                        float floatValue4 = floatValue3 + 7.0F;
                        int expiringEffectColor = this.getExpiringEffectColor(potionEntryCopy.durationTicks(), floatValue2);
                        int intValueCopy = potionEntryCopy.bad() ? ColorMathUtil.rgba(255, 85, 75, expiringEffectColor) : ColorUtil.rgba(255, 255, 255, expiringEffectColor);
                        int intValue2 = ColorUtil.rgba(150, 150, 150, expiringEffectColor);
                        int intValue3 = potionEntryCopy.bad() ? intValueCopy : ColorMathUtil.rgba(255, 255, 255, expiringEffectColor);
                        Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(potionEntryCopy.entry());
                        float floatValue5 = x + 7.0F;
                        matrices.push();
                        matrices.translate(floatValue5, floatValue4, 0.0F);
                        matrices.scale(1.0F, 1.0F, 1.0F);
                        matrices.translate(-floatValue5, -floatValue4, 0.0F);
                        Renderer2D.drawSprite(matrices, sprite, x + 7.0F + 0.0F, floatValue3 + 3.5F, 7.5F, intValue3);
                        float floatValue6 = x + 7.0F + 11.0F;
                        fontValue.drawString(matrices, potionEntryCopy.name(), floatValue6, floatValue3 + 5.0F, intValueCopy);
                        if (!potionEntryCopy.amplifier().isEmpty()) {
                            fontValue.drawString(matrices, potionEntryCopy.amplifier(), floatValue6 + fontValue.getStringWidth(potionEntryCopy.name()) - 0.8F, floatValue3 + 5.0F, intValue2);
                        }
                        matrices.pop();
                        float renderWidth = fontResult.getRenderWidth(potionEntryCopy.duration());
                        float floatValue7 = Math.max(24.0F, renderWidth + 12.0F);
                        float interpolatedValueValue = this.lerp(this.effectVerticalPositions.getOrDefault(potionEntryCopy.effect(), floatValue7), floatValue7, 0.18F);
                        this.effectVerticalPositions.put(potionEntryCopy.effect(), interpolatedValueValue);
                        float floatValue8 = x + width - 7.0F - interpolatedValueValue;
                        int intValue4 = BackgroundSettings.overlayColor(255, 255, 255, 20, floatValue2);
                        float floatValue9 = floatValue8 + interpolatedValueValue + 1.0F;
                        matrices.push();
                        matrices.translate(floatValue9, floatValue4, 0.0F);
                        matrices.scale(1.0F, 1.0F, 1.0F);
                        matrices.translate(-floatValue9, -floatValue4, 0.0F);
                        Renderer2D.drawRoundedRect(matrices, floatValue8 - 1.0F, floatValue3 + 2.0F, interpolatedValueValue + 2.5F, 10.0F, 3.0F, intValue4);
                        fontResult.drawString(matrices, potionEntryCopy.duration(), floatValue8 + 2.0F, floatValue3 + 5.0F, ColorUtil.rgba(255, 255, 255, expiringEffectColor));
                        float floatValue10 = 7.8F;
                        float floatValue11 = floatValue8 + interpolatedValueValue - floatValue10 - 1.0F;
                        float floatValue12 = floatValue3 + 3.0F;
                        Renderer2D.drawRingArc(matrices, floatValue11, floatValue12, floatValue10, 1.7F, -90.0F, 269.9F, ColorUtil.rgba(255, 255, 255, (int) (8.0F * floatValue2)));
                        float floatValue13 = potionEntryCopy.maximumDurationTicks() <= 0 ? 0.0F : Math.max(0.0F, Math.min(1.0F, (float) potionEntryCopy.durationTicks() / potionEntryCopy.maximumDurationTicks()));
                        if (floatValue13 > 0.002F) {
                            Renderer2D.drawRingArc(matrices, floatValue11, floatValue12, floatValue10, 1.7F, -90.0F, -90.0F + 360.0F * floatValue13, ColorMathUtil.applyAlpha(themeStaticColor, floatValue2));
                        }
                        matrices.pop();
                        float floatValue14 = 23.5F + fontValue.getStringWidth(potionEntryCopy.name() + potionEntryCopy.amplifier());
                        float floatValue15 = floatValue14 + interpolatedValueValue + 14.0F;
                        floatValueCopy = Math.max(floatValueCopy, floatValue15);
                        floatValueEntry += 10.5F * orDefaultValue;
                    }
                }
            }
            if (floatValueValue > 0.5F && !this.fadingEntries.isEmpty()) {
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

    private int getExpiringEffectColor(int intValue, float floatValue) {
        int intValueValue = 255;
        if (intValue <= 200 && intValue > 0) {
            double doubleValue = 0.5 + 0.5 * Math.cos((Math.PI * 2) * (System.currentTimeMillis() % 700L) / 700.0);
            intValueValue = (int) (100.0 + 155.0 * doubleValue);
        } else if (intValue == 0) {
            intValueValue = 0;
        }
        return (int) (intValueValue * floatValue);
    }

    public static record PotionEntry(StatusEffect effect, RegistryEntry<StatusEffect> entry, String name, String amplifier, String duration, boolean bad, int durationTicks, int maximumDurationTicks) {
    }
}
