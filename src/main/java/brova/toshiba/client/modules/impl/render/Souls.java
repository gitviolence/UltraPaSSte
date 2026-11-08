package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ClientTickEndEvent;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.event.impl.TotemPopEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.Souls.SoulSnapshot;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.utils.render.animation.EasingFunctions;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class Souls extends Module {

    public static Souls INSTANCE = new Souls();

    private final BooleanSetting onDeath = new BooleanSetting("При смерти", true);

    private final BooleanSetting onTotemPop = new BooleanSetting("При потери тотема", true);

    private final List<SoulSnapshot> removeIfSoulsSoulSnapshotEntries = new ArrayList<>();

    private final Map<Model, PlayerEntityModel> ifAbsentPlayerEntityModelByModel = new EnumMap<>(Model.class);

    private ClientWorld clientWorld;

    private PlayerEntity playerEntity;

    private long lastUpdateNanos;

    private LoadedEntityModels loadedEntityModels;

    public Souls() {
        super("Souls", "Вылет души игрока при смерти и потери тотема", ModuleCategory.RENDER);
        this.addSettings(this.onDeath, this.onTotemPop);
    }

    @Override
    public void onEnable() {
        this.clearState();
        this.clientWorld = mc.world;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.clearState();
        this.clientWorld = null;
        super.onDisable();
    }

    @EventHandler(priority = -200)
    public void onAttack(TargetPlayerEvent event) {
        if (this.isLocalState() && this.onDeath.isState() && !event.isCancelled() && event.getPlayer() == mc.player && event.getTarget() instanceof PlayerEntity playerEntity && playerEntity != mc.player) {
            this.playerEntity = playerEntity;
            this.lastUpdateNanos = System.nanoTime();
        }
    }

    @EventHandler
    public void onTotemPop(TotemPopEvent event) {
        if (this.isLocalState() && this.onTotemPop.isState()) {
            this.updatePartVisibleWithMainArm(event.getPlayer(), System.nanoTime());
        }
    }

    @EventHandler
    public void onTick(ClientTickEndEvent event) {
        if (this.isLocalState()) {
            long currentTimeNanos = System.nanoTime();
            this.removeIfSoulsSoulSnapshotEntries.removeIf(input -> currentTimeNanos - input.createdAt() >= 1600000000L);
            if (!this.onDeath.isState() || currentTimeNanos - this.lastUpdateNanos > 3000000000L) {
                this.playerEntity = null;
            } else if (this.playerEntity != null && (this.playerEntity.getHealth() <= 0.0F || this.playerEntity.deathTime > 0)) {
                this.updatePartVisibleWithMainArm(this.playerEntity, currentTimeNanos);
                this.playerEntity = null;
            }
        }
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
        if (this.isLocalState() && !this.removeIfSoulsSoulSnapshotEntries.isEmpty()) {
            long currentTimeNanos = System.nanoTime();
            this.removeIfSoulsSoulSnapshotEntries.removeIf(input -> currentTimeNanos - input.createdAt() >= 1600000000L);
            Vec3d pos = event.getCamera().getPos();
            this.removeIfSoulsSoulSnapshotEntries.sort(Comparator.<SoulSnapshot>comparingDouble(inputValue -> inputValue.position().squaredDistanceTo(pos)).reversed());
            MatrixStack matrices = event.getMatrices();
            Immediate entityVertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            for (SoulSnapshot soulSnapshot : this.removeIfSoulsSoulSnapshotEntries) {
                float clampedValue = MathHelper.clamp((float) (currentTimeNanos - soulSnapshot.createdAt()) / 1.6E9F, 0.0F, 1.0F);
                float floatValue = EasingFunctions.easeOutCubic(clampedValue);
                int intValue = Math.round(255.0F * calculateFloat(clampedValue));
                if (intValue != 0) {
                    PlayerEntityRenderState playerEntityRenderState = soulSnapshot.state();
                    PlayerEntityModel playerEntityModel = this.createLoadedEntityModelsWithIfAbsent(playerEntityRenderState.skinTextures.model());
                    playerEntityModel.setAngles(playerEntityRenderState);
                    RenderLayer entityTranslucent = RenderLayer.getEntityTranslucent(playerEntityRenderState.skinTextures.texture(), false);
                    matrices.push();
                    try {
                        matrices.translate(soulSnapshot.position().x - pos.x, soulSnapshot.position().y + floatValue * 1.8F - pos.y - (playerEntityRenderState.sneaking ? 0.125 : 0.0), soulSnapshot.position().z - pos.z);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - soulSnapshot.baseYaw() - 180.0F * floatValue));
                        matrices.scale(-0.9375F, -0.9375F, 0.9375F);
                        matrices.translate(0.0F, -1.501F, 0.0F);
                        playerEntityModel.render(matrices, entityVertexConsumers.getBuffer(entityTranslucent), 15728880, OverlayTexture.DEFAULT_UV, intValue << 24 | 16777215);
                        entityVertexConsumers.draw(entityTranslucent);
                    } finally {
                        matrices.pop();
                    }
                }
            }
        }
    }

    private void updatePartVisibleWithMainArm(PlayerEntity player, long longValue) {
        if (player instanceof AbstractClientPlayerEntity playerValue && player != mc.player && player.getWorld() == this.clientWorld) {
            PlayerEntityRenderState playerEntityRenderState = new PlayerEntityRenderState();
            playerEntityRenderState.skinTextures = playerValue.getSkinTextures();
            playerEntityRenderState.mainArm = player.getMainArm();
            playerEntityRenderState.preferredArm = player.getMainArm();
            playerEntityRenderState.isInSneakingPose = player.isInSneakingPose();
            playerEntityRenderState.sneaking = player.isSneaking();
            playerEntityRenderState.hatVisible = player.isPartVisible(PlayerModelPart.HAT);
            playerEntityRenderState.jacketVisible = player.isPartVisible(PlayerModelPart.JACKET);
            playerEntityRenderState.leftSleeveVisible = player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            playerEntityRenderState.rightSleeveVisible = player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
            playerEntityRenderState.leftPantsLegVisible = player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            playerEntityRenderState.rightPantsLegVisible = player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            if (this.removeIfSoulsSoulSnapshotEntries.size() >= 24) {
                this.removeIfSoulsSoulSnapshotEntries.removeFirst();
            }
            this.removeIfSoulsSoulSnapshotEntries.add(new SoulSnapshot(player.getPos().add(0.0, 0.1, 0.0), longValue, playerEntityRenderState, player.getYaw()));
        }
    }

    private PlayerEntityModel createLoadedEntityModelsWithIfAbsent(Model model) {
        LoadedEntityModels loadedEntityModels = mc.getLoadedEntityModels();
        if (this.loadedEntityModels != loadedEntityModels) {
            this.loadedEntityModels = loadedEntityModels;
            this.ifAbsentPlayerEntityModelByModel.clear();
        }
        return this.ifAbsentPlayerEntityModelByModel.computeIfAbsent(model, input -> {
            boolean flag = input == Model.SLIM;
            return new PlayerEntityModel(this.loadedEntityModels.getModelPart(flag ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), flag);
        });
    }

    private boolean isLocalState() {
        if (this.clientWorld != mc.world || mc.player == null) {
            this.clearState();
            this.clientWorld = mc.world;
        }
        return this.clientWorld != null && mc.player != null;
    }

    private void clearState() {
        this.removeIfSoulsSoulSnapshotEntries.clear();
        this.playerEntity = null;
        this.lastUpdateNanos = 0L;
        this.loadedEntityModels = null;
        this.ifAbsentPlayerEntityModelByModel.clear();
    }

    private static float calculateFloat(float floatValue) {
        float clampedValue = MathHelper.clamp((floatValue - 0.62F) / 0.32F, 0.0F, 1.0F);
        float floatValueValue = clampedValue * clampedValue * clampedValue * (clampedValue * (clampedValue * 6.0F - 15.0F) + 10.0F);
        return 0.65F * (1.0F - floatValueValue);
    }

    public static record SoulSnapshot(Vec3d position, long createdAt, PlayerEntityRenderState state, float baseYaw) {
    }
}
