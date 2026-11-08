package brova.toshiba.client.utils.render.animation;

public class DiscreteInterpolator extends Interpolator {

    @Override
    public double curve(double doubleValue) {
        return Math.abs(doubleValue - 1.0) < 1.0E-12 ? 1.0 : 0.0;
    }

    @Override
    public String toString() {
        return "Interpolator.DISCRETE";
    }
}
