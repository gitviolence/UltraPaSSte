package brova.toshiba.client.utils.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ElytraBoostUtil {

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private static final float[] horizontalBoostTable = new float[] { 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.62F, 1.62F, 1.62F, 1.63F, 1.63F, 1.64F, 1.65F, 1.65F, 1.66F, 1.67F, 1.68F, 1.69F, 1.7F, 1.71F, 1.72F, 1.73F, 1.73F, 1.75F, 1.76F, 1.78F, 1.79F, 1.81F, 1.83F, 1.85F, 1.87F, 1.89F, 1.91F, 1.93F, 1.95F, 1.98F, 2.01F, 2.03F, 2.06F, 2.09F, 2.12F, 2.16F, 2.19F, 2.23F, 2.27F, 2.31F, 2.35F, 2.31F, 2.27F, 2.23F, 2.19F, 2.16F, 2.12F, 2.09F, 2.06F, 2.03F, 2.01F, 1.98F, 1.95F, 1.93F, 1.89F, 1.87F, 1.85F, 1.83F, 1.81F, 1.79F, 1.78F, 1.76F, 1.75F, 1.73F, 1.72F, 1.71F, 1.7F, 1.69F, 1.68F, 1.67F, 1.66F, 1.65F, 1.64F, 1.63F, 1.63F, 1.63F, 1.62F, 1.62F, 1.62F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F };

    private static final float[] verticalBoostTable = new float[] { 1.61F, 1.61F, 1.61F, 1.62F, 1.62F, 1.62F, 1.63F, 1.63F, 1.64F, 1.65F, 1.65F, 1.66F, 1.67F, 1.68F, 1.69F, 1.7F, 1.71F, 1.72F, 1.73F, 1.73F, 1.75F, 1.76F, 1.78F, 1.79F, 1.81F, 1.83F, 1.85F, 1.87F, 1.89F, 1.91F, 1.93F, 1.95F, 1.98F, 2.01F, 2.03F, 2.06F, 2.09F, 2.12F, 2.16F, 2.19F, 2.23F, 2.24F, 2.21F, 2.21F, 2.21F, 2.23F, 2.23F, 2.19F, 2.16F, 2.12F, 2.09F, 2.06F, 2.03F, 2.01F, 1.98F, 1.95F, 1.93F, 1.89F, 1.87F, 1.85F, 1.83F, 1.81F, 1.79F, 1.78F, 1.76F, 1.75F, 1.73F, 1.72F, 1.71F, 1.7F, 1.69F, 1.68F, 1.67F, 1.66F, 1.65F, 1.64F, 1.63F, 1.63F, 1.63F, 1.62F, 1.62F, 1.62F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F, 1.61F };

    public static Vec3d getBoost(LivingEntity entity) {
        float floatValue = calculateWrapDegreesWithYaw(entity);
        Vec3d rotationVector = entity.getRotationVector();
        Vec3d position = Vec3d.fromPolar(entity.getPitch(), entity.getYaw()).multiply(floatValue);
        float floatValueValue = entity.getPitch() * (float) (Math.PI / 180.0);
        double squareRoot = Math.sqrt(rotationVector.x * rotationVector.x + rotationVector.z * rotationVector.z);
        double doubleValue = position.horizontalLength();
        boolean flag = entity.getVelocity().y <= 0.0;
        double doubleValueValue = flag && entity.hasStatusEffect(StatusEffects.SLOW_FALLING) ? Math.min(entity.getFinalGravity(), 0.01) : entity.getFinalGravity();
        double doubleValueResult = MathHelper.square(Math.cos(floatValueValue));
        position = position.add(0.0, doubleValueValue * (-1.0 + doubleValueResult * 0.75), 0.0);
        if (position.y < 0.0 && squareRoot > 0.0) {
            double doubleValueItem = position.y * -0.1 * doubleValueResult;
            position = position.add(rotationVector.x * doubleValueItem / squareRoot, doubleValueItem, rotationVector.z * doubleValueItem / squareRoot);
        }
        if (floatValueValue < 0.0F && squareRoot > 0.0) {
            double doubleValueEntry = doubleValue * -MathHelper.sin(floatValueValue) * 0.04;
            position = position.add(-rotationVector.x * doubleValueEntry / squareRoot, doubleValueEntry * 3.2, -rotationVector.z * doubleValueEntry / squareRoot);
        }
        if (squareRoot > 0.0) {
            position = position.add((rotationVector.x / squareRoot * doubleValue - position.x) * 0.1, 0.0, (rotationVector.z / squareRoot * doubleValue - position.z) * 0.1);
        }
        double length = position.length();
        return new Vec3d(length, length, length).multiply(0.99, 0.98, 0.99);
    }

    private static float calculateWrapDegreesWithYaw(LivingEntity entity) {
        float magnitude = Math.abs(MathHelper.wrapDegrees(entity.getYaw()));
        float floatValue = foldAngleToFirstQuadrant(magnitude);
        float magnitudeValue = Math.abs(clampPitch(entity.getPitch()));
        if (magnitudeValue >= 70.0F && magnitudeValue <= 90.0F) {
            return 1.615F;
        } else {
            float floatValueValue = horizontalBoostTable[Math.min((int) Math.ceil(floatValue), 90)];
            int intValue = Math.min((int) Math.ceil(magnitudeValue), verticalBoostTable.length - 1);
            float floatValueResult = verticalBoostTable[intValue];
            float floatValueItem = magnitudeValue >= 75.0F ? floatValueResult : Math.max(floatValueValue, floatValueResult);
            return Math.max(floatValueItem, magnitudeValue >= 75.0F ? 1.5F : 1.61F);
        }
    }

    private static float foldAngleToFirstQuadrant(float floatValue) {
        float floatValueValue = floatValue > 180.0F ? 360.0F - floatValue : floatValue;
        return floatValueValue > 90.0F ? 180.0F - floatValueValue : floatValueValue;
    }

    private static float clampPitch(float floatValue) {
        return Math.max(-90.0F, Math.min(90.0F, floatValue));
    }

    public static Vec3d getBoostAntiTarget(LivingEntity entity, float floatValue) {
        float magnitude = Math.abs((entity.getYaw() - 360.0F) % 360.0F);
        float pitch = entity.getPitch();
        float magnitudeValue = Math.abs(pitch);
        float floatValueValue = 0.0F;
        if (magnitudeValue >= 30.0F && magnitudeValue <= 50.0F) {
            floatValueValue = 0.15F;
        } else if (magnitudeValue >= 25.0F && magnitudeValue <= 55.0F) {
            floatValueValue = 0.1F;
        } else if (magnitudeValue >= 20.0F && magnitudeValue <= 60.0F) {
            floatValueValue = 0.05F;
        }
        float floatValueResult = floatValue + floatValueValue;
        float[] values = new float[] { 45.0F, 135.0F, 225.0F, 315.0F };
        float floatValueItem = 9999.0F;
        for (float floatValueEntry : values) {
            float magnitudeResult = Math.abs(magnitude - floatValueEntry);
            if (magnitudeResult < floatValueItem) {
                floatValueItem = magnitudeResult;
            }
        }
        if (floatValueItem < 15.0F) {
            floatValueResult += 0.1F;
        } else if (floatValueItem < 25.0F) {
            floatValueResult += 0.05F;
        }
        floatValueResult = Math.min(floatValueResult, 2.8F);
        return new Vec3d(floatValueResult, floatValueResult, floatValueResult);
    }

    public static Vec3d getBoostAntiTargetFast(LivingEntity entity) {
        float magnitude = Math.abs((entity.getYaw() - 360.0F) % 360.0F);
        float pitch = entity.getPitch();
        float magnitudeValue = Math.abs(pitch);
        float floatValue = 2.5F;
        float floatValueValue = 2.3F;
        if (magnitudeValue >= 35.0F && magnitudeValue <= 50.0F) {
            floatValue = 2.7F;
            floatValueValue = 2.5F;
        } else if (magnitudeValue >= 30.0F && magnitudeValue <= 55.0F) {
            floatValue = 2.6F;
            floatValueValue = 2.4F;
        }
        float[] values = new float[] { 45.0F, 135.0F, 225.0F, 315.0F };
        float floatValueResult = 9999.0F;
        for (float floatValueItem : values) {
            float magnitudeResult = Math.abs(magnitude - floatValueItem);
            if (magnitudeResult < floatValueResult) {
                floatValueResult = magnitudeResult;
            }
        }
        if (floatValueResult < 20.0F) {
            floatValue += 0.15F;
        }
        return new Vec3d(floatValue, floatValueValue, floatValue);
    }

    public static Vec3d getBoostAntiTargetWithAura(LivingEntity entity, float floatValue, float floatValueValue, float floatValueResult) {
        float magnitude = Math.abs(floatValue);
        float floatValueItem = floatValueResult;
        float floatValueEntry;
        if (magnitude >= 38.0F && magnitude <= 52.0F) {
            floatValueItem = Math.min(floatValueResult + 0.2F, 2.7F);
            floatValueEntry = Math.min(floatValueResult + 0.15F, 2.5F);
        } else if (magnitude >= 30.0F && magnitude <= 60.0F) {
            floatValueItem = Math.min(floatValueResult + 0.1F, 2.6F);
            floatValueEntry = Math.min(floatValueResult + 0.1F, 2.4F);
        } else if (magnitude >= 25.0F && magnitude <= 65.0F) {
            floatValueEntry = floatValueResult - 0.05F;
        } else {
            floatValueItem = floatValueResult - 0.1F;
            floatValueEntry = floatValueResult - 0.15F;
        }
        return new Vec3d(floatValueItem, floatValueEntry, floatValueItem);
    }

    public static Vec3d getBoostslime(LivingEntity entity) {
        return getBoostCustom(entity, 42.0F);
    }

    public static Vec3d getBoostbravo(LivingEntity entity) {
        return getBoostCustom(entity, 39.0F);
    }

    public static Vec3d getBoostrw(LivingEntity entity) {
        return getBoostCustom(entity, 33.2F);
    }

    public static Vec3d getBoostGrim(LivingEntity entity) {
        float floatValue = 1.504F;
        return new Vec3d(floatValue, floatValue, floatValue);
    }

    public static Vec3d getBoostCustom(LivingEntity entity, float floatValue) {
        float floatValueValue = floatValue / 20.0F;
        float magnitude = Math.abs((entity.getYaw() - 360.0F) % 360.0F);
        float pitch = entity.getPitch();
        float floatValueResult = Math.min(floatValueValue * 0.7F, 1.67F);
        float[] values = new float[] { 45.0F, 135.0F, 225.0F, 315.0F };
        float floatValueItem = 9999.0F;
        for (float floatValueEntry : values) {
            float magnitudeValue = Math.abs(magnitude - floatValueEntry);
            if (magnitudeValue < floatValueItem) {
                floatValueItem = magnitudeValue;
            }
        }
        float floatValueCopy = 1.0F - floatValueItem / 45.0F;
        floatValueCopy = Math.max(0.0F, Math.min(1.0F, floatValueCopy));
        float pitchBoostMultiplier = getPitchBoostMultiplier(pitch);
        float floatValue2 = floatValueCopy * pitchBoostMultiplier;
        float floatValue3 = floatValueResult + (floatValueValue - floatValueResult) * floatValue2;
        Vec3d rotationVector = entity.getRotationVector();
        Vec3d position = Vec3d.fromPolar(pitch, entity.getYaw()).multiply(floatValue3);
        float floatValue4 = pitch * (float) (Math.PI / 180.0);
        double squareRoot = Math.sqrt(rotationVector.x * rotationVector.x + rotationVector.z * rotationVector.z);
        double doubleValue = position.horizontalLength();
        boolean flag = entity.getVelocity().y <= 0.0;
        double doubleValueValue = flag && entity.hasStatusEffect(StatusEffects.SLOW_FALLING) ? Math.min(entity.getFinalGravity(), 0.01) : entity.getFinalGravity();
        double doubleValueResult = MathHelper.square(Math.cos(floatValue4));
        position = position.add(0.0, doubleValueValue * (-1.0 + doubleValueResult * 0.75), 0.0);
        if (position.y < 0.0 && squareRoot > 0.0) {
            double doubleValueItem = position.y * -0.1 * doubleValueResult;
            position = position.add(rotationVector.x * doubleValueItem / squareRoot, doubleValueItem, rotationVector.z * doubleValueItem / squareRoot);
        }
        if (floatValue4 < 0.0F && squareRoot > 0.0) {
            double doubleValueEntry = doubleValue * -MathHelper.sin(floatValue4) * 0.04;
            position = position.add(-rotationVector.x * doubleValueEntry / squareRoot, doubleValueEntry * 3.2, -rotationVector.z * doubleValueEntry / squareRoot);
        }
        if (squareRoot > 0.0) {
            position = position.add((rotationVector.x / squareRoot * doubleValue - position.x) * 0.1, 0.0, (rotationVector.z / squareRoot * doubleValue - position.z) * 0.1);
        }
        double length = position.length();
        return new Vec3d(length, length, length).multiply(0.99, 0.98, 0.99);
    }

    public static Vec3d getBoostFixedBps(LivingEntity entity, float floatValue) {
        float floatValueValue = floatValue / 20.0F;
        return new Vec3d(floatValueValue, floatValueValue, floatValueValue).multiply(0.99, 0.98, 0.99);
    }

    private static float getPitchBoostMultiplier(float floatValue) {
        float magnitude = Math.abs(floatValue);
        if (magnitude <= 5.0F) {
            return 1.0F;
        } else if (magnitude <= 15.0F) {
            return 0.95F;
        } else if (magnitude <= 25.0F) {
            return 0.85F;
        } else if (magnitude <= 35.0F) {
            return 0.75F;
        } else if (magnitude <= 45.0F) {
            return 0.65F;
        } else if (magnitude <= 55.0F) {
            return 0.55F;
        } else if (magnitude <= 65.0F) {
            return 0.45F;
        } else {
            return magnitude <= 75.0F ? 0.35F : 0.25F;
        }
    }

    private ElytraBoostUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
