package brova.toshiba.client.utils.rotate.modes.shard;

import java.util.Random;
import net.minecraft.util.math.MathHelper;

public final class SmoothedRandomScalar {

    private final float minimum;

    private final float maximum;

    float value;

    private float targetValue;

    private float smoothingTime;

    private float retargetDelay;

    SmoothedRandomScalar(float minimum, float maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.reset();
    }

    void reset() {
        this.value = this.targetValue = (this.minimum + this.maximum) * 0.5F;
        this.smoothingTime = 0.1F;
        this.retargetDelay = 0.0F;
    }

    void update(Random random) {
        this.retargetDelay -= 0.004166667F;
        if (this.retargetDelay <= 0.0F) {
            this.targetValue = this.minimum + random.nextFloat() * (this.maximum - this.minimum);
            this.smoothingTime = 0.06F + random.nextFloat() * 0.18F;
            this.retargetDelay = 0.1F + random.nextFloat() * 0.5F;
        }
        float floatValue = (float) (1.0 - Math.exp(-0.004166667F / this.smoothingTime));
        this.value = MathHelper.clamp(this.value + (this.targetValue - this.value) * floatValue, this.minimum, this.maximum);
    }
}
