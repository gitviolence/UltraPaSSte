package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PostPlayerTickEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public final class SpinBot extends Module {

    private final BooleanSetting rotateYaw = new BooleanSetting("Крутить Yaw", true);

    private final FloatSetting yawSpeed = new FloatSetting("Скорость Yaw", 45.0F, 15.0F, 360.0F, 1.0F);

    private final BooleanSetting rotatePitch = new BooleanSetting("Крутить Pitch", false);

    private final FloatSetting pitchSpeed = new FloatSetting("Скорость Pitch", 30.0F, 15.0F, 360.0F, 1.0F);

    private final BooleanSetting rotateWithAura = new BooleanSetting("Крутить с Aura", true);

    private ClientPlayerEntity clientPlayerEntity;

    private float startValue;

    private float previousYaw;

    private float pitch;

    private float previousPitch;

    public SpinBot() {
        super("SpinBot", "Визуальное вращение игрока", ModuleCategory.RENDER);
        this.yawSpeed.visible(this.rotateYaw::isState);
        this.pitchSpeed.visible(this.rotatePitch::isState);
        this.addSettings(this.rotateYaw, this.yawSpeed, this.rotatePitch, this.pitchSpeed, this.rotateWithAura);
    }

    @Override
    public void onEnable() {
        this.updatePitch();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.clientPlayerEntity = null;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PostPlayerTickEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.clientPlayerEntity != mc.player) {
                this.updatePitch();
            }
            if (!this.isAlive()) {
                this.updatePitch();
            } else {
                this.startValue = this.rotateYaw.isState() ? MathHelper.wrapDegrees(this.previousYaw) : mc.player.bodyYaw;
                this.previousYaw = this.startValue + (this.rotateYaw.isState() ? this.yawSpeed.get() : 0.0F);
                this.pitch = this.rotatePitch.isState() ? MathHelper.wrapDegrees(this.previousPitch) : mc.player.getPitch();
                this.previousPitch = this.pitch + (this.rotatePitch.isState() ? this.pitchSpeed.get() : 0.0F);
            }
        } else {
            this.clientPlayerEntity = null;
        }
    }

    public void applyRotation(AbstractClientPlayerEntity player, PlayerEntityRenderState playerEntityRenderState, float floatValue) {
        if (this.isEnable() && player == mc.player && this.isAlive()) {
            if (this.clientPlayerEntity != player) {
                this.updatePitch();
            }
            if (this.rotateYaw.isState()) {
                playerEntityRenderState.bodyYaw = MathHelper.lerp(floatValue, this.startValue, this.previousYaw);
                playerEntityRenderState.yawDegrees = 0.0F;
                playerEntityRenderState.applyFlyingRotation = false;
                playerEntityRenderState.flyingRotation = 0.0F;
            }
            if (this.rotatePitch.isState()) {
                playerEntityRenderState.pitch = MathHelper.lerp(floatValue, this.pitch, this.previousPitch);
            }
        }
    }

    private boolean isAlive() {
        return mc.player != null && mc.world != null && mc.player.isAlive() && (this.rotateYaw.isState() || this.rotatePitch.isState()) && (this.rotateWithAura.isState() || !this.isTargetWithAlive());
    }

    private boolean isTargetWithAlive() {
        AttackAura aura = ModuleRegistry.aura;
        if (aura != null && !aura.rotationType.is("БезРотации")) {
            boolean flag = aura.isEnable() || ModuleRegistry.maceAura != null && ModuleRegistry.maceAura.isEnable();
            LivingEntity target = aura.getTarget();
            return flag && target != null && target.isAlive() && !target.isRemoved();
        } else {
            return false;
        }
    }

    private void updatePitch() {
        this.clientPlayerEntity = mc.player;
        this.startValue = this.previousYaw = this.clientPlayerEntity != null ? this.clientPlayerEntity.bodyYaw : 0.0F;
        this.pitch = this.previousPitch = this.clientPlayerEntity != null ? this.clientPlayerEntity.getPitch() : 0.0F;
    }
}
