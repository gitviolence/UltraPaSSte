package brova.toshiba.client.utils.movement;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MovementUtil implements MinecraftAccess {

    public static boolean hasPlayerMovement() {
        return mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F;
    }

    public static double[] calculateDirection(double doubleValue) {
        float movementForward = mc.player.input.movementForward;
        float movementSideways = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();
        if (movementForward != 0.0F) {
            if (movementSideways > 0.0F) {
                yaw += movementForward > 0.0F ? -45.0F : 45.0F;
            } else if (movementSideways < 0.0F) {
                yaw += movementForward > 0.0F ? 45.0F : -45.0F;
            }
            movementSideways = 0.0F;
            movementForward = movementForward > 0.0F ? 1.0F : -1.0F;
        }
        double doubleValueValue = Math.sin(Math.toRadians(yaw + 90.0F));
        double doubleValueResult = Math.cos(Math.toRadians(yaw + 90.0F));
        double doubleValueItem = movementForward * doubleValue * doubleValueResult + movementSideways * doubleValue * doubleValueValue;
        double doubleValueEntry = movementForward * doubleValue * doubleValueValue - movementSideways * doubleValue * doubleValueResult;
        return new double[] { doubleValueItem, doubleValueEntry };
    }

    public static double getSpeedSqrt(Entity entity) {
        double doubleValue = entity.getX() - entity.prevX;
        double doubleValueValue = entity.getY() - entity.prevY;
        double doubleValueResult = entity.getZ() - entity.prevZ;
        return Math.sqrt(doubleValue * doubleValue + doubleValueResult * doubleValueResult + doubleValueValue * doubleValueValue);
    }

    public static void setVelocity(double velocity) {
        double[] values = calculateDirection(velocity);
        Objects.requireNonNull(mc.player).setVelocity(values[0], mc.player.getVelocity().getY(), values[1]);
    }

    public static void setVelocity(double doubleValue, double doubleValueValue) {
        double[] values = calculateDirection(doubleValue);
        Objects.requireNonNull(mc.player).setVelocity(values[0], doubleValueValue, values[1]);
    }

    public static double getDegreesRelativeToView(Vec3d position, float floatValue) {
        float floatValueValue = (float) Math.atan2(-position.x, position.z);
        double doubleValue = Math.toRadians(MathHelper.wrapDegrees(floatValue));
        return Math.toDegrees(MathHelper.wrapDegrees(floatValueValue - doubleValue));
    }

    public static PlayerInput getDirectionalInputForDegrees(PlayerInput playerInput, double doubleValue, float floatValue) {
        boolean flag = playerInput.forward();
        boolean flagValue = playerInput.backward();
        boolean flagResult = playerInput.left();
        boolean flagItem = playerInput.right();
        if (doubleValue >= -90.0F + floatValue && doubleValue <= 90.0F - floatValue) {
            flag = true;
        } else if (doubleValue < -90.0F - floatValue || doubleValue > 90.0F + floatValue) {
            flagValue = true;
        }
        if (doubleValue >= 0.0F + floatValue && doubleValue <= 180.0F - floatValue) {
            flagItem = true;
        } else if (doubleValue >= -180.0F + floatValue && doubleValue <= 0.0F - floatValue) {
            flagResult = true;
        }
        return new PlayerInput(flag, flagValue, flagResult, flagItem, playerInput.jump(), playerInput.sneak(), playerInput.sprint());
    }

    public static void fixMovementFocus(DirectionalInput directionalInput, float floatValue) {
        float forward = directionalInput.getForward();
        float strafe = directionalInput.getStrafe();
        if (forward != 0.0F || strafe != 0.0F) {
            double doubleValue = MathHelper.wrapDegrees(Math.toDegrees(direction(floatValue, forward, strafe)));
            float floatValueValue = 0.0F;
            float floatValueResult = 0.0F;
            float MAX_VALUE = Float.MAX_VALUE;
            for (float index = -1.0F; index <= 1.0F; index++) {
                for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                    if (index != 0.0F || indexValue != 0.0F) {
                        double doubleValueValue = MathHelper.wrapDegrees(Math.toDegrees(direction(floatValue, index, indexValue)));
                        float magnitude = Math.abs(MathHelper.wrapDegrees((float) (doubleValue - doubleValueValue)));
                        if (magnitude < MAX_VALUE) {
                            MAX_VALUE = magnitude;
                            floatValueValue = index;
                            floatValueResult = indexValue;
                        }
                    }
                }
            }
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(directionalInput, new Object[] { floatValueValue }, (accessReceiver, accessArguments) -> {
                ((DirectionalInput) accessReceiver).setForward(((float) accessArguments[0]));
            });
            directionalInput.setStrafe(floatValueResult);
        }
    }

    public static void fixMovementFree(DirectionalInput directionalInput) {
        float forward = directionalInput.getForward();
        float strafe = directionalInput.getStrafe();
        double doubleValue = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.isGliding() ? mc.player.getYaw() : FreeLookStorage.getFreeYaw(), forward, strafe)));
        if (forward != 0.0F || strafe != 0.0F) {
            float floatValue = 0.0F;
            float floatValueValue = 0.0F;
            float MAX_VALUE = Float.MAX_VALUE;
            for (float index = -1.0F; index <= 1.0F; index++) {
                for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                    if (indexValue != 0.0F || index != 0.0F) {
                        double doubleValueValue = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.getYaw(), index, indexValue)));
                        double magnitude = Math.abs(doubleValue - doubleValueValue);
                        if (magnitude < MAX_VALUE) {
                            MAX_VALUE = (float) magnitude;
                            floatValue = index;
                            floatValueValue = indexValue;
                        }
                    }
                }
            }
            directionalInput.setForward(floatValue);
            directionalInput.setStrafe(floatValueValue);
        }
    }

    public static double direction(float floatValue, float floatValueValue, float floatValueResult) {
        if (floatValueValue < 0.0F) {
            floatValue += 180.0F;
        }
        float floatValueItem = 1.0F;
        if (floatValueValue < 0.0F) {
            floatValueItem = -0.5F;
        }
        if (floatValueValue > 0.0F) {
            floatValueItem = 0.5F;
        }
        if (floatValueResult > 0.0F) {
            floatValue -= 90.0F * floatValueItem;
        }
        if (floatValueResult < 0.0F) {
            floatValue += 90.0F * floatValueItem;
        }
        return Math.toRadians(floatValue);
    }

    public static PlayerInput getDirectionalInputForDegrees(PlayerInput playerInput, double doubleValue) {
        return getDirectionalInputForDegrees(playerInput, doubleValue, 20.0F);
    }

    private MovementUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
