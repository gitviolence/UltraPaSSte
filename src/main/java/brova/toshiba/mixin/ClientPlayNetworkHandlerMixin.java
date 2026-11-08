package brova.toshiba.mixin;

import brova.toshiba.Toshiba;
import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.TotemPopEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.player.CommandFix;
import brova.toshiba.client.modules.impl.player.PvPSave;
import brova.toshiba.client.utils.network.BotManager;
import brova.toshiba.client.utils.text.ChatUtil;
import brova.toshiba.client.utils.world.BaritoneController;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    private void toshiba$onTotemPop(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (this.world != null && packet.getStatus() == 35 && EventBus.hasListeners(TotemPopEvent.class) && packet.getEntity(this.world) instanceof PlayerEntity player) {
            new TotemPopEvent(player).call();
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(@NotNull String message, CallbackInfo ci) {
        if (PanicController.isActive()) {
            String prefix = Toshiba.INSTANCE.commandStorage.getPrefix();
            if (prefix != null && !prefix.isEmpty() && message.startsWith(prefix)) {
                ci.cancel();
            }
        } else {
            if (CommandFix.INSTANCE != null && CommandFix.INSTANCE.isEnable()) {
                String fixed = CommandFix.INSTANCE.fixCommandName(message);
                if (fixed.startsWith("/")) {
                    ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
                    if (handler != null) {
                        handler.sendChatCommand(fixed.substring(1));
                    }
                    ci.cancel();
                    return;
                }
                if (fixed.startsWith(Toshiba.INSTANCE.commandStorage.getPrefix())) {
                    String body = fixed.substring(Toshiba.INSTANCE.commandStorage.getPrefix().length());
                    try {
                        Toshiba.INSTANCE.commandStorage.getDispatcher().execute(body, Toshiba.INSTANCE.commandStorage.getSource());
                    } catch (CommandSyntaxException failure) {
                        ChatUtil.sendMessage(Formatting.RED + "Ошибка в использовании!");
                    }
                    ci.cancel();
                    return;
                }
            }
            String prefix = Toshiba.INSTANCE.commandStorage.getPrefix();
            if (message.startsWith(prefix)) {
                String body = message.substring(prefix.length());
                try {
                    Toshiba.INSTANCE.commandStorage.getDispatcher().execute(body, Toshiba.INSTANCE.commandStorage.getSource());
                } catch (CommandSyntaxException failureValue) {
                    ChatUtil.sendMessage(Formatting.RED + "Ошибка в использовании!");
                }
                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "sendChatCommand", at = @At("HEAD"), argsOnly = true)
    private String fixChatCommand(String command) {
        return CommandFix.INSTANCE != null && CommandFix.INSTANCE.isEnable() ? CommandFix.INSTANCE.fixCommandName(command) : command;
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void toshiba$protectHubCommand(String command, CallbackInfo ci) {
        PvPSave pvpSave = ModuleRegistry.pvpSave;
        if (pvpSave != null && pvpSave.shouldBlockHub(command)) {
            ci.cancel();
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (EventBus.hasListeners(EventPacket.class)) {
            EventPacket event = new EventPacket(packet, Type.RECEIVE);
            event.call();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        if (EventBus.hasListeners(EventPacket.class)) {
            EventPacket event = new EventPacket(packet, Type.RECEIVE);
            event.call();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onEntityPositionSync", at = @At("HEAD"), cancellable = true)
    private void onEntityPositionSync(EntityPositionSyncS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (this.world == null || mc.player == null || mc.world == null) {
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        BaritoneController.onGameMessage(packet.content().getString());
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        BotManager.finishBotConnectStage();
    }

    @Inject(method = "onBossBar", at = @At("HEAD"), cancellable = true)
    private void onBossBar(BossBarS2CPacket packet, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, MinecraftClient.getInstance());
        try {
            MinecraftClient.getInstance().inGameHud.getBossBarHud().handlePacket(packet);
        } catch (NullPointerException failure) {
        }
    }
}
