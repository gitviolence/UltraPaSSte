package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.mixin.ILivingEntity;

public class AttackTimer implements MinecraftAccess {

    private int lastAttackedTicks = 0;

    private long lastUpdateMillis = -1L;

    private boolean stateFlag = false;

    public boolean update() {
        this.stateFlag = false;
        if (mc.player == null) {
            return false;
        } else {
            int lastAttackedTicks = ((ILivingEntity) mc.player).getLastAttackedTicks();
            if (lastAttackedTicks < this.lastAttackedTicks) {
                this.lastUpdateMillis = System.currentTimeMillis();
                this.stateFlag = true;
            }
            this.lastAttackedTicks = lastAttackedTicks;
            return this.stateFlag;
        }
    }

    public boolean justAttacked() {
        return this.stateFlag;
    }

    public boolean hasAttacked() {
        return this.lastUpdateMillis >= 0L;
    }

    public long timeSinceAttack() {
        return this.lastUpdateMillis < 0L ? Long.MAX_VALUE : System.currentTimeMillis() - this.lastUpdateMillis;
    }

    public boolean inAttackWindow(long longValue) {
        return this.hasAttacked() && this.timeSinceAttack() <= longValue;
    }

    public void reset() {
        this.lastAttackedTicks = mc.player == null ? 0 : ((ILivingEntity) mc.player).getLastAttackedTicks();
        this.lastUpdateMillis = -1L;
        this.stateFlag = false;
    }
}
