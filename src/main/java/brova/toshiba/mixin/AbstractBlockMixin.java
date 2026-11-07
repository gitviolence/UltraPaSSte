package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBase;
import brova.toshiba.client.event.impl.BlockShapeEvent;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        EventBase event = new BlockShapeEvent(pos);
        event.call();
        if (event.isCancelled()) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        EventBase event = new BlockShapeEvent(pos);
        event.call();
        if (event.isCancelled()) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }

    @Inject(method = "getRaycastShape", at = @At("HEAD"), cancellable = true)
    public void getRaycastShape(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        EventBase event = new BlockShapeEvent(pos);
        event.call();
        if (event.isCancelled()) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }
}
