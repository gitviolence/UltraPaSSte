package brova.toshiba.client.utils.render.animation;

public class EasingFunctions {

    public static float ease(EasingType easingType, float floatValue) {
        floatValue = Math.max(0.0F, Math.min(1.0F, floatValue));
        switch(easingType) {
            case EASE_IN_SINE:
                return easeInSine(floatValue);
            case EASE_OUT_SINE:
                return easeOutSine(floatValue);
            case EASE_IN_OUT_SINE:
                return easeInOutSine(floatValue);
            case EASE_IN_QUAD:
                return easeInQuad(floatValue);
            case EASE_OUT_QUAD:
                return easeOutQuad(floatValue);
            case EASE_IN_OUT_QUAD:
                return easeInOutQuad(floatValue);
            case EASE_IN_CUBIC:
                return easeInCubic(floatValue);
            case EASE_OUT_CUBIC:
                return easeOutCubic(floatValue);
            case EASE_IN_OUT_CUBIC:
                return easeInOutCubic(floatValue);
            case EASE_IN_QUART:
                return easeInQuart(floatValue);
            case EASE_OUT_QUART:
                return easeOutQuart(floatValue);
            case EASE_IN_OUT_QUART:
                return easeInOutQuart(floatValue);
            case EASE_IN_QUINT:
                return easeInQuint(floatValue);
            case EASE_OUT_QUINT:
                return easeOutQuint(floatValue);
            case EASE_IN_OUT_QUINT:
                return easeInOutQuint(floatValue);
            case EASE_IN_EXPO:
                return easeInExpo(floatValue);
            case EASE_OUT_EXPO:
                return easeOutExpo(floatValue);
            case EASE_IN_OUT_EXPO:
                return easeInOutExpo(floatValue);
            case EASE_IN_CIRC:
                return easeInCirc(floatValue);
            case EASE_OUT_CIRC:
                return easeOutCirc(floatValue);
            case EASE_IN_OUT_CIRC:
                return easeInOutCirc(floatValue);
            case EASE_IN_BACK:
                return easeInBack(floatValue);
            case EASE_OUT_BACK:
                return easeOutBack(floatValue);
            case EASE_IN_OUT_BACK:
                return easeInOutBack(floatValue);
            case EASE_IN_ELASTIC:
                return easeInElastic(floatValue);
            case EASE_OUT_ELASTIC:
                return easeOutElastic(floatValue);
            case EASE_IN_OUT_ELASTIC:
                return easeInOutElastic(floatValue);
            case EASE_IN_BOUNCE:
                return easeInBounce(floatValue);
            case EASE_OUT_BOUNCE:
                return easeOutBounce(floatValue);
            case EASE_IN_OUT_BOUNCE:
                return easeInOutBounce(floatValue);
            default:
                return floatValue;
        }
    }

    public static float easeInSine(float floatValue) {
        return 1.0F - (float) Math.cos(floatValue * Math.PI / 2.0);
    }

    public static float easeOutSine(float floatValue) {
        return (float) Math.sin(floatValue * Math.PI / 2.0);
    }

    public static float easeInOutSine(float floatValue) {
        return -((float) (Math.cos(Math.PI * floatValue) - 1.0)) / 2.0F;
    }

    public static float easeInQuad(float floatValue) {
        return floatValue * floatValue;
    }

    public static float easeOutQuad(float floatValue) {
        return 1.0F - (1.0F - floatValue) * (1.0F - floatValue);
    }

    public static float easeInOutQuad(float floatValue) {
        return floatValue < 0.5F ? 2.0F * floatValue * floatValue : 1.0F - (float) Math.pow(-2.0 * floatValue + 2.0, 2.0) / 2.0F;
    }

    public static float easeInCubic(float floatValue) {
        return floatValue * floatValue * floatValue;
    }

    public static float easeOutCubic(float floatValue) {
        return 1.0F - (float) Math.pow(1.0 - floatValue, 3.0);
    }

    public static float easeInOutCubic(float floatValue) {
        return floatValue < 0.5F ? 4.0F * floatValue * floatValue * floatValue : 1.0F - (float) Math.pow(-2.0 * floatValue + 2.0, 3.0) / 2.0F;
    }

    public static float easeInQuart(float floatValue) {
        return floatValue * floatValue * floatValue * floatValue;
    }

    public static float easeOutQuart(float floatValue) {
        return 1.0F - (float) Math.pow(1.0 - floatValue, 4.0);
    }

    public static float easeInOutQuart(float floatValue) {
        return floatValue < 0.5F ? 8.0F * floatValue * floatValue * floatValue * floatValue : 1.0F - (float) Math.pow(-2.0 * floatValue + 2.0, 4.0) / 2.0F;
    }

    public static float easeInQuint(float floatValue) {
        return floatValue * floatValue * floatValue * floatValue * floatValue;
    }

    public static float easeOutQuint(float floatValue) {
        return 1.0F - (float) Math.pow(1.0 - floatValue, 5.0);
    }

    public static float easeInOutQuint(float floatValue) {
        return floatValue < 0.5F ? 16.0F * floatValue * floatValue * floatValue * floatValue * floatValue : 1.0F - (float) Math.pow(-2.0 * floatValue + 2.0, 5.0) / 2.0F;
    }

    public static float easeInExpo(float floatValue) {
        return floatValue == 0.0F ? 0.0F : (float) Math.pow(2.0, 10.0 * floatValue - 10.0);
    }

    public static float easeOutExpo(float floatValue) {
        return floatValue == 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0, -10.0 * floatValue);
    }

    public static float easeInOutExpo(float floatValue) {
        if (floatValue == 0.0F) {
            return 0.0F;
        } else if (floatValue == 1.0F) {
            return 1.0F;
        } else {
            return floatValue < 0.5F ? (float) Math.pow(2.0, 20.0 * floatValue - 10.0) / 2.0F : (2.0F - (float) Math.pow(2.0, -20.0 * floatValue + 10.0)) / 2.0F;
        }
    }

    public static float easeInCirc(float floatValue) {
        return 1.0F - (float) Math.sqrt(1.0 - Math.pow(floatValue, 2.0));
    }

    public static float easeOutCirc(float floatValue) {
        return (float) Math.sqrt(1.0 - Math.pow(floatValue - 1.0, 2.0));
    }

    public static float easeInOutCirc(float floatValue) {
        return floatValue < 0.5F ? (1.0F - (float) Math.sqrt(1.0 - Math.pow(2.0 * floatValue, 2.0))) / 2.0F : ((float) Math.sqrt(1.0 - Math.pow(-2.0 * floatValue + 2.0, 2.0)) + 1.0F) / 2.0F;
    }

    public static float easeInBack(float floatValue) {
        float floatValueValue = 1.70158F;
        float floatValueResult = floatValueValue + 1.0F;
        return floatValueResult * floatValue * floatValue * floatValue - floatValueValue * floatValue * floatValue;
    }

    public static float easeOutBack(float floatValue) {
        float floatValueValue = 1.70158F;
        float floatValueResult = floatValueValue + 1.0F;
        return 1.0F + floatValueResult * (float) Math.pow(floatValue - 1.0, 3.0) + floatValueValue * (float) Math.pow(floatValue - 1.0, 2.0);
    }

    public static float easeInOutBack(float floatValue) {
        float floatValueValue = 1.70158F;
        float floatValueResult = floatValueValue * 1.525F;
        return floatValue < 0.5F ? (float) Math.pow(2.0 * floatValue, 2.0) * ((floatValueResult + 1.0F) * 2.0F * floatValue - floatValueResult) / 2.0F : ((float) Math.pow(2.0 * floatValue - 2.0, 2.0) * ((floatValueResult + 1.0F) * (floatValue * 2.0F - 2.0F) + floatValueResult) + 2.0F) / 2.0F;
    }

    public static float easeInElastic(float floatValue) {
        float floatValueValue = (float) (Math.PI * 2.0 / 3.0);
        return floatValue == 0.0F ? 0.0F : (floatValue == 1.0F ? 1.0F : -((float) Math.pow(2.0, 10.0 * floatValue - 10.0)) * (float) Math.sin((floatValue * 10.0F - 10.75F) * floatValueValue));
    }

    public static float easeOutElastic(float floatValue) {
        float floatValueValue = (float) (Math.PI * 2.0 / 3.0);
        return floatValue == 0.0F ? 0.0F : (floatValue == 1.0F ? 1.0F : (float) Math.pow(2.0, -10.0 * floatValue) * (float) Math.sin((floatValue * 10.0F - 0.75F) * floatValueValue) + 1.0F);
    }

    public static float easeInOutElastic(float floatValue) {
        float floatValueValue = (float) Math.PI * 4.0F / 9.0F;
        if (floatValue == 0.0F) {
            return 0.0F;
        } else if (floatValue == 1.0F) {
            return 1.0F;
        } else {
            return floatValue < 0.5F ? -((float) Math.pow(2.0, 20.0 * floatValue - 10.0) * (float) Math.sin((20.0F * floatValue - 11.125F) * floatValueValue)) / 2.0F : (float) Math.pow(2.0, -20.0 * floatValue + 10.0) * (float) Math.sin((20.0F * floatValue - 11.125F) * floatValueValue) / 2.0F + 1.0F;
        }
    }

    public static float easeOutBounce(float floatValue) {
        float floatValueValue = 7.5625F;
        float floatValueResult = 2.75F;
        if (floatValue < 1.0F / floatValueResult) {
            return floatValueValue * floatValue * floatValue;
        } else if (floatValue < 2.0F / floatValueResult) {
            float floatValueItem;
            return floatValueValue * (floatValueItem = floatValue - 1.5F / floatValueResult) * floatValueItem + 0.75F;
        } else {
            float floatValueEntry;
            float floatValueCopy;
            return floatValue < 2.5F / floatValueResult ? floatValueValue * (floatValueEntry = floatValue - 2.25F / floatValueResult) * floatValueEntry + 0.9375F : floatValueValue * (floatValueCopy = floatValue - 2.625F / floatValueResult) * floatValueCopy + 0.984375F;
        }
    }

    public static float easeInBounce(float floatValue) {
        return 1.0F - easeOutBounce(1.0F - floatValue);
    }

    public static float easeInOutBounce(float floatValue) {
        return floatValue < 0.5F ? (1.0F - easeOutBounce(1.0F - 2.0F * floatValue)) / 2.0F : (1.0F + easeOutBounce(2.0F * floatValue - 1.0F)) / 2.0F;
    }
}
