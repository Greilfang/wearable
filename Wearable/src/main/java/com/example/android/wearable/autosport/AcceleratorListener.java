package com.example.android.wearable.autosport;

import android.hardware.SensorEvent;
import android.util.Log;

public class AcceleratorListener extends SensorListener {
    private static final String TAG = AcceleratorListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public AcceleratorListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new AcceleratorSensorData(event.timestamp, event.values, event.accuracy));
        Log.i(TAG, "ax : "+event.values[0]+" ay : "+event.values[1]+" az : "+event.values[2]);
    }
}
