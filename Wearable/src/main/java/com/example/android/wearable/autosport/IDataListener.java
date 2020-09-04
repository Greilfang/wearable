package com.example.android.wearable.autosport;

public interface IDataListener<T extends SensorData> {
    void onDataReceived(T data);
}
