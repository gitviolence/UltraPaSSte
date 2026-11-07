package brova.toshiba.client.utils.neuro;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.utils.rotate.AimProjection;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class NeuroManager implements MinecraftAccess {

    private static final NeuroManager INSTANCE = new NeuroManager();

    private final MotionFeaturePipeline updateMotionFeaturePipeline = new MotionFeaturePipeline();

    private NeuroPredictor neuroPredictor;

    private LivingEntity targetForNextMovement;

    private LivingEntity targetForTargetTrackingSeconds;

    private long lastUpdateNanos;

    private float observe = 0.05F;

    private boolean stateFlag;

    private NeuroManager() {
        EventBus.register(this);
    }

    public static NeuroManager getInstance() {
        return INSTANCE;
    }

    public NeuroPredictor getActiveModel() {
        if (!this.stateFlag) {
            this.stateFlag = true;
            try (InputStream resourceAsStream = NeuroManager.class.getResourceAsStream("/assets/toshiba/neuro/dgsgs.nrm")) {
                if (resourceAsStream != null) {
                    this.neuroPredictor = NeuroPredictor.load(resourceAsStream);
                }
            } catch (IOException failure) {
            }
        }
        return this.neuroPredictor;
    }

    @EventHandler
    public void onAttackEntity(TargetPlayerEvent event) {
        if (mc.player != null && event.getPlayer() == mc.player && event.getTarget() instanceof LivingEntity entity) {
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, entity, (accessReceiver, accessValue) -> {
                accessReceiver.targetForTargetTrackingSeconds = accessValue;
            });
            this.lastUpdateNanos = System.nanoTime();
        }
    }

    public RotationStep nextMovement(LivingEntity targetForNextMovement, float floatValue, float floatValueValue, float observe, Box box, Vec3d position) {
        NeuroPredictor activeModel = this.getActiveModel();
        if (activeModel != null && targetForNextMovement != null && mc.player != null && mc.world != null && box != null) {
            if (targetForNextMovement != this.targetForNextMovement) {
                this.resetPlayback();
                this.targetForNextMovement = targetForNextMovement;
            }
            float[] values = CombatContextFeatures.capture(targetForNextMovement, box, position, floatValue, floatValueValue, observe, this.getTargetTrackingSeconds(targetForNextMovement));
            this.observe = observe;
            return this.updateMotionFeaturePipeline.update(activeModel, values, floatValueValue, CombatMotionSchema.rotationGcd(mc.options.getMouseSensitivity().getValue().floatValue()), new PitchStepBounds(-90.0F - floatValueValue, 90.0F - floatValueValue), AimProjection.from(box, mc.player.getEyePos(), position == null ? CombatContextFeatures.defaultPoint(box) : position));
        } else {
            this.resetPlayback();
            return RotationStep.ZERO;
        }
    }

    public void movementApplied(LivingEntity entity, float floatValue, float floatValueValue) {
        if (mc.player != null && this.neuroPredictor != null && entity == this.targetForNextMovement) {
            this.updateMotionFeaturePipeline.observe(MathHelper.wrapDegrees(mc.player.getYaw() - floatValue), mc.player.getPitch() - floatValueValue, CombatMotionSchema.rotationGcd(mc.options.getMouseSensitivity().getValue().floatValue()), this.observe);
        }
    }

    public void resetPlayback() {
        if (this.neuroPredictor != null) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this.neuroPredictor, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((NeuroPredictor) accessReceiver).reset();
            });
        }
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.targetForNextMovement = (LivingEntity) accessValue;
        });
        this.updateMotionFeaturePipeline.reset();
        this.observe = 0.05F;
    }

    public void shutdownForPanic() {
        this.resetPlayback();
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.targetForTargetTrackingSeconds = (LivingEntity) accessValue;
        });
        this.lastUpdateNanos = 0L;
        EventBus.unregister(this);
    }

    private float getTargetTrackingSeconds(LivingEntity entity) {
        return entity == this.targetForTargetTrackingSeconds && this.lastUpdateNanos != 0L ? MathHelper.clamp((float) (System.nanoTime() - this.lastUpdateNanos) / 1.0E9F, 0.0F, 3.0F) : 3.0F;
    }
}
