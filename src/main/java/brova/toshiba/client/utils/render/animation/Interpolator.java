package brova.toshiba.client.utils.render.animation;

public abstract class Interpolator {

    private static final double cachedCoordinate = 1.0E-12;

    public static final Interpolator DISCRETE = new DiscreteInterpolator();

    public static final Interpolator LINEAR = new LinearInterpolator();

    public static final Interpolator EASE_BOTH = new EaseBothInterpolator();

    public static final Interpolator EASE_IN = new EaseInInterpolator();

    public static final Interpolator EASE_OUT = new EaseOutInterpolator();

    public boolean interpolate(boolean flag, boolean flagValue, double doubleValue) {
        return Math.abs(this.curve(doubleValue) - 1.0) < 1.0E-12 ? flagValue : flag;
    }

    public double interpolate(double doubleValue, double doubleValueValue, double doubleValueResult) {
        return doubleValue + (doubleValueValue - doubleValue) * this.curve(doubleValueResult);
    }

    public int interpolate(int intValue, int intValueValue, double doubleValue) {
        return intValue + (int) Math.round((intValueValue - intValue) * this.curve(doubleValue));
    }

    public long interpolate(long longValue, long longValueValue, double doubleValue) {
        return longValue + Math.round((longValueValue - longValue) * this.curve(doubleValue));
    }

    static double calculateDouble(double doubleValue) {
        return doubleValue < 0.0 ? 0.0 : (doubleValue > 1.0 ? 1.0 : doubleValue);
    }

    public abstract double curve(double doubleValue);
}
