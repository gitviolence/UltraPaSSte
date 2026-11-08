package brova.toshiba.client.utils.render.shader;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.impl.render.ShaderHands;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.HandsOverlayRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class HandsPostProcessor implements MinecraftAccess {

    private ClientWorld clientWorld;

    private long lastUpdateTimestamp;

    private float shaderTime;

    private float motionYaw;

    private float motionPitch;

    private float motionAmount;

    private float movementAmount;

    private float wrapDegrees;

    private float cachedScalar;

    private boolean stateFlagForReset;

    private boolean uniformEnabled;

    private boolean stateFlagForRender = true;

    public void render(ShaderHands shaderHands, Framebuffer framebuffer, Framebuffer framebufferValue, Framebuffer framebufferResult, Framebuffer framebufferItem) {
        ShaderProgram orCreateProgram = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsFireTrail);
        ShaderProgram orCreateProgramValue = mc.getShaderLoader().getOrCreateProgram(ShaderPrograms.shaderHandsFireComposite);
        if (orCreateProgram != null && orCreateProgramValue != null) {
            long lastUpdateTimestamp = System.nanoTime();
            float floatValue = this.lastUpdateTimestamp == 0L ? 0.0F : (float) (lastUpdateTimestamp - this.lastUpdateTimestamp) / 1.0E9F;
            if (this.clientWorld != mc.world || floatValue > 0.12F) {
                this.reset();
                this.clientWorld = mc.world;
                floatValue = 0.0F;
            }
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            this.shaderTime = (this.shaderTime + floatValue) % 3600.0F;
            this.updateVelocityWithCamera(floatValue);
            Framebuffer framebufferEntry = this.stateFlagForRender ? framebufferResult : framebufferItem;
            Framebuffer framebufferCopy = this.stateFlagForRender ? framebufferItem : framebufferResult;
            framebufferCopy.beginWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(ShaderPrograms.shaderHandsFireTrail);
            RenderSystem.setShaderTexture(0, framebufferEntry.getColorAttachment());
            RenderSystem.setShaderTexture(1, framebufferValue.getColorAttachment());
            RenderSystem.setShaderTexture(2, framebuffer.getColorAttachment());
            HandsOverlayRenderer.setUniform(orCreateProgram, "resolution", framebufferCopy.textureWidth, framebufferCopy.textureHeight, this.shaderTime, Math.min(floatValue, 0.05F));
            HandsOverlayRenderer.setUniform(orCreateProgram, "settings", shaderHands.fireTrail.get(), shaderHands.fireSpeed.get(), shaderHands.fireSize.get(), shaderHands.fireDensity.get());
            HandsOverlayRenderer.setUniform(orCreateProgram, "motion", this.motionYaw, this.motionPitch, this.uniformEnabled ? this.motionAmount : -1.0F, this.movementAmount);
            HandsOverlayRenderer.drawFullscreenQuad();
            this.stateFlagForRender = !this.stateFlagForRender;
            this.uniformEnabled = true;
            mc.getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(SrcFactor.ONE, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
            RenderSystem.colorMask(true, true, true, false);
            RenderSystem.setShader(ShaderPrograms.shaderHandsFireComposite);
            RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(1, framebufferValue.getColorAttachment());
            RenderSystem.setShaderTexture(2, framebufferCopy.getColorAttachment());
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "resolution", framebuffer.textureWidth, framebuffer.textureHeight);
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "time", this.shaderTime);
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "itemColor", shaderHands.fireColorMode.is("Предмет") ? 1.0F : 0.0F);
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "fireSettings", 1.0F, shaderHands.fireIntensity.get(), shaderHands.fireSize.get(), shaderHands.fireDensity.get());
            int intValue = shaderHands.resolveFireColor();
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "fireColor", ColorMathUtil.redf(intValue), ColorMathUtil.greenf(intValue), ColorMathUtil.bluef(intValue), shaderHands.fireGlow.get());
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "fireAdvanced", shaderHands.fireSmoke.get(), shaderHands.fireSoftness.get(), shaderHands.fireBlur.get(), 0.0F);
            HandsOverlayRenderer.setUniform(orCreateProgramValue, "fireHand", shaderHands.fireHandFade.get(), shaderHands.fireHandSoftness.get(), shaderHands.fireHandBloom.get(), 0.0F);
            HandsOverlayRenderer.drawFullscreenQuad();
        }
    }

    public void reset() {
        this.clientWorld = null;
        this.lastUpdateTimestamp = 0L;
        this.shaderTime = 0.0F;
        this.motionYaw = this.motionPitch = this.motionAmount = this.movementAmount = 0.0F;
        this.stateFlagForReset = this.uniformEnabled = false;
        this.stateFlagForRender = true;
    }

    private void updateVelocityWithCamera(float floatValue) {
        float floatValueValue = 0.0F;
        float floatValueResult = 0.0F;
        float floatValueItem = 0.0F;
        float floatValueEntry = 0.0F;
        if (mc.player != null) {
            Vec3d velocity = mc.player.getVelocity();
            Camera camera = mc.gameRenderer.getCamera();
            float wrapDegrees = camera.getYaw();
            float cachedScalar = camera.getPitch();
            float floatValueCopy = 0.0F;
            float floatValue2 = 0.0F;
            if (this.stateFlagForReset && floatValue > 1.0E-4F && floatValue < 0.12F) {
                float floatValue3 = MathHelper.wrapDegrees(wrapDegrees - this.wrapDegrees);
                float floatValue4 = cachedScalar - this.cachedScalar;
                if (!(Math.abs(floatValue3) > 28.0F) && !(Math.abs(floatValue4) > 20.0F)) {
                    floatValueCopy = MathHelper.clamp(-floatValue3 / 24.0F, -0.55F, 0.55F);
                    floatValue2 = MathHelper.clamp(floatValue4 / 22.0F, -0.42F, 0.42F);
                } else {
                    this.uniformEnabled = false;
                }
            }
            this.wrapDegrees = wrapDegrees;
            this.cachedScalar = cachedScalar;
            this.stateFlagForReset = true;
            double doubleValue = Math.toRadians(wrapDegrees);
            double doubleValueValue = velocity.x * Math.cos(doubleValue) + velocity.z * Math.sin(doubleValue);
            floatValueValue = MathHelper.clamp((float) doubleValueValue * 5.5F + floatValueCopy, -2.5F, 2.5F);
            floatValueResult = MathHelper.clamp((float) velocity.y * 2.8F + floatValue2, -2.0F, 2.0F);
            float floatValue5 = (float) Math.hypot(velocity.x, velocity.z) * 4.8F + Math.abs((float) velocity.y) * 1.25F;
            float floatValue6 = Math.max(Math.abs(floatValueCopy) * 0.65F, Math.abs(floatValue2) * 0.55F);
            floatValueItem = MathHelper.clamp(Math.max(floatValue5, floatValue6), 0.0F, 2.5F);
            floatValueEntry = mc.player.handSwingTicks > 0 ? 1.0F : 0.0F;
        } else {
            this.stateFlagForReset = false;
        }
        float clampedValue = MathHelper.clamp(floatValue, 0.0F, 0.05F);
        float floatValue7 = calculateExp(clampedValue, 9.0F);
        float floatValue8 = Math.max(floatValueItem, floatValueEntry * 0.85F);
        this.motionYaw = this.motionYaw + (floatValueValue - this.motionYaw) * floatValue7;
        this.motionPitch = this.motionPitch + (floatValueResult - this.motionPitch) * floatValue7;
        this.motionAmount = this.motionAmount + (floatValue8 - this.motionAmount) * calculateExp(clampedValue, floatValue8 > this.motionAmount ? 13.0F : 3.8F);
        this.movementAmount = this.movementAmount + (floatValueEntry - this.movementAmount) * calculateExp(clampedValue, floatValueEntry > this.movementAmount ? 16.0F : 5.0F);
    }

    private static float calculateExp(float floatValue, float floatValueValue) {
        return 1.0F - (float) Math.exp(-floatValue * floatValueValue);
    }
}
