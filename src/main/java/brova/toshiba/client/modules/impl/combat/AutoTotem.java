package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.profile.Profiled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

@Profiled
public final class AutoTotem extends Module {

    public static AutoTotem INSTANCE = new AutoTotem();

    private final MultiSetting triggerSource = new MultiSetting("Брать от", new BooleanSetting("Кристалл рядом", true), new BooleanSetting("Падение", true));

    private final FloatSetting healthThreshold = new FloatSetting("Брать от кол-во здоровья", 4.5F, 1.0F, 20.0F, 0.5F);

    private final FloatSetting elytraHealthThreshold = new FloatSetting("Брать от кол-во здоровья с элитрой", 8.0F, 1.0F, 20.0F, 0.5F);

    private final FloatSetting crystalRadius = new FloatSetting("Радиус кристалла", 4.0F, 1.0F, 8.0F, 0.5F).visible(() -> this.isLocalState());

    private final BooleanSetting preserveEnchantedTotems = new BooleanSetting("Сохранять зачарованые тотемы", true);

    private final BooleanSetting restoreItem = new BooleanSetting("Возвращать предмет", true);

    private final FloatSetting fallHeight = new FloatSetting("Высота падения", 10.0F, 3.0F, 50.0F, 1.0F).visible(() -> this.triggerSource.is("Падение"));

    private final BooleanSetting legitMode = new BooleanSetting("Легит", true);

    private int cachedCountForInput;

    private boolean stateFlagForUpdatePushPause;

    private int swapCooldownTicks;

    private int cachedCountForSendPacket = -1;

    private ItemStack empty = ItemStack.EMPTY;

    private boolean stateFlagForSendPacket = false;

    private boolean restoreOffhandPhase = false;

    private boolean swapDirection = false;

    private int swapPhaseTicks = 0;

    public AutoTotem() {
        super("AutoTotem", "Автоматически берёт при выбранных настроек", ModuleCategory.COMBAT);
        this.addSettings(this.healthThreshold, this.elytraHealthThreshold, this.legitMode, this.preserveEnchantedTotems, this.restoreItem, this.triggerSource, this.crystalRadius, this.fallHeight);
    }

    @EventHandler
    public void onInput(DirectionalInput directionalInput) {
        if (this.legitMode.isState() && this.cachedCountForInput > 0) {
            if (mc.player == null) {
                return;
            }
            mc.player.setSprinting(false);
            directionalInput.setForward(0.0F);
            directionalInput.setStrafe(0.0F);
            boolean flag = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(directionalInput, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                ((DirectionalInput) accessReceiver).setJump(((boolean) accessArguments[0]));
            });
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            boolean entitiesWithSquaredDistanceTo = this.isEntitiesWithSquaredDistanceTo();
            if (entitiesWithSquaredDistanceTo) {
                this.swapDirection = true;
                this.swapPhaseTicks = 0;
            }
            if (this.swapCooldownTicks > 0) {
                this.swapCooldownTicks--;
            }
            if (this.legitMode.isState() && this.cachedCountForInput > 0) {
                mc.player.setSprinting(false);
                this.cachedCountForInput--;
                if (this.cachedCountForInput <= 0) {
                    if (this.restoreOffhandPhase) {
                        this.restoreOffhandItem();
                    } else {
                        this.equipEmergencyTotem();
                    }
                    this.updateLocalState();
                }
            } else {
                boolean healthWithAbsorptionAmount = this.isHealthWithAbsorptionAmount(entitiesWithSquaredDistanceTo);
                if (healthWithAbsorptionAmount && !this.isOfWithOffHandStack()) {
                    int cachedCountForSendPacket = this.calculateStackWithSlot();
                    if (cachedCountForSendPacket == -1) {
                        return;
                    }
                    if (!this.swapDirection && this.swapCooldownTicks > 0) {
                        return;
                    }
                    if (this.empty.isEmpty() && !this.stateFlagForSendPacket) {
                        ItemStack stack = mc.player.getOffHandStack().copy();
                        brova.toshiba.client.utils.runtime.AccessGuard.write(this, stack, (accessReceiver, accessValue) -> {
                            accessReceiver.empty = accessValue;
                        });
                    }
                    this.cachedCountForSendPacket = cachedCountForSendPacket;
                    this.restoreOffhandPhase = false;
                    this.swapPhaseTicks = 0;
                    if (this.legitMode.isState()) {
                        this.updatePushPause();
                        this.cachedCountForInput = this.swapDirection ? 1 : 2;
                        this.swapCooldownTicks = this.swapDirection ? 0 : 2;
                    } else {
                        this.equipEmergencyTotem();
                        this.swapCooldownTicks = this.swapDirection ? 0 : 2;
                    }
                }
                boolean flag = !healthWithAbsorptionAmount;
                if (flag) {
                    this.swapPhaseTicks++;
                } else {
                    this.swapPhaseTicks = 0;
                }
                if (this.restoreItem.isState() && !healthWithAbsorptionAmount && this.isOfWithOffHandStack() && this.stateFlagForSendPacket && (!this.preserveEnchantedTotems.isState() || !this.isOffHandStackWithOf()) && this.swapPhaseTicks >= 20) {
                    if (!this.swapDirection && this.swapCooldownTicks > 0) {
                        return;
                    }
                    this.restoreOffhandPhase = true;
                    if (this.legitMode.isState()) {
                        this.updatePushPause();
                        this.cachedCountForInput = this.swapDirection ? 1 : 2;
                        this.swapCooldownTicks = this.swapDirection ? 0 : 2;
                    } else {
                        this.restoreOffhandItem();
                        this.swapCooldownTicks = this.swapDirection ? 0 : 2;
                    }
                }
                if (!entitiesWithSquaredDistanceTo) {
                    this.swapDirection = false;
                }
            }
        }
    }

    private boolean isEntitiesWithSquaredDistanceTo() {
        float floatValue = this.crystalRadius.getValue().floatValue();
        double doubleValue = floatValue * floatValue;
        if (this.triggerSource.is("Кристалл рядом")) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity && mc.player.squaredDistanceTo(entity) <= doubleValue) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHealthWithAbsorptionAmount(boolean flag) {
        float floatValue = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean flagValue = mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && mc.player.isGliding();
        float floatValueValue = flagValue ? this.elytraHealthThreshold.getValue().floatValue() : this.healthThreshold.getValue().floatValue();
        if (floatValue <= floatValueValue) {
            return true;
        } else if (flag) {
            return true;
        } else {
            float floatValueResult = this.crystalRadius.getValue().floatValue();
            double doubleValue = floatValueResult * floatValueResult;
            return this.triggerSource.is("Падение") && mc.player.fallDistance >= this.fallHeight.getValue().floatValue() && !flagValue;
        }
    }

    private boolean isOfWithOffHandStack() {
        return mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
    }

    private boolean isOffHandStackWithOf() {
        ItemStack offHandStack = mc.player.getOffHandStack();
        return offHandStack.isOf(Items.TOTEM_OF_UNDYING) && offHandStack.hasEnchantments();
    }

    private int calculateStackWithSlot() {
        int intValue = -1;
        int intValueValue = -1;
        for (int index = 9; index < 45; index++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(index).getStack();
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                boolean enchantmentsPresent = stack.hasEnchantments();
                if (enchantmentsPresent) {
                    if (intValueValue == -1) {
                        intValueValue = index;
                    }
                } else if (intValue == -1) {
                    intValue = index;
                }
            }
        }
        if (this.preserveEnchantedTotems.isState()) {
            return intValue != -1 ? intValue : intValueValue;
        } else {
            return intValueValue != -1 ? intValueValue : intValue;
        }
    }

    private void equipEmergencyTotem() {
        if (!this.preserveEnchantedTotems.isState() || !this.isOffHandStackWithOf()) {
            int cachedCountForSendPacket = this.calculateStackWithSlot();
            if (cachedCountForSendPacket != -1) {
                this.cachedCountForSendPacket = cachedCountForSendPacket;
                this.clickSlot(cachedCountForSendPacket);
                this.stateFlagForSendPacket = true;
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }
    }

    private void restoreOffhandItem() {
        if (!this.isOfWithOffHandStack()) {
            this.stateFlagForSendPacket = false;
        } else {
            if (!this.empty.isEmpty()) {
                int intValue = this.calculateStackWithSlot(this.empty);
                if (intValue != -1) {
                    this.clickSlot(intValue);
                } else {
                    if (this.cachedCountForSendPacket == -1) {
                        this.cachedCountForSendPacket = 9;
                    }
                    this.clickSlot(this.cachedCountForSendPacket);
                }
            } else {
                if (this.cachedCountForSendPacket == -1) {
                    this.cachedCountForSendPacket = 9;
                }
                this.clickSlot(this.cachedCountForSendPacket);
            }
            this.stateFlagForSendPacket = false;
            this.cachedCountForSendPacket = -1;
            this.empty = ItemStack.EMPTY;
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }

    private int calculateStackWithSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        } else {
            for (int index = 9; index < 45; index++) {
                ItemStack stackValue = mc.player.playerScreenHandler.getSlot(index).getStack();
                if (ItemStack.areItemsEqual(stackValue, stack) && ItemStack.areEqual(stackValue, stack)) {
                    return index;
                }
            }
            return -1;
        }
    }

    private void clickSlot(int intValue) {
        mc.interactionManager.clickSlot(0, intValue, 40, SlotActionType.SWAP, mc.player);
    }

    private void updatePushPause() {
        if (!this.stateFlagForUpdatePushPause) {
            Sprint.pushPause(1000L);
            this.stateFlagForUpdatePushPause = true;
        }
    }

    private void updateLocalState() {
        if (this.stateFlagForUpdatePushPause) {
            this.stateFlagForUpdatePushPause = false;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
                Sprint.popPause();
            });
        }
    }

    private boolean isLocalState() {
        return this.triggerSource.is("Кристалл рядом");
    }

    @Override
    public void onDisable() {
        this.cachedCountForInput = 0;
        this.swapCooldownTicks = 0;
        this.cachedCountForSendPacket = -1;
        ItemStack EMPTY = ItemStack.EMPTY;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, EMPTY, (accessReceiver, accessValue) -> {
            accessReceiver.empty = accessValue;
        });
        this.stateFlagForSendPacket = false;
        this.restoreOffhandPhase = false;
        this.swapDirection = false;
        this.swapPhaseTicks = 0;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((AutoTotem) accessReceiver).updateLocalState();
        });
        super.onDisable();
    }
}
