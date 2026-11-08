package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.utils.world.EntityPrediction.EntityMotion;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityPrediction implements MinecraftAccess {

    private static final Map<UUID, EntityMotion> data = new ConcurrentHashMap<>();

    public static void updateEntity(LivingEntity entity) {
        EntityMotion entityMotion = data.computeIfAbsent(entity.getUuid(), input -> new EntityMotion());
        entityMotion.update(entity.getX(), entity.getY(), entity.getZ());
    }

    public static EntityMotion getData(LivingEntity entity) {
        return data.get(entity.getUuid());
    }

    public static Vec3d predict(LivingEntity entity, int intValue, float floatValue, boolean flag) {
        EntityMotion data = getData(entity);
        Vec3d position = new Vec3d(entity.getX(), entity.getY() + entity.getStandingEyeHeight() / 2.0F, entity.getZ());
        if (data == null) {
            return predictElytraPhysics(entity, position, intValue);
        } else {
            Vec3d resolvedForward = data.getResolvedForward();
            double lastSpeed = data.getLastSpeed();
            boolean speedChanged = data.isSpeedChanged();
            if (entity.isGliding()) {
                double doubleValue = Math.hypot(resolvedForward.x, resolvedForward.z) * 20.0;
                double doubleValueValue = Math.abs(resolvedForward.y) * 20.0;
                if (doubleValue <= 5.0 && doubleValueValue <= 5.0) {
                    return position;
                } else {
                    boolean flagValue = flag && entity.isGliding() && speedChanged;
                    float floatValueValue = flagValue ? intValue + 2 + floatValue : intValue;
                    Vec3d positionValue = position.add(resolvedForward.multiply(floatValueValue, floatValueValue, floatValueValue));
                    Vec3d positionResult = predictElytraPhysics(entity, position, intValue);
                    double clampedValue = MathHelper.clamp(lastSpeed / 50.0, 0.3, 0.9);
                    return new Vec3d(MathHelper.lerp(clampedValue, positionResult.x, positionValue.x), MathHelper.lerp(clampedValue, positionResult.y, positionValue.y), MathHelper.lerp(clampedValue, positionResult.z, positionValue.z));
                }
            } else {
                return lastSpeed > 1.0 ? position.add(resolvedForward.multiply(intValue, intValue, intValue)) : position;
            }
        }
    }

    public static Vec3d predict(LivingEntity entity, Vec3d position, int intValue) {
        EntityMotion data = getData(entity);
        if (data != null && entity.isGliding()) {
            Vec3d resolvedForward = data.getResolvedForward();
            double doubleValue = Math.hypot(resolvedForward.x, resolvedForward.z) * 20.0;
            double doubleValueValue = Math.abs(resolvedForward.y) * 20.0;
            return doubleValue <= 5.0 && doubleValueValue <= 5.0 ? position : position.add(resolvedForward.multiply(intValue, intValue, intValue));
        } else {
            return predictElytraPhysics(entity, position, intValue);
        }
    }

    public static Vec3d predictElytraPhysics(LivingEntity entity, Vec3d position, int intValue) {
        Vec3d velocity = entity.getVelocity();
        if (!entity.isGliding()) {
            return position.add(velocity.multiply(intValue, intValue, intValue));
        } else {
            double doubleValue = Math.hypot(entity.prevX - entity.getX(), entity.prevZ - entity.getZ()) * 20.0;
            double doubleValueValue = Math.abs(entity.getY() - entity.prevY) * 20.0;
            if (doubleValue <= 5.0 && doubleValueValue <= 5.0) {
                return position;
            } else {
                for (int index = 0; index < intValue; index++) {
                    Vec3d rotationVector = entity.getRotationVector();
                    float floatValue = (float) Math.toRadians(entity.getPitch());
                    double squareRoot = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
                    double length = velocity.length();
                    float floatValueValue = MathHelper.cos(floatValue);
                    floatValueValue = (float) (floatValueValue * floatValueValue * Math.min(1.0, rotationVector.length() / 0.4));
                    velocity = velocity.add(0.0, -0.08 * (-1.0 + floatValueValue * 0.75), 0.0);
                    if (velocity.y < 0.0 && squareRoot > 0.0) {
                        double doubleValueResult = velocity.y * -0.1 * floatValueValue;
                        velocity = velocity.add(rotationVector.x * doubleValueResult / squareRoot, doubleValueResult, rotationVector.z * doubleValueResult / squareRoot);
                    }
                    if (floatValue < 0.0F && squareRoot > 0.0) {
                        double doubleValueItem = length * -MathHelper.sin(floatValue) * 0.04;
                        velocity = velocity.add(-rotationVector.x * doubleValueItem / squareRoot, doubleValueItem * 3.2, -rotationVector.z * doubleValueItem / squareRoot);
                    }
                    if (squareRoot > 0.0) {
                        velocity = velocity.add((rotationVector.x / squareRoot * length - velocity.x) * 0.1, 0.0, (rotationVector.z / squareRoot * length - velocity.z) * 0.1);
                    }
                    velocity = velocity.multiply(0.99, 0.98, 0.99);
                    position = position.add(velocity);
                }
                return position;
            }
        }
    }

    public static Vec3d bypasselytrahacking(LivingEntity entity) {
        Vec3d position = entity.getPos().add(0.0, entity.getHeight() * 0.6F, 0.0);
        int intValue = 0;
        if (ModuleRegistry.elytraTarget != null) {
            intValue = (int) ModuleRegistry.elytraTarget.getPredictionMultiplier(entity);
        }
        return predict(entity, position, intValue);
    }

    public static void cleanup() {
        long currentTimeMillis = System.currentTimeMillis();
        data.entrySet().removeIf(input -> currentTimeMillis - input.getValue().getLastUpdate() > 10000L);
    }

    public static void clear() {
        data.clear();
    }

    public static class EntityMotion {

        private double serverX;

        private double serverY;

        private double serverZ;

        private double prevServerX;

        private double prevServerY;

        private double prevServerZ;

        private double backUpX;

        private double backUpY;

        private double backUpZ;

        private double lastSpeed;

        private double prevSpeed;

        private long lastUpdate;

        public Vec3d getResolvedPos() {
            return new Vec3d(this.serverX, this.serverY, this.serverZ);
        }

        public Vec3d getResolvedForward() {
            return new Vec3d(this.serverX - this.prevServerX, this.serverY - this.prevServerY, this.serverZ - this.prevServerZ);
        }

        public void update(double serverX, double serverY, double serverZ) {
            this.backUpX = this.prevServerX;
            this.backUpY = this.prevServerY;
            this.backUpZ = this.prevServerZ;
            this.prevServerX = this.serverX;
            this.prevServerY = this.serverY;
            this.prevServerZ = this.serverZ;
            this.serverX = serverX;
            this.serverY = serverY;
            this.serverZ = serverZ;
            this.prevSpeed = this.lastSpeed;
            this.lastSpeed = this.getResolvedForward().length() * 20.0;
            this.lastUpdate = System.currentTimeMillis();
        }

        public boolean isSpeedChanged() {
            return this.lastSpeed >= 20.0 || this.lastSpeed != this.prevSpeed && this.lastSpeed == 0.0;
        }

        public double getServerX() {
            return this.serverX;
        }

        public double getServerY() {
            return this.serverY;
        }

        public double getServerZ() {
            return this.serverZ;
        }

        public double getPrevServerX() {
            return this.prevServerX;
        }

        public double getPrevServerY() {
            return this.prevServerY;
        }

        public double getPrevServerZ() {
            return this.prevServerZ;
        }

        public double getBackUpX() {
            return this.backUpX;
        }

        public double getBackUpY() {
            return this.backUpY;
        }

        public double getBackUpZ() {
            return this.backUpZ;
        }

        public double getLastSpeed() {
            return this.lastSpeed;
        }

        public double getPrevSpeed() {
            return this.prevSpeed;
        }

        public long getLastUpdate() {
            return this.lastUpdate;
        }
    }
}
