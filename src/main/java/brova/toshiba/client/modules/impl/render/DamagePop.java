package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ClientTickEndEvent;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.DamagePop.DamageIndicator;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.player.HealthUtil;
import brova.toshiba.client.utils.render.animation.EasingFunctions;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class DamagePop extends Module {

    public static DamagePop INSTANCE = new DamagePop();

    private final FloatSetting lifetime = new FloatSetting("Время жизни", 1.2F, 0.4F, 5.0F, 0.1F);

    private final FloatSetting size = new FloatSetting("Размер", 18.0F, 10.0F, 36.0F, 1.0F);

    private final ColorSetting healthColor = new ColorSetting("Цвет HP", -43691);

    private final List<DamageIndicator> removeFirstDamagePopDamageIndicatorEntries = new ArrayList<>();

    private final Matrix4f matrix4f = new Matrix4f();

    private final Vector4f vector4f = new Vector4f();

    private ClientWorld clientWorld;

    private Vec3d pos = Vec3d.ZERO;

    private boolean stateFlag;

    public DamagePop() {
        super("DamagePop", "Отображает сколько вы нанесли урона по игроку", ModuleCategory.RENDER);
        this.addSettings(this.lifetime, this.size, this.healthColor);
    }

    @Override
    public void onDisable() {
        this.clearState();
        this.clientWorld = null;
        super.onDisable();
    }

    @EventHandler(priority = 200)
    public void onAttack(TargetPlayerEvent event) {
        if (this.isLocalState() && !event.isCancelled() && event.getPlayer() == mc.player && event.getTarget() instanceof LivingEntity entity && entity.isAlive()) {
            long currentTimeNanos = System.nanoTime();
            this.updateCancelledWithHealth(currentTimeNanos);
            if (this.removeFirstDamagePopDamageIndicatorEntries.size() >= 64) {
                this.removeFirstDamagePopDamageIndicatorEntries.removeFirst();
            }
            DamageIndicator damageIndicator = new DamageIndicator(event, this.createPositionFromCenterWithMultiply(entity), currentTimeNanos, (long) (this.lifetime.get() * 1.0E9F), currentTimeNanos + this.calculateNetworkHandlerWithPlayerListEntry());
            this.removeFirstDamagePopDamageIndicatorEntries.add(damageIndicator);
        }
    }

    @EventHandler
    public void onTick(ClientTickEndEvent event) {
        if (this.isLocalState()) {
            long currentTimeNanos = System.nanoTime();
            this.removeFirstDamagePopDamageIndicatorEntries.removeIf(input -> input.targetPlayerEvent.isCancelled() || currentTimeNanos - input.creationMillis >= input.lifetimeMillis);
            this.updateCancelledWithHealth(currentTimeNanos);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (this.isLocalState()) {
            this.matrix4f.set(event.getProjectionMatrix()).mul(event.getPositionMatrix());
            this.pos = event.getCamera().getPos();
            this.stateFlag = true;
        }
    }

    @EventHandler
    public void onRender(HudRenderEvent event) {
        if (this.isLocalState() && this.stateFlag && !this.removeFirstDamagePopDamageIndicatorEntries.isEmpty()) {
            long currentTimeNanos = System.nanoTime();
            TextRenderer font = FontManager.getFont("manrope", Math.round(this.size.get()));
            if (font != null) {
                float scaledWidth = mc.getWindow().getScaledWidth();
                float scaledHeight = mc.getWindow().getScaledHeight();
                int color = this.healthColor.getColor();
                for (DamageIndicator damageIndicator : this.removeFirstDamagePopDamageIndicatorEntries) {
                    long longValue = currentTimeNanos - damageIndicator.creationMillis;
                    float floatValue = (float) longValue / (float) damageIndicator.lifetimeMillis;
                    if (!(floatValue < 0.0F) && !(floatValue >= 1.0F) && this.isTransformWithFinite(damageIndicator.vec3d)) {
                        float floatValueValue = EasingFunctions.easeOutCubic(MathHelper.clamp((float) longValue / 8.0E7F, 0.0F, 1.0F));
                        float floatValueResult = calculateFloat(MathHelper.clamp((floatValue - 0.7F) / 0.3F, 0.0F, 1.0F));
                        int intValue = Math.round((color >>> 24) * floatValueValue * (1.0F - floatValueResult));
                        if (intValue > 1) {
                            float renderWidth = font.getRenderWidth(damageIndicator.withWeightText);
                            float floatValueItem = font.getHeight() * 0.5F;
                            float floatValueEntry = (this.vector4f.x / this.vector4f.w * 0.5F + 0.5F) * scaledWidth - renderWidth * 0.5F;
                            float floatValueCopy = (0.5F - this.vector4f.y / this.vector4f.w * 0.5F) * scaledHeight - floatValueItem * 0.5F;
                            floatValueCopy += this.size.get() * 0.75F * (1.0F - floatValueValue + floatValueResult);
                            if (!(floatValueEntry + renderWidth < 0.0F) && !(floatValueEntry > scaledWidth) && !(floatValueCopy + floatValueItem < 0.0F) && !(floatValueCopy > scaledHeight)) {
                                font.drawWithWeight(event.getContext().getMatrices(), damageIndicator.withWeightText, floatValueEntry + 0.6F, floatValueCopy + 0.6F, intValue / 2 << 24 | 1052696, 0.025F);
                                font.drawWithWeight(event.getContext().getMatrices(), damageIndicator.withWeightText, floatValueEntry, floatValueCopy, intValue << 24 | color & 16777215, 0.025F);
                            }
                        }
                    }
                }
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private void updateCancelledWithHealth(long longValue) {
        for (DamageIndicator damageIndicator : this.removeFirstDamagePopDamageIndicatorEntries) {
            if (damageIndicator.stateFlag && !damageIndicator.targetPlayerEvent.isCancelled()) {
                float health = HealthUtil.getHealth(damageIndicator.target);
                float absorptionAmount = damageIndicator.target.getAbsorptionAmount();
                float floatValue = Math.max(0.0F, damageIndicator.health - health) + Math.max(0.0F, damageIndicator.absorptionAmount - absorptionAmount);
                damageIndicator.health = health;
                damageIndicator.absorptionAmount = absorptionAmount;
                if (floatValue > 1.0E-4F) {
                    damageIndicator.updateToString(floatValue);
                    damageIndicator.stateFlag = false;
                    for (DamageIndicator damageIndicatorValue : this.removeFirstDamagePopDamageIndicatorEntries) {
                        if (damageIndicatorValue.target == damageIndicator.target) {
                            damageIndicatorValue.health = health;
                            damageIndicatorValue.absorptionAmount = absorptionAmount;
                        }
                    }
                }
                if (longValue >= damageIndicator.lastUpdateTimestampForUpdateCancelledWithHealth || damageIndicator.target.isRemoved() || this.clientWorld.getEntityById(damageIndicator.target.getId()) != damageIndicator.target) {
                    damageIndicator.stateFlag = false;
                }
            }
        }
    }

    private Vec3d createPositionFromCenterWithMultiply(LivingEntity entity) {
        Box boundingBox = entity.getBoundingBox();
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d rotationVec = mc.player.getRotationVec(1.0F);
        double doubleValue = eyePos.distanceTo(boundingBox.getCenter()) + entity.getWidth() + entity.getHeight() + 1.0;
        Vec3d position = boundingBox.raycast(eyePos, eyePos.add(rotationVec.multiply(doubleValue))).orElse(null);
        if (position != null) {
            return position;
        } else if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == entity) {
            return entityHitResult.getPos();
        } else {
            double doubleValueValue = Math.max(0.0, boundingBox.getCenter().subtract(eyePos).dotProduct(rotationVec));
            Vec3d positionValue = eyePos.add(rotationVec.multiply(doubleValueValue));
            return new Vec3d(MathHelper.clamp(positionValue.x, boundingBox.minX, boundingBox.maxX), MathHelper.clamp(positionValue.y, boundingBox.minY, boundingBox.maxY), MathHelper.clamp(positionValue.z, boundingBox.minZ, boundingBox.maxZ));
        }
    }

    private long calculateNetworkHandlerWithPlayerListEntry() {
        PlayerListEntry playerListEntry = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        long longValue = playerListEntry == null ? 0L : Math.max(0, playerListEntry.getLatency());
        return Math.max(300L, Math.min(1500L, 200L + longValue * 2L)) * 1000000L;
    }

    private boolean isTransformWithFinite(Vec3d position) {
        this.vector4f.set((float) (position.x - this.pos.x), (float) (position.y - this.pos.y), (float) (position.z - this.pos.z), 1.0F);
        this.matrix4f.transform(this.vector4f);
        return this.vector4f.isFinite() && this.vector4f.w > 1.0E-5F && this.vector4f.z >= -this.vector4f.w && this.vector4f.z <= this.vector4f.w;
    }

    private boolean isLocalState() {
        if (this.clientWorld != mc.world || mc.player == null) {
            this.clearState();
            this.clientWorld = mc.world;
        }
        return this.clientWorld != null && mc.player != null;
    }

    private void clearState() {
        this.removeFirstDamagePopDamageIndicatorEntries.clear();
        this.stateFlag = false;
    }

    private static float calculateFloat(float floatValue) {
        return floatValue * floatValue * (3.0F - 2.0F * floatValue);
    }

    public final static class DamageIndicator {

        final TargetPlayerEvent targetPlayerEvent;

        final LivingEntity target;

        final Vec3d vec3d;

        final long creationMillis;

        final long lifetimeMillis;

        final long lastUpdateTimestampForUpdateCancelledWithHealth;

        String withWeightText = "0 HP";

        float health;

        float absorptionAmount;

        boolean stateFlag = true;

        DamageIndicator(TargetPlayerEvent targetPlayerEvent, Vec3d vec3d, long creationMillis, long lifetimeMillis, long lastUpdateTimestampForUpdateCancelledWithHealth) {
            this.targetPlayerEvent = targetPlayerEvent;
            this.target = (LivingEntity) targetPlayerEvent.getTarget();
            this.vec3d = vec3d;
            this.creationMillis = creationMillis;
            this.lifetimeMillis = lifetimeMillis;
            this.lastUpdateTimestampForUpdateCancelledWithHealth = lastUpdateTimestampForUpdateCancelledWithHealth;
            this.health = HealthUtil.getHealth(this.target);
            this.absorptionAmount = this.target.getAbsorptionAmount();
        }

        void updateToString(float floatValue) {
            float floatValueValue = Math.max(0.1F, Math.round(floatValue * 10.0F) / 10.0F);
            this.withWeightText = (floatValueValue == (int) floatValueValue ? Integer.toString((int) floatValueValue) : Float.toString(floatValueValue)) + " HP";
        }
    }
}
