package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.AttackEffect.Burst;
import brova.toshiba.client.modules.impl.render.AttackEffect.BurstParticle;
import brova.toshiba.client.modules.impl.render.AttackEffect.Spark;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.render.shader.ShaderPrograms;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class AttackEffect extends Module {

    public static AttackEffect INSTANCE = new AttackEffect();

    private static final Identifier bloomTexture = Identifier.of("toshiba", "featrues/particle/bloom.png");

    private static final float piRadians = (float) Math.PI;

    private static final float fullCircleRadians = (float) (Math.PI * 2);

    private static final float cachedScalarForUpdateCosWithSin = (float) Math.toRadians(72.0);

    private final ModeSetting mode = new ModeSetting("Режим", "Искры", "Искры", "Разрез", "Молния");

    private final FloatSetting size = new FloatSetting("Размер", 1.0F, 0.5F, 2.0F, 0.05F);

    private final FloatSetting duration = new FloatSetting("Длительность", 1.0F, 0.5F, 2.0F, 0.05F);

    private final FloatSetting sparks = new FloatSetting("Искры", 14.0F, 0.0F, 32.0F, 1.0F);

    private final FloatSetting brightness = new FloatSetting("Яркость", 1.0F, 0.2F, 1.0F, 0.05F);

    private final BooleanSetting criticalBoost = new BooleanSetting("Усиление крита", true);

    private final BooleanSetting followTarget = new BooleanSetting("Следовать за целью", true).visible(() -> !this.mode.is("Искры"));

    private final BooleanSetting throughWalls = new BooleanSetting("Сквозь стены", false);

    private final BooleanSetting customColor = new BooleanSetting("Свой цвет", false);

    private final ColorSetting color = new ColorSetting("Цвет", -7709441).visible(this.customColor::isState);

    private final List<Burst> removeIfAttackEffectBurstEntries = new ArrayList<>();

    private final Random random = new Random();

    private final float[] floatValues = new float[3];

    private ClientWorld clientWorld;

    private boolean stateFlagForUpdateCosWithSin;

    private BufferBuilder bufferBuilder;

    private Matrix4f positionMatrix;

    private boolean stateFlagForRenderBeginWithSqrt;

    private float value = 1.0F;

    private float billboardRightX;

    private float billboardRightY;

    private float billboardRightZ;

    private float billboardUpX;

    private float billboardUpY;

    private float billboardUpZ;

    public AttackEffect() {
        super("AttackEffect", "Красивый эффект при ударе по цели", ModuleCategory.RENDER);
        this.addSettings(this.mode, this.size, this.duration, this.sparks, this.brightness, this.criticalBoost, this.followTarget, this.throughWalls, this.customColor, this.color);
    }

    @Override
    public void onDisable() {
        this.removeIfAttackEffectBurstEntries.clear();
        this.clientWorld = null;
        super.onDisable();
    }

    @EventHandler(priority = -200)
    public void onAttack(TargetPlayerEvent event) {
        if (!event.isCancelled() && this.isLocalState() && event.getPlayer() == mc.player) {
            Entity target = event.getTarget();
            if (target != null && target != mc.player) {
                ClientPlayerEntity player = mc.player;
                Vec3d eyePos = player.getEyePos();
                Vec3d position = this.createPositionFromCenterWithRaycast(target, eyePos, player.getRotationVec(1.0F));
                Vec3d positionValue = position.subtract(eyePos);
                positionValue = positionValue.lengthSquared() < 1.0E-6 ? player.getRotationVec(1.0F) : positionValue.normalize();
                float clampedValue = MathHelper.clamp(player.getAttackCooldownProgress(0.5F), 0.0F, 1.0F);
                boolean flag = this.criticalBoost.isState() && this.isOnGroundWithClimbing(player, target, clampedValue);
                float floatValue = this.size.get() * (0.6F + 0.4F * clampedValue) * (flag ? 1.3F : 1.0F);
                int intValue = this.calculateColor();
                Burst burst = new Burst(this.mode.getIndex(), target, position, target.getPos(), positionValue, floatValue, flag, intValue, this.calculateRGBtoHSBWithHSBtoRGB(intValue), System.nanoTime());
                switch(burst.cachedCountForAttack) {
                    case 1:
                        this.updateCosWithSin(burst);
                        break;
                    case 2:
                        this.spawnLightningBranches(burst);
                        break;
                    default:
                        this.spawnImpactSparks(burst);
                }
                burst.finish();
                this.removeIfAttackEffectBurstEntries.add(burst);
                while (this.removeIfAttackEffectBurstEntries.size() > 12) {
                    this.removeIfAttackEffectBurstEntries.remove(0);
                }
            }
        }
    }

    private void spawnImpactSparks(Burst burst) {
        float floatValue = this.duration.get();
        float floatValueValue = this.random.nextFloat() * (float) (Math.PI * 2);
        int intValue = this.calculateAttackEffectBurst(burst);
        for (int index = 0; index < intValue; index++) {
            boolean flag = index % 4 == 0;
            float floatValueResult = floatValueValue + (float) (Math.PI * 2) * (index + this.randomFloatBetween(-0.3F, 0.3F)) / intValue;
            float floatValueItem = burst.cachedScalarForUpdateNextFloatWithCos * this.randomFloatBetween(1.3F, 3.2F) * (flag ? 0.65F : 1.0F) / floatValue;
            float velocityY = burst.cachedScalarForUpdateNextFloatWithCos * this.randomFloatBetween(2.6F, 5.2F) * (flag ? 0.8F : 1.0F) / floatValue;
            BurstParticle burstParticle = new BurstParticle();
            burstParticle.originX = (this.randomFloatBetween(-0.035F, 0.035F) - burst.normalX * 0.06F) * burst.cachedScalarForUpdateNextFloatWithCos;
            burstParticle.originY = this.randomFloatBetween(-0.04F, 0.04F) * burst.cachedScalarForUpdateNextFloatWithCos;
            burstParticle.originZ = (this.randomFloatBetween(-0.035F, 0.035F) - burst.normalZ * 0.06F) * burst.cachedScalarForUpdateNextFloatWithCos;
            burstParticle.velocityX = (MathHelper.cos(floatValueResult) - burst.normalX * 0.22F) * floatValueItem;
            burstParticle.velocityY = velocityY;
            burstParticle.velocityZ = (MathHelper.sin(floatValueResult) - burst.normalZ * 0.22F) * floatValueItem;
            burstParticle.drag = this.randomFloatBetween(0.45F, 0.85F) / floatValue;
            burstParticle.gravity = burst.cachedScalarForUpdateNextFloatWithCos * this.randomFloatBetween(7.5F, 10.0F) / (floatValue * floatValue);
            burstParticle.spawnDelay = this.randomFloatBetween(0.0F, 0.025F) * floatValue;
            burstParticle.lifetime = this.randomFloatBetween(1.05F, 1.55F) * floatValue;
            burstParticle.size = burst.cachedScalarForUpdateNextFloatWithCos * (flag ? this.randomFloatBetween(0.009F, 0.014F) : this.randomFloatBetween(0.016F, 0.025F));
            burstParticle.trailDuration = (flag ? this.randomFloatBetween(0.025F, 0.045F) : this.randomFloatBetween(0.075F, 0.13F)) * floatValue;
            burstParticle.rotationPhase = this.random.nextFloat() * (float) (Math.PI * 2);
            burstParticle.color = interpolateRgb(index % 3 == 0 ? burst.secondaryColor : burst.primaryColor, -1, this.randomFloatBetween(0.08F, 0.25F));
            burst.attackEffectBurstParticleEntries.add(burstParticle);
        }
    }

    private void updateCosWithSin(Burst burst) {
        float floatValue = this.duration.get();
        burst.impactDuration = 0.45F * floatValue;
        this.stateFlagForUpdateCosWithSin = !this.stateFlagForUpdateCosWithSin;
        burst.rotationDirection = this.stateFlagForUpdateCosWithSin ? 1.0F : -1.0F;
        burst.cos = (float) Math.toRadians(this.randomFloatBetween(18.0F, 55.0F)) * (this.random.nextBoolean() ? 1.0F : -1.0F);
        float floatValueValue = MathHelper.cos(burst.cos);
        float floatValueResult = MathHelper.sin(burst.cos);
        float floatValueItem = burst.tangentX * floatValueValue + burst.bitangentX * floatValueResult;
        float floatValueEntry = burst.tangentY * floatValueValue + burst.bitangentY * floatValueResult;
        float floatValueCopy = burst.tangentZ * floatValueValue + burst.bitangentZ * floatValueResult;
        float floatValue2 = -burst.tangentX * floatValueResult + burst.bitangentX * floatValueValue;
        float floatValue3 = -burst.tangentY * floatValueResult + burst.bitangentY * floatValueValue;
        float floatValue4 = -burst.tangentZ * floatValueResult + burst.bitangentZ * floatValueValue;
        float floatValue5 = 0.95F * burst.cachedScalarForUpdateNextFloatWithCos;
        float floatValue6 = 0.3F * burst.cachedScalarForUpdateNextFloatWithCos;
        float floatValue7 = -burst.normalX * floatValue6 - floatValue2 * floatValue5;
        float floatValue8 = -burst.normalY * floatValue6 - floatValue3 * floatValue5;
        float floatValue9 = -burst.normalZ * floatValue6 - floatValue4 * floatValue5;
        float floatValue10 = (float) (Math.PI / 2) - burst.rotationDirection * cachedScalarForUpdateCosWithSin;
        float floatValue11 = burst.rotationDirection * 2.0F * cachedScalarForUpdateCosWithSin;
        int intValue = this.calculateAttackEffectBurst(burst);
        for (int index = 0; index < intValue; index++) {
            boolean glowing = index % 3 == 2;
            float floatValue12 = this.randomFloatBetween(0.12F, 0.95F);
            float floatValue13 = floatValue10 + floatValue11 * floatValue12;
            float floatValue14 = MathHelper.cos(floatValue13);
            float floatValue15 = MathHelper.sin(floatValue13);
            float floatValue16 = floatValueItem * floatValue14 + floatValue2 * floatValue15;
            float floatValue17 = floatValueEntry * floatValue14 + floatValue3 * floatValue15;
            float floatValue18 = floatValueCopy * floatValue14 + floatValue4 * floatValue15;
            float floatValue19 = (-floatValueItem * floatValue15 + floatValue2 * floatValue14) * burst.rotationDirection;
            float floatValue20 = (-floatValueEntry * floatValue15 + floatValue3 * floatValue14) * burst.rotationDirection;
            float floatValue21 = (-floatValueCopy * floatValue15 + floatValue4 * floatValue14) * burst.rotationDirection;
            float floatValue22 = burst.cachedScalarForUpdateNextFloatWithCos * (glowing ? this.randomFloatBetween(0.8F, 1.8F) : this.randomFloatBetween(2.5F, 5.5F));
            this.updateNextFloatWithSqrt();
            BurstParticle burstParticle = new BurstParticle();
            burstParticle.originX = floatValue7 + floatValue16 * floatValue5;
            burstParticle.originY = floatValue8 + floatValue17 * floatValue5;
            burstParticle.originZ = floatValue9 + floatValue18 * floatValue5;
            burstParticle.velocityX = (floatValue19 * 0.75F + floatValue16 * 0.55F + this.floatValues[0] * 0.25F) * floatValue22;
            burstParticle.velocityY = (floatValue20 * 0.75F + floatValue17 * 0.55F + this.floatValues[1] * 0.25F) * floatValue22;
            burstParticle.velocityZ = (floatValue21 * 0.75F + floatValue18 * 0.55F + this.floatValues[2] * 0.25F) * floatValue22;
            burstParticle.drag = glowing ? 2.5F : 6.0F;
            burstParticle.gravity = glowing ? -0.3F * burst.cachedScalarForUpdateNextFloatWithCos : 5.0F * burst.cachedScalarForUpdateNextFloatWithCos;
            burstParticle.spawnDelay = 0.28F * burst.impactDuration * (1.0F - (float) Math.cbrt(1.0F - floatValue12));
            burstParticle.lifetime = (glowing ? this.randomFloatBetween(0.45F, 0.75F) : this.randomFloatBetween(0.2F, 0.38F)) * floatValue;
            burstParticle.size = burst.cachedScalarForUpdateNextFloatWithCos * (glowing ? this.randomFloatBetween(0.045F, 0.07F) : this.randomFloatBetween(0.024F, 0.034F));
            burstParticle.trailDuration = 0.035F;
            burstParticle.glowing = glowing;
            burstParticle.rotationPhase = this.random.nextFloat() * (float) (Math.PI * 2);
            burstParticle.color = interpolateRgb(burst.primaryColor, -1, this.randomFloatBetween(0.25F, 0.65F));
            burstParticle.glowColor = interpolateRgb(burstParticle.color, -1, 0.6F);
            burst.attackEffectBurstParticleEntries.add(burstParticle);
        }
    }

    private void spawnLightningBranches(Burst burst) {
        float floatValue = this.duration.get();
        burst.impactDuration = 0.5F * floatValue;
        int intValue = burst.stateFlag ? 6 : 4;
        burst.attackEffectSparkValues = new Spark[intValue];
        float floatValueValue = this.random.nextFloat() * (float) (Math.PI * 2);
        for (int index = 0; index < intValue; index++) {
            float floatValueResult = floatValueValue + index * (float) (Math.PI * 2) / intValue + this.randomFloatBetween(-0.35F, 0.35F);
            float floatValueItem = MathHelper.cos(floatValueResult);
            float floatValueEntry = MathHelper.sin(floatValueResult);
            float floatValueCopy = this.randomFloatBetween(-0.6F, 0.25F);
            float floatValue2 = burst.tangentX * floatValueItem + burst.bitangentX * floatValueEntry + burst.normalX * floatValueCopy;
            float floatValue3 = burst.tangentY * floatValueItem + burst.bitangentY * floatValueEntry + burst.normalY * floatValueCopy;
            float floatValue4 = burst.tangentZ * floatValueItem + burst.bitangentZ * floatValueEntry + burst.normalZ * floatValueCopy;
            float floatValue5 = burst.cachedScalarForUpdateNextFloatWithCos * this.randomFloatBetween(0.55F, 1.1F) * (burst.stateFlag ? 1.2F : 1.0F);
            float floatValue6 = inverseVectorLength(floatValue2, floatValue3, floatValue4) * floatValue5;
            Spark spark = new Spark(floatValue2 * floatValue6, floatValue3 * floatValue6, floatValue4 * floatValue6, floatValue5, index * 0.025F + this.randomFloatBetween(0.0F, 0.02F), 5 + this.random.nextInt(5));
            this.updateAttackEffectSpark(spark);
            burst.attackEffectSparkValues[index] = spark;
        }
        burst.lastLightningRebuildMillis = burst.creationMillis + 45000000L;
        int intValueValue = Math.round(this.calculateAttackEffectBurst(burst) * 0.6F);
        for (int indexValue = 0; indexValue < intValueValue; indexValue++) {
            this.updateNextFloatWithSqrt();
            float floatValue7 = burst.cachedScalarForUpdateNextFloatWithCos * this.randomFloatBetween(4.0F, 8.0F);
            BurstParticle burstParticle = new BurstParticle();
            burstParticle.velocityX = this.floatValues[0] * floatValue7;
            burstParticle.velocityY = this.floatValues[1] * floatValue7;
            burstParticle.velocityZ = this.floatValues[2] * floatValue7;
            burstParticle.drag = 8.0F;
            burstParticle.gravity = 3.0F * burst.cachedScalarForUpdateNextFloatWithCos;
            burstParticle.spawnDelay = this.randomFloatBetween(0.0F, 0.12F);
            burstParticle.lifetime = this.randomFloatBetween(0.15F, 0.3F) * floatValue;
            burstParticle.size = burst.cachedScalarForUpdateNextFloatWithCos * this.randomFloatBetween(0.016F, 0.026F);
            burstParticle.trailDuration = 0.03F;
            burstParticle.color = interpolateRgb(burst.primaryColor, -1, this.randomFloatBetween(0.45F, 0.8F));
            burstParticle.glowColor = interpolateRgb(burstParticle.color, -1, 0.7F);
            burst.attackEffectBurstParticleEntries.add(burstParticle);
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (this.isLocalState() && !this.removeIfAttackEffectBurstEntries.isEmpty()) {
            long currentTimeNanos = System.nanoTime();
            this.removeIfAttackEffectBurstEntries.removeIf(input -> (float) (currentTimeNanos - input.creationMillis) * 1.0E-9F > input.totalDuration);
            if (!this.removeIfAttackEffectBurstEntries.isEmpty()) {
                Camera camera = event.getCamera();
                Vec3d pos = camera.getPos();
                Vector3f horizontalPlane = camera.getHorizontalPlane();
                Vector3f verticalPlane = camera.getVerticalPlane();
                this.billboardRightX = horizontalPlane.y * verticalPlane.z - horizontalPlane.z * verticalPlane.y;
                this.billboardRightY = horizontalPlane.z * verticalPlane.x - horizontalPlane.x * verticalPlane.z;
                this.billboardRightZ = horizontalPlane.x * verticalPlane.y - horizontalPlane.y * verticalPlane.x;
                this.billboardUpX = verticalPlane.x;
                this.billboardUpY = verticalPlane.y;
                this.billboardUpZ = verticalPlane.z;
                this.value = this.brightness.get();
                this.positionMatrix = event.getMatrices().peek().getPositionMatrix();
                for (Burst burst : this.removeIfAttackEffectBurstEntries) {
                    this.updateRemovedWithAlive(burst, event.getTickDelta());
                }
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(770, 1);
                RenderSystem.disableCull();
                RenderSystem.depthMask(false);
                if (this.throughWalls.isState()) {
                    RenderSystem.disableDepthTest();
                } else {
                    RenderSystem.enableDepthTest();
                }
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                try {
                    this.renderBeginWithSqrt(pos, currentTimeNanos, false);
                    this.renderBeginWithSqrt(pos, currentTimeNanos, true);
                } finally {
                    this.positionMatrix = null;
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(true);
                    RenderSystem.enableCull();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableBlend();
                }
            }
        }
    }

    private void renderBeginWithSqrt(Vec3d position, long longValue, boolean flag) {
        this.bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        this.stateFlagForRenderBeginWithSqrt = false;
        for (Burst burst : this.removeIfAttackEffectBurstEntries) {
            if (burst.cachedCountForAttack == 0 == flag) {
                double doubleValue = burst.originX - position.x;
                double doubleValueValue = burst.originY - position.y;
                double doubleValueResult = burst.originZ - position.z;
                double doubleValueItem = doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult;
                if (!(doubleValueItem > 16384.0)) {
                    float floatValue = (float) doubleValue;
                    float floatValueValue = (float) doubleValueValue;
                    float floatValueResult = (float) doubleValueResult;
                    float floatValueItem = (float) (longValue - burst.creationMillis) * 1.0E-9F;
                    float floatValueEntry = smoothstep(0.4F, 1.8F, (float) Math.sqrt(doubleValueItem));
                    if (flag) {
                        this.updateTravelWithHeight(burst, floatValue, floatValueValue, floatValueResult, floatValueItem);
                    } else {
                        switch(burst.cachedCountForAttack) {
                            case 1:
                                this.updateAttackEffectBurst(burst, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry);
                                break;
                            case 2:
                                this.updateNextFloatWithPow(burst, floatValue, floatValueValue, floatValueResult, floatValueItem, floatValueEntry, longValue);
                        }
                        this.updateExpWithPow(burst, floatValue, floatValueValue, floatValueResult, floatValueItem);
                    }
                }
            }
        }
        BuiltBuffer buffer = this.bufferBuilder.endNullable();
        this.bufferBuilder = null;
        if (buffer != null) {
            if (!this.stateFlagForRenderBeginWithSqrt) {
                buffer.close();
            } else {
                ShaderProgramKey shaderProgramKey = flag ? ShaderPrograms.attackSparks : ShaderProgramKeys.POSITION_TEX_COLOR;
                RenderSystem.setShader(shaderProgramKey);
                if (!flag) {
                    AbstractTexture texture = mc.getTextureManager().getTexture(bloomTexture);
                    texture.setFilter(true, false);
                    RenderSystem.setShaderTexture(0, bloomTexture);
                }
                BufferRenderer.drawWithGlobalProgram(buffer);
            }
        }
    }

    private void updateAttackEffectBurst(Burst burst, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry) {
        float floatValueCopy = floatValueItem / burst.impactDuration;
        this.updateCosWithSin(burst, floatValue, floatValueValue, floatValueResult, floatValueCopy, burst.cos, burst.rotationDirection, 1.0F, 1.0F, burst.primaryColor, 1.0F, floatValueEntry);
        this.updateCosWithSin(burst, floatValue, floatValueValue, floatValueResult, floatValueCopy - 0.09F, burst.cos, burst.rotationDirection, 0.82F, 0.5F, burst.secondaryColor, 0.6F, floatValueEntry);
        if (burst.stateFlag) {
            float floatValue2 = burst.cos + burst.rotationDirection * (float) Math.PI * 0.5F;
            this.updateCosWithSin(burst, floatValue, floatValueValue, floatValueResult, floatValueCopy - 0.16F, floatValue2, -burst.rotationDirection, 1.0F, 0.9F, burst.primaryColor, 0.95F, floatValueEntry);
        }
    }

    private void updateCosWithSin(Burst burst, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, int intValue, float floatValue4, float floatValue5) {
        if (!(floatValueItem <= 0.0F) && !(floatValueItem >= 1.0F)) {
            float cachedScalarForUpdateNextFloatWithCos = burst.cachedScalarForUpdateNextFloatWithCos;
            float floatValue6 = MathHelper.cos(floatValueEntry);
            float floatValue7 = MathHelper.sin(floatValueEntry);
            float floatValue8 = burst.tangentX * floatValue6 + burst.bitangentX * floatValue7;
            float floatValue9 = burst.tangentY * floatValue6 + burst.bitangentY * floatValue7;
            float floatValue10 = burst.tangentZ * floatValue6 + burst.bitangentZ * floatValue7;
            float floatValue11 = -burst.tangentX * floatValue7 + burst.bitangentX * floatValue6;
            float floatValue12 = -burst.tangentY * floatValue7 + burst.bitangentY * floatValue6;
            float floatValue13 = -burst.tangentZ * floatValue7 + burst.bitangentZ * floatValue6;
            float floatValue14 = 0.95F * cachedScalarForUpdateNextFloatWithCos * floatValue2 * (1.0F + 0.12F * easeOutCubic(floatValueItem));
            float floatValue15 = 0.3F * cachedScalarForUpdateNextFloatWithCos;
            float floatValue16 = floatValue - burst.normalX * floatValue15 - floatValue11 * floatValue14;
            float floatValue17 = floatValueValue - burst.normalY * floatValue15 - floatValue12 * floatValue14;
            float floatValue18 = floatValueResult - burst.normalZ * floatValue15 - floatValue13 * floatValue14;
            float floatValue19 = easeOutCubic(clampUnit(floatValueItem / 0.28F));
            float floatValue20 = easeInCubic(clampUnit((floatValueItem - 0.08F) / 0.8F));
            if (!(floatValue19 - floatValue20 < 0.003F)) {
                float floatValue21 = (1.0F - smoothstep(0.6F, 1.0F, floatValueItem)) * floatValue4;
                float floatValue22 = 0.17F * cachedScalarForUpdateNextFloatWithCos * floatValue3 * (1.0F - 0.35F * floatValueItem);
                float floatValue23 = (float) (Math.PI / 2) - floatValueCopy * cachedScalarForUpdateCosWithSin;
                float floatValue24 = floatValueCopy * 2.0F * cachedScalarForUpdateCosWithSin;
                int intValueValue = interpolateRgb(intValue, -1, 0.8F);
                this.updateSinWithCos(floatValue16, floatValue17, floatValue18, floatValue8, floatValue9, floatValue10, floatValue11, floatValue12, floatValue13, floatValue14, floatValue23, floatValue24, floatValue20, floatValue19, floatValue22 * 2.4F, intValue, floatValue21 * 0.45F);
                this.updateSinWithCos(floatValue16, floatValue17, floatValue18, floatValue8, floatValue9, floatValue10, floatValue11, floatValue12, floatValue13, floatValue14, floatValue23, floatValue24, floatValue20, floatValue19, floatValue22 * 1.1F, interpolateRgb(intValue, intValueValue, 0.35F), floatValue21 * 0.85F);
                this.updateSinWithCos(floatValue16, floatValue17, floatValue18, floatValue8, floatValue9, floatValue10, floatValue11, floatValue12, floatValue13, floatValue14, floatValue23, floatValue24, floatValue20, floatValue19, floatValue22 * 0.4F, intValueValue, floatValue21);
                float floatValue25 = (1.0F - smoothstep(0.2F, 0.45F, floatValueItem)) * floatValue4 * (0.5F + 0.5F * floatValue5);
                if (floatValue25 > 0.003F) {
                    float floatValue26 = floatValue23 + floatValue24 * floatValue19;
                    float floatValue27 = MathHelper.cos(floatValue26);
                    float floatValue28 = MathHelper.sin(floatValue26);
                    float floatValue29 = floatValue16 + (floatValue8 * floatValue27 + floatValue11 * floatValue28) * floatValue14;
                    float floatValue30 = floatValue17 + (floatValue9 * floatValue27 + floatValue12 * floatValue28) * floatValue14;
                    float floatValue31 = floatValue18 + (floatValue10 * floatValue27 + floatValue13 * floatValue28) * floatValue14;
                    this.updateFloat(floatValue29, floatValue30, floatValue31, cachedScalarForUpdateNextFloatWithCos * 0.55F * floatValue3, intValue, floatValue25 * 0.6F);
                    this.updateFloat(floatValue29, floatValue30, floatValue31, cachedScalarForUpdateNextFloatWithCos * 0.22F * floatValue3, intValueValue, floatValue25);
                }
            }
        }
    }

    private void updateNextFloatWithPow(Burst burst, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, long longValue) {
        float floatValueCopy = floatValueItem / burst.impactDuration;
        if (!(floatValueCopy >= 1.0F)) {
            if (longValue >= burst.lastLightningRebuildMillis) {
                for (Spark spark : burst.attackEffectSparkValues) {
                    this.updateAttackEffectSpark(spark);
                    spark.flickerAlpha = 0.55F + 0.45F * this.random.nextFloat();
                }
                burst.lastLightningRebuildMillis = longValue + 45000000L;
            }
            float cachedScalarForUpdateNextFloatWithCos = burst.cachedScalarForUpdateNextFloatWithCos;
            int intValue = interpolateRgb(burst.primaryColor, -1, 0.8F);
            float floatValue2 = (float) Math.pow(1.0F - floatValueCopy, 1.3F);
            float floatValue3 = Math.max(0.0F, 1.0F - floatValueCopy / 0.45F);
            floatValue3 *= floatValue3 * floatValueEntry;
            if (floatValue3 > 0.003F) {
                float floatValue4 = burst.attackEffectSparkValues.length > 0 ? burst.attackEffectSparkValues[0].flickerAlpha : 1.0F;
                this.updateFloat(floatValue, floatValueValue, floatValueResult, cachedScalarForUpdateNextFloatWithCos * 1.4F, burst.primaryColor, floatValue3 * 0.5F * floatValue4);
                this.updateFloat(floatValue, floatValueValue, floatValueResult, cachedScalarForUpdateNextFloatWithCos * 0.45F, intValue, floatValue3);
            }
            for (Spark sparkValue : burst.attackEffectSparkValues) {
                float floatValue5 = floatValueItem - sparkValue.jitterScale;
                if (!(floatValue5 <= 0.0F)) {
                    float floatValue6 = clampUnit(floatValue5 / 0.07F);
                    int intValueValue = Math.min(17, Math.max(2, Math.round(floatValue6 * 16.0F) + 1));
                    float floatValue7 = floatValue2 * sparkValue.flickerAlpha;
                    this.updateSqrtWithLerp(sparkValue.mainPathVertices, intValueValue, 16, floatValue, floatValueValue, floatValueResult, cachedScalarForUpdateNextFloatWithCos * 0.085F, cachedScalarForUpdateNextFloatWithCos * 0.03F, burst.primaryColor, floatValue7 * 0.45F);
                    this.updateSqrtWithLerp(sparkValue.mainPathVertices, intValueValue, 16, floatValue, floatValueValue, floatValueResult, cachedScalarForUpdateNextFloatWithCos * 0.022F, cachedScalarForUpdateNextFloatWithCos * 0.008F, intValue, floatValue7);
                    if (floatValue6 >= 1.0F) {
                        this.updateSqrtWithLerp(sparkValue.branchPathVertices, 9, 8, floatValue, floatValueValue, floatValueResult, cachedScalarForUpdateNextFloatWithCos * 0.05F, cachedScalarForUpdateNextFloatWithCos * 0.015F, burst.primaryColor, floatValue7 * 0.35F);
                        this.updateSqrtWithLerp(sparkValue.branchPathVertices, 9, 8, floatValue, floatValueValue, floatValueResult, cachedScalarForUpdateNextFloatWithCos * 0.014F, cachedScalarForUpdateNextFloatWithCos * 0.005F, intValue, floatValue7 * 0.8F);
                        this.updateFloat(floatValue + sparkValue.endX, floatValueValue + sparkValue.endY, floatValueResult + sparkValue.endZ, cachedScalarForUpdateNextFloatWithCos * 0.32F, burst.primaryColor, floatValue7 * 0.6F);
                    }
                }
            }
        }
    }

    private void updateTravelWithHeight(Burst burst, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        for (BurstParticle burstParticle : burst.attackEffectBurstParticleEntries) {
            float floatValueEntry = floatValueItem - burstParticle.spawnDelay;
            if (!(floatValueEntry <= 0.0F) && !(floatValueEntry >= burstParticle.lifetime)) {
                float floatValueCopy = floatValueEntry / burstParticle.lifetime;
                float floatValue2 = burstParticle.travel(floatValueEntry);
                float floatValue3 = floatValue + burstParticle.originX + burstParticle.velocityX * floatValue2;
                float floatValue4 = floatValueValue + burstParticle.height(floatValueEntry, floatValue2);
                float floatValue5 = floatValueResult + burstParticle.originZ + burstParticle.velocityZ * floatValue2;
                float floatValue6 = smoothstep(0.0F, 0.025F, floatValueCopy) * (1.0F - smoothstep(0.4F, 1.0F, floatValueCopy));
                float floatValue7 = 0.88F + 0.12F * MathHelper.sin(floatValueEntry * 42.0F + burstParticle.rotationPhase);
                float floatValue8 = burstParticle.size * (1.0F - 0.5F * floatValueCopy);
                float floatValue9 = floatValue6 * floatValue7;
                this.updateLerpWithExp(burstParticle, floatValue, floatValueValue, floatValueResult, floatValueEntry, floatValue8 * 3.0F, floatValue9);
                this.updateFloat(floatValue3, floatValue4, floatValue5, floatValue8 * 8.0F, burstParticle.color, floatValue9);
            }
        }
    }

    private void updateLerpWithExp(BurstParticle burstParticle, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        int intValue = this.calculateFloat(floatValueCopy);
        float floatValue2 = Math.max(0.0F, floatValueItem - burstParticle.trailDuration);
        if (intValue > 0 && !(floatValueItem - floatValue2 < 1.0E-5F)) {
            int intValueValue = burstParticle.color >> 16 & 0xFF;
            int intValueResult = burstParticle.color >> 8 & 0xFF;
            int intValueItem = burstParticle.color & 0xFF;
            float floatValue3 = burstParticle.gravity / burstParticle.drag;
            float floatValue4 = 0.0F;
            float floatValue5 = 0.0F;
            float floatValue6 = 0.0F;
            float floatValue7 = 0.0F;
            float floatValue8 = 0.0F;
            float floatValue9 = 0.0F;
            for (int index = 0; index <= 8; index++) {
                float floatValue10 = index / 8.0F;
                float interpolatedValue = MathHelper.lerp(floatValue10, floatValue2, floatValueItem);
                float floatValue11 = (float) Math.exp(-burstParticle.drag * interpolatedValue);
                float floatValue12 = (1.0F - floatValue11) / burstParticle.drag;
                float floatValue13 = floatValue + burstParticle.originX + burstParticle.velocityX * floatValue12;
                float floatValue14 = floatValueValue + burstParticle.height(interpolatedValue, floatValue12);
                float floatValue15 = floatValueResult + burstParticle.originZ + burstParticle.velocityZ * floatValue12;
                float floatValue16 = burstParticle.velocityX * floatValue11;
                float floatValue17 = (burstParticle.velocityY + floatValue3) * floatValue11 - floatValue3;
                float floatValue18 = burstParticle.velocityZ * floatValue11;
                float floatValue19 = floatValue17 * floatValue15 - floatValue18 * floatValue14;
                float floatValue20 = floatValue18 * floatValue13 - floatValue16 * floatValue15;
                float floatValue21 = floatValue16 * floatValue14 - floatValue17 * floatValue13;
                float floatValue22 = inverseVectorLength(floatValue19, floatValue20, floatValue21);
                float floatValue23 = floatValueEntry * (0.25F + 0.75F * floatValue10);
                if (floatValue22 > 0.0F) {
                    floatValue19 *= floatValue22 * floatValue23;
                    floatValue20 *= floatValue22 * floatValue23;
                    floatValue21 *= floatValue22 * floatValue23;
                } else {
                    floatValue19 = this.billboardRightX * floatValue23;
                    floatValue20 = this.billboardRightY * floatValue23;
                    floatValue21 = this.billboardRightZ * floatValue23;
                }
                if (index > 0) {
                    float floatValue24 = 2.0F + (index - 1) / 8.0F;
                    float floatValue25 = 2.0F + floatValue10;
                    this.renderColorWithTexture(floatValue4 - floatValue7, floatValue5 - floatValue8, floatValue6 - floatValue9, floatValue24, 0.0F, intValueValue, intValueResult, intValueItem, intValue);
                    this.renderColorWithTexture(floatValue4 + floatValue7, floatValue5 + floatValue8, floatValue6 + floatValue9, floatValue24, 1.0F, intValueValue, intValueResult, intValueItem, intValue);
                    this.renderColorWithTexture(floatValue13 + floatValue19, floatValue14 + floatValue20, floatValue15 + floatValue21, floatValue25, 1.0F, intValueValue, intValueResult, intValueItem, intValue);
                    this.renderColorWithTexture(floatValue13 - floatValue19, floatValue14 - floatValue20, floatValue15 - floatValue21, floatValue25, 0.0F, intValueValue, intValueResult, intValueItem, intValue);
                }
                floatValue4 = floatValue13;
                floatValue5 = floatValue14;
                floatValue6 = floatValue15;
                floatValue7 = floatValue19;
                floatValue8 = floatValue20;
                floatValue9 = floatValue21;
            }
            this.stateFlagForRenderBeginWithSqrt = true;
        }
    }

    private void updateExpWithPow(Burst burst, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        for (BurstParticle burstParticle : burst.attackEffectBurstParticleEntries) {
            float floatValueEntry = floatValueItem - burstParticle.spawnDelay;
            if (!(floatValueEntry <= 0.0F) && !(floatValueEntry >= burstParticle.lifetime)) {
                float floatValueCopy = floatValueEntry / burstParticle.lifetime;
                float floatValue2 = (float) Math.exp(-burstParticle.drag * floatValueEntry);
                float floatValue3 = (1.0F - floatValue2) / burstParticle.drag;
                float floatValue4 = 0.5F * burstParticle.gravity * floatValueEntry * floatValueEntry;
                float floatValue5 = burstParticle.originX + burstParticle.velocityX * floatValue3;
                float floatValue6 = burstParticle.originY + burstParticle.velocityY * floatValue3 - floatValue4;
                float floatValue7 = burstParticle.originZ + burstParticle.velocityZ * floatValue3;
                float floatValue8 = (float) Math.pow(1.0F - floatValueCopy, 1.4F);
                if (burstParticle.glowing) {
                    float floatValue9 = 0.6F + 0.4F * MathHelper.sin(floatValueEntry * 38.0F + burstParticle.rotationPhase);
                    float floatValue10 = burstParticle.size * (1.0F - 0.5F * floatValueCopy);
                    this.updateFloat(floatValue + floatValue5, floatValueValue + floatValue6, floatValueResult + floatValue7, floatValue10 * 2.4F, burstParticle.color, floatValue8 * floatValue9 * 0.45F);
                    this.updateFloat(floatValue + floatValue5, floatValueValue + floatValue6, floatValueResult + floatValue7, floatValue10, burstParticle.glowColor, floatValue8 * floatValue9);
                } else {
                    float floatValue11 = burstParticle.velocityX * floatValue2 * burstParticle.trailDuration;
                    float floatValue12 = (burstParticle.velocityY * floatValue2 - burstParticle.gravity * floatValueEntry) * burstParticle.trailDuration;
                    float floatValue13 = burstParticle.velocityZ * floatValue2 * burstParticle.trailDuration;
                    float floatValue14 = floatValue5 - burstParticle.originX;
                    float floatValue15 = floatValue6 - burstParticle.originY;
                    float floatValue16 = floatValue7 - burstParticle.originZ;
                    float floatValue17 = floatValue14 * floatValue14 + floatValue15 * floatValue15 + floatValue16 * floatValue16;
                    float floatValue18 = floatValue11 * floatValue11 + floatValue12 * floatValue12 + floatValue13 * floatValue13;
                    if (floatValue18 > floatValue17 && floatValue18 > 1.0E-8F) {
                        float squareRoot = (float) Math.sqrt(floatValue17 / floatValue18);
                        floatValue11 *= squareRoot;
                        floatValue12 *= squareRoot;
                        floatValue13 *= squareRoot;
                    }
                    float floatValue19 = burstParticle.size * (1.0F - 0.4F * floatValueCopy);
                    float floatValue20 = floatValue + floatValue5;
                    float floatValue21 = floatValueValue + floatValue6;
                    float floatValue22 = floatValueResult + floatValue7;
                    this.updateSqrt(floatValue20, floatValue21, floatValue22, floatValue20 - floatValue11, floatValue21 - floatValue12, floatValue22 - floatValue13, floatValue19 * 2.6F, burstParticle.color, floatValue8 * 0.4F);
                    this.updateSqrt(floatValue20, floatValue21, floatValue22, floatValue20 - floatValue11, floatValue21 - floatValue12, floatValue22 - floatValue13, floatValue19, burstParticle.glowColor, floatValue8);
                }
            }
        }
    }

    private void renderColorWithTexture(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        this.bufferBuilder.vertex(this.positionMatrix, floatValue, floatValueValue, floatValueResult).texture(floatValueItem, floatValueEntry).color(intValue, intValueValue, intValueResult, intValueItem);
    }

    private int calculateFloat(float floatValue) {
        return MathHelper.clamp((int) (floatValue * this.value * 255.0F), 0, 255);
    }

    private void updateFloat(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, float floatValueEntry) {
        int intValueValue = this.calculateFloat(floatValueEntry);
        if (intValueValue > 0 && !(floatValueItem <= 5.0E-4F)) {
            int intValueResult = intValue >> 16 & 0xFF;
            int intValueItem = intValue >> 8 & 0xFF;
            int intValueEntry = intValue & 0xFF;
            float floatValueCopy = floatValueItem * 0.5F;
            float floatValue2 = this.billboardRightX * floatValueCopy;
            float floatValue3 = this.billboardRightY * floatValueCopy;
            float floatValue4 = this.billboardRightZ * floatValueCopy;
            float floatValue5 = this.billboardUpX * floatValueCopy;
            float floatValue6 = this.billboardUpY * floatValueCopy;
            float floatValue7 = this.billboardUpZ * floatValueCopy;
            this.renderColorWithTexture(floatValue - floatValue2 - floatValue5, floatValueValue - floatValue3 - floatValue6, floatValueResult - floatValue4 - floatValue7, 0.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.renderColorWithTexture(floatValue - floatValue2 + floatValue5, floatValueValue - floatValue3 + floatValue6, floatValueResult - floatValue4 + floatValue7, 0.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.renderColorWithTexture(floatValue + floatValue2 + floatValue5, floatValueValue + floatValue3 + floatValue6, floatValueResult + floatValue4 + floatValue7, 1.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.renderColorWithTexture(floatValue + floatValue2 - floatValue5, floatValueValue + floatValue3 - floatValue6, floatValueResult + floatValue4 - floatValue7, 1.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
            this.stateFlagForRenderBeginWithSqrt = true;
        }
    }

    private void updateSqrt(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, int intValue, float floatValue3) {
        int intValueValue = this.calculateFloat(floatValue3);
        if (intValueValue > 0 && !(floatValue2 <= 1.0E-4F)) {
            float floatValue4 = floatValue - floatValueItem;
            float floatValue5 = floatValueValue - floatValueEntry;
            float floatValue6 = floatValueResult - floatValueCopy;
            float squareRoot = (float) Math.sqrt(floatValue4 * floatValue4 + floatValue5 * floatValue5 + floatValue6 * floatValue6);
            if (squareRoot < 1.0E-4F) {
                this.updateFloat(floatValue, floatValueValue, floatValueResult, floatValue2 * 2.0F, intValue, floatValue3);
            } else {
                float floatValue7 = (floatValue + floatValueItem) * 0.5F;
                float floatValue8 = (floatValueValue + floatValueEntry) * 0.5F;
                float floatValue9 = (floatValueResult + floatValueCopy) * 0.5F;
                float floatValue10 = floatValue5 * floatValue9 - floatValue6 * floatValue8;
                float floatValue11 = floatValue6 * floatValue7 - floatValue4 * floatValue9;
                float floatValue12 = floatValue4 * floatValue8 - floatValue5 * floatValue7;
                float squareRootValue = (float) Math.sqrt(floatValue10 * floatValue10 + floatValue11 * floatValue11 + floatValue12 * floatValue12);
                if (!(squareRootValue < 1.0E-6F)) {
                    float floatValue13 = floatValue2 / squareRootValue;
                    floatValue10 *= floatValue13;
                    floatValue11 *= floatValue13;
                    floatValue12 *= floatValue13;
                    float floatValue14 = floatValue2 / squareRoot;
                    float floatValue15 = floatValue4 * floatValue14;
                    float floatValue16 = floatValue5 * floatValue14;
                    float floatValue17 = floatValue6 * floatValue14;
                    float floatValue18 = floatValue + floatValue15;
                    float floatValue19 = floatValueValue + floatValue16;
                    float floatValue20 = floatValueResult + floatValue17;
                    float floatValue21 = floatValueItem - floatValue15;
                    float floatValue22 = floatValueEntry - floatValue16;
                    float floatValue23 = floatValueCopy - floatValue17;
                    int intValueResult = intValue >> 16 & 0xFF;
                    int intValueItem = intValue >> 8 & 0xFF;
                    int intValueEntry = intValue & 0xFF;
                    this.renderColorWithTexture(floatValue21 - floatValue10, floatValue22 - floatValue11, floatValue23 - floatValue12, 0.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.renderColorWithTexture(floatValue21 + floatValue10, floatValue22 + floatValue11, floatValue23 + floatValue12, 0.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.renderColorWithTexture(floatValue18 + floatValue10, floatValue19 + floatValue11, floatValue20 + floatValue12, 1.0F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.renderColorWithTexture(floatValue18 - floatValue10, floatValue19 - floatValue11, floatValue20 - floatValue12, 1.0F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueValue);
                    this.stateFlagForRenderBeginWithSqrt = true;
                }
            }
        }
    }

    private void updateSinWithCos(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3, float floatValue4, float floatValue5, float floatValue6, float floatValue7, float floatValue8, float floatValue9, float floatValue10, int intValue, float floatValue11) {
        int intValueValue = this.calculateFloat(floatValue11);
        if (intValueValue > 0 && !(floatValue9 - floatValue8 < 0.002F)) {
            int intValueResult = intValue >> 16 & 0xFF;
            int intValueItem = intValue >> 8 & 0xFF;
            int intValueEntry = intValue & 0xFF;
            float floatValue12 = 0.0F;
            float floatValue13 = 0.0F;
            float floatValue14 = 0.0F;
            float floatValue15 = 0.0F;
            float floatValue16 = 0.0F;
            float floatValue17 = 0.0F;
            int intValueCopy = 0;
            for (int index = 0; index <= 30; index++) {
                float floatValue18 = index / 30.0F;
                float floatValue19 = floatValue8 + (floatValue9 - floatValue8) * floatValue18;
                float floatValue20 = floatValue6 + floatValue7 * floatValue19;
                float floatValue21 = MathHelper.cos(floatValue20);
                float floatValue22 = MathHelper.sin(floatValue20);
                float floatValue23 = floatValueItem * floatValue21 + floatValue2 * floatValue22;
                float floatValue24 = floatValueEntry * floatValue21 + floatValue3 * floatValue22;
                float floatValue25 = floatValueCopy * floatValue21 + floatValue4 * floatValue22;
                float floatValue26 = (float) Math.pow(Math.max(0.0F, MathHelper.sin((float) Math.PI * floatValue19)), 0.7F) * smoothstep(0.0F, 0.45F, floatValue18) * smoothstep(0.0F, 0.12F, 1.0F - floatValue18);
                float floatValue27 = floatValue10 * floatValue26;
                float floatValue28 = floatValue5 - floatValue27 * 0.4F;
                float floatValue29 = floatValue5 + floatValue27 * 0.6F;
                float floatValue30 = floatValue + floatValue23 * floatValue28;
                float floatValue31 = floatValueValue + floatValue24 * floatValue28;
                float floatValue32 = floatValueResult + floatValue25 * floatValue28;
                float floatValue33 = floatValue + floatValue23 * floatValue29;
                float floatValue34 = floatValueValue + floatValue24 * floatValue29;
                float floatValue35 = floatValueResult + floatValue25 * floatValue29;
                int intValue2 = (int) (intValueValue * (0.35F + 0.65F * floatValue18));
                if (index > 0) {
                    this.renderColorWithTexture(floatValue12, floatValue13, floatValue14, 0.5F, 0.0F, intValueResult, intValueItem, intValueEntry, intValueCopy);
                    this.renderColorWithTexture(floatValue15, floatValue16, floatValue17, 0.5F, 1.0F, intValueResult, intValueItem, intValueEntry, intValueCopy);
                    this.renderColorWithTexture(floatValue33, floatValue34, floatValue35, 0.5F, 1.0F, intValueResult, intValueItem, intValueEntry, intValue2);
                    this.renderColorWithTexture(floatValue30, floatValue31, floatValue32, 0.5F, 0.0F, intValueResult, intValueItem, intValueEntry, intValue2);
                }
                floatValue12 = floatValue30;
                floatValue13 = floatValue31;
                floatValue14 = floatValue32;
                floatValue15 = floatValue33;
                floatValue16 = floatValue34;
                floatValue17 = floatValue35;
                intValueCopy = intValue2;
            }
            this.stateFlagForRenderBeginWithSqrt = true;
        }
    }

    private void updateSqrtWithLerp(float[] values, int intValue, int intValueValue, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValueResult, float floatValueCopy) {
        int intValueItem = this.calculateFloat(floatValueCopy);
        if (intValueItem > 0 && intValue >= 2) {
            int intValueEntry = intValueResult >> 16 & 0xFF;
            int intValueCopy = intValueResult >> 8 & 0xFF;
            int intValue2 = intValueResult & 0xFF;
            float floatValue2 = 0.0F;
            float floatValue3 = 0.0F;
            float floatValue4 = 0.0F;
            float floatValue5 = 0.0F;
            float floatValue6 = 0.0F;
            float floatValue7 = 0.0F;
            for (int index = 0; index < intValue; index++) {
                int intValue3 = index * 3;
                float floatValue8 = floatValue + values[intValue3];
                float floatValue9 = floatValueValue + values[intValue3 + 1];
                float floatValue10 = floatValueResult + values[intValue3 + 2];
                int intValue4 = Math.max(0, index - 1) * 3;
                int intValue5 = Math.min(intValue - 1, index + 1) * 3;
                float floatValue11 = values[intValue5] - values[intValue4];
                float floatValue12 = values[intValue5 + 1] - values[intValue4 + 1];
                float floatValue13 = values[intValue5 + 2] - values[intValue4 + 2];
                float floatValue14 = floatValue12 * floatValue10 - floatValue13 * floatValue9;
                float floatValue15 = floatValue13 * floatValue8 - floatValue11 * floatValue10;
                float floatValue16 = floatValue11 * floatValue9 - floatValue12 * floatValue8;
                float squareRoot = (float) Math.sqrt(floatValue14 * floatValue14 + floatValue15 * floatValue15 + floatValue16 * floatValue16);
                if (squareRoot > 1.0E-6F) {
                    float floatValue17 = MathHelper.lerp((float) index / intValueValue, floatValueItem, floatValueEntry) / squareRoot;
                    floatValue14 *= floatValue17;
                    floatValue15 *= floatValue17;
                    floatValue16 *= floatValue17;
                } else {
                    floatValue14 = floatValue5;
                    floatValue15 = floatValue6;
                    floatValue16 = floatValue7;
                }
                if (index > 0) {
                    this.renderColorWithTexture(floatValue2 - floatValue5, floatValue3 - floatValue6, floatValue4 - floatValue7, 0.5F, 0.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                    this.renderColorWithTexture(floatValue2 + floatValue5, floatValue3 + floatValue6, floatValue4 + floatValue7, 0.5F, 1.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                    this.renderColorWithTexture(floatValue8 + floatValue14, floatValue9 + floatValue15, floatValue10 + floatValue16, 0.5F, 1.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                    this.renderColorWithTexture(floatValue8 - floatValue14, floatValue9 - floatValue15, floatValue10 - floatValue16, 0.5F, 0.0F, intValueEntry, intValueCopy, intValue2, intValueItem);
                }
                floatValue2 = floatValue8;
                floatValue3 = floatValue9;
                floatValue4 = floatValue10;
                floatValue5 = floatValue14;
                floatValue6 = floatValue15;
                floatValue7 = floatValue16;
            }
            this.stateFlagForRenderBeginWithSqrt = true;
        }
    }

    private void updateAttackEffectSpark(Spark spark) {
        float[] mainPathVertices = spark.mainPathVertices;
        mainPathVertices[0] = mainPathVertices[1] = mainPathVertices[2] = 0.0F;
        byte byteValue = 48;
        mainPathVertices[byteValue] = spark.endX;
        mainPathVertices[byteValue + 1] = spark.endY;
        mainPathVertices[byteValue + 2] = spark.endZ;
        this.updateFloat(mainPathVertices, 0, 16, spark.length * 0.22F);
        float[] branchPathVertices = spark.branchPathVertices;
        int intValue = spark.color * 3;
        float floatValue = mainPathVertices[intValue + 3] - mainPathVertices[intValue];
        float floatValueValue = mainPathVertices[intValue + 4] - mainPathVertices[intValue + 1];
        float floatValueResult = mainPathVertices[intValue + 5] - mainPathVertices[intValue + 2];
        float floatValueItem = inverseVectorLength(floatValue, floatValueValue, floatValueResult);
        this.updateNextFloatWithSqrt();
        floatValue = floatValue * floatValueItem + this.floatValues[0] * 0.9F;
        floatValueValue = floatValueValue * floatValueItem + this.floatValues[1] * 0.9F;
        floatValueResult = floatValueResult * floatValueItem + this.floatValues[2] * 0.9F;
        float floatValueEntry = spark.length * 0.45F;
        floatValueItem = inverseVectorLength(floatValue, floatValueValue, floatValueResult) * floatValueEntry;
        branchPathVertices[0] = mainPathVertices[intValue];
        branchPathVertices[1] = mainPathVertices[intValue + 1];
        branchPathVertices[2] = mainPathVertices[intValue + 2];
        byte byteValueValue = 24;
        branchPathVertices[byteValueValue] = branchPathVertices[0] + floatValue * floatValueItem;
        branchPathVertices[byteValueValue + 1] = branchPathVertices[1] + floatValueValue * floatValueItem;
        branchPathVertices[byteValueValue + 2] = branchPathVertices[2] + floatValueResult * floatValueItem;
        this.updateFloat(branchPathVertices, 0, 8, floatValueEntry * 0.25F);
    }

    private void updateFloat(float[] values, int intValue, int intValueValue, float floatValue) {
        if (intValueValue - intValue >= 2) {
            int intValueResult = intValue + intValueValue >>> 1;
            int intValueItem = intValue * 3;
            int intValueEntry = intValueValue * 3;
            int intValueCopy = intValueResult * 3;
            float floatValueValue = values[intValueEntry] - values[intValueItem];
            float floatValueResult = values[intValueEntry + 1] - values[intValueItem + 1];
            float floatValueItem = values[intValueEntry + 2] - values[intValueItem + 2];
            float floatValueEntry = this.randomFloatBetween(-1.0F, 1.0F);
            float floatValueCopy = this.randomFloatBetween(-1.0F, 1.0F);
            float floatValue2 = this.randomFloatBetween(-1.0F, 1.0F);
            float floatValue3 = floatValueValue * floatValueValue + floatValueResult * floatValueResult + floatValueItem * floatValueItem;
            if (floatValue3 > 1.0E-8F) {
                float floatValue4 = (floatValueEntry * floatValueValue + floatValueCopy * floatValueResult + floatValue2 * floatValueItem) / floatValue3;
                floatValueEntry -= floatValueValue * floatValue4;
                floatValueCopy -= floatValueResult * floatValue4;
                floatValue2 -= floatValueItem * floatValue4;
            }
            values[intValueCopy] = (values[intValueItem] + values[intValueEntry]) * 0.5F + floatValueEntry * floatValue;
            values[intValueCopy + 1] = (values[intValueItem + 1] + values[intValueEntry + 1]) * 0.5F + floatValueCopy * floatValue;
            values[intValueCopy + 2] = (values[intValueItem + 2] + values[intValueEntry + 2]) * 0.5F + floatValue2 * floatValue;
            this.updateFloat(values, intValue, intValueResult, floatValue * 0.55F);
            this.updateFloat(values, intValueResult, intValueValue, floatValue * 0.55F);
        }
    }

    private void updateRemovedWithAlive(Burst burst, float floatValue) {
        if (burst.cachedCountForAttack != 0) {
            Entity entity = burst.entity;
            if (this.followTarget.isState() && entity != null && !entity.isRemoved() && entity.isAlive() && entity.getWorld() == mc.world) {
                Vec3d lerpedPos = entity.getLerpedPos(floatValue);
                burst.originX = lerpedPos.x + burst.targetOffsetX;
                burst.originY = lerpedPos.y + burst.targetOffsetY;
                burst.originZ = lerpedPos.z + burst.targetOffsetZ;
            }
        }
    }

    private Vec3d createPositionFromCenterWithRaycast(Entity entity, Vec3d position, Vec3d positionValue) {
        Box boundingBox = entity.getBoundingBox();
        double doubleValue = position.distanceTo(boundingBox.getCenter()) + entity.getWidth() + entity.getHeight() + 1.0;
        var optional = boundingBox.raycast(position, position.add(positionValue.multiply(doubleValue)));
        if (optional.isPresent()) {
            return optional.get();
        } else if (boundingBox.contains(position)) {
            return boundingBox.getCenter();
        } else {
            double lengthY = boundingBox.getLengthY();
            Vec3d positionResult = new Vec3d(boundingBox.getCenter().x, MathHelper.clamp(position.y, boundingBox.minY + lengthY * 0.25, boundingBox.maxY - lengthY * 0.15), boundingBox.getCenter().z);
            return boundingBox.raycast(position, positionResult).orElse(positionResult);
        }
    }

    private boolean isOnGroundWithClimbing(PlayerEntity player, Entity entity, float floatValue) {
        return floatValue > 0.9F && player.fallDistance > 0.0F && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.isInLava() && !player.hasVehicle() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && entity instanceof LivingEntity;
    }

    private int calculateAttackEffectBurst(Burst burst) {
        return Math.round(this.sparks.get() * (burst.stateFlag ? 1.6F : 1.0F));
    }

    private int calculateColor() {
        if (this.customColor.isState()) {
            return this.color.getColor() | 0xFF000000;
        } else {
            int[] values = this.createIntegerFromThemeWithThemes();
            return (values != null && values.length > 0 ? values[0] : -7709441) | 0xFF000000;
        }
    }

    private int calculateRGBtoHSBWithHSBtoRGB(int intValue) {
        int[] values = this.customColor.isState() ? null : this.createIntegerFromThemeWithThemes();
        if (values != null && values.length > 1 && (values[1] & 16777215) != (intValue & 16777215)) {
            float[] valuesValue = Color.RGBtoHSB(values[1] >> 16 & 0xFF, values[1] >> 8 & 0xFF, values[1] & 0xFF, null);
            float[] valuesResult = Color.RGBtoHSB(intValue >> 16 & 0xFF, intValue >> 8 & 0xFF, intValue & 0xFF, null);
            return Color.HSBtoRGB(valuesValue[0], valuesValue[1], Math.max(valuesValue[2], valuesResult[2])) | 0xFF000000;
        } else {
            float[] valuesItem = Color.RGBtoHSB(intValue >> 16 & 0xFF, intValue >> 8 & 0xFF, intValue & 0xFF, null);
            return Color.HSBtoRGB(valuesItem[0] + 0.07F, valuesItem[1], valuesItem[2]) | 0xFF000000;
        }
    }

    private int[] createIntegerFromThemeWithThemes() {
        try {
            return Toshiba.INSTANCE.themeStorage.getThemes().getTheme().color;
        } catch (Exception failure) {
            return null;
        }
    }

    private boolean isLocalState() {
        if (this.clientWorld != mc.world || mc.player == null) {
            this.removeIfAttackEffectBurstEntries.clear();
            this.clientWorld = mc.world;
        }
        return this.clientWorld != null && mc.player != null;
    }

    private void updateNextFloatWithSqrt() {
        float floatValue = this.randomFloatBetween(-1.0F, 1.0F);
        float floatValueValue = this.random.nextFloat() * (float) (Math.PI * 2);
        float squareRoot = (float) Math.sqrt(1.0F - floatValue * floatValue);
        this.floatValues[0] = squareRoot * MathHelper.cos(floatValueValue);
        this.floatValues[1] = squareRoot * MathHelper.sin(floatValueValue);
        this.floatValues[2] = floatValue;
    }

    private float randomFloatBetween(float floatValue, float floatValueValue) {
        return floatValue + this.random.nextFloat() * (floatValueValue - floatValue);
    }

    private static float inverseVectorLength(float floatValue, float floatValueValue, float floatValueResult) {
        float squareRoot = (float) Math.sqrt(floatValue * floatValue + floatValueValue * floatValueValue + floatValueResult * floatValueResult);
        return squareRoot < 1.0E-6F ? 0.0F : 1.0F / squareRoot;
    }

    private static int interpolateRgb(int intValue, int intValueValue, float floatValue) {
        int intValueResult = (int) ((intValue >> 16 & 0xFF) + ((intValueValue >> 16 & 0xFF) - (intValue >> 16 & 0xFF)) * floatValue);
        int intValueItem = (int) ((intValue >> 8 & 0xFF) + ((intValueValue >> 8 & 0xFF) - (intValue >> 8 & 0xFF)) * floatValue);
        int intValueEntry = (int) ((intValue & 0xFF) + ((intValueValue & 0xFF) - (intValue & 0xFF)) * floatValue);
        return 0xFF000000 | intValueResult << 16 | intValueItem << 8 | intValueEntry;
    }

    private static float clampUnit(float floatValue) {
        return floatValue < 0.0F ? 0.0F : Math.min(floatValue, 1.0F);
    }

    private static float smoothstep(float floatValue, float floatValueValue, float floatValueResult) {
        float floatValueItem = clampUnit((floatValueResult - floatValue) / (floatValueValue - floatValue));
        return floatValueItem * floatValueItem * (3.0F - 2.0F * floatValueItem);
    }

    private static float easeOutCubic(float floatValue) {
        float floatValueValue = 1.0F - floatValue;
        return 1.0F - floatValueValue * floatValueValue * floatValueValue;
    }

    private static float easeInCubic(float floatValue) {
        return floatValue * floatValue * floatValue;
    }

    public final static class BurstParticle {

        public float originX;

        public float originY;

        public float originZ;

        public float velocityX;

        public float velocityY;

        public float velocityZ;

        public float drag = 1.0F;

        public float gravity;

        public float spawnDelay;

        public float lifetime = 0.3F;

        public float size;

        public float trailDuration;

        public float rotationPhase;

        public boolean glowing;

        public int color;

        public int glowColor;

        BurstParticle() {
        }

        public float travel(float floatValue) {
            return (1.0F - (float) Math.exp(-this.drag * floatValue)) / this.drag;
        }

        public float height(float floatValue, float floatValueValue) {
            float floatValueResult = this.gravity / this.drag;
            return this.originY + (this.velocityY + floatValueResult) * floatValueValue - floatValueResult * floatValue;
        }
    }

    public final static class Spark {

        public final float endX;

        public final float endY;

        public final float endZ;

        public final float length;

        public final float jitterScale;

        public final int color;

        public final float[] mainPathVertices = new float[51];

        public final float[] branchPathVertices = new float[27];

        public float flickerAlpha = 1.0F;

        public Spark(float endX, float endY, float endZ, float length, float jitterScale, int color) {
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
            this.length = length;
            this.jitterScale = jitterScale;
            this.color = color;
        }
    }

    public final static class Burst {

        public final int cachedCountForAttack;

        public final Entity entity;

        public final double targetOffsetX;

        public final double targetOffsetY;

        public final double targetOffsetZ;

        public double originX;

        public double originY;

        public double originZ;

        public final float normalX;

        public final float normalY;

        public final float normalZ;

        public final float tangentX;

        public final float tangentY;

        public final float tangentZ;

        public final float bitangentX;

        public final float bitangentY;

        public final float bitangentZ;

        public final float cachedScalarForUpdateNextFloatWithCos;

        public final boolean stateFlag;

        public final int primaryColor;

        public final int secondaryColor;

        public final long creationMillis;

        public final List<BurstParticle> attackEffectBurstParticleEntries = new ArrayList<>();

        public float impactDuration;

        public float totalDuration;

        public float cos;

        public float rotationDirection = 1.0F;

        public Spark[] attackEffectSparkValues = new Spark[0];

        public long lastLightningRebuildMillis;

        public Burst(int cachedCountForAttack, Entity entity, Vec3d position, Vec3d positionValue, Vec3d positionResult, float cachedScalarForUpdateNextFloatWithCos, boolean stateFlag, int primaryColor, int secondaryColor, long creationMillis) {
            this.cachedCountForAttack = cachedCountForAttack;
            this.entity = entity;
            this.targetOffsetX = position.x - positionValue.x;
            this.targetOffsetY = position.y - positionValue.y;
            this.targetOffsetZ = position.z - positionValue.z;
            this.originX = position.x;
            this.originY = position.y;
            this.originZ = position.z;
            this.normalX = (float) positionResult.x;
            this.normalY = (float) positionResult.y;
            this.normalZ = (float) positionResult.z;
            this.cachedScalarForUpdateNextFloatWithCos = cachedScalarForUpdateNextFloatWithCos;
            this.stateFlag = stateFlag;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.creationMillis = creationMillis;
            float tangentX = -this.normalZ;
            float tangentZ = this.normalX;
            float squareRoot = (float) Math.sqrt(tangentX * tangentX + tangentZ * tangentZ);
            if (squareRoot < 1.0E-4F) {
                tangentX = 1.0F;
                tangentZ = 0.0F;
            } else {
                tangentX /= squareRoot;
                tangentZ /= squareRoot;
            }
            this.tangentX = tangentX;
            this.tangentY = 0.0F;
            this.tangentZ = tangentZ;
            this.bitangentX = this.tangentY * this.normalZ - this.tangentZ * this.normalY;
            this.bitangentY = this.tangentZ * this.normalX - this.tangentX * this.normalZ;
            this.bitangentZ = this.tangentX * this.normalY - this.tangentY * this.normalX;
        }

        public void finish() {
            this.totalDuration = this.impactDuration;
            for (BurstParticle burstParticle : this.attackEffectBurstParticleEntries) {
                this.totalDuration = Math.max(this.totalDuration, burstParticle.spawnDelay + burstParticle.lifetime);
            }
        }
    }
}
