package brova.toshiba.client.utils.neuro;

public final class CombatMotionSchema {

    public static final int DATA_VERSION = 10;

    public static final int CONTEXT_COUNT = 39;

    public static final int MOTION_OFFSET = 39;

    public static final int DERIVED_OFFSET = 80;

    public static final int FEATURE_COUNT = 88;

    public static final String NAME = "causal-combat-motion-v5";

    private CombatMotionSchema() {
    }

    public static float rotationGcd(float floatValue) {
        float floatValueValue = floatValue * 0.6F + 0.2F;
        return floatValueValue * floatValueValue * floatValueValue * 1.2F;
    }

    public static float targetYawDelta(float floatValue, float floatValueValue) {
        float floatValueResult = (floatValue + floatValueValue) % 360.0F;
        if (floatValueResult >= 180.0F) {
            floatValueResult -= 360.0F;
        }
        if (floatValueResult < -180.0F) {
            floatValueResult += 360.0F;
        }
        return floatValueResult;
    }
}
