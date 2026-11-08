package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.Predictions.LandingMarker;
import brova.toshiba.client.modules.impl.render.Predictions.PathSegment;
import brova.toshiba.client.modules.impl.render.Predictions.ProjectilePath;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class Predictions extends Module {

    public static Predictions INSTANCE = new Predictions();

    private final List<LandingMarker> predictionsLandingMarkerEntries = new ArrayList<>();

    private final List<Entity> removeIfEntityEntries = new ArrayList<>();

    private final List<Box> boxEntries = new ArrayList<>();

    private final Map<Integer, ProjectilePath> keySetPredictionsProjectilePathEntries = new HashMap<>();

    private final Matrix4f matrix4f = new Matrix4f();

    private final Quaternionf quaternionf = new Quaternionf();

    private Vec3d pos = Vec3d.ZERO;

    private boolean stateFlag = false;

    public Predictions() {
        super("Predictions", "Показывает куда упадет предмет", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.clearPredictionState();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.clearPredictionState();
    }

    private void clearPredictionState() {
        this.keySetPredictionsProjectilePathEntries.clear();
        this.removeIfEntityEntries.clear();
        this.boxEntries.clear();
        this.predictionsLandingMarkerEntries.clear();
        this.stateFlag = false;
    }

    @EventHandler
    public void onDraw(HudRenderEvent event) {
        if (this.isEnable() && this.stateFlag && !this.predictionsLandingMarkerEntries.isEmpty() && mc.player != null) {
            DrawContext context = event.getContext();
            MatrixStack matrices = context.getMatrices();
            TextRenderer font = FontManager.getFont("manrope", 14);
            for (LandingMarker landingMarker : this.predictionsLandingMarkerEntries) {
                Vec3d position = this.createScaledWidthWithScaledHeight(landingMarker.pos());
                if (position != null) {
                    int intValue = landingMarker.ticks();
                    String text = formatTextFromFormat(intValue) + " сек";
                    float floatValue = font != null ? font.getStringWidth(text) : mc.textRenderer.getWidth(text);
                    float x = (float) position.getX();
                    float y = (float) position.getY();
                    float floatValueValue = 11.0F;
                    float floatValueResult = x - floatValue / 2.0F;
                    float floatValueItem = y - floatValueValue / 2.0F;
                    Renderer2D.drawRoundedRect(matrices, floatValueResult - 5.0F, floatValueItem, floatValue + 12.0F, floatValueValue, 3.0F, ColorMathUtil.rgba(0, 0, 0, 166));
                    if (landingMarker.stack() != null && !landingMarker.stack().isEmpty()) {
                        matrices.push();
                        matrices.translate(floatValueResult - 3.5F, floatValueItem + 1.2F, 0.0F);
                        matrices.scale(0.5F, 0.5F, 1.0F);
                        context.drawItem(landingMarker.stack(), 0, 0);
                        matrices.pop();
                    }
                    String textValue = text.replace(",", ".");
                    if (font != null) {
                        font.drawString(matrices, textValue, floatValueResult + 5.0F, floatValueItem + 2.8F, -1);
                    } else {
                        context.drawText(mc.textRenderer, textValue, (int) (floatValueResult + 5.0F), (int) (floatValueItem + 2.8F), -1, true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (this.isEnable() && mc.world != null && mc.player != null) {
            this.matrix4f.set(event.getProjectionMatrix());
            this.pos = event.getCamera().getPos();
            this.quaternionf.set(event.getCamera().getRotation());
            this.stateFlag = true;
            this.predictionsLandingMarkerEntries.clear();
            this.collectProjectiles();
            if (this.removeIfEntityEntries.isEmpty()) {
                if (!this.keySetPredictionsProjectilePathEntries.isEmpty()) {
                    this.keySetPredictionsProjectilePathEntries.clear();
                }
            } else {
                if (!this.keySetPredictionsProjectilePathEntries.isEmpty()) {
                    this.keySetPredictionsProjectilePathEntries.keySet().removeIf(input -> {
                        for (Entity entity : this.removeIfEntityEntries) {
                            if (entity.getId() == input) {
                                return false;
                            }
                        }
                        return true;
                    });
                }
                boolean flag = false;
                Vec3d pos = event.getCamera().getPos();
                MatrixStack matrices = event.getMatrices();
                for (Entity entityValue : this.removeIfEntityEntries) {
                    Vec3d posValue = entityValue.getPos();
                    Vec3d velocity = entityValue.getVelocity();
                    ProjectilePath projectilePath = this.keySetPredictionsProjectilePathEntries.get(entityValue.getId());
                    if (projectilePath == null || !projectilePath.startPosition.equals(posValue) || !projectilePath.endPosition.equals(velocity)) {
                        if (!flag) {
                            this.clearFrameTrajectories();
                            flag = true;
                        }
                        projectilePath = this.createPredictionsProjectilePathFromSquaredDistanceToWithRaycast(entityValue, posValue, velocity);
                        this.keySetPredictionsProjectilePathEntries.put(entityValue.getId(), projectilePath);
                    }
                    int themeColor = ColorMathUtil.getThemeColor();
                    for (PathSegment pathSegment : projectilePath.predictionsPathSegmentEntries) {
                        int intValue = pathSegment.index();
                        float clampedValue = MathHelper.clamp(intValue / 25.0F, 0.0F, 1.0F);
                        int intValueValue = calculateInteger(themeColor, clampedValue);
                        renderDepthMaskWithColor(matrices, pathSegment.from(), pathSegment.to(), intValueValue, 2.0F, pos);
                        float clampedValueValue = MathHelper.clamp(intValue / 50.0F, 0.0F, 0.3F);
                        int intValueResult = calculateInteger(themeColor, clampedValueValue);
                        renderDepthMaskWithColor(matrices, pathSegment.from(), pathSegment.to(), intValueResult, 4.5F, pos);
                        renderDepthMaskWithColor(matrices, pathSegment.from(), pathSegment.to(), intValueResult, 7.0F, pos);
                    }
                    if (projectilePath.predictionsLandingMarker != null) {
                        this.predictionsLandingMarkerEntries.add(projectilePath.predictionsLandingMarker);
                    }
                }
            }
        }
    }

    private ProjectilePath createPredictionsProjectilePathFromSquaredDistanceToWithRaycast(Entity entity, Vec3d position, Vec3d positionValue) {
        ProjectilePath projectilePath = new ProjectilePath(position, positionValue);
        Vec3d positionResult = position;
        Vec3d positionItem = positionValue;
        ItemStack stack = this.createStack(entity);
        for (int index = 0; index < 150; index++) {
            Vec3d positionEntry = positionResult.add(positionItem);
            BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(positionResult, positionEntry, ShapeType.COLLIDER, FluidHandling.NONE, entity));
            EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, positionResult, positionEntry, entity.getBoundingBox().stretch(positionItem).expand(1.0), input -> this.isValidTarget(entity, input), positionResult.squaredDistanceTo(positionEntry));
            if (entityHitResult != null && (blockHitResult.getType() == Type.MISS || positionResult.squaredDistanceTo(entityHitResult.getPos()) < positionResult.squaredDistanceTo(blockHitResult.getPos()))) {
                positionEntry = entityHitResult.getPos();
                projectilePath.predictionsPathSegmentEntries.add(new PathSegment(positionResult, positionEntry, index));
                projectilePath.predictionsLandingMarker = new LandingMarker(stack, positionEntry, index);
                break;
            }
            if (blockHitResult.getType() != Type.MISS) {
                positionEntry = blockHitResult.getPos();
                projectilePath.predictionsPathSegmentEntries.add(new PathSegment(positionResult, positionEntry, index));
                projectilePath.predictionsLandingMarker = new LandingMarker(stack, positionEntry, index);
                break;
            }
            projectilePath.predictionsPathSegmentEntries.add(new PathSegment(positionResult, positionEntry, index));
            positionResult = positionEntry;
            positionItem = this.calculateMotion(entity, positionEntry, positionItem);
        }
        return projectilePath;
    }

    private ItemStack createStack(Entity entity) {
        if (entity instanceof EnderPearlEntity) {
            return new ItemStack(Items.ENDER_PEARL);
        } else if (entity instanceof ThrownItemEntity thrownItemEntity) {
            return thrownItemEntity.getStack();
        } else if (entity instanceof TridentEntity) {
            return new ItemStack(Items.TRIDENT);
        } else {
            return entity instanceof PersistentProjectileEntity ? new ItemStack(Items.ARROW) : new ItemStack(Items.ENDER_PEARL);
        }
    }

    private void collectProjectiles() {
        this.removeIfEntityEntries.clear();
        if (mc.world != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (this.isAlive(entity)) {
                    this.removeIfEntityEntries.add(entity);
                }
            }
        }
    }

    private void clearFrameTrajectories() {
        this.boxEntries.clear();
    }

    public Vec3d calculateMotion(Entity entity, Vec3d position, Vec3d positionValue) {
        double projectileGravity = this.getProjectileGravity(entity);
        double doubleValue = entity.isSubmergedInWater() ? this.getWaterDrag(entity) : this.getAirDrag(entity);
        double doubleValueValue = positionValue.y * doubleValue;
        if (!entity.hasNoGravity()) {
            doubleValueValue -= projectileGravity;
        }
        return new Vec3d(positionValue.x * doubleValue, doubleValueValue, positionValue.z * doubleValue);
    }

    private double getProjectileGravity(Entity entity) {
        if (entity instanceof EnderPearlEntity) {
            return 0.03;
        } else if (entity instanceof TridentEntity) {
            return 0.05;
        } else if (entity instanceof PersistentProjectileEntity) {
            return 0.05;
        } else if (entity instanceof ExperienceBottleEntity) {
            return 0.07;
        } else {
            return entity instanceof PotionEntity ? 0.05 : 0.03;
        }
    }

    private double getAirDrag(Entity entity) {
        return 0.99;
    }

    private double getWaterDrag(Entity entity) {
        return entity instanceof PersistentProjectileEntity ? 0.6 : 0.8;
    }

    private boolean isValidTarget(Entity entity, Entity entityValue) {
        return entityValue.isAlive() && !entityValue.isSpectator() && entityValue != entity ? !(entity instanceof ProjectileEntity projectileEntity && entityValue == projectileEntity.getOwner()) : false;
    }

    private boolean isAlive(Entity entity) {
        return entity != null && entity.isAlive() ? entity instanceof EnderPearlEntity || entity instanceof PersistentProjectileEntity || entity instanceof ThrownItemEntity : false;
    }

    private static String formatTextFromFormat(int intValue) {
        float floatValue = intValue / 20.0F;
        return String.format(Locale.US, "%.1f", floatValue);
    }

    private Vec3d createScaledWidthWithScaledHeight(Vec3d position) {
        if (mc.getWindow() == null) {
            return null;
        } else {
            Vector3f vector3f = new Vector3f((float) (position.x - this.pos.x), (float) (position.y - this.pos.y), (float) (position.z - this.pos.z));
            Quaternionf quaternion = new Quaternionf(this.quaternionf).conjugate();
            vector3f.rotate(quaternion);
            Vector4f vector4f = new Vector4f(vector3f.x, vector3f.y, vector3f.z, 1.0F);
            this.matrix4f.transform(vector4f);
            float w = vector4f.w;
            if (w <= 1.0E-5F) {
                return null;
            } else {
                float floatValue = vector4f.x / w;
                float floatValueValue = vector4f.y / w;
                float floatValueResult = vector4f.z / w;
                float floatValueItem = (floatValue * 0.5F + 0.5F) * mc.getWindow().getScaledWidth();
                float floatValueEntry = (1.0F - (floatValueValue * 0.5F + 0.5F)) * mc.getWindow().getScaledHeight();
                if (!Float.isNaN(floatValueItem) && !Float.isNaN(floatValueEntry) && !Float.isInfinite(floatValueItem) && !Float.isInfinite(floatValueEntry)) {
                    return !(floatValueItem < -400.0F) && !(floatValueEntry < -400.0F) && !(floatValueItem > mc.getWindow().getScaledWidth() + 400) && !(floatValueEntry > mc.getWindow().getScaledHeight() + 400) ? new Vec3d(floatValueItem, floatValueEntry, floatValueResult) : null;
                } else {
                    return null;
                }
            }
        }
    }

    private static int calculateInteger(int intValue, float floatValue) {
        int intValueValue = Math.round((intValue >> 24 & 0xFF) * floatValue);
        intValueValue = MathHelper.clamp(intValueValue, 0, 255);
        return intValue & 16777215 | intValueValue << 24;
    }

    private static void renderDepthMaskWithColor(MatrixStack matrices, Vec3d position, Vec3d positionValue, int intValue, float floatValue, Vec3d positionResult) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(floatValue);
        Tessellator instance = Tessellator.getInstance();
        BufferBuilder buffer = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        float floatValueValue = (float) (position.x - positionResult.x);
        float floatValueResult = (float) (position.y - positionResult.y);
        float floatValueItem = (float) (position.z - positionResult.z);
        float floatValueEntry = (float) (positionValue.x - positionResult.x);
        float floatValueCopy = (float) (positionValue.y - positionResult.y);
        float floatValue2 = (float) (positionValue.z - positionResult.z);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        buffer.vertex(positionMatrix, floatValueValue, floatValueResult, floatValueItem).color(intValue);
        buffer.vertex(positionMatrix, floatValueEntry, floatValueCopy, floatValue2).color(intValue);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public final static class ProjectilePath {

        public final Vec3d startPosition;

        public final Vec3d endPosition;

        public final List<PathSegment> predictionsPathSegmentEntries = new ArrayList<>();

        public LandingMarker predictionsLandingMarker;

        public ProjectilePath(Vec3d startPosition, Vec3d endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    public static record LandingMarker(ItemStack stack, Vec3d pos, int ticks) {
    }

    public static record PathSegment(Vec3d from, Vec3d to, int index) {
    }
}
