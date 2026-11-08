package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AppleFarmer extends Module {

    public static AppleFarmer INSTANCE = new AppleFarmer();

    public AppleFarmer() {
        super("AppleFarmer", "Автоматически фармит яблоки", ModuleCategory.MISC);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            BlockPos position = this.createBlockPosWithTest(5.0, input -> input.isIn(BlockTags.LEAVES), this::calculateSubtractWithEyePos);
            if (position != null) {
                this.interactWithTree(position, inputValue -> inputValue.getItem() instanceof HoeItem);
            } else {
                BlockPos positionValue = this.createBlockPosWithTest(5.0, inputResult -> inputResult.isIn(BlockTags.LOGS), inputItem -> inputItem.getY());
                if (positionValue != null) {
                    this.interactWithTree(positionValue, inputEntry -> inputEntry.getItem() instanceof AxeItem);
                } else if (mc.currentScreen instanceof GenericContainerScreen) {
                    this.lootContainer();
                } else if (InventoryUtil.getItemCount(Items.BONE) > 0 && InventoryUtil.getItemCount(Items.BONE_MEAL) == 0) {
                    this.craftBoneMeal();
                } else if (!this.isHarvestInventoryFull() && !this.needsFarmingSupplies()) {
                    BlockPos positionResult = this.createBlockStateWithBlockPos(16.0);
                    if (positionResult != null) {
                        if (InventoryUtil.getItemCount(Items.STICK) <= 128 && InventoryUtil.getItemCount(Items.OAK_SAPLING) <= 128) {
                            this.selectTreeAction(positionResult);
                        } else {
                            this.updateStackWithOf();
                        }
                    }
                } else {
                    this.lootContainer();
                }
            }
        }
    }

    private void interactWithTree(BlockPos position, Predicate<ItemStack> predicate) {
        if (!predicate.test(mc.player.getMainHandStack())) {
            this.isInventoryWithStack(predicate);
        } else {
            Vec3d positionValue = position.toCenterPos();
            if (this.isEyePos(positionValue)) {
                Vec3d positionResult = new Box(position).raycast(mc.player.getEyePos(), positionValue).orElse(positionValue);
                mc.interactionManager.updateBlockBreakingProgress(position, Direction.getFacing(positionResult.subtract(positionValue)));
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private void selectTreeAction(BlockPos position) {
        boolean flag = mc.world.getBlockState(position.up()).getBlock() instanceof SaplingBlock;
        Predicate<ItemStack> predicate = flag ? input -> input.isOf(Items.BONE_MEAL) : inputValue -> {
            if (!inputValue.isOf(Items.OAK_SAPLING) && !inputValue.isOf(Items.BIRCH_SAPLING) && !inputValue.isOf(Items.SPRUCE_SAPLING) && !inputValue.isOf(Items.JUNGLE_SAPLING) && !inputValue.isOf(Items.ACACIA_SAPLING) && !inputValue.isOf(Items.DARK_OAK_SAPLING) && !inputValue.isOf(Items.CHERRY_SAPLING) && !inputValue.isOf(Items.MANGROVE_PROPAGULE)) {
                return inputValue.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() instanceof SaplingBlock : false;
            } else {
                return true;
            }
        };
        if (!predicate.test(mc.player.getMainHandStack())) {
            this.isInventoryWithStack(predicate);
        } else {
            Vec3d positionValue = new Vec3d(position.getX() + 0.5, position.getY() + 1, position.getZ() + 0.5);
            if (this.isEyePos(positionValue)) {
                BlockPos positionResult = flag ? position.up() : position;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(positionValue, Direction.UP, positionResult, false));
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private void lootContainer() {
        if (mc.currentScreen instanceof GenericContainerScreen genericContainerScreen) {
            ScreenHandler screenHandler = genericContainerScreen.getScreenHandler();
            boolean flag;
            if (genericContainerScreen.getTitle().getContent() instanceof TranslatableTextContent translatableTextContent) {
                flag = translatableTextContent.getKey().contains("chest");
            } else {
                String normalizedText = genericContainerScreen.getTitle().getString().toLowerCase(Locale.ROOT);
                flag = normalizedText.contains("chest") || normalizedText.contains("сундук");
            }
            int intValue;
            if (flag) {
                intValue = this.calculateTestWithStack(screenHandler, false, 36, input -> input.isOf(Items.APPLE) || input.isOf(Items.OAK_LOG));
            } else {
                intValue = this.needsFarmingSupplies() ? this.calculateTestWithStack(screenHandler, true, 36, this::isOfWithItemCount) : 0;
            }
            if (intValue == 0) {
                mc.player.closeHandledScreen();
            }
        } else {
            this.updateToCenterPosWithOrElse(this.createBlockPosWithTest(5.0, inputValue -> inputValue.isOf(this.isHarvestInventoryFull() ? Blocks.CHEST : Blocks.BARREL), this::squaredDistanceToBlock));
        }
    }

    private void craftBoneMeal() {
        PlayerScreenHandler playerScreenHandler = mc.player.playerScreenHandler;
        if (playerScreenHandler.getSlot(0).getStack().isOf(Items.BONE_MEAL)) {
            this.clickSlot(playerScreenHandler, 0, 0, SlotActionType.QUICK_MOVE);
            mc.player.closeHandledScreen();
        } else {
            int intValue = this.calculateOfWithStack(Items.BONE);
            if (intValue >= 0) {
                this.clickSlot(playerScreenHandler, intValue, 0, SlotActionType.PICKUP);
                this.clickSlot(playerScreenHandler, 1, 0, SlotActionType.PICKUP);
                if (playerScreenHandler.getSlot(0).getStack().isOf(Items.BONE_MEAL)) {
                    this.clickSlot(playerScreenHandler, 0, 0, SlotActionType.QUICK_MOVE);
                    mc.player.closeHandledScreen();
                }
            }
        }
    }

    private void updateStackWithOf() {
        float floatValue = this.calculateSin();
        this.applyQuantizedRotation(new Rotation(floatValue * 10.0F, MathHelper.clamp(floatValue / 4.0F, -30.0F, 30.0F)));
        PlayerScreenHandler playerScreenHandler = mc.player.playerScreenHandler;
        int intValue = -1;
        int intValueValue = -1;
        for (Slot slot : playerScreenHandler.slots) {
            if (this.isPlayerInventorySlot(slot) && slot.getStack().isOf(Items.OAK_SAPLING) && slot.getStack().getCount() > intValueValue) {
                intValueValue = slot.getStack().getCount();
                intValue = slot.id;
            }
        }
        for (Slot slotValue : playerScreenHandler.slots) {
            if (this.isPlayerInventorySlot(slotValue) && (slotValue.getStack().isOf(Items.STICK) || slotValue.getStack().isOf(Items.OAK_SAPLING) && slotValue.id != intValue)) {
                this.clickSlot(playerScreenHandler, slotValue.id, 1, SlotActionType.THROW);
            }
        }
    }

    private void updateToCenterPosWithOrElse(BlockPos position) {
        if (position != null && this.isEyePos(position.toCenterPos())) {
            Vec3d positionValue = position.toCenterPos();
            Vec3d positionResult = new Box(position).raycast(mc.player.getEyePos(), positionValue).orElse(positionValue);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(positionResult, Direction.getFacing(positionResult.subtract(positionValue)), position, false));
        }
    }

    private boolean isInventoryWithStack(Predicate<ItemStack> predicate) {
        if (mc.player == null) {
            return false;
        } else {
            for (int index = 0; index < 9; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (predicate.test(stack)) {
                    this.selectHotbarSlot(index);
                    return true;
                }
            }
            for (int indexValue = 9; indexValue < 36; indexValue++) {
                ItemStack stackValue = mc.player.getInventory().getStack(indexValue);
                if (predicate.test(stackValue)) {
                    int intValue = IntStream.range(0, 9).filter(input -> mc.player.getInventory().getStack(input).isEmpty()).findFirst().orElse(mc.player.getInventory().selectedSlot);
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, indexValue, intValue, SlotActionType.SWAP, mc.player);
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
                    this.selectHotbarSlot(intValue);
                    return true;
                }
            }
            return false;
        }
    }

    private void selectHotbarSlot(int selectedSlot) {
        if (mc.player != null && selectedSlot >= 0 && selectedSlot <= 8 && mc.player.getInventory().selectedSlot != selectedSlot) {
            mc.player.getInventory().selectedSlot = selectedSlot;
            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            }
        }
    }

    private boolean isEyePos(Vec3d position) {
        Rotation rotation = this.calculateRotations(mc.player.getEyePos(), position);
        this.applyQuantizedRotation(rotation);
        return true;
    }

    private void applyQuantizedRotation(Rotation rotation) {
        float gCDValue = MouseGcdUtil.getGCDValue();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        float floatValue = MathHelper.wrapDegrees(rotation.getYaw() + -yaw);
        float floatValueValue = rotation.getPitch() + -pitch;
        int intValue = Math.round(floatValue / gCDValue);
        int intValueValue = Math.round(floatValueValue / gCDValue);
        mc.player.setYaw(yaw + intValue * gCDValue);
        mc.player.setPitch(MathHelper.clamp(pitch + intValueValue * gCDValue, -90.0F, 90.0F));
    }

    private Rotation calculateRotations(Vec3d position, Vec3d positionValue) {
        Vec3d positionResult = positionValue.subtract(position);
        double squareRoot = Math.sqrt(positionResult.x * positionResult.x + positionResult.z * positionResult.z);
        float floatValue = (float) Math.toDegrees(Math.atan2(positionResult.z, positionResult.x)) - 90.0F;
        float floatValueValue = (float) (-Math.toDegrees(Math.atan2(positionResult.y, squareRoot)));
        return new Rotation(MathHelper.wrapDegrees(floatValue), MathHelper.clamp(floatValueValue, -90.0F, 90.0F));
    }

    private float calculateSin() {
        float floatValue = mc.player.age + (mc.getRenderTickCounter() != null ? mc.getRenderTickCounter().getTickDelta(false) : 0.0F);
        return (float) ((Math.sin(floatValue * 0.31F) * 0.5 + Math.sin(floatValue * 0.73F + 1.1F) * 0.30000003042305273 + Math.sin(floatValue * 1.7F + 2.6F) * 0.2000000149681302) * 8.0);
    }

    private int calculateTestWithStack(ScreenHandler screenHandler, boolean flag, int intValue, Predicate<ItemStack> predicate) {
        int intValueValue = 0;
        for (Slot slot : screenHandler.slots) {
            if (intValueValue >= intValue) {
                break;
            }
            if (this.isPlayerInventorySlot(slot) != flag && predicate.test(slot.getStack())) {
                this.clickSlot(screenHandler, slot.id, 0, SlotActionType.QUICK_MOVE);
                intValueValue++;
            }
        }
        return intValueValue;
    }

    private void clickSlot(ScreenHandler screenHandler, int intValue, int intValueValue, SlotActionType slotActionType) {
        mc.interactionManager.clickSlot(screenHandler.syncId, intValue, intValueValue, slotActionType, mc.player);
    }

    private boolean isPlayerInventorySlot(Slot slot) {
        return slot.inventory == mc.player.getInventory();
    }

    private int calculateOfWithStack(Item item) {
        for (Slot slot : mc.player.playerScreenHandler.slots) {
            if (this.isPlayerInventorySlot(slot) && slot.getStack().isOf(item)) {
                return slot.id;
            }
        }
        return -1;
    }

    private boolean isOfWithItemCount(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() instanceof SaplingBlock || !stack.isOf(Items.BONE_MEAL) || stack.isOf(Items.BONE) && InventoryUtil.getItemCount(Items.BONE_MEAL) == 0 : stack.isOf(Items.BONE_MEAL) || stack.isOf(Items.BONE) && InventoryUtil.getItemCount(Items.BONE_MEAL) == 0;
    }

    private boolean isHarvestInventoryFull() {
        return InventoryUtil.getItemCount(Items.APPLE) > 128 || InventoryUtil.getItemCount(Items.OAK_LOG) > 192;
    }

    private boolean needsFarmingSupplies() {
        return InventoryUtil.getItemCount(Items.OAK_SAPLING) == 0 || InventoryUtil.getItemCount(Items.BONE_MEAL) == 0;
    }

    private double squaredDistanceToBlock(BlockPos position) {
        return mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position));
    }

    private double calculateSubtractWithEyePos(BlockPos position) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d rotationVec = mc.player.getRotationVec(1.0F);
        Vec3d positionValue = Vec3d.ofCenter(position).subtract(eyePos);
        double doubleValue = positionValue.dotProduct(rotationVec);
        return doubleValue <= 0.0 ? Double.MAX_VALUE : positionValue.subtract(rotationVec.multiply(doubleValue)).lengthSquared();
    }

    private BlockPos createBlockStateWithBlockPos(double doubleValue) {
        BlockPos blockPos = mc.player.getBlockPos();
        BlockPos position = null;
        double MAX_VALUE = Double.MAX_VALUE;
        double doubleValueValue = doubleValue * doubleValue;
        int roundedUp = (int) Math.ceil(doubleValue);
        Mutable mutablePosition = new Mutable();
        for (int index = -roundedUp; index <= roundedUp; index++) {
            for (int indexValue = -roundedUp; indexValue <= roundedUp; indexValue++) {
                for (int indexResult = -roundedUp; indexResult <= roundedUp; indexResult++) {
                    mutablePosition.set(blockPos.getX() + index, blockPos.getY() + indexValue, blockPos.getZ() + indexResult);
                    BlockState blockState = mc.world.getBlockState(mutablePosition);
                    if (blockState.isIn(BlockTags.DIRT)) {
                        BlockState blockStateValue = mc.world.getBlockState(mutablePosition.up());
                        if ((blockStateValue.isAir() || blockStateValue.getBlock() instanceof SaplingBlock) && this.squaredDistanceToBlock(mutablePosition) <= doubleValueValue) {
                            double doubleValueResult = this.squaredDistanceToBlock(mutablePosition);
                            if (doubleValueResult < MAX_VALUE) {
                                MAX_VALUE = doubleValueResult;
                                position = mutablePosition.toImmutable();
                            }
                        }
                    }
                }
            }
        }
        return position;
    }

    private BlockPos createBlockPosWithTest(double doubleValue, Predicate<BlockState> predicate, ToDoubleFunction<BlockPos> toDoubleFunction) {
        BlockPos blockPos = mc.player.getBlockPos();
        BlockPos position = null;
        double MAX_VALUE = Double.MAX_VALUE;
        double doubleValueValue = doubleValue * doubleValue;
        int roundedUp = (int) Math.ceil(doubleValue);
        Mutable mutablePosition = new Mutable();
        for (int index = -roundedUp; index <= roundedUp; index++) {
            for (int indexValue = -roundedUp; indexValue <= roundedUp; indexValue++) {
                for (int indexResult = -roundedUp; indexResult <= roundedUp; indexResult++) {
                    mutablePosition.set(blockPos.getX() + index, blockPos.getY() + indexValue, blockPos.getZ() + indexResult);
                    if (predicate.test(mc.world.getBlockState(mutablePosition)) && this.squaredDistanceToBlock(mutablePosition) <= doubleValueValue) {
                        double doubleValueResult = toDoubleFunction.applyAsDouble(mutablePosition);
                        if (doubleValueResult < MAX_VALUE) {
                            MAX_VALUE = doubleValueResult;
                            position = mutablePosition.toImmutable();
                        }
                    }
                }
            }
        }
        return position;
    }
}
