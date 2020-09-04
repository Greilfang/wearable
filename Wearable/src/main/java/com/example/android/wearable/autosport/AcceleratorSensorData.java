package com.example.android.wearable.autosport;

public class AcceleratorSensorData extends SensorData {
    private float acceleration;

    public AcceleratorSensorData(long timestamp, float acceleration, int accuracy) {
        super(timestamp, accuracy);
        this.acceleration = acceleration;
    }

    public float getAcceleration() {
        return acceleration;
    }
}
