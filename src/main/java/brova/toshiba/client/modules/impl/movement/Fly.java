package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.MovePositionEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class Fly extends Module {

    public static Fly INSTANCE = new Fly();

    private final ModeSetting mode = new ModeSetting("Режим", "Обычный", "Обычный");

    private final FloatSetting speed = new FloatSetting("Скорость", 3.0F, 0.1F, 10.0F, 0.1F);

    private boolean flightStarted;

    private boolean flightResetPending;

    private boolean leaveModePending;

    private double y;

    public Fly() {
        super("Fly", "Полёт", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode, this.speed);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.flightStarted = false;
        this.flightResetPending = false;
        this.leaveModePending = false;
        this.y = 0.0;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.mode.is("KTLeave")) {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((Fly) accessReceiver).updateOnGroundWithBlockState();
                });
            } else if (this.mode.is("FunTime")) {
                if (this.isCollisionShapeWithExpand()) {
                    Vec3d velocity = mc.player.getVelocity();
                    mc.player.setVelocity(velocity.x, 0.5, velocity.z);
                }
            } else {
                double doubleValue = this.speed.get();
                float floatValue = (float) Math.toRadians(mc.player.getYaw());
                double doubleValueValue = 0.0;
                double doubleValueResult = 0.0;
                double doubleValueItem = 0.0;
                double doubleValueEntry = 0.0;
                double doubleValueCopy = 0.0;
                if (mc.options.forwardKey.isPressed()) {
                    doubleValueEntry++;
                }
                if (mc.options.backKey.isPressed()) {
                    doubleValueEntry--;
                }
                if (mc.options.leftKey.isPressed()) {
                    doubleValueCopy++;
                }
                if (mc.options.rightKey.isPressed()) {
                    doubleValueCopy--;
                }
                if (doubleValueEntry != 0.0 || doubleValueCopy != 0.0) {
                    double doubleValue2 = Math.atan2(doubleValueEntry, doubleValueCopy) - (Math.PI / 2);
                    doubleValueValue = -Math.sin(floatValue + doubleValue2) * doubleValue;
                    doubleValueItem = Math.cos(floatValue + doubleValue2) * doubleValue;
                }
                if (mc.options.jumpKey.isPressed()) {
                    doubleValueResult = doubleValue;
                } else if (mc.options.sneakKey.isPressed()) {
                    doubleValueResult = -doubleValue;
                }
                mc.player.setVelocity(new Vec3d(doubleValueValue, doubleValueResult, doubleValueItem));
            }
        }
    }

    @EventHandler
    public void onMove(MovePositionEvent event) {
        if (mc.player != null && this.mode.is("KTLeave") && this.leaveModePending) {
            mc.player.noClip = true;
            if (this.y - mc.player.getY() < 100.0) {
                event.setMovePos(new Vec3d(mc.player.getVelocity().x, -100.0, mc.player.getVelocity().z));
            } else {
                this.leaveModePending = false;
                mc.player.noClip = false;
            }
        }
    }

    private void updateOnGroundWithBlockState() {
        BlockPos blockPos = mc.player.getBlockPos();
        BlockPos position = blockPos.down();
        boolean flag = this.isOf(mc.world.getBlockState(blockPos)) || this.isOf(mc.world.getBlockState(position));
        if (!flag && !this.leaveModePending) {
            this.flightStarted = false;
            this.flightResetPending = false;
            this.leaveModePending = false;
        } else if (mc.player.isOnGround() && !this.flightStarted && !this.leaveModePending) {
            this.flightStarted = true;
            this.flightResetPending = false;
        } else {
            if (this.flightStarted && !this.flightResetPending && !mc.player.isOnGround()) {
                this.flightResetPending = true;
            }
            if (this.flightStarted && this.flightResetPending && mc.player.isOnGround() && !this.leaveModePending) {
                this.leaveModePending = true;
                this.y = mc.player.getY();
                this.flightStarted = false;
                this.flightResetPending = false;
            }
        }
    }

    private boolean isOf(BlockState blockState) {
        return blockState.isOf(Blocks.SNOW) || blockState.isOf(Blocks.CHAIN);
    }

    private boolean isCollisionShapeWithExpand() {
        if (mc.world == null) {
            return false;
        } else {
            Box box = mc.player.getBoundingBox().expand(0.15);
            int roundedDown = (int) Math.floor(box.minX);
            int roundedDownValue = (int) Math.floor(box.minY);
            int roundedDownResult = (int) Math.floor(box.minZ);
            int roundedDownItem = (int) Math.floor(box.maxX);
            int roundedDownEntry = (int) Math.floor(box.maxY);
            int roundedDownCopy = (int) Math.floor(box.maxZ);
            for (int index = roundedDown; index <= roundedDownItem; index++) {
                for (int indexValue = roundedDownValue; indexValue <= roundedDownEntry; indexValue++) {
                    for (int indexResult = roundedDownResult; indexResult <= roundedDownCopy; indexResult++) {
                        BlockPos position = new BlockPos(index, indexValue, indexResult);
                        BlockState blockState = mc.world.getBlockState(position);
                        if (!blockState.isFullCube(mc.world, position) && !blockState.getCollisionShape(mc.world, position).isEmpty() && blockState.getCollisionShape(mc.world, position).getBoundingBoxes().stream().map(input -> input.offset(position)).anyMatch(inputValue -> inputValue.intersects(box))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.flightStarted = false;
        this.flightResetPending = false;
        this.leaveModePending = false;
        this.y = 0.0;
        if (mc.player != null) {
            mc.player.noClip = false;
            mc.player.setVelocity(Vec3d.ZERO);
        }
    }
}
