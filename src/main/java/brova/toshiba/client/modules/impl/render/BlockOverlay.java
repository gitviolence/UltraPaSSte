package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.helpertstorages.Theme;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class BlockOverlay extends Module {

    public static BlockOverlay INSTANCE = new BlockOverlay();

    private final ModeSetting shaderMode = new ModeSetting("Режим шейдера", "Fog", "Fog", "Caustic", "Sunset", "Nebula");

    private final FloatSetting outlineWidth = new FloatSetting("Ширина обводки", 1.1F, 0.1F, 5.0F, 0.1F);

    private final FloatSetting shaderSpeed = new FloatSetting("Скорость шейдера", 1.2F, 0.1F, 5.0F, 0.1F);

    private final FloatSetting fill = new FloatSetting("Заливка", 0.6F, 0.0F, 1.0F, 0.01F);

    private final FloatSetting smoothing = new FloatSetting("Плавность", 4.0F, 0.0F, 50.0F, 1.0F);

    private Framebuffer framebuffer;

    private int previousPrimaryColor = -1;

    private int previousSecondaryColor = -1;

    private boolean stateFlag;

    private BlockPos blockPos;

    private Box offsetBox;

    private Box box;

    private int cachedPrimaryColor = -1;

    private int cachedSecondaryColor = -1;

    public BlockOverlay() {
        super("BlockOverlay", "Красивый шейдер на блоки", ModuleCategory.RENDER);
        this.addSettings(this.shaderMode, this.outlineWidth, this.shaderSpeed, this.fill, this.smoothing);
    }

    @Override
    public void onDisable() {
        this.stateFlag = false;
        this.blockPos = null;
        this.offsetBox = null;
        this.box = null;
        super.onDisable();
    }

    @EventHandler(priority = -100)
    public void onRender3D(WorldRenderEvent event) {
        if (mc != null && mc.world != null && mc.player != null) {
            Box box = this.createBoxFromBlockStateWithType();
            if (box == null) {
                this.stateFlag = false;
                this.blockPos = null;
                this.offsetBox = null;
                this.box = null;
            } else {
                if (this.offsetBox != null && this.box != null && this.blockPos != null) {
                    this.box = box;
                    this.offsetBox = this.createBox(this.offsetBox, this.box, (this.smoothing.get() + 20.0F) / 100.0F);
                } else {
                    this.offsetBox = box;
                    this.box = box;
                }
                this.blockPos = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
                this.updateThemeColorWithThemes();
                Vec3d pos = event.getCamera().getPos();
                Box boxValue = this.offsetBox.offset(-pos.x, -pos.y, -pos.z);
                Matrix4f positionMatrix = event.getMatrices().peek().getPositionMatrix();
                this.updateFramebufferWidthWithFramebufferHeight();
                if (this.framebuffer != null) {
                    this.stateFlag = true;
                    this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    this.framebuffer.clear();
                    this.updateGlBindFramebufferWithGlGetInteger();
                    this.framebuffer.beginWrite(false);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableCull();
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                    this.renderColorWithVertex(positionMatrix, boxValue);
                    RenderSystem.depthMask(true);
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                    mc.getFramebuffer().beginWrite(false);
                }
            }
        }
    }

    @EventHandler(priority = 200)
    public void onRender2D(HudRenderEvent event) {
        if (this.stateFlag && this.framebuffer != null) {
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.blockOverlay);
            if (orCreateProgram != null) {
                int cachedPrimaryColor = this.cachedPrimaryColor;
                int cachedSecondaryColor = this.cachedSecondaryColor;
                mc.getFramebuffer().beginWrite(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.setShader(ShaderPrograms.blockOverlay);
                RenderSystem.setShaderTexture(0, this.framebuffer.getColorAttachment());
                String current = this.shaderMode.getCurrent();
                float floatValue = switch(current) {
                    case "Caustic" ->
                        1.0F;
                    case "Sunset" ->
                        2.0F;
                    case "Nebula" ->
                        3.0F;
                    default ->
                        0.0F;
                };
                Camera camera = mc.gameRenderer.getCamera();
                float floatValueValue = (float) Math.toRadians(-camera.getYaw());
                float floatValueResult = (float) Math.toRadians(camera.getPitch());
                float floatValueItem = mc.options.getFov().getValue().floatValue();
                this.updateUniform(orCreateProgram, "texelSize", 1.0F / Math.max(1, mc.getWindow().getFramebufferWidth()), 1.0F / Math.max(1, mc.getWindow().getFramebufferHeight()));
                this.updateUniform(orCreateProgram, "color", ColorMathUtil.redf(cachedPrimaryColor), ColorMathUtil.greenf(cachedPrimaryColor), ColorMathUtil.bluef(cachedPrimaryColor));
                this.updateUniform(orCreateProgram, "color2", ColorMathUtil.redf(cachedSecondaryColor), ColorMathUtil.greenf(cachedSecondaryColor), ColorMathUtil.bluef(cachedSecondaryColor));
                this.updateUniform(orCreateProgram, "time", (float) (System.currentTimeMillis() % 100000L) / 1000.0F);
                this.updateUniform(orCreateProgram, "speed", this.shaderSpeed.get());
                this.updateUniform(orCreateProgram, "scale", 1.0F);
                this.updateUniform(orCreateProgram, "outline", this.outlineWidth.get());
                this.updateUniform(orCreateProgram, "glow", 2.0F);
                this.updateUniform(orCreateProgram, "fill", this.fill.get());
                this.updateUniform(orCreateProgram, "alpha", 1.0F);
                this.updateUniform(orCreateProgram, "outlineOnly", 0.0F);
                this.updateUniform(orCreateProgram, "optimize", 0.0F);
                this.updateUniform(orCreateProgram, "mode", floatValue);
                this.updateUniform(orCreateProgram, "cameraDir", floatValueValue, floatValueResult);
                this.updateUniform(orCreateProgram, "fov", floatValueItem);
                this.updateUniform(orCreateProgram, "resolution", mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
                this.renderColorWithTexture();
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderTexture(0, 0);
            }
        }
    }

    private void updateUniform(ShaderProgram shaderProgram, String text, float floatValue) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue);
        }
    }

    private void updateUniform(ShaderProgram shaderProgram, String text, float floatValue, float floatValueValue) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue);
        }
    }

    private void updateUniform(ShaderProgram shaderProgram, String text, float floatValue, float floatValueValue, float floatValueResult) {
        GlUniform uniform = shaderProgram.getUniform(text);
        if (uniform != null) {
            uniform.set(floatValue, floatValueValue, floatValueResult);
        }
    }

    private void updateFramebufferWidthWithFramebufferHeight() {
        int previousPrimaryColor = mc.getWindow().getFramebufferWidth();
        int previousSecondaryColor = mc.getWindow().getFramebufferHeight();
        if (this.framebuffer == null || this.previousPrimaryColor != previousPrimaryColor || this.previousSecondaryColor != previousSecondaryColor) {
            if (this.framebuffer != null) {
                this.framebuffer.delete();
            }
            this.framebuffer = new SimpleFramebuffer(previousPrimaryColor, previousSecondaryColor, true);
            this.previousPrimaryColor = previousPrimaryColor;
            this.previousSecondaryColor = previousSecondaryColor;
        }
    }

    private Box createBoxFromBlockStateWithType() {
        HitResult crosshairTarget = mc.crosshairTarget;
        if (crosshairTarget instanceof BlockHitResult blockHitResult && crosshairTarget.getType() == Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (blockPos == null) {
                return null;
            } else if (mc.world.getBlockState(blockPos).isAir()) {
                return null;
            } else {
                VoxelShape outlineShape = mc.world.getBlockState(blockPos).getOutlineShape(mc.world, blockPos);
                Box box = outlineShape.isEmpty() ? new Box(blockPos) : outlineShape.getBoundingBox().offset(blockPos);
                return box.expand(0.002);
            }
        } else {
            return null;
        }
    }

    private Box createBox(Box box, Box boxValue, float floatValue) {
        return new Box(box.minX + (boxValue.minX - box.minX) * floatValue, box.minY + (boxValue.minY - box.minY) * floatValue, box.minZ + (boxValue.minZ - box.minZ) * floatValue, box.maxX + (boxValue.maxX - box.maxX) * floatValue, box.maxY + (boxValue.maxY - box.maxY) * floatValue, box.maxZ + (boxValue.maxZ - box.maxZ) * floatValue);
    }

    private void updateGlBindFramebufferWithGlGetInteger() {
        if (this.framebuffer != null) {
            int intValue = GL11.glGetInteger(36010);
            int intValueValue = GL11.glGetInteger(36006);
            int framebufferWidth = mc.getWindow().getFramebufferWidth();
            int framebufferHeight = mc.getWindow().getFramebufferHeight();
            GL30.glBindFramebuffer(36008, mc.getFramebuffer().fbo);
            GL30.glBindFramebuffer(36009, this.framebuffer.fbo);
            GL30.glBlitFramebuffer(0, 0, framebufferWidth, framebufferHeight, 0, 0, framebufferWidth, framebufferHeight, 256, 9728);
            GL30.glBindFramebuffer(36008, intValue);
            GL30.glBindFramebuffer(36009, intValueValue);
        }
    }

    private void updateThemeColorWithThemes() {
        if (Toshiba.INSTANCE != null && Toshiba.INSTANCE.themeStorage != null && Toshiba.INSTANCE.themeStorage.getThemes() != null) {
            Theme theme = Toshiba.INSTANCE.themeStorage.getThemes().getTheme();
            if (theme == null) {
                this.cachedPrimaryColor = ColorMathUtil.getThemeColor(0);
                this.cachedSecondaryColor = ColorMathUtil.getThemeColor(180);
            } else {
                int cachedPrimaryColor = theme.color != null && theme.color.length > 0 ? theme.color[0] : ColorMathUtil.getThemeColor(0);
                this.cachedPrimaryColor = cachedPrimaryColor;
                this.cachedSecondaryColor = cachedPrimaryColor;
            }
        } else {
            this.cachedPrimaryColor = ColorMathUtil.getThemeColor(0);
            this.cachedSecondaryColor = ColorMathUtil.getThemeColor(180);
        }
    }

    private void renderColorWithVertex(Matrix4f matrix, Box box) {
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        byte byteValue = -1;
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(byteValue);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(byteValue);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void renderColorWithTexture() {
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float floatValue = Math.max(mc.getWindow().getScaledWidth(), 1);
        float floatValueValue = Math.max(mc.getWindow().getScaledHeight(), 1);
        buffer.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(0.0F, floatValueValue, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, floatValueValue, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
