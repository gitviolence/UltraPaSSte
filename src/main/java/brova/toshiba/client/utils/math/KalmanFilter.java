package brova.toshiba.client.utils.math;

public final class KalmanFilter {

    private final double baseSpeed;

    private final double boostSpeed;

    private final double maximumSpeed;

    private double copySign;

    private double accumulatedVelocity;

    private double verticalMotion;

    private double previousPlayerY;

    private double smoothedSpeed;

    public KalmanFilter() {
        this(1.25, 1.2, Double.POSITIVE_INFINITY);
    }

    public KalmanFilter(double baseSpeed, double boostSpeed, double maximumSpeed) {
        this.baseSpeed = baseSpeed;
        this.boostSpeed = boostSpeed;
        this.maximumSpeed = maximumSpeed;
    }

    public double correct(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry, boolean flag) {
        return this.correct(doubleValue, doubleValueValue, doubleValueResult, doubleValueItem, doubleValueEntry, flag, 0.0);
    }

    public double correct(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double previousPlayerY, boolean flag, double doubleValueEntry) {
        if (Double.isFinite(doubleValue) && Double.isFinite(doubleValueValue) && Double.isFinite(doubleValueResult) && Double.isFinite(doubleValueItem) && Double.isFinite(previousPlayerY) && Double.isFinite(doubleValueEntry) && !(doubleValueResult <= 0.0) && !(doubleValueItem <= 0.0) && !(previousPlayerY <= 0.0)) {
            if (previousPlayerY != this.previousPlayerY) {
                this.accumulatedVelocity = 0.0;
                this.copySign = 0.0;
                this.smoothedSpeed = 0.0;
                this.previousPlayerY = previousPlayerY;
            }
            double doubleValueCopy = previousPlayerY * 0.5;
            double doubleValue2 = Math.abs(doubleValue) - doubleValueCopy;
            if (doubleValue2 < previousPlayerY) {
                this.reset();
                return 0.0;
            } else {
                double magnitude = Math.abs(doubleValueValue);
                if (flag && magnitude == 0.0) {
                    this.pause(doubleValueResult);
                    return 0.0;
                } else {
                    double doubleValue3 = flag && this.verticalMotion < 0.06 ? this.verticalMotion : 0.0;
                    double doubleValue4 = Math.min(0.05, doubleValueResult + doubleValue3);
                    this.verticalMotion = 0.0;
                    if (this.copySign * doubleValue < 0.0 || this.accumulatedVelocity * doubleValue < 0.0) {
                        this.copySign = 0.0;
                        this.accumulatedVelocity = 0.0;
                    }
                    double doubleValue5 = magnitude / doubleValue4;
                    boolean flagValue = doubleValueValue * doubleValue < 0.0 && magnitude > 0.0;
                    double doubleValue6 = flagValue ? Math.max(0.0, Math.min(1.0, doubleValueEntry)) : 0.0;
                    double doubleValue7 = 0.055 / (1.0 + doubleValue6 * 1.5);
                    double doubleValue8 = -Math.expm1(-doubleValue4 / doubleValue7);
                    this.smoothedSpeed = this.smoothedSpeed + (doubleValue5 - this.smoothedSpeed) * doubleValue8;
                    double doubleValue9 = flagValue ? this.boostSpeed + (1.9 - this.boostSpeed) * doubleValue6 : this.baseSpeed;
                    double doubleValue10 = Math.min(doubleValue5, this.smoothedSpeed);
                    double doubleValue11 = doubleValueItem + doubleValue10 * doubleValue6 * 1.8;
                    this.copySign = Math.copySign(Math.min(Math.abs(this.copySign), doubleValue11), this.copySign);
                    double doubleValue12 = flag ? 0.026 + 0.028 / (1.0 + this.smoothedSpeed / 75.0) : 0.04;
                    doubleValue12 /= 1.0 + doubleValue6 * 0.5;
                    double doubleValue13 = doubleValue11 * Math.tanh(doubleValue2 / (doubleValue11 * doubleValue12));
                    if (flag) {
                        doubleValue13 = this.calculateHypot(doubleValue13, doubleValue10 * doubleValue9);
                    }
                    double doubleValue14 = -Math.expm1(-doubleValue4 / doubleValue12);
                    double doubleValue15 = Math.copySign(doubleValue13, doubleValue);
                    double accumulatedVelocity = doubleValue15 * doubleValue4 + (this.copySign - doubleValue15) * doubleValue12 * doubleValue14;
                    this.copySign = this.copySign + (doubleValue15 - this.copySign) * doubleValue14;
                    if (flag) {
                        accumulatedVelocity = Math.copySign(this.calculateHypot(Math.abs(accumulatedVelocity), magnitude * doubleValue9), doubleValue);
                    }
                    accumulatedVelocity = Math.copySign(Math.min(Math.abs(accumulatedVelocity), doubleValue2), doubleValue);
                    this.accumulatedVelocity += accumulatedVelocity;
                    double roundedDown = Math.floor(Math.abs(this.accumulatedVelocity) / previousPlayerY + 1.0E-9);
                    double roundedDownValue = Math.floor(doubleValue2 / previousPlayerY);
                    double doubleValue16 = doubleValue6 > 0.0 ? Math.min(this.maximumSpeed, 1.0 + doubleValue6 * 0.5) : this.maximumSpeed;
                    if (flag && Double.isFinite(doubleValue16)) {
                        double roundedDownResult = Math.floor(magnitude * doubleValue16 / previousPlayerY + 1.0E-9);
                        roundedDownValue = Math.min(roundedDownValue, roundedDownResult);
                    }
                    double doubleValue17 = Math.copySign(Math.min(roundedDown, roundedDownValue) * previousPlayerY, doubleValue);
                    this.accumulatedVelocity = Math.copySign(Math.max(0.0, Math.abs(this.accumulatedVelocity) - roundedDown * previousPlayerY), doubleValue);
                    return doubleValue17;
                }
            }
        } else {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((KalmanFilter) accessReceiver).reset();
            });
            return 0.0;
        }
    }

    private double calculateHypot(double doubleValue, double doubleValueValue) {
        return doubleValueValue > 0.0 ? doubleValue * (doubleValueValue / Math.hypot(doubleValue, doubleValueValue)) : 0.0;
    }

    public void pause(double doubleValue) {
        if (Double.isFinite(doubleValue) && !(doubleValue <= 0.0)) {
            this.verticalMotion = Math.min(0.06, this.verticalMotion + doubleValue);
            if (this.verticalMotion >= 0.06) {
                this.copySign = 0.0;
                this.accumulatedVelocity = 0.0;
                this.smoothedSpeed = 0.0;
            }
        } else {
            this.reset();
        }
    }

    public void reset() {
        this.copySign = 0.0;
        this.accumulatedVelocity = 0.0;
        this.verticalMotion = 0.0;
        this.previousPlayerY = 0.0;
        this.smoothedSpeed = 0.0;
    }
}
