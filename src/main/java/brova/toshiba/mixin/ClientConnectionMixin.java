package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.utils.network.PacketUtil;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (EventBus.hasListeners(EventPacket.class)) {
            EventPacket eventReceive = new EventPacket(packet, Type.RECEIVE);
            try {
                EventBus.invoke(eventReceive);
            } catch (Throwable failure) {
                Throwable cause = failure instanceof InvocationTargetException ite ? ite.getCause() : failure;
                if (!(cause instanceof OffThreadException)) {
                    System.err.println("[Toshiba] EventPacket RECEIVE handler error: " + cause);
                }
                return;
            }
            if (eventReceive.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet<?> packet, CallbackInfo ci) {
        if (PacketUtil.getSilentPackets().contains(packet)) {
            PacketUtil.getSilentPackets().remove(packet);
        } else if (EventBus.hasListeners(EventPacket.class)) {
            EventPacket eventSend = new EventPacket(packet, Type.SEND);
            try {
                EventBus.invoke(eventSend);
            } catch (Throwable failure) {
                Throwable cause = failure instanceof InvocationTargetException ite ? ite.getCause() : failure;
                if (!(cause instanceof OffThreadException)) {
                    System.err.println("[Toshiba] EventPacket SEND handler error: " + cause);
                }
                return;
            }
            if (eventSend.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
