package brova.toshiba.client.utils.render.animation;

public class EaseBothInterpolator extends Interpolator {

    @Override
    public double curve(double doubleValue) {
        return Interpolator.calculateDouble(doubleValue < 0.2 ? 3.125 * doubleValue * doubleValue : (doubleValue > 0.8 ? -3.125 * doubleValue * doubleValue + 6.25 * doubleValue - 2.125 : 1.25 * doubleValue - 0.125));
    }

    @Override
    public String toString() {
        return "Interpolator.EASE_BOTH";
    }
}
