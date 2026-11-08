package brova.toshiba.client.utils.render.animation;

public class LinearInterpolator extends Interpolator {

    @Override
    public double curve(double doubleValue) {
        return doubleValue;
    }

    @Override
    public String toString() {
        return "Interpolator.LINEAR";
    }
}
