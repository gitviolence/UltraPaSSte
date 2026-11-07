package brova.toshiba.client.utils.render.animation;

public class EaseInInterpolator extends Interpolator {

    @Override
    public double curve(double doubleValue) {
        return Interpolator.calculateDouble(doubleValue < 0.2 ? 2.7777777777777777 * doubleValue * doubleValue : 1.1111111111111112 * doubleValue - 0.1111111111111111);
    }

    @Override
    public String toString() {
        return "Interpolator.EASE_IN";
    }
}
