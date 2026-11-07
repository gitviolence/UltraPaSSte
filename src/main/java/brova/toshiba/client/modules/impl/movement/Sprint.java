package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

public class Sprint extends Module {

    public static Sprint INSTANCE = new Sprint();

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    private static boolean sprinting;

    private static long time = 0L;

    public static int tickStop;

    private static int cachedCount = 0;

    private static boolean stateFlag = false;

    private ClientPlayerEntity clientPlayerEntity;

    public Sprint() {
        super("Sprint", "Автоматический бег", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        updateLocalState();
        sprinting = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        updateLocalState();
        sprinting = false;
        this.clientPlayerEntity = null;
        if (mc.options != null) {
            mc.options.sprintKey.setPressed(false);
        }
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
        super.onDisable();
    }

    @EventHandler
    public void onEvent(PlayerUpdateEvent event) {
        if (mc.player == null) {
            Object value = null;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                accessReceiver.clientPlayerEntity = (ClientPlayerEntity) accessValue;
            });
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
                Sprint.updateLocalState();
            });
            if (mc.options != null) {
                mc.options.sprintKey.setPressed(false);
            }
        } else {
            if (this.clientPlayerEntity != mc.player) {
                ClientPlayerEntity player = mc.player;
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, player, (accessReceiver, accessValue) -> {
                    accessReceiver.clientPlayerEntity = accessValue;
                });
                updateLocalState();
                sprinting = true;
            }
            if (tickStop > 0) {
                tickStop--;
                mc.options.sprintKey.setPressed(false);
                mc.player.setSprinting(false);
            } else if (cachedCount <= 0 && System.currentTimeMillis() >= time && sprinting && (AttackAura.INSTANCE == null || !AttackAura.INSTANCE.isEnable() || AttackAura.INSTANCE.getLegitSprintResetTicks() <= 0 && !AttackAura.INSTANCE.isWaitingForServerSprintStop() && !AttackAura.INSTANCE.isSprintResetPending()) && !mc.player.isSneaking() && !mc.player.isSprinting() && (!mc.player.horizontalCollision || mc.player.collidedSoftly) && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && (!mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() || !mc.player.isSprinting()) && mc.player.forwardSpeed > 0.0F && !mc.options.sprintKey.isPressed()) {
                mc.player.setSprinting(true);
            }
        }
    }

    public boolean shouldKeepSprintInWater() {
        return this.isEnable();
    }

    public static void pushPause(long longValue) {
        stateFlag = stateFlag | isSprintActive();
        cachedCount++;
        time = Math.max(time, System.currentTimeMillis() + Math.max(0L, longValue));
        sprinting = false;
        if (minecraftClient.options != null) {
            minecraftClient.options.sprintKey.setPressed(false);
        }
        if (minecraftClient.player != null) {
            minecraftClient.player.setSprinting(false);
        }
    }

    public static void popPause() {
        if (cachedCount > 0) {
            cachedCount--;
        }
        if (cachedCount <= 0) {
            time = 0L;
            sprinting = stateFlag;
            stateFlag = false;
        }
    }

    private static boolean isSprintActive() {
        return minecraftClient.player != null && minecraftClient.player.isSprinting() ? true : ModuleRegistry.sprint != null && ModuleRegistry.sprint.isEnable() && sprinting;
    }

    static private void updateLocalState() {
        cachedCount = 0;
        stateFlag = false;
        time = 0L;
    }

    public static boolean isSprinting() {
        return sprinting;
    }

    public static void setSprinting(boolean sprintingValue) {
        sprinting = sprintingValue;
    }

    public static long getTime() {
        return time;
    }

    public static void setTime(long timeValue) {
        time = timeValue;
    }
}
