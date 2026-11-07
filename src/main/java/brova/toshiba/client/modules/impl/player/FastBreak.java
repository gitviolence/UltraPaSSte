package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class FastBreak extends Module {

    public static FastBreak INSTANCE = new FastBreak();

    private final FloatSetting speed = new FloatSetting("Ускорение", 1.0F, 0.1F, 1.0F, 0.05F);

    public FastBreak() {
        super("FastBreak", "Ускоряет ломание блоков", ModuleCategory.PLAYER);
        this.addSettings(this.speed);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null && mc.crosshairTarget instanceof BlockHitResult blockHitResult && mc.options.attackKey.isPressed()) {
            accelerateClientBreak(mc.interactionManager, mc.player, mc.world, blockHitResult.getBlockPos(), blockHitResult.getSide(), this.speed.get(), true);
        }
    }

    public float getSpeed() {
        return this.speed.get();
    }

    public static int getExtraTicks(float floatValue) {
        return Math.max(1, Math.round(Math.max(0.3F, floatValue) / 0.35F));
    }

    public static boolean accelerateClientBreak(ClientPlayerInteractionManager clientPlayerInteractionManager, ClientPlayerEntity player, ClientWorld world, BlockPos position, Direction direction, float floatValue, boolean flag) {
        if (clientPlayerInteractionManager != null && player != null && world != null && position != null) {
            BlockState blockState = world.getBlockState(position);
            if (blockState != null && !blockState.isAir()) {
                Direction directionValue = direction == null ? Direction.UP : direction;
                int extraTicks = getExtraTicks(floatValue);
                for (int index = 0; index < extraTicks; index++) {
                    clientPlayerInteractionManager.updateBlockBreakingProgress(position, directionValue);
                }
                if (flag) {
                    player.swingHand(Hand.MAIN_HAND);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean packetBreak(ClientPlayNetworkHandler clientPlayNetworkHandler, ClientPlayerEntity player, BlockPos position, Direction direction, boolean flag) {
        if (clientPlayNetworkHandler != null && player != null && position != null) {
            Direction directionValue = direction == null ? Direction.UP : direction;
            clientPlayNetworkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, position, directionValue));
            clientPlayNetworkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, position, directionValue));
            if (flag) {
                clientPlayNetworkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            return true;
        } else {
            return false;
        }
    }
}
