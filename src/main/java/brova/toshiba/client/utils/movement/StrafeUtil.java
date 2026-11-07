package brova.toshiba.client.utils.movement;

import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class StrafeUtil {

    private static final MinecraftClient moving = MinecraftClient.getInstance();

    public static void setMotion(double motion) {
        if (moving.player != null) {
            double movementForward = moving.player.input.movementForward;
            double movementSideways = moving.player.input.movementSideways;
            float yaw = moving.player.getYaw();
            if (movementForward == 0.0 && movementSideways == 0.0) {
                moving.player.setVelocity(0.0, moving.player.getVelocity().y, 0.0);
            } else {
                if (movementForward != 0.0) {
                    if (movementSideways > 0.0) {
                        yaw += movementForward > 0.0 ? -45.0F : 45.0F;
                    } else if (movementSideways < 0.0) {
                        yaw += movementForward > 0.0 ? 45.0F : -45.0F;
                    }
                    movementSideways = 0.0;
                    if (movementForward > 0.0) {
                        movementForward = 1.0;
                    } else if (movementForward < 0.0) {
                        movementForward = -1.0;
                    }
                }
                double doubleValue = movementForward * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0F)) + movementSideways * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0F));
                double doubleValueValue = movementForward * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0F)) - movementSideways * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0F));
                moving.player.setVelocity(doubleValue, moving.player.getVelocity().y, doubleValueValue);
            }
        }
    }

    public static double getSpeed() {
        if (moving.player == null) {
            return 0.0;
        } else {
            Vec3d velocity = moving.player.getVelocity();
            return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        }
    }

    public static void setVelocity(double velocity) {
        double[] values = MovementUtil.calculateDirection(velocity);
        Objects.requireNonNull(moving.player).setVelocity(values[0], moving.player.getVelocity().getY(), values[1]);
    }

    public static void setVelocity(double doubleValue, double doubleValueValue) {
        double[] values = MovementUtil.calculateDirection(doubleValue);
        Objects.requireNonNull(moving.player).setVelocity(values[0], doubleValueValue, values[1]);
    }

    public static void strafe() {
        double speed = getSpeed();
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { speed }, (accessReceiver, accessArguments) -> {
            StrafeUtil.strafe(((double) accessArguments[0]));
        });
    }

    public static void strafe(double doubleValue) {
        if (moving.player != null) {
            float yaw = moving.player.getYaw();
            double movementForward = moving.player.input.movementForward;
            double movementSideways = moving.player.input.movementSideways;
            if (movementForward == 0.0 && movementSideways == 0.0) {
                moving.player.setVelocity(0.0, moving.player.getVelocity().y, 0.0);
            } else {
                if (movementForward != 0.0) {
                    if (movementSideways > 0.0) {
                        yaw += movementForward > 0.0 ? -45.0F : 45.0F;
                    } else if (movementSideways < 0.0) {
                        yaw += movementForward > 0.0 ? 45.0F : -45.0F;
                    }
                    movementSideways = 0.0;
                    movementForward = movementForward > 0.0 ? 1.0 : -1.0;
                }
                double doubleValueValue = Math.toRadians(yaw + 90.0F);
                double doubleValueResult = movementForward * doubleValue * Math.cos(doubleValueValue) + movementSideways * doubleValue * Math.sin(doubleValueValue);
                double doubleValueItem = movementForward * doubleValue * Math.sin(doubleValueValue) - movementSideways * doubleValue * Math.cos(doubleValueValue);
                moving.player.setVelocity(doubleValueResult, moving.player.getVelocity().y, doubleValueItem);
            }
        }
    }

    public static boolean isMoving() {
        return moving.player == null ? false : moving.player.input.movementForward != 0.0F || moving.player.input.movementSideways != 0.0F;
    }
}
