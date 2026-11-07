package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoArmor extends Module {

    public static AutoArmor INSTANCE = new AutoArmor();

    private final FloatSetting delay = new FloatSetting("Задержка", 25.0F, 1.0F, 1000.0F, 1.0F);

    private long lastUpdateTimestamp = 0L;

    public AutoArmor() {
        super("AutoArmor", "Автоматически одевает броню", ModuleCategory.PLAYER);
        this.addSettings(this.delay);
    }

    @EventHandler
    public void onEvent(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && !this.isLocalState()) {
            long lastUpdateTimestamp = System.currentTimeMillis();
            if (!((float) (lastUpdateTimestamp - this.lastUpdateTimestamp) < this.delay.get())) {
                for (int index = 0; index < 4; index++) {
                    ItemStack armorStack = mc.player.getInventory().getArmorStack(index);
                    if (armorStack.isEmpty()) {
                        for (int indexValue = 0; indexValue < 36; indexValue++) {
                            ItemStack stack = mc.player.getInventory().getStack(indexValue);
                            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem && this.calculateToLowerCaseWithToString(armorItem) == index) {
                                int intValue = indexValue;
                                if (indexValue < 9) {
                                    intValue = indexValue + 36;
                                }
                                mc.interactionManager.clickSlot(0, intValue, 0, SlotActionType.QUICK_MOVE, mc.player);
                                this.lastUpdateTimestamp = lastUpdateTimestamp;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isLocalState() {
        return mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F;
    }

    private int calculateToLowerCaseWithToString(ArmorItem armorItem) {
        String normalizedText = armorItem.toString().toLowerCase();
        if (normalizedText.contains("helmet") || normalizedText.contains("skull")) {
            return 3;
        } else if (normalizedText.contains("chestplate") || normalizedText.contains("tunic")) {
            return 2;
        } else if (!normalizedText.contains("leggings") && !normalizedText.contains("pants")) {
            return !normalizedText.contains("boots") && !normalizedText.contains("shoes") ? 0 : 0;
        } else {
            return 1;
        }
    }
}
