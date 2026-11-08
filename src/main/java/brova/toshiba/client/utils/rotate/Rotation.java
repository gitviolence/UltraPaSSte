package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Rotation implements MinecraftAccess {

    private float yaw;

    private float pitch;

    public Rotation(Entity entity) {
        this.yaw = entity.getYaw();
        this.pitch = entity.getPitch();
    }

    public float getDelta(Rotation rotation) {
        float floatValue = MathHelper.wrapDegrees(rotation.getYaw() - this.yaw);
        float floatValueValue = rotation.getPitch() - this.pitch;
        return (float) Math.hypot(Math.abs(floatValue), Math.abs(floatValueValue));
    }

    public double getDeltaDouble(Rotation rotation) {
        double doubleValue = MathHelper.wrapDegrees(rotation.getYaw() - this.yaw);
        double doubleValueValue = MathHelper.wrapDegrees(rotation.getPitch() - this.pitch);
        return Math.hypot(doubleValue, doubleValueValue);
    }

    public static Vector2f camera() {
        return new Vector2f(cameraYaw(), cameraPitch());
    }

    public static float cameraYaw() {
        return MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw() + (mc.gameRenderer.getCamera().isThirdPerson() ? 180 : 0));
    }

    public static float cameraPitch() {
        return (mc.gameRenderer.getCamera().isThirdPerson() ? -1 : 1) * mc.gameRenderer.getCamera().getPitch();
    }

    public static Rotation from(PlayerEntity player, Entity entity) {
        Vec3d cameraPosVec = player.getCameraPosVec(0.0F);
        Vec3d position = entity.getPos().add(0.0, entity.getHeight() * 0.5, 0.0);
        double doubleValue = position.x - cameraPosVec.x;
        double doubleValueValue = position.y - cameraPosVec.y;
        double doubleValueResult = position.z - cameraPosVec.z;
        double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueResult * doubleValueResult);
        float floatValue = (float) Math.toDegrees(Math.atan2(doubleValueResult, doubleValue)) - 90.0F;
        float floatValueValue = (float) (-Math.toDegrees(Math.atan2(doubleValueValue, squareRoot)));
        return new Rotation(floatValue, floatValueValue);
    }

    public static Rotation from(Vec3d position, Vec3d positionValue) {
        Vec3d positionResult = positionValue.subtract(position);
        double squareRoot = Math.sqrt(positionResult.x * positionResult.x + positionResult.z * positionResult.z);
        float floatValue = (float) Math.toDegrees(Math.atan2(positionResult.z, positionResult.x)) - 90.0F;
        float floatValueValue = (float) (-Math.toDegrees(Math.atan2(positionResult.y, squareRoot)));
        return new Rotation(MathHelper.wrapDegrees(floatValue), MathHelper.clamp(floatValueValue, -90.0F, 90.0F));
    }

    public final Vec3d toVector() {
        float floatValue = this.pitch * (float) (Math.PI / 180.0);
        float floatValueValue = -this.yaw * (float) (Math.PI / 180.0);
        float floatValueResult = MathHelper.cos(floatValueValue);
        float floatValueItem = MathHelper.sin(floatValueValue);
        float floatValueEntry = MathHelper.cos(floatValue);
        float floatValueCopy = MathHelper.sin(floatValue);
        return new Vec3d(floatValueItem * floatValueEntry, -floatValueCopy, floatValueResult * floatValueEntry);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object value) {
        if (value == this) {
            return true;
        } else if (value instanceof Rotation rotation) {
            if (!rotation.canEqual(this)) {
                return false;
            } else {
                return Float.compare(this.getYaw(), rotation.getYaw()) != 0 ? false : Float.compare(this.getPitch(), rotation.getPitch()) == 0;
            }
        } else {
            return false;
        }
    }

    public boolean canEqual(Object value) {
        return value instanceof Rotation;
    }

    @Override
    public int hashCode() {
        byte byteValue = 59;
        int intValue = 1;
        intValue = intValue * 59 + Float.floatToIntBits(this.getYaw());
        return intValue * 59 + Float.floatToIntBits(this.getPitch());
    }

    @Override
    public String toString() {
        return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

    public Rotation() {
    }

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
