package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;

public class RotationEvent extends EventBase {

    private double yaw;

    private double pitch;

    private final boolean physicalMouseMoved;

    public RotationEvent(double doubleValue, double doubleValueValue) {
        this(doubleValue, doubleValueValue, false);
    }

    public RotationEvent(double yaw, double pitch, boolean physicalMouseMoved) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.physicalMouseMoved = physicalMouseMoved;
    }

    public double getYaw() {
        return this.yaw;
    }

    public double getPitch() {
        return this.pitch;
    }

    public boolean isPhysicalMouseMoved() {
        return this.physicalMouseMoved;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }
}
