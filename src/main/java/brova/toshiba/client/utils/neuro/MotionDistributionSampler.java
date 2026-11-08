package brova.toshiba.client.utils.neuro;

import brova.toshiba.client.utils.rotate.AimProjection;
import java.util.random.RandomGenerator;

public final class MotionDistributionSampler {

    private final GaussianMotionDistribution sampleDirectGaussianMotionDistribution;

    private boolean stateFlag;

    private double cachedCoordinate;

    private MotionDistributionSampler(GaussianMotionDistribution sampleDirectGaussianMotionDistribution) {
        this.sampleDirectGaussianMotionDistribution = sampleDirectGaussianMotionDistribution;
    }

    public static MotionDistributionSampler forModel(NeuroPredictor neuroPredictor) {
        return new MotionDistributionSampler(brova.toshiba.client.utils.runtime.AccessGuard.read(neuroPredictor, accessReceiver -> accessReceiver.sampleDirectGaussianMotionDistribution));
    }

    public MotionSample apply(MotionSample motionSample, float[] values, float[] valuesValue, float floatValue, PitchStepBounds pitchStepBounds, AimProjection aimProjection, RandomGenerator randomGenerator) {
        double cachedCoordinate = calculateHypotWithIntersects(valuesValue[0], valuesValue[1], valuesValue, aimProjection);
        MotionSample motionSampleValue = motionSample.stopped() ? this.createSampleDirectWithStopped(motionSample, values, valuesValue, floatValue, pitchStepBounds, aimProjection, cachedCoordinate, randomGenerator) : this.createYawVelocityWithSampleRestingYaw(motionSample, values, valuesValue, floatValue, pitchStepBounds, aimProjection, randomGenerator);
        this.stateFlag = motionSampleValue.stopped();
        this.cachedCoordinate = cachedCoordinate;
        return motionSampleValue;
    }

    private MotionSample createSampleDirectWithStopped(MotionSample motionSample, float[] values, float[] valuesValue, float floatValue, PitchStepBounds pitchStepBounds, AimProjection aimProjection, double doubleValue, RandomGenerator randomGenerator) {
        if (this.stateFlag && !(valuesValue[20] >= 0.5F) && !(this.cachedCoordinate <= floatValue) && !(doubleValue <= this.cachedCoordinate + floatValue) && !(Math.abs(valuesValue[3]) > 1.0E-5F) && !(Math.abs(valuesValue[4]) > 1.0E-5F)) {
            byte byteValue = 82;
            double doubleValueValue = (double) valuesValue[byteValue] * valuesValue[21];
            double doubleValueResult = (double) valuesValue[byteValue + 1] * valuesValue[21];
            if (!(Math.abs(doubleValueValue) > Math.max(floatValue, valuesValue[29])) && !(Math.abs(doubleValueResult) > Math.max(floatValue, valuesValue[30]))) {
                double doubleValueItem = calculateDouble(valuesValue[0] + doubleValueValue);
                double doubleValueEntry = valuesValue[1] + doubleValueResult;
                double doubleValueCopy = calculateHypotWithIntersects(doubleValueItem, doubleValueEntry, valuesValue, aimProjection);
                if (doubleValueCopy + floatValue < doubleValue) {
                    return motionSample;
                } else {
                    double doubleValue2 = calculateYawMiss(doubleValueItem, valuesValue, aimProjection) + floatValue;
                    for (int index = 0; index < 4; index++) {
                        MotionSample motionSampleValue = this.sampleDirectGaussianMotionDistribution.sampleDirect(values, randomGenerator);
                        if (!motionSampleValue.stopped() && !isMotionSample(motionSampleValue, valuesValue, floatValue, aimProjection)) {
                            double doubleValue3 = calculateNextDownWithYawVelocity(motionSampleValue, valuesValue[21]);
                            double doubleValue4 = calculateMinimumStepWithMaximumStep(motionSampleValue, valuesValue[21], pitchStepBounds);
                            double doubleValue5 = calculateDouble(doubleValueItem - doubleValue3);
                            double doubleValue6 = doubleValueEntry - doubleValue4;
                            if (!(calculateYawMiss(doubleValue5, valuesValue, aimProjection) > doubleValue2) && !(calculateHypotWithIntersects(doubleValue5, doubleValue6, valuesValue, aimProjection) + floatValue >= doubleValueCopy) && !(calculateHypotWithIntersects(calculateDouble(valuesValue[0] - doubleValue3), valuesValue[1] - doubleValue4, valuesValue, aimProjection) > doubleValue + floatValue)) {
                                return motionSampleValue;
                            }
                        }
                    }
                    return motionSample;
                }
            } else {
                return motionSample;
            }
        } else {
            return motionSample;
        }
    }

    private MotionSample createYawVelocityWithSampleRestingYaw(MotionSample motionSample, float[] values, float[] valuesValue, float floatValue, PitchStepBounds pitchStepBounds, AimProjection aimProjection, RandomGenerator randomGenerator) {
        if (valuesValue[20] >= 0.5F && isBoxWithEye(valuesValue, aimProjection) && isIntersects(motionSample, valuesValue, pitchStepBounds, aimProjection)) {
            if (isMotionSample(motionSample, valuesValue)) {
                return motionSample;
            } else {
                for (int index = 0; index < 4; index++) {
                    MotionSample motionSampleValue = this.sampleDirectGaussianMotionDistribution.sampleRestingYaw(values, motionSample, valuesValue[21], randomGenerator);
                    if (!(Math.abs(motionSampleValue.yawVelocity()) >= Math.abs(motionSample.yawVelocity())) && !((double) motionSampleValue.yawVelocity() * motionSample.yawVelocity() < 0.0) && isIntersects(motionSampleValue, valuesValue, pitchStepBounds, aimProjection)) {
                        return motionSampleValue;
                    }
                }
                return motionSample;
            }
        } else {
            TruncatedGaussianBounds truncatedGaussianBounds = createTruncatedGaussianBoundsFromYawNegativeMarginWithYawPositiveMargin(motionSample, valuesValue, floatValue, aimProjection);
            if (truncatedGaussianBounds != null) {
                MotionSample motionSampleResult = this.sampleDirectGaussianMotionDistribution.sampleYaw(values, motionSample, valuesValue[21], truncatedGaussianBounds, randomGenerator);
                if (motionSampleResult != motionSample) {
                    return motionSampleResult;
                }
            }
            boolean motionSampleItem = isMotionSample(motionSample, valuesValue, floatValue, aimProjection);
            double doubleValue = calculateHypotWithIntersects(valuesValue[0], valuesValue[1], valuesValue, aimProjection);
            if (motionSampleItem || !(valuesValue[20] >= 0.5F) && !(doubleValue <= floatValue) && !(calculateMotionSample(motionSample, valuesValue, pitchStepBounds, aimProjection, 0.0) <= doubleValue + floatValue)) {
                double doubleValueValue = motionSampleItem ? (double) valuesValue[82] * valuesValue[21] : 0.0;
                double doubleValueResult = calculateDouble(valuesValue[0] + doubleValueValue);
                double doubleValueItem = calculateDouble(doubleValueResult - calculateNextDownWithYawVelocity(motionSample, valuesValue[21]));
                double doubleValueEntry = Math.min(calculateYawMiss(doubleValueResult, valuesValue, aimProjection), calculateYawMiss(doubleValueItem, valuesValue, aimProjection)) + floatValue;
                double doubleValueCopy = motionSampleItem ? Math.max(calculateHypotWithIntersects(doubleValueResult, valuesValue[1], valuesValue, aimProjection), calculateMotionSample(motionSample, valuesValue, pitchStepBounds, aimProjection, doubleValueValue)) : doubleValue;
                for (int indexValue = 0; indexValue < 4; indexValue++) {
                    MotionSample motionSampleEntry = this.sampleDirectGaussianMotionDistribution.sampleDirect(values, randomGenerator);
                    if (!motionSampleEntry.stopped() && !isMotionSample(motionSampleEntry, valuesValue, floatValue, aimProjection)) {
                        double doubleValue2 = calculateDouble(doubleValueResult - calculateNextDownWithYawVelocity(motionSampleEntry, valuesValue[21]));
                        if (!(calculateYawMiss(doubleValue2, valuesValue, aimProjection) > doubleValueEntry) && !(calculateMotionSample(motionSampleEntry, valuesValue, pitchStepBounds, aimProjection, doubleValueValue) + floatValue >= doubleValueCopy)) {
                            return motionSampleEntry;
                        }
                    }
                }
                return motionSample;
            } else {
                return motionSample;
            }
        }
    }

    private static boolean isMotionSample(MotionSample motionSample, float[] values) {
        double doubleValue = calculateNextDownWithYawVelocity(motionSample, values[21]);
        double doubleValueValue = values[0];
        double doubleValueResult = calculateDouble(doubleValueValue + (double) values[82] * values[21]);
        return Math.abs(calculateDouble(doubleValueValue - doubleValue)) < Math.abs(doubleValueValue) || Math.abs(calculateDouble(doubleValueResult - doubleValue)) < Math.abs(doubleValueResult);
    }

    private static boolean isBoxWithEye(float[] values, AimProjection aimProjection) {
        if (aimProjection != null && aimProjection.box().contains(aimProjection.eye())) {
            double doubleValue = Math.toRadians(aimProjection.referenceYaw() - values[0]);
            double doubleValueValue = values[21] / 0.05F;
            double doubleValueResult = values[25] * doubleValueValue;
            double doubleValueItem = values[26] * doubleValueValue;
            double doubleValueEntry = values[27] * doubleValueValue;
            double doubleValueCopy = Math.sin(doubleValue);
            double doubleValue2 = Math.cos(doubleValue);
            return aimProjection.box().contains(aimProjection.eye().add(doubleValueResult * doubleValueCopy - doubleValueItem * doubleValue2, -doubleValueEntry, -doubleValueResult * doubleValue2 - doubleValueItem * doubleValueCopy));
        } else {
            return false;
        }
    }

    private static TruncatedGaussianBounds createTruncatedGaussianBoundsFromYawNegativeMarginWithYawPositiveMargin(MotionSample motionSample, float[] values, float floatValue, AimProjection aimProjection) {
        if (calculateYawMiss(values[0], values, aimProjection) > floatValue) {
            return null;
        } else {
            double doubleValue = (double) values[82] * values[21];
            if (Math.abs(doubleValue) > Math.max(floatValue, values[29])) {
                return null;
            } else {
                double doubleValueValue = calculateDouble(values[0] + doubleValue);
                double doubleValueResult = calculateDouble(doubleValueValue - calculateNextDownWithYawVelocity(motionSample, values[21]));
                if (calculateYawMiss(doubleValueResult, values, aimProjection) <= floatValue) {
                    return null;
                } else {
                    double doubleValueItem = aimProjection == null ? values[29] : aimProjection.yawNegativeMargin();
                    double doubleValueEntry = aimProjection == null ? values[29] : aimProjection.yawPositiveMargin();
                    double doubleValueCopy = doubleValueValue - doubleValueItem + floatValue;
                    double doubleValue2 = doubleValueValue + doubleValueEntry - floatValue;
                    if (doubleValueCopy >= doubleValue2) {
                        return null;
                    } else {
                        TruncatedGaussianBounds truncatedGaussianBounds = TruncatedGaussianBounds.physical(-180.0, Math.nextDown(180.0F));
                        TruncatedGaussianBounds truncatedGaussianBoundsValue = truncatedGaussianBounds.intersect(doubleValueCopy, doubleValue2, true);
                        return truncatedGaussianBoundsValue == truncatedGaussianBounds ? null : truncatedGaussianBoundsValue;
                    }
                }
            }
        }
    }

    private static double calculateMotionSample(MotionSample motionSample, float[] values, PitchStepBounds pitchStepBounds, AimProjection aimProjection, double doubleValue) {
        return calculateHypotWithIntersects(calculateDouble(values[0] + doubleValue - calculateNextDownWithYawVelocity(motionSample, values[21])), values[1] - calculateMinimumStepWithMaximumStep(motionSample, values[21], pitchStepBounds), values, aimProjection);
    }

    private static boolean isIntersects(MotionSample motionSample, float[] values, PitchStepBounds pitchStepBounds, AimProjection aimProjection) {
        return aimProjection.intersects(calculateDouble(values[0] - calculateNextDownWithYawVelocity(motionSample, values[21])), values[1] - calculateMinimumStepWithMaximumStep(motionSample, values[21], pitchStepBounds));
    }

    private static double calculateMinimumStepWithMaximumStep(MotionSample motionSample, float floatValue, PitchStepBounds pitchStepBounds) {
        double doubleValue = pitchStepBounds == null ? -180.0 : pitchStepBounds.minimumStep();
        double doubleValueValue = pitchStepBounds == null ? 180.0 : pitchStepBounds.maximumStep();
        return Math.max(doubleValue, Math.min(doubleValueValue, (double) motionSample.pitchVelocity() * floatValue));
    }

    private static boolean isMotionSample(MotionSample motionSample, float[] values, float floatValue, AimProjection aimProjection) {
        double doubleValue = (double) values[82] * values[21];
        if (Math.abs(doubleValue) > Math.max(floatValue, values[29])) {
            return false;
        } else {
            double doubleValueValue = calculateDouble(values[0] + doubleValue);
            double doubleValueResult = calculateDouble(doubleValueValue - calculateNextDownWithYawVelocity(motionSample, values[21]));
            return doubleValueValue * doubleValueResult < 0.0 && Math.abs(doubleValueValue - doubleValueResult) < 180.0 && calculateYawMiss(doubleValueResult, values, aimProjection) > floatValue;
        }
    }

    private static double calculateNextDownWithYawVelocity(MotionSample motionSample, float floatValue) {
        return Math.max(-180.0, Math.min((double) Math.nextDown(180.0F), (double) motionSample.yawVelocity() * floatValue));
    }

    private static double calculateYawMiss(double doubleValue, float[] values, AimProjection aimProjection) {
        return aimProjection == null ? Math.max(0.0, Math.abs(doubleValue) - values[29]) : aimProjection.yawMiss(doubleValue);
    }

    private static double calculateHypotWithIntersects(double doubleValue, double doubleValueValue, float[] values, AimProjection aimProjection) {
        if (aimProjection != null) {
            if (aimProjection.intersects(doubleValue, doubleValueValue)) {
                return 0.0;
            } else {
                PitchStepBounds pitchStepBounds = aimProjection.pitchSteps(doubleValue, doubleValueValue);
                double doubleValueResult = pitchStepBounds == null ? aimProjection.pitchMiss(doubleValueValue) : Math.max(0.0, (double) Math.max(pitchStepBounds.minimumStep(), -pitchStepBounds.maximumStep()));
                return Math.hypot(aimProjection.yawMiss(doubleValue), doubleValueResult);
            }
        } else {
            return Math.hypot(Math.max(0.0, Math.abs(doubleValue) - values[29]), Math.max(0.0, Math.abs(doubleValueValue) - values[30]));
        }
    }

    private static double calculateDouble(double doubleValue) {
        double doubleValueValue = doubleValue % 360.0;
        if (doubleValueValue >= 180.0) {
            doubleValueValue -= 360.0;
        }
        if (doubleValueValue < -180.0) {
            doubleValueValue += 360.0;
        }
        return doubleValueValue;
    }

    public void reset() {
        this.stateFlag = false;
        this.cachedCoordinate = 0.0;
    }
}
