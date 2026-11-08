package brova.toshiba.mixin;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the cached flag used by Minecraft 1.21.4's deprecated isSolid getter. */
@Mixin(AbstractBlock.AbstractBlockState.class)
public interface BlockStateAccessor {
    @Accessor("solid")
    boolean toshiba$isSolid();
}
