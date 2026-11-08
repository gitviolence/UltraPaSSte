package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Vector3f;

public final class EntityRaycastUtil implements MinecraftAccess {

    public static HitResult rayTrace(double doubleValue, float floatValue, float floatValueValue, Entity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d vectorForRotation = getVectorForRotation(floatValueValue, floatValue);
        Vec3d position = eyePos.add(vectorForRotation.x * doubleValue, vectorForRotation.y * doubleValue, vectorForRotation.z * doubleValue);
        return mc.world.raycast(new RaycastContext(eyePos, position, ShapeType.OUTLINE, FluidHandling.NONE, entity));
    }

    public static BlockHitResult raycast(Vec3d position, Vec3d positionValue, ShapeType shapeType) {
        return raycast(position, positionValue, shapeType, mc.player);
    }

    public static BlockHitResult raycast(Vec3d position, Vec3d positionValue, ShapeType shapeType, Entity entity) {
        return mc.world.raycast(new RaycastContext(position, positionValue, shapeType, FluidHandling.NONE, entity));
    }

    public static boolean rayTrace(Vec3d position, double doubleValue, Box box) {
        Vec3d eyePos = Objects.requireNonNull(mc.player).getEyePos();
        return box.contains(eyePos) || box.raycast(eyePos, eyePos.add(position.multiply(doubleValue))).isPresent();
    }

    public static boolean isViewEntity(LivingEntity entity, float floatValue, float floatValueValue, float floatValueResult, boolean flag) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity != null && mc.world != null) {
            double doubleValue = floatValueResult * floatValueResult;
            Vec3d eyePos = cameraEntity.getEyePos();
            Vector3f vector3f = calculateViewVector(floatValue, floatValueValue);
            vector3f.mul(floatValueResult, floatValueResult, floatValueResult);
            Vec3d position = eyePos.add(vector3f.x, vector3f.y, vector3f.z);
            Box boundingBox = entity.getBoundingBox();
            EntityHitResult entityHitResult = ProjectileUtil.raycast(cameraEntity, eyePos, position, boundingBox, input -> !input.isSpectator() && input.isAlive() && input == entity, doubleValue);
            return entityHitResult != null;
        } else {
            return false;
        }
    }

    public static Vector3f calculateViewVector(float floatValue, float floatValueValue) {
        float floatValueResult = floatValueValue * (float) (Math.PI / 180.0);
        float floatValueItem = -floatValue * (float) (Math.PI / 180.0);
        float floatValueEntry = MathHelper.cos(floatValueItem);
        float floatValueCopy = MathHelper.sin(floatValueItem);
        float floatValue2 = MathHelper.cos(floatValueResult);
        float floatValue3 = MathHelper.sin(floatValueResult);
        return new Vector3f(floatValueCopy * floatValue2, -floatValue3, floatValueEntry * floatValue2);
    }

    public static Vec3d getVectorForRotation(float floatValue, float floatValueValue) {
        float floatValueResult = -floatValueValue * (float) (Math.PI / 180.0) - (float) Math.PI;
        float floatValueItem = -floatValue * (float) (Math.PI / 180.0);
        float floatValueEntry = MathHelper.cos(floatValueResult);
        float floatValueCopy = MathHelper.sin(floatValueResult);
        float floatValue2 = -MathHelper.cos(floatValueItem);
        float floatValue3 = MathHelper.sin(floatValueItem);
        return new Vec3d(floatValueCopy * floatValue2, floatValue3, floatValueEntry * floatValue2);
    }

    public static boolean rayTraceSingleEntity(float floatValue, float floatValueValue, double doubleValue, Entity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d rotationVector = mc.player.getRotationVector(floatValueValue, floatValue);
        Vec3d position = eyePos.add(rotationVector.multiply(doubleValue));
        Box boundingBox = entity.getBoundingBox();
        return boundingBox.contains(eyePos) || boundingBox.raycast(eyePos, position).isPresent();
    }

    private EntityRaycastUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
