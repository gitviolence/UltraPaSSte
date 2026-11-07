package brova.toshiba.client.modules.impl.player;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.player.TargetPearl.ThrowCandidate;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.time.IntervalTimer;
import java.util.Comparator;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class TargetPearl extends Module {

    public static final TargetPearl INSTANCE = new TargetPearl();

    private final ModeSetting type = new ModeSetting("Тип", "Автоматический", "По бинду", "Автоматический");

    private final BindSetting keyBind = new BindSetting("Бинд", -1).visible(() -> this.type.is("По бинду"));

    private final BooleanSetting opponentsOnly = new BooleanSetting("Только за противником", false);

    private final BooleanSetting ignoreFriends = new BooleanSetting("Игнорировать друзей", true);

    private final IntervalTimer intervalTimer = new IntervalTimer();

    private EnderPearlEntity enderPearlEntity;

    private int id = -1;

    private long lastUpdateMillis;

    private boolean throwing;

    private Vec2f vec2f;

    public boolean isThrowing() {
        return this.throwing;
    }

    public TargetPearl() {
        super("TargetPearl", "Автоматически бросает жемчуг в цель", ModuleCategory.PLAYER);
        this.addSettings(this.type, this.keyBind, this.opponentsOnly, this.ignoreFriends);
    }

    @EventHandler
    public void onBinding(KeyBind keyBind) {
        if (mc.player != null && mc.world != null && mc.currentScreen == null && this.type.is("По бинду") && keyBind.getKey() == this.keyBind.getKey() && this.isCurrentTimeMillisWithCoolingDown()) {
            this.sendNaNWithDistanceTo();
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.id != -1 && !(mc.world.getEntityById(this.id) instanceof EnderPearlEntity enderPearlEntity && enderPearlEntity.isAlive())) {
                this.id = -1;
            }
            if (this.type.is("Автоматический") && this.isCurrentTimeMillisWithCoolingDown()) {
                this.sendNaNWithDistanceTo();
            }
        } else {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((TargetPearl) accessReceiver).updateLocalState();
            });
        }
    }

    @EventHandler
    public void onMoveInput(DirectionalInput directionalInput) {
        if (this.isEnable() && this.throwing && this.vec2f != null) {
            float forward = directionalInput.getForward();
            float strafe = directionalInput.getStrafe();
            if (forward != 0.0F || strafe != 0.0F) {
                double doubleValue = MathHelper.wrapDegrees(Math.toDegrees(calculateToRadians(this.vec2f.x, forward, strafe)));
                float floatValue = 0.0F;
                float floatValueValue = 0.0F;
                float MAX_VALUE = Float.MAX_VALUE;
                for (float index = -1.0F; index <= 1.0F; index++) {
                    for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                        if (index != 0.0F || indexValue != 0.0F) {
                            double doubleValueValue = MathHelper.wrapDegrees(Math.toDegrees(calculateToRadians(this.vec2f.x, index, indexValue)));
                            float magnitude = Math.abs(MathHelper.wrapDegrees((float) (doubleValue - doubleValueValue)));
                            if (magnitude < MAX_VALUE) {
                                MAX_VALUE = magnitude;
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
    }

    @Override
    public void onDisable() {
        this.updateLocalState();
        this.id = -1;
        this.lastUpdateMillis = 0L;
        this.intervalTimer.reset();
        super.onDisable();
    }

    private boolean isCurrentTimeMillisWithCoolingDown() {
        return System.currentTimeMillis() < this.lastUpdateMillis ? false : !mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL)) && this.intervalTimer.finished(1000L);
    }

    private void sendNaNWithDistanceTo() {
        Vec3d position = this.createPositionFromAlive();
        if (position == null) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((TargetPearl) accessReceiver).updateLocalState();
            });
        } else {
            float[] values = this.createFloatFromToDegreesWithAtan2(position);
            if (values != null && !Float.isNaN(values[0]) && !Float.isNaN(values[1])) {
                Vec3d positionValue = this.createPositionFromCosWithSin(values[0], values[1]);
                double doubleValue = Math.max(3.0, mc.player.getPos().distanceTo(position) * 0.12);
                if (positionValue == null || position.distanceTo(positionValue) > doubleValue) {
                    this.updateLocalState();
                } else if (!this.isOfWithFind()) {
                    this.updateLocalState();
                } else {
                    float yaw = mc.player.getYaw();
                    float pitch = mc.player.getPitch();
                    this.throwing = true;
                    this.vec2f = new Vec2f(values[0], values[1]);
                    try {
                        mc.player.setYaw(values[0]);
                        mc.player.setPitch(values[1]);
                        mc.player.networkHandler.sendPacket(new LookAndOnGround(values[0], values[1], mc.player.isOnGround(), mc.player.horizontalCollision));
                        InventoryUtil.swapAndUseHvH(Items.ENDER_PEARL);
                        this.intervalTimer.reset();
                        this.lastUpdateMillis = System.currentTimeMillis() + 2500L;
                        if (this.enderPearlEntity != null) {
                            this.id = this.enderPearlEntity.getId();
                        }
                    } finally {
                        mc.player.setYaw(yaw);
                        mc.player.setPitch(pitch);
                        this.updateLocalState();
                    }
                }
            } else {
                this.updateLocalState();
            }
        }
    }

    private Vec3d createPositionFromAlive() {
        this.enderPearlEntity = this.findEnderPearlEntityFromOwnerWithExpand();
        if (this.enderPearlEntity != null && this.enderPearlEntity.isAlive()) {
            Vec3d position = this.createPositionFromVelocityWithBottomY(this.enderPearlEntity);
            return position != null && this.isDistanceTo(position) ? position : null;
        } else {
            return null;
        }
    }

    private EnderPearlEntity findEnderPearlEntityFromOwnerWithExpand() {
        Box box = mc.player.getBoundingBox().expand(256.0);
        LivingEntity entity = ModuleRegistry.INSTANCE != null ? ModuleRegistry.aura.getTarget() : null;
        return mc.world.getOtherEntities(mc.player, box, input -> input instanceof EnderPearlEntity enderPearlEntity && enderPearlEntity.isAlive() && enderPearlEntity.getOwner() != mc.player && enderPearlEntity.getId() != this.id && !this.isFriendWithName(enderPearlEntity.getOwner()) && (!this.opponentsOnly.isState() || entity != null && enderPearlEntity.getOwner() == entity)).stream().map(inputValue -> (EnderPearlEntity) inputValue).filter(inputResult -> this.calculateSqrt(inputResult) <= 256.0).min(Comparator.comparingDouble(this::calculateSqrt)).orElse(null);
    }

    private boolean isFriendWithName(Entity entity) {
        return this.ignoreFriends.isState() && entity instanceof PlayerEntity player ? Toshiba.INSTANCE != null && Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString()) : false;
    }

    private double calculateSqrt(EnderPearlEntity enderPearlEntity) {
        Vec3d pos = mc.player.getPos();
        Vec3d posValue = enderPearlEntity.getPos();
        double doubleValue = posValue.x - pos.x;
        double doubleValueValue = posValue.z - pos.z;
        return Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue);
    }

    private Vec3d createPositionFromVelocityWithBottomY(EnderPearlEntity enderPearlEntity) {
        Vec3d pos = enderPearlEntity.getPos();
        Vec3d velocity = enderPearlEntity.getVelocity();
        Vec3d position = pos;
        for (int index = 0; index < 200; index++) {
            position = pos;
            pos = pos.add(velocity);
            if (this.canTypeWithRaycast(position, pos) || pos.y <= mc.world.getBottomY()) {
                return new Vec3d(MathHelper.floor(position.x) + 0.5, MathHelper.floor(position.y), MathHelper.floor(position.z) + 0.5);
            }
            velocity = this.createMultiplyWithOfFloored(velocity, pos);
        }
        return new Vec3d(MathHelper.floor(position.x) + 0.5, MathHelper.floor(position.y), MathHelper.floor(position.z) + 0.5);
    }

    private Vec3d createMultiplyWithOfFloored(Vec3d position, Vec3d positionValue) {
        BlockPos positionResult = BlockPos.ofFloored(positionValue);
        return mc.world.getBlockState(positionResult).isOf(Blocks.WATER) ? position.multiply(0.8).add(0.0, -0.03, 0.0) : position.multiply(0.99).add(0.0, -0.03, 0.0);
    }

    private boolean isDistanceTo(Vec3d position) {
        double doubleValue = mc.player.getPos().distanceTo(position);
        return doubleValue >= 11.0 && doubleValue <= 256.0;
    }

    private float[] createFloatFromToDegreesWithAtan2(Vec3d position) {
        Vec3d pos = mc.player.getPos();
        double doubleValue = position.x - pos.x;
        double doubleValueValue = position.y - mc.player.getEyeY();
        double doubleValueResult = position.z - pos.z;
        float floatValue = (float) Math.toDegrees(Math.atan2(doubleValueResult, doubleValue)) - 90.0F;
        double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueResult * doubleValueResult);
        double doubleValueItem = Math.max(1.5, mc.player.getPos().distanceTo(position) * 0.08);
        ThrowCandidate throwCandidate = this.createTargetPearlThrowCandidateFromToRadiansWithCos(position, floatValue, -25.0F, 35.0F, doubleValueItem, true);
        if (throwCandidate != null) {
            return new float[] { floatValue, MathHelper.clamp(throwCandidate.value, -90.0F, 90.0F) };
        } else {
            ThrowCandidate throwCandidateValue = this.createTargetPearlThrowCandidateFromToRadiansWithCos(position, floatValue, -85.0F, 85.0F, doubleValueItem, false);
            if (throwCandidateValue == null) {
                double doubleValueEntry = -Math.toDegrees(Math.atan2(doubleValueValue, squareRoot)) + 5.0;
                return new float[] { floatValue, MathHelper.clamp((float) doubleValueEntry, -90.0F, 90.0F) };
            } else {
                return new float[] { floatValue, MathHelper.clamp(throwCandidateValue.value, -90.0F, 90.0F) };
            }
        }
    }

    private ThrowCandidate createTargetPearlThrowCandidateFromToRadiansWithCos(Vec3d position, float floatValue, float floatValueValue, float floatValueResult, double doubleValue, boolean flag) {
        Vec3d pos = mc.player.getPos();
        double doubleValueValue = 1.5;
        ThrowCandidate throwCandidate = null;
        for (float index = floatValueValue; index <= floatValueResult; index += 0.25F) {
            float floatValueItem = (float) Math.toRadians(index);
            double doubleValueResult = -MathHelper.sin((float) Math.toRadians(floatValue)) * MathHelper.cos(floatValueItem) * doubleValueValue;
            double doubleValueItem = -MathHelper.sin(floatValueItem) * doubleValueValue;
            double doubleValueEntry = MathHelper.cos((float) Math.toRadians(floatValue)) * MathHelper.cos(floatValueItem) * doubleValueValue;
            Vec3d positionValue = new Vec3d(pos.x, mc.player.getEyeY(), pos.z);
            Vec3d positionResult = new Vec3d(doubleValueResult, doubleValueItem, doubleValueEntry);
            int intValue = 0;
            for (int indexValue = 0; indexValue < 200; indexValue++) {
                Vec3d positionItem = positionValue;
                positionValue = positionValue.add(positionResult);
                positionResult = this.createMultiplyWithOfFloored(positionResult, positionValue);
                intValue++;
                if (this.canExpandWithOtherEntities(positionItem, positionValue)) {
                    break;
                }
                if (this.canTypeWithRaycast(positionItem, positionValue) || !(positionValue.y > mc.world.getBottomY())) {
                    double doubleValueCopy = positionValue.distanceTo(position);
                    ThrowCandidate throwCandidateValue = new ThrowCandidate(index, doubleValueCopy, intValue, positionValue);
                    if (this.isTargetPearlThrowCandidate(throwCandidateValue, throwCandidate, doubleValue, flag)) {
                        throwCandidate = throwCandidateValue;
                    }
                    break;
                }
            }
        }
        return throwCandidate != null && !(throwCandidate.cachedCoordinate > doubleValue) ? throwCandidate : null;
    }

    private boolean isTargetPearlThrowCandidate(ThrowCandidate throwCandidate, ThrowCandidate throwCandidateValue, double doubleValue, boolean flag) {
        if (throwCandidateValue == null) {
            return true;
        } else {
            boolean flagValue = throwCandidate.cachedCoordinate <= doubleValue;
            boolean flagResult = throwCandidateValue.cachedCoordinate <= doubleValue;
            if (flagValue != flagResult) {
                return flagValue;
            } else {
                if (flag && flagValue && flagResult) {
                    float magnitude = Math.abs(throwCandidate.value);
                    float magnitudeValue = Math.abs(throwCandidateValue.value);
                    if (Math.abs(magnitude - magnitudeValue) > 0.01F) {
                        return magnitude < magnitudeValue;
                    }
                    if (throwCandidate.cachedCount != throwCandidateValue.cachedCount) {
                        return throwCandidate.cachedCount < throwCandidateValue.cachedCount;
                    }
                }
                if (Math.abs(throwCandidate.cachedCoordinate - throwCandidateValue.cachedCoordinate) > 0.01) {
                    return throwCandidate.cachedCoordinate < throwCandidateValue.cachedCoordinate;
                } else if (throwCandidate.cachedCount != throwCandidateValue.cachedCount) {
                    return throwCandidate.cachedCount < throwCandidateValue.cachedCount;
                } else {
                    return !flag ? Math.abs(throwCandidate.value) < Math.abs(throwCandidateValue.value) : false;
                }
            }
        }
    }

    private Vec3d createPositionFromCosWithSin(float floatValue, float floatValueValue) {
        float floatValueResult = (float) Math.toRadians(floatValue);
        float floatValueItem = (float) Math.toRadians(floatValueValue);
        double doubleValue = 1.5;
        double doubleValueValue = mc.player.getX() - MathHelper.cos(floatValueResult) * 0.16F;
        double doubleValueResult = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - 0.1;
        double doubleValueItem = mc.player.getZ() - MathHelper.sin(floatValueResult) * 0.16F;
        double doubleValueEntry = -MathHelper.sin(floatValueResult) * MathHelper.cos(floatValueItem) * doubleValue;
        double doubleValueCopy = -MathHelper.sin(floatValueItem) * doubleValue;
        double doubleValue2 = MathHelper.cos(floatValueResult) * MathHelper.cos(floatValueItem) * doubleValue;
        Vec3d position = new Vec3d(doubleValueValue, doubleValueResult, doubleValueItem);
        Vec3d positionValue = new Vec3d(doubleValueEntry, doubleValueCopy, doubleValue2);
        for (int index = 0; index <= 200; index++) {
            Vec3d positionResult = position;
            position = position.add(positionValue);
            positionValue = this.createMultiplyWithOfFloored(positionValue, position);
            if (this.canExpandWithOtherEntities(positionResult, position)) {
                return null;
            }
            if (this.canTypeWithRaycast(positionResult, position) || position.y <= mc.world.getBottomY()) {
                return new Vec3d(MathHelper.floor(position.x) + 0.5, MathHelper.floor(position.y), MathHelper.floor(position.z) + 0.5);
            }
        }
        return null;
    }

    private boolean canTypeWithRaycast(Vec3d position, Vec3d positionValue) {
        return mc.world.raycast(new RaycastContext(position, positionValue, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() == Type.BLOCK;
    }

    private boolean canExpandWithOtherEntities(Vec3d position, Vec3d positionValue) {
        Box box = new Box(position, positionValue).expand(0.3);
        for (Entity entity : mc.world.getOtherEntities(mc.player, box, input -> {
            if (input.isAlive() && !input.isSpectator() && !input.noClip) {
                return input == this.enderPearlEntity ? false : !(input instanceof EnderPearlEntity);
            } else {
                return false;
            }
        })) {
            if (entity.getBoundingBox().expand(0.25).raycast(position, positionValue).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean isOfWithFind() {
        return mc.player.getMainHandStack().isOf(Items.ENDER_PEARL) || mc.player.getOffHandStack().isOf(Items.ENDER_PEARL) || InventoryUtil.find(Items.ENDER_PEARL, 0, 8) != -1 || InventoryUtil.find(Items.ENDER_PEARL, 9, 45) != -1;
    }

    private void updateLocalState() {
        this.throwing = false;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.enderPearlEntity = (EnderPearlEntity) accessValue;
        });
        value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.vec2f = (Vec2f) accessValue;
        });
    }

    private static double calculateToRadians(float floatValue, float floatValueValue, float floatValueResult) {
        if (floatValueValue < 0.0F) {
            floatValue += 180.0F;
        }
        float floatValueItem = 1.0F;
        if (floatValueValue < 0.0F) {
            floatValueItem = -0.5F;
        } else if (floatValueValue > 0.0F) {
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

    public final static class ThrowCandidate {

        final float value;

        final double cachedCoordinate;

        final int cachedCount;

        private final Vec3d vec3d;

        ThrowCandidate(float value, double cachedCoordinate, int cachedCount, Vec3d vec3d) {
            this.value = value;
            this.cachedCoordinate = cachedCoordinate;
            this.cachedCount = cachedCount;
            this.vec3d = vec3d;
        }
    }
}
