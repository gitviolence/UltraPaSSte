package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.time.IntervalTimer;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class TapeMouse extends Module {

    public static TapeMouse INSTANCE = new TapeMouse();

    private final ModeSetting buttonMode = new ModeSetting("Какую кнопку нажимать", "Левую", "Левую", "Правую");

    private final FloatSetting duration = new FloatSetting("Длительность нажатия (мс)", 500.0F, 50.0F, 5000.0F, 50.0F);

    private final IntervalTimer clickTimer = new IntervalTimer();

    public TapeMouse() {
        super("TapeMouse", "Автоматически кликает выбранную кнопку мыши с заданной задержкой", ModuleCategory.COMBAT);
        this.addSettings(this.buttonMode, this.duration);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.clickTimer.reset();
    }

    @Override
    public void onDisable() {
        this.applyPressed();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            this.applyPressed();
        } else if (mc.currentScreen != null) {
            this.applyPressed();
            this.clickTimer.reset();
        } else {
            long longValue = (long) this.duration.get();
            if (this.clickTimer.finished(longValue)) {
                this.updateLocalState();
                this.clickTimer.reset();
            }
        }
    }

    private void updateLocalState() {
        if (this.buttonMode.is("Левую")) {
            this.updateEntityWithAttackEntity();
        } else if (this.buttonMode.is("Правую")) {
            this.updateEntityWithType();
        }
    }

    private void updateEntityWithAttackEntity() {
        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != null) {
            mc.interactionManager.attackEntity(mc.player, entityHitResult.getEntity());
        } else if (mc.crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() != Type.MISS) {
            mc.interactionManager.attackBlock(blockHitResult.getBlockPos(), blockHitResult.getSide());
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void updateEntityWithType() {
        if (mc.crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() != Type.MISS) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
        } else if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != null) {
            mc.interactionManager.interactEntity(mc.player, entityHitResult.getEntity(), Hand.MAIN_HAND);
        } else {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void applyPressed() {
        if (mc.options != null) {
            if (mc.options.attackKey != null && mc.options.attackKey.isPressed()) {
                mc.options.attackKey.setPressed(false);
            }
            if (mc.options.useKey != null && mc.options.useKey.isPressed()) {
                mc.options.useKey.setPressed(false);
            }
        }
    }

    public ModeSetting getButtonMode() {
        return this.buttonMode;
    }

    public FloatSetting getDuration() {
        return this.duration;
    }

    public IntervalTimer getClickTimer() {
        return this.clickTimer;
    }
}
