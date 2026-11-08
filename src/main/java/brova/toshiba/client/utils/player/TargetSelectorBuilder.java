package brova.toshiba.client.utils.player;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;

public class TargetSelectorBuilder extends TargetSelector {

    public TargetSelectorBuilder range(DoubleSupplier range) {
        this.range = range;
        return this;
    }

    public TargetSelectorBuilder elytraRange(DoubleSupplier doubleSupplier) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, doubleSupplier, (accessReceiver, accessValue) -> {
            accessReceiver.elytraRange = accessValue;
        });
        return this;
    }

    public TargetSelectorBuilder throughWalls(BooleanSupplier throughWalls) {
        this.throughWalls = throughWalls;
        return this;
    }

    public TargetSelectorBuilder saveCurrentTarget(BooleanSupplier booleanSupplier) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, booleanSupplier, (accessReceiver, accessValue) -> {
            accessReceiver.saveCurrentTarget = accessValue;
        });
        return this;
    }

    public TargetSelectorBuilder elytraHighPriority(BooleanSupplier elytraHighPriority) {
        this.elytraHighPriority = elytraHighPriority;
        return this;
    }

    public <T extends Entity> TargetSelectorBuilder filterEntities(Class<T> type, Predicate<T> predicate, int intValue) {
        super.entityFilters.add(new TargetRule<>(type, predicate, intValue));
        return this;
    }
}
