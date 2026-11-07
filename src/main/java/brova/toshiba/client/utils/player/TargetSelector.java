package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.world.NearestPointUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class TargetSelector implements MinecraftAccess {

    private LivingEntity target;

    public DoubleSupplier range = () -> 3.0;

    public DoubleSupplier elytraRange = () -> 30.0;

    public BooleanSupplier throughWalls = () -> true;

    public BooleanSupplier elytraHighPriority = () -> true;

    public BooleanSupplier saveCurrentTarget = () -> true;

    public List<TargetRule<?>> entityFilters = new ArrayList<>();

    public LivingEntity findTarget() {
        if (mc.player == null || mc.world == null) {
            return null;
        } else if (this.saveCurrentTarget.getAsBoolean() && this.target != null && this.isValidTarget(this.target)) {
            return this.target;
        } else {
            List<LivingEntity> entities = this.findListFromEntities();
            if (entities.isEmpty()) {
                this.target = null;
                return null;
            } else {
                entities.sort((leftEntity, rightEntity) -> {
                    int intValue = this.calculateInstanceWithType(leftEntity);
                    int intValueValue = this.calculateInstanceWithType(rightEntity);
                    if (intValue != intValueValue) {
                        return Integer.compare(intValueValue, intValue);
                    } else {
                        if (this.elytraHighPriority.getAsBoolean()) {
                            boolean itemWithEquippedStack = this.isItemWithEquippedStack(leftEntity);
                            boolean itemWithEquippedStackValue = this.isItemWithEquippedStack(rightEntity);
                            if (itemWithEquippedStack != itemWithEquippedStackValue) {
                                return itemWithEquippedStack ? -1 : 1;
                            }
                        }
                        return Float.compare(this.calculateYawWithCamera(leftEntity), this.calculateYawWithCamera(rightEntity));
                    }
                });
                this.target = entities.getFirst();
                return this.target;
            }
        }
    }

    private boolean isItemWithEquippedStack(LivingEntity entity) {
        return entity.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    private List<LivingEntity> findListFromEntities() {
        ArrayList<LivingEntity> entities = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity entityValue && entityValue != mc.player && this.isValidTarget(entityValue)) {
                entities.add(entityValue);
            }
        }
        return entities;
    }

    private float calculateYawWithCamera(LivingEntity entity) {
        Vec3d nearestPoint = NearestPointUtil.getNearestPoint(entity, 0.0);
        if (nearestPoint == null) {
            return Float.MAX_VALUE;
        } else {
            Rotation rotation = RotationUtil.calculate(nearestPoint);
            return RotationUtil.calculateFov(mc.gameRenderer.getCamera().getYaw(), mc.gameRenderer.getCamera().getPitch(), rotation.getYaw(), rotation.getPitch());
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (this.entityFilters.isEmpty()) {
            return false;
        } else {
            Vec3d nearestPoint = NearestPointUtil.getNearestPoint(entity, 0.0);
            if (nearestPoint == null) {
                return false;
            } else {
                float floatValue = (float) (mc.player.isGliding() ? this.elytraRange.getAsDouble() : this.range.getAsDouble());
                return !(entity instanceof ClientPlayerEntity) && entity.isAlive() && !entity.isInvulnerable() && !entity.isInCreativeMode() && !entity.isSpectator() && !(mc.player.getEyePos().distanceTo(nearestPoint) > floatValue) && (this.throughWalls.getAsBoolean() || this.canRaycastWithEyePos(nearestPoint)) ? this.calculateInstanceWithType(entity) != Integer.MIN_VALUE : false;
            }
        }
    }

    private int calculateInstanceWithType(LivingEntity entity) {
        int MIN_VALUE = Integer.MIN_VALUE;
        for (var targetRule : this.entityFilters) {
            if (targetRule.type().isInstance(entity) && this.isTestWithPredicate(targetRule, entity)) {
                MIN_VALUE = Math.max(MIN_VALUE, targetRule.priority());
            }
        }
        return MIN_VALUE;
    }

    private boolean canRaycastWithEyePos(Vec3d position) {
        if (mc.world != null && mc.player != null) {
            BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(mc.player.getEyePos(), position, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            return blockHitResult.getType() == Type.MISS;
        } else {
            return false;
        }
    }

    private <T extends Entity> boolean isTestWithPredicate(TargetRule<T> targetRule, Entity entity) {
        return targetRule.predicate().test(targetRule.type().cast(entity));
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }
}
