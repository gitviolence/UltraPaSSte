package brova.toshiba.client.gui.click;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.data.LoaderUserInfo;
import brova.toshiba.client.utils.math.HoverUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class ProfilePanel implements MinecraftAccess {

    private static final Identifier profileTexture = Identifier.of("toshiba", "featrues/gui/profile.png");

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH);

    private static final DateTimeFormatter[] dateTimeFormatterValues = new DateTimeFormatter[] { DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.ofPattern("d.MM.uuuu"), DateTimeFormatter.ofPattern("uuuu/MM/dd"), createToFormatterWithAppendPattern("MMMM d, uuuu"), createToFormatterWithAppendPattern("MMM d, uuuu") };

    private final Animation hoverAnimation = new Animation(0.0F, 10.0F, PowerEasing.CUBIC_OUT);

    private final Animation popupAnimation = new Animation(0.0F, 8.0F, PowerEasing.CUBIC_OUT);

    private final DraggableSettingWidget draggableSettingWidget;

    private boolean avatarHeld;

    private long lastUpdateNanos;

    private boolean easingEnabled;

    private boolean stateFlag;

    private String equalsText;

    private String cachedText = "Lifetime";

    public ProfilePanel(ClickGuiController clickGuiController) {
        this.draggableSettingWidget = new DraggableSettingWidget(clickGuiController);
    }

    public boolean mouseClicked(double doubleValue, double doubleValueValue, int intValue, float floatValue, float floatValueValue) {
        if (this.avatarHeld) {
            return true;
        } else if (intValue != 0 && intValue != 1) {
            return false;
        } else if (!this.isProfileButtonHovered(doubleValue, doubleValueValue, floatValue, floatValueValue)) {
            if (this.easingEnabled && this.isHoveredWithProfilePanelX(doubleValue, doubleValueValue, floatValue, floatValueValue)) {
                this.draggableSettingWidget.mouseClicked(doubleValue, doubleValueValue, intValue, ClickGuiLayout.getProfilePanelX(floatValue), this.calculateProfilePanelYWithHeight(floatValueValue));
                return true;
            } else {
                this.closeProfilePanel();
                return false;
            }
        } else {
            if (intValue == 0 && this.isAvatarHovered(doubleValue, doubleValueValue, floatValue, floatValueValue) && mc.getResourceManager().getResource(profileTexture).isPresent()) {
                this.avatarHeld = true;
                this.lastUpdateNanos = System.nanoTime();
            } else {
                this.easingEnabled = !this.easingEnabled;
            }
            return true;
        }
    }

    public boolean mouseReleased(double doubleValue, double doubleValueValue, int intValue, float floatValue, float floatValueValue) {
        if (this.draggableSettingWidget.mouseReleased(intValue)) {
            return true;
        } else if (intValue == 0 && this.avatarHeld) {
            boolean flag = System.nanoTime() - this.lastUpdateNanos < 150000000L;
            this.cancelAvatarHold();
            if (flag && this.isProfileButtonHovered(doubleValue, doubleValueValue, floatValue, floatValueValue)) {
                this.easingEnabled = !this.easingEnabled;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isAvatarHeld() {
        return this.avatarHeld;
    }

    public boolean mouseDragged(double doubleValue, int intValue, float floatValue) {
        return this.draggableSettingWidget.mouseDragged(doubleValue, intValue, ClickGuiLayout.getProfilePanelX(floatValue));
    }

    public void cancelAvatarHold() {
        this.avatarHeld = false;
        this.lastUpdateNanos = 0L;
    }

    public boolean closeProfilePanel() {
        boolean easingEnabled = this.easingEnabled;
        this.easingEnabled = false;
        this.draggableSettingWidget.stopDragging();
        return easingEnabled;
    }

    public void beginClosing() {
        this.stateFlag = true;
        this.draggableSettingWidget.stopDragging();
    }

    public void resetOverlays() {
        this.stateFlag = false;
        this.cancelAvatarHold();
        this.hoverAnimation.setValue(0.0F);
        this.closeProfilePanel();
        this.popupAnimation.setValue(0.0F);
    }

    public void renderPanel(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, int intValue, double doubleValue, double doubleValueValue) {
        if (!this.stateFlag) {
            this.draggableSettingWidget.updateAnimations();
            this.popupAnimation.setEasing(this.easingEnabled ? PowerEasing.CUBIC_OUT : PowerEasing.CUBIC_IN);
            this.popupAnimation.update(this.easingEnabled ? 1.0F : 0.0F);
        }
        float floatValueItem = MathHelper.clamp(this.popupAnimation.getValue(), 0.0F, 1.0F) * floatValueResult;
        ClickGuiBackground.render(context.getMatrices(), ClickGuiLayout.getProfilePanelX(floatValue), this.calculateProfilePanelYWithHeight(floatValueValue), 123.0F, this.draggableSettingWidget.getHeight(), 12.0F, floatValueItem);
        this.draggableSettingWidget.render(context, ClickGuiLayout.getProfilePanelX(floatValue), this.calculateProfilePanelYWithHeight(floatValueValue), floatValueItem, intValue, doubleValue, doubleValueValue);
    }

    public void renderAvatarPreview(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, Window window) {
        if (!this.stateFlag) {
            if (this.avatarHeld && (GLFW.glfwGetWindowAttrib(window.getHandle(), 131073) != 1 || GLFW.glfwGetMouseButton(window.getHandle(), 0) != 1)) {
                this.cancelAvatarHold();
            }
            boolean flag = this.avatarHeld && System.nanoTime() - this.lastUpdateNanos >= 150000000L;
            this.hoverAnimation.update(flag ? 1.0F : 0.0F);
        }
        float clampedValue = MathHelper.clamp(this.hoverAnimation.getValue(), 0.0F, 1.0F);
        float floatValueItem = clampedValue * floatValueResult;
        if (!(floatValueItem <= 0.001F)) {
            MatrixStack matrices = context.getMatrices();
            int intValue = Math.round(190.0F * floatValueItem);
            if (intValue > 0) {
                Renderer2D.drawRoundedRect(matrices, floatValue, floatValueValue, 500.0F, 350.0F, 10.0F, ColorMathUtil.rgba(0, 0, 0, intValue));
            }
            int intValueValue = Math.round(intValue * this.popupAnimation.getValue());
            if (intValueValue > 0) {
                Renderer2D.drawRoundedRect(matrices, ClickGuiLayout.getProfilePanelX(floatValue), this.calculateProfilePanelYWithHeight(floatValueValue), 123.0F, this.draggableSettingWidget.getHeight(), 12.0F, ColorMathUtil.rgba(0, 0, 0, intValueValue));
            }
            float floatValueEntry = 180.0F * (0.92F + 0.08F * clampedValue);
            float floatValueCopy = floatValue + (500.0F - floatValueEntry) * 0.5F;
            float floatValue2 = floatValueValue + (350.0F - floatValueEntry) * 0.5F;
            float floatValue3 = floatValueEntry * 6.0F / 20.0F;
            Renderer2D.drawRoundedImage(matrices, profileTexture, floatValueCopy, floatValue2, floatValueEntry, floatValueEntry, floatValue3, floatValueItem);
        }
    }

    public void render(DrawContext context, float floatValue, float floatValueValue, float floatValueResult) {
        MatrixStack matrices = context.getMatrices();
        float floatValueItem = floatValue + 9.0F;
        float floatValueEntry = floatValueValue + 309.0F;
        float floatValueCopy = 122.0F;
        float floatValue2 = 32.0F;
        float floatValue3 = 6.0F;
        int intValue = calculateRgba(floatValueResult / 255.0F);
        int intValueValue = calculateRgba(2.0F * floatValueResult / 255.0F);
        Renderer2D.drawDiagonalGradientRect(matrices, floatValueItem, floatValueEntry, floatValueCopy, floatValue2, floatValue3, intValue, intValueValue);
        float floatValue4 = floatValue + 15.0F;
        float floatValue5 = floatValueValue + 315.0F;
        float floatValue6 = 20.0F;
        float floatValue7 = 6.0F;
        int intValueResult = calculateRgba(0.03F * floatValueResult);
        if (mc.getResourceManager().getResource(profileTexture).isPresent()) {
            Renderer2D.drawRoundedImage(matrices, profileTexture, floatValue4, floatValue5, floatValue6, floatValue6, floatValue7, floatValueResult);
        } else if (intValueResult >>> 24 != 0) {
            Renderer2D.drawRoundedRect(matrices, floatValue4, floatValue5, floatValue6, floatValue6, floatValue7, intValueResult);
        }
        if (intValueResult >>> 24 != 0) {
            float floatValue8 = 1.0F;
            float floatValue9 = floatValue7 + floatValue8;
            Renderer2D.drawRoundedRectOutline(matrices, floatValue4 - floatValue8, floatValue5 - floatValue8, floatValue6 + floatValue8 * 2.0F, floatValue6 + floatValue8 * 2.0F, floatValue9, floatValue8, intValueResult, true);
        }
        float floatValue10 = floatValue + 41.0F;
        float floatValue11 = floatValueItem + floatValueCopy - 6.0F - floatValue10;
        this.updateWidthWithBlank(matrices, FontManager.getFont("suisse", 14), LoaderUserInfo.getUserName(), floatValue10, floatValueValue + 319.5F, floatValue11, calculateRgba(floatValueResult));
        this.updateWidthWithBlank(matrices, FontManager.getFont("suisse", 10), this.createTextFromSubTillWithBlank(), floatValue10, floatValueValue + 327.0F, floatValue11, calculateRgba(0.25F * floatValueResult));
    }

    private void updateWidthWithBlank(MatrixStack matrices, TextRenderer textRenderer, String text, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        if (textRenderer != null && text != null && !text.isBlank()) {
            String textValue = text;
            if (textRenderer.getWidth(text) > floatValueResult) {
                while (!textValue.isEmpty() && textRenderer.getWidth(textValue + "…") > floatValueResult) {
                    textValue = textValue.substring(0, textValue.offsetByCodePoints(textValue.length(), -1));
                }
                textValue = textValue + "…";
            }
            float floatValueItem = floatValueValue + 2.5F - (textRenderer.getFont().getBaselineHeight() - 0.725F) * textRenderer.getSize() * 0.5F;
            textRenderer.drawWithWeight(matrices, textValue, floatValue, floatValueItem, intValue, 0.04F);
        }
    }

    private String createTextFromSubTillWithBlank() {
        String subTill = LoaderUserInfo.getSubTill();
        if (subTill == null || subTill.isBlank()) {
            subTill = "Lifetime";
        }
        if (!subTill.equals(this.equalsText)) {
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, subTill, (accessReceiver, accessValue) -> {
                accessReceiver.equalsText = accessValue;
            });
            String text = this.formatTextFromEqualsIgnoreCaseWithRegionMatches(subTill.trim());
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                accessReceiver.cachedText = accessValue;
            });
        }
        return this.cachedText;
    }

    private String formatTextFromEqualsIgnoreCaseWithRegionMatches(String text) {
        if (text.equalsIgnoreCase("Lifetime")) {
            return "Lifetime";
        } else {
            String textValue = text.regionMatches(true, 0, "Till ", 0, 5) ? text.substring(5).trim() : text;
            for (DateTimeFormatter dateTimeFormatterValue : dateTimeFormatterValues) {
                try {
                    LocalDate localDate = LocalDate.parse(textValue, dateTimeFormatterValue);
                    return "Till " + dateTimeFormatter.format(localDate).toLowerCase(Locale.ENGLISH);
                } catch (DateTimeParseException failure) {
                }
            }
            return "Till " + textValue;
        }
    }

    private static DateTimeFormatter createToFormatterWithAppendPattern(String text) {
        return new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(text).toFormatter(Locale.ENGLISH);
    }

    private boolean isProfileButtonHovered(double doubleValue, double doubleValueValue, float floatValue, float floatValueValue) {
        return HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue + 9.0F, floatValueValue + 309.0F, 122.0, 32.0);
    }

    private boolean isAvatarHovered(double doubleValue, double doubleValueValue, float floatValue, float floatValueValue) {
        return HoverUtil.isHovered(doubleValue, doubleValueValue, floatValue + 15.0F, floatValueValue + 315.0F, 20.0, 20.0);
    }

    private float calculateProfilePanelYWithHeight(float floatValue) {
        return ClickGuiLayout.getProfilePanelY(floatValue, this.draggableSettingWidget.getHeight());
    }

    private boolean isHoveredWithProfilePanelX(double doubleValue, double doubleValueValue, float floatValue, float floatValueValue) {
        return HoverUtil.isHovered(doubleValue, doubleValueValue, ClickGuiLayout.getProfilePanelX(floatValue), this.calculateProfilePanelYWithHeight(floatValueValue), 123.0, this.draggableSettingWidget.getHeight());
    }

    private static int calculateRgba(float floatValue) {
        return ColorUtil.rgba(255, 255, 255, Math.round(255.0F * floatValue));
    }
}
