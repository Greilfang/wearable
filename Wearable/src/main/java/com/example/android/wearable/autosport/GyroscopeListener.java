package com.example.android.wearable.autosport;

import android.hardware.SensorEvent;
import android.util.Log;

public class GyroscopeListener extends SensorListener {
    private static final String TAG = GyroscopeListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public GyroscopeListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new GyroscopeSensorData(event.timestamp, event.values, event.accuracy));
        Log.i(TAG, "gx : "+event.values[0]+" gy : "+event.values[1]+" gz : "+event.values[2]);
    }
}
