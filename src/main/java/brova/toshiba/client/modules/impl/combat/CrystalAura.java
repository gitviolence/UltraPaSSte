package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.BlockBreakingEvent;
import brova.toshiba.client.event.impl.GameUpdateEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.CrystalAura.PlaceData;
import brova.toshiba.client.modules.impl.combat.CrystalAura.PlaceResult;
import brova.toshiba.client.modules.impl.combat.CrystalAura.ReturnResult;
import brova.toshiba.client.modules.impl.combat.CrystalAura.VisualData;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.player.TargetSelector;
import brova.toshiba.client.utils.player.TargetSelectorBuilder;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.world.BlockFaceUtil;
import brova.toshiba.client.utils.world.ExplosionDamageUtil;
import brova.toshiba.client.utils.world.FaceDirectionUtil;
import brova.toshiba.client.utils.world.NearestPointUtil;
import brova.toshiba.client.utils.world.RaycastUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

public final class CrystalAura extends Module {

    public static CrystalAura INSTANCE;

    private final ModeSetting rotate = new ModeSetting("Ротация", "Vanilla", "Vanilla", "С Ауры", "Без ротации");

    private final BooleanSetting autoKrot = new BooleanSetting("Делать подкоп", true);

    private final TargetSelector targetFinder = new TargetSelectorBuilder().range(() -> 8.0).throughWalls(() -> true).saveCurrentTarget(() -> false).filterEntities(PlayerEntity.class, input -> Toshiba.INSTANCE.friendStorage == null || !Toshiba.INSTANCE.friendStorage.isFriend(input.getName().getString()), 0);

    private int ticksToBack;

    private int lastSlot = -1;

    private PlaceData lastData;

    private final ConcurrentLinkedQueue<VisualData> visualDatas = new ConcurrentLinkedQueue<>();

    private Rotation rotation;

    private LivingEntity auraRotationTarget;

    private Vec3d auraRotationPoint;

    private boolean auraRotationActive;

    public CrystalAura() {
        super("CrystalAura", "Автоматически ставит и взрывает кристаллы", ModuleCategory.COMBAT);
        INSTANCE = this;
        this.addSettings(this.rotate, this.autoKrot);
    }

    @Override
    public void onDisable() {
        this.ticksToBack = 0;
        this.rotation = null;
        this.lastData = null;
        this.clearAuraRotation();
        if (this.lastSlot != -1 && mc.player != null) {
            this.selectHotbarSlot(this.lastSlot);
        }
        this.lastSlot = -1;
        if (!this.rotate.is("С Ауры") && RotationStorage.instance != null) {
            RotationStorage.instance.stopRotation();
        }
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && !mc.player.isUsingItem()) {
            if (this.ticksToBack > 0) {
                this.ticksToBack--;
            }
            if (this.ticksToBack == 0 && this.lastSlot != -1) {
                this.selectHotbarSlot(this.lastSlot);
                this.lastSlot = -1;
            }
            LivingEntity entity = this.targetFinder.findTarget();
            int intValue = InventoryUtil.find(Items.OBSIDIAN, 0, 8);
            int intValueValue = InventoryUtil.find(Items.END_CRYSTAL, 0, 8);
            if (intValue != -1 && intValueValue != -1) {
                ClientPlayerEntity player = mc.player;
                if (entity instanceof PlayerEntity playerValue) {
                    ClientWorld world = mc.world;
                    var entities = world.getEntities();
                    PlaceData placeData = this.findBestBlockToPlaceObsidianTheBiggestChinaWall(playerValue, world, player, entities);
                    PlaceData placeDataValue = this.findBestBlockToPlaceCrystalTheBiggestChinaWall(playerValue, world, player, entities);
                    PlaceData placeDataResult = this.autoKrot.isState() ? this.findBestBlockToBreakTheBiggestChinaWall(playerValue, player, entities) : null;
                    PlaceData lastData = placeData;
                    if (placeDataValue.damage() > placeData.damage()) {
                        lastData = placeDataValue;
                    }
                    if (placeDataResult != null && placeDataResult.damage() > lastData.damage()) {
                        lastData = placeDataResult;
                    }
                    if (placeData.placeResult() == PlaceResult.AWAITING) {
                        lastData = placeData;
                    }
                    if (this.lastData == null || this.lastData.placeResult() != PlaceResult.EXPLOSION) {
                        this.lastData = lastData;
                    }
                }
                if (this.lastData == null || this.lastData.placeResult() == PlaceResult.AWAITING) {
                    this.rotation = null;
                    this.updateAuraRotationTarget(null);
                } else if (this.lastData.damage() == 0.0) {
                    this.lastData = null;
                    this.rotation = null;
                    this.updateAuraRotationTarget(null);
                } else {
                    this.updateAuraRotationTarget(entity, this.getAuraRotationPoint(this.lastData));
                    if (this.lastData.placeResult() == PlaceResult.EXPLOSION) {
                        BlockPos position = this.lastData.blockPos();
                        Direction direction = this.lastData.face();
                        if (position != null && direction != null) {
                            Rotation rotation = RotationUtil.calculate(BlockFaceUtil.getFacePoint(position, direction));
                            boolean flag = !this.rotate.is("С Ауры") || RaycastUtil.raycastBlock(position, 5.5) != null && RaycastUtil.isLookingAtEntity(this.lastData.entity(), 3.0);
                            if (flag && RaycastUtil.raycastBlock(rotation, position, 5.5) != null && RaycastUtil.rayTrace(rotation, 3.0, this.lastData.entity().getBoundingBox())) {
                                if (this.rotate.is("Vanilla")) {
                                    RotationStorage.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1002, false);
                                }
                                mc.interactionManager.attackEntity(player, this.lastData.entity());
                                player.swingHand(Hand.MAIN_HAND);
                                this.lastData = null;
                                this.rotation = null;
                                return;
                            }
                        }
                        Rotation rotationValue = RotationUtil.calculate(NearestPointUtil.getNearestPoint(this.lastData.entity(), 0.0, 0.05F));
                        if (RaycastUtil.isLookingAtEntity(rotationValue, this.lastData.entity(), 3.0)) {
                            this.rotation = rotationValue;
                            if (this.rotate.is("Без ротации") || RaycastUtil.isLookingAtEntity(this.lastData.entity(), 3.0)) {
                                mc.interactionManager.attackEntity(player, this.lastData.entity());
                                player.swingHand(Hand.MAIN_HAND);
                            }
                            if (this.rotate.is("Vanilla")) {
                                RotationStorage.update(rotationValue, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1001, false);
                            }
                        }
                        this.lastData = null;
                        this.rotation = null;
                    } else {
                        BlockPos positionValue = this.lastData.blockPos();
                        if (positionValue == null) {
                            this.lastData = null;
                            this.rotation = null;
                            this.updateAuraRotationTarget(null);
                        } else {
                            Direction directionValue = this.lastData.face();
                            if (directionValue == null) {
                                this.lastData = null;
                                this.rotation = null;
                                this.updateAuraRotationTarget(null);
                            } else {
                                Rotation rotationResult = RotationUtil.calculate(BlockFaceUtil.getFacePoint(positionValue, directionValue));
                                if (RaycastUtil.raycastBlock(rotationResult, positionValue, 5.5) == null) {
                                    this.rotation = null;
                                    this.lastData = null;
                                    this.updateAuraRotationTarget(null);
                                } else {
                                    if (this.rotate.is("Без ротации") || RaycastUtil.raycastBlock(positionValue, 5.5) != null) {
                                        if (this.lastData.placeResult() == PlaceResult.BREAK) {
                                            if (!mc.world.getBlockState(positionValue).isAir()) {
                                                mc.interactionManager.attackBlock(positionValue, directionValue);
                                                player.swingHand(Hand.MAIN_HAND);
                                                if (!mc.world.getBlockState(positionValue).isAir() && mc.interactionManager.updateBlockBreakingProgress(positionValue, directionValue)) {
                                                    mc.particleManager.addBlockBreakingParticles(positionValue, directionValue);
                                                    player.swingHand(Hand.MAIN_HAND);
                                                }
                                            } else {
                                                mc.interactionManager.cancelBlockBreaking();
                                            }
                                            if (!this.hasVisual(positionValue)) {
                                                this.visualDatas.add(new VisualData(positionValue, 0.8F, 0.1F, 0.1F));
                                            }
                                        } else {
                                            int intValueResult = InventoryUtil.find(Items.OBSIDIAN, 0, 8);
                                            if (this.lastData.placeResult() == PlaceResult.PLACE_CRYSTAL) {
                                                intValueResult = InventoryUtil.find(Items.END_CRYSTAL, 0, 8);
                                            }
                                            if (intValueResult != -1) {
                                                if (this.lastSlot == -1) {
                                                    this.lastSlot = player.getInventory().selectedSlot;
                                                }
                                                this.ticksToBack = 3;
                                                this.selectHotbarSlot(intValueResult);
                                                Direction faceDirection = FaceDirectionUtil.getFaceDirection(rotationResult, player, positionValue);
                                                mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(positionValue.toCenterPos(), faceDirection, positionValue, false));
                                                player.swingHand(Hand.MAIN_HAND);
                                                BlockPos positionResult = this.lastData.placeResult().equals(PlaceResult.PLACE_CRYSTAL) ? positionValue : positionValue.offset(faceDirection);
                                                if (!this.hasVisual(positionResult)) {
                                                    this.visualDatas.add(new VisualData(positionResult, 0.2F, 0.8F, 1.0F));
                                                }
                                                this.lastData = null;
                                            }
                                        }
                                    }
                                    this.rotation = rotationResult;
                                }
                                if (this.rotation != null && this.rotate.is("Vanilla")) {
                                    RotationStorage.update(this.rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 999, false);
                                }
                            }
                        }
                    }
                }
            } else {
                this.lastData = null;
                this.rotation = null;
                this.updateAuraRotationTarget(null);
            }
        } else {
            this.clearAuraRotation();
        }
    }

    private void selectHotbarSlot(int selectedSlot) {
        if (mc.player != null && selectedSlot >= 0 && selectedSlot <= 8) {
            mc.player.getInventory().selectedSlot = selectedSlot;
            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            }
        }
    }

    @EventHandler
    public void onGameUpdate(GameUpdateEvent event) {
        if (this.rotation != null && this.rotate.is("Vanilla")) {
            RotationStorage.update(this.rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 999, false);
        }
        if (this.rotate.is("С Ауры") && this.auraRotationTarget != null && this.auraRotationPoint != null && ModuleRegistry.aura != null && !ModuleRegistry.aura.isEnable()) {
            ModuleRegistry.aura.rotateToPoint(this.auraRotationTarget, this.auraRotationPoint);
        }
    }

    @EventHandler
    public void onHandleBreaking(BlockBreakingEvent event) {
        if (this.lastData != null && this.lastData.placeResult() == PlaceResult.BREAK) {
            event.cancel();
        }
    }

    private Vec3d getAuraRotationPoint(PlaceData placeData) {
        if (placeData == null) {
            return null;
        } else if (placeData.blockPos() != null && placeData.face() != null) {
            return BlockFaceUtil.getFacePoint(placeData.blockPos(), placeData.face());
        } else {
            return placeData.entity() != null ? NearestPointUtil.getNearestPoint(placeData.entity(), 0.0, 0.05F) : null;
        }
    }

    private void updateAuraRotationTarget(LivingEntity entity) {
        this.updateAuraRotationTarget(entity, null);
    }

    private void updateAuraRotationTarget(LivingEntity auraRotationTarget, Vec3d auraRotationPoint) {
        if (!this.rotate.is("С Ауры")) {
            this.clearAuraRotation();
        } else if (auraRotationTarget != null && auraRotationPoint != null) {
            if (!this.auraRotationActive && RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
                RotationStorage.instance.stopRotation();
            }
            this.auraRotationTarget = auraRotationTarget;
            this.auraRotationPoint = auraRotationPoint;
            this.auraRotationActive = true;
            if (ModuleRegistry.aura != null) {
                ModuleRegistry.aura.setExternalRotationPoint(auraRotationPoint);
            }
        } else {
            this.clearAuraRotation();
        }
    }

    private void clearAuraRotation() {
        if (this.auraRotationActive || this.auraRotationTarget != null || this.auraRotationPoint != null) {
            this.auraRotationTarget = null;
            this.auraRotationPoint = null;
            this.auraRotationActive = false;
            if (ModuleRegistry.aura != null) {
                ModuleRegistry.aura.setExternalRotationPoint(null);
                if (!ModuleRegistry.aura.isEnable()) {
                    ModuleRegistry.aura.returnToCamera();
                }
            }
        }
    }

    private PlaceData findBestBlockToPlaceObsidianTheBiggestChinaWall(PlayerEntity player, World world, PlayerEntity playerValue, Iterable<Entity> entities) {
        BlockPos position = null;
        double doubleValue = 0.0;
        Direction direction = null;
        BlockPos blockPos = playerValue.getBlockPos();
        Mutable mutablePosition = new Mutable();
        for (int index = -5; index <= 5; index++) {
            int intValue = blockPos.getX() + index;
            for (int indexValue = -5; indexValue <= 5; indexValue++) {
                int intValueValue = blockPos.getY() + indexValue;
                for (int indexResult = -5; indexResult <= 5; indexResult++) {
                    mutablePosition.set(intValue, intValueValue, blockPos.getZ() + indexResult);
                    BlockState blockState = world.getBlockState(mutablePosition);
                    if (((brova.toshiba.mixin.BlockStateAccessor) (Object) blockState.getBlock().getDefaultState()).toshiba$isSolid()) {
                        Direction bestVisibleFaceToObsidian = BlockFaceUtil.getBestVisibleFaceToObsidian(mutablePosition, playerValue);
                        if (bestVisibleFaceToObsidian != null) {
                            Vec3d facePoint = BlockFaceUtil.getFacePoint(mutablePosition, bestVisibleFaceToObsidian);
                            Rotation rotation = RotationUtil.calculate(facePoint);
                            BlockHitResult blockHitResult = RaycastUtil.raycastBlock(rotation, mutablePosition, 5.5);
                            if (blockHitResult != null && blockHitResult.getSide() == bestVisibleFaceToObsidian) {
                                BlockPos positionValue = mutablePosition.offset(bestVisibleFaceToObsidian);
                                if (positionValue.getY() < (int) player.getY()) {
                                    BlockPos positionResult = positionValue.up();
                                    if (world.getBlockState(positionValue).isAir() && !this.isBlockedByEntitiesForObsidian(entities, positionValue).equals(ReturnResult.PLAYER) && !this.isBlockedByEntitiesForObsidian(entities, positionResult).equals(ReturnResult.PLAYER) && RaycastUtil.canReachBox(new Box(positionValue.getX() - 0.5, positionValue.getY(), positionValue.getZ() - 0.5, positionValue.getX() + 2, positionValue.getY() + 1.5, positionValue.getZ() + 1.5), 3.0)) {
                                        double doubleValueValue = ExplosionDamageUtil.getDamageOfGhostBlock(positionResult.toCenterPos(), player, positionValue) + 19.0F - 6.0F;
                                        if (!(doubleValueValue <= doubleValue) && !(doubleValueValue < 25.0)) {
                                            double damageOfGhostBlock = ExplosionDamageUtil.getDamageOfGhostBlock(positionResult.toCenterPos(), playerValue, positionValue);
                                            if (!(damageOfGhostBlock > 30.0)) {
                                                if (this.isBlockedByEntitiesForObsidian(entities, positionResult).equals(ReturnResult.ITEM)) {
                                                    return new PlaceData(null, null, 0.0, PlaceResult.AWAITING, null);
                                                }
                                                doubleValue = doubleValueValue;
                                                position = new BlockPos(mutablePosition.getX(), mutablePosition.getY(), mutablePosition.getZ());
                                                direction = bestVisibleFaceToObsidian;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        BlockPos positionItem = null;
        Direction directionValue = null;
        EndCrystalEntity endCrystalEntity = null;
        double doubleValueResult = 0.0;
        for (Entity entity : entities) {
            if (entity instanceof EndCrystalEntity endCrystalEntityValue) {
                Rotation rotationValue = RotationUtil.calculate(NearestPointUtil.getNearestPoint(endCrystalEntityValue, 0.0, 0.05F));
                double doubleValueItem = ExplosionDamageUtil.getExplosionDamage(endCrystalEntityValue.getBlockPos().toCenterPos(), player, false) + 30.0F;
                if (!(doubleValueItem <= doubleValueResult)) {
                    double explosionDamage = ExplosionDamageUtil.getExplosionDamage(endCrystalEntityValue.getBlockPos().toCenterPos(), playerValue, false);
                    if (!(explosionDamage > 30.0)) {
                        BlockPos positionEntry = endCrystalEntityValue.getBlockPos().down();
                        Direction UP = Direction.UP;
                        Vec3d facePointValue = BlockFaceUtil.getFacePoint(positionEntry, UP);
                        Rotation rotationResult = RotationUtil.calculate(facePointValue);
                        BlockHitResult blockHitResultValue = RaycastUtil.raycastBlock(rotationResult, positionEntry, 5.5);
                        if (blockHitResultValue != null && blockHitResultValue.getSide() == UP && RaycastUtil.rayTrace(rotationResult, 3.0, endCrystalEntityValue.getBoundingBox())) {
                            positionItem = positionEntry;
                            directionValue = UP;
                        }
                        if (RaycastUtil.isLookingAtEntity(rotationValue, endCrystalEntityValue, 3.0) || positionItem != null) {
                            doubleValueResult = doubleValueItem;
                            endCrystalEntity = endCrystalEntityValue;
                        }
                    }
                }
            }
        }
        return doubleValueResult > doubleValue ? new PlaceData(positionItem, directionValue, doubleValueResult, PlaceResult.EXPLOSION, endCrystalEntity) : new PlaceData(position, direction, doubleValue, PlaceResult.PLACE_OBSIDIAN, null);
    }

    private PlaceData findBestBlockToPlaceCrystalTheBiggestChinaWall(PlayerEntity player, World world, PlayerEntity playerValue, Iterable<Entity> entities) {
        BlockPos position = null;
        double doubleValue = 0.0;
        Direction direction = null;
        BlockPos blockPos = playerValue.getBlockPos();
        Mutable mutablePosition = new Mutable();
        for (int index = -5; index <= 5; index++) {
            int intValue = blockPos.getX() + index;
            for (int indexValue = -5; indexValue <= 5; indexValue++) {
                int intValueValue = blockPos.getY() + indexValue;
                for (int indexResult = -5; indexResult <= 5; indexResult++) {
                    mutablePosition.set(intValue, intValueValue, blockPos.getZ() + indexResult);
                    if (mutablePosition.getY() < (int) player.getY()) {
                        BlockState blockState = world.getBlockState(mutablePosition);
                        if (blockState.getBlock() == Blocks.OBSIDIAN || blockState.getBlock() == Blocks.BEDROCK) {
                            Direction bestVisibleFace = BlockFaceUtil.getBestVisibleFace(mutablePosition, playerValue);
                            if (bestVisibleFace != null) {
                                BlockPos positionValue = mutablePosition.up();
                                Vec3d facePoint = BlockFaceUtil.getFacePoint(mutablePosition, bestVisibleFace);
                                Rotation rotation = RotationUtil.calculate(facePoint);
                                BlockHitResult blockHitResult = RaycastUtil.raycastBlock(rotation, mutablePosition, 5.5);
                                if (blockHitResult != null && blockHitResult.getSide() == bestVisibleFace) {
                                    Vec3d facePointValue = BlockFaceUtil.getFacePoint(mutablePosition, Direction.UP);
                                    Rotation rotationValue = RotationUtil.calculate(facePointValue);
                                    BlockHitResult blockHitResultValue = RaycastUtil.raycastBlock(rotationValue, mutablePosition, 5.5);
                                    if (blockHitResultValue != null && blockHitResultValue.getSide() == Direction.UP) {
                                        bestVisibleFace = Direction.UP;
                                    }
                                    if (!this.isBlockedByEntitiesForCrystal(entities, positionValue) && RaycastUtil.canReachBox(new Box(positionValue.getX() - 0.5, positionValue.getY(), positionValue.getZ() - 0.5, positionValue.getX() + 2, positionValue.getY() + 1.5, positionValue.getZ() + 1.5), 3.0)) {
                                        double doubleValueValue = ExplosionDamageUtil.getExplosionDamage(positionValue.toCenterPos(), player, false) + 19.0F;
                                        if (!(doubleValueValue <= doubleValue) && !(doubleValueValue < 31.0)) {
                                            double explosionDamage = ExplosionDamageUtil.getExplosionDamage(positionValue.toCenterPos(), playerValue, false);
                                            if (!(explosionDamage > 30.0)) {
                                                doubleValue = doubleValueValue;
                                                position = new BlockPos(mutablePosition.getX(), mutablePosition.getY(), mutablePosition.getZ());
                                                direction = bestVisibleFace;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new PlaceData(position, direction, doubleValue, PlaceResult.PLACE_CRYSTAL, null);
    }

    private ReturnResult isBlockedByEntitiesForObsidian(Iterable<Entity> entities, BlockPos position) {
        for (Entity entity : entities) {
            Box box = new Box(position.getX(), position.getY() - 0.2F, position.getZ(), position.getX() + 1.0, position.getY() + 2.2, position.getZ() + 1.0);
            if (entity instanceof ItemEntity && !entity.isOnGround() && entity.getBoundingBox().intersects(box)) {
                return ReturnResult.ITEM;
            }
            if ((entity instanceof PlayerEntity || entity instanceof EndCrystalEntity) && ExplosionDamageUtil.intersectsEntityBox(position, entity)) {
                return ReturnResult.PLAYER;
            }
        }
        return ReturnResult.NONE;
    }

    private boolean isBlockedByEntitiesForCrystal(Iterable<Entity> entities, BlockPos position) {
        for (Entity entity : entities) {
            if (ExplosionDamageUtil.intersectsEntityBox(position, entity)) {
                return true;
            }
        }
        return false;
    }

    private PlaceData findBestBlockToBreakTheBiggestChinaWall(PlayerEntity player, PlayerEntity playerValue, Iterable<Entity> entities) {
        BlockPos position = null;
        double doubleValue = 0.0;
        Direction direction = null;
        BlockPos blockPos = playerValue.getBlockPos();
        Mutable mutablePosition = new Mutable();
        for (int index = -5; index <= 5; index++) {
            for (int indexValue = -5; indexValue <= 5; indexValue++) {
                for (int indexResult = -5; indexResult <= 5; indexResult++) {
                    mutablePosition.set(blockPos.getX() + index, blockPos.getY() + indexValue, blockPos.getZ() + indexResult);
                    if (!(mutablePosition.getY() >= player.getY())) {
                        BlockState blockState = mc.world.getBlockState(mutablePosition);
                        if (((brova.toshiba.mixin.BlockStateAccessor) (Object) blockState).toshiba$isSolid()) {
                            Direction bestVisibleFace = BlockFaceUtil.getBestVisibleFace(mutablePosition, playerValue);
                            if (bestVisibleFace != null) {
                                Rotation rotation = RotationUtil.calculate(BlockFaceUtil.getFacePoint(mutablePosition, bestVisibleFace));
                                BlockHitResult blockHitResult = RaycastUtil.raycastBlock(rotation, mutablePosition, 5.5);
                                if (blockHitResult != null && blockHitResult.getSide() == bestVisibleFace) {
                                    BlockPos positionValue = mutablePosition.up();
                                    if (!this.isBlockedByEntitiesForBreak(entities, positionValue) && RaycastUtil.canReachBox(new Box(positionValue.getX() - 0.5, positionValue.getY(), positionValue.getZ() - 0.5, positionValue.getX() + 2, positionValue.getY() + 1.5, positionValue.getZ() + 1.5), 3.0)) {
                                        BlockPos positionResult = mc.world.getBlockState(positionValue).isAir() ? mutablePosition : positionValue;
                                        BlockState blockStateValue = mc.world.getBlockState(positionResult);
                                        if (InventoryUtil.findBestToolSlot(blockStateValue) != -1) {
                                            double doubleValueValue = ExplosionDamageUtil.getExplosionDamage(positionValue.toCenterPos(), player, false) + 7.0F;
                                            if (!(doubleValueValue <= doubleValue) && !(doubleValueValue < 19.0)) {
                                                double explosionDamage = ExplosionDamageUtil.getExplosionDamage(positionValue.toCenterPos(), playerValue, false);
                                                if (!(explosionDamage > 30.0)) {
                                                    position = positionResult.toImmutable();
                                                    direction = bestVisibleFace;
                                                    doubleValue = doubleValueValue;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new PlaceData(position, direction, doubleValue, PlaceResult.BREAK, null);
    }

    private boolean isBlockedByEntitiesForBreak(Iterable<Entity> entities, BlockPos position) {
        for (Entity entity : entities) {
            if (ExplosionDamageUtil.intersectsEntityBox(position, entity) && !(entity instanceof ItemEntity)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVisual(BlockPos position) {
        for (VisualData visualData : this.visualDatas) {
            if (visualData.getBlockPos().equals(position)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (mc.world != null && mc.player != null && !this.visualDatas.isEmpty()) {
            this.visualDatas.removeIf(VisualData::isFinished);
            Vec3d pos = event.getCamera().getPos();
            MatrixStack matrices = event.getMatrices();
            matrices.push();
            matrices.translate(-pos.x, -pos.y, -pos.z);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Tessellator instance = Tessellator.getInstance();
            for (VisualData visualData : this.visualDatas) {
                visualData.update();
                float alpha = visualData.getAlpha();
                if (!(alpha <= 0.01F)) {
                    Box box = new Box(visualData.getBlockPos());
                    BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    this.addFilledBoxDirect(buffer, positionMatrix, box, visualData.getR(), visualData.getG(), visualData.getB(), alpha * 0.25F);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    RenderSystem.lineWidth(1.5F);
                    BufferBuilder bufferValue = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                    this.addOutlinedBoxDirect(bufferValue, positionMatrix, box, visualData.getR(), visualData.getG(), visualData.getB(), alpha);
                    BufferRenderer.drawWithGlobalProgram(bufferValue.end());
                }
            }
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            matrices.pop();
        }
    }

    private void addFilledBoxDirect(BufferBuilder buffer, Matrix4f matrix, Box box, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    private void addOutlinedBoxDirect(BufferBuilder buffer, Matrix4f matrix, Box box, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    public ModeSetting getRotate() {
        return this.rotate;
    }

    public BooleanSetting getAutoKrot() {
        return this.autoKrot;
    }

    public TargetSelector getTargetFinder() {
        return this.targetFinder;
    }

    public int getTicksToBack() {
        return this.ticksToBack;
    }

    public int getLastSlot() {
        return this.lastSlot;
    }

    public PlaceData getLastData() {
        return this.lastData;
    }

    public ConcurrentLinkedQueue<VisualData> getVisualDatas() {
        return this.visualDatas;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public LivingEntity getAuraRotationTarget() {
        return this.auraRotationTarget;
    }

    public Vec3d getAuraRotationPoint() {
        return this.auraRotationPoint;
    }

    public boolean isAuraRotationActive() {
        return this.auraRotationActive;
    }

    public void setTicksToBack(int ticksToBack) {
        this.ticksToBack = ticksToBack;
    }

    public void setLastSlot(int lastSlot) {
        this.lastSlot = lastSlot;
    }

    public void setLastData(PlaceData lastData) {
        this.lastData = lastData;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public void setAuraRotationTarget(LivingEntity auraRotationTarget) {
        this.auraRotationTarget = auraRotationTarget;
    }

    public void setAuraRotationPoint(Vec3d auraRotationPoint) {
        this.auraRotationPoint = auraRotationPoint;
    }

    public void setAuraRotationActive(boolean auraRotationActive) {
        this.auraRotationActive = auraRotationActive;
    }

    public enum ReturnResult {

        ITEM, PLAYER, NONE
    }

    public enum PlaceResult {

        PLACE_OBSIDIAN, PLACE_CRYSTAL, EXPLOSION, BREAK, AWAITING
    }

    public static class VisualData {

        final BlockPos blockPos;

        private final Animation animation = new Animation(1.0F, 3.33F, PowerEasing.CUBIC_OUT);

        final float r;

        final float g;

        final float b;

        public VisualData(BlockPos blockPos, float r, float g, float b) {
            this.blockPos = blockPos;
            this.r = r;
            this.g = g;
            this.b = b;
            this.animation.update(0.0F);
        }

        public void update() {
            this.animation.update(0.0F);
        }

        public float getAlpha() {
            return this.animation.getValue();
        }

        public boolean isFinished() {
            return this.animation.getValue() <= 0.005F;
        }

        public BlockPos getBlockPos() {
            return this.blockPos;
        }

        public Animation getAnimation() {
            return this.animation;
        }

        public float getR() {
            return this.r;
        }

        public float getG() {
            return this.g;
        }

        public float getB() {
            return this.b;
        }
    }

    public static record PlaceData(BlockPos blockPos, Direction face, double damage, PlaceResult placeResult, Entity entity) {
    }
}
