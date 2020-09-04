package com.example.android.wearable.autosport;

import android.hardware.SensorEvent;

public class MagneticListener extends SensorListener {
    private static final String TAG = MagneticListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public MagneticListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new MagneticSensorData(event.timestamp, event.values[0], event.accuracy));
    }
}
