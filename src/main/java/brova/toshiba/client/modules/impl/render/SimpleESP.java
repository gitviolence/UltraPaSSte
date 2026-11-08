package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Простой ESP по сущностям: 3D бокс вокруг мобов и игроков.
 * Полностью независим от ShaderESP - это отдельный модуль со своими настройками.
 */
public final class SimpleESP extends Module {

    private final ModeSetting targets = new ModeSetting("Цели", "Игроки и мобы", "Игроки и мобы", "Только игроки", "Только мобы");

    private final FloatSetting distance = new FloatSetting("Дистанция", 64.0F, 1.0F, 120.0F, 1.0F);

    private final BooleanSetting box = new BooleanSetting("Бокс", true);

    private final BooleanSetting filledBox = new BooleanSetting("Бокс с заливкой", false);

    private final FloatSetting lineWidth = new FloatSetting("Ширина бокса", 1.5F, 0.5F, 4.0F, 0.1F);

    private final ColorSetting boxColor = new ColorSetting("Цвет бокса", 0xFFFF5555);

    private final ColorSetting fillColor = new ColorSetting("Цвет заливки", 0xFFFF5555);

    private final FloatSetting fillTransparency = new FloatSetting("Прозрачность заливки", 0.35F, 0.0F, 1.0F, 0.01F);

    private final BooleanSetting throughWalls = new BooleanSetting("Через стены", true);

    private final List<float[]> boxes = new ArrayList<>();

    public SimpleESP() {
        super("SimpleESP", "Простой ESP по мобам и игрокам", ModuleCategory.RENDER);
        this.boxColor.visible(() -> this.box.isState());
        this.lineWidth.visible(() -> this.box.isState());
        this.fillColor.visible(() -> this.filledBox.isState());
        this.fillTransparency.visible(() -> this.filledBox.isState());
        this.addSettings(this.targets, this.distance, this.box, this.filledBox, this.lineWidth, this.boxColor, this.fillColor, this.fillTransparency, this.throughWalls);
    }

    @Override
    public void onDisable() {
        this.boxes.clear();
        super.onDisable();
    }

    @EventHandler(priority = 100)
    public void onRender3D(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        boolean drawBox = this.box.isState();
        boolean drawFill = this.filledBox.isState();
        if (!drawBox && !drawFill) {
            return;
        }
        this.collectBoxes(event.getTickDelta());
        if (this.boxes.isEmpty()) {
            return;
        }
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        MatrixStack matrices = event.getMatrices();
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        if (this.throughWalls.isState()) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        if (drawFill) {
            int color = this.fillColor.getColor();
            float alpha = (color >>> 24) / 255.0F * MathHelper.clamp(this.fillTransparency.get(), 0.0F, 1.0F);
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (float[] box : this.boxes) {
                this.appendFilledBox(buffer, matrix, box, (color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        if (drawBox) {
            int color = this.boxColor.getColor();
            float alpha = (color >>> 24) / 255.0F;
            RenderSystem.lineWidth(this.lineWidth.get());
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (float[] box : this.boxes) {
                this.appendBoxOutline(buffer, matrix, box, (color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    /** Собирает габариты всех подходящих сущностей с учётом интерполяции между тиками. */
    private void collectBoxes(float tickDelta) {
        this.boxes.clear();
        double maxDistance = this.distance.get();
        double maxDistanceSquared = maxDistance * maxDistance;
        boolean onlyPlayers = this.targets.is("Только игроки");
        boolean onlyMobs = this.targets.is("Только мобы");
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player || entity.isRemoved()) {
                continue;
            }
            boolean player = entity instanceof PlayerEntity;
            if (onlyPlayers && !player) {
                continue;
            }
            if (onlyMobs && player) {
                continue;
            }
            if (!living.isAlive() && !player) {
                continue;
            }
            if (entity.squaredDistanceTo(mc.player) > maxDistanceSquared) {
                continue;
            }
            Box box = entity.getBoundingBox();
            double offsetX = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double offsetY = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double offsetZ = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();
            this.boxes.add(new float[] {
                    (float) (box.minX + offsetX),
                    (float) (box.minY + offsetY),
                    (float) (box.minZ + offsetZ),
                    (float) (box.maxX + offsetX),
                    (float) (box.maxY + offsetY),
                    (float) (box.maxZ + offsetZ)
            });
        }
    }

    private void appendFilledBox(BufferBuilder buffer, Matrix4f matrix, float[] box, float red, float green, float blue, float alpha) {
        float x1 = box[0];
        float y1 = box[1];
        float z1 = box[2];
        float x2 = box[3];
        float y2 = box[4];
        float z2 = box[5];
        // передняя и задняя грани
        this.appendQuad(buffer, matrix, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, red, green, blue, alpha);
        this.appendQuad(buffer, matrix, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, red, green, blue, alpha);
        // левая и правая грани
        this.appendQuad(buffer, matrix, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, red, green, blue, alpha);
        this.appendQuad(buffer, matrix, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, red, green, blue, alpha);
        // нижняя и верхняя грани
        this.appendQuad(buffer, matrix, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, red, green, blue, alpha);
        this.appendQuad(buffer, matrix, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
    }

    private void appendQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);
        buffer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha);
        buffer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha);
    }

    private void appendBoxOutline(BufferBuilder buffer, Matrix4f matrix, float[] box, float red, float green, float blue, float alpha) {
        float x1 = box[0];
        float y1 = box[1];
        float z1 = box[2];
        float x2 = box[3];
        float y2 = box[4];
        float z2 = box[5];
        // нижнее кольцо
        this.appendLine(buffer, matrix, x1, y1, z1, x2, y1, z1, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x2, y1, z1, x2, y1, z2, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x1, y1, z2, x1, y1, z1, red, green, blue, alpha);
        // верхнее кольцо
        this.appendLine(buffer, matrix, x1, y2, z1, x2, y2, z1, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x2, y2, z1, x2, y2, z2, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);
        // вертикальные рёбра
        this.appendLine(buffer, matrix, x1, y1, z1, x1, y2, z1, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x2, y1, z1, x2, y2, z1, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x2, y1, z2, x2, y2, z2, red, green, blue, alpha);
        this.appendLine(buffer, matrix, x1, y1, z2, x1, y2, z2, red, green, blue, alpha);
    }

    private void appendLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);
    }
}
