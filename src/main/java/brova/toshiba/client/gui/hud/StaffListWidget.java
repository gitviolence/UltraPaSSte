package brova.toshiba.client.gui.hud;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.StaffListWidget.StaffAnimation;
import brova.toshiba.client.gui.hud.StaffListWidget.StaffEntry;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.RankGlyphReplacement;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class StaffListWidget extends HudWidgetBase {

    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private final Map<String, StaffAnimation> staffListWidgetStaffAnimationEntries = new LinkedHashMap<>();

    private final Map<String, Float> orDefaultFloatEntries = new HashMap<>();

    private final Map<String, Float> floatEntries = new HashMap<>();

    private final Set<String> addAllStringEntriesForUpdateEqualsWithName = new HashSet<>();

    private final Map<String, Long> putIfAbsentLongEntries = new HashMap<>();

    private final Pattern pattern = Pattern.compile("^\\w{3,16}$");

    private final Set<String> addAllStringEntriesForToLowerCase = new HashSet<>();

    private float width = 70.0F;

    private float height = 17.0F;

    private float cachedScalarForHudRender = 0.0F;

    private long lastUpdateTimestamp = 0L;

    private final List<String> sortStringEntries = new ArrayList<>();

    private final Set<String> addAllStringEntriesForHudRender = new HashSet<>();

    private TextRenderer robotoFlexMediumFont15;

    public StaffListWidget(Draggable draggable) {
        super(draggable);
        this.addAllStringEntriesForToLowerCase.addAll(Arrays.asList("supp", "ꜱupp", "mod", "der", "adm", "wne", "мод", "помо", "адм", "владе", "отри", "таф", "taf", "curat", "курато", "dev", "раз", "сапп", "yt", "ютуб", "стажер", "сотрудник"));
    }

    private void updateLocalState() {
        if (this.robotoFlexMediumFont15 == null) {
            this.robotoFlexMediumFont15 = FontManager.getFont("roboto_flex_medium", 15);
        }
    }

    private String formatTextFromFormatWithCurrentTimeMillis(long longValue) {
        long elapsedMillis = System.currentTimeMillis() - longValue;
        long longValueValue = elapsedMillis / 1000L;
        long longValueResult = longValueValue / 60L;
        long longValueItem = longValueResult / 60L;
        return longValueItem > 0L ? String.format("%02d:%02d:%02d", longValueItem, longValueResult % 60L, longValueValue % 60L) : longValueResult + ":" + String.format("%02d", longValueValue % 60L);
    }

    private float calculateFloat(float floatValue, float floatValueValue, float floatValueResult) {
        float floatValueItem = floatValueValue - floatValue;
        return floatValue + floatValueItem * floatValueResult;
    }

    @Override
    public void onRender(HudRenderEvent event) {
        if (this.minecraftClient.player != null && this.minecraftClient.world != null) {
            this.updateLocalState();
            long lastUpdateTimestamp = System.currentTimeMillis();
            if (lastUpdateTimestamp - this.lastUpdateTimestamp > 500L) {
                this.updateEqualsWithName();
                this.lastUpdateTimestamp = lastUpdateTimestamp;
            }
            this.onHudRender(event);
            super.onRender(event);
        }
    }

    private boolean isToLowerCase(String text) {
        String normalizedText = text.toLowerCase(Locale.ROOT);
        for (String textValue : this.addAllStringEntriesForToLowerCase) {
            if (normalizedText.contains(textValue)) {
                return true;
            }
        }
        return false;
    }

    private List<StaffEntry> createListFromColorWithEmpty(Text text) {
        ArrayList<StaffEntry> values = new ArrayList<>();
        text.visit((input, inputValue) -> {
            if (inputValue != null && !inputValue.isEmpty()) {
                this.updateCharAtWithCodePointAt(values, inputValue, input.getColor() != null ? input.getColor().getRgb() : 16777215);
                return Optional.empty();
            } else {
                return Optional.empty();
            }
        }, Style.EMPTY);
        return values;
    }

    private void updateCharAtWithCodePointAt(List<StaffEntry> staffEntries, String text, int intValue) {
        int intValueValue = intValue;
        StringBuilder textBuilder = new StringBuilder();
        int intValueResult = intValue;
        int intValueItem = 0;
        while (intValueItem < text.length()) {
            int intValueEntry = text.codePointAt(intValueItem);
            int intValueCopy = Character.charCount(intValueEntry);
            if (intValueEntry == 167 && intValueItem + intValueCopy < text.length()) {
                this.updateToStringWithLength(staffEntries, textBuilder, intValueResult);
                char normalizedText = Character.toLowerCase(text.charAt(intValueItem + intValueCopy));
                Integer value = this.createIntegerFromChar(normalizedText);
                intValueValue = value != null ? value : (normalizedText == 'r' ? intValue : intValueValue);
                intValueResult = intValueValue;
                intValueItem += intValueCopy + 1;
            } else {
                String textValue = RankGlyphReplacement.replaceCodePoint(intValueEntry);
                if (textValue == null) {
                    if (textBuilder.length() > 0 && intValueResult != intValueValue) {
                        this.updateToStringWithLength(staffEntries, textBuilder, intValueResult);
                    }
                    intValueResult = intValueValue;
                    textBuilder.appendCodePoint(intValueEntry);
                    intValueItem += intValueCopy;
                } else {
                    this.updateToStringWithLength(staffEntries, textBuilder, intValueResult);
                    int intValue2 = Math.max(1, textValue.length());
                    for (int index = 0; index < textValue.length(); index++) {
                        int gradientColorForReplacement = RankGlyphReplacement.getGradientColorForReplacement(intValueEntry, index, intValue2, 1.0F, intValueValue);
                        if (textBuilder.length() > 0 && intValueResult != gradientColorForReplacement) {
                            this.updateToStringWithLength(staffEntries, textBuilder, intValueResult);
                        }
                        intValueResult = gradientColorForReplacement;
                        textBuilder.append(textValue.charAt(index));
                    }
                    intValueItem += intValueCopy;
                }
            }
        }
        this.updateToStringWithLength(staffEntries, textBuilder, intValueResult);
    }

    private void updateToStringWithLength(List<StaffEntry> staffEntries, StringBuilder textBuilder, int intValue) {
        if (!textBuilder.isEmpty()) {
            String text = textBuilder.toString();
            StaffEntry staffEntry = new StaffEntry(text, intValue);
            staffEntries.add(staffEntry);
            textBuilder.setLength(0);
        }
    }

    private Integer createIntegerFromChar(char character) {
        return switch(character) {
            case '0' ->
                0;
            case '1' ->
                170;
            case '2' ->
                43520;
            case '3' ->
                43690;
            case '4' ->
                11141120;
            case '5' ->
                11141290;
            case '6' ->
                16755200;
            case '7' ->
                11184810;
            case '8' ->
                5592405;
            case '9' ->
                5592575;
            default ->
                null;
            case 'a' ->
                5635925;
            case 'b' ->
                5636095;
            case 'c' ->
                16733525;
            case 'd' ->
                16733695;
            case 'e' ->
                16777045;
            case 'f' ->
                16777215;
        };
    }

    private void updateEqualsWithName() {
        this.addAllStringEntriesForUpdateEqualsWithName.clear();
        String string = this.minecraftClient.player.getName().getString();
        for (Team team : this.minecraftClient.world.getScoreboard().getTeams()) {
            var playerList = team.getPlayerList();
            if (playerList.size() == 1) {
                String text = playerList.iterator().next();
                if (this.pattern.matcher(text).matches() && !text.equals(string)) {
                    PlayerListEntry playerListEntry = this.minecraftClient.getNetworkHandler().getPlayerListEntry(text);
                    boolean flag = playerListEntry == null;
                    boolean flagValue = playerListEntry != null && playerListEntry.getGameMode() == GameMode.SPECTATOR;
                    Text prefix = team.getPrefix();
                    String prefixValue = prefix.getString();
                    boolean toLowerCase = this.isToLowerCase(prefixValue);
                    boolean staff = Toshiba.INSTANCE.staffStorage.isStaff(text);
                    if (toLowerCase || flag || flagValue || staff) {
                        this.addAllStringEntriesForUpdateEqualsWithName.add(text);
                        this.putIfAbsentLongEntries.putIfAbsent(text, System.currentTimeMillis());
                        String playerName;
                        if (flag) {
                            playerName = "VANISH";
                        } else if (flagValue) {
                            playerName = "GM3";
                        } else {
                            playerName = "ONLINE";
                        }
                        StaffAnimation staffAnimation = this.staffListWidgetStaffAnimationEntries.get(text);
                        boolean flagResult = staffAnimation == null;
                        if (staffAnimation == null) {
                            staffAnimation = new StaffAnimation(playerName);
                            this.staffListWidgetStaffAnimationEntries.put(text, staffAnimation);
                        }
                        if (flagResult || !playerName.equals(staffAnimation.playerName) || !prefixValue.equals(staffAnimation.prefix)) {
                            staffAnimation.playerName = playerName;
                            staffAnimation.prefix = prefixValue;
                            staffAnimation.staffListWidgetStaffEntryEntries = this.createListFromColorWithEmpty(prefix);
                        }
                    }
                }
            }
        }
        for (PlayerListEntry playerListEntryValue : this.minecraftClient.getNetworkHandler().getPlayerList()) {
            String name = playerListEntryValue.getProfile().getName();
            if (!name.equals(string) && this.pattern.matcher(name).matches() && !this.addAllStringEntriesForUpdateEqualsWithName.contains(name) && Toshiba.INSTANCE.staffStorage.isStaff(name)) {
                this.addAllStringEntriesForUpdateEqualsWithName.add(name);
                this.putIfAbsentLongEntries.putIfAbsent(name, System.currentTimeMillis());
                String playerNameValue = playerListEntryValue.getGameMode() == GameMode.SPECTATOR ? "GM3" : "ONLINE";
                StaffAnimation staffAnimationValue = this.staffListWidgetStaffAnimationEntries.get(name);
                if (staffAnimationValue == null) {
                    staffAnimationValue = new StaffAnimation(playerNameValue);
                    staffAnimationValue.staffListWidgetStaffEntryEntries = new ArrayList<>();
                    this.staffListWidgetStaffAnimationEntries.put(name, staffAnimationValue);
                } else {
                    staffAnimationValue.playerName = playerNameValue;
                }
            }
        }
    }

    private float calculateSizeWithCharAt(TextRenderer textRenderer, String text) {
        if (text != null && !text.isEmpty()) {
            float floatValue = 0.0F;
            float floatValueValue = textRenderer.getSize() * 0.5F;
            float floatValueResult = (floatValueValue - 1.0F) / (floatValueValue - 0.5F);
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                if (character == 167 && index + 1 < text.length()) {
                    index++;
                } else {
                    String textValue = String.valueOf(character);
                    float stringWidth = textRenderer.getStringWidth(textValue);
                    if (Character.isWhitespace(character)) {
                        floatValue += stringWidth;
                    } else {
                        floatValue += stringWidth * floatValueResult;
                    }
                }
            }
            return floatValue;
        } else {
            return 0.0F;
        }
    }

    private int calculateText(String text) {
        return switch(text) {
            case "VANISH" ->
                -47526;
            case "GM3" ->
                -9146;
            default ->
                -10158216;
        };
    }

    private boolean isToLowerCase(StaffAnimation staffAnimation) {
        String normalizedText = staffAnimation.prefix.toLowerCase(Locale.ROOT);
        return normalizedText.contains("admin") || normalizedText.contains("moder") || normalizedText.contains("админ") || normalizedText.contains("модер");
    }

    private void onHudRender(HudRenderEvent event) {
        MatrixStack matrices = event.getContext().getMatrices();
        this.addAllStringEntriesForHudRender.clear();
        this.addAllStringEntriesForHudRender.addAll(this.orDefaultFloatEntries.keySet());
        this.addAllStringEntriesForHudRender.addAll(this.addAllStringEntriesForUpdateEqualsWithName);
        for (String text : this.addAllStringEntriesForHudRender) {
            boolean flag = this.addAllStringEntriesForUpdateEqualsWithName.contains(text);
            float orDefault = this.orDefaultFloatEntries.getOrDefault(text, 0.0F);
            float floatValue = flag ? 1.0F : 0.0F;
            float floatValueValue = this.calculateFloat(orDefault, floatValue, 0.12F);
            if (!(floatValueValue > 0.01F) && !flag) {
                this.orDefaultFloatEntries.remove(text);
                this.floatEntries.remove(text);
                this.staffListWidgetStaffAnimationEntries.remove(text);
                this.putIfAbsentLongEntries.remove(text);
            } else {
                this.orDefaultFloatEntries.put(text, floatValueValue);
            }
        }
        boolean empty = this.addAllStringEntriesForUpdateEqualsWithName.isEmpty();
        float floatValueResult = empty ? 0.0F : 1.0F;
        this.cachedScalarForHudRender = this.calculateFloat(this.cachedScalarForHudRender, floatValueResult, 0.16F);
        if (empty && this.cachedScalarForHudRender <= 0.03F) {
            this.draggable.setWidth(70.0F);
            this.draggable.setHeight(17.0F);
        } else {
            int themeStaticColor = ColorMathUtil.getThemeStaticColor();
            float floatValueItem = 7.0F;
            float floatValueEntry = 18.0F;
            float floatValueCopy = 77.5F;
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            float width = this.width;
            float height = this.height;
            float floatValue2 = 1.1F;
            float floatValue3 = Math.max(0.0F, Math.min(1.0F, this.cachedScalarForHudRender));
            if (empty && floatValue3 <= 0.03F) {
                this.draggable.setWidth(70.0F);
                this.draggable.setHeight(17.0F);
            } else {
                float floatValue4 = empty ? (1.0F - floatValue3) * -6.0F : 0.0F;
                matrices.push();
                matrices.translate(x, y, 0.0F);
                matrices.scale(floatValue2, floatValue2, 1.0F);
                matrices.translate(-x, -y, 0.0F);
                matrices.translate(0.0F, floatValue4, 0.0F);
                this.sortStringEntries.clear();
                for (var entry : this.orDefaultFloatEntries.entrySet()) {
                    if (entry.getValue() > 0.01F) {
                        this.sortStringEntries.add(entry.getKey());
                    }
                }
                Collections.sort(this.sortStringEntries);
                float floatValue5 = 0.0F;
                for (String textValue : this.sortStringEntries) {
                    float orDefaultValue = this.orDefaultFloatEntries.getOrDefault(textValue, 0.0F);
                    if (orDefaultValue > 0.01F) {
                        floatValue5 += 9.5F * orDefaultValue;
                    }
                }
                float floatValue6 = 0.0F;
                float floatValue7 = floatValueEntry + floatValue5 + floatValueItem * 0.5F;
                int intValue = BackgroundSettings.backgroundColor(18, 18, 18, floatValue3);
                int intValueValue = BackgroundSettings.overlayColor(0, 0, 0, 38, floatValue3);
                if (floatValue3 > 0.03F) {
                    Renderer2D.drawHudShadow(matrices, x, y, width, floatValue7, 7.0F, 2.0F, ColorMathUtil.rgba(0, 0, 0, (int) (90.0F * floatValue3)));
                    Renderer2D.drawHudBlur(matrices, x + 0.5F, y + 0.5F, width - 1.0F, floatValue7 - 1.0F, 6.5F, floatValue3);
                }
                Renderer2D.drawRoundedRect(matrices, x, y, width, floatValue7, 7.0F, intValue);
                Renderer2D.drawRoundedRect(matrices, x, y, width, floatValueEntry, 7.0F, 0.0F, 7.0F, 0.0F, intValueValue);
                int intValueResult = ColorMathUtil.rgba(ColorMathUtil.getRed(themeStaticColor), ColorMathUtil.getGreen(themeStaticColor), ColorMathUtil.getBlue(themeStaticColor), (int) (255.0F * floatValue3));
                float floatValue8 = x + width - floatValueItem - 8.5F;
                FontManager.getFont("toshiba", 16).drawGradientStringVertical(matrices, "d", floatValue8, y + 6.7F, ColorMathUtil.applyAlpha(themeStaticColor, floatValue3), ColorMathUtil.applyAlpha(ColorMathUtil.darken(themeStaticColor, 0.55F), floatValue3));
                FontManager.getFont("roboto_flex_semibold", 15).drawString(matrices, "Staff", x + floatValueItem, y + 6.2F, ColorUtil.rgba(255, 255, 255, (int) (255.0F * floatValue3)));
                float floatValue9 = 0.0F;
                float floatValue10 = floatValueCopy;
                if (floatValue5 > 0.5F && !this.sortStringEntries.isEmpty()) {
                    ScissorStack.push();
                    ScissorStack.setFromComponentCoordinates((double) x, (double) (y + floatValueEntry + floatValue6), (double) width, (double) (floatValue5 + 1.0F));
                }
                for (String textResult : this.sortStringEntries) {
                    float orDefaultResult = this.orDefaultFloatEntries.getOrDefault(textResult, 0.0F);
                    if (!(orDefaultResult <= 0.01F)) {
                        StaffAnimation staffAnimation = this.staffListWidgetStaffAnimationEntries.get(textResult);
                        if (staffAnimation != null) {
                            float floatValue11 = y + floatValueEntry + floatValue6 + floatValue9 - (1.0F - orDefaultResult) * 5.0F;
                            float floatValue12 = orDefaultResult * floatValue3;
                            if (floatValue12 <= 0.03F) {
                                floatValue9 += 9.5F * orDefaultResult;
                            } else {
                                int intValueItem = (int) (255.0F * floatValue12);
                                int intValueEntry = ColorUtil.rgba(255, 255, 255, intValueItem);
                                float floatValue13 = floatValue11 + 7.0F;
                                float floatValue14 = x + floatValueItem;
                                matrices.push();
                                matrices.translate(floatValue14, floatValue13, 0.0F);
                                matrices.scale(0.9F, 0.9F, 1.0F);
                                matrices.translate(-floatValue14, -floatValue13, 0.0F);
                                Renderer2D.drawPlayerHead(matrices, textResult, x + floatValueItem, floatValue11 + 3.0F, 8.5F, 1.0F, floatValue12, 0.0F);
                                float floatValue15 = x + floatValueItem + 12.0F;
                                float floatValue16 = 0.0F;
                                for (StaffEntry staffEntry : staffAnimation.staffListWidgetStaffEntryEntries) {
                                    int intValueCopy = this.isToLowerCase(staffAnimation) ? ColorUtil.rgba(255, 255, 255, (int) (102.0F * floatValue12)) : ColorUtil.neutralColor(ColorMathUtil.setAlphaColor(staffEntry.alphaColor, intValueItem));
                                    this.robotoFlexMediumFont15.drawString(matrices, staffEntry.stringText, floatValue15 - 2.0F + floatValue16, floatValue11 + 4.5F, intValueCopy);
                                    floatValue16 += this.calculateSizeWithCharAt(this.robotoFlexMediumFont15, staffEntry.stringText);
                                }
                                this.robotoFlexMediumFont15.drawString(matrices, textResult, floatValue15 - 2.0F + floatValue16, floatValue11 + 4.5F, intValueEntry);
                                matrices.pop();
                                String textItem = staffAnimation.playerName.equals("ONLINE") ? "Online" : "Spec";
                                int intValue2 = staffAnimation.playerName.equals("ONLINE") ? -10158216 : -47526;
                                TextRenderer font = FontManager.getFont("roboto_flex_semibold", 15);
                                float renderWidth = font.getRenderWidth(textItem);
                                float floatValue17 = Math.max(24.0F, renderWidth + 6.0F);
                                float floatValue18 = x + width - floatValueItem - floatValue17;
                                float floatValue19 = floatValue18 + floatValue17 + 1.0F;
                                matrices.push();
                                matrices.translate(floatValue19, floatValue13, 0.0F);
                                matrices.scale(0.9F, 0.9F, 1.0F);
                                matrices.translate(-floatValue19, -floatValue13, 0.0F);
                                Renderer2D.drawRoundedRect(matrices, floatValue18 - 1.0F, floatValue11 + 2.0F, floatValue17 + 2.5F, 10.0F, 3.0F, BackgroundSettings.overlayColor(255, 255, 255, 5, floatValue12));
                                font.drawString(matrices, textItem, floatValue18 + 2.0F, floatValue11 + 5.0F, ColorMathUtil.setAlphaColor(intValue2, intValueItem));
                                matrices.pop();
                                float floatValue20 = floatValueItem + 12.0F + floatValue16 + this.calculateSizeWithCharAt(this.robotoFlexMediumFont15, textResult);
                                float floatValue21 = floatValue20 + floatValue17 + floatValueItem * 3.0F;
                                if (floatValue21 > floatValue10) {
                                    floatValue10 = floatValue21;
                                }
                                floatValue9 += 9.5F * orDefaultResult;
                            }
                        }
                    }
                }
                if (floatValue5 > 0.5F && !this.sortStringEntries.isEmpty()) {
                    ScissorStack.unset();
                    ScissorStack.pop();
                }
                matrices.pop();
                float floatValue22 = floatValueEntry + floatValue6 + floatValue9 + floatValueItem * 0.5F;
                this.width = this.calculateFloat(this.width, Math.max(floatValueCopy, floatValue10), 0.15F);
                this.height = this.calculateFloat(this.height, floatValue22, 0.15F);
                this.draggable.setWidth(this.width * floatValue2);
                this.draggable.setHeight(this.height * floatValue2);
            }
        }
    }

    public static class StaffAnimation {

        public String playerName;

        public String prefix = "";

        public List<StaffEntry> staffListWidgetStaffEntryEntries;

        public StaffAnimation(String playerName) {
            this.playerName = playerName;
            this.staffListWidgetStaffEntryEntries = new ArrayList<>();
        }
    }

    public static class StaffEntry {

        public final String stringText;

        public final int alphaColor;

        public StaffEntry(String stringText, int alphaColor) {
            this.stringText = stringText;
            this.alphaColor = alphaColor;
        }
    }
}
