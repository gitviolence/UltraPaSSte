package brova.toshiba.client.gui.hud;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.utils.math.HoverUtil;
import brova.toshiba.client.utils.math.MathUtil;
import brova.toshiba.client.utils.math.Point2D;
import brova.toshiba.client.utils.render.Renderer2D;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

public class Draggable implements MinecraftAccess {

    @Expose
    @SerializedName("x")
    private float x;

    @Expose
    @SerializedName("y")
    private float y;

    public float initialXVal;

    public float initialYVal;

    private float dragOffsetX;

    private float dragOffsetY;

    private boolean dragging;

    private float width;

    private float height;

    @Expose
    @SerializedName("name")
    private String name;

    private final Module module;

    private float dragTargetX;

    private float dragTargetY;

    private float snapGuideAlpha = 0.0F;

    private long lastDragUpdateMillis;

    private boolean showHorizontalCenterGuide;

    private boolean showVerticalCenterGuide;

    private boolean showLeftQuarterGuide;

    private boolean showRightQuarterGuide;

    private boolean showLeftEdgeGuide;

    private boolean showRightEdgeGuide;

    private boolean showTopQuarterGuide;

    private boolean showBottomQuarterGuide;

    private boolean showTopEdgeGuide;

    private boolean showBottomEdgeGuide;

    public Draggable(Module module, String name, float x, float y) {
        this.module = module;
        this.name = name;
        this.x = x;
        this.y = y;
        this.initialXVal = x;
        this.initialYVal = y;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    private Point2D toScaledMousePosition(int intValue, int intValueValue) {
        MinecraftClient instance = MinecraftClient.getInstance();
        Window window = instance == null ? null : instance.getWindow();
        double doubleValue = window == null ? 1.0 : window.getScaleFactor();
        return new Point2D((int) (intValue * doubleValue / 2.0), (int) (intValueValue * doubleValue / 2.0));
    }

    public final void onDraw(int intValue, int intValueValue, Window window, MatrixStack matrices) {
        float floatValue = this.width * BackgroundSettings.HUD_SCALE.get();
        float floatValueValue = this.height * BackgroundSettings.HUD_SCALE.get();
        Point2D point2D = this.toScaledMousePosition(intValue, intValueValue);
        intValue = point2D.getX();
        intValueValue = point2D.getY();
        float floatValueResult = window.getScaledWidth() / 2.0F;
        float floatValueItem = window.getScaledHeight() / 2.0F;
        float floatValueEntry = window.getScaledWidth() / 4.0F;
        float floatValueCopy = window.getScaledHeight() / 4.0F;
        float floatValue2 = window.getScaledWidth() / 8.0F;
        float floatValue3 = window.getScaledHeight() / 8.0F;
        float floatValue4 = window.getScaledWidth() / 1.15F;
        float floatValue5 = window.getScaledHeight() / 1.15F;
        float floatValue6 = window.getScaledWidth() / 1.35F;
        float floatValue7 = window.getScaledHeight() / 1.35F;
        this.showHorizontalCenterGuide = this.showVerticalCenterGuide = this.showLeftQuarterGuide = this.showRightQuarterGuide = this.showLeftEdgeGuide = this.showRightEdgeGuide = this.showTopQuarterGuide = this.showBottomQuarterGuide = this.showTopEdgeGuide = this.showBottomEdgeGuide = false;
        if (this.dragging) {
            this.dragTargetX = intValue - this.dragOffsetX;
            this.dragTargetY = intValueValue - this.dragOffsetY;
            boolean flag = false;
            if (Math.abs(this.dragTargetX + floatValue / 2.0F - floatValueResult) < 10.0F) {
                this.dragTargetX = floatValueResult - floatValue / 2.0F;
                this.showVerticalCenterGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetY + floatValueValue / 2.0F - floatValueItem) < 10.0F) {
                this.dragTargetY = floatValueItem - floatValueValue / 2.0F;
                this.showHorizontalCenterGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetX + floatValue / 2.0F - floatValueEntry) < 10.0F) {
                this.dragTargetX = floatValueEntry - floatValue / 2.0F;
                this.showLeftQuarterGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetY + floatValueValue / 2.0F - floatValueCopy) < 10.0F) {
                this.dragTargetY = floatValueCopy - floatValueValue / 2.0F;
                this.showTopQuarterGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetX + floatValue / 2.0F - floatValue2) < 10.0F) {
                this.dragTargetX = floatValue2 - floatValue / 2.0F;
                this.showRightQuarterGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetY + floatValueValue / 2.0F - floatValue3) < 10.0F) {
                this.dragTargetY = floatValue3 - floatValueValue / 2.0F;
                this.showBottomQuarterGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetX + floatValue / 2.0F - floatValue4) < 10.0F) {
                this.dragTargetX = floatValue4 - floatValue / 2.0F;
                this.showLeftEdgeGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetY + floatValueValue / 2.0F - floatValue5) < 10.0F) {
                this.dragTargetY = floatValue5 - floatValueValue / 2.0F;
                this.showTopEdgeGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetX + floatValue / 2.0F - floatValue6) < 10.0F) {
                this.dragTargetX = floatValue6 - floatValue / 2.0F;
                this.showRightEdgeGuide = true;
                flag = true;
            }
            if (Math.abs(this.dragTargetY + floatValueValue / 2.0F - floatValue7) < 10.0F) {
                this.dragTargetY = floatValue7 - floatValueValue / 2.0F;
                this.showBottomEdgeGuide = true;
                flag = true;
            }
            if (this.dragTargetX + floatValue > window.getScaledWidth()) {
                this.dragTargetX = window.getScaledWidth() - floatValue;
            }
            if (this.dragTargetY + floatValueValue > window.getScaledHeight()) {
                this.dragTargetY = window.getScaledHeight() - floatValueValue;
            }
            if (this.dragTargetX < 0.0F) {
                this.dragTargetX = 0.0F;
            }
            if (this.dragTargetY < 0.0F) {
                this.dragTargetY = 0.0F;
            }
            this.x = MathUtil.lerp(this.x, this.dragTargetX, 0.19F);
            this.y = MathUtil.lerp(this.y, this.dragTargetY, 0.19F);
            this.updateDragAnimation(flag);
        } else {
            this.updateDragAnimation(false);
        }
        this.renderSnapGuides(matrices, window);
    }

    private void updateDragAnimation(boolean flag) {
        long lastDragUpdateMillis = System.currentTimeMillis();
        float floatValue = (float) (lastDragUpdateMillis - this.lastDragUpdateMillis) / 1000.0F;
        this.lastDragUpdateMillis = lastDragUpdateMillis;
        float floatValueValue = 2.0F;
        float floatValueResult = 2.0F;
        if (flag) {
            this.snapGuideAlpha += floatValue * floatValueValue;
            if (this.snapGuideAlpha > 1.0F) {
                this.snapGuideAlpha = 1.0F;
            }
        } else {
            this.snapGuideAlpha -= floatValue * floatValueResult;
            if (this.snapGuideAlpha < 0.0F) {
                this.snapGuideAlpha = 0.0F;
            }
        }
    }

    private void renderSnapGuides(MatrixStack matrices, Window window) {
        if (this.snapGuideAlpha > 0.0F) {
            float floatValue = window.getScaledWidth() / 2.0F;
            float floatValueValue = window.getScaledHeight() / 2.0F;
            float floatValueResult = window.getScaledWidth() / 4.0F;
            float floatValueItem = window.getScaledHeight() / 4.0F;
            float floatValueEntry = window.getScaledWidth() / 8.0F;
            float floatValueCopy = window.getScaledHeight() / 8.0F;
            float floatValue2 = window.getScaledWidth() / 1.15F;
            float floatValue3 = window.getScaledHeight() / 1.15F;
            float floatValue4 = window.getScaledWidth() / 1.35F;
            float floatValue5 = window.getScaledHeight() / 1.35F;
            int intValue = (int) (this.snapGuideAlpha * 255.0F) << 24 | 16777215;
            if (this.showVerticalCenterGuide) {
                Renderer2D.drawRoundedRect(matrices, floatValue - 0.33333334F, 0.0F, 1.0F, window.getScaledHeight(), 1.0F, intValue);
            }
            if (this.showHorizontalCenterGuide) {
                Renderer2D.drawRoundedRect(matrices, 0.0F, floatValueValue - 0.33333334F, window.getScaledWidth(), 1.0F, 1.0F, intValue);
            }
            if (this.showLeftQuarterGuide) {
                Renderer2D.drawRoundedRect(matrices, floatValueResult - 0.33333334F, 0.0F, 1.0F, window.getScaledHeight(), 1.0F, intValue);
            }
            if (this.showTopQuarterGuide) {
                Renderer2D.drawRoundedRect(matrices, 0.0F, floatValueItem - 0.33333334F, window.getScaledWidth(), 1.0F, 1.0F, intValue);
            }
            if (this.showRightQuarterGuide) {
                Renderer2D.drawRoundedRect(matrices, floatValueEntry - 0.33333334F, 0.0F, 1.0F, window.getScaledHeight(), 1.0F, intValue);
            }
            if (this.showBottomQuarterGuide) {
                Renderer2D.drawRoundedRect(matrices, 0.0F, floatValueCopy - 0.33333334F, window.getScaledWidth(), 1.0F, 1.0F, intValue);
            }
            if (this.showLeftEdgeGuide) {
                Renderer2D.drawRoundedRect(matrices, floatValue2 - 0.33333334F, 0.0F, 1.0F, window.getScaledHeight(), 1.0F, intValue);
            }
            if (this.showTopEdgeGuide) {
                Renderer2D.drawRoundedRect(matrices, 0.0F, floatValue3 - 0.33333334F, window.getScaledWidth(), 1.0F, 1.0F, intValue);
            }
            if (this.showRightEdgeGuide) {
                Renderer2D.drawRoundedRect(matrices, floatValue4 - 0.33333334F, 0.0F, 1.0F, window.getScaledHeight(), 1.0F, intValue);
            }
            if (this.showBottomEdgeGuide) {
                Renderer2D.drawRoundedRect(matrices, 0.0F, floatValue5 - 0.33333334F, window.getScaledWidth(), 1.0F, 1.0F, intValue);
            }
        }
    }

    public final boolean onClick(double doubleValue, double doubleValueValue, int intValue) {
        if (intValue == 0 && HoverUtil.isInRegion(doubleValue, doubleValueValue, this.x, this.y, this.width * BackgroundSettings.HUD_SCALE.get(), this.height * BackgroundSettings.HUD_SCALE.get())) {
            this.dragging = true;
            this.dragOffsetX = (int) (doubleValue - this.x);
            this.dragOffsetY = (int) (doubleValueValue - this.y);
            this.lastDragUpdateMillis = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    public final void onRelease(int intValue) {
        if (intValue == 0) {
            this.dragging = false;
        }
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return this.width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getHeight() {
        return this.height;
    }

    public String getName() {
        return this.name;
    }

    public Module getModule() {
        return this.module;
    }
}
