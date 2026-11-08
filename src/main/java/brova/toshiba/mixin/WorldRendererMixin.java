package brova.toshiba.mixin;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.CustomWorld;
import brova.toshiba.client.modules.impl.render.NoRender;
import brova.toshiba.client.modules.impl.render.ShaderESP;
import brova.toshiba.client.utils.render.shader.SkyShaderMode;
import brova.toshiba.client.utils.render.shader.SkyShaderRenderer;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.DimensionEffects.SkyType;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderPass;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements MinecraftAccess {

    @Shadow
    private ClientWorld world;

    @Shadow
    @Final
    private DefaultFramebufferSet framebufferSet;

    @Shadow
    abstract boolean hasBlindnessOrDarkness(Camera camera);

    @Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
    private void toshiba$renderParticles(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Партиклы")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void toshiba$renderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Погода")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "addWeatherParticlesAndSound", at = @At("HEAD"), cancellable = true)
    private void toshiba$addWeatherParticlesAndSound(Camera camera, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Погода")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void toshiba$renderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f positionMatrix, Matrix4f projectionMatrix, CloudRenderMode renderMode, Vec3d cameraPos, float ticks, int color, float cloudHeight, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            NoRender noRender = ModuleRegistry.noRender;
            if (noRender != null && noRender.isEnabled("Облака")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        Profilers.get().swap("toshiba_renderWorld");
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);
        new WorldRenderEvent(matrices, positionMatrix, projectionMatrix, camera, tickCounter.getTickDelta(false)).call();
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void toshiba$drawEntityOutlinesFramebuffer(CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null) {
            ShaderESP shaderEsp = ModuleRegistry.shaderEsp;
            if (shaderEsp != null && shaderEsp.isEnable()) {
                ci.cancel();
            } else {
                NoRender noRender = ModuleRegistry.noRender;
                if (noRender != null && noRender.isEnabled("Свечение")) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    public void onDrawBlockOutline(CallbackInfo ci) {
        if (ModuleRegistry.blockOverlay.isEnable()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void toshiba$renderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        CustomWorld tweaks = ModuleRegistry.worldTweaks;
        if (tweaks != null && tweaks.isShaderSkyEnabled()) {
            CameraSubmersionType submersionType = camera.getSubmersionType();
            if (submersionType == CameraSubmersionType.POWDER_SNOW || submersionType == CameraSubmersionType.LAVA || this.hasBlindnessOrDarkness(camera)) {
                return;
            }
            DimensionEffects dimensionEffects = this.world.getDimensionEffects();
            SkyType skyType = dimensionEffects.getSkyType();
            if (skyType == SkyType.NONE) {
                return;
            }
            RenderPass renderPass = frameGraphBuilder.createPass("sky_shader");
            this.framebufferSet.mainFramebuffer = renderPass.transfer(this.framebufferSet.mainFramebuffer);
            renderPass.setRenderer(() -> {
                String shaderMode = tweaks.getShaderMode().getCurrent();
                SkyShaderMode mode = switch(shaderMode) {
                    case "Caustic" ->
                        SkyShaderMode.CAUSTIC;
                    case "Sunset" ->
                        SkyShaderMode.SUNSET;
                    case "Nebula" ->
                        SkyShaderMode.NEBULA;
                    case "Северное сияние" ->
                        SkyShaderMode.SEVERNOESIYANIE;
                    default ->
                        SkyShaderMode.FOG;
                };
                boolean isBlur = tweaks.isBlurEnabled();
                if (mode != SkyShaderMode.FOG) {
                    SkyShaderRenderer.render(mode, tweaks.getPolarColor1(), tweaks.getPolarColor2(), tweaks.getPolarSpeed(), 1.0F, isBlur);
                } else {
                    SkyShaderRenderer.render(mode, isBlur);
                }
            });
            ci.cancel();
        }
    }
}
