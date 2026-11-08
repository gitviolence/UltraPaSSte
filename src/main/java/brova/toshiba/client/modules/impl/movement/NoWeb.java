package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.movement.MovementUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class NoWeb extends Module {

    public static NoWeb INSTANCE = new NoWeb();

    public NoWeb() {
        super("NoWeb", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && this.isBoundingBoxWithIterate()) {
            Vec3d ZERO = Vec3d.ZERO;
            if (MovementUtil.hasPlayerMovement()) {
                double doubleValue = 0.35F;
                double[] values = brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] { doubleValue }, (accessReceiver, accessArguments) -> MovementUtil.calculateDirection(((double) accessArguments[0])));
                ZERO = mc.player.getVelocity().add(values[0], 0.0, values[1]);
            }
            double doubleValueValue = 0.0;
            if (mc.options.jumpKey.isPressed()) {
                doubleValueValue = 0.65F;
            } else if (mc.options.sneakKey.isPressed()) {
                doubleValueValue = -0.65F;
            }
            mc.player.setVelocity(ZERO.x, doubleValueValue, ZERO.z);
        }
    }

    private boolean isBoundingBoxWithIterate() {
        Box boundingBox = mc.player.getBoundingBox();
        for (BlockPos position : BlockPos.iterate(MathHelper.floor(boundingBox.minX), MathHelper.floor(boundingBox.minY), MathHelper.floor(boundingBox.minZ), MathHelper.floor(boundingBox.maxX), MathHelper.floor(boundingBox.maxY), MathHelper.floor(boundingBox.maxZ))) {
            if (mc.world.getBlockState(position).isOf(Blocks.COBWEB)) {
                return true;
            }
        }
        return false;
    }
}
