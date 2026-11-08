package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.ScreenEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.time.IntervalTimer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class TapeMouse extends Module {

    public static TapeMouse INSTANCE = new TapeMouse();

    private final ModeSetting buttonMode = new ModeSetting("Какую кнопку нажимать", "Левую", "Левую", "Правую");

    private final FloatSetting duration = new FloatSetting("Длительность нажатия (мс)", 700.0F, 50.0F, 5000.0F, 50.0F);

    private final BooleanSetting blockMenus = new BooleanSetting("Блокировать меню и инвентарь", true);

    private final BooleanSetting forceUnlimitedFps = new BooleanSetting("Не ограничивать FPS в фоне", true);

    private final IntervalTimer clickTimer = new IntervalTimer();

    private long clickDelayMillis = 700L;

    public TapeMouse() {
        super("TapeMouse", "Легит-кликер: бьёт только по энтити под прицелом с полным зарядом и человеческим таймингом", ModuleCategory.COMBAT);
        this.addSettings(this.buttonMode, this.duration, this.blockMenus, this.forceUnlimitedFps);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.clickTimer.reset();
        this.clickDelayMillis = (long) this.duration.get();
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
        } else if (this.clickTimer.finished(this.clickDelayMillis) && this.updateLocalState()) {
            // Легит-тайминг: интервал — это база ±20% рандома + редкая микро-пауза.
            // Постоянного периода нет, ач не вытаскивает периодичность из статистики кликов.
            long longValue = (long) (this.duration.get() * (0.9F + Math.random() * 0.2F));
            if (Math.random() < 0.08) {
                longValue += 60L + (long) (Math.random() * 200.0);
            }
            this.clickDelayMillis = longValue;
            this.clickTimer.reset();
        }
    }

    private boolean updateLocalState() {
        if (this.buttonMode.is("Левую")) {
            return this.updateEntityWithAttackEntity();
        } else {
            return this.updateEntityWithType();
        }
    }

    private boolean updateEntityWithAttackEntity() {
        // Норм-удары без настроек: только живые энтити под прицелом и только с полным зарядом —
        // удар не теряет урон, а клик в воздух/по блоку (палево и гриферство) исключён.
        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity) {
            if (mc.player.getAttackCooldownProgress(0.0F) < 0.9F) {
                return false;
            }
            mc.interactionManager.attackEntity(mc.player, entityHitResult.getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onSetScreen(ScreenEvent event) {
        if (this.blockMenus.isState() && (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof GameMenuScreen)) {
            event.setScreen(null);
        }
    }

    private boolean updateEntityWithType() {
        if (mc.crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() != Type.MISS) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
        } else if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != null) {
            mc.interactionManager.interactEntity(mc.player, entityHitResult.getEntity(), Hand.MAIN_HAND);
        } else {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
        return true;
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

    public BooleanSetting getForceUnlimitedFps() {
        return this.forceUnlimitedFps;
    }

    public IntervalTimer getClickTimer() {
        return this.clickTimer;
    }
}
