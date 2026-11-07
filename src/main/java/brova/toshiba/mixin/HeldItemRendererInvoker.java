package brova.toshiba.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererInvoker {

    @Accessor("mainHand")
    ItemStack whylol$getMainHand();

    @Accessor("offHand")
    ItemStack whylol$getOffHand();

    @Invoker("renderFirstPersonItem")
    void whylol$callRenderFirstPersonItem(AbstractClientPlayerEntity player, float floatValue, float floatValueValue, Hand hand, float floatValueResult, ItemStack stack, float floatValueItem, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue);

    @Invoker("applyEquipOffset")
    void whylol$applyEquipOffset(MatrixStack matrices, Arm arm, float floatValue);

    @Invoker("swingArm")
    void whylol$callSwingArm(float floatValue, float floatValueValue, MatrixStack matrices, int intValue, Arm arm);

    @Invoker("renderArmHoldingItem")
    void whylol$renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue, float floatValue, float floatValueValue, Arm arm);

    @Invoker("renderMapInBothHands")
    void whylol$renderMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue, float floatValue, float floatValueValue, float floatValueResult);

    @Invoker("renderMapInOneHand")
    void whylol$renderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue, float floatValue, Arm arm, float floatValueValue, ItemStack stack);

    @Invoker("renderItem")
    void whylol$renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode modelTransformationMode, boolean flag, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue);
}
