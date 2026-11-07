package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class Nuker extends Module {

    public static Nuker INSTANCE = new Nuker();

    private final FloatSetting distance = new FloatSetting("Дистанция", 3.0F, 1.0F, 5.0F, 1.0F);

    private final BooleanSetting breakAllBlocks = new BooleanSetting("Ломать все блоки", false);

    private final BooleanSetting swingHand = new BooleanSetting("Анимация руки", true);

    private final Set<String> targetBlocks = new HashSet<>();

    private BlockPos blockPos;

    public Nuker() {
        super("Nuker", "Автоматически ломает блоки в радиусе", ModuleCategory.PLAYER);
        this.addSettings(this.distance, this.breakAllBlocks, this.swingHand);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            this.updateCancelBlockBreaking();
        } else if (!this.breakAllBlocks.isState() && this.targetBlocks.isEmpty()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((Nuker) accessReceiver).updateCancelBlockBreaking();
            });
        } else {
            if (!this.isLocalState()) {
                this.blockPos = this.findFilterWithBlockPos();
            }
            if (this.blockPos != null) {
                this.updateAttackBlockWithBlockBreakingProgress();
            }
        }
    }

    private boolean isLocalState() {
        return this.blockPos != null && this.isSquaredDistanceToWithOfCenter(this.blockPos) && this.isBlockStateWithAir(this.blockPos);
    }

    private BlockPos findFilterWithBlockPos() {
        int intValue = Math.round(this.distance.get());
        BlockPos blockPos = mc.player.getBlockPos();
        return BlockPos.stream(blockPos.add(-intValue, 0, -intValue), blockPos.add(intValue, intValue, intValue)).map(BlockPos::toImmutable).filter(this::isSquaredDistanceToWithOfCenter).filter(this::isBlockStateWithAir).min(Comparator.comparingDouble(input -> mc.player.squaredDistanceTo(Vec3d.ofCenter(input)))).orElse(null);
    }

    private boolean isSquaredDistanceToWithOfCenter(BlockPos position) {
        double doubleValue = this.distance.get();
        return mc.player.squaredDistanceTo(Vec3d.ofCenter(position)) <= doubleValue * doubleValue;
    }

    private boolean isBlockStateWithAir(BlockPos position) {
        BlockState blockState = mc.world.getBlockState(position);
        if (blockState == null || blockState.isAir() || blockState.getHardness(mc.world, position) < 0.0F) {
            return false;
        } else if (this.breakAllBlocks.isState()) {
            return true;
        } else {
            String normalizedText = Registries.BLOCK.getId(blockState.getBlock()).getPath().toLowerCase();
            return this.targetBlocks.contains(normalizedText);
        }
    }

    private void updateAttackBlockWithBlockBreakingProgress() {
        if (this.blockPos != null && mc.player != null && mc.interactionManager != null) {
            mc.interactionManager.attackBlock(this.blockPos, Direction.UP);
            mc.interactionManager.updateBlockBreakingProgress(this.blockPos, Direction.UP);
            if (this.swingHand.isState()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            if (mc.world.getBlockState(this.blockPos).isAir()) {
                this.updateCancelBlockBreaking();
            }
        }
    }

    private void updateCancelBlockBreaking() {
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.blockPos = (BlockPos) accessValue;
        });
        if (mc.interactionManager != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
    }

    public void addBlock(String text) {
        this.targetBlocks.add(normalizeBlockName(text));
    }

    public void removeBlock(String text) {
        this.targetBlocks.remove(normalizeBlockName(text));
    }

    public void clearBlocks() {
        this.targetBlocks.clear();
        this.updateCancelBlockBreaking();
    }

    public boolean isTargetBlock(String text) {
        return this.targetBlocks.contains(normalizeBlockName(text));
    }

    public Set<String> getTargetBlocks() {
        return new HashSet<>(this.targetBlocks);
    }

    public static String normalizeBlockName(String text) {
        if (text == null) {
            return "";
        } else {
            String textValue = text.toLowerCase().trim();
            int index = textValue.indexOf(58);
            return index >= 0 ? textValue.substring(index + 1) : textValue;
        }
    }

    @Override
    public void onDisable() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
            ((Nuker) accessReceiver).updateCancelBlockBreaking();
        });
        super.onDisable();
    }
}
