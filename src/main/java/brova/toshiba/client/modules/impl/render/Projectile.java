package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.Projectile.FlightPoint;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.Identifier;
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
import org.joml.Vector4f;

public final class Projectile extends Module {

    private final TextRenderer textRenderer = FontManager.getFont("sf_regular", 14);

    public static Projectile INSTANCE = new Projectile();

    private static final Identifier bloomTexture = Identifier.of("toshiba", "featrues/particle/bloom.png");

    private final FloatSetting size = new FloatSetting("Размер", 1.2F, 0.6F, 2.4F, 0.1F);

    private final List<FlightPoint> projectileFlightPointEntries = new ArrayList<>();

    private final Matrix4f matrix4f = new Matrix4f();

    private final Quaternionf quaternionf = new Quaternionf();

    private Vec3d pos = Vec3d.ZERO;

    private boolean stateFlag;

    public Projectile() {
        super("Projectile", "Траектория эндер-жемчуга.", ModuleCategory.RENDER);
        this.addSettings(this.size);
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (mc.world != null && mc.player != null) {
            this.projectileFlightPointEntries.clear();
            this.stateFlag = true;
            this.matrix4f.set(event.getProjectionMatrix());
            this.pos = event.getCamera().getPos();
            this.quaternionf.set(event.getCamera().getRotation());
            MatrixStack matrices = event.getMatrices();
            Camera camera = event.getCamera();
            Vec3d pos = camera.getPos();
            Quaternionf rotation = camera.getRotation();
            float tickDelta = event.getTickDelta();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, bloomTexture);
            Box box = mc.player.getBoundingBox().expand(128.0);
            for (EnderPearlEntity enderPearlEntity : mc.world.getEntitiesByClass(EnderPearlEntity.class, box, Entity::isAlive)) {
                var values = this.createListFromLerpWithVelocity(enderPearlEntity, tickDelta);
                if (values.size() >= 2) {
                    float floatValue = (values.size() - 1) / 20.0F;
                    Vec3d position = values.get(values.size() - 1);
                    this.projectileFlightPointEntries.add(new FlightPoint(position, floatValue));
                    float floatValueValue = this.size.get() * 0.2F;
                    int intValue = ColorMathUtil.setAlphaColor(ColorMathUtil.getThemeColor(), 40);
                    int intValueValue = intValue >> 16 & 0xFF;
                    int intValueResult = intValue >> 8 & 0xFF;
                    int intValueItem = intValue & 0xFF;
                    int intValueEntry = intValue >> 24 & 0xFF;
                    matrices.push();
                    matrices.translate(-pos.x, -pos.y, -pos.z);
                    BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    for (int index = 0; index < values.size() - 1; index++) {
                        Vec3d positionValue = values.get(index);
                        Vec3d positionResult = values.get(index + 1);
                        int intValueCopy = Math.max(2, Math.min(12, (int) Math.ceil(positionValue.distanceTo(positionResult) / Math.max(floatValueValue * 1.75F, 0.08F))));
                        for (int indexValue = 0; indexValue < intValueCopy; indexValue++) {
                            Vec3d interpolatedValue = positionValue.lerp(positionResult, (double) indexValue / intValueCopy);
                            matrices.push();
                            matrices.translate(interpolatedValue.x, interpolatedValue.y, interpolatedValue.z);
                            matrices.multiply(rotation);
                            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                            buffer.vertex(positionMatrix, -floatValueValue, -floatValueValue, 0.0F).texture(0.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
                            buffer.vertex(positionMatrix, -floatValueValue, floatValueValue, 0.0F).texture(0.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
                            buffer.vertex(positionMatrix, floatValueValue, floatValueValue, 0.0F).texture(1.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
                            buffer.vertex(positionMatrix, floatValueValue, -floatValueValue, 0.0F).texture(1.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, intValueEntry);
                            matrices.pop();
                        }
                    }
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    matrices.pop();
                    float floatValueResult = floatValueValue * 1.6F;
                    int intValue2 = ColorMathUtil.setAlphaColor(ColorMathUtil.getThemeColor(), 170);
                    int intValue3 = intValue2 >> 16 & 0xFF;
                    int intValue4 = intValue2 >> 8 & 0xFF;
                    int intValue5 = intValue2 & 0xFF;
                    int intValue6 = intValue2 >> 24 & 0xFF;
                    float floatValueItem = (float) (position.x - pos.x);
                    float floatValueEntry = (float) (position.y - pos.y + 0.03F);
                    float floatValueCopy = (float) (position.z - pos.z);
                    matrices.push();
                    matrices.translate(floatValueItem, floatValueEntry, floatValueCopy);
                    matrices.multiply(rotation);
                    Matrix4f positionMatrixValue = matrices.peek().getPositionMatrix();
                    BufferBuilder bufferValue = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    bufferValue.vertex(positionMatrixValue, -floatValueResult, -floatValueResult, 0.0F).texture(0.0F, 0.0F).color(intValue3, intValue4, intValue5, intValue6);
                    bufferValue.vertex(positionMatrixValue, -floatValueResult, floatValueResult, 0.0F).texture(0.0F, 1.0F).color(intValue3, intValue4, intValue5, intValue6);
                    bufferValue.vertex(positionMatrixValue, floatValueResult, floatValueResult, 0.0F).texture(1.0F, 1.0F).color(intValue3, intValue4, intValue5, intValue6);
                    bufferValue.vertex(positionMatrixValue, floatValueResult, -floatValueResult, 0.0F).texture(1.0F, 0.0F).color(intValue3, intValue4, intValue5, intValue6);
                    BufferRenderer.drawWithGlobalProgram(bufferValue.end());
                    matrices.pop();
                }
            }
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    @EventHandler
    public void onRender2D(HudRenderEvent event) {
        if (this.stateFlag && !this.projectileFlightPointEntries.isEmpty() && mc.player != null) {
            MatrixStack matrices = event.getContext().getMatrices();
            TextRenderer textRenderer = this.textRenderer;
            if (textRenderer != null) {
                int themeColor = ColorMathUtil.getThemeColor();
                for (FlightPoint flightPoint : this.projectileFlightPointEntries) {
                    Vec3d position = this.createScaledWidthWithScaledHeight(flightPoint.pos());
                    if (position != null) {
                        String text = this.createTextFromFloat(flightPoint.seconds()) + " сек";
                        float stringWidth = textRenderer.getStringWidth(text);
                        float floatValue = stringWidth + 10.0F;
                        float floatValueValue = 12.5F;
                        float floatValueResult = (float) position.x - floatValue / 2.0F;
                        float floatValueItem = (float) position.y - 6.0F;
                        Renderer2D.drawDefaultHudThemedPanel(matrices, floatValueResult, floatValueItem, floatValue, floatValueValue, 3.0F, 3.5F, themeColor);
                        textRenderer.drawString(matrices, text, floatValueResult + 5.5F, floatValueItem + 4.55F, -1);
                    }
                }
            }
        }
    }

    private Vec3d createScaledWidthWithScaledHeight(Vec3d position) {
        if (mc != null && mc.getWindow() != null) {
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
        } else {
            return null;
        }
    }

    private String createTextFromFloat(float floatValue) {
        int intValue = Math.round(floatValue * 10.0F);
        return intValue / 10 + "." + Math.abs(intValue % 10);
    }

    private List<Vec3d> createListFromLerpWithVelocity(EnderPearlEntity enderPearlEntity, float floatValue) {
        ArrayList<net.minecraft.util.math.Vec3d> values = new ArrayList<>();
        Vec3d position = new Vec3d(MathHelper.lerp((double) floatValue, enderPearlEntity.prevX, enderPearlEntity.getX()), MathHelper.lerp((double) floatValue, enderPearlEntity.prevY, enderPearlEntity.getY()), MathHelper.lerp((double) floatValue, enderPearlEntity.prevZ, enderPearlEntity.getZ()));
        Vec3d velocity = enderPearlEntity.getVelocity();
        values.add(position);
        for (int index = 0; index < 300; index++) {
            Vec3d positionValue = position.add(velocity);
            BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(position, positionValue, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            if (blockHitResult.getType() == Type.BLOCK) {
                values.add(blockHitResult.getPos());
                break;
            }
            values.add(positionValue);
            position = positionValue;
            boolean of = mc.world.getBlockState(BlockPos.ofFloored(positionValue)).isOf(Blocks.WATER);
            double doubleValue = of ? 0.8 : 0.99;
            velocity = velocity.multiply(doubleValue).subtract(0.0, 0.03, 0.0);
            if (positionValue.y <= mc.world.getBottomY()) {
                break;
            }
        }
        return values;
    }

    public static record FlightPoint(Vec3d pos, float seconds) {
    }
}
