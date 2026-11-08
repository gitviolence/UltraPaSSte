package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.util.math.MathHelper;

public class MovementInputEvent extends EventBase {

    private float movementForward;

    private float movementSideways;

    public void setYaw(float floatValue, float floatValueValue) {
        float movementForward = this.getMovementForward();
        float movementSideways = this.getMovementSideways();
        double doubleValue = MathHelper.wrapDegrees(Math.toDegrees(this.calculateToRadians(floatValueValue, movementForward, movementSideways)));
        if (movementForward != 0.0F || movementSideways != 0.0F) {
            float floatValueResult = 0.0F;
            float floatValueItem = 0.0F;
            float MAX_VALUE = Float.MAX_VALUE;
            for (float index = -1.0F; index <= 1.0F; index++) {
                for (float indexValue = -1.0F; indexValue <= 1.0F; indexValue++) {
                    if (indexValue != 0.0F || index != 0.0F) {
                        double doubleValueValue = MathHelper.wrapDegrees(Math.toDegrees(this.calculateToRadians(floatValue, index, indexValue)));
                        double magnitude = Math.abs(doubleValue - doubleValueValue);
                        if (magnitude < MAX_VALUE) {
                            MAX_VALUE = (float) magnitude;
                            floatValueResult = index;
                            floatValueItem = indexValue;
                        }
                    }
                }
            }
            this.setMovementForward(floatValueResult);
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { floatValueItem }, (accessReceiver, accessArguments) -> {
                ((MovementInputEvent) accessReceiver).setMovementSideways(((float) accessArguments[0]));
            });
        }
    }

    private double calculateToRadians(float floatValue, double doubleValue, double doubleValueValue) {
        if (doubleValue < 0.0) {
            floatValue += 180.0F;
        }
        float floatValueValue = 1.0F;
        if (doubleValue < 0.0) {
            floatValueValue = -0.5F;
        } else if (doubleValue > 0.0) {
            floatValueValue = 0.5F;
        }
        if (doubleValueValue > 0.0) {
            floatValue -= 90.0F * floatValueValue;
        }
        if (doubleValueValue < 0.0) {
            floatValue += 90.0F * floatValueValue;
        }
        return Math.toRadians(floatValue);
    }

    public MovementInputEvent(float movementForward, float movementSideways) {
        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
    }

    public float getMovementForward() {
        return this.movementForward;
    }

    public float getMovementSideways() {
        return this.movementSideways;
    }

    public void setMovementForward(float movementForward) {
        this.movementForward = movementForward;
    }

    public void setMovementSideways(float movementSideways) {
        this.movementSideways = movementSideways;
    }
}
