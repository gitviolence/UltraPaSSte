package brova.toshiba.mixin;

import net.minecraft.client.MinecraftClient;
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
public interface HeldItemRendererAccessor {

    @Accessor("mainHand")
    ItemStack getMainHand();

    @Accessor("offHand")
    ItemStack getOffHand();

    @Accessor("equipProgressMainHand")
    float getEquipProgressMainHand();

    @Accessor("prevEquipProgressMainHand")
    float getPrevEquipProgressMainHand();

    @Accessor("equipProgressOffHand")
    float getEquipProgressOffHand();

    @Accessor("prevEquipProgressOffHand")
    float getPrevEquipProgressOffHand();

    @Accessor("client")
    MinecraftClient getClient();

    @Invoker("renderFirstPersonItem")
    void invokeRenderFirstPersonItem(AbstractClientPlayerEntity player, float floatValue, float floatValueValue, Hand hand, float floatValueResult, ItemStack stack, float floatValueItem, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue);

    @Invoker("applyEquipOffset")
    void invokeApplyEquipOffset(MatrixStack matrices, Arm arm, float floatValue);

    @Invoker("swingArm")
    void invokeSwingArm(float floatValue, float floatValueValue, MatrixStack matrices, int intValue, Arm arm);

    @Invoker("renderArmHoldingItem")
    void invokeRenderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue, float floatValue, float floatValueValue, Arm arm);

    @Invoker("renderMapInBothHands")
    void invokeRenderMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue, float floatValue, float floatValueValue, float floatValueResult);

    @Invoker("renderMapInOneHand")
    void invokeRenderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue, float floatValue, Arm arm, float floatValueValue, ItemStack stack);

    @Invoker("applySwingOffset")
    void invokeApplySwingOffset(MatrixStack matrices, Arm arm, float floatValue);

    @Invoker("renderItem")
    void invokeRenderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode modelTransformationMode, boolean flag, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue);
}
