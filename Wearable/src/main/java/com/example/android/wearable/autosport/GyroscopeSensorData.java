package com.example.android.wearable.autosport;

public class GyroscopeSensorData extends SensorData {
    private float[] gyroscope;

    public GyroscopeSensorData(long timestamp, float[] gyroscope, int accuracy) {
        super(timestamp, accuracy);
        this.gyroscope = gyroscope.clone();
    }

    public float[] getGyroscope() {
        return gyroscope;
    }
}
