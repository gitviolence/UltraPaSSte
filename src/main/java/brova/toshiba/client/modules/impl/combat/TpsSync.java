package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.Toshiba;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.profile.Profiled;
import brova.toshiba.client.utils.time.TpsCalculator;
import net.minecraft.util.math.MathHelper;

@Profiled
public class TpsSync extends Module {

    public static TpsSync INSTANCE = new TpsSync();

    public TpsSync() {
        super("TpsSync", "Синхронизацирует удары с TPS сервера.", ModuleCategory.COMBAT);
    }

    public float getCurrentTPS() {
        if (Toshiba.INSTANCE != null && brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.tpsCalc) != null) {
            float tPS = brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.tpsCalc).getTPS();
            return MathHelper.clamp(tPS, 0.1F, 20.0F);
        } else {
            return 20.0F;
        }
    }

    public long getAdjustedCooldown(long longValue) {
        if (!this.isEnable()) {
            return longValue;
        } else {
            float currentTPS = this.getCurrentTPS();
            if (currentTPS >= 20.0F) {
                return longValue;
            } else {
                float floatValue = 20.0F / currentTPS;
                float floatValueValue = 1.0F + (20.0F - currentTPS) * 0.05F;
                long longValueValue = (long) ((float) longValue * floatValue * floatValueValue);
                return Math.min(longValueValue, 3000L);
            }
        }
    }

    public boolean canAttack(long longValue, long longValueValue, long longValueResult) {
        if (!this.isEnable()) {
            return longValueResult >= longValue + longValueValue;
        } else {
            long adjustedCooldown = this.getAdjustedCooldown(longValueValue);
            return longValueResult >= longValue + adjustedCooldown;
        }
    }
}
