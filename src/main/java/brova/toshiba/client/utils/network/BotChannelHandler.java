package brova.toshiba.client.utils.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class BotChannelHandler extends ChannelDuplexHandler {

    public final ClientPlayNetworkHandler clientPlayNetworkHandler;

    public final ClientPlayerEntity clientPlayerEntity;

    public final String cachedText;

    public BotChannelHandler(ClientPlayNetworkHandler clientPlayNetworkHandler, ClientPlayerEntity clientPlayerEntity, String cachedText) {
        this.clientPlayNetworkHandler = clientPlayNetworkHandler;
        this.clientPlayerEntity = clientPlayerEntity;
        this.cachedText = cachedText;
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object value) throws Exception {
        if (value instanceof KeepAliveS2CPacket keepAliveS2CPacket) {
            this.clientPlayNetworkHandler.getConnection().send(new KeepAliveC2SPacket(keepAliveS2CPacket.getId()));
            if (this.clientPlayerEntity != null) {
                this.clientPlayNetworkHandler.sendPacket(new OnGroundOnly(this.clientPlayerEntity.isOnGround(), this.clientPlayerEntity.horizontalCollision));
            }
            ReferenceCountUtil.release(value);
        } else if (value instanceof CommonPingS2CPacket commonPingS2CPacket) {
            this.clientPlayNetworkHandler.getConnection().send(new CommonPongC2SPacket(commonPingS2CPacket.getParameter()));
            ReferenceCountUtil.release(value);
        } else if (value instanceof ResourcePackSendS2CPacket resourcePackSendS2CPacket) {
            this.clientPlayNetworkHandler.sendPacket(new ResourcePackStatusC2SPacket(resourcePackSendS2CPacket.id(), Status.ACCEPTED));
            this.clientPlayNetworkHandler.sendPacket(new ResourcePackStatusC2SPacket(resourcePackSendS2CPacket.id(), Status.SUCCESSFULLY_LOADED));
            ReferenceCountUtil.release(value);
        } else if (value instanceof PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
            BotManager.applyYawWithPitch(this.clientPlayerEntity, playerPositionLookS2CPacket);
            this.clientPlayNetworkHandler.sendPacket(new TeleportConfirmC2SPacket(playerPositionLookS2CPacket.teleportId()));
            if (this.clientPlayerEntity != null) {
                this.clientPlayNetworkHandler.sendPacket(new Full(this.clientPlayerEntity.getX(), this.clientPlayerEntity.getY(), this.clientPlayerEntity.getZ(), this.clientPlayerEntity.getYaw(), this.clientPlayerEntity.getPitch(), this.clientPlayerEntity.isOnGround(), this.clientPlayerEntity.horizontalCollision));
            }
            ReferenceCountUtil.release(value);
        } else if (value instanceof EntityPositionSyncS2CPacket entityPositionSyncS2CPacket) {
            BotManager.applyValuesWithDeltaMovement(this.clientPlayerEntity, entityPositionSyncS2CPacket);
            ReferenceCountUtil.release(value);
        } else if (value instanceof HealthUpdateS2CPacket healthUpdateS2CPacket) {
            if (this.clientPlayerEntity != null) {
                this.clientPlayerEntity.setHealth(healthUpdateS2CPacket.getHealth());
            }
            ReferenceCountUtil.release(value);
        } else if (value instanceof DisconnectS2CPacket) {
            BotManager.connections.removeIf(input -> BotManager.isEqualsIgnoreCase(input.name(), this.cachedText));
            channelHandlerContext.close();
            ReferenceCountUtil.release(value);
        } else {
            String simpleName = value.getClass().getSimpleName();
            if (!simpleName.contains("Sound") && !simpleName.contains("Particle") && !simpleName.contains("Screen") && (!BotManager.ignoreBotMessages || !BotManager.isText(simpleName)) && !simpleName.contains("Explosion") && !simpleName.contains("BossBar") && !simpleName.contains("Scoreboard") && !simpleName.contains("OverlayMessage")) {
                super.channelRead(channelHandlerContext, value);
            } else {
                ReferenceCountUtil.release(value);
            }
        }
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        BotManager.connections.removeIf(input -> BotManager.isEqualsIgnoreCase(input.name(), this.cachedText));
        super.channelInactive(channelHandlerContext);
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable failure) throws Exception {
        BotManager.connections.removeIf(input -> BotManager.isEqualsIgnoreCase(input.name(), this.cachedText));
        channelHandlerContext.close();
    }
}
