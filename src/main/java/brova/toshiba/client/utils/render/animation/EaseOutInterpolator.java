package brova.toshiba.client.utils.render.animation;

public class EaseOutInterpolator extends Interpolator {

    @Override
    public double curve(double doubleValue) {
        return Interpolator.calculateDouble(doubleValue > 0.8 ? -2.7777777777777777 * doubleValue * doubleValue + 5.555555555555555 * doubleValue + -1.7777777777777777 : 1.1111111111111112 * doubleValue);
    }

    @Override
    public String toString() {
        return "Interpolator.EASE_OUT";
    }
}
