package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.Toshiba;
import brova.toshiba.client.core.OptimizationFlags;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.player.PlayerEntity;

public class Optimization extends Module {

    public static final Optimization INSTANCE = new Optimization();

    private final BooleanSetting armor = new BooleanSetting("Armor", true);

    private final BooleanSetting heldItems = new BooleanSetting("Held Items", true);

    private final BooleanSetting cape = new BooleanSetting("Cape", true);

    private final BooleanSetting elytra = new BooleanSetting("Elytra", true);

    private final BooleanSetting headItems = new BooleanSetting("Head Items", true);

    private final BooleanSetting vanillaNameTags = new BooleanSetting("Vanilla NameTags", true);

    private final BooleanSetting skinDetails = new BooleanSetting("Skin Details", true);

    private final MultiSetting elements = new MultiSetting("Elements", this.armor, this.heldItems, this.cape, this.elytra, this.headItems, this.vanillaNameTags, this.skinDetails);

    private final BooleanSetting onlyOthers = new BooleanSetting("Only Others", true);

    private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore Friends", false);

    private final FloatSetting minDistance = new FloatSetting("Min Distance", 0.0F, 0.0F, 128.0F, 1.0F);

    private Optimization() {
        super("Optimization", "Reduces player render cost", ModuleCategory.MISC);
        this.addSettings(this.elements, this.onlyOthers, this.ignoreFriends, this.minDistance);
    }

    public boolean shouldHideArmor(PlayerEntity player) {
        return this.isAliveWithFriend(player) && this.armor.isState();
    }

    public boolean shouldHideHeldItems(PlayerEntity player) {
        return this.isAliveWithFriend(player) && this.heldItems.isState();
    }

    public boolean shouldHideCape(PlayerEntity player) {
        return this.isAliveWithFriend(player) && this.cape.isState();
    }

    public boolean shouldHideElytra(PlayerEntity player) {
        return this.isAliveWithFriend(player) && this.elytra.isState();
    }

    public boolean shouldHideHeadItems(PlayerEntity player) {
        return this.isAliveWithFriend(player) && this.headItems.isState();
    }

    public boolean shouldHideVanillaNameTag(PlayerEntity player) {
        return this.isAliveWithFriend(player) && this.vanillaNameTags.isState();
    }

    public void optimizeState(PlayerEntity player, PlayerEntityRenderState playerEntityRenderState) {
        if (playerEntityRenderState != null) {
            boolean aliveWithFriend = this.isAliveWithFriend(player);
            boolean flag = aliveWithFriend && this.armor.isState();
            boolean flagValue = aliveWithFriend && this.heldItems.isState();
            boolean flagResult = aliveWithFriend && this.cape.isState();
            boolean flagItem = aliveWithFriend && this.elytra.isState();
            boolean flagEntry = aliveWithFriend && this.headItems.isState();
            boolean flagCopy = aliveWithFriend && this.vanillaNameTags.isState();
            if (playerEntityRenderState instanceof OptimizationFlags optimizationFlags) {
                optimizationFlags.toshiba$setOptimizationFlags(flag, flagValue, flagResult, flagItem, flagEntry, flagCopy);
            }
            if (aliveWithFriend) {
                if (flagResult) {
                    playerEntityRenderState.capeVisible = false;
                }
                if (this.skinDetails.isState()) {
                    playerEntityRenderState.hatVisible = false;
                    playerEntityRenderState.jacketVisible = false;
                    playerEntityRenderState.leftPantsLegVisible = false;
                    playerEntityRenderState.rightPantsLegVisible = false;
                    playerEntityRenderState.leftSleeveVisible = false;
                    playerEntityRenderState.rightSleeveVisible = false;
                    playerEntityRenderState.stuckArrowCount = 0;
                    playerEntityRenderState.stingerCount = 0;
                    playerEntityRenderState.leftShoulderParrotVariant = null;
                    playerEntityRenderState.rightShoulderParrotVariant = null;
                }
            }
        }
    }

    private boolean isAliveWithFriend(PlayerEntity player) {
        if (!this.isEnable() || mc.player == null || mc.world == null || player == null || !player.isAlive()) {
            return false;
        } else if (this.onlyOthers.isState() && player == mc.player) {
            return false;
        } else if (this.ignoreFriends.isState() && Toshiba.INSTANCE != null && Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString())) {
            return false;
        } else {
            float floatValue = this.minDistance.get();
            return floatValue <= 0.0F || mc.player.squaredDistanceTo(player) >= floatValue * floatValue;
        }
    }
}
