package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.ModeSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;

public final class HighJump extends Module {

    public static HighJump INSTANCE = new HighJump();

    private final ModeSetting mode = new ModeSetting("Режим", "Шалкер Grim", "Шалкер Grim", "Лодка", "СлаймБлок", "FunTime");

    private boolean wasOnGround;

    private double cachedCoordinate;

    private int cachedCount;

    private double y;

    private boolean jumpPending;

    private boolean launchPending;

    private boolean heightReached;

    public HighJump() {
        super("HighJump", "Высокий прыжок от различных источников", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.wasOnGround = false;
        this.cachedCoordinate = 0.0;
        this.cachedCount = 0;
        this.jumpPending = false;
        this.y = 0.0;
        this.launchPending = false;
        this.heightReached = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.wasOnGround = false;
        this.cachedCoordinate = 0.0;
        this.cachedCount = 0;
        this.jumpPending = false;
        this.y = 0.0;
        this.launchPending = false;
        this.heightReached = false;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.cachedCount > 0) {
                this.cachedCount += -1;
            }
            if (this.mode.is("Шалкер Grim")) {
                this.applyVelocityWithBlockPos();
            }
            if (this.mode.is("СлаймБлок")) {
                this.applyVelocityWithDown();
            }
            if (this.mode.is("Лодка")) {
                this.applyVelocityWithVehicle();
            }
            if (this.mode.is("FunTime")) {
                this.applyOnGroundWithBlockState();
            }
        }
    }

    private void applyVelocityWithBlockPos() {
        if (mc.currentScreen instanceof ShulkerBoxScreen) {
            BlockPos blockPos = mc.player.getBlockPos();
            BlockPos[] positions = new BlockPos[] { blockPos.down(), blockPos, blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west() };
            boolean flag = false;
            for (BlockPos position : positions) {
                BlockState blockState = mc.world.getBlockState(position);
                if (blockState.getBlock() instanceof ShulkerBoxBlock) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                mc.player.setVelocity(mc.player.getVelocity().x, 2.0, mc.player.getVelocity().z);
                mc.player.closeHandledScreen();
            }
        }
    }

    private void applyVelocityWithDown() {
        double cachedCoordinate = mc.player.getVelocity().y;
        BlockPos position = mc.player.getBlockPos().down();
        BlockPos positionValue = mc.player.getBlockPos().down(2);
        boolean flag = mc.world.getBlockState(position).isOf(Blocks.SLIME_BLOCK) || mc.world.getBlockState(positionValue).isOf(Blocks.SLIME_BLOCK);
        if (this.cachedCoordinate < -0.1 && cachedCoordinate > 0.1 && flag && this.cachedCount == 0) {
            double doubleValue = cachedCoordinate * 2.0;
            mc.player.setVelocity(mc.player.getVelocity().x, doubleValue, mc.player.getVelocity().z);
            this.cachedCount = 5;
        }
        this.cachedCoordinate = cachedCoordinate;
    }

    private void applyVelocityWithVehicle() {
        boolean wasOnGround = mc.player.getVehicle() instanceof BoatEntity;
        if (this.wasOnGround && !wasOnGround && this.cachedCount == 0) {
            mc.player.setVelocity(mc.player.getVelocity().x, 1.5, mc.player.getVelocity().z);
            this.cachedCount = 20;
        }
        this.wasOnGround = wasOnGround;
    }

    private void applyOnGroundWithBlockState() {
        BlockPos blockPos = mc.player.getBlockPos();
        BlockPos position = blockPos.down();
        boolean flag = this.isOf(mc.world.getBlockState(blockPos)) || this.isOf(mc.world.getBlockState(position));
        if (!flag) {
            this.launchPending = false;
            this.heightReached = false;
            this.jumpPending = false;
        } else if (mc.player.isOnGround() && !this.launchPending && !this.jumpPending) {
            mc.player.jump();
            this.launchPending = true;
            this.heightReached = false;
        } else {
            if (this.launchPending && !this.heightReached && !mc.player.isOnGround()) {
                this.heightReached = true;
            }
            if (this.launchPending && this.heightReached && mc.player.isOnGround() && !this.jumpPending) {
                this.jumpPending = true;
                this.y = mc.player.getY();
                this.launchPending = false;
                this.heightReached = false;
            }
            if (this.jumpPending) {
                if (mc.player.getY() + -this.y < 1.0) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 1.0, mc.player.getVelocity().z);
                } else {
                    this.jumpPending = false;
                }
            }
        }
    }

    private boolean isOf(BlockState blockState) {
        return blockState.isOf(Blocks.SNOW) || blockState.isOf(Blocks.CHAIN);
    }
}
