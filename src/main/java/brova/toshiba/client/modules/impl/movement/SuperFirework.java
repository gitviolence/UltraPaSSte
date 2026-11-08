package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.text.ChatUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public final class SuperFirework extends Module {

    private static final int[] integerValuesForCalculateWrapDegrees = new int[] { -45, 45, 135, -135 };

    private static final int[] integerValuesForCalculateFloat = new int[] { -45, 45 };

    private static final String[] maximumTextValues = new String[] { "0-9", "9-18", "18-27", "27-36", "36-45" };

    public static SuperFirework INSTANCE = new SuperFirework();

    private final ModeSetting mode = new ModeSetting("Режим", "ReallyWorld", "ReallyWorld", "BravoHvH", "Кастомный");

    private final BooleanSetting autoDecrease = new BooleanSetting("Скорость после флага", false).visible(() -> this.mode.is("Кастомный"));

    private final FloatSetting[] xzSpeeds = new FloatSetting[maximumTextValues.length];

    private final FloatSetting[] yUpSpeeds = new FloatSetting[maximumTextValues.length];

    private final FloatSetting[] yDownSpeeds = new FloatSetting[maximumTextValues.length];

    private int lastYawRange;

    private int lastPitchRange;

    private boolean lastYUp = true;

    private double lastX = 0.0;

    private double lastY = 0.0;

    private double lastZ = 0.0;

    public SuperFirework() {
        super("SuperFirework", "Ускоряет буст от фейерверка", ModuleCategory.MOVEMENT);
        for (int index = 0; index < maximumTextValues.length; index++) {
            String text = maximumTextValues[index] + "°";
            this.xzSpeeds[index] = new FloatSetting("Скорость по Yaw " + text, 1.6F, 1.5F, 2.5F, 0.01F).visible(() -> this.mode.is("Кастомный"));
            this.yUpSpeeds[index] = new FloatSetting("Скорость вверх по Pitch " + text, 1.6F, 1.5F, 2.5F, 0.01F).visible(() -> this.mode.is("Кастомный"));
            this.yDownSpeeds[index] = new FloatSetting("Скорость вниз по Pitch " + text, 1.6F, 1.5F, 2.5F, 0.01F).visible(() -> this.mode.is("Кастомный"));
        }
        this.addSettings(this.mode, this.autoDecrease);
        this.addSettings(this.xzSpeeds);
        this.addSettings(this.yUpSpeeds);
        this.addSettings(this.yDownSpeeds);
        this.updateValue();
    }

    public Vec2f computeBoost(float floatValue, float floatValueValue) {
        if (this.mode.is("Кастомный")) {
            float horizontalSpeedForYaw = this.getHorizontalSpeedForYaw(floatValue);
            float verticalSpeedForPitch = this.getVerticalSpeedForPitch(floatValueValue);
            return new Vec2f(horizontalSpeedForYaw, verticalSpeedForPitch);
        } else if (this.mode.is("BravoHvH")) {
            float floatValueResult = this.calculateWrapDegrees(floatValueValue, floatValue);
            float specialPitchSpeed = this.getSpecialPitchSpeed(floatValueValue);
            return new Vec2f(floatValueResult, specialPitchSpeed);
        } else {
            float floatValueItem = this.calculateFloat(floatValueValue, floatValue, false, true);
            return new Vec2f(floatValueItem, floatValueItem);
        }
    }

    public void handleFlag(double lastX, double lastY, double lastZ) {
        if (this.autoDecrease.isState() && this.mode.is("Кастомный")) {
            double magnitude = Math.abs(lastX - this.lastX);
            double magnitudeValue = Math.abs(lastY - this.lastY);
            double magnitudeResult = Math.abs(lastZ - this.lastZ);
            double squareRoot = Math.sqrt(magnitude * magnitude + magnitudeResult * magnitudeResult);
            if (squareRoot < 0.01 && magnitudeValue < 0.01) {
                this.lastX = lastX;
                this.lastY = lastY;
                this.lastZ = lastZ;
            } else {
                if (squareRoot > magnitudeValue) {
                    FloatSetting setting = this.xzSpeeds[this.lastYawRange];
                    float floatValue = Math.max(1.5F, setting.getValue().floatValue() - 0.01F);
                    setting.setValue(floatValue);
                    ChatUtil.sendMessage("Скорость Yaw " + maximumTextValues[this.lastYawRange] + "° снижена на 0.01");
                } else {
                    FloatSetting settingValue = (this.lastYUp ? this.yUpSpeeds : this.yDownSpeeds)[this.lastPitchRange];
                    float floatValueValue = Math.max(1.5F, settingValue.getValue().floatValue() - 0.01F);
                    settingValue.setValue(floatValueValue);
                    ChatUtil.sendMessage("Скорость Pitch " + maximumTextValues[this.lastPitchRange] + "° снижена на 0.01");
                }
                this.lastX = lastX;
                this.lastY = lastY;
                this.lastZ = lastZ;
            }
        }
    }

    public void saveLastPos(double lastX, double lastY, double lastZ) {
        this.lastX = lastX;
        this.lastY = lastY;
        this.lastZ = lastZ;
    }

    private float getHorizontalSpeedForYaw(float floatValue) {
        this.lastYawRange = this.getAngleBracketIndex(floatValue);
        return this.xzSpeeds[this.lastYawRange].getValue().floatValue();
    }

    private float getVerticalSpeedForPitch(float floatValue) {
        this.lastPitchRange = this.getAngleBracketIndex(floatValue);
        this.lastYUp = floatValue < 0.0F;
        return (this.lastYUp ? this.yUpSpeeds : this.yDownSpeeds)[this.lastPitchRange].getValue().floatValue();
    }

    private float calculateFloat(float floatValue, float floatValueValue, boolean flag, boolean flagValue) {
        if (Math.abs(floatValue) > 55.0F) {
            return 1.55F;
        } else {
            float floatValueResult = this.calculateWrapDegrees(floatValueValue, flagValue);
            floatValueResult = this.calculateFloat(floatValue, floatValueResult);
            floatValueResult = Math.max(flag ? 1.65F : 1.6F, floatValueResult);
            return Math.min(floatValueResult, flag ? 1.9F : 2.2F);
        }
    }

    private float calculateWrapDegrees(float floatValue, boolean flag) {
        int intValue = calculateWrapDegrees(floatValue, integerValuesForCalculateWrapDegrees);
        if (intValue == -1) {
            return 1.6F;
        } else {
            float magnitude = Math.abs(MathHelper.wrapDegrees(floatValue) + -integerValuesForCalculateWrapDegrees[intValue]);
            float floatValueValue = 2.2F;
            float floatValueResult = 1.6F;
            float floatValueItem = 12.0F;
            float floatValueEntry = 0.0F;
            if (magnitude <= floatValueItem) {
                float floatValueCopy = magnitude / floatValueItem;
                floatValueEntry = floatValueValue + -((floatValueValue + -floatValueResult) * floatValueCopy);
            }
            float pitchBracketSpeed = getPitchBracketSpeed(magnitude);
            float floatValue2 = Math.max(floatValueEntry, pitchBracketSpeed);
            return flag ? Math.min(floatValue2, 1.8F) : floatValue2;
        }
    }

    private float calculateFloat(float floatValue, float floatValueValue) {
        int intValue = calculateWrapDegrees(floatValue, integerValuesForCalculateFloat);
        if (intValue == -1) {
            return floatValueValue;
        } else {
            float magnitude = Math.abs(Math.abs(floatValue) + -Math.abs(integerValuesForCalculateFloat[intValue]));
            if (magnitude < 30.0F) {
                floatValueValue += 0.4F * (1.0F + -(magnitude / 30.0F));
            }
            return floatValueValue;
        }
    }

    private static float getPitchBracketSpeed(float floatValue) {
        float[] values = new float[] { 4.0F, 8.0F, 11.0F, 15.0F, 21.0F, 28.0F };
        float[] valuesValue = new float[] { 2.2F, 2.1F, 2.0F, 1.9F, 1.8F, 1.7F, 1.6F };
        int intValue = 0;
        while (intValue < values.length && floatValue >= values[intValue]) {
            intValue++;
        }
        return valuesValue[intValue];
    }

    private static int calculateWrapDegrees(float floatValue, int[] values) {
        int intValue = -1;
        float MAX_VALUE = Float.MAX_VALUE;
        for (int index = 0; index < values.length; index++) {
            float magnitude = Math.abs(MathHelper.wrapDegrees(floatValue) + -values[index]);
            if (magnitude < MAX_VALUE) {
                MAX_VALUE = magnitude;
                intValue = index;
            }
        }
        return intValue;
    }

    private float calculateWrapDegrees(float floatValue, float floatValueValue) {
        float magnitude = Math.abs(floatValue);
        float magnitudeValue = Math.abs(MathHelper.wrapDegrees(floatValueValue) % 90.0F);
        float floatValueResult;
        if (magnitude >= 38.0F && magnitude <= 52.0F) {
            floatValueResult = 2.0F;
        } else if (magnitude >= 32.0F && magnitude <= 58.0F) {
            floatValueResult = 1.96F;
        } else if (magnitude >= 28.0F && magnitude <= 62.0F) {
            floatValueResult = 1.95F;
        } else if ((!(magnitudeValue >= 29.0F) || !(magnitudeValue <= 61.0F)) && (!(magnitude >= 29.0F) || !(magnitude <= 61.0F))) {
            if ((!(magnitudeValue >= 28.0F) || !(magnitudeValue <= 60.0F)) && (!(magnitude >= 28.0F) || !(magnitude <= 60.0F))) {
                if ((!(magnitudeValue >= 26.0F) || !(magnitudeValue <= 64.0F)) && (!(magnitude >= 26.0F) || !(magnitude <= 64.0F))) {
                    if ((!(magnitudeValue >= 24.0F) || !(magnitudeValue <= 66.0F)) && (!(magnitude >= 24.0F) || !(magnitude <= 66.0F))) {
                        if ((!(magnitudeValue >= 15.0F) || !(magnitudeValue <= 75.0F)) && (!(magnitude >= 15.0F) || !(magnitude <= 75.0F))) {
                            if ((!(magnitudeValue >= 13.0F) || !(magnitudeValue <= 77.0F)) && (!(magnitude >= 13.0F) || !(magnitude <= 77.0F))) {
                                if ((!(magnitudeValue >= 12.0F) || !(magnitudeValue <= 78.0F)) && (!(magnitude >= 12.0F) || !(magnitude <= 78.0F))) {
                                    if ((!(magnitudeValue >= 8.0F) || !(magnitudeValue <= 82.0F)) && (!(magnitude >= 11.0F) || !(magnitude <= 79.0F))) {
                                        if (magnitudeValue >= 5.0F && magnitudeValue <= 85.0F || magnitude >= 8.0F && magnitude <= 82.0F) {
                                            floatValueResult = 1.67F;
                                        } else {
                                            floatValueResult = 1.71F;
                                        }
                                    } else {
                                        floatValueResult = 1.72F;
                                    }
                                } else {
                                    floatValueResult = 1.72F;
                                }
                            } else {
                                floatValueResult = 1.72F;
                            }
                        } else {
                            floatValueResult = 1.72F;
                        }
                    } else {
                        floatValueResult = 1.72F;
                    }
                } else {
                    floatValueResult = 1.874F;
                }
            } else {
                floatValueResult = 1.954F;
            }
        } else {
            floatValueResult = 1.963F;
        }
        return floatValue > 15.0F ? floatValueResult + -0.068F : floatValueResult;
    }

    private float getSpecialPitchSpeed(float floatValue) {
        float magnitude = Math.abs(floatValue);
        if (magnitude >= 37.0F && magnitude <= 38.0F) {
            return 2.03F;
        } else if (magnitude >= 25.0F && magnitude <= 30.0F) {
            return 2.0F;
        } else if (magnitude >= 35.0F && magnitude <= 45.0F) {
            return 1.99F;
        } else if (magnitude >= 40.0F && magnitude <= 50.0F) {
            return 1.97F;
        } else if (magnitude >= 50.0F && magnitude <= 60.0F) {
            return 1.96F;
        } else if (magnitude >= 51.0F && magnitude <= 61.0F) {
            return 1.85F;
        } else {
            return magnitude >= 52.0F && magnitude <= 65.0F ? 1.8F : 1.59F;
        }
    }

    private float calculateWrapDegrees(float floatValue) {
        float magnitude = Math.abs(MathHelper.wrapDegrees(floatValue));
        if (magnitude > 90.0F) {
            magnitude = 180.0F + -magnitude;
        }
        if (magnitude > 45.0F) {
            magnitude = 90.0F + -magnitude;
        }
        return magnitude;
    }

    private int getAngleBracketIndex(float floatValue) {
        int intValue = MathHelper.ceil(this.calculateWrapDegrees(floatValue) / 9.0F) - 1;
        return MathHelper.clamp(intValue, 0, maximumTextValues.length - 1);
    }

    private void updateValue() {
        float[] values = new float[] { 1.61F, 1.66F, 1.77F, 1.8F, 1.79F };
        float[] valuesValue = new float[] { 1.6F, 1.61F, 1.69F, 1.72F, 1.94F };
        float[] valuesResult = new float[] { 1.97F, 2.11F, 2.2F, 2.15F, 2.08F };
        for (int index = 0; index < maximumTextValues.length; index++) {
            this.xzSpeeds[index].setValue(values[index]);
            FloatSetting setting = this.yUpSpeeds[index];
            float floatValue = valuesValue[index];
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(setting, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((FloatSetting) accessReceiver).setValue(((float) accessArguments[0]));
            });
            this.yDownSpeeds[index].setValue(valuesResult[index]);
        }
    }

    public ModeSetting getMode() {
        return this.mode;
    }

    public BooleanSetting getAutoDecrease() {
        return this.autoDecrease;
    }

    public FloatSetting[] getXzSpeeds() {
        return this.xzSpeeds;
    }

    public FloatSetting[] getYUpSpeeds() {
        return this.yUpSpeeds;
    }

    public FloatSetting[] getYDownSpeeds() {
        return this.yDownSpeeds;
    }

    public int getLastYawRange() {
        return this.lastYawRange;
    }

    public int getLastPitchRange() {
        return this.lastPitchRange;
    }

    public boolean isLastYUp() {
        return this.lastYUp;
    }

    public double getLastX() {
        return this.lastX;
    }

    public double getLastY() {
        return this.lastY;
    }

    public double getLastZ() {
        return this.lastZ;
    }

    public void setLastYawRange(int lastYawRange) {
        this.lastYawRange = lastYawRange;
    }

    public void setLastPitchRange(int lastPitchRange) {
        this.lastPitchRange = lastPitchRange;
    }

    public void setLastYUp(boolean lastYUp) {
        this.lastYUp = lastYUp;
    }

    public void setLastX(double lastX) {
        this.lastX = lastX;
    }

    public void setLastY(double lastY) {
        this.lastY = lastY;
    }

    public void setLastZ(double lastZ) {
        this.lastZ = lastZ;
    }
}
