package brova.toshiba.client.gui.click;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.math.HoverUtil;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.transform.ScissorStack;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class DraggableSettingWidget implements MinecraftAccess {

    private final ClickGuiController clickGuiController;

    private final SettingRenderer settingRenderer = new SettingRenderer();

    private final Map<SettingBase, Animation> ifAbsentAnimationBySettingBase = new IdentityHashMap<>();

    private FloatSetting floatSetting;

    private double x;

    private double scaleFactor;

    private float value;

    public DraggableSettingWidget(ClickGuiController clickGuiController) {
        this.clickGuiController = clickGuiController;
    }

    public void updateAnimations() {
        for (SettingBase setting : BackgroundSettings.getSettings()) {
            this.createAnimationFromIfAbsentWithVisible(setting).update(brova.toshiba.client.utils.runtime.AccessGuard.invoke(setting, new Object[] {}, (accessReceiver, accessArguments) -> ((SettingBase) accessReceiver).visible()) ? 1.0F : 0.0F);
        }
    }

    public float getHeight() {
        float floatValue = 13.5F;
        for (SettingBase setting : BackgroundSettings.getSettings()) {
            floatValue += ClickGuiLayout.getProfileSettingHeight(setting) * this.calculateSettingBase(setting);
        }
        return floatValue;
    }

    private Animation createAnimationFromIfAbsentWithVisible(SettingBase setting) {
        return this.ifAbsentAnimationBySettingBase.computeIfAbsent(setting, input -> new Animation(input.visible() ? 1.0F : 0.0F, 8.0F, PowerEasing.CUBIC_OUT));
    }

    private float calculateSettingBase(SettingBase setting) {
        return MathHelper.clamp(this.createAnimationFromIfAbsentWithVisible(setting).getValue(), 0.0F, 1.0F);
    }

    public void render(DrawContext context, float floatValue, float floatValueValue, float floatValueResult, int intValue, double doubleValue, double doubleValueValue) {
        if (this.floatSetting != null) {
            long handle = mc.getWindow().getHandle();
            if (this.floatSetting.visible() && GLFW.glfwGetMouseButton(handle, 0) == 1 && GLFW.glfwGetWindowAttrib(handle, 131073) == 1) {
                this.updateMaxWithMin(doubleValue, floatValue);
            } else {
                this.stopDragging();
            }
        }
        float floatValueItem = floatValue + 7.0F;
        float floatValueEntry = floatValueValue + 10.0F;
        float floatValueCopy = this.calculateFloat();
        for (SettingBase setting : BackgroundSettings.getSettings()) {
            float floatValue2 = this.calculateSettingBase(setting);
            float floatValue3 = ClickGuiLayout.getProfileSettingHeight(setting) * floatValue2;
            int intValueValue = Math.round(255.0F * floatValueResult * floatValue2);
            if (!(floatValue3 <= 0.0F)) {
                boolean flag = floatValue2 < 0.999F;
                if (flag) {
                    ScissorStack.push();
                    ScissorStack.setFromComponentCoordinates((double) (floatValueItem - 1.0F), (double) (floatValueEntry - 2.0F), (double) (floatValueCopy + 2.0F), (double) floatValue3);
                }
                if (setting instanceof ModeSetting settingValue) {
                    this.settingRenderer.renderSlidingModeSetting(context, floatValueItem, floatValueEntry, floatValueCopy, intValueValue, intValue, doubleValue, doubleValueValue, settingValue, this.clickGuiController);
                } else if (setting instanceof BooleanSetting settingResult) {
                    this.settingRenderer.renderBooleanSetting(context, floatValueItem, floatValueEntry, floatValueCopy, intValueValue, intValue, settingResult, this.clickGuiController);
                } else if (setting instanceof FloatSetting settingItem) {
                    this.settingRenderer.renderFloatSetting(context, floatValueItem, floatValueEntry, floatValueCopy, intValueValue, intValue, settingItem, this.clickGuiController);
                }
                if (flag) {
                    ScissorStack.pop();
                }
                floatValueEntry += floatValue3;
            }
        }
    }

    public void mouseClicked(double doubleValue, double doubleValueValue, int intValue, float floatValue, float floatValueValue) {
        if (intValue == 0) {
            float floatValueResult = floatValue + 7.0F;
            float floatValueItem = floatValueValue + 10.0F;
            float floatValueEntry = this.calculateFloat();
            for (SettingBase setting : BackgroundSettings.getSettings()) {
                float floatValueCopy = this.calculateSettingBase(setting);
                float floatValue2 = ClickGuiLayout.getProfileSettingHeight(setting) * floatValueCopy;
                if (setting.visible() && !(floatValueCopy < 0.95F)) {
                    if (setting instanceof ModeSetting settingValue) {
                        if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValueResult, floatValueItem + 13.0F, floatValueEntry, 15.5)) {
                            int intValueValue = Math.min(settingValue.getMods().size() - 1, (int) ((doubleValue - floatValueResult) / (floatValueEntry / settingValue.getMods().size())));
                            settingValue.set(settingValue.getMods().get(intValueValue));
                            this.stopDragging();
                            return;
                        }
                    } else if (setting instanceof BooleanSetting settingResult) {
                        if (HoverUtil.isHovered(doubleValue, doubleValueValue, floatValueResult, floatValueItem - 2.0F, floatValueEntry, 12.0)) {
                            settingResult.setState(!settingResult.isState());
                            this.stopDragging();
                            return;
                        }
                    } else if (setting instanceof FloatSetting floatSetting && HoverUtil.isHovered(doubleValue, doubleValueValue, floatValueResult, floatValueItem + 11.0F, floatValueEntry, 8.0)) {
                        this.floatSetting = floatSetting;
                        if (floatSetting == BackgroundSettings.GUI_SCALE) {
                            this.x = mc.mouse.getX();
                            this.value = floatSetting.get();
                            this.scaleFactor = floatValueEntry * this.value * mc.getWindow().getScaleFactor();
                        } else {
                            this.updateMaxWithMin(doubleValue, floatValue);
                        }
                        return;
                    }
                    floatValueItem += floatValue2;
                } else {
                    floatValueItem += floatValue2;
                }
            }
        }
    }

    public boolean mouseDragged(double doubleValue, int intValue, float floatValue) {
        if (intValue == 0 && this.floatSetting != null) {
            this.updateMaxWithMin(doubleValue, floatValue);
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(int intValue) {
        if (intValue == 0 && this.floatSetting != null) {
            this.stopDragging();
            return true;
        } else {
            return false;
        }
    }

    public void stopDragging() {
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.floatSetting = (FloatSetting) accessValue;
        });
        this.x = 0.0;
        this.scaleFactor = 0.0;
        this.value = 0.0F;
    }

    private void updateMaxWithMin(double doubleValue, float floatValue) {
        if (this.floatSetting == BackgroundSettings.GUI_SCALE) {
            double x = mc.mouse.getX();
            double doubleValueValue = x - this.x;
            this.x = x;
            if (doubleValueValue != 0.0 && !(this.scaleFactor <= 0.0)) {
                float floatValueValue = this.floatSetting.getMax() - this.floatSetting.getMin();
                this.value = MathHelper.clamp(this.value + (float) (doubleValueValue / this.scaleFactor) * floatValueValue, this.floatSetting.getMin(), this.floatSetting.getMax());
                float increment = this.floatSetting.getIncrement();
                this.floatSetting.setValue(Math.round(this.value / increment) * increment);
            }
        } else {
            float floatValueResult = floatValue + 7.0F;
            float clampedValue = MathHelper.clamp((float) (doubleValue - floatValueResult) / this.calculateFloat(), 0.0F, 1.0F);
            float interpolatedValue = MathHelper.lerp(clampedValue, this.floatSetting.getMin(), this.floatSetting.getMax());
            float incrementValue = this.floatSetting.getIncrement();
            this.floatSetting.setValue(Math.round(interpolatedValue / incrementValue) * incrementValue);
        }
    }

    private float calculateFloat() {
        return 109.0F;
    }
}
