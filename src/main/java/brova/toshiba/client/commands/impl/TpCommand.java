package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;

public class TpCommand extends CommandBase {

    public TpCommand() {
        super("tp");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.arg("Y", IntegerArgumentType.integer()).executes(input -> {
            int argument = input.getArgument("Y", Integer.class);
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + argument, mc.player.getZ());
            return 1;
        }));
        literalArgumentBuilder.then(this.literal("up").executes(inputValue -> {
            this.applyBlockXWithBlockZ(true);
            return 1;
        }));
        literalArgumentBuilder.then(this.literal("down").executes(inputResult -> {
            this.applyBlockXWithBlockZ(false);
            return 1;
        }));
    }

    private void applyBlockXWithBlockZ(boolean flag) {
        if (mc.player != null && mc.world != null) {
            int blockY = mc.player.getBlockY();
            int bottomY = mc.world.getBottomY();
            int intValue = mc.world.getTopYInclusive() - 2;
            int intValueValue = flag ? 1 : -1;
            int intValueResult = flag ? blockY + 1 : blockY - 1;
            int intValueItem = flag ? intValue : bottomY;
            for (int index = intValueResult; flag ? index <= intValueItem : index >= intValueItem; index += intValueValue) {
                if (this.isBlockStateWithCollisionShape(index)) {
                    VoxelShape collisionShape = mc.world.getBlockState(new BlockPos(mc.player.getBlockX(), index - 1, mc.player.getBlockZ())).getCollisionShape(mc.world, new BlockPos(mc.player.getBlockX(), index - 1, mc.player.getBlockZ()));
                    double doubleValue = collisionShape.isEmpty() ? 0.0 : collisionShape.getMax(Axis.Y);
                    mc.player.setPosition(mc.player.getX(), index + doubleValue, mc.player.getZ());
                    return;
                }
            }
        }
    }

    private boolean isBlockStateWithCollisionShape(int intValue) {
        BlockPos position = new BlockPos(mc.player.getBlockX(), intValue - 1, mc.player.getBlockZ());
        BlockPos positionValue = position.up();
        BlockPos positionResult = positionValue.up();
        BlockState blockState = mc.world.getBlockState(position);
        return blockState.getCollisionShape(mc.world, position).isEmpty() ? false : mc.world.getBlockState(positionValue).getCollisionShape(mc.world, positionValue).isEmpty() && mc.world.getBlockState(positionResult).getCollisionShape(mc.world, positionResult).isEmpty();
    }
}
