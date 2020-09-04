package com.example.android.wearable.autosport;

import android.hardware.SensorEvent;
import android.util.Log;

public class MagneticListener extends SensorListener {
    private static final String TAG = MagneticListener.class.getSimpleName();
    private ISensorConsumer consumer;

    public MagneticListener(ISensorConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        consumer.addData(new MagneticSensorData(event.timestamp, event.values, event.accuracy));
        Log.i(TAG, "mx : "+event.values[0]+" my : "+event.values[1]+" mz : "+event.values[2]);
    }
}
