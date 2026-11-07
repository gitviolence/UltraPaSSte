package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.GameUpdateEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.WebTrap.Placement;
import brova.toshiba.client.modules.impl.combat.WebTrap.PlacementAnimation;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
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
import brova.toshiba.client.utils.world.RaycastUtil;
import brova.toshiba.mixin.IClientPlayerInteractionManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class WebTrap extends Module {

    public static WebTrap INSTANCE;

    private final ModeSetting rotate = new ModeSetting("Ротация", "Vanilla", "Vanilla", "С Ауры", "Без ротации");

    private final FloatSetting webCount = new FloatSetting("Количество", 2.0F, 1.0F, 8.0F, 1.0F);

    private final FloatSetting range = new FloatSetting("Дистанция", 4.5F, 2.0F, 5.5F, 0.1F);

    private final FloatSetting delay = new FloatSetting("Задержка", 1.0F, 0.0F, 10.0F, 1.0F);

    private final BooleanSetting swing = new BooleanSetting("Взмах", true);

    private final BooleanSetting render = new BooleanSetting("Визуал", true);

    private final TargetSelector targetFinder = new TargetSelectorBuilder().range(() -> this.range.get()).throughWalls(() -> true).saveCurrentTarget(() -> false).filterEntities(PlayerEntity.class, input -> Toshiba.INSTANCE.friendStorage == null || !Toshiba.INSTANCE.friendStorage.isFriend(input.getName().getString()), 0);

    private int ticksToBack;

    private int lastSlot = -1;

    private int delayTicks;

    private Rotation rotation;

    private LivingEntity auraRotationTarget;

    private Vec3d auraRotationPoint;

    private boolean auraRotationActive;

    private final ConcurrentLinkedQueue<PlacementAnimation> visualDatas = new ConcurrentLinkedQueue<>();

    public WebTrap() {
        super("WebTrap", "Автоматически застраивает игрока паутиной", ModuleCategory.COMBAT);
        INSTANCE = this;
        this.addSettings(this.rotate, this.webCount, this.range, this.delay, this.swing, this.render);
    }

    @Override
    public void onDisable() {
        this.delayTicks = 0;
        this.ticksToBack = 0;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.rotation = (Rotation) accessValue;
        });
        this.updateExternalRotationPointWithReturnToCamera();
        if (this.lastSlot != -1 && mc.player != null) {
            int lastSlot = this.lastSlot;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { lastSlot }, (accessReceiver, accessArguments) -> {
                ((WebTrap) accessReceiver).updateInventoryWithInvokeSyncSelectedSlot(((int) accessArguments[0]));
            });
        }
        this.lastSlot = -1;
        if (!this.rotate.is("С Ауры") && RotationStorage.instance != null) {
            RotationStorage.instance.stopRotation();
        }
        this.visualDatas.clear();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null && !mc.player.isUsingItem()) {
            if (this.ticksToBack > 0) {
                this.ticksToBack--;
            }
            if (this.ticksToBack == 0 && this.lastSlot != -1) {
                this.updateInventoryWithInvokeSyncSelectedSlot(this.lastSlot);
                this.lastSlot = -1;
            }
            if (this.delayTicks > 0) {
                this.delayTicks--;
            } else if (this.targetFinder.findTarget() instanceof PlayerEntity player) {
                int intValue = -1;
                Hand hand;
                if (mc.player.getOffHandStack().isOf(Items.COBWEB)) {
                    hand = Hand.OFF_HAND;
                } else if (mc.player.getMainHandStack().isOf(Items.COBWEB)) {
                    hand = Hand.MAIN_HAND;
                } else {
                    intValue = InventoryUtil.find(Items.COBWEB, 0, 8);
                    if (intValue == -1) {
                        this.updateLocalState();
                        if (this.ticksToBack == 0 && this.lastSlot != -1) {
                            this.ticksToBack = 2;
                        }
                        return;
                    }
                    hand = Hand.MAIN_HAND;
                }
                var values = this.createListFromUpWithNorth(player);
                int intValueValue = (int) this.webCount.get();
                int intValueResult = 0;
                for (BlockPos position : values) {
                    if (mc.world.getBlockState(position).isOf(Blocks.COBWEB)) {
                        intValueResult++;
                    }
                }
                if (intValueResult >= intValueValue) {
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                        ((WebTrap) accessReceiver).updateLocalState();
                    });
                    if (this.ticksToBack == 0 && this.lastSlot != -1) {
                        this.ticksToBack = 2;
                    }
                } else {
                    Placement placement = null;
                    double doubleValue = this.range.get();
                    for (BlockPos positionValue : values) {
                        BlockState blockState = mc.world.getBlockState(positionValue);
                        if (!blockState.isOf(Blocks.COBWEB) && (blockState.isReplaceable() || blockState.isAir())) {
                            Placement placementValue = this.createWebTrapPlacementFromEyePosWithOffset(positionValue, doubleValue);
                            if (placementValue != null) {
                                placement = placementValue;
                                break;
                            }
                        }
                    }
                    if (placement == null) {
                        this.updateLocalState();
                        if (this.ticksToBack == 0 && this.lastSlot != -1) {
                            this.ticksToBack = 2;
                        }
                    } else {
                        boolean flag = false;
                        if (!this.rotate.is("Vanilla")) {
                            if (this.rotate.is("С Ауры")) {
                                this.rotation = null;
                                this.updateRotatingWithStopRotation(player, placement.hitVec());
                                flag = RaycastUtil.raycastBlock(placement.neighborPos(), doubleValue + 0.5) != null;
                            } else {
                                Object value = null;
                                brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                                    accessReceiver.rotation = (Rotation) accessValue;
                                });
                                this.updateExternalRotationPointWithReturnToCamera();
                                flag = true;
                            }
                        } else {
                            Rotation rotation = RotationUtil.calculate(placement.hitVec());
                            this.rotation = rotation;
                            RotationStorage.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 999, false);
                            flag = RaycastUtil.raycastBlock(rotation, placement.neighborPos(), doubleValue + 0.5) != null || RaycastUtil.raycastBlock(placement.neighborPos(), doubleValue + 0.5) != null;
                        }
                        if (flag) {
                            if (intValue != -1 && mc.player.getInventory().selectedSlot != intValue) {
                                if (this.lastSlot == -1) {
                                    this.lastSlot = mc.player.getInventory().selectedSlot;
                                }
                                this.updateInventoryWithInvokeSyncSelectedSlot(intValue);
                            }
                            if (hand == Hand.MAIN_HAND && this.lastSlot != -1) {
                                this.ticksToBack = 3;
                            }
                            Vec3d positionResult = placement.hitVec();
                            BlockPos positionItem = placement.neighborPos();
                            double doubleValueValue = Math.max(0.0, Math.min(1.0, positionResult.x - positionItem.getX()));
                            double doubleValueResult = Math.max(0.0, Math.min(1.0, positionResult.y - positionItem.getY()));
                            double doubleValueItem = Math.max(0.0, Math.min(1.0, positionResult.z - positionItem.getZ()));
                            Vec3d positionEntry = new Vec3d(positionItem.getX() + doubleValueValue, positionItem.getY() + doubleValueResult, positionItem.getZ() + doubleValueItem);
                            mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(positionEntry, placement.face(), positionItem, false));
                            if (this.swing.isState()) {
                                mc.player.swingHand(hand);
                            }
                            if (this.render.isState() && !this.isEqualsWithBlockPos(placement.targetPos())) {
                                this.visualDatas.add(new PlacementAnimation(placement.targetPos(), 0.9F, 0.9F, 0.95F));
                            }
                            this.delayTicks = (int) this.delay.get();
                        }
                    }
                }
            } else {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((WebTrap) accessReceiver).updateLocalState();
                });
                if (this.ticksToBack == 0 && this.lastSlot != -1) {
                    this.ticksToBack = 2;
                }
            }
        } else {
            this.updateLocalState();
        }
    }

    @EventHandler
    public void onGameUpdate(GameUpdateEvent event) {
        if (this.rotation != null && this.rotate.is("Vanilla")) {
            RotationStorage.update(this.rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 999, false);
        }
        if (this.rotate.is("С Ауры") && this.auraRotationTarget != null && this.auraRotationPoint != null && ModuleRegistry.aura != null) {
            ModuleRegistry.aura.setExternalRotationPoint(this.auraRotationPoint);
            if (!ModuleRegistry.aura.isEnable() || ModuleRegistry.aura.getTarget() == null) {
                ModuleRegistry.aura.rotateToPoint(this.auraRotationTarget, this.auraRotationPoint);
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (this.render.isState() && mc.world != null && mc.player != null && !this.visualDatas.isEmpty()) {
            this.visualDatas.removeIf(PlacementAnimation::isFinished);
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
            for (PlacementAnimation placementAnimation : this.visualDatas) {
                placementAnimation.update();
                float alpha = placementAnimation.getAlpha();
                if (!(alpha <= 0.01F)) {
                    Box box = new Box(placementAnimation.blockPos);
                    BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    this.appendFilledTrapBox(buffer, positionMatrix, box, placementAnimation.r, placementAnimation.g, placementAnimation.b, alpha * 0.25F);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    RenderSystem.lineWidth(1.5F);
                    BufferBuilder bufferValue = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                    this.appendTrapBoxOutline(bufferValue, positionMatrix, box, placementAnimation.r, placementAnimation.g, placementAnimation.b, alpha);
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

    private List<BlockPos> createListFromUpWithNorth(PlayerEntity player) {
        ArrayList<net.minecraft.util.math.BlockPos> values = new ArrayList<>();
        BlockPos blockPos = player.getBlockPos();
        BlockPos position = blockPos.up();
        values.add(blockPos);
        values.add(position);
        values.add(blockPos.north());
        values.add(blockPos.east());
        values.add(blockPos.south());
        values.add(blockPos.west());
        values.add(position.north());
        values.add(position.east());
        values.add(position.south());
        values.add(position.west());
        values.add(position.up());
        return values;
    }

    private Placement createWebTrapPlacementFromEyePosWithOffset(BlockPos position, double doubleValue) {
        if (mc.world != null && mc.player != null) {
            Vec3d eyePos = mc.player.getEyePos();
            double doubleValueValue = doubleValue * doubleValue;
            Direction[] directions = new Direction[] { Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP };
            for (Direction direction : directions) {
                BlockPos positionValue = position.offset(direction);
                BlockState blockState = mc.world.getBlockState(positionValue);
                if (!blockState.isAir() && !blockState.isReplaceable()) {
                    Direction opposite = direction.getOpposite();
                    Vec3d facePoint = BlockFaceUtil.getFacePoint(positionValue, opposite);
                    if (!(eyePos.squaredDistanceTo(facePoint) > doubleValueValue)) {
                        return new Placement(position, positionValue, opposite, facePoint);
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private void updateInventoryWithInvokeSyncSelectedSlot(int selectedSlot) {
        if (mc.player != null && mc.interactionManager != null && selectedSlot >= 0 && selectedSlot <= 8) {
            mc.player.getInventory().selectedSlot = selectedSlot;
            ((IClientPlayerInteractionManager) mc.interactionManager).invokeSyncSelectedSlot();
        }
    }

    private void updateRotatingWithStopRotation(LivingEntity auraRotationTarget, Vec3d auraRotationPoint) {
        if (!this.rotate.is("С Ауры")) {
            this.updateExternalRotationPointWithReturnToCamera();
        } else if (auraRotationTarget != null && auraRotationPoint != null) {
            if (!this.auraRotationActive && RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
                RotationStorage.instance.stopRotation();
            }
            this.auraRotationTarget = auraRotationTarget;
            this.auraRotationPoint = auraRotationPoint;
            this.auraRotationActive = true;
            if (ModuleRegistry.aura != null) {
                ModuleRegistry.aura.setExternalRotationPoint(auraRotationPoint);
                if (!ModuleRegistry.aura.isEnable() || ModuleRegistry.aura.getTarget() == null) {
                    ModuleRegistry.aura.rotateToPoint(auraRotationTarget, auraRotationPoint);
                }
            }
        } else {
            this.updateExternalRotationPointWithReturnToCamera();
        }
    }

    private void updateExternalRotationPointWithReturnToCamera() {
        if (this.auraRotationActive || this.auraRotationTarget != null || this.auraRotationPoint != null) {
            Object value = null;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                accessReceiver.auraRotationTarget = (LivingEntity) accessValue;
            });
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

    private void updateLocalState() {
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.rotation = (Rotation) accessValue;
        });
        this.updateExternalRotationPointWithReturnToCamera();
    }

    private boolean isEqualsWithBlockPos(BlockPos position) {
        for (PlacementAnimation placementAnimation : this.visualDatas) {
            if (placementAnimation.getBlockPos().equals(position)) {
                return true;
            }
        }
        return false;
    }

    private void appendFilledTrapBox(BufferBuilder buffer, Matrix4f matrix, Box box, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
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
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, minX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, minZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, maxX, minY, maxZ).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    private void appendTrapBoxOutline(BufferBuilder buffer, Matrix4f matrix, Box box, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
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

    public FloatSetting getWebCount() {
        return this.webCount;
    }

    public FloatSetting getRange() {
        return this.range;
    }

    public FloatSetting getDelay() {
        return this.delay;
    }

    public BooleanSetting getSwing() {
        return this.swing;
    }

    public BooleanSetting getRender() {
        return this.render;
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

    public int getDelayTicks() {
        return this.delayTicks;
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

    public ConcurrentLinkedQueue<PlacementAnimation> getVisualDatas() {
        return this.visualDatas;
    }

    public void setTicksToBack(int ticksToBack) {
        this.ticksToBack = ticksToBack;
    }

    public void setLastSlot(int lastSlot) {
        this.lastSlot = lastSlot;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
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

    public static class PlacementAnimation {

        final BlockPos blockPos;

        private final Animation animation = new Animation(1.0F, 3.33F, PowerEasing.CUBIC_OUT);

        final float r;

        final float g;

        final float b;

        public PlacementAnimation(BlockPos blockPos, float r, float g, float b) {
            this.blockPos = blockPos;
            this.r = r;
            this.g = g;
            this.b = b;
            this.animation.update(0.0F);
        }

        public void update() {
            float floatValue = 0.0F;
            Animation animation = this.animation;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(animation, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((Animation) accessReceiver).update(((float) accessArguments[0]));
            });
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

    public static record Placement(BlockPos targetPos, BlockPos neighborPos, Direction face, Vec3d hitVec) {
    }
}
