package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Простой ESP по сущностям: 3D бокс вокруг мобов и игроков.
 * Полностью независим от ShaderESP - это отдельный модуль со своими настройками.
 * Заливка бокса умеет рисоваться обычным цветом или шейдерами клиента.
 */
public final class ESP extends Module {

    /** Допуск, в пределах которого два бокса считаются одним и тем же объектом. */
    private static final float DUPLICATE_RANGE = 0.4F;

    private static final float DUPLICATE_VERTICAL_GAP = 0.8F;

    private final MultiSetting targets = new MultiSetting("Цели", new BooleanSetting("Игроки", true), new BooleanSetting("Мобы", true));

    private final FloatSetting distance = new FloatSetting("Дистанция", 64.0F, 1.0F, 120.0F, 1.0F);

    private final BooleanSetting box = new BooleanSetting("Бокс", true);

    private final FloatSetting lineWidth = new FloatSetting("Ширина бокса", 1.5F, 0.5F, 4.0F, 0.1F);

    private final ColorSetting boxColor = new ColorSetting("Цвет бокса", 0xFFFF5555);

    private final BooleanSetting filledBox = new BooleanSetting("Бокс с заливкой", false);

    private final ModeSetting fillMode = new ModeSetting("Тип заливки", "Обычная", "Обычная", "Волна", "Туман", "Каустика", "Закат", "Туманность");

    private final ColorSetting fillColor = new ColorSetting("Цвет заливки", 0xFFFF5555);

    private final FloatSetting fillTransparency = new FloatSetting("Прозрачность заливки", 0.35F, 0.0F, 1.0F, 0.01F);

    private final FloatSetting shaderSpeed = new FloatSetting("Скорость шейдера", 1.2F, 0.1F, 5.0F, 0.1F);

    private final BooleanSetting throughWalls = new BooleanSetting("Через стены", true);

    private final List<float[]> boxes = new ArrayList<>();

    public ESP() {
        super("ESP", "Простой ESP по мобам и игрокам", ModuleCategory.RENDER);
        this.lineWidth.visible(() -> this.box.isState());
        this.boxColor.visible(() -> this.box.isState());
        this.fillMode.visible(() -> this.filledBox.isState());
        this.fillColor.visible(() -> this.filledBox.isState());
        this.fillTransparency.visible(() -> this.filledBox.isState());
        this.shaderSpeed.visible(() -> this.filledBox.isState() && !this.fillMode.is("Обычная"));
        this.addSettings(this.targets, this.distance, this.box, this.lineWidth, this.boxColor, this.filledBox, this.fillMode, this.fillColor, this.fillTransparency, this.shaderSpeed, this.throughWalls);
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

        if (drawFill) {
            this.renderFill(matrix, cameraPos);
        }

        if (drawBox) {
            int color = this.boxColor.getColor();
            float alpha = (color >>> 24) / 255.0F;
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
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

    /** Заливка бокса: обычным цветом или одним из шейдеров клиента. */
    private void renderFill(Matrix4f matrix, Vec3d cameraPos) {
        int color = this.fillColor.getColor();
        float alpha = (color >>> 24) / 255.0F * MathHelper.clamp(this.fillTransparency.get(), 0.0F, 1.0F);
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        boolean shaderFill = !this.fillMode.is("Обычная");
        ShaderProgram program = null;
        if (shaderFill) {
            program = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.espFill);
            if (program != null) {
                this.updateUniform(program, "time", (float) (System.currentTimeMillis() % 100000L) / 1000.0F);
                this.updateUniform(program, "speed", this.shaderSpeed.get());
                this.updateUniform(program, "intensity", 1.0F);
                this.updateUniform(program, "mode", this.getShaderMode());
                this.updateUniform(program, "cameraPos", (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
                RenderSystem.setShader(ShaderPrograms.espFill);
            } else {
                shaderFill = false;
            }
        }
        if (!shaderFill) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (float[] box : this.boxes) {
            this.appendFilledBox(buffer, matrix, box, red, green, blue, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
    }

    /** Номер режима шейдера заливки. */
    private float getShaderMode() {
        return switch (this.fillMode.getCurrent()) {
            case "Туман" -> 1.0F;
            case "Каустика" -> 2.0F;
            case "Закат" -> 3.0F;
            case "Туманность" -> 4.0F;
            default -> 0.0F;
        };
    }

    private void updateUniform(ShaderProgram shaderProgram, String text, float floatValue) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue);
        }
    }

    private void updateUniform(ShaderProgram shaderProgram, String text, float floatValue, float floatValueValue, float floatValueResult) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue, floatValueResult);
        }
    }

    /**
     * Собирает габариты всех подходящих сущностей с учётом интерполяции между тиками.
     *
     * Серверные кликабельные NPC часто спавнятся вместе с маркерной сущностью в той же
     * точке - из-за этого бокс рисуется дважды. Поэтому одинаковые по позиции боксы
     * отбрасываются, а маркерные сущности проверяются в последнюю очередь.
     */
    private void collectBoxes(float tickDelta) {
        this.boxes.clear();
        double maxDistance = this.distance.get();
        double maxDistanceSquared = maxDistance * maxDistance;
        boolean showPlayers = this.targets.is("Игроки");
        boolean showMobs = this.targets.is("Мобы");
        if (!showPlayers && !showMobs) {
            return;
        }
        this.addBoxes(mc.world.getEntities(), tickDelta, showPlayers, showMobs, maxDistanceSquared, false);
        this.addBoxes(mc.world.getEntities(), tickDelta, showPlayers, showMobs, maxDistanceSquared, true);
    }

    private void addBoxes(Iterable<Entity> entities, float tickDelta, boolean showPlayers, boolean showMobs, double maxDistanceSquared, boolean markers) {
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player || entity.isRemoved()) {
                continue;
            }
            boolean marker = entity instanceof ArmorStandEntity;
            if (marker != markers) {
                continue;
            }
            boolean player = entity instanceof PlayerEntity;
            if (player && !showPlayers) {
                continue;
            }
            if (!player && !showMobs) {
                continue;
            }
            if (!living.isAlive() && !player) {
                continue;
            }
            if (entity.squaredDistanceTo(mc.player) > maxDistanceSquared) {
                continue;
            }
            Box box = entity.getBoundingBox();
            double offsetX = MathHelper.lerp(tickDelta, entity.prevX, entity.getX()) - entity.getX();
            double offsetY = MathHelper.lerp(tickDelta, entity.prevY, entity.getY()) - entity.getY();
            double offsetZ = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()) - entity.getZ();
            float[] bounds = new float[] {
                    (float) (box.minX + offsetX),
                    (float) (box.minY + offsetY),
                    (float) (box.minZ + offsetZ),
                    (float) (box.maxX + offsetX),
                    (float) (box.maxY + offsetY),
                    (float) (box.maxZ + offsetZ)
            };
            if (this.isDuplicate(bounds)) {
                continue;
            }
            this.boxes.add(bounds);
        }
    }

    /** Проверяет, не совпадает ли бокс с уже добавленным (NPC + маркер в одной точке). */
    private boolean isDuplicate(float[] candidate) {
        float centerX = (candidate[0] + candidate[3]) * 0.5F;
        float centerZ = (candidate[2] + candidate[5]) * 0.5F;
        for (float[] other : this.boxes) {
            float otherX = (other[0] + other[3]) * 0.5F;
            float otherZ = (other[2] + other[5]) * 0.5F;
            if (Math.abs(centerX - otherX) > DUPLICATE_RANGE || Math.abs(centerZ - otherZ) > DUPLICATE_RANGE) {
                continue;
            }
            float gap = Math.max(0.0F, Math.max(candidate[1] - other[4], other[1] - candidate[4]));
            if (gap <= DUPLICATE_VERTICAL_GAP) {
                return true;
            }
        }
        return false;
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
