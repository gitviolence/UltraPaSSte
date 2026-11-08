package brova.toshiba.client.utils.rotate;

import brova.toshiba.client.utils.neuro.PitchStepBounds;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public record AimProjection(float yawNegativeMargin, float yawPositiveMargin, float pitchNegativeMargin, float pitchPositiveMargin, Box box, Vec3d eye, float referenceYaw, float referencePitch) {

    public static AimProjection from(Box box, Vec3d position, Vec3d positionValue) {
        if (!box.contains(positionValue)) {
            return null;
        } else {
            double doubleValue = positionValue.x - position.x;
            double doubleValueValue = positionValue.z - position.z;
            double doubleValueResult = Math.toDegrees(Math.atan2(doubleValueValue, doubleValue));
            double doubleValueItem = -Math.toDegrees(Math.atan2(positionValue.y - position.y, Math.hypot(doubleValue, doubleValueValue)));
            double doubleValueEntry = 0.0;
            double doubleValueCopy = 0.0;
            if (position.x >= box.minX && position.x <= box.maxX && position.z >= box.minZ && position.z <= box.maxZ) {
                doubleValueEntry = -180.0;
                doubleValueCopy = 180.0;
            } else {
                for (double doubleValue2 : new double[] { box.minX, box.maxX }) {
                    for (double doubleValue3 : new double[] { box.minZ, box.maxZ }) {
                        double doubleValue4 = Math.toDegrees(Math.atan2(doubleValue3 - position.z, doubleValue2 - position.x)) - doubleValueResult;
                        doubleValue4 = (doubleValue4 + 540.0) % 360.0 - 180.0;
                        doubleValueEntry = Math.min(doubleValueEntry, doubleValue4);
                        doubleValueCopy = Math.max(doubleValueCopy, doubleValue4);
                    }
                }
            }
            double doubleValue5 = Math.max(0.0, Math.max(box.minX - position.x, position.x - box.maxX));
            double doubleValue6 = Math.max(0.0, Math.max(box.minZ - position.z, position.z - box.maxZ));
            double doubleValue7 = Math.max(Math.abs(box.minX - position.x), Math.abs(box.maxX - position.x));
            double doubleValue8 = Math.max(Math.abs(box.minZ - position.z), Math.abs(box.maxZ - position.z));
            PitchStepBounds pitchStepBounds = PitchStepBounds.fromProjection(Math.hypot(doubleValue5, doubleValue6), Math.hypot(doubleValue7, doubleValue8), box.minY - position.y, box.maxY - position.y, (float) doubleValueItem);
            return new AimProjection((float) (-doubleValueEntry), (float) doubleValueCopy, Math.max(0.0F, -pitchStepBounds.minimumStep()), Math.max(0.0F, pitchStepBounds.maximumStep()), box, position, (float) (doubleValueResult - 90.0), (float) doubleValueItem);
        }
    }

    public boolean intersects(double doubleValue, double doubleValueValue) {
        float floatValue = (float) (this.referenceYaw - doubleValue);
        float floatValueValue = (float) Math.max(-90.0, Math.min(90.0, this.referencePitch - doubleValueValue));
        Vec3d position = Vec3d.fromPolar(floatValueValue, floatValue);
        return this.box.contains(this.eye) || this.box.raycast(this.eye, this.eye.add(position.multiply(8.0))).isPresent();
    }

    public PitchStepBounds pitchSteps(double doubleValue, double doubleValueValue) {
        Vec3d position = Vec3d.fromPolar(0.0F, (float) (this.referenceYaw - doubleValue));
        double doubleValueResult = 0.0;
        double POSITIVE_INFINITY = Double.POSITIVE_INFINITY;
        if (Math.abs(position.x) < 1.0E-12) {
            if (this.eye.x < this.box.minX || this.eye.x > this.box.maxX) {
                return null;
            }
        } else {
            double doubleValueItem = (this.box.minX - this.eye.x) / position.x;
            double doubleValueEntry = (this.box.maxX - this.eye.x) / position.x;
            doubleValueResult = Math.max(doubleValueResult, Math.min(doubleValueItem, doubleValueEntry));
            POSITIVE_INFINITY = Math.min(POSITIVE_INFINITY, Math.max(doubleValueItem, doubleValueEntry));
        }
        if (Math.abs(position.z) < 1.0E-12) {
            if (this.eye.z < this.box.minZ || this.eye.z > this.box.maxZ) {
                return null;
            }
        } else {
            double doubleValueCopy = (this.box.minZ - this.eye.z) / position.z;
            double doubleValue2 = (this.box.maxZ - this.eye.z) / position.z;
            doubleValueResult = Math.max(doubleValueResult, Math.min(doubleValueCopy, doubleValue2));
            POSITIVE_INFINITY = Math.min(POSITIVE_INFINITY, Math.max(doubleValueCopy, doubleValue2));
        }
        return doubleValueResult >= POSITIVE_INFINITY ? null : PitchStepBounds.fromProjection(doubleValueResult, POSITIVE_INFINITY, this.box.minY - this.eye.y, this.box.maxY - this.eye.y, (float) (this.referencePitch - doubleValueValue));
    }

    public float yawExtent(float floatValue) {
        return floatValue >= 0.0F ? this.yawNegativeMargin : this.yawPositiveMargin;
    }

    public float pitchExtent(float floatValue) {
        return floatValue >= 0.0F ? this.pitchNegativeMargin : this.pitchPositiveMargin;
    }

    public double yawMiss(double doubleValue) {
        return Math.max(0.0, Math.max(doubleValue - this.yawNegativeMargin, -doubleValue - this.yawPositiveMargin));
    }

    public double pitchMiss(double doubleValue) {
        return Math.max(0.0, Math.max(doubleValue - this.pitchNegativeMargin, -doubleValue - this.pitchPositiveMargin));
    }
}
