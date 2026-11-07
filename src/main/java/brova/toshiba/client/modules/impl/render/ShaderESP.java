package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import brova.toshiba.mixin.WorldRendererAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class ShaderESP extends Module {

    public static ShaderESP INSTANCE = new ShaderESP();

    private final MultiSetting targetTypes = new MultiSetting("Цели", new BooleanSetting("Игроки", true), new BooleanSetting("Кристаллы", true), new BooleanSetting("Предметы", false), new BooleanSetting("Себя", false));

    public final ModeSetting mode = new ModeSetting("Режим", "Свечение", "Свечение", "Красивый");

    public final FloatSetting waveSpeed = new FloatSetting("Скорость волн", 1.2F, 0.1F, 5.0F, 0.1F).visible(() -> this.mode.is("Красивый"));

    public final FloatSetting waveScale = new FloatSetting("Частота волн", 1.0F, 1.0F, 3.0F, 0.1F).visible(() -> this.mode.is("Красивый"));

    public final FloatSetting outline = new FloatSetting("Ширина обводки", 1.2F, 0.1F, 5.0F, 0.1F);

    public final FloatSetting glow = new FloatSetting("Сила свечения", 1.0F, 0.0F, 5.0F, 0.1F);

    public final FloatSetting fill = new FloatSetting("Заливка", 0.6F, 0.0F, 1.0F, 0.01F);

    public final FloatSetting alpha = new FloatSetting("Прозрачность", 1.0F, 0.0F, 1.0F, 0.05F);

    public final BooleanSetting optimize = new BooleanSetting("Оптимизация", false);

    private final List<Framebuffer> framebufferEntries = new ArrayList<>();

    private Framebuffer framebuffer;

    private int framebufferWidth = -1;

    private int framebufferHeight = -1;

    private boolean stateFlagForEnable;

    private boolean stateFlagForDisable;

    private long lastUpdateMillis;

    public ShaderESP() {
        super("ShaderESP", "Красивый шейдер на энтити.", ModuleCategory.RENDER);
        this.addSettings(this.targetTypes, this.mode, this.waveSpeed, this.waveScale, this.outline, this.glow, this.fill, this.alpha, this.optimize);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.stateFlagForEnable = false;
        this.lastUpdateMillis = 0L;
        this.isCurrentTimeMillisWithColorAttachment();
    }

    @Override
    public void onDisable() {
        for (Framebuffer framebuffer : this.framebufferEntries) {
            framebuffer.delete();
        }
        this.framebufferEntries.clear();
        if (this.framebuffer != null) {
            this.framebuffer.delete();
            this.framebuffer = null;
        }
        this.framebufferWidth = -1;
        this.framebufferHeight = -1;
        this.stateFlagForEnable = false;
        this.stateFlagForDisable = false;
        this.lastUpdateMillis = 0L;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (this.isEnable()) {
            if (mc.world != null && mc.worldRenderer != null) {
                this.stateFlagForDisable = this.isEntitiesWithShouldOutline();
                if (!this.stateFlagForDisable) {
                    this.stateFlagForEnable = false;
                } else if (!this.stateFlagForEnable && System.currentTimeMillis() >= this.lastUpdateMillis) {
                    this.isCurrentTimeMillisWithColorAttachment();
                }
            } else {
                this.stateFlagForEnable = false;
                this.stateFlagForDisable = false;
            }
        }
    }

    @EventHandler(priority = 200)
    public void onRender2D(HudRenderEvent event) {
        if (mc.currentScreen != null) {
            // Пока открыто любое меню, пост-обработку не гоняем: она переключает фреймбуферы
            // прямо во время отрисовки gui и из-за этого экран вспыхивал белым.
            // Сама обводка рисуется ванильным рендером и остаётся видна.
            return;
        }
        if (this.isEnable() && mc.world != null && mc.player != null && mc.worldRenderer != null && this.stateFlagForDisable && this.isCurrentTimeMillisWithColorAttachment()) {
            Framebuffer framebuffer = this.createToshiba$getEntityOutlineFramebufferRaw();
            if (framebuffer != null && framebuffer.getColorAttachment() != 0) {
                Framebuffer framebufferValue = mc.getFramebuffer();
                this.updateDelete(framebufferValue.textureWidth, framebufferValue.textureHeight);
                float floatValue = this.glow.get();
                float floatValueValue = this.fill.get();
                float floatValueResult = this.alpha.get();
                float floatValueItem = this.outline.get();
                boolean flag = floatValue > 0.001F;
                boolean flagValue = floatValueValue > 0.001F && floatValueResult > 0.001F;
                if (flag || flagValue || !(floatValueItem <= 0.001F) || this.mode.is("Красивый")) {
                    int intValue = Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color[0];
                    if (this.mode.is("Красивый")) {
                        this.renderDefaultBlendFuncWithRedf(intValue, intValue, floatValue, floatValueValue, floatValueResult, floatValueItem, framebufferValue, framebuffer);
                    } else {
                        int intValueValue = 0;
                        if (flag) {
                            int intValueResult;
                            if (this.optimize.isState()) {
                                intValueResult = Math.max(2, Math.min(4, 2 + Math.round(floatValue * 0.4F)));
                            } else {
                                intValueResult = Math.max(3, Math.min(8, 4 + Math.round(floatValue * 0.7F)));
                            }
                            intValueValue = this.calculateBeginWriteWithOrCreateProgram(framebuffer.getColorAttachment(), intValueResult);
                        }
                        framebufferValue.beginWrite(false);
                        RenderSystem.enableBlend();
                        RenderSystem.colorMask(true, true, true, false);
                        RenderSystem.disableDepthTest();
                        if (flag) {
                            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsGlow);
                            if (orCreateProgram != null) {
                                RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
                                RenderSystem.setShader(ShaderPrograms.shaderHandsGlow);
                                RenderSystem.setShaderTexture(0, intValueValue);
                                RenderSystem.setShaderTexture(1, framebuffer.getColorAttachment());
                                this.updateUniform(orCreateProgram, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                this.updateUniform(orCreateProgram, "color2", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                this.updateUniform(orCreateProgram, "exposure", 1.0F + floatValue * 1.8F);
                                this.updateGlBindFramebufferWithGlBlitFramebuffer(framebufferValue, framebuffer);
                            }
                        }
                        if (flagValue) {
                            ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsOverlay);
                            if (orCreateProgramValue != null) {
                                RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                                RenderSystem.setShader(ShaderPrograms.shaderHandsOverlay);
                                RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
                                this.updateUniform(orCreateProgramValue, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                this.updateUniform(orCreateProgramValue, "fill", floatValueValue);
                                this.updateUniform(orCreateProgramValue, "alpha", floatValueResult);
                                this.updateGlBindFramebufferWithGlBlitFramebuffer(framebufferValue, framebuffer);
                            }
                        }
                        if (floatValueItem > 0.001F) {
                            ShaderProgram orCreateProgramResult = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.blockOverlay);
                            if (orCreateProgramResult != null) {
                                RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                                RenderSystem.setShader(ShaderPrograms.blockOverlay);
                                RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
                                this.updateUniform(orCreateProgramResult, "texelSize", 1.0F / Math.max(1, mc.getWindow().getFramebufferWidth()), 1.0F / Math.max(1, mc.getWindow().getFramebufferHeight()));
                                this.updateUniform(orCreateProgramResult, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                this.updateUniform(orCreateProgramResult, "color2", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
                                this.updateUniform(orCreateProgramResult, "outline", floatValueItem);
                                this.updateUniform(orCreateProgramResult, "alpha", Math.max(0.2F, floatValueResult));
                                this.updateUniform(orCreateProgramResult, "outlineOnly", 1.0F);
                                this.updateUniform(orCreateProgramResult, "optimize", this.optimize.isState() ? 1.0F : 0.0F);
                                this.updateGlBindFramebufferWithGlBlitFramebuffer(framebufferValue, framebuffer);
                            }
                        }
                        RenderSystem.colorMask(true, true, true, true);
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.setShaderTexture(0, 0);
                        RenderSystem.setShaderTexture(1, 0);
                        framebufferValue.beginWrite(true);
                    }
                }
            }
        }
    }

    private void renderDefaultBlendFuncWithRedf(int intValue, int intValueValue, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, Framebuffer framebuffer, Framebuffer framebufferValue) {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.blockOverlay);
        if (orCreateProgram != null) {
            framebuffer.beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(ShaderPrograms.blockOverlay);
            RenderSystem.setShaderTexture(0, framebufferValue.getColorAttachment());
            this.updateUniform(orCreateProgram, "texelSize", 1.0F / Math.max(1, mc.getWindow().getFramebufferWidth()), 1.0F / Math.max(1, mc.getWindow().getFramebufferHeight()));
            this.updateUniform(orCreateProgram, "color", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue));
            this.updateUniform(orCreateProgram, "color2", ColorMathUtil.redf(intValueValue), ColorMathUtil.greenf(intValueValue), ColorMathUtil.bluef(intValueValue));
            this.updateUniform(orCreateProgram, "time", (float) (System.currentTimeMillis() % 100000L) / 1000.0F);
            this.updateUniform(orCreateProgram, "speed", this.waveSpeed.get());
            this.updateUniform(orCreateProgram, "scale", this.waveScale.get());
            this.updateUniform(orCreateProgram, "outline", floatValueItem);
            this.updateUniform(orCreateProgram, "glow", floatValue);
            this.updateUniform(orCreateProgram, "fill", floatValueValue);
            this.updateUniform(orCreateProgram, "alpha", floatValueResult);
            this.updateUniform(orCreateProgram, "outlineOnly", 0.0F);
            this.updateUniform(orCreateProgram, "optimize", this.optimize.isState() ? 1.0F : 0.0F);
            this.updateGlBindFramebufferWithGlBlitFramebuffer(framebuffer, framebufferValue);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    private void updateGlBindFramebufferWithGlBlitFramebuffer(Framebuffer framebuffer, Framebuffer framebufferValue) {
        if (this.framebuffer == null) {
            this.renderColorWithTexture();
        } else {
            GL30.glBindFramebuffer(36008, framebuffer.fbo);
            GL30.glBindFramebuffer(36009, this.framebuffer.fbo);
            GL30.glBlitFramebuffer(0, 0, framebuffer.textureWidth, framebuffer.textureHeight, 0, 0, this.framebuffer.textureWidth, this.framebuffer.textureHeight, 256, 9728);
            GL30.glBindFramebuffer(36008, framebufferValue.fbo);
            GL30.glBindFramebuffer(36009, framebuffer.fbo);
            GL30.glBlitFramebuffer(0, 0, framebufferValue.textureWidth, framebufferValue.textureHeight, 0, 0, framebuffer.textureWidth, framebuffer.textureHeight, 256, 9728);
            framebuffer.beginWrite(false);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);
            RenderSystem.depthMask(false);
            this.renderColorWithTexture();
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            GL30.glBindFramebuffer(36008, this.framebuffer.fbo);
            GL30.glBindFramebuffer(36009, framebuffer.fbo);
            GL30.glBlitFramebuffer(0, 0, this.framebuffer.textureWidth, this.framebuffer.textureHeight, 0, 0, framebuffer.textureWidth, framebuffer.textureHeight, 256, 9728);
            framebuffer.beginWrite(false);
        }
    }

    private void updateDelete(int intValue, int intValueValue) {
        if (this.framebuffer != null && (this.framebuffer.textureWidth != intValue || this.framebuffer.textureHeight != intValueValue)) {
            this.framebuffer.delete();
            this.framebuffer = null;
        }
        if (this.framebuffer == null) {
            this.framebuffer = new SimpleFramebuffer(intValue, intValueValue, true);
        }
    }

    private boolean isCurrentTimeMillisWithColorAttachment() {
        if (mc.world != null && mc.worldRenderer != null) {
            Framebuffer framebuffer = this.createToshiba$getEntityOutlineFramebufferRaw();
            if (framebuffer != null && framebuffer.getColorAttachment() != 0) {
                this.stateFlagForEnable = true;
                return true;
            } else {
                if (this.stateFlagForEnable) {
                    this.stateFlagForEnable = false;
                }
                if (System.currentTimeMillis() < this.lastUpdateMillis) {
                    return false;
                } else {
                    try {
                        mc.worldRenderer.loadEntityOutlinePostProcessor();
                        framebuffer = this.createToshiba$getEntityOutlineFramebufferRaw();
                        this.stateFlagForEnable = framebuffer != null && framebuffer.getColorAttachment() != 0;
                        if (!this.stateFlagForEnable) {
                            this.lastUpdateMillis = System.currentTimeMillis() + 3000L;
                        }
                        return this.stateFlagForEnable;
                    } catch (Throwable failure) {
                        this.stateFlagForEnable = false;
                        this.lastUpdateMillis = System.currentTimeMillis() + 3000L;
                        return false;
                    }
                }
            }
        } else {
            this.stateFlagForEnable = false;
            return false;
        }
    }

    private Framebuffer createToshiba$getEntityOutlineFramebufferRaw() {
        if (mc.worldRenderer instanceof WorldRendererAccessor worldRendererAccessor) {
            Framebuffer framebuffer = worldRendererAccessor.toshiba$getEntityOutlineFramebufferRaw();
            if (framebuffer != null && framebuffer.getColorAttachment() != 0) {
                return framebuffer;
            }
        }
        return mc.worldRenderer.getEntityOutlinesFramebuffer();
    }

    public boolean shouldOutline(Entity entity) {
        if (!this.isEnable() || entity == null || mc.player == null || mc.world == null) {
            return false;
        } else if (!entity.isAlive()) {
            return false;
        } else if (entity.isRemoved()) {
            return false;
        } else if (entity == mc.player && !this.targetTypes.is("Себя")) {
            return false;
        } else if (entity.squaredDistanceTo(mc.player) > 65536.0) {
            return false;
        } else if (entity instanceof PlayerEntity) {
            return this.targetTypes.is("Игроки");
        } else if (entity instanceof EndCrystalEntity) {
            return this.targetTypes.is("Кристаллы");
        } else {
            return entity instanceof ItemEntity ? this.targetTypes.is("Предметы") : false;
        }
    }

    private boolean isEntitiesWithShouldOutline() {
        if (mc.world != null && mc.player != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (this.shouldOutline(entity)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public int getOutlineColor() {
        return ColorMathUtil.setAlphaColor(ColorMathUtil.getThemeColor(), 255) & 16777215;
    }

    private int calculateBeginWriteWithOrCreateProgram(int intValue, int intValueValue) {
        this.updateDeleteWithFramebufferWidth(intValueValue);
        if (this.framebufferEntries.isEmpty()) {
            return intValue;
        } else {
            int intValueResult = intValue;
            ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsKawaseDown);
            ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsKawaseUp);
            if (orCreateProgram != null && orCreateProgramValue != null) {
                for (int index = 0; index < intValueValue; index++) {
                    Framebuffer framebuffer = this.framebufferEntries.get(index);
                    framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    framebuffer.clear();
                    framebuffer.beginWrite(true);
                    RenderSystem.setShader(ShaderPrograms.shaderHandsKawaseDown);
                    RenderSystem.setShaderTexture(0, intValueResult);
                    this.updateShaderProgram(orCreateProgram, framebuffer.textureWidth, framebuffer.textureHeight, 1.0F + index);
                    this.renderColorWithTexture();
                    intValueResult = framebuffer.getColorAttachment();
                }
                for (int indexValue = intValueValue - 1; indexValue >= 1; indexValue--) {
                    Framebuffer framebufferValue = this.framebufferEntries.get(indexValue - 1);
                    framebufferValue.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                    framebufferValue.clear();
                    framebufferValue.beginWrite(true);
                    RenderSystem.setShader(ShaderPrograms.shaderHandsKawaseUp);
                    RenderSystem.setShaderTexture(0, intValueResult);
                    this.updateShaderProgram(orCreateProgramValue, framebufferValue.textureWidth, framebufferValue.textureHeight, 1.0F + indexValue);
                    this.updateUniform(orCreateProgramValue, "color", 1.0F, 1.0F, 1.0F);
                    this.renderColorWithTexture();
                    intValueResult = framebufferValue.getColorAttachment();
                }
                mc.getFramebuffer().beginWrite(true);
                return intValueResult;
            } else {
                return intValue;
            }
        }
    }

    private void updateDeleteWithFramebufferWidth(int intValue) {
        int framebufferWidth = mc.getWindow().getFramebufferWidth();
        int framebufferHeight = mc.getWindow().getFramebufferHeight();
        if (this.framebufferWidth != framebufferWidth || this.framebufferHeight != framebufferHeight) {
            for (Framebuffer framebuffer : this.framebufferEntries) {
                framebuffer.delete();
            }
            this.framebufferEntries.clear();
            this.framebufferWidth = framebufferWidth;
            this.framebufferHeight = framebufferHeight;
        }
        while (this.framebufferEntries.size() > intValue) {
            int intValueValue = this.framebufferEntries.size() - 1;
            this.framebufferEntries.get(intValueValue).delete();
            this.framebufferEntries.remove(intValueValue);
        }
        for (int index = 0; index < intValue; index++) {
            int intValueResult = Math.max(2, framebufferWidth >> index + 1);
            int intValueItem = Math.max(2, framebufferHeight >> index + 1);
            if (index >= this.framebufferEntries.size()) {
                SimpleFramebuffer framebufferValue = new SimpleFramebuffer(intValueResult, intValueItem, false);
                this.updateBindTextureWithGlTexParameteri(framebufferValue);
                this.framebufferEntries.add(framebufferValue);
            } else {
                Framebuffer framebufferResult = this.framebufferEntries.get(index);
                if (framebufferResult.textureWidth != intValueResult || framebufferResult.textureHeight != intValueItem) {
                    framebufferResult.delete();
                    SimpleFramebuffer framebufferItem = new SimpleFramebuffer(intValueResult, intValueItem, false);
                    this.updateBindTextureWithGlTexParameteri(framebufferItem);
                    this.framebufferEntries.set(index, framebufferItem);
                }
            }
        }
    }

    private void updateBindTextureWithGlTexParameteri(Framebuffer framebuffer) {
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        RenderSystem.bindTexture(0);
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

    private void updateShaderProgram(ShaderProgram shaderProgram, int intValue, int intValueValue, float floatValue) {
        this.updateUniform(shaderProgram, "uSize", Math.max(1, intValue), Math.max(1, intValueValue));
        this.updateUniform(shaderProgram, "uOffset", floatValue, floatValue);
        this.updateUniform(shaderProgram, "uHalfPixel", 0.5F / Math.max(1, intValue), 0.5F / Math.max(1, intValueValue));
    }

    private void renderColorWithTexture() {
        float floatValue = Math.max(mc.getWindow().getScaledWidth(), 1);
        float floatValueValue = Math.max(mc.getWindow().getScaledHeight(), 1);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(0.0F, floatValueValue, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, floatValueValue, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.vertex(floatValue, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
