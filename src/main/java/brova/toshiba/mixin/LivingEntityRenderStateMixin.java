package brova.toshiba.mixin;

import brova.toshiba.client.core.OptimizationFlags;
import brova.toshiba.client.data.SeeInvisiblesTarget;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements SeeInvisiblesTarget, OptimizationFlags {

    @Unique
    private boolean toshiba$seeInvisiblesTarget;

    @Unique
    private boolean toshiba$hideArmor;

    @Unique
    private boolean toshiba$hideHeldItems;

    @Unique
    private boolean toshiba$hideCape;

    @Unique
    private boolean toshiba$hideElytra;

    @Unique
    private boolean toshiba$hideHeadItems;

    @Unique
    private boolean toshiba$hideVanillaNameTag;

    @Override
    public boolean toshiba$isSeeInvisiblesTarget() {
        return this.toshiba$seeInvisiblesTarget;
    }

    @Override
    public void toshiba$setSeeInvisiblesTarget(boolean value) {
        this.toshiba$seeInvisiblesTarget = value;
    }

    @Override
    public boolean toshiba$hideArmor() {
        return this.toshiba$hideArmor;
    }

    @Override
    public boolean toshiba$hideHeldItems() {
        return this.toshiba$hideHeldItems;
    }

    @Override
    public boolean toshiba$hideCape() {
        return this.toshiba$hideCape;
    }

    @Override
    public boolean toshiba$hideElytra() {
        return this.toshiba$hideElytra;
    }

    @Override
    public boolean toshiba$hideHeadItems() {
        return this.toshiba$hideHeadItems;
    }

    @Override
    public boolean toshiba$hideVanillaNameTag() {
        return this.toshiba$hideVanillaNameTag;
    }

    @Override
    public void toshiba$setOptimizationFlags(boolean armor, boolean heldItems, boolean cape, boolean elytra, boolean headItems, boolean vanillaNameTag) {
        this.toshiba$hideArmor = armor;
        this.toshiba$hideHeldItems = heldItems;
        this.toshiba$hideCape = cape;
        this.toshiba$hideElytra = elytra;
        this.toshiba$hideHeadItems = headItems;
        this.toshiba$hideVanillaNameTag = vanillaNameTag;
    }
}
