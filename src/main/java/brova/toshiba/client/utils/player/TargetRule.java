package brova.toshiba.client.utils.player;

import java.util.function.Predicate;
import net.minecraft.entity.Entity;

public record TargetRule<T extends Entity>(Class<T> type, Predicate<T> predicate, int priority) {
}
