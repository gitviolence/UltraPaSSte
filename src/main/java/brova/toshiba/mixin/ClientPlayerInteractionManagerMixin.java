package brova.toshiba.mixin;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.modules.impl.player.AutoTool;
import brova.toshiba.client.modules.impl.player.NoInteract;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        try {
            if (player != null && target != null) {
                TargetPlayerEvent event = new TargetPlayerEvent(player, target);
                EventBus.invoke(event);
                if (event.isCancelled()) {
                    ci.cancel();
                }
            }
        } catch (Exception failure) {
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (NoInteract.INSTANCE.isEnable() && NoInteract.INSTANCE.blocks.isState()) {
            Block block = player.getWorld().getBlockState(hitResult.getBlockPos()).getBlock();
            if (NoInteract.INSTANCE.shouldBlockBlock(block)) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() instanceof BlockItem) {
                    ActionResult result = stack.useOnBlock(new ItemUsageContext(player, hand, hitResult));
                    cir.setReturnValue(result);
                } else {
                    cir.setReturnValue(ActionResult.PASS);
                }
            }
        }
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    public void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (NoInteract.INSTANCE.isEnable() && NoInteract.INSTANCE.shouldBlockEntity(entity)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
    public void onInteractEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (NoInteract.INSTANCE.isEnable() && NoInteract.INSTANCE.shouldBlockEntity(entity)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    public void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (AutoTool.INSTANCE != null && AutoTool.INSTANCE.isEnable() && AutoTool.INSTANCE.handleBlockBreaking(pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    public void onUpdateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (AutoTool.INSTANCE != null && AutoTool.INSTANCE.isEnable() && AutoTool.INSTANCE.handleBlockBreaking(pos)) {
            cir.setReturnValue(false);
        }
    }
}
