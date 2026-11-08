package brova.toshiba.client.utils.world;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.utils.player.InventoryUtil;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ExplosionDamageUtil implements MinecraftAccess {

    public static float getExplosionDamage(Vec3d position, PlayerEntity player, boolean flag) {
        if (mc.world != null && mc.world.getDifficulty() != Difficulty.PEACEFUL && player != null) {
            if (!new Box(MathHelper.floor(position.x - 11.0), MathHelper.floor(position.y - 11.0), MathHelper.floor(position.z - 11.0), MathHelper.floor(position.x + 13.0), MathHelper.floor(position.y + 13.0), MathHelper.floor(position.z + 13.0)).intersects(player.getBoundingBox())) {
                return 0.0F;
            } else if (player.isInvulnerable()) {
                return 0.0F;
            } else {
                double doubleValue = player.squaredDistanceTo(position) / 144.0;
                if (doubleValue > 1.0) {
                    return 0.0F;
                } else {
                    double exposure = getExposure(position, player.getBoundingBox(), flag);
                    double doubleValueValue = (1.0 - doubleValue) * exposure;
                    float roundedDown = (float) Math.floor((doubleValueValue * doubleValueValue + doubleValueValue) / 2.0 * 7.0 * 12.0 + 1.0);
                    if (mc.world.getDifficulty() == Difficulty.EASY) {
                        roundedDown = Math.min(roundedDown / 2.0F + 1.0F, roundedDown);
                    } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        roundedDown = roundedDown * 3.0F / 2.0F;
                    }
                    DamageSource damageSource = mc.world.getDamageSources().explosion(null, null);
                    roundedDown = DamageUtil.getDamageLeft(player, roundedDown, damageSource, player.getArmor(), (float) Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS)).getValue());
                    if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int intValue = 25 - (Objects.requireNonNull(player.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float floatValue = roundedDown * intValue;
                        roundedDown = Math.max(floatValue / 25.0F, 0.0F);
                    }
                    if (roundedDown <= 0.0F) {
                        return 0.0F;
                    } else {
                        float protectionAmount = getProtectionAmount(List.of(player.getEquippedStack(EquipmentSlot.HEAD), player.getEquippedStack(EquipmentSlot.CHEST), player.getEquippedStack(EquipmentSlot.LEGS), player.getEquippedStack(EquipmentSlot.FEET)));
                        if (protectionAmount > 0.0F) {
                            roundedDown = DamageUtil.getInflictedDamage(roundedDown, protectionAmount);
                        }
                        return roundedDown;
                    }
                }
            }
        } else {
            return 0.0F;
        }
    }

    public static float getDamageOfGhostBlock(Vec3d position, PlayerEntity player, BlockPos positionValue) {
        if (mc.world != null && mc.world.getDifficulty() != Difficulty.PEACEFUL && player != null && positionValue != null) {
            double doubleValue = 12.0;
            if (!new Box(MathHelper.floor(position.x - doubleValue - 1.0), MathHelper.floor(position.y - doubleValue - 1.0), MathHelper.floor(position.z - doubleValue - 1.0), MathHelper.floor(position.x + doubleValue + 1.0), MathHelper.floor(position.y + doubleValue + 1.0), MathHelper.floor(position.z + doubleValue + 1.0)).intersects(player.getBoundingBox())) {
                return 0.0F;
            } else if (player.isInvulnerable()) {
                return 0.0F;
            } else {
                double doubleValueValue = player.squaredDistanceTo(position) / 144.0;
                if (doubleValueValue > 1.0) {
                    return 0.0F;
                } else {
                    double doubleValueResult = calculateExplosionExposure(position, player.getBoundingBox(), player, positionValue);
                    double doubleValueItem = (1.0 - doubleValueValue) * doubleValueResult;
                    float roundedDown = (float) Math.floor((doubleValueItem * doubleValueItem + doubleValueItem) / 2.0 * 7.0 * doubleValue + 1.0);
                    if (mc.world.getDifficulty() == Difficulty.EASY) {
                        roundedDown = Math.min(roundedDown / 2.0F + 1.0F, roundedDown);
                    } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        roundedDown = roundedDown * 3.0F / 2.0F;
                    }
                    DamageSource damageSource = mc.world.getDamageSources().explosion(null, null);
                    roundedDown = DamageUtil.getDamageLeft(player, roundedDown, damageSource, player.getArmor(), (float) Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS)).getValue());
                    if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int intValue = 25 - (Objects.requireNonNull(player.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float floatValue = roundedDown * intValue;
                        roundedDown = Math.max(floatValue / 25.0F, 0.0F);
                    }
                    if (roundedDown <= 0.0F) {
                        return 0.0F;
                    } else {
                        float protectionAmount = getProtectionAmount(List.of(player.getEquippedStack(EquipmentSlot.HEAD), player.getEquippedStack(EquipmentSlot.CHEST), player.getEquippedStack(EquipmentSlot.LEGS), player.getEquippedStack(EquipmentSlot.FEET)));
                        if (protectionAmount > 0.0F) {
                            roundedDown = DamageUtil.getInflictedDamage(roundedDown, protectionAmount);
                        }
                        return roundedDown;
                    }
                }
            }
        } else {
            return 0.0F;
        }
    }

    public static float getDamageIgnoringBlock(Vec3d position, PlayerEntity player, BlockPos positionValue) {
        if (mc.world != null && mc.world.getDifficulty() != Difficulty.PEACEFUL && player != null && positionValue != null) {
            double doubleValue = 12.0;
            if (!new Box(MathHelper.floor(position.x - doubleValue - 1.0), MathHelper.floor(position.y - doubleValue - 1.0), MathHelper.floor(position.z - doubleValue - 1.0), MathHelper.floor(position.x + doubleValue + 1.0), MathHelper.floor(position.y + doubleValue + 1.0), MathHelper.floor(position.z + doubleValue + 1.0)).intersects(player.getBoundingBox())) {
                return 0.0F;
            } else if (player.isInvulnerable()) {
                return 0.0F;
            } else {
                double doubleValueValue = player.squaredDistanceTo(position) / 144.0;
                if (doubleValueValue > 1.0) {
                    return 0.0F;
                } else {
                    double doubleValueResult = calculateAlternateExplosionExposure(position, player.getBoundingBox(), player, positionValue);
                    double doubleValueItem = (1.0 - doubleValueValue) * doubleValueResult;
                    float roundedDown = (float) Math.floor((doubleValueItem * doubleValueItem + doubleValueItem) / 2.0 * 7.0 * doubleValue + 1.0);
                    if (mc.world.getDifficulty() == Difficulty.EASY) {
                        roundedDown = Math.min(roundedDown / 2.0F + 1.0F, roundedDown);
                    } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        roundedDown = roundedDown * 3.0F / 2.0F;
                    }
                    DamageSource damageSource = mc.world.getDamageSources().explosion(null, null);
                    roundedDown = DamageUtil.getDamageLeft(player, roundedDown, damageSource, player.getArmor(), (float) Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS)).getValue());
                    if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int intValue = 25 - (Objects.requireNonNull(player.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float floatValue = roundedDown * intValue;
                        roundedDown = Math.max(floatValue / 25.0F, 0.0F);
                    }
                    if (roundedDown <= 0.0F) {
                        return 0.0F;
                    } else {
                        float protectionAmount = getProtectionAmount(List.of(player.getEquippedStack(EquipmentSlot.HEAD), player.getEquippedStack(EquipmentSlot.CHEST), player.getEquippedStack(EquipmentSlot.LEGS), player.getEquippedStack(EquipmentSlot.FEET)));
                        if (protectionAmount > 0.0F) {
                            roundedDown = DamageUtil.getInflictedDamage(roundedDown, protectionAmount);
                        }
                        return roundedDown;
                    }
                }
            }
        } else {
            return 0.0F;
        }
    }

    public static boolean intersectsEntityBox(BlockPos position, Entity entity) {
        if (entity != null && position != null) {
            Box box = new Box(position.getX(), position.getY(), position.getZ(), position.getX() + 1.0, position.getY() + 2.0, position.getZ() + 1.0);
            return entity.getBoundingBox().intersects(box);
        } else {
            return false;
        }
    }

    private static float calculateExplosionExposure(Vec3d position, Box box, Entity entity, BlockPos positionValue) {
        double doubleValue = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double doubleValueValue = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double doubleValueResult = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double doubleValueItem = (1.0 - Math.floor(1.0 / doubleValue) * doubleValue) / 2.0;
        double doubleValueEntry = (1.0 - Math.floor(1.0 / doubleValueResult) * doubleValueResult) / 2.0;
        if (!(doubleValue < 0.0) && !(doubleValueValue < 0.0) && !(doubleValueResult < 0.0)) {
            int intValue = 0;
            int intValueValue = 0;
            for (double index = 0.0; index <= 1.0; index += doubleValue) {
                for (double indexValue = 0.0; indexValue <= 1.0; indexValue += doubleValueValue) {
                    for (double indexResult = 0.0; indexResult <= 1.0; indexResult += doubleValueResult) {
                        double interpolatedValue = MathHelper.lerp(index, box.minX, box.maxX);
                        double interpolatedValueValue = MathHelper.lerp(indexValue, box.minY, box.maxY);
                        double interpolatedValueResult = MathHelper.lerp(indexResult, box.minZ, box.maxZ);
                        Vec3d positionResult = new Vec3d(interpolatedValue + doubleValueItem, interpolatedValueValue, interpolatedValueResult + doubleValueEntry);
                        if (createBlockHitResultFromEndWithRaycast(new RaycastContext(positionResult, position, ShapeType.COLLIDER, FluidHandling.NONE, entity), positionValue).getType() == Type.MISS) {
                            intValue++;
                        }
                        intValueValue++;
                    }
                }
            }
            return (float) intValue / intValueValue;
        } else {
            return 0.0F;
        }
    }

    private static BlockHitResult createBlockHitResultFromEndWithRaycast(RaycastContext raycastContext, BlockPos position) {
        return BlockView.raycast(raycastContext.getStart(), raycastContext.getEnd(), raycastContext, (input, inputValue) -> {
            BlockState blockState;
            if (inputValue.equals(position)) {
                blockState = Blocks.OBSIDIAN.getDefaultState();
            } else {
                blockState = mc.world.getBlockState(inputValue);
            }
            VoxelShape collisionShape = blockState.getCollisionShape(mc.world, inputValue);
            return collisionShape.raycast(raycastContext.getStart(), raycastContext.getEnd(), inputValue);
        }, inputResult -> BlockHitResult.createMissed(inputResult.getEnd(), Direction.UP, BlockPos.ofFloored(inputResult.getEnd())));
    }

    private static float calculateAlternateExplosionExposure(Vec3d position, Box box, Entity entity, BlockPos positionValue) {
        double doubleValue = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double doubleValueValue = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double doubleValueResult = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double doubleValueItem = (1.0 - Math.floor(1.0 / doubleValue) * doubleValue) / 2.0;
        double doubleValueEntry = (1.0 - Math.floor(1.0 / doubleValueResult) * doubleValueResult) / 2.0;
        if (!(doubleValue < 0.0) && !(doubleValueValue < 0.0) && !(doubleValueResult < 0.0)) {
            int intValue = 0;
            int intValueValue = 0;
            for (double index = 0.0; index <= 1.0; index += doubleValue) {
                for (double indexValue = 0.0; indexValue <= 1.0; indexValue += doubleValueValue) {
                    for (double indexResult = 0.0; indexResult <= 1.0; indexResult += doubleValueResult) {
                        double interpolatedValue = MathHelper.lerp(index, box.minX, box.maxX);
                        double interpolatedValueValue = MathHelper.lerp(indexValue, box.minY, box.maxY);
                        double interpolatedValueResult = MathHelper.lerp(indexResult, box.minZ, box.maxZ);
                        Vec3d positionResult = new Vec3d(interpolatedValue + doubleValueItem, interpolatedValueValue, interpolatedValueResult + doubleValueEntry);
                        if (createBlockHitResultFromEndWithStart(new RaycastContext(positionResult, position, ShapeType.COLLIDER, FluidHandling.NONE, entity), positionValue).getType() == Type.MISS) {
                            intValue++;
                        }
                        intValueValue++;
                    }
                }
            }
            return (float) intValue / intValueValue;
        } else {
            return 0.0F;
        }
    }

    private static BlockHitResult createBlockHitResultFromEndWithStart(RaycastContext raycastContext, BlockPos position) {
        return BlockView.raycast(raycastContext.getStart(), raycastContext.getEnd(), raycastContext, (input, inputValue) -> {
            BlockState blockState = inputValue.equals(position) ? Blocks.AIR.getDefaultState() : mc.world.getBlockState(inputValue);
            VoxelShape collisionShape = blockState.getCollisionShape(mc.world, inputValue);
            return collisionShape.raycast(input.getStart(), input.getEnd(), inputValue);
        }, inputResult -> BlockHitResult.createMissed(inputResult.getEnd(), Direction.getFacing(inputResult.getStart().x - inputResult.getEnd().x, inputResult.getStart().y - inputResult.getEnd().y, inputResult.getStart().z - inputResult.getEnd().z), BlockPos.ofFloored(inputResult.getEnd())));
    }

    public static float getExposure(Vec3d position, Box box, boolean flag) {
        if (!flag) {
            return getExposure(position, box);
        } else {
            int intValue = 0;
            int intValueValue = 0;
            for (int index = 0; index <= 1; index++) {
                for (int indexValue = 0; indexValue <= 1; indexValue++) {
                    for (int indexResult = 0; indexResult <= 1; indexResult++) {
                        double interpolatedValue = MathHelper.lerp((double) index, box.minX, box.maxX);
                        double interpolatedValueValue = MathHelper.lerp((double) indexValue, box.minY, box.maxY);
                        double interpolatedValueResult = MathHelper.lerp((double) indexResult, box.minZ, box.maxZ);
                        Vec3d positionValue = new Vec3d(interpolatedValue, interpolatedValueValue, interpolatedValueResult);
                        if (raycast(positionValue, position) == Type.MISS) {
                            intValue++;
                        }
                        intValueValue++;
                    }
                }
            }
            return (float) intValue / intValueValue;
        }
    }

    public static float getExposure(Vec3d position, Box box) {
        double doubleValue = 0.4545454446934474;
        double doubleValueValue = 0.21739130885479366;
        double doubleValueResult = 0.4545454446934474;
        int intValue = 0;
        int intValueValue = 0;
        for (double index = 0.0; index <= 1.0; index += doubleValue) {
            for (double indexValue = 0.0; indexValue <= 1.0; indexValue += doubleValueValue) {
                for (double indexResult = 0.0; indexResult <= 1.0; indexResult += doubleValueResult) {
                    double interpolatedValue = MathHelper.lerp(index, box.minX, box.maxX);
                    double interpolatedValueValue = MathHelper.lerp(indexValue, box.minY, box.maxY);
                    double interpolatedValueResult = MathHelper.lerp(indexResult, box.minZ, box.maxZ);
                    Vec3d positionValue = new Vec3d(interpolatedValue + 0.045454555306552624, interpolatedValueValue, interpolatedValueResult + 0.045454555306552624);
                    if (raycast(positionValue, position) == Type.MISS) {
                        intValue++;
                    }
                    intValueValue++;
                }
            }
        }
        return (float) intValue / intValueValue;
    }

    public static Type raycast(Vec3d position, Vec3d positionValue) {
        return BlockView.raycast(position, positionValue, null, (input, inputValue) -> {
            BlockState blockState = mc.world.getBlockState(inputValue);
            BlockHitResult blockHitResult = blockState.getCollisionShape(mc.world, inputValue).raycast(position, positionValue, inputValue);
            return blockHitResult == null ? null : blockHitResult.getType();
        }, inputResult -> Type.MISS);
    }

    public static int getProtectionAmount(Iterable<ItemStack> stacks) {
        MutableInt mutableInt = new MutableInt();
        stacks.forEach(stack -> mutableInt.add(getProtectionAmount(stack)));
        return mutableInt.intValue();
    }

    public static int getProtectionAmount(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            int enchantmentLevel = InventoryUtil.getEnchantmentLevel(stack, Enchantments.BLAST_PROTECTION);
            int enchantmentLevelValue = InventoryUtil.getEnchantmentLevel(stack, Enchantments.PROTECTION);
            return enchantmentLevel * 2 + enchantmentLevelValue;
        } else {
            return 0;
        }
    }
}
