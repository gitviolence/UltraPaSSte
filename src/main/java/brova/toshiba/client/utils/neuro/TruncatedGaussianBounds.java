package brova.toshiba.client.utils.neuro;

import java.util.random.RandomGenerator;

public record TruncatedGaussianBounds(double minimum, double maximum, double secondMinimum, double secondMaximum) {

    private static final double exp = 0.5 * Math.log(Math.PI * 2);

    private static final double cachedCoordinate = Math.log(0.5);

    private static final double[] quadratureNodes = new double[] { 0.09501250983763744, 0.2816035507792589, 0.45801677765722737, 0.6178762444026438, 0.755404408355003, 0.8656312023878318, 0.9445750230732326, 0.9894009349916499 };

    private static final double[] quadratureWeights = new double[] { 0.1894506104550685, 0.1826034150449236, 0.16915651939500254, 0.14959598881657674, 0.12462897125553388, 0.09515851168249279, 0.06225352393864789, 0.027152459411754096 };

    public static TruncatedGaussianBounds physical(double doubleValue, double doubleValueValue) {
        return new TruncatedGaussianBounds(doubleValue, doubleValueValue, 1.0, 0.0);
    }

    public TruncatedGaussianBounds intersect(double doubleValue, double doubleValueValue, boolean flag) {
        if (flag && doubleValueValue - doubleValue >= 360.0) {
            return this;
        } else {
            double doubleValueResult = 1.0;
            double doubleValueItem = 0.0;
            double doubleValueEntry = 1.0;
            double doubleValueCopy = 0.0;
            for (int index = flag ? -1 : 0; index <= (flag ? 1 : 0); index++) {
                double doubleValue2 = Math.max(this.minimum, doubleValue + index * 360.0);
                double doubleValue3 = Math.min(this.maximum, doubleValueValue + index * 360.0);
                if (!(doubleValue2 >= doubleValue3)) {
                    if (doubleValueResult > doubleValueItem) {
                        doubleValueResult = doubleValue2;
                        doubleValueItem = doubleValue3;
                    } else {
                        doubleValueEntry = doubleValue2;
                        doubleValueCopy = doubleValue3;
                    }
                }
            }
            return doubleValueResult > doubleValueItem ? this : new TruncatedGaussianBounds(doubleValueResult, doubleValueItem, doubleValueEntry, doubleValueCopy);
        }
    }

    public boolean contains(double doubleValue) {
        return doubleValue >= this.minimum && doubleValue <= this.maximum || doubleValue >= this.secondMinimum && doubleValue <= this.secondMaximum;
    }

    public double logProbability(double doubleValue, double doubleValueValue) {
        double doubleValueResult = calculateLogWithExp((this.minimum - doubleValue) / doubleValueValue, (this.maximum - doubleValue) / doubleValueValue);
        if (this.secondMinimum > this.secondMaximum) {
            return doubleValueResult;
        } else {
            double doubleValueItem = calculateLogWithExp((this.secondMinimum - doubleValue) / doubleValueValue, (this.secondMaximum - doubleValue) / doubleValueValue);
            double doubleValueEntry = Math.max(doubleValueResult, doubleValueItem);
            return doubleValueEntry == Double.NEGATIVE_INFINITY ? doubleValueEntry : doubleValueEntry + Math.log1p(Math.exp(Math.min(doubleValueResult, doubleValueItem) - doubleValueEntry));
        }
    }

    public double sample(double doubleValue, double doubleValueValue, RandomGenerator randomGenerator) {
        double minimum = this.minimum;
        double maximum = this.maximum;
        if (this.secondMinimum <= this.secondMaximum) {
            double doubleValueResult = calculateLogWithExp((this.minimum - doubleValue) / doubleValueValue, (this.maximum - doubleValue) / doubleValueValue);
            double doubleValueItem = calculateLogWithExp((this.secondMinimum - doubleValue) / doubleValueValue, (this.secondMaximum - doubleValue) / doubleValueValue);
            double doubleValueEntry = Math.max(doubleValueResult, doubleValueItem);
            if (doubleValueEntry == Double.NEGATIVE_INFINITY) {
                return this.closest(doubleValue);
            }
            double doubleValueCopy = Math.exp(doubleValueResult - doubleValueEntry);
            double doubleValue2 = Math.exp(doubleValueItem - doubleValueEntry);
            if (randomGenerator.nextDouble() * (doubleValueCopy + doubleValue2) >= doubleValueCopy) {
                minimum = this.secondMinimum;
                maximum = this.secondMaximum;
            }
        }
        double doubleValue3 = doubleValue + doubleValueValue * calculateNextDoubleWithExp((minimum - doubleValue) / doubleValueValue, (maximum - doubleValue) / doubleValueValue, randomGenerator);
        return Math.max(minimum, Math.min(maximum, doubleValue3));
    }

    public double closest(double doubleValue) {
        double doubleValueValue = Math.max(this.minimum, Math.min(this.maximum, doubleValue));
        if (this.secondMinimum > this.secondMaximum) {
            return doubleValueValue;
        } else {
            double doubleValueResult = Math.max(this.secondMinimum, Math.min(this.secondMaximum, doubleValue));
            return Math.abs(doubleValue - doubleValueValue) <= Math.abs(doubleValue - doubleValueResult) ? doubleValueValue : doubleValueResult;
        }
    }

    private static double calculateLogWithExp(double doubleValue, double doubleValueValue) {
        if (doubleValue >= doubleValueValue) {
            return Double.NEGATIVE_INFINITY;
        } else {
            double doubleValueResult = doubleValueValue - doubleValue;
            if (doubleValueResult < 1.0E-5 / (1.0 + Math.min(Math.abs(doubleValue), Math.abs(doubleValueValue)))) {
                double doubleValueItem = doubleValue + doubleValueResult * 0.5;
                return Math.log(doubleValueResult) - exp - 0.5 * doubleValueItem * doubleValueItem;
            } else if (doubleValueValue <= 0.0) {
                return calculateLogWithExp(-doubleValueValue, -doubleValue);
            } else if (doubleValue >= 0.0) {
                double doubleValueEntry = calculateExpWithLog(doubleValue);
                return doubleValueEntry + Math.log(-Math.expm1(calculateExpWithLog(doubleValueValue) - doubleValueEntry));
            } else {
                return Math.log1p(-Math.exp(calculateExpWithLog(-doubleValue)) - Math.exp(calculateExpWithLog(doubleValueValue)));
            }
        }
    }

    private static double calculateExpWithLog(double doubleValue) {
        if (doubleValue == Double.POSITIVE_INFINITY) {
            return Double.NEGATIVE_INFINITY;
        } else if (doubleValue == 0.0) {
            return cachedCoordinate;
        } else if (doubleValue >= 3.0) {
            double doubleValueValue = 0.0;
            for (int index = 64; index > 0; index--) {
                doubleValueValue = index / (doubleValue + doubleValueValue);
            }
            return -0.5 * doubleValue * doubleValue - exp - Math.log(doubleValue + doubleValueValue);
        } else {
            double doubleValueResult = doubleValue * 0.5;
            double doubleValueItem = 0.0;
            for (int indexValue = 0; indexValue < quadratureNodes.length; indexValue++) {
                double doubleValueEntry = doubleValueResult * (1.0 - quadratureNodes[indexValue]);
                double doubleValueCopy = doubleValueResult * (1.0 + quadratureNodes[indexValue]);
                doubleValueItem += quadratureWeights[indexValue] * (Math.exp(-0.5 * doubleValueEntry * doubleValueEntry) + Math.exp(-0.5 * doubleValueCopy * doubleValueCopy));
            }
            doubleValueItem *= doubleValueResult * Math.exp(-exp);
            return cachedCoordinate + Math.log1p(-2.0 * doubleValueItem);
        }
    }

    private static double calculateNextDoubleWithExp(double doubleValue, double doubleValueValue, RandomGenerator randomGenerator) {
        if (doubleValue >= doubleValueValue) {
            return doubleValue;
        } else if (doubleValueValue < 0.0) {
            return -calculateNextDoubleWithExp(-doubleValueValue, -doubleValue, randomGenerator);
        } else if (doubleValue > 0.0) {
            double doubleValueResult = 0.5 * (doubleValue + Math.hypot(doubleValue, 2.0));
            double doubleValueItem = -Math.expm1(-doubleValueResult * (doubleValueValue - doubleValue));
            double doubleValueEntry;
            do {
                doubleValueEntry = doubleValue - Math.log1p(-randomGenerator.nextDouble() * doubleValueItem) / doubleValueResult;
            } while (randomGenerator.nextDouble() > Math.exp(-0.5 * (doubleValueEntry - doubleValueResult) * (doubleValueEntry - doubleValueResult)));
            return doubleValueEntry;
        } else if (doubleValueValue - doubleValue < 2.0) {
            double doubleValueCopy;
            do {
                doubleValueCopy = doubleValue + randomGenerator.nextDouble() * (doubleValueValue - doubleValue);
            } while (randomGenerator.nextDouble() > Math.exp(-0.5 * doubleValueCopy * doubleValueCopy));
            return doubleValueCopy;
        } else {
            double doubleValue2;
            do {
                doubleValue2 = randomGenerator.nextGaussian();
            } while (doubleValue2 < doubleValue || doubleValue2 > doubleValueValue);
            return doubleValue2;
        }
    }
}
