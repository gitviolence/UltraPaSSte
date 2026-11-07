package brova.toshiba.client.gui.swap;

import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import net.minecraft.item.ItemStack;

public class SwapItem {

    ItemStack stack;

    String customName;

    final Animation anim = new Animation(0.0F, 16.0F, PowerEasing.CUBIC_OUT);

    public SwapItem(ItemStack stack, String customName) {
        this.stack = stack;
        this.customName = customName;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public String getCustomName() {
        return this.customName;
    }

    public Animation getAnim() {
        return this.anim;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public void setCustomName(String customName) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, customName, (accessReceiver, accessValue) -> {
            accessReceiver.customName = accessValue;
        });
    }
}
