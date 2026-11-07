package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.Trajectory.EntityCollision;
import brova.toshiba.client.modules.impl.render.Trajectory.Physics;
import brova.toshiba.client.modules.impl.render.Trajectory.TraceResult;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class Trajectory extends Module {

    public static Trajectory INSTANCE = new Trajectory();

    private final FloatSetting pointSize = new FloatSetting("Размер точки", 0.35F, 0.1F, 1.0F, 0.05F);

    private final ColorSetting trajectoryColor = new ColorSetting("Цвет траектории", -10184961);

    public Trajectory() {
        super("Trajectory", "Показывает траекторию арбалета,трезубца,эндер-жемчуга,лука...", ModuleCategory.RENDER);
        this.addSettings(this.pointSize, this.trajectoryColor);
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (mc.player != null && mc.world != null) {
            ItemStack stack = this.createMainHandStackWithOffHandStack();
            if (!stack.isEmpty()) {
                Physics physics = this.createTrajectoryPhysicsFromItemWithUsingItem(stack);
                if (physics != null) {
                    float tickDelta = event.getTickDelta();
                    Camera camera = event.getCamera();
                    Vec3d pos = camera.getPos();
                    boolean firstPerson = mc.options.getPerspective().isFirstPerson();
                    Vec3d position = firstPerson ? pos : mc.player.getCameraPosVec(tickDelta);
                    Vec3d[] positions = this.createPositionFromCosWithSin(stack, camera, tickDelta);
                    TraceResult[] traceResults = new TraceResult[positions.length];
                    int intValue = 0;
                    for (Vec3d positionValue : positions) {
                        TraceResult traceResult = this.createTrajectoryTraceResultFromMultiplyWithNormalize(mc.player, physics, position, positionValue);
                        if (traceResult != null && traceResult.points().length >= 2) {
                            traceResults[intValue++] = traceResult;
                        }
                    }
                    if (intValue != 0) {
                        MatrixStack matrices = event.getMatrices();
                        int themeColor = ColorMathUtil.getThemeColor();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.disableCull();
                        RenderSystem.enableDepthTest();
                        RenderSystem.depthMask(false);
                        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                        RenderSystem.lineWidth(1.0F);
                        matrices.push();
                        matrices.translate(-pos.x, -pos.y, -pos.z);
                        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                        for (int index = 0; index < intValue; index++) {
                            TraceResult traceResultValue = traceResults[index];
                            this.renderSubtractWithMultiply(positionMatrix, traceResultValue.points(), camera, ColorMathUtil.setAlphaColor(this.trajectoryColor.getColor(), 190));
                            if (traceResultValue.entityHit() != null && traceResultValue.entityHit().isAlive()) {
                                this.renderBoundingBoxWithBegin(positionMatrix, traceResultValue.entityHit(), ColorMathUtil.rgba(255, 70, 70, 210));
                            } else if (traceResultValue.blockHit() != null) {
                                this.renderBeginWithWithGlobalProgram(positionMatrix, traceResultValue.hitPos(), this.trajectoryColor.getColor());
                            }
                            if (stack.isOf(Items.SPLASH_POTION) && traceResultValue.hitPos() != null) {
                                this.renderColorWithVertex(matrices, traceResultValue.hitPos(), themeColor);
                            }
                        }
                        matrices.pop();
                        RenderSystem.depthMask(true);
                        RenderSystem.enableCull();
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
                }
            }
        }
    }

    private ItemStack createMainHandStackWithOffHandStack() {
        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (!mainHandStack.isEmpty() && this.createTrajectoryPhysicsFromItemWithUsingItem(mainHandStack) != null) {
            return mainHandStack;
        } else {
            ItemStack offHandStack = mc.player.getOffHandStack();
            return !offHandStack.isEmpty() && this.createTrajectoryPhysicsFromItemWithUsingItem(offHandStack) != null ? offHandStack : ItemStack.EMPTY;
        }
    }

    private Physics createTrajectoryPhysicsFromItemWithUsingItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG) {
            return new Physics(1.5, 0.03, 0.99);
        } else if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            return new Physics(0.5, 0.05, 0.99);
        } else if (item instanceof BowItem) {
            float floatValue = 1.0F;
            if (mc.player.isUsingItem() && mc.player.getActiveItem() == stack) {
                float itemUseTime = mc.player.getItemUseTime();
                float floatValueValue = itemUseTime / 20.0F;
                floatValueValue = (floatValueValue * floatValueValue + floatValueValue * 2.0F) / 3.0F;
                floatValue = Math.min(floatValueValue, 1.0F);
            }
            double doubleValue = 3.0 * floatValue;
            return doubleValue <= 0.01 ? null : new Physics(doubleValue, 0.05, 0.99);
        } else if (item instanceof CrossbowItem) {
            return !CrossbowItem.isCharged(stack) ? null : new Physics(3.15, 0.05, 0.99);
        } else {
            return item instanceof TridentItem ? new Physics(2.5, 0.05, 0.99) : null;
        }
    }

    private Vec3d[] createPositionFromCosWithSin(ItemStack stack, Camera camera, float floatValue) {
        boolean firstPerson = mc.options.getPerspective().isFirstPerson();
        Vec3d position;
        if (firstPerson) {
            float pitch = camera.getPitch();
            float yaw = camera.getYaw();
            float floatValueValue = pitch * (float) (Math.PI / 180.0);
            float floatValueResult = -yaw * (float) (Math.PI / 180.0);
            float floatValueItem = MathHelper.cos(floatValueResult);
            float floatValueEntry = MathHelper.sin(floatValueResult);
            float floatValueCopy = MathHelper.cos(floatValueValue);
            float floatValue2 = MathHelper.sin(floatValueValue);
            position = new Vec3d(floatValueEntry * floatValueCopy, -floatValue2, floatValueItem * floatValueCopy).normalize();
        } else {
            position = mc.player.getRotationVec(floatValue).normalize();
        }
        if (stack.getItem() instanceof CrossbowItem && InventoryUtil.getEnchantmentLevel(stack, Enchantments.MULTISHOT) > 0) {
            float floatValue3 = (float) (MathHelper.atan2(position.z, position.x) * (180.0 / Math.PI)) - 90.0F;
            float floatValue4 = (float) (-(MathHelper.atan2(position.y, MathHelper.sqrt((float) (position.x * position.x + position.z * position.z))) * (180.0 / Math.PI)));
            return new Vec3d[] { this.createPositionFromCosWithSin(floatValue3 - 10.0F, floatValue4), position, this.createPositionFromCosWithSin(floatValue3 + 10.0F, floatValue4) };
        } else {
            return new Vec3d[] { position };
        }
    }

    private Vec3d createPositionFromCosWithSin(float floatValue, float floatValueValue) {
        float floatValueResult = floatValue * (float) (Math.PI / 180.0);
        float floatValueItem = floatValueValue * (float) (Math.PI / 180.0);
        float floatValueEntry = MathHelper.sin(-floatValueResult - (float) Math.PI) * -MathHelper.cos(-floatValueItem);
        float floatValueCopy = MathHelper.sin(-floatValueItem);
        float floatValue2 = MathHelper.cos(-floatValueResult - (float) Math.PI) * -MathHelper.cos(-floatValueItem);
        return new Vec3d(floatValueEntry, floatValueCopy, floatValue2).normalize();
    }

    private TraceResult createTrajectoryTraceResultFromMultiplyWithNormalize(PlayerEntity player, Physics physics, Vec3d position, Vec3d positionValue) {
        Vec3d positionResult = position;
        Vec3d positionItem = positionValue.normalize().multiply(physics.velocity());
        Vec3d[] positions = new Vec3d[441];
        int intValue = 0;
        positions[intValue++] = position;
        Entity entity = null;
        Vec3d positionEntry = null;
        for (int index = 0; index < 440; index++) {
            Vec3d positionCopy = positionResult.add(positionItem.multiply(0.5));
            if (entity == null) {
                EntityCollision entityCollision = this.createTrajectoryEntityCollisionFromExpandWithOtherEntities(positionResult, positionCopy, player);
                if (entityCollision != null) {
                    entity = entityCollision.entity();
                    positionEntry = entityCollision.hitPos();
                }
            }
            BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(positionResult, positionCopy, ShapeType.COLLIDER, FluidHandling.NONE, player));
            if (blockHitResult.getType() == Type.BLOCK) {
                positions[intValue++] = blockHitResult.getPos();
                return new TraceResult(this.createPositionFromArraycopy(positions, intValue), blockHitResult, blockHitResult.getPos(), entity, positionEntry);
            }
            positions[intValue++] = positionCopy;
            positionResult = positionCopy;
            boolean of = mc.world.getBlockState(BlockPos.ofFloored(positionCopy)).isOf(Blocks.WATER);
            double doubleValue = Math.pow(of ? 0.8 : physics.drag(), 0.5);
            positionItem = positionItem.multiply(doubleValue).subtract(0.0, physics.gravity() * 0.5, 0.0);
            if (positionCopy.y <= mc.world.getBottomY()) {
                break;
            }
        }
        Vec3d position2 = positionEntry != null ? positionEntry : positions[intValue - 1];
        return new TraceResult(this.createPositionFromArraycopy(positions, intValue), null, position2, entity, positionEntry);
    }

    private Vec3d[] createPositionFromArraycopy(Vec3d[] positions, int intValue) {
        Vec3d[] positionsValue = new Vec3d[intValue];
        System.arraycopy(positions, 0, positionsValue, 0, intValue);
        return positionsValue;
    }

    private EntityCollision createTrajectoryEntityCollisionFromExpandWithOtherEntities(Vec3d position, Vec3d positionValue, Entity entity) {
        Box box = new Box(position, positionValue).expand(1.0);
        Entity entityValue = null;
        Vec3d positionResult = null;
        double MAX_VALUE = Double.MAX_VALUE;
        for (Entity entityResult : mc.world.getOtherEntities(entity, box, input -> input != null && input.isAlive() && input.canHit())) {
            var optional = entityResult.getBoundingBox().expand(0.3).raycast(position, positionValue);
            if (!optional.isEmpty()) {
                double doubleValue = position.squaredDistanceTo(optional.get());
                if (doubleValue < MAX_VALUE) {
                    MAX_VALUE = doubleValue;
                    entityValue = entityResult;
                    positionResult = optional.get();
                }
            }
        }
        return entityValue == null ? null : new EntityCollision(entityValue, positionResult);
    }

    private void renderSubtractWithMultiply(Matrix4f matrix, Vec3d[] positions, Camera camera, int intValue) {
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        int intValueEntry = intValue >> 24 & 0xFF;
        float floatValue = 0.0018F;
        Quaternionf rotation = camera.getRotation();
        Vector3f vector3f = new Vector3f(1.0F, 0.0F, 0.0F);
        Vector3f vector3fValue = new Vector3f(0.0F, 1.0F, 0.0F);
        rotation.transform(vector3f);
        rotation.transform(vector3fValue);
        Vec3d position = new Vec3d(vector3f.x, vector3f.y, vector3f.z);
        Vec3d positionValue = new Vec3d(vector3fValue.x, vector3fValue.y, vector3fValue.z);
        Vec3d pos = camera.getPos();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int index = 1; index < positions.length; index++) {
            Vec3d positionResult = positions[index];
            Vec3d positionItem;
            if (index < positions.length - 1) {
                positionItem = positions[index + 1].subtract(positionResult);
            } else {
                positionItem = positionResult.subtract(positions[index - 1]);
            }
            if (positionItem.lengthSquared() < 1.0E-6) {
                positionItem = new Vec3d(0.0, 1.0, 0.0);
            }
            Vec3d positionEntry = positionItem.normalize();
            Vec3d positionCopy = position.subtract(positionEntry.multiply(position.dotProduct(positionEntry)));
            if (positionCopy.lengthSquared() < 1.0E-4) {
                positionCopy = positionValue.subtract(positionEntry.multiply(positionValue.dotProduct(positionEntry)));
            }
            if (positionCopy.lengthSquared() > 1.0E-6) {
                positionCopy = positionCopy.normalize();
            } else {
                positionCopy = position;
            }
            Vec3d position2 = positionResult.subtract(pos);
            double length = position2.length();
            positionCopy = positionCopy.multiply(floatValue * length);
            Vec3d position3 = positionCopy.multiply(1.2);
            buffer.vertex(matrix, (float) (positionResult.x - positionCopy.x - position3.x), (float) (positionResult.y - positionCopy.y - position3.y), (float) (positionResult.z - positionCopy.z - position3.z)).color(intValueValue, intValueResult, intValueItem, intValueEntry);
            buffer.vertex(matrix, (float) (positionResult.x + positionCopy.x - position3.x), (float) (positionResult.y + positionCopy.y - position3.y), (float) (positionResult.z + positionCopy.z - position3.z)).color(intValueValue, intValueResult, intValueItem, intValueEntry);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void renderBeginWithWithGlobalProgram(Matrix4f matrix, Vec3d position, int intValue) {
        double doubleValue = this.pointSize.get();
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        int intValueEntry = Math.round((intValue >> 24 & 0xFF) * 0.9F);
        byte byteValue = 16;
        byte byteValueValue = 24;
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        for (int index = 0; index < byteValue; index++) {
            double doubleValueValue = Math.PI * index / byteValue;
            double doubleValueResult = Math.PI * (index + 1) / byteValue;
            for (int indexValue = 0; indexValue < byteValueValue; indexValue++) {
                double doubleValueItem = (Math.PI * 2) * indexValue / byteValueValue;
                double doubleValueEntry = (Math.PI * 2) * (indexValue + 1) / byteValueValue;
                Vec3d positionValue = this.createPositionFromSinWithCos(position, doubleValue, doubleValueValue, doubleValueItem);
                Vec3d positionResult = this.createPositionFromSinWithCos(position, doubleValue, doubleValueResult, doubleValueItem);
                Vec3d positionItem = this.createPositionFromSinWithCos(position, doubleValue, doubleValueResult, doubleValueEntry);
                Vec3d positionEntry = this.createPositionFromSinWithCos(position, doubleValue, doubleValueValue, doubleValueEntry);
                this.renderColorWithVertex(buffer, matrix, positionValue, intValueValue, intValueResult, intValueItem, intValueEntry);
                this.renderColorWithVertex(buffer, matrix, positionResult, intValueValue, intValueResult, intValueItem, intValueEntry);
                this.renderColorWithVertex(buffer, matrix, positionItem, intValueValue, intValueResult, intValueItem, intValueEntry);
                this.renderColorWithVertex(buffer, matrix, positionValue, intValueValue, intValueResult, intValueItem, intValueEntry);
                this.renderColorWithVertex(buffer, matrix, positionItem, intValueValue, intValueResult, intValueItem, intValueEntry);
                this.renderColorWithVertex(buffer, matrix, positionEntry, intValueValue, intValueResult, intValueItem, intValueEntry);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private Vec3d createPositionFromSinWithCos(Vec3d position, double doubleValue, double doubleValueValue, double doubleValueResult) {
        double doubleValueItem = doubleValue * Math.sin(doubleValueValue) * Math.cos(doubleValueResult);
        double doubleValueEntry = doubleValue * Math.cos(doubleValueValue);
        double doubleValueCopy = doubleValue * Math.sin(doubleValueValue) * Math.sin(doubleValueResult);
        return position.add(doubleValueItem, doubleValueEntry, doubleValueCopy);
    }

    private void renderColorWithVertex(BufferBuilder buffer, Matrix4f matrix, Vec3d position, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        buffer.vertex(matrix, (float) position.x, (float) position.y, (float) position.z).color(intValue, intValueValue, intValueResult, intValueItem);
    }

    private void renderBoundingBoxWithBegin(Matrix4f matrix, Entity entity, int intValue) {
        Box boundingBox = entity.getBoundingBox();
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        int intValueEntry = intValue >> 24 & 0xFF;
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        this.updateBufferBuilder(buffer, matrix, boundingBox, intValueValue, intValueResult, intValueItem, intValueEntry);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void updateBufferBuilder(BufferBuilder buffer, Matrix4f matrix, Box box, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        this.renderColorWithVertex(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, intValue, intValueValue, intValueResult, intValueItem);
        this.renderColorWithVertex(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, intValue, intValueValue, intValueResult, intValueItem);
    }

    private void renderColorWithVertex(BufferBuilder buffer, Matrix4f matrix, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        buffer.vertex(matrix, floatValue, floatValueValue, floatValueResult).color(intValue, intValueValue, intValueResult, intValueItem);
        buffer.vertex(matrix, floatValueItem, floatValueEntry, floatValueCopy).color(intValue, intValueValue, intValueResult, intValueItem);
    }

    private void renderColorWithVertex(MatrixStack matrices, Vec3d position, int intValue) {
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        byte byteValue = 45;
        short shortValue = 200;
        float floatValue = 2.5F;
        float floatValueValue = 0.08F;
        byte byteValueValue = 64;
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        matrices.push();
        matrices.translate(position.x, position.y + 0.012, position.z);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, 0.0F, 0.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, byteValue);
        for (int index = 0; index <= byteValueValue; index++) {
            double doubleValue = (Math.PI * 2) * index / byteValueValue;
            float floatValueResult = (float) (Math.cos(doubleValue) * floatValue);
            float floatValueItem = (float) (Math.sin(doubleValue) * floatValue);
            buffer.vertex(positionMatrix, floatValueResult, 0.0F, floatValueItem).color(intValueValue, intValueResult, intValueItem, byteValue);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        float floatValueEntry = floatValue - floatValueValue / 2.0F;
        float floatValueCopy = floatValue + floatValueValue / 2.0F;
        BufferBuilder bufferValue = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int indexValue = 0; indexValue <= byteValueValue; indexValue++) {
            double doubleValueValue = (Math.PI * 2) * indexValue / byteValueValue;
            float floatValue2 = (float) Math.cos(doubleValueValue);
            float floatValue3 = (float) Math.sin(doubleValueValue);
            bufferValue.vertex(positionMatrix, floatValue2 * floatValueEntry, 0.0F, floatValue3 * floatValueEntry).color(intValueValue, intValueResult, intValueItem, shortValue);
            bufferValue.vertex(positionMatrix, floatValue2 * floatValueCopy, 0.0F, floatValue3 * floatValueCopy).color(intValueValue, intValueResult, intValueItem, shortValue);
        }
        BufferRenderer.drawWithGlobalProgram(bufferValue.end());
        matrices.pop();
        RenderSystem.enableCull();
    }

    public static record EntityCollision(Entity entity, Vec3d hitPos) {
    }

    public static record TraceResult(Vec3d[] points, BlockHitResult blockHit, Vec3d hitPos, Entity entityHit, Vec3d entityHitPos) {
    }

    public static record Physics(double velocity, double gravity, double drag) {
    }
}
