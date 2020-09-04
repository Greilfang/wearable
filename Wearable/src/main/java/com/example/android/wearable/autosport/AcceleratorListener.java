package com.example.android.wearable.autosport;

import android.hardware.SensorEvent;

public class AcceleratorListener extends SensorListener {
    private static final String TAG = AcceleratorListener.class.getSimpleName();
    private static final float ACCELERATION_THRESHOLD = 0.25f;
    private ISensorConsumer consumer;
    //private float lastAcceleration;

    public AcceleratorListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new AcceleratorSensorData(event.timestamp, event.values[0], event.accuracy));
    }
}
