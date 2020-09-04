package com.example.android.wearable.autosport;

import android.hardware.SensorEvent;

public class GyroscopeListener extends SensorListener {
    private static final String TAG = GyroscopeListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public GyroscopeListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new GyroscopeSensorData(event.timestamp, event.values[0], event.accuracy));
    }
}
