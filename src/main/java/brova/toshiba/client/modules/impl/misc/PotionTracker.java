package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.misc.PotionTracker.PotionDrop;
import brova.toshiba.client.modules.impl.misc.PotionTracker.PotionInfo;
import brova.toshiba.client.modules.impl.misc.PotionTracker.TrackedPlayer;
import brova.toshiba.client.utils.render.ColorMathUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Box;

public class PotionTracker extends Module {

    public static PotionTracker INSTANCE = new PotionTracker();

    private static final int fromRgbForCreateAccentStyle = new Color(200, 200, 200).getRGB();

    private static final int fromRgbForSendPotionNotification = new Color(235, 235, 235).getRGB();

    private final Map<Integer, PotionDrop> keySetPotionTrackerPotionDropEntries = new HashMap<>();

    private ClientWorld clientWorld;

    public PotionTracker() {
        super("PotionTracker", "Показывает попадание выкинутых зелий по игрокам", ModuleCategory.MISC);
    }

    @Override
    public void onDisable() {
        this.keySetPotionTrackerPotionDropEntries.clear();
        this.clientWorld = null;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.clientWorld != mc.world) {
                this.keySetPotionTrackerPotionDropEntries.clear();
                ClientWorld world = mc.world;
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, world, (accessReceiver, accessValue) -> {
                    accessReceiver.clientWorld = accessValue;
                });
            }
            HashSet<Integer> values = new HashSet<>();
            double doubleValue = 2500.0;
            Box box = mc.player.getBoundingBox().expand(50.0);
            for (PotionEntity potionEntity : mc.world.getEntitiesByClass(PotionEntity.class, box, Entity::isAlive)) {
                if (!(mc.player.squaredDistanceTo(potionEntity) > doubleValue)) {
                    PotionInfo potionTrackerPotionInfo = this.identifyThrownPotion(potionEntity);
                    if (potionTrackerPotionInfo != null) {
                        int id = potionEntity.getId();
                        values.add(id);
                        PotionDrop potionDrop = this.keySetPotionTrackerPotionDropEntries.get(id);
                        if (potionDrop == null) {
                            this.keySetPotionTrackerPotionDropEntries.put(id, new PotionDrop(potionTrackerPotionInfo, potionEntity.getX(), potionEntity.getY(), potionEntity.getZ()));
                        } else {
                            potionDrop.splashX = potionEntity.getX();
                            potionDrop.splashY = potionEntity.getY();
                            potionDrop.splashZ = potionEntity.getZ();
                            potionDrop.potionTrackerPotionInfo = potionTrackerPotionInfo;
                        }
                    }
                }
            }
            HashSet<Integer> valuesValue = new HashSet<>(this.keySetPotionTrackerPotionDropEntries.keySet());
            valuesValue.removeAll(values);
            for (int intValue : valuesValue) {
                PotionDrop potionDropValue = this.keySetPotionTrackerPotionDropEntries.remove(intValue);
                if (potionDropValue != null) {
                    this.trackSplashTargets(potionDropValue);
                }
            }
        } else {
            this.keySetPotionTrackerPotionDropEntries.clear();
            Object value = null;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                accessReceiver.clientWorld = (ClientWorld) accessValue;
            });
        }
    }

    private void trackSplashTargets(PotionDrop potionDrop) {
        Box box = new Box(potionDrop.splashX - 4.0, potionDrop.splashY - 2.0, potionDrop.splashZ - 4.0, potionDrop.splashX + 4.0, potionDrop.splashY + 2.0, potionDrop.splashZ + 4.0);
        ArrayList<TrackedPlayer> values = new ArrayList<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != null && player.isAlive() && box.contains(player.getPos())) {
                double doubleValue = player.getX() - potionDrop.splashX;
                double doubleValueValue = player.getZ() - potionDrop.splashZ;
                double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue);
                if (!(squareRoot > 4.0)) {
                    double doubleValueResult = Math.max(0.0, 1.0 - squareRoot / 4.0);
                    int intValue = Math.max(1, Math.min(100, (int) Math.round(doubleValueResult * 100.0)));
                    values.add(new TrackedPlayer(player.getName().getString(), intValue, squareRoot));
                }
            }
        }
        values.sort(Comparator.comparingDouble(TrackedPlayer::distance));
        for (int index = 0; index < Math.min(4, values.size()); index++) {
            TrackedPlayer trackedPlayer = values.get(index);
            this.sendPotionNotification(trackedPlayer.playerName(), potionDrop.potionTrackerPotionInfo, trackedPlayer.percent());
        }
    }

    private PotionInfo identifyThrownPotion(PotionEntity potionEntity) {
        PotionContentsComponent potionContentsComponent = potionEntity.getStack().get(DataComponentTypes.POTION_CONTENTS);
        PotionInfo potionInfo = this.identifyPotionContents(potionContentsComponent);
        return potionInfo != null ? potionInfo : this.identifyPotionName(potionEntity.getStack().getName().getString());
    }

    private PotionInfo identifyPotionContents(PotionContentsComponent potionContentsComponent) {
        if (potionContentsComponent != null && potionContentsComponent.hasEffects()) {
            boolean effectAmplifierPresent = this.hasEffectAmplifier(potionContentsComponent, StatusEffects.REGENERATION, 1);
            boolean effectAmplifierPresentValue = this.hasEffectAmplifier(potionContentsComponent, StatusEffects.STRENGTH, 4);
            boolean effectAmplifierPresentResult = this.hasEffectAmplifier(potionContentsComponent, StatusEffects.HEALTH_BOOST, 2);
            boolean effectAmplifierPresentItem = this.hasEffectAmplifier(potionContentsComponent, StatusEffects.STRENGTH, 3);
            boolean effectAmplifierPresentEntry = this.hasEffectAmplifier(potionContentsComponent, StatusEffects.SPEED, 2);
            if (effectAmplifierPresent) {
                return PotionInfo.HOLY_WATER;
            } else if (effectAmplifierPresentValue) {
                return PotionInfo.WRATH;
            } else if (effectAmplifierPresentResult) {
                return PotionInfo.PALADIN;
            } else if (effectAmplifierPresentItem && effectAmplifierPresentEntry) {
                return PotionInfo.ASSASSIN;
            } else {
                return effectAmplifierPresentItem ? PotionInfo.ASSASSIN : null;
            }
        } else {
            return null;
        }
    }

    private boolean hasEffectAmplifier(PotionContentsComponent potionContentsComponent, RegistryEntry<StatusEffect> registryEntry, int intValue) {
        for (StatusEffectInstance statusEffectInstance : potionContentsComponent.getEffects()) {
            if (statusEffectInstance.getEffectType().equals(registryEntry) && statusEffectInstance.getAmplifier() == intValue) {
                return true;
            }
        }
        return false;
    }

    private PotionInfo identifyPotionName(String text) {
        String textValue = this.normalizePotionName(text);
        for (PotionInfo potionInfo : brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> PotionInfo.values())) {
            if (textValue.contains(this.normalizePotionName(potionInfo.getDisplayName()))) {
                return potionInfo;
            }
        }
        return null;
    }

    private String normalizePotionName(String text) {
        return text.replaceAll("В§.", "").replace("[", "").replace("]", "").replace("✦", "").toLowerCase(Locale.ROOT).trim();
    }

    private void sendPotionNotification(String text, PotionInfo potionInfo, int intValue) {
        if (mc.player != null) {
            MutableText mutableText = Text.literal("");
            mutableText.append(this.createGradientText("toshiba", ColorMathUtil.getThemeColor(0), ColorMathUtil.getThemeColor(90), true));
            mutableText.append(Text.literal(" ⇒ ").setStyle(this.createAccentStyle()));
            mutableText.append(Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(fromRgbForSendPotionNotification))));
            mutableText.append(Text.literal(" получил ").setStyle(this.createAccentStyle()));
            mutableText.append(this.createGradientText(potionInfo.indexOfText, potionInfo.displayColor, potionInfo.effectDurationTicks, true));
            mutableText.append(Text.literal(" " + intValue + "%").setStyle(this.createAccentStyle()));
            mc.player.sendMessage(mutableText, false);
        }
    }

    private MutableText createGradientText(String text, int intValue, int intValueValue, boolean flag) {
        MutableText mutableText = Text.literal("");
        for (int index = 0; index < text.length(); index++) {
            float floatValue = text.length() <= 1 ? 0.0F : (float) index / (text.length() - 1);
            int intValueResult = ColorMathUtil.gradient(intValue, intValueValue, floatValue);
            mutableText.append(Text.literal(String.valueOf(text.charAt(index))).setStyle(Style.EMPTY.withBold(flag).withColor(TextColor.fromRgb(intValueResult))));
        }
        return mutableText;
    }

    private Style createAccentStyle() {
        return Style.EMPTY.withColor(TextColor.fromRgb(fromRgbForCreateAccentStyle));
    }

    public static record TrackedPlayer(String playerName, int percent, double distance) {
    }

    public static class PotionDrop {

        PotionInfo potionTrackerPotionInfo;

        double splashX;

        double splashY;

        double splashZ;

        PotionDrop(PotionInfo potionTrackerPotionInfo, double splashX, double splashY, double splashZ) {
            this.potionTrackerPotionInfo = potionTrackerPotionInfo;
            this.splashX = splashX;
            this.splashY = splashY;
            this.splashZ = splashZ;
        }
    }

    public enum PotionInfo {

        HOLY_WATER("[✦] Святая вода", 16774507, 12123970), WRATH("[✦] Зелье Гнева", 12849682, 16757051), PALADIN("[✦] Зелье Паладина", 12123970, 16773280), ASSASSIN("[✦] Зелье Ассасина", 5592405, 11545130);

        final String indexOfText;

        final int displayColor;

        final int effectDurationTicks;

        private PotionInfo(String nullxx, int nullxxx, int nullxxxx) {
            this.indexOfText = nullxx;
            this.displayColor = nullxxx;
            this.effectDurationTicks = nullxxxx;
        }

        String getDisplayName() {
            int index = this.indexOfText.indexOf("] ");
            return index >= 0 ? this.indexOfText.substring(index + 2) : this.indexOfText;
        }
    }
}
