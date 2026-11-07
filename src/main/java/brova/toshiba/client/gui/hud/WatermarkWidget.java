package brova.toshiba.client.gui.hud;

import brova.toshiba.Toshiba;
import brova.toshiba.client.data.LoaderUserInfo;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.WatermarkWidget.TextTransition;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;

public class WatermarkWidget extends HudWidgetBase {

    private boolean showFps = true;

    private boolean showMs = true;

    private boolean showServer = true;

    private boolean showTps = true;

    private final long creationMillis = System.currentTimeMillis();

    private final TextTransition unusedFpsTransition = new TextTransition();

    private final TextTransition unusedPingTransition = new TextTransition();

    private final TextTransition unusedTpsTransition = new TextTransition();

    private final TextTransition unusedServerTransition = new TextTransition();

    private TextRenderer rainIconFont;

    private TextRenderer topgorobIconFont;

    private TextRenderer textRenderer;

    private TextRenderer bodyTextFont;

    private TextRenderer legacyIconFont;

    private TextRenderer alternateIconFont;

    private TextRenderer brandLogoFont;

    private TextRenderer usernameIconFont;

    private TextRenderer fpsIconFont;

    private TextRenderer serverIconFont;

    private TextRenderer getBrandTitleFont() {
        return FontManager.getFont("toshiba", 17);
    }

    private TextRenderer getSmallInfoFont() {
        return FontManager.getFont("toshiba", 14);
    }

    private TextRenderer getPrimaryInfoFont() {
        return FontManager.getFont("toshiba", 15);
    }

    private TextRenderer getSecondaryInfoFont() {
        return FontManager.getFont("toshiba", 15);
    }

    private TextRenderer getLargeIconFont() {
        return FontManager.getFont("toshiba", 19);
    }

    public static String getUsername() {
        return LoaderUserInfo.getUserName();
    }

    public static String getUID() {
        return LoaderUserInfo.getUID();
    }

    public WatermarkWidget(Draggable draggable) {
        super(draggable);
    }

    private void updateLocalState() {
        if (this.rainIconFont == null) {
            this.rainIconFont = FontManager.getFont("rain", 15);
        }
        if (this.topgorobIconFont == null) {
            this.topgorobIconFont = FontManager.getFont("topgorob", 15);
        }
        this.bodyTextFont = FontManager.getFont("roboto_flex_medium", 15);
        if (this.legacyIconFont == null) {
            this.legacyIconFont = FontManager.getFont("icon", 15);
        }
        if (this.alternateIconFont == null) {
            this.alternateIconFont = FontManager.getFont("iconnew", 15);
        }
        this.brandLogoFont = FontManager.getFont("logo", 22);
        this.usernameIconFont = FontManager.getFont("watermark", 13);
        this.fpsIconFont = FontManager.getFont("watermark", 12);
        this.serverIconFont = FontManager.getFont("watermark", 10);
    }

    public boolean isShowFps() {
        return this.showFps;
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
    }

    public boolean isShowMs() {
        return this.showMs;
    }

    public void setShowMs(boolean showMs) {
        this.showMs = showMs;
    }

    public boolean isShowServer() {
        return this.showServer;
    }

    public void setShowServer(boolean showServer) {
        this.showServer = showServer;
    }

    public boolean isShowTps() {
        return this.showTps;
    }

    public void setShowTps(boolean showTps) {
        this.showTps = showTps;
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            this.DefaultStyle(event);
            super.onRender(event);
        }
    }

    public void DefaultStyle(HudRenderEvent event) {
        MatrixStack matrices = event.getContext().getMatrices();
        float x = this.draggable.getX();
        float y = this.draggable.getY();
        this.updateLocalState();
        TextRenderer bodyTextFont = this.bodyTextFont;
        if (bodyTextFont != null && this.brandLogoFont != null && this.usernameIconFont != null && this.fpsIconFont != null && this.serverIconFont != null) {
            matrices.push();
            matrices.translate(x, y, 0.0F);
            matrices.scale(1.1F, 1.1F, 1.0F);
            matrices.translate(-x, -y, 0.0F);
            int intValue = mc != null ? mc.getCurrentFps() : 0;
            float floatValue = this.calculateTPS();
            String username = getUsername();
            String text = String.valueOf(intValue);
            String textValue = String.valueOf(this.calculateNetworkHandlerWithPlayerListEntry());
            String textResult = String.valueOf(Math.round(floatValue));
            String textItem = this.createIndexOfWithSubstring(this.createTextFromCurrentServerEntry());
            LocalTime localTime = LocalTime.now();
            String textEntry = String.format("%02d", localTime.getHour());
            String textCopy = String.format("%02d", localTime.getMinute());
            float stringWidth = bodyTextFont.getStringWidth(textValue + "ms");
            float floatValueValue = x + 35.5F;
            float floatValueResult = floatValueValue + bodyTextFont.getStringWidth(username) + 4.5F;
            float floatValueItem = floatValueResult + 7.0F;
            float floatValueEntry = floatValueItem + 8.5F;
            float floatValueCopy = floatValueEntry + bodyTextFont.getStringWidth(text + "fps") + 4.3F;
            float floatValue2 = floatValueCopy + 7.0F;
            float floatValue3 = floatValue2 + 8.5F;
            float floatValue4 = floatValue3 - x + stringWidth + 7.0F;
            float floatValue5 = x + 14.5F;
            float floatValue6 = floatValue5 + bodyTextFont.getStringWidth(textItem) + 4.5F;
            float floatValue7 = floatValue6 + 6.0F;
            float floatValue8 = floatValue7 + 9.5F;
            float floatValue9 = floatValue8 + bodyTextFont.getStringWidth(textResult + "tps") + 4.3F;
            float floatValue10 = floatValue9 + 7.0F;
            float floatValue11 = floatValue10 + 8.5F;
            float floatValue12 = floatValue11 - x + bodyTextFont.getStringWidth(textEntry + ":" + textCopy) + 7.0F;
            float floatValue13 = 18.0F;
            float floatValue14 = 7.0F;
            int intValueValue = BackgroundSettings.backgroundColor(18, 18, 18, 1.0F);
            int intValueResult = ColorMathUtil.rgba(0, 0, 0, 80);
            int intValueItem = ColorUtil.rgba(255, 255, 255, 255);
            int intValueEntry = ColorUtil.rgba(255, 255, 255, 102);
            int themeStaticColor = ColorMathUtil.getThemeStaticColor();
            int intValueCopy = ColorMathUtil.darken(themeStaticColor, 0.55F);
            int intValue2 = ColorUtil.rgba(255, 255, 255, 38);
            float floatValue15 = y + 23.0F;
            Renderer2D.drawHudShadow(matrices, x, y, floatValue4, floatValue13, floatValue14, 2.5F, intValueResult);
            Renderer2D.drawHudBlur(matrices, x + 0.5F, y + 0.5F, floatValue4 - 1.0F, floatValue13 - 1.0F, floatValue14 - 0.5F, 1.0F);
            Renderer2D.drawRoundedRect(matrices, x, y, floatValue4, floatValue13, floatValue14, intValueValue);
            Renderer2D.drawGradientRect(matrices, x, y, 21.0F, floatValue13, 7.0F, 7.0F, 0.0F, 0.0F, BackgroundSettings.overlayColor(153, 155, 255, 13, 1.0F), BackgroundSettings.overlayColor(153, 155, 255, 13, 1.0F), BackgroundSettings.overlayColor(111, 113, 212, 13, 1.0F), BackgroundSettings.overlayColor(111, 113, 212, 13, 1.0F), true);
            this.brandLogoFont.drawGradientStringVertical(matrices, "A", x + 5.0F, y + 6.0F, themeStaticColor, intValueCopy);
            this.usernameIconFont.drawGradientStringVertical(matrices, "P", x + 27.0F, y + 8.5F, themeStaticColor, intValueCopy);
            bodyTextFont.drawString(matrices, username, floatValueValue, y + 7.0F, intValueItem);
            Renderer2D.drawPixelRect(matrices, floatValueResult, y + 5.0F, 0.5F, 8.0F, intValue2);
            this.fpsIconFont.drawGradientStringVertical(matrices, "F", floatValueItem, y + 8.5F, themeStaticColor, intValueCopy);
            bodyTextFont.drawString(matrices, text, floatValueEntry, y + 7.0F, intValueItem);
            bodyTextFont.drawString(matrices, "fps", floatValueEntry + bodyTextFont.getStringWidth(text), y + 7.0F, intValueEntry);
            Renderer2D.drawPixelRect(matrices, floatValueCopy, y + 5.0F, 0.5F, 8.0F, intValue2);
            FontManager.getFont("watermark", 13).drawGradientStringVertical(matrices, "Q", floatValue2, y + 8.3F, themeStaticColor, intValueCopy);
            this.updateStringWithStringWidth(matrices, bodyTextFont, textValue, "ms", floatValue3, y + 7.0F, intValueItem, intValueEntry);
            Renderer2D.drawHudShadow(matrices, x, floatValue15, floatValue12, floatValue13, floatValue14, 2.5F, intValueResult);
            Renderer2D.drawHudBlur(matrices, x + 0.5F, floatValue15 + 0.5F, floatValue12 - 1.0F, floatValue13 - 1.0F, floatValue14 - 0.5F, 1.0F);
            Renderer2D.drawRoundedRect(matrices, x, floatValue15, floatValue12, floatValue13, floatValue14, intValueValue);
            this.serverIconFont.drawGradientStringVertical(matrices, "Z", x + 6.0F, floatValue15 + 8.9F, themeStaticColor, intValueCopy);
            bodyTextFont.drawString(matrices, textItem, floatValue5, floatValue15 + 7.0F, intValueItem);
            Renderer2D.drawPixelRect(matrices, floatValue6, floatValue15 + 5.0F, 0.5F, 8.0F, intValue2);
            FontManager.getFont("watermark", 14).drawGradientStringVertical(matrices, "V", floatValue7, floatValue15 + 8.0F, themeStaticColor, intValueCopy);
            this.updateStringWithStringWidth(matrices, bodyTextFont, textResult, "tps", floatValue8, floatValue15 + 7.0F, intValueItem, intValueEntry);
            Renderer2D.drawPixelRect(matrices, floatValue9, floatValue15 + 5.0F, 0.5F, 8.0F, intValue2);
            FontManager.getFont("watermark", 14).drawGradientStringVertical(matrices, "H", floatValue10, floatValue15 + 8.0F, themeStaticColor, intValueCopy);
            this.updateStringWithStringWidth(matrices, bodyTextFont, textEntry, ":" + textCopy, floatValue11, floatValue15 + 7.0F, intValueItem, intValueEntry);
            matrices.pop();
            this.draggable.setWidth(Math.max(floatValue4, floatValue12) * 1.1F);
            this.draggable.setHeight((floatValue15 - y + floatValue13) * 1.1F);
        }
    }

    private void updateStringWithStringWidth(MatrixStack matrices, TextRenderer textRenderer, String text, String textValue, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        textRenderer.drawString(matrices, text, floatValue, floatValueValue, intValue);
        textRenderer.drawString(matrices, textValue, floatValue + textRenderer.getStringWidth(text), floatValueValue, intValueValue);
    }

    private float calculateStringWidth(TextRenderer textRenderer, TextTransition textTransition, String text) {
        return textTransition.calculateStringWidth(textRenderer) + textRenderer.getStringWidth(text);
    }

    private float calculateStringWidth(TextRenderer textRenderer, TextTransition textTransition, String text, TextTransition textTransitionValue, String textValue) {
        return textTransition.calculateStringWidth(textRenderer) + textRenderer.getStringWidth(text + " ") - 1.0F + textTransitionValue.calculateStringWidth(textRenderer) + textRenderer.getStringWidth(textValue);
    }

    private void updateString(MatrixStack matrices, TextRenderer textRenderer, TextTransition textTransition, String text, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue) {
        this.updateStringWidthWithCharAt(matrices, textRenderer, textTransition, floatValue, floatValueValue, floatValueResult, floatValueItem, intValue);
        textRenderer.drawString(matrices, text, floatValue + textTransition.calculateStringWidth(textRenderer) - 0.5F, floatValueValue, intValueValue);
    }

    private void updateStringWidthWithCharAt(MatrixStack matrices, TextRenderer textRenderer, TextTransition textTransition, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        String rightText = textTransition.getRightText();
        String leftText = textTransition.getLeftText();
        float floatValueEntry = Math.max(textRenderer.getStringWidth(rightText), textRenderer.getStringWidth(leftText)) + 2.0F;
        float floatValueCopy = textTransition.calculateCurrentTimeMillis();
        float floatValue2 = this.easeOutCubic(floatValueCopy);
        boolean flag = floatValueCopy < 1.0F;
        float floatValue3 = floatValue;
        ScissorStack.push();
        ScissorStack.setFromComponentCoordinates((double) (floatValue - 1.0F), (double) floatValueResult, (double) floatValueEntry, (double) floatValueItem);
        for (int index = 0; index < rightText.length(); index++) {
            String text = String.valueOf(rightText.charAt(index));
            float stringWidth = textRenderer.getStringWidth(text);
            boolean flagValue = flag && textTransition.isCharAt(index);
            if (flagValue) {
                if (index < leftText.length()) {
                    String textValue = String.valueOf(leftText.charAt(index));
                    float stringWidthValue = textRenderer.getStringWidth(textValue);
                    float floatValue4 = floatValue3 + (stringWidth - stringWidthValue) / 2.0F;
                    this.updateStringWithApplyAlpha(matrices, textRenderer, textValue, floatValue4, floatValueValue - 6.5F * floatValue2, intValue, 1.0F - floatValue2);
                }
                this.updateStringWithApplyAlpha(matrices, textRenderer, text, floatValue3, floatValueValue + 6.5F * (1.0F - floatValue2), intValue, floatValue2);
            } else {
                textRenderer.drawString(matrices, text, floatValue3, floatValueValue, intValue);
            }
            floatValue3 += stringWidth;
        }
        if (flag && leftText.length() > rightText.length()) {
            for (int length = rightText.length(); length < leftText.length(); length++) {
                String textResult = String.valueOf(leftText.charAt(length));
                float floatValue5 = floatValue + textRenderer.getStringWidth(leftText.substring(0, length));
                this.updateStringWithApplyAlpha(matrices, textRenderer, textResult, floatValue5, floatValueValue - 6.5F * floatValue2, intValue, 1.0F - floatValue2);
            }
        }
        ScissorStack.pop();
    }

    private void updateStringWithApplyAlpha(MatrixStack matrices, TextRenderer textRenderer, String text, float floatValue, float floatValueValue, int intValue, float floatValueResult) {
        if (!(floatValueResult <= 0.01F)) {
            textRenderer.drawString(matrices, text, floatValue, floatValueValue, ColorMathUtil.applyAlpha(intValue, floatValueResult));
        }
    }

    private float easeOutCubic(float floatValue) {
        float floatValueValue = Math.max(0.0F, Math.min(1.0F, floatValue));
        float floatValueResult = 1.0F - floatValueValue;
        return 1.0F - floatValueResult * floatValueResult * floatValueResult;
    }

    private void updateStringWithSplit(MatrixStack matrices, String text, float floatValue, float floatValueValue, int intValue, int intValueValue) {
        this.updateLocalState();
        TextRenderer bodyTextFont = this.bodyTextFont;
        String[] texts = text.split("\\.");
        if (texts.length < 2) {
            bodyTextFont.drawString(matrices, text, floatValue, floatValueValue, intValueValue);
        } else {
            String textValue = String.join(".", Arrays.copyOf(texts, texts.length - 1));
            String textResult = "." + texts[texts.length - 1];
            bodyTextFont.drawString(matrices, textValue, floatValue, floatValueValue, intValueValue);
            float floatValueResult = floatValue + bodyTextFont.getStringWidth(textValue) - 2.0F;
            bodyTextFont.drawString(matrices, textResult, floatValueResult, floatValueValue, intValue);
        }
    }

    private String createTextFromRole() {
        return Toshiba.INSTANCE != null && Toshiba.INSTANCE.userInfo != null && Toshiba.INSTANCE.userInfo.getRole() != null && !Toshiba.INSTANCE.userInfo.getRole().isEmpty() ? Toshiba.INSTANCE.userInfo.getRole() : "user";
    }

    private String createSubstringWithToLowerCase(String text) {
        if (text != null && !text.isEmpty()) {
            String normalizedText = text.toLowerCase(Locale.ROOT);
            return normalizedText.substring(0, 1).toUpperCase(Locale.ROOT) + normalizedText.substring(1);
        } else {
            return "User";
        }
    }

    private int calculateRgbaWithToLowerCase(String text, int intValue) {
        if (text == null) {
            return intValue;
        } else {
            String normalizedText = text.toLowerCase(Locale.ROOT);
            return switch(normalizedText) {
                case "admin", "developer" ->
                    ColorMathUtil.rgba(255, 80, 80, 255);
                case "tester" ->
                    ColorMathUtil.rgba(255, 200, 50, 255);
                case "media" ->
                    ColorMathUtil.rgba(211, 0, 255, 255);
                case "user" ->
                    ColorMathUtil.rgba(0, 178, 255, 255);
                default ->
                    intValue;
            };
        }
    }

    private int calculateThemeColor() {
        return ColorMathUtil.getThemeColor();
    }

    private int calculateNetworkHandlerWithPlayerListEntry() {
        if (mc != null && mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (playerListEntry != null) {
                return playerListEntry.getLatency();
            }
        }
        return 0;
    }

    private String createTextFromCurrentServerEntry() {
        return mc != null && mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null && !mc.getCurrentServerEntry().address.isEmpty() ? mc.getCurrentServerEntry().address : "Singleplayer";
    }

    private String createIndexOfWithSubstring(String text) {
        if (text != null && !text.isEmpty()) {
            String textValue = text;
            int index = text.indexOf(58);
            if (index > 0) {
                textValue = text.substring(0, index);
            }
            return textValue;
        } else {
            return "";
        }
    }

    private float calculateTPS() {
        return Toshiba.INSTANCE != null && Toshiba.INSTANCE.tpsCalc != null ? Math.max(0.0F, Math.min(20.0F, Toshiba.INSTANCE.tpsCalc.getTPS())) : 20.0F;
    }

    private String createTextFromFloat(float floatValue) {
        int intValue = Math.round(floatValue * 10.0F);
        return intValue / 10 + "." + Math.abs(intValue % 10);
    }

    public static class TextTransition {

        private String charAtText = "";

        private String equalsText = "";

        private long lastUpdateMillis;

        private boolean stateFlag;

        TextTransition() {
        }

        private void updateEqualsWithCurrentTimeMillis(String text) {
            String charAtText = text == null ? "" : text;
            if (!this.stateFlag) {
                this.charAtText = charAtText;
                this.equalsText = charAtText;
                this.stateFlag = true;
                this.lastUpdateMillis = 0L;
            } else if (!this.equalsText.equals(charAtText)) {
                this.charAtText = this.equalsText;
                this.equalsText = charAtText;
                this.lastUpdateMillis = System.currentTimeMillis();
            }
        }

        String getLeftText() {
            return this.charAtText;
        }

        String getRightText() {
            return this.equalsText;
        }

        boolean isCharAt(int intValue) {
            return intValue >= this.charAtText.length() || this.charAtText.charAt(intValue) != this.equalsText.charAt(intValue);
        }

        float calculateCurrentTimeMillis() {
            return this.lastUpdateMillis == 0L ? 1.0F : Math.max(0.0F, Math.min(1.0F, (float) (System.currentTimeMillis() - this.lastUpdateMillis) / 260.0F));
        }

        float calculateStringWidth(TextRenderer textRenderer) {
            float stringWidth = textRenderer.getStringWidth(this.equalsText);
            return this.calculateCurrentTimeMillis() >= 1.0F ? stringWidth : Math.max(stringWidth, textRenderer.getStringWidth(this.charAtText));
        }
    }
}
