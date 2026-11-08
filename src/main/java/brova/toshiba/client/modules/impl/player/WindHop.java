package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.impl.player.WindHop.JumpPoint;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class WindHop extends Module {

    public static WindHop INSTANCE = new WindHop();

    private final BooleanSetting ownChargeOnly = new BooleanSetting("Только свой заряд", true);

    private final BindSetting throwChargeAtFeet = new BindSetting("Кинуть под себя заряд", -1);

    private final BindSetting useWindCharge = new BindSetting("Использовать заряд ветра", -1);

    private final BooleanSetting legitMode = new BooleanSetting("Легит", true);

    private int id = -1;

    private int launchTicks;

    private int cooldownTicks;

    private int cachedCountForInput;

    private int cachedCountForUpdate;

    private int cachedCountForUpdateWithYaw;

    private int cachedCountForUpdateInteger;

    private int clickSlotForUpdateWithYaw = -1;

    private int clickSlotForSendInteractItemWithSwingHand = -1;

    private boolean stateFlagForUpdatePushPause;

    private boolean normalChargePending;

    private boolean reverseChargePending;

    private boolean stateFlagForUpdateWithYaw;

    private boolean stateFlagForUpdate;

    public WindHop() {
        super("WindHop", "Авто-прыжок для максимального прыжка от заряда ветра", ModuleCategory.PLAYER);
        this.addSettings(this.ownChargeOnly, this.throwChargeAtFeet, this.useWindCharge, this.legitMode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.id = -1;
        this.launchTicks = 0;
        this.cooldownTicks = 0;
        this.applyPressed();
    }

    @Override
    public void onDisable() {
        this.id = -1;
        this.launchTicks = 0;
        this.cooldownTicks = 0;
        this.applyPressed();
        super.onDisable();
    }

    @EventHandler
    public void onInput(DirectionalInput directionalInput) {
        if (mc.player != null && this.legitMode.isState() && this.cachedCountForInput > 0) {
            mc.player.setSprinting(false);
            directionalInput.setForward(0.0F);
            directionalInput.setStrafe(0.0F);
            directionalInput.setJump(false);
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onBind(KeyBind keyBind) {
        if (mc.currentScreen == null) {
            if (keyBind.getKey() == this.useWindCharge.getKey()) {
                this.normalChargePending = true;
            }
            if (keyBind.getKey() == this.throwChargeAtFeet.getKey()) {
                this.reverseChargePending = true;
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.stateFlagForUpdate) {
                mc.options.jumpKey.setPressed(false);
                this.stateFlagForUpdate = false;
            }
            if (this.launchTicks > 0) {
                this.launchTicks += -1;
            }
            if (this.cooldownTicks > 0) {
                this.cooldownTicks += -1;
            }
            if (this.cachedCountForUpdate > 0) {
                this.cachedCountForUpdate += -1;
            }
            if (!this.isUpdateWithYawAndSprinting()) {
                if (this.id != -1) {
                    Entity entityById = mc.world.getEntityById(this.id);
                    if (!(entityById instanceof AbstractWindChargeEntity) || !entityById.isAlive()) {
                        this.id = -1;
                    }
                }
                if (this.launchTicks <= 0) {
                    if (this.cooldownTicks > 0) {
                        if (!mc.player.isOnGround() || !(mc.player.getVelocity().y <= 0.0)) {
                            return;
                        }
                        this.cooldownTicks = 0;
                    }
                    if (mc.player.isOnGround() && this.isBoundingBoxWithCollisionShape()) {
                        AbstractWindChargeEntity abstractWindChargeEntity = this.findAbstractWindChargeEntityFromExpandWithBoundingBox();
                        if (abstractWindChargeEntity != null) {
                            JumpPoint jumpPoint = this.createWindHopJumpPointFromVelocityWithRaycast(abstractWindChargeEntity);
                            if (jumpPoint != null && jumpPoint.ticks() <= 3) {
                                Vec3d pos = mc.player.getPos();
                                double doubleValue = jumpPoint.point().x + -pos.x;
                                double doubleValueValue = jumpPoint.point().z + -pos.z;
                                double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue);
                                double doubleValueResult = jumpPoint.point().y + -pos.y;
                                if (!(squareRoot > 3.5) && !(doubleValueResult > 3.0) && !(doubleValueResult < -3.0)) {
                                    mc.options.jumpKey.setPressed(true);
                                    this.stateFlagForUpdate = true;
                                    this.id = abstractWindChargeEntity.getId();
                                    this.launchTicks = 4;
                                    this.cooldownTicks = 35;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isUpdateWithYawAndSprinting() {
        if (this.clickSlotForUpdateWithYaw != -1) {
            this.sendClickSlotWithPacket();
            return true;
        } else if (this.legitMode.isState() && this.cachedCountForInput > 0) {
            mc.player.setSprinting(false);
            this.cachedCountForInput--;
            if (this.stateFlagForUpdateWithYaw) {
                if (this.cachedCountForInput == 2) {
                    RotationStorage.update(new Rotation(mc.player.getYaw(), 90.0F), 360.0F, 360.0F, 360.0F, 360.0F, 1, 8, false);
                } else if (this.cachedCountForInput == 1) {
                    RotationStorage.update(new Rotation(mc.player.getYaw(), 90.0F), 360.0F, 360.0F, 360.0F, 360.0F, 1, 8, false);
                    this.sendInteractItemWithSwingHand();
                    this.stateFlagForUpdateWithYaw = false;
                }
            } else if (this.cachedCountForInput == 1) {
                this.sendInteractItemWithSwingHand();
            }
            if (this.cachedCountForInput == 0) {
                this.updatePopPause();
            }
            return true;
        } else if (this.cachedCountForUpdateWithYaw > 0) {
            return this.isUpdateWithYaw();
        } else if (this.normalChargePending) {
            this.normalChargePending = false;
            this.applyOnGroundWithPressed(false);
            return true;
        } else if (this.reverseChargePending) {
            this.reverseChargePending = false;
            this.applyOnGroundWithPressed(true);
            return true;
        } else {
            return false;
        }
    }

    private void applyOnGroundWithPressed(boolean stateFlagForUpdateWithYaw) {
        if (this.cachedCountForUpdate <= 0) {
            int intValue = this.calculateSlotWithToContainerSlot();
            if (intValue != -1) {
                this.stateFlagForUpdateWithYaw = stateFlagForUpdateWithYaw;
                if (this.stateFlagForUpdateWithYaw && mc.player.isOnGround() && this.isBoundingBoxWithCollisionShape()) {
                    mc.options.jumpKey.setPressed(true);
                    this.stateFlagForUpdate = true;
                }
                if (this.legitMode.isState()) {
                    this.updatePushPause();
                    if (this.stateFlagForUpdateWithYaw) {
                        this.cachedCountForInput = 4;
                        this.cachedCountForUpdate = 4;
                    } else {
                        this.cachedCountForInput = 2;
                        this.cachedCountForUpdate = 2;
                    }
                } else if (this.stateFlagForUpdateWithYaw) {
                    this.stateFlagForUpdateWithYaw = false;
                    this.cachedCountForUpdate = 4;
                    this.updateInteger(2);
                } else {
                    this.cachedCountForUpdate = 2;
                    this.sendInteractItemWithSwingHand();
                }
            }
        }
    }

    private int calculateSlotWithToContainerSlot() {
        int slot = InventoryUtil.getSlot(Items.WIND_CHARGE);
        return slot == -1 ? -1 : InventoryUtil.toContainerSlot(slot);
    }

    private void updateInteger(int cachedCountForUpdateInteger) {
        this.cachedCountForUpdateWithYaw = 1;
        this.cachedCountForUpdateInteger = cachedCountForUpdateInteger;
    }

    private boolean isUpdateWithYaw() {
        RotationStorage.update(new Rotation(mc.player.getYaw(), 90.0F), 360.0F, 360.0F, 360.0F, 360.0F, 1, 8, false);
        if (this.cachedCountForUpdateInteger > 0) {
            this.cachedCountForUpdateInteger--;
            return true;
        } else if (this.cachedCountForUpdateWithYaw == 1) {
            this.sendInteractItemWithSwingHand();
            this.cachedCountForUpdateWithYaw = 0;
            return false;
        } else {
            this.cachedCountForUpdateWithYaw = 0;
            return false;
        }
    }

    private void sendInteractItemWithSwingHand() {
        if (mc.interactionManager != null) {
            if (mc.player.getMainHandStack().isOf(Items.WIND_CHARGE)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
            } else {
                int clickSlotForUpdateWithYaw = this.calculateSlotWithToContainerSlot();
                if (clickSlotForUpdateWithYaw != -1) {
                    int clickSlotForSendInteractItemWithSwingHand = mc.player.getInventory().selectedSlot;
                    int intValue = 36 + clickSlotForSendInteractItemWithSwingHand;
                    if (clickSlotForUpdateWithYaw == intValue) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                    } else if (clickSlotForUpdateWithYaw >= 36 && clickSlotForUpdateWithYaw <= 44) {
                        int intValueValue = clickSlotForUpdateWithYaw + -36;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValueValue));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(clickSlotForSendInteractItemWithSwingHand));
                    } else {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, clickSlotForUpdateWithYaw, clickSlotForSendInteractItemWithSwingHand, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        this.clickSlotForUpdateWithYaw = clickSlotForUpdateWithYaw;
                        this.clickSlotForSendInteractItemWithSwingHand = clickSlotForSendInteractItemWithSwingHand;
                    }
                }
            }
        }
    }

    private void sendClickSlotWithPacket() {
        if (this.clickSlotForUpdateWithYaw != -1) {
            if (mc.player != null && mc.interactionManager != null) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, this.clickSlotForUpdateWithYaw, this.clickSlotForSendInteractItemWithSwingHand, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                this.clickSlotForUpdateWithYaw = -1;
                this.clickSlotForSendInteractItemWithSwingHand = -1;
            } else {
                this.clickSlotForUpdateWithYaw = -1;
                this.clickSlotForSendInteractItemWithSwingHand = -1;
            }
        }
    }

    private void updatePushPause() {
        if (!this.stateFlagForUpdatePushPause) {
            Sprint.pushPause(1000L);
            this.stateFlagForUpdatePushPause = true;
        }
    }

    private void updatePopPause() {
        if (this.stateFlagForUpdatePushPause) {
            this.stateFlagForUpdatePushPause = false;
            Sprint.popPause();
        }
    }

    private void applyPressed() {
        this.sendClickSlotWithPacket();
        this.cachedCountForInput = 0;
        this.cachedCountForUpdate = 0;
        this.cachedCountForUpdateWithYaw = 0;
        this.cachedCountForUpdateInteger = 0;
        this.normalChargePending = false;
        this.reverseChargePending = false;
        this.stateFlagForUpdateWithYaw = false;
        this.stateFlagForUpdate = false;
        if (mc.options != null && mc.options.jumpKey != null) {
            mc.options.jumpKey.setPressed(false);
        }
        this.updatePopPause();
    }

    private AbstractWindChargeEntity findAbstractWindChargeEntityFromExpandWithBoundingBox() {
        Box box = mc.player.getBoundingBox().expand(7.5, 7.0, 7.5);
        Vec3d pos = mc.player.getPos();
        return mc.world.getEntitiesByClass(AbstractWindChargeEntity.class, box, input -> {
            if (input.isAlive() && input.getId() != this.id) {
                return input.age > 8 ? false : !this.ownChargeOnly.isState() || this.isOwnerWithSquaredDistanceTo(input);
            } else {
                return false;
            }
        }).stream().min(Comparator.comparingDouble(inputValue -> inputValue.squaredDistanceTo(pos))).orElse(null);
    }

    private boolean isOwnerWithSquaredDistanceTo(AbstractWindChargeEntity abstractWindChargeEntity) {
        Entity owner = abstractWindChargeEntity.getOwner();
        return owner == mc.player ? true : owner == null && abstractWindChargeEntity.age <= 3 && abstractWindChargeEntity.getPos().squaredDistanceTo(mc.player.getEyePos()) <= 6.25;
    }

    private boolean isBoundingBoxWithCollisionShape() {
        if (mc.player != null && mc.world != null) {
            Box boundingBox = mc.player.getBoundingBox();
            int roundedDown = MathHelper.floor(boundingBox.minX);
            int roundedDownValue = MathHelper.floor(boundingBox.maxX);
            int roundedDownResult = MathHelper.floor(boundingBox.minZ);
            int roundedDownItem = MathHelper.floor(boundingBox.maxZ);
            int roundedDownEntry = MathHelper.floor(boundingBox.minY + -0.01);
            for (int index = roundedDown; index <= roundedDownValue; index++) {
                for (int indexValue = roundedDownResult; indexValue <= roundedDownItem; indexValue++) {
                    BlockPos position = new BlockPos(index, roundedDownEntry, indexValue);
                    if (!mc.world.getBlockState(position).getCollisionShape(mc.world, position).isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private JumpPoint createWindHopJumpPointFromVelocityWithRaycast(AbstractWindChargeEntity abstractWindChargeEntity) {
        Vec3d pos = abstractWindChargeEntity.getPos();
        Vec3d velocity = abstractWindChargeEntity.getVelocity();
        for (int index = 1; index <= 12; index++) {
            Vec3d position = pos.add(velocity);
            BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(pos, position, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            if (blockHitResult.getType() == Type.BLOCK) {
                return new JumpPoint(blockHitResult.getPos(), index);
            }
            if (position.y <= mc.world.getBottomY()) {
                return new JumpPoint(position, index);
            }
            pos = position;
        }
        return null;
    }

    public static record JumpPoint(Vec3d point, int ticks) {
    }
}
