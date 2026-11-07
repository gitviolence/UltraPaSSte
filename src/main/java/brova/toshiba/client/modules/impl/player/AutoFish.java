package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public final class AutoFish extends Module {

    public static AutoFish INSTANCE = new AutoFish();

    private final BooleanSetting autoSelectFishingRod = new BooleanSetting("Автоматически брать удочку в руки", true);

    private boolean recastPending = false;

    private boolean rodSelected = false;

    private int packet = -1;

    private long lastUpdateTimestamp = 0L;

    private long lastUpdateMillis = 0L;

    public AutoFish() {
        super("AutoFish", "Автоматически ловит рыбу за вас", ModuleCategory.PLAYER);
        this.addSettings(this.autoSelectFishingRod);
    }

    @Override
    public void onDisable() {
        this.recastPending = false;
        this.rodSelected = false;
        this.packet = -1;
        this.lastUpdateTimestamp = 0L;
        this.lastUpdateMillis = 0L;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.autoSelectFishingRod.isState() && this.packet == -1) {
                this.updateStackWithInventory();
            }
            if (this.packet != -1 && mc.player.getInventory().selectedSlot != this.packet) {
                mc.player.getInventory().selectedSlot = this.packet;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.packet));
            }
            long lastUpdateTimestamp = System.currentTimeMillis();
            if (this.recastPending && lastUpdateTimestamp - this.lastUpdateMillis >= 600L) {
                this.sendInventoryWithStack();
                this.recastPending = false;
                this.rodSelected = true;
                this.lastUpdateTimestamp = lastUpdateTimestamp;
            }
            if (this.rodSelected && lastUpdateTimestamp - this.lastUpdateTimestamp >= 300L) {
                this.sendInventoryWithStack();
                this.rodSelected = false;
                this.lastUpdateTimestamp = lastUpdateTimestamp;
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null && eventPacket.getPacket() instanceof PlaySoundS2CPacket playSoundS2CPacket && playSoundS2CPacket.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
            this.recastPending = true;
            this.lastUpdateMillis = System.currentTimeMillis();
        }
    }

    private void sendInventoryWithStack() {
        if (mc.player != null && mc.interactionManager != null && this.packet != -1 && this.packet < 9) {
            ItemStack stack = mc.player.getInventory().getStack(this.packet);
            if (stack.getItem() instanceof FishingRodItem) {
                if (mc.player.getInventory().selectedSlot != this.packet) {
                    mc.player.getInventory().selectedSlot = this.packet;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.packet));
                }
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        }
    }

    private void updateStackWithInventory() {
        if (mc.player != null) {
            int packet = -1;
            int intValue = -1;
            for (int index = 0; index < 9; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (stack.getItem() instanceof FishingRodItem) {
                    int size = EnchantmentHelper.getEnchantments(stack).getSize();
                    if (size > intValue) {
                        intValue = size;
                        packet = index;
                    }
                }
            }
            if (packet != -1) {
                this.packet = packet;
            }
        }
    }
}
