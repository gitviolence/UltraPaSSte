package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.PostPlayerUpdateEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.movement.MovementUtil;
import brova.toshiba.client.utils.text.ChatUtil;
import brova.toshiba.client.utils.time.TimerController;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;

public final class Timer extends Module {

    public static Timer INSTANCE = new Timer();

    public final ModeSetting mode = new ModeSetting("Режим", "Matrix", "Matrix", "Shift", "Grim");

    public final BooleanSetting oldMatrix = new BooleanSetting("Старый Matrix", false).visible(() -> this.mode.is("Matrix"));

    public final FloatSetting speed = new FloatSetting("Скорость", 2.0F, 0.1F, 10.0F, 0.1F).visible(() -> !this.mode.is("Shift"));

    public final FloatSetting shiftTicks = new FloatSetting("Тики сдвига", 10.0F, 1.0F, 40.0F, 1.0F).visible(() -> this.mode.is("Shift"));

    public final BindSetting boostKey = new BindSetting("Кнопка Grim", -1).visible(() -> this.mode.is("Grim"));

    public final ModeSetting onFlag = new ModeSetting("При флаге", "Reset", "Reset", "Disable", "None");

    private double cachedCoordinate = 1.0;

    private static float energy = 0.0F;

    private long grimChargeStartMillis = 0L;

    private long lastFlagMillis = 0L;

    private static double previousX;

    private static double previousY;

    private static double previousZ;

    private static float previousYaw;

    private static float previousPitch;

    public Timer() {
        super("Timer", "Ускоряет время в игре", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode, this.oldMatrix, this.speed, this.shiftTicks, this.boostKey, this.onFlag);
    }

    @Override
    public void onEnable() {
        this.setEnable(true);
        EventBus.register(this);
        this.cachedCoordinate = 1.0;
        if (!this.mode.is("Matrix")) {
            energy = 0.0F;
        }
        if (this.mode.is("Grim")) {
            this.grimChargeStartMillis = System.currentTimeMillis();
        }
    }

    @Override
    public void onDisable() {
        this.setEnable(false);
        EventBus.unregister(this);
        TimerController.reset();
        this.cachedCoordinate = 1.0;
        energy = 0.0F;
        this.grimChargeStartMillis = 0L;
        this.lastFlagMillis = 0L;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (this.isEnable() && mc.player != null && mc.world != null) {
            if (this.mode.is("Matrix")) {
                energy = MathHelper.clamp(isYawWithPitch() ? energy + 0.025F : energy - (this.oldMatrix.isState() ? 0.005F : 0.0F), 0.0F, 1.0F);
            } else if (this.mode.is("Shift")) {
                energy = MathHelper.clamp(isYawWithPitch() ? energy + 0.025F : energy, 0.0F, 1.0F);
            }
            previousX = mc.player.getX();
            previousY = mc.player.getY();
            previousZ = mc.player.getZ();
            previousYaw = mc.player.getYaw();
            previousPitch = mc.player.getPitch();
            String current = this.mode.getCurrent();
            switch(current) {
                case "Matrix":
                    if (!MovementUtil.hasPlayerMovement()) {
                        this.cachedCoordinate = 1.0;
                    } else if (energy > 0.0F) {
                        this.cachedCoordinate = Math.max((double) this.speed.get(), 1.0);
                        energy = MathHelper.clamp(energy - (float) (0.1 * this.speed.get() - 0.1), 0.0F, 1.0F);
                    } else {
                        this.cachedCoordinate = 1.0;
                        this.updateMessageWithEnabled("Заряд таймера кончился! Отключаю..");
                    }
                    break;
                case "Grim":
                    long elapsedMillis = System.currentTimeMillis() - this.lastFlagMillis;
                    if (!(energy <= 0.0F) && this.boostKey.isPressed() && elapsedMillis >= 2000L) {
                        this.cachedCoordinate = Math.max((double) this.speed.get(), 1.0);
                        energy = MathHelper.clamp(energy - (float) (0.0025 * this.speed.get() - 0.0025), 0.0F, 1.0F);
                        break;
                    }
                    this.cachedCoordinate = 1.0;
                    break;
                default:
                    this.cachedCoordinate = 1.0;
            }
            float floatValue = this.isEnable() ? (float) this.cachedCoordinate : 1.0F;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                TimerController.setTimer(((float) accessArguments[0]));
            });
        }
    }

    @EventHandler
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (this.isEnable() && this.mode.is("Shift")) {
            if (energy < 0.9F) {
                this.updateMessageWithEnabled("Перед повторным использованием необходимо постоять на месте!");
            } else {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(event, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((PostPlayerUpdateEvent) accessReceiver).cancel();
                });
                event.setIterations((int) this.shiftTicks.get());
                energy = 0.0F;
                this.updateMessageWithEnabled("Тики пропущены! Отключаем");
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (this.isEnable() && mc.player != null && mc.world != null && eventPacket.getType() == Type.RECEIVE) {
            if (this.mode.is("Grim") && eventPacket.getPacket() instanceof CommonPingS2CPacket) {
                long longValue = System.currentTimeMillis() + -this.lastFlagMillis;
                if (longValue > 2000L) {
                    if (System.currentTimeMillis() + -this.grimChargeStartMillis > 25000L) {
                        this.grimChargeStartMillis = System.currentTimeMillis();
                        energy = 0.0F;
                        return;
                    }
                    if (!MovementUtil.hasPlayerMovement()) {
                        energy = MathHelper.clamp(energy + 0.005F, 0.0F, 1.0F);
                    }
                    eventPacket.cancel();
                }
            }
            if (eventPacket.getPacket() instanceof PlayerPositionLookS2CPacket) {
                this.lastFlagMillis = System.currentTimeMillis();
                String current = this.onFlag.getCurrent();
                switch(current) {
                    case "羞eset":
                        this.cachedCoordinate = 1.0;
                        energy = 0.0F;
                        break;
                    case "Disa⺧le":
                        energy = 0.0F;
                        this.updateMessageWithEnabled("Отключён т.к. тебя флагнуло!");
                }
            }
            if (eventPacket.getPacket() instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket && this.mode.is("Grim") && entityVelocityUpdateS2CPacket.getEntityId() == mc.player.getId()) {
                this.cachedCoordinate = 1.0;
                energy = 0.0F;
            }
        }
    }

    public static float getEnergy() {
        return energy;
    }

    private void updateMessageWithEnabled(String text) {
        ChatUtil.sendMessage(text);
        this.setEnabled(false);
    }

    private static boolean isYawWithPitch() {
        return mc.player == null ? true : previousX == mc.player.getX() && previousY == mc.player.getY() && previousZ == mc.player.getZ() && previousYaw == mc.player.getYaw() && previousPitch == mc.player.getPitch();
    }
}
