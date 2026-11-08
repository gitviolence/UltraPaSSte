package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.FireworkESP.FireworkTrail;
import brova.toshiba.client.modules.impl.render.FireworkESP.TrailPoint;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class FireworkESP extends Module {

    public static FireworkESP INSTANCE = new FireworkESP();

    private final Matrix4f matrix4f = new Matrix4f();

    private final Quaternionf quaternionf = new Quaternionf();

    private Vec3d pos = Vec3d.ZERO;

    private float tickDelta;

    private final Map<Integer, FireworkTrail> entrySetFireworkESPFireworkTrailEntries = new HashMap<>();

    public FireworkESP() {
        super("FireworkESP", "Помогает понять использует ли противник фейерверк и сколько он уже длиться.", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.entrySetFireworkESPFireworkTrailEntries.clear();
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        this.matrix4f.set(event.getProjectionMatrix());
        this.pos = event.getCamera().getPos();
        this.quaternionf.set(event.getCamera().getRotation());
        this.tickDelta = event.getTickDelta();
        if (mc.world != null) {
            long lastUpdateTimestamp = System.currentTimeMillis();
            this.entrySetFireworkESPFireworkTrailEntries.entrySet().removeIf(input -> {
                Entity entityById = mc.world.getEntityById(input.getKey());
                boolean flag = entityById == null || !entityById.isAlive();
                input.getValue().removeIfFireworkESPTrailPointEntries.removeIf(inputValue -> (float) (lastUpdateTimestamp - inputValue.lastUpdateTimestamp) > 1300.0F);
                return flag && input.getValue().removeIfFireworkESPTrailPointEntries.isEmpty();
            });
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof FireworkRocketEntity && entity.isAlive()) {
                    FireworkTrail fireworkTrail = this.entrySetFireworkESPFireworkTrailEntries.computeIfAbsent(entity.getId(), inputResult -> new FireworkTrail());
                    if ((float) (lastUpdateTimestamp - fireworkTrail.lastUpdateTimestamp) >= 150.0F) {
                        Vec3d position = new Vec3d(MathHelper.lerp((double) this.tickDelta, entity.lastRenderX, entity.getX()), MathHelper.lerp((double) this.tickDelta, entity.lastRenderY, entity.getY()) + 0.5, MathHelper.lerp((double) this.tickDelta, entity.lastRenderZ, entity.getZ()));
                        float floatValue = entity.age / 20.0F;
                        fireworkTrail.removeIfFireworkESPTrailPointEntries.add(new TrailPoint(position, lastUpdateTimestamp, floatValue));
                        fireworkTrail.lastUpdateTimestamp = lastUpdateTimestamp;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender2D(HudRenderEvent event) {
        if (mc.player != null && mc.world != null) {
            MatrixStack matrices = event.getContext().getMatrices();
            ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
            TextRenderer font = FontManager.getFont("sf_regular", 14);
            long currentTimeMillis = System.currentTimeMillis();
            for (var entry : this.entrySetFireworkESPFireworkTrailEntries.entrySet()) {
                FireworkTrail value = entry.getValue();
                for (TrailPoint trailPoint : value.removeIfFireworkESPTrailPointEntries) {
                    Vec3d position = this.createPositionFromNaNWithInfinite(trailPoint.vec3d);
                    if (position != null) {
                        float floatValue = 1.0F - (float) (currentTimeMillis - trailPoint.lastUpdateTimestamp) / 1300.0F;
                        floatValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
                        String text = String.format("%.1fs", trailPoint.format);
                        this.onHudRender(event, matrices, font, stack, position, floatValue, text);
                    }
                }
                Entity entityById = mc.world.getEntityById(entry.getKey());
                if (entityById instanceof FireworkRocketEntity && entityById.isAlive()) {
                    Vec3d positionValue = new Vec3d(MathHelper.lerp((double) this.tickDelta, entityById.lastRenderX, entityById.getX()), MathHelper.lerp((double) this.tickDelta, entityById.lastRenderY, entityById.getY()) + 0.5, MathHelper.lerp((double) this.tickDelta, entityById.lastRenderZ, entityById.getZ()));
                    Vec3d positionResult = this.createPositionFromNaNWithInfinite(positionValue);
                    if (positionResult != null) {
                        String textValue = String.format("%.1fs", entityById.age / 20.0F);
                        this.onHudRender(event, matrices, font, stack, positionResult, 1.0F, textValue);
                    }
                }
            }
        }
    }

    private void onHudRender(HudRenderEvent event, MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, Vec3d position, float floatValue, String text) {
        float floatValueValue = 0.6F;
        float floatValueResult = 12.0F;
        float floatValueItem = 2.5F;
        float floatValueEntry = 2.0F;
        float floatValueCopy = 3.5F;
        float floatValue2 = 0.35F + 0.65F * floatValue;
        int intValue = (int) (200.0F * floatValue);
        if (intValue > 5) {
            int intValueValue = intValue << 24 | 16777215;
            float floatValue3 = textRenderer != null ? textRenderer.getStringWidth(text) : 0.0F;
            float floatValue4 = 16.0F * floatValueValue;
            float floatValue5 = floatValueItem + floatValue4 + floatValueEntry + floatValue3 + floatValueItem;
            matrices.push();
            matrices.translate(position.x, position.y, 0.0);
            matrices.scale(floatValue2, floatValue2, 1.0F);
            int intValueResult = (int) (80.0F * floatValue);
            this.renderColorWithVertex(matrices, -floatValue5 / 2.0F, -floatValueResult / 2.0F, floatValue5, floatValueResult, intValueResult);
            float floatValue6 = -floatValue5 / 2.0F + floatValueItem;
            matrices.push();
            matrices.translate(floatValue6, -(16.0F * floatValueValue) / 2.0F, 0.0F);
            matrices.scale(floatValueValue, floatValueValue, 1.0F);
            event.getContext().drawItem(stack, 0, 0);
            matrices.pop();
            floatValue6 += floatValue4 + floatValueEntry;
            if (textRenderer != null) {
                textRenderer.drawString(matrices, text, floatValue6, -floatValueResult / 2.0F + floatValueCopy + 0.5F, intValueValue);
            }
            matrices.pop();
        }
    }

    private void renderColorWithVertex(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        int intValueValue = ColorMathUtil.rgba(0, 0, 0, intValue);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).color(intValueValue);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).color(intValueValue);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(intValueValue);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(intValueValue);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    private Vec3d createPositionFromNaNWithInfinite(Vec3d position) {
        Vector3f vector3f = new Vector3f((float) (position.x - this.pos.x), (float) (position.y - this.pos.y), (float) (position.z - this.pos.z));
        vector3f.rotate(new Quaternionf(this.quaternionf).conjugate());
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
            return !Float.isNaN(floatValueItem) && !Float.isNaN(floatValueEntry) && !Float.isInfinite(floatValueItem) && !Float.isInfinite(floatValueEntry) ? new Vec3d(floatValueItem, floatValueEntry, floatValueResult) : null;
        }
    }

    public static class FireworkTrail {

        public long lastUpdateTimestamp;

        public final List<TrailPoint> removeIfFireworkESPTrailPointEntries = new ArrayList<>();

        FireworkTrail() {
        }
    }

    public static class TrailPoint {

        public final Vec3d vec3d;

        public final long lastUpdateTimestamp;

        public final float format;

        public TrailPoint(Vec3d vec3d, long lastUpdateTimestamp, float format) {
            this.vec3d = vec3d;
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            this.format = format;
        }
    }
}
