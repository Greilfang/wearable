package com.example.android.wearable.autosport;

public class MagneticSensorData extends SensorData {
    private float[] magnetic;

    public MagneticSensorData(long timestamp, float[] magnetic, int accuracy) {
        super(timestamp, accuracy);
        this.magnetic = magnetic;
    }

    public float[] getMagnetic() {
        return magnetic;
    }
}
