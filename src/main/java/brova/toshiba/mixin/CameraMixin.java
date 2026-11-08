package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.ViewRotationEvent;
import brova.toshiba.client.modules.impl.render.CustomSize;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void redirectSetPos(Camera instance, double x, double y, double z, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        CustomSize customSize = CustomSize.INSTANCE;
        if (customSize != null && customSize.isEnable() && focusedEntity != null && customSize.shouldScale(focusedEntity.getName().getString())) {
            double entityY = MathHelper.lerp((double) tickDelta, focusedEntity.prevY, focusedEntity.getY());
            double eyeHeight = y - entityY;
            y = entityY + eyeHeight * customSize.getScale();
        }
        ((ICameraMixin) instance).setCustomPos(x, y, z);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void redirectSetRotation(Camera instance, float yaw, float pitch, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        ViewRotationEvent event = new ViewRotationEvent(yaw, pitch, tickDelta);
        EventBus.invoke(event);
        float newYaw = event.getYaw();
        float newPitch = event.getPitch();
        if (thirdPerson && inverseView) {
            newYaw += 180.0F;
            newPitch = -newPitch;
        }
        ((ICameraMixin) instance).setCustomRotation(newYaw, newPitch);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
    private float redirectClipToSpace(Camera instance, float distance, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        return !thirdPerson ? ((ICameraMixin) instance).setClipToSpace(distance) : ((ICameraMixin) instance).setClipToSpace(distance);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V"))
    private void redirectMoveBy(Camera instance, float x, float y, float z, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        ((ICameraMixin) instance).setCustomMoveBy(x, y, z);
    }
}
