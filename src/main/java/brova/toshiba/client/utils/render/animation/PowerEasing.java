package brova.toshiba.client.utils.render.animation;

import brova.toshiba.client.utils.animation.Easing;

public final class PowerEasing {

    public static final double c1 = 1.70158;

    public static final double c2 = 2.5949095;

    public static final double c3 = 2.70158;

    public static final double c4 = Math.PI * 2.0 / 3.0;

    public static final double c5 = Math.PI * 4.0 / 9.0;

    public static final Easing LINEAR = input -> input;

    public static final Easing QUAD_IN = powIn(2);

    public static final Easing QUAD_OUT = powOut(2);

    public static final Easing QUAD_IN_OUT = powIN_OUT(2.0);

    public static final Easing CUBIC_IN = powIn(3);

    public static final Easing CUBIC_OUT = powOut(3);

    public static final Easing CUBIC_IN_OUT = powIN_OUT(3.0);

    public static final Easing QUART_IN = powIn(4);

    public static final Easing QUART_OUT = powOut(4);

    public static final Easing QUART_IN_OUT = powIN_OUT(4.0);

    public static final Easing QUINT_IN = powIn(5);

    public static final Easing QUINT_OUT = powOut(5);

    public static final Easing QUINT_IN_OUT = powIN_OUT(5.0);

    public static final Easing SINE_IN = inputValue -> 1.0 - Math.cos(inputValue * Math.PI / 2.0);

    public static final Easing SINE_OUT = inputResult -> Math.sin(inputResult * Math.PI / 2.0);

    public static final Easing SINE_IN_OUT = inputItem -> -(Math.cos(Math.PI * inputItem) - 1.0) / 2.0;

    public static final Easing CIRC_IN = inputEntry -> 1.0 - Math.sqrt(1.0 - Math.pow(inputEntry, 2.0));

    public static final Easing CIRC_OUT = inputCopy -> Math.sqrt(1.0 - Math.pow(inputCopy - 1.0, 2.0));

    public static final Easing CIRC_IN_OUT = input2 -> input2 < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * input2, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * input2 + 2.0, 2.0)) + 1.0) / 2.0;

    public static final Easing ELASTIC_IN = input3 -> input3 != 0.0 && input3 != 1.0 ? Math.pow(-2.0, 10.0 * input3 - 10.0) * Math.sin((input3 * 10.0 - 10.75) * (Math.PI * 2.0 / 3.0)) : input3;

    public static final Easing ELASTIC_OUT = input4 -> input4 != 0.0 && input4 != 1.0 ? Math.pow(2.0, -10.0 * input4) * Math.sin((input4 * 10.0 - 0.75) * (Math.PI * 2.0 / 3.0)) + 1.0 : input4;

    public static final Easing ELASTIC_IN_OUT = input5 -> {
        if (input5 != 0.0 && input5 != 1.0) {
            return input5 < 0.5 ? -(Math.pow(2.0, 20.0 * input5 - 10.0) * Math.sin((20.0 * input5 - 11.125) * (Math.PI * 4.0 / 9.0))) / 2.0 : Math.pow(2.0, -20.0 * input5 + 10.0) * Math.sin((20.0 * input5 - 11.125) * (Math.PI * 4.0 / 9.0)) / 2.0 + 1.0;
        } else {
            return input5;
        }
    };

    public static final Easing EXPO_IN = input6 -> input6 != 0.0 ? Math.pow(2.0, 10.0 * input6 - 10.0) : input6;

    public static final Easing EXPO_OUT = input7 -> input7 != 1.0 ? 1.0 - Math.pow(2.0, -10.0 * input7) : input7;

    public static final Easing EXPO_IN_OUT = input8 -> {
        if (input8 != 0.0 && input8 != 1.0) {
            return input8 < 0.5 ? Math.pow(2.0, 20.0 * input8 - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * input8 + 10.0)) / 2.0;
        } else {
            return input8;
        }
    };

    public static final Easing BACK_IN = input9 -> 2.70158 * Math.pow(input9, 3.0) - 1.70158 * Math.pow(input9, 2.0);

    public static final Easing BACK_OUT = input10 -> 1.0 + 2.70158 * Math.pow(input10 - 1.0, 3.0) + 1.70158 * Math.pow(input10 - 1.0, 2.0);

    public static final Easing BACK_IN_OUT = input11 -> input11 < 0.5 ? Math.pow(2.0 * input11, 2.0) * (7.189819 * input11 - 2.5949095) / 2.0 : (Math.pow(2.0 * input11 - 2.0, 2.0) * (3.5949095 * (input11 * 2.0 - 2.0) + 2.5949095) + 2.0) / 2.0;

    public static final Easing BOUNCE_OUT = input12 -> {
        double doubleValue = 7.5625;
        double doubleValueValue = 2.75;
        if (input12 < 1.0 / doubleValueValue) {
            return doubleValue * Math.pow(input12, 2.0);
        } else if (input12 < 2.0 / doubleValueValue) {
            return doubleValue * Math.pow(input12 - 1.5 / doubleValueValue, 2.0) + 0.75;
        } else {
            return input12 < 2.5 / doubleValueValue ? doubleValue * Math.pow(input12 - 2.25 / doubleValueValue, 2.0) + 0.9375 : doubleValue * Math.pow(input12 - 2.625 / doubleValueValue, 2.0) + 0.984375;
        }
    };

    public static final Easing BOUNCE_IN = input13 -> 1.0 - BOUNCE_OUT.ease(1.0 - input13);

    public static final Easing BOUNCE_IN_OUT = input14 -> input14 < 0.5 ? (1.0 - BOUNCE_OUT.ease(1.0 - 2.0 * input14)) / 2.0 : (1.0 + BOUNCE_OUT.ease(2.0 * input14 - 1.0)) / 2.0;

    public static Easing powIn(double doubleValue) {
        return input -> Math.pow(input, doubleValue);
    }

    public static Easing powIn(int intValue) {
        double doubleValue = intValue;
        return brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] { doubleValue }, (accessReceiver, accessArguments) -> PowerEasing.powIn(((double) accessArguments[0])));
    }

    public static Easing powOut(double doubleValue) {
        return input -> 1.0 - Math.pow(1.0 - input, doubleValue);
    }

    public static Easing powOut(int intValue) {
        return powOut((double) intValue);
    }

    public static Easing powIN_OUT(double doubleValue) {
        return input -> input < 0.5 ? Math.pow(2.0, doubleValue - 1.0) * Math.pow(input, doubleValue) : 1.0 - Math.pow(-2.0 * input + 2.0, doubleValue) / 2.0;
    }

    private PowerEasing() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
