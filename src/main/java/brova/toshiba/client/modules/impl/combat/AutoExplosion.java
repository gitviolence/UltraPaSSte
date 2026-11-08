package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.input.KeyBindUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class AutoExplosion extends Module {

    public static AutoExplosion INSTANCE = new AutoExplosion();

    private final ModeSetting modeBaxa = new ModeSetting("Режим взрыва", "Авто", "Авто", "По бинду");

    private final BindSetting bind = new BindSetting("Бинд", -1).visible(() -> this.modeBaxa.is("По бинду"));

    private final BooleanSetting explosionOnRightClick = new BooleanSetting("Взрыв при ПКМ", true);

    private final BooleanSetting keepCrystal = new BooleanSetting("Оставлять кристалл", false);

    private BlockPos targetPos;

    private int targetSlot = -1;

    private int oldSlot = -1;

    private boolean needSync;

    private Box crystalArea;

    private boolean blocked;

    private boolean internalInteract;

    public AutoExplosion() {
        super("AutoExplosion", "Автоматически взрывает кристалл", ModuleCategory.COMBAT);
        this.addSettings(this.modeBaxa, this.bind, this.explosionOnRightClick, this.keepCrystal);
    }

    @EventHandler
    public void onBinding(KeyBind keyBind) {
        if (mc.player != null && mc.world != null && mc.currentScreen == null && this.modeBaxa.is("По бинду")) {
            boolean flag = this.bind.getKey() == -1 ? keyBind.getKey() == KeyBindUtil.createMouseBind(2) : keyBind.getKey() == this.bind.getKey();
            if (flag) {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((AutoExplosion) accessReceiver).sendBlockPosWithType();
                });
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null && eventPacket.getType() == EventPacket.Type.SEND && !this.internalInteract && eventPacket.getPacket() instanceof PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket) {
            BlockHitResult blockHitResult = playerInteractBlockC2SPacket.getBlockHitResult();
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockPos position = blockPos.offset(blockHitResult.getSide());
            if (this.isHoldingObsidian() && this.isDistanceToWithEyePos(position) && !mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.END_CRYSTAL))) {
                int targetSlot = this.calculateItemWithStack();
                if (targetSlot != -1) {
                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, position, (accessReceiver, accessValue) -> {
                        accessReceiver.targetPos = accessValue;
                    });
                    this.targetSlot = targetSlot;
                    this.blocked = true;
                }
            }
            if (this.explosionOnRightClick.isState() && this.isBlockStateWithCoolingDown(blockPos) && this.isItemWithOffHandStack(blockHitResult, blockPos)) {
                eventPacket.cancel();
            }
        }
    }

    @EventHandler
    public void onTick(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.needSync) {
                this.needSync = false;
                this.selectHotbarSlot();
            }
            if (this.targetPos != null) {
                if (mc.world.getBlockState(this.targetPos).isAir()) {
                    this.targetPos = null;
                } else if (this.blocked) {
                    this.blocked = false;
                } else {
                    this.sendOfCenterWithInventory(this.targetPos);
                }
            }
            this.updateBoundingBoxWithOtherEntities();
        } else {
            this.updateNetworkHandler();
        }
    }

    private void sendOfCenterWithInventory(BlockPos position) {
        if (this.targetSlot >= 0 && this.targetSlot <= 8 && this.isUpWithAir(position)) {
            this.updateRotationsWithUpdate(Vec3d.ofCenter(position));
            this.oldSlot = mc.player.getInventory().selectedSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(this.targetSlot));
            mc.player.getInventory().selectedSlot = this.targetSlot;
            Vec3d positionValue = Vec3d.ofCenter(position).add(0.0, 0.5, 0.0);
            BlockHitResult blockHitResult = new BlockHitResult(positionValue, Direction.UP, position, false);
            this.sendPacketWithNetworkHandler(Hand.MAIN_HAND, blockHitResult);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.needSync = true;
            this.crystalArea = this.createBoxFromBlockPos(position.up()).expand(0.1);
            this.targetPos = null;
        }
    }

    private void updateBoundingBoxWithOtherEntities() {
        if (this.crystalArea != null) {
            for (Entity entity : mc.world.getOtherEntities(null, this.crystalArea)) {
                if (entity instanceof EndCrystalEntity endCrystalEntity && endCrystalEntity.isAlive()) {
                    if (!endCrystalEntity.getBoundingBox().contains(mc.player.getEyePos())) {
                        this.updateRotationsWithUpdate(endCrystalEntity.getBoundingBox().getCenter());
                    }
                    this.sendPacketWithNetworkHandler(endCrystalEntity);
                    this.crystalArea = null;
                    if (!this.keepCrystal.isState()) {
                        this.selectHotbarSlot();
                    }
                    return;
                }
            }
        }
    }

    private boolean isBlockStateWithCoolingDown(BlockPos position) {
        if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.END_CRYSTAL))) {
            return false;
        } else if (this.isHoldingPlaceableBlock()) {
            return false;
        } else {
            Block block = mc.world.getBlockState(position).getBlock();
            return block != Blocks.OBSIDIAN && block != Blocks.BEDROCK ? false : mc.world.getBlockState(position.up()).isAir();
        }
    }

    private boolean isItemWithOffHandStack(BlockHitResult blockHitResult, BlockPos position) {
        int intValue = this.calculateStackWithSlot(Items.END_CRYSTAL);
        if (intValue == -1 && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            return false;
        } else {
            boolean flag = false;
            if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                this.updateClickSlot(intValue);
                flag = true;
            }
            this.sendPacketWithNetworkHandler(Hand.OFF_HAND, blockHitResult);
            mc.player.swingHand(Hand.OFF_HAND);
            this.crystalArea = this.createBoxFromBlockPos(position.up()).expand(0.1);
            if (flag) {
                this.updateClickSlot(intValue);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
            return true;
        }
    }

    private void sendBlockPosWithType() {
        int intValue = this.calculateStackWithSlot(Items.OBSIDIAN);
        int targetSlot = this.calculateItemWithStack();
        if (intValue != -1 && targetSlot != -1 && mc.crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK && !mc.world.getBlockState(blockHitResult.getBlockPos()).isAir()) {
            BlockPos targetPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            this.targetPos = targetPos;
            this.targetSlot = targetSlot;
            this.blocked = true;
            this.updateClickSlot(intValue);
            this.sendPacketWithNetworkHandler(Hand.OFF_HAND, blockHitResult);
            mc.player.swingHand(Hand.OFF_HAND);
            this.updateClickSlot(intValue);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }

    private void sendPacketWithNetworkHandler(EndCrystalEntity endCrystalEntity) {
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(endCrystalEntity, false));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void sendPacketWithNetworkHandler(Hand hand, BlockHitResult blockHitResult) {
        this.internalInteract = true;
        try {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, blockHitResult, 0));
        } finally {
            this.internalInteract = false;
        }
    }

    private void updateRotationsWithUpdate(Vec3d position) {
        Vec2f rotations = RotationUtil.getRotations(position);
        RotationStorage.update(new Rotation(rotations.x, rotations.y), 360.0F, 360.0F, 360.0F, 360.0F, 1, 2, false);
    }

    private boolean isUpWithAir(BlockPos position) {
        BlockPos positionValue = position.up();
        BlockPos positionResult = position.up(2);
        if (!mc.world.getBlockState(positionValue).isAir()) {
            return false;
        } else if (!mc.world.getBlockState(positionResult).isAir()) {
            return false;
        } else {
            Box box = new Box(positionValue.getX(), positionValue.getY(), positionValue.getZ(), positionValue.getX() + 1.0, positionValue.getY() + 2.0, positionValue.getZ() + 1.0);
            for (Entity entity : mc.world.getOtherEntities(null, box)) {
                if (!(entity instanceof EndCrystalEntity)) {
                    return false;
                }
            }
            return true;
        }
    }

    private int calculateItemWithStack() {
        for (int index = 0; index < 9; index++) {
            if (mc.player.getInventory().getStack(index).getItem() == Items.END_CRYSTAL) {
                return index;
            }
        }
        return -1;
    }

    private int calculateStackWithSlot(Item item) {
        for (int index = 9; index < 45; index++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(index).getStack();
            if (stack.getItem() == item) {
                return index;
            }
        }
        return -1;
    }

    private void updateClickSlot(int intValue) {
        if (intValue >= 36 && intValue <= 44) {
            mc.interactionManager.clickSlot(0, 45, intValue + -36, SlotActionType.SWAP, mc.player);
        } else {
            mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.SWAP, mc.player);
            mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.SWAP, mc.player);
            mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.SWAP, mc.player);
        }
    }

    private void selectHotbarSlot() {
        if (this.oldSlot != -1) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(this.oldSlot));
            mc.player.getInventory().selectedSlot = this.oldSlot;
            this.oldSlot = -1;
        }
    }

    private Box createBoxFromBlockPos(BlockPos position) {
        return new Box(position.getX(), position.getY(), position.getZ(), position.getX() + 1.0, position.getY() + 1.0, position.getZ() + 1.0);
    }

    private boolean isHoldingObsidian() {
        return mc.player.getMainHandStack().getItem() == Items.OBSIDIAN || mc.player.getOffHandStack().getItem() == Items.OBSIDIAN;
    }

    private boolean isHoldingPlaceableBlock() {
        Item item = mc.player.getMainHandStack().getItem();
        Item itemValue = mc.player.getOffHandStack().getItem();
        return item instanceof BlockItem && item != Items.PLAYER_HEAD || itemValue instanceof BlockItem && itemValue != Items.PLAYER_HEAD;
    }

    private boolean isDistanceToWithEyePos(BlockPos position) {
        return mc.player.getEyePos().distanceTo(Vec3d.ofCenter(position)) <= 4.5;
    }

    private void updateNetworkHandler() {
        if (this.oldSlot != -1 && mc.player != null && mc.getNetworkHandler() != null) {
            this.selectHotbarSlot();
        }
        this.targetPos = null;
        this.targetSlot = -1;
        this.needSync = false;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.crystalArea = (Box) accessValue;
        });
        this.blocked = false;
        this.internalInteract = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.updateNetworkHandler();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AutoExplosion) accessReceiver).updateNetworkHandler();
        });
    }

    public ModeSetting getModeBaxa() {
        return this.modeBaxa;
    }

    public BindSetting getBind() {
        return this.bind;
    }

    public BooleanSetting getExplosionOnRightClick() {
        return this.explosionOnRightClick;
    }

    public BooleanSetting getKeepCrystal() {
        return this.keepCrystal;
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    public int getTargetSlot() {
        return this.targetSlot;
    }

    public int getOldSlot() {
        return this.oldSlot;
    }

    public boolean isNeedSync() {
        return this.needSync;
    }

    public Box getCrystalArea() {
        return this.crystalArea;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public boolean isInternalInteract() {
        return this.internalInteract;
    }

    public void setTargetPos(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    public void setTargetSlot(int targetSlot) {
        this.targetSlot = targetSlot;
    }

    public void setOldSlot(int oldSlot) {
        this.oldSlot = oldSlot;
    }

    public void setNeedSync(boolean needSync) {
        this.needSync = needSync;
    }

    public void setCrystalArea(Box crystalArea) {
        this.crystalArea = crystalArea;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setInternalInteract(boolean internalInteract) {
        this.internalInteract = internalInteract;
    }
}
