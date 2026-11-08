package brova.toshiba.mixin;

import java.util.Map;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCooldownManager.class)
public interface ItemCooldownManagerAccessor {

    @Accessor("entries")
    Map<Identifier, ?> toshiba$getEntries();

    @Accessor("tick")
    int toshiba$getTick();
}
