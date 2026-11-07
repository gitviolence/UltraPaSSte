package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.player.NoDelay;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.mixin.IEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class CriticalHitUtil implements MinecraftAccess {

    private static int cachedCountForUpdateSubmergedInWater = Integer.MIN_VALUE;

    private static int cachedCountForCanCritical = Integer.MIN_VALUE;

    public static float getAICooldown() {
        if (mc.player.getMainHandStack().getItem() == Items.AIR) {
            return 0.9F;
        } else {
            return !(mc.player.getMainHandStack().getItem() instanceof AxeItem) && !(mc.player.getMainHandStack().getItem() instanceof ShovelItem) ? 0.93F : 0.95F;
        }
    }

    public static boolean canAIFall() {
        BlockPos position = BlockPos.ofFloored(mc.player.getPos().add(0.0, -0.4F, 0.0));
        return mc.world.getBlockState(position).isOf(Blocks.WATER) ? true : getBlock(0.0, 3.0, 0.0) == Blocks.AIR && getBlock(0.0, 2.0, 0.0) == Blocks.AIR && getBlock(0.0, 1.0, 0.0) == Blocks.AIR || mc.player.fallDistance < (getBlock(0.0, 2.0, 0.0) != Blocks.AIR ? 0.08F : 0.6F) || mc.player.fallDistance > 1.2F;
    }

    public static boolean canCritical(LivingEntity entity) {
        updateSubmergedInWater();
        if (mc.player == null) {
            return false;
        } else if (!mc.player.isSubmergedInWater() && (!mc.player.isTouchingWater() || isWithinJumpCriticalWindow())) {
            if (!isWithinJumpCriticalWindow()) {
                boolean enable = ModuleRegistry.packetCriticals.isEnable();
                boolean statusEffectPresent = mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);
                boolean inCobweb = isInCobweb();
                if (enable && inCobweb) {
                    return true;
                } else if (enable && statusEffectPresent) {
                    return mc.player.getVelocity().y < 0.0 && mc.player.fallDistance > 0.0F;
                } else if (isNoDelayJumpCriticalReady()) {
                    return isFallingForCritical();
                } else if (isNoDelayJumpHeld()) {
                    return isNoJumpDelayJumpCritWindow();
                } else if (hasCriticalMovementObstruction()) {
                    return true;
                } else {
                    boolean flag = mc.player.getVelocity().y < 0.0;
                    boolean flagValue = mc.player.fallDistance > 0.0F || mc.player.hurtTime > 0 && flag;
                    boolean flagResult = !mc.player.isOnGround() && flag && flagValue;
                    boolean state = ModuleRegistry.aura.smartCrit.isState();
                    if (!state) {
                        return flagResult;
                    } else {
                        boolean flagItem = mc.options != null && mc.options.jumpKey.isPressed();
                        return mc.player.isOnGround() && !flagItem || flagResult;
                    }
                }
            } else {
                return isOnGroundWithTouchingWater() ? true : mc.player.age - cachedCountForCanCritical > 6;
            }
        } else {
            return true;
        }
    }

    private static boolean isNoDelayJumpCriticalReady() {
        return ModuleRegistry.noDelay.isEnable() && brova.toshiba.client.utils.runtime.AccessGuard.read(ModuleRegistry.noDelay, accessReceiver -> accessReceiver.noDelayModes).is("Прыжок") && mc.options != null && mc.options.jumpKey.isPressed() && isContractWithBoundingBox();
    }

    private static boolean isNoDelayJumpHeld() {
        return ModuleRegistry.noDelay.isEnable() && brova.toshiba.client.utils.runtime.AccessGuard.read(ModuleRegistry.noDelay, accessReceiver -> accessReceiver.noDelayModes).is("Прыжок") && mc.options != null && mc.options.jumpKey.isPressed();
    }

    private static boolean isFallingForCritical() {
        return mc.player != null && !mc.player.isOnGround() && mc.player.getVelocity().y <= 0.01 && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() && !mc.player.isInLava() && !mc.player.isClimbing() && !mc.player.hasVehicle() && !mc.player.getAbilities().flying;
    }

    public static boolean isNoJumpDelayJumpCritWindow() {
        return mc.player != null && mc.world != null && ModuleRegistry.noDelay.isEnable() && ModuleRegistry.noDelay.noDelayModes.is("Прыжок") && mc.options != null && mc.options.jumpKey.isPressed() && !mc.player.isOnGround() && mc.player.getVelocity().y < 0.0 && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() && !mc.player.isInLava() && !mc.player.isClimbing() && !mc.player.hasVehicle() && !mc.player.getAbilities().flying && !mc.player.hasStatusEffect(StatusEffects.LEVITATION) && !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !mc.player.isGliding() && !isInCobweb();
    }

    private static boolean isContractWithBoundingBox() {
        if (mc.player != null && mc.world != null) {
            Box box = mc.player.getBoundingBox().contract(0.03);
            Box boxValue = new Box(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + 0.32, box.maxZ);
            for (BlockPos position : BlockPos.iterate(MathHelper.floor(boxValue.minX), MathHelper.floor(boxValue.minY), MathHelper.floor(boxValue.minZ), MathHelper.floor(boxValue.maxX), MathHelper.floor(boxValue.maxY), MathHelper.floor(boxValue.maxZ))) {
                BlockState blockState = mc.world.getBlockState(position);
                if (!blockState.isAir() && !blockState.getCollisionShape(mc.world, position).isEmpty()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public static boolean canPacketCrit() {
        return isInCobweb() || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);
    }

    private static void updateSubmergedInWater() {
        if (mc.player == null || mc.world == null) {
            cachedCountForUpdateSubmergedInWater = Integer.MIN_VALUE;
            cachedCountForCanCritical = Integer.MIN_VALUE;
        } else if (mc.player.isSubmergedInWater()) {
            cachedCountForCanCritical = Integer.MIN_VALUE;
        } else {
            boolean ofFlooredWithTouchingWater = isOfFlooredWithTouchingWater();
            if (ofFlooredWithTouchingWater) {
                cachedCountForUpdateSubmergedInWater = mc.player.age;
                if (isPressedWithOnGround()) {
                    cachedCountForCanCritical = mc.player.age;
                }
            }
        }
    }

    private static boolean isPressedWithOnGround() {
        return mc.player != null && mc.options != null ? mc.options.jumpKey.isPressed() && !mc.player.isOnGround() && !mc.player.isSubmergedInWater() && mc.player.getVelocity().y > 0.05 : false;
    }

    private static boolean isWithinJumpCriticalWindow() {
        return mc.player != null && mc.options != null && mc.options.jumpKey.isPressed() ? mc.player.age - cachedCountForCanCritical <= 8 && mc.player.age - cachedCountForUpdateSubmergedInWater <= 10 : false;
    }

    private static boolean isOnGroundWithTouchingWater() {
        return mc.player != null && !mc.player.isOnGround() && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() && (mc.player.fallDistance > 0.0F || mc.player.hurtTime > 0) && mc.player.getVelocity().y < 0.0;
    }

    private static boolean isOfFlooredWithTouchingWater() {
        if (mc.player != null && mc.world != null) {
            BlockPos position = BlockPos.ofFloored(mc.player.getPos().add(0.0, -0.4F, 0.0));
            return mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.world.getBlockState(position).isOf(Blocks.WATER);
        } else {
            return false;
        }
    }

    private static boolean hasCriticalMovementObstruction() {
        double stepHeight = mc.player.getStepHeight();
        Vec3d position = new Vec3d(0.0, stepHeight, 0.0);
        Vec3d positionValue = ((IEntity) mc.player).invokeAdjustMovementForCollisions(position);
        boolean inCobweb = isInCobweb();
        BlockPos positionResult = BlockPos.ofFloored(mc.player.getPos().add(0.0, mc.player.getHeight() / 2.0F, 0.0));
        return mc.player.isInLava() || mc.player.isClimbing() || mc.world.getBlockState(positionResult).isOf(Blocks.WATER) || mc.player.hasStatusEffect(StatusEffects.LEVITATION) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || inCobweb || mc.player.isGliding() || mc.player.hasVehicle() || mc.player.getAbilities().flying || mc.player.isTouchingWater() || positionValue.y < mc.player.getStepHeight() - 0.5 && mc.player.isOnGround();
    }

    public static boolean isInCobweb() {
        Box boundingBox = mc.player.getBoundingBox();
        for (BlockPos position : BlockPos.iterate(MathHelper.floor(boundingBox.minX), MathHelper.floor(boundingBox.minY), MathHelper.floor(boundingBox.minZ), MathHelper.floor(boundingBox.maxX), MathHelper.floor(boundingBox.maxY), MathHelper.floor(boundingBox.maxZ))) {
            if (mc.world.getBlockState(position).isOf(Blocks.COBWEB)) {
                return true;
            }
        }
        return false;
    }

    public static Block getBlock(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return mc.world.getBlockState(mc.player.getBlockPos().add((int) doubleValue, (int) doubleValueValue, (int) doubleValueResult)).getBlock();
    }

    public static boolean findFall(float floatValue) {
        Vec3d rotationVector = mc.player.getRotationVector();
        double x = mc.player.getVelocity().x;
        double y = mc.player.getVelocity().y;
        double z = mc.player.getVelocity().z;
        float floatValueValue = MathHelper.cos(mc.player.getPitch() * (float) (Math.PI / 180.0));
        floatValueValue = (float) (floatValueValue * floatValueValue * Math.min(rotationVector.length() / 0.4, 1.0));
        Vec3d position = new Vec3d(x, y, z).add(0.0, 0.08 * (-1.0 + floatValueValue * 0.75), 0.0);
        y = position.y * 0.98F;
        return y < floatValue;
    }

    private CriticalHitUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
