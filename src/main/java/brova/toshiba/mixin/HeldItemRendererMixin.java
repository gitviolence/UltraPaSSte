package brova.toshiba.mixin;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.modules.impl.render.ShaderHands;
import brova.toshiba.client.modules.impl.render.SwingAnimations;
import brova.toshiba.client.modules.impl.render.ViewModel;
import brova.toshiba.client.utils.render.HandsOverlayRenderer;
import com.google.common.base.MoreObjects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.HeldItemRenderer.HandRenderType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float prevEquipProgressMainHand;

    @Shadow
    private float prevEquipProgressOffHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private ItemStack offHand;

    @Shadow
    protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float floatValue, float floatValueValue, Hand hand, float floatValueResult, ItemStack stack, float floatValueItem, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int intValue);

    @ModifyArg(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V"), index = 5)
    private Arm swapEmptyHandArm(Arm arm) {
        return arm;
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = Shift.AFTER))
    private void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModel viewModel = this.getViewModel();
        if (viewModel != null && viewModel.isEnable()) {
            if (hand == Hand.MAIN_HAND) {
                matrices.translate(viewModel.mainHandX.get(), viewModel.mainHandY.get(), viewModel.mainHandZ.get());
            } else {
                matrices.translate(viewModel.offHandX.get(), viewModel.offHandY.get(), viewModel.offHandZ.get());
            }
        }
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V", ordinal = 2))
    private void onSwingArm(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
        SwingAnimations tweaks = this.getTweaks();
        if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState()) {
            AttackAura aura = ModuleRegistry.INSTANCE != null ? ModuleRegistry.aura : null;
            if (!tweaks.auraTargetOnly.isState() || aura != null && aura.isEnable() && aura.getTarget() != null && aura.getTarget().isAlive()) {
                if (MinecraftClient.getInstance().player != null) {
                    Arm expectedSwingArm = MinecraftClient.getInstance().player.getMainArm();
                    if (arm != expectedSwingArm) {
                        this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
                        return;
                    }
                }
                int i = arm == Arm.RIGHT ? 1 : -1;
                float strength = tweaks.swingStrength.get();
                float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                String current = tweaks.swingType.getCurrent();
                switch(current) {
                    case "1":
                    case "Down":
                        matrices.translate(i * 0.56F, -0.32F, -0.72F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76 * i));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0F * strength));
                        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0F * strength));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0F * strength));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
                        break;
                    case "Static":
                        matrices.translate(i * 0.56F, -0.42F, -0.72F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -60.0F * strength));
                        matrices.translate(0.0, -0.1, 0.0);
                        break;
                    case "2":
                    case "Smooth":
                        this.applySwingOffset(matrices, i, swingProgress, strength);
                        break;
                    case "3":
                    case "Block":
                        if (swingProgress > 0.0F) {
                            float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                            matrices.translate(0.56F * i, equipProgress * -0.2F - 0.5F, -0.7F);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 * i));
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -85.0F * strength));
                            matrices.translate(-0.1F * i, 0.28F, 0.2F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0F));
                        } else {
                            float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                            float m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                            float f1 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
                            matrices.translate(n * i * strength, m * strength, f1 * strength);
                            this.applyEquipOffset(matrices, i, equipProgress);
                            this.applySwingOffset(matrices, i, swingProgress, strength);
                        }
                        break;
                    case "4":
                    case "SelfBack":
                        {
                            float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2.0);
                            matrices.translate(0.65F * i, -0.3F, -0.8F);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 * i));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-70 * i));
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F - 60.0F * strength * anim));
                            break;
                        }
                    case "5":
                    case "DropDown":
                        {
                            float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2.0);
                            this.applyEquipOffset(matrices, i, 0.0F);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(80.0F));
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tweaks.corner.get()));
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-tweaks.slant.get() * anim * strength));
                            break;
                        }
                    default:
                        this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
                }
            } else {
                this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
            }
        } else {
            this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
        }
    }

    @Overwrite
    public void renderItem(float tickDelta, MatrixStack matrices, Immediate vertexConsumers, ClientPlayerEntity player, int light) {
        ShaderHands shaderHands = this.getShaderHands();
        if (shaderHands != null && shaderHands.isEnable()) {
            HandsOverlayRenderer.getInstance().captureBeforeHands();
        }
        float f = player.getHandSwingProgress(tickDelta);
        Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
        float g = player.getLerpedPitch(tickDelta);
        HandRenderType handRenderType = HeldItemRenderer.getHandRenderType(player);
        float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
        if (handRenderType.renderMainHand) {
            float j = hand == Hand.MAIN_HAND ? f : 0.0F;
            float k = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressMainHand, this.equipProgressMainHand);
            this.renderFirstPersonItem(player, tickDelta, g, Hand.MAIN_HAND, j, this.mainHand, k, matrices, vertexConsumers, light);
        }
        if (handRenderType.renderOffHand) {
            float j = hand == Hand.OFF_HAND ? f : 0.0F;
            float k = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressOffHand, this.equipProgressOffHand);
            this.renderFirstPersonItem(player, tickDelta, g, Hand.OFF_HAND, j, this.offHand, k, matrices, vertexConsumers, light);
        }
        vertexConsumers.draw();
        shaderHands = this.getShaderHands();
        if (shaderHands != null && shaderHands.isEnable()) {
            HandsOverlayRenderer.getInstance().captureAfterHands();
        }
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At("HEAD"), cancellable = true)
    private void onApplyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        SwingAnimations tweaks = this.getTweaks();
        if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.eatAnim.isState() && player.isUsingItem()) {
            this.applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, stack);
            ci.cancel();
        }
    }

    private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack) {
        if (MinecraftClient.getInstance().player != null) {
            float f = MinecraftClient.getInstance().player.getItemUseTimeLeft() - tickDelta + 1.0F;
            float g = f / stack.getMaxUseTime(MinecraftClient.getInstance().player);
            if (g < 0.8F) {
                float h = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.005F);
                matrices.translate(0.0F, h, 0.0F);
            }
            float h = 1.0F - (float) Math.pow(g, 27.0);
            int i = arm == Arm.RIGHT ? 1 : -1;
            float offsetX = 0.0F;
            float offsetY = 0.0F;
            float offsetZ = 0.0F;
            ViewModel viewModel = this.getViewModel();
            if (viewModel != null && viewModel.isEnable()) {
                if (arm == Arm.RIGHT) {
                    offsetX = viewModel.mainHandX.get();
                    offsetY = viewModel.mainHandY.get();
                    offsetZ = viewModel.mainHandZ.get();
                } else {
                    offsetX = viewModel.offHandX.get();
                    offsetY = viewModel.offHandY.get();
                    offsetZ = viewModel.offHandZ.get();
                }
            }
            matrices.translate(h * 0.6F * i + offsetX, h * -0.5F + offsetY, offsetZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * h * 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * h * 30.0F));
        }
    }

    private void applyEquipOffset(MatrixStack matrices, int i, float equipProgress) {
        matrices.translate(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    private void applySwingOffset(MatrixStack matrices, int i, float swingProgress, float strength) {
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.translate(0.56F * i, -0.52F, -0.72F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (45.0F + f * -20.0F * strength)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * g * -20.0F * strength));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F * strength));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45.0F));
    }

    private void callSwingArm(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
        ((HeldItemRendererInvoker) instance).whylol$callSwingArm(swingProgress, equipProgress, matrices, armX, arm);
    }

    private SwingAnimations getTweaks() {
        return ModuleRegistry.INSTANCE == null ? null : ModuleRegistry.swingAnimations;
    }

    private ViewModel getViewModel() {
        return ModuleRegistry.INSTANCE == null ? null : ModuleRegistry.viewModel;
    }

    private ShaderHands getShaderHands() {
        return ModuleRegistry.INSTANCE == null ? null : ModuleRegistry.shaderHands;
    }
}
