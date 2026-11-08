package brova.toshiba.mixin;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.Cancellable;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.CloseHandledScreenEvent;
import brova.toshiba.client.event.impl.MovePositionEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.PostMotionEvent;
import brova.toshiba.client.event.impl.PostPlayerTickEvent;
import brova.toshiba.client.event.impl.PostPlayerUpdateEvent;
import brova.toshiba.client.event.impl.SprintKeyEvent;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.utils.network.ProtocolVersionUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements MinecraftAccess {

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Unique
    private boolean toshiba$tickLock = false;

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    public abstract void closeScreen();

    @Shadow
    protected abstract void sendMovementPackets();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", ordinal = 0, shift = Shift.AFTER), cancellable = true)
    private void onPostPlayerUpdate(CallbackInfo info) {
        if (!this.toshiba$tickLock) {
            if (EventBus.hasListeners(PostPlayerUpdateEvent.class)) {
                PostPlayerUpdateEvent event = new PostPlayerUpdateEvent();
                event.call();
                if (event.isCancelled()) {
                    info.cancel();
                    if (event.getIterations() > 0) {
                        for (int i = 0; i < event.getIterations(); i++) {
                            this.toshiba$tickLock = true;
                            this.tick();
                            this.toshiba$tickLock = false;
                            this.sendMovementPackets();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
    private void onTick(CallbackInfo ci) {
        if (EventBus.hasListeners(PlayerUpdateEvent.class)) {
            new PlayerUpdateEvent().call();
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
    private void onTickPost(CallbackInfo ci) {
        if (EventBus.hasListeners(PostPlayerTickEvent.class)) {
            new PostPlayerTickEvent().call();
        }
        if (this.shouldSyncRotation()) {
            this.headYaw = this.getYaw();
            this.prevHeadYaw = this.getYaw();
            this.bodyYaw = this.getYaw();
            this.prevBodyYaw = this.getYaw();
        }
    }

    @Unique
    private boolean shouldSyncRotation() {
        return false;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 1), require = 0)
    private boolean onSprintKeyPressed(KeyBinding instance) {
        if (!ProtocolVersionUtil.isTargetProtocolBelowOneNineteen() || !this.horizontalCollision && !this.collidedSoftly) {
            if (!EventBus.hasListeners(SprintKeyEvent.class)) {
                return instance.isPressed();
            } else {
                SprintKeyEvent event = new SprintKeyEvent();
                event.call();
                return event.isCancelled() ? false : instance.isPressed();
            }
        } else {
            return false;
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    private boolean onSlowDownRedirect(ClientPlayerEntity player) {
        if (player.isUsingItem()) {
            if (!EventBus.hasListeners(Cancellable.class)) {
                return player.isUsingItem() && player.getVehicle() == null;
            } else {
                Cancellable event = new Cancellable();
                event.call();
                return player.isUsingItem() && player.getVehicle() == null && !event.isCancelled();
            }
        } else {
            return player.isUsingItem() && player.getVehicle() == null;
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        if (ModuleRegistry.noPush.isEnable() && ModuleRegistry.noPush.getCollisionList().is("Блоки")) {
            ci.cancel();
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMoveHook(MovementType movementType, Vec3d movement, @NotNull CallbackInfo ci) {
        if (EventBus.hasListeners(MovePositionEvent.class)) {
            MovePositionEvent event = new MovePositionEvent(movement);
            event.call();
            if (event.isCancelled() || !event.getMovePos().equals(movement)) {
                if (event.isCancelled()) {
                    ci.cancel();
                } else {
                    double d = this.getX();
                    double e = this.getZ();
                    super.move(movementType, event.getMovePos());
                    float f = (float) Math.sqrt(Math.pow(this.getX() - d, 2.0) + Math.pow(this.getZ() - e, 2.0));
                    this.updateLimbs(f);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
    private void onCloseHandledScreen(CallbackInfo ci) {
        if (EventBus.hasListeners(CloseHandledScreenEvent.class)) {
            int syncId = this.currentScreenHandler.syncId;
            CloseHandledScreenEvent event = new CloseHandledScreenEvent(syncId);
            event.call();
            if (!event.isCancelled()) {
                this.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
            }
            this.closeScreen();
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void onSendMovementPacketsPost(CallbackInfo ci) {
        if (EventBus.hasListeners(PostMotionEvent.class)) {
            new PostMotionEvent().call();
        }
    }
}
