package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.ClientTickEndEvent;
import brova.toshiba.client.event.impl.VelocityEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.network.PacketUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrimGlide extends Module {

    public static GrimGlide INSTANCE = new GrimGlide();

    private int cachedCount;

    private boolean stateFlag;

    public GrimGlide() {
        super("GrimGlide", "Флай без фейерверков под GrimAC", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        this.cachedCount = 0;
        this.stateFlag = false;
        super.onEnable();
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null) {
            if (brova.toshiba.client.utils.runtime.AccessGuard.invoke(eventPacket, new Object[] {}, (accessReceiver, accessArguments) -> ((EventPacket) accessReceiver).getType()) == Type.RECEIVE && eventPacket.getPacket() instanceof PlayerPositionLookS2CPacket) {
                this.cachedCount = 2;
                this.stateFlag = true;
            }
            if (brova.toshiba.client.utils.runtime.AccessGuard.invoke(eventPacket, new Object[] {}, (accessReceiver, accessArguments) -> ((EventPacket) accessReceiver).getType()) == Type.SEND && eventPacket.getPacket() instanceof PlayerMoveC2SPacket) {
                if (mc.player.isGliding() && this.cachedCount == 0 && !this.stateFlag) {
                    PacketUtil.sendSilentPacket(new OnGroundOnly(true, true));
                    eventPacket.cancel();
                }
                this.stateFlag = false;
            }
        }
    }

    @EventHandler
    public void onTick(ClientTickEndEvent event) {
        if (this.cachedCount > 0) {
            this.cachedCount--;
        }
    }

    @EventHandler
    public void onCalcGlidingVelocity(VelocityEvent event) {
        if (mc.player != null && mc.player.isGliding()) {
            Vec3d velocity = mc.player.getVelocity();
            Vec3d rotationVector = mc.player.getRotationVector();
            float floatValue = mc.player.getPitch() * (float) (Math.PI / 180.0);
            double squareRoot = Math.sqrt(rotationVector.x * rotationVector.x + rotationVector.z * rotationVector.z);
            double doubleValue = velocity.horizontalLength();
            boolean flag = mc.player.getVelocity().y <= 0.0;
            double doubleValueValue = flag && mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) ? Math.min(mc.player.getFinalGravity(), 0.01) : mc.player.getFinalGravity();
            double doubleValueResult = MathHelper.square(Math.cos(floatValue));
            velocity = velocity.add(0.0, doubleValueValue * (-1.0 + doubleValueResult * 0.75), 0.0);
            if (velocity.y < 0.0 && squareRoot > 0.0) {
                double doubleValueItem = velocity.y * -0.1 * doubleValueResult;
                velocity = velocity.add(rotationVector.x * doubleValueItem / squareRoot, doubleValueItem, rotationVector.z * doubleValueItem / squareRoot);
            }
            if (floatValue < 0.0F && squareRoot > 0.0) {
                double doubleValueEntry = doubleValue * -MathHelper.sin(floatValue) * 0.04F;
                velocity = velocity.add(-rotationVector.x * doubleValueEntry / squareRoot, doubleValueEntry * 3.2, -rotationVector.z * doubleValueEntry / squareRoot);
            }
            if (squareRoot > 0.0) {
                velocity = velocity.add((rotationVector.x / squareRoot * doubleValue - velocity.x) * 0.1, 0.0, (rotationVector.z / squareRoot * doubleValue - velocity.z) * 0.1);
            }
            double doubleValueCopy = Math.toRadians(mc.player.getYaw());
            double doubleValue2 = -Math.sin(doubleValueCopy);
            double doubleValue3 = Math.cos(doubleValueCopy);
            if (this.cachedCount >= 1) {
                double doubleValue4 = 0.09F;
                event.setVelocity(velocity.multiply(0.99F, 0.98F, 0.99F).add(doubleValue2 * doubleValue4, 0.03F, doubleValue3 * doubleValue4));
            } else {
                event.setVelocity(velocity.multiply(0.3F, 0.3F, 0.3F));
            }
        }
    }
}
