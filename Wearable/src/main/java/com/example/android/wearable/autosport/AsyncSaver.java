
package com.example.android.wearable.autosport;

import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Consumer;

public class AsyncSaver extends AsyncTask<ISensorReadout, Float, Integer> {
    private static final String TAG = AsyncSaver.class.getSimpleName();
    private static final int NSAMPLES = 100; // by default 20s
    private final Consumer<Integer> finishedCallback;
    private final File targetDirectory;

    public AsyncSaver(Consumer<Integer> finishedCallback, File targetDirectory) {
        this.finishedCallback = finishedCallback;
        this.targetDirectory = targetDirectory;
    }

    @Override
    protected Integer doInBackground(ISensorReadout... iSensorReadouts) {
        if (iSensorReadouts == null || iSensorReadouts.length == 0) {
            return 0;
        }

        int succeeded = 0;

        for (ISensorReadout sensorReadout : iSensorReadouts) {
            List<? extends HeartRateSensorData> heartRateSensorData = sensorReadout.getHeartRateData();
            List<? extends GeoLocationData> geoLocationData = sensorReadout.getGeoLocationData();
            List<? extends AcceleratorSensorData> acceleratorSensorData = sensorReadout.getAcceleratorData();
            List<? extends GyroscopeSensorData> gyroscopeSensorData = sensorReadout.getGyroscopeData();
            List<? extends MagneticSensorData> magneticSensorData = sensorReadout.getMagneticData();
            // TODO: gyro and mag
            Log.i(TAG, "No. of events "
                    + heartRateSensorData.size() + " heart, "
                    + geoLocationData.size() + " geo, "
                    + acceleratorSensorData.size() + " acceleration.");

            try {
                FileOutputStream fos = new FileOutputStream(new File(targetDirectory, System.currentTimeMillis() + ".trk"));
                fos.write(FileItem.HEADER); // header
                fos.write(BitUtility.getBytes(FileItem.VERSION)); // version

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1001), BitUtility.getBytes(sensorReadout.getSportActivityStartTimeRtc()));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1002), BitUtility.getBytes(sensorReadout.getSportActivityStopTimeRtc()));
                long startTimestampNs = sensorReadout.getSportActivityStartTimeNs();
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1003), BitUtility.getBytes(startTimestampNs));
                long stopTimestampNs = sensorReadout.getSportActivityStopTimeNs();
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1004), BitUtility.getBytes(stopTimestampNs));

                // create a few statistics for faster lookup
                // average and maximum heart rate
                /*
                float avgHeartRate = 0;
                int maxHeartRate = 0;
                int countHeartRate = 0;
                for (HeartRateSensorData data : heartRateSensorData) {
                    if (data.getTimestamp() >= startTimestampNs && data.getTimestamp() <= stopTimestampNs && data.getAccuracy() >= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                        avgHeartRate = avgHeartRate * ((float)countHeartRate / (++countHeartRate)) + (float)data.getHeartRate() / countHeartRate;
                        if (data.getHeartRate() > maxHeartRate) {
                            maxHeartRate = data.getHeartRate();
                        }
                    }
                }

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1011), BitUtility.getBytes(avgHeartRate));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1012), BitUtility.getBytes(maxHeartRate));


                // total ascent und descent, average speed as of the GNSS
                boolean firstAltitude = true;
                double lastAltitude = 0;
                double totalAscent = 0;
                double totalDescent = 0;
                float avgSpeed = 0;
                int countSpeed = 0;
                for (GeoLocationData data : geoLocationData) {
                    if (data.getTimestamp() >= startTimestampNs && data.getTimestamp() <= stopTimestampNs) {
                        Location location = data.getLocation();

                        if (firstAltitude) {
                            firstAltitude = false;
                        } else {
                            double diff = location.getAltitude() - lastAltitude;
                            if (diff > 0) {
                                totalAscent += diff;
                            } else {
                                totalDescent -= diff;
                            }
                        }

                        lastAltitude = location.getAltitude();

                        avgSpeed = avgSpeed * ((float)countSpeed / (++countSpeed)) + location.getSpeed() / countSpeed;
                    }
                }

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1015), BitUtility.getBytes((float)totalAscent));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1016), BitUtility.getBytes((float)totalDescent));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1017), BitUtility.getBytes(avgSpeed));
                */
                // store individual events
                for (AcceleratorSensorData data : acceleratorSensorData) {
                    // 2 = data, 0 = n/a, 3 = acceleration, 1 = first version
                    FileItem.writeField(fos, BitUtility.getBytes((short)0x2031), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getAcceleration()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (GyroscopeSensorData data : gyroscopeSensorData) {
                    // 2 = data, 0 = n/a, 5 = gyroscope, 1 = first version
                    FileItem.writeField(fos, BitUtility.getBytes((short)0x2051), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getGyroscope()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (MagneticSensorData data : magneticSensorData) {
                    // 2 = data, 0 = n/a, 7 = magnetic, 1 = first version
                    FileItem.writeField(fos, BitUtility.getBytes((short)0x2071), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getMagnetic()), BitUtility.getBytes(data.getAccuracy()));
                }

                // classify activity if collected sufficient data
                if (acceleratorSensorData.size()>NSAMPLES)
                    // TODO: classifier
                    results = classifier.predictProbabilities(toFloatArray(data));
                /*for (GeoLocationData data : geoLocationData) {
                    Location location = data.getLocation();

                    // 2 = data, 0 = n/a, 4 = geo, 1 = first version
                    FileItem.writeField(fos,
                            BitUtility.getBytes((short)0x2041),
                            BitUtility.getBytes(data.getTimestamp()),
                            BitUtility.getBytes(location.getElapsedRealtimeNanos()),
                            BitUtility.getBytes(location.getTime()),
                            BitUtility.getBytes(location.getLatitude()),
                            BitUtility.getBytes(location.getLongitude()),
                            BitUtility.getBytes(location.getAccuracy()),
                            BitUtility.getBytes(location.getAltitude()),
                            BitUtility.getBytes(location.getBearing()),
                            BitUtility.getBytes(location.getSpeed()),
                            BitUtility.getBytes(data.getAccuracy()));
                }*/

                // end of file marker
                FileItem.writeField(fos, BitUtility.getBytes((short)0xffff));
                fos.close();

                succeeded++;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to write data file " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            }
        }

        return succeeded;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (this.finishedCallback != null) {
            this.finishedCallback.accept(integer);
        }
    }
}
