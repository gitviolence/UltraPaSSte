package brova.toshiba.client.storages.implement;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.RotationEvent;
import brova.toshiba.client.event.impl.ViewRotationEvent;
import net.minecraft.util.math.MathHelper;

public final class FreeLookStorage implements MinecraftAccess {

    private static boolean active;

    private static float freeYaw;

    private static float freePitch;

    public FreeLookStorage() {
        EventBus.register(this);
    }

    public static boolean isActive() {
        return active;
    }

    @EventHandler
    public void onLook(RotationEvent event) {
        if (active) {
            double pitch = event.getPitch();
            double yaw = event.getYaw();
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { yaw, pitch }, (accessReceiver, accessArguments) -> {
                ((FreeLookStorage) accessReceiver).updateDouble(((double) accessArguments[0]), ((double) accessArguments[1]));
            });
            event.cancel();
        }
    }

    @EventHandler
    public void onRotation(ViewRotationEvent event) {
        if (active) {
            event.setYaw(freeYaw);
            float floatValue = freePitch;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(event, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((ViewRotationEvent) accessReceiver).setPitch(((float) accessArguments[0]));
            });
        } else {
            freeYaw = event.getYaw();
            freePitch = event.getPitch();
        }
    }

    private void updateDouble(double doubleValue, double doubleValueValue) {
        freePitch = MathHelper.clamp((float) (freePitch + doubleValueValue * 0.15), -90.0F, 90.0F);
        freeYaw = (float) (freeYaw + doubleValue * 0.15);
    }

    public static void setActive(boolean activeValue) {
        active = activeValue;
    }

    public static float getFreeYaw() {
        return freeYaw;
    }

    public static float getFreePitch() {
        return freePitch;
    }

    public static void setFreeYaw(float freeYawValue) {
        freeYaw = freeYawValue;
    }

    public static void setFreePitch(float freePitchValue) {
        freePitch = freePitchValue;
    }
}
