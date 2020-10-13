
package com.example.android.wearable.autosport;

import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class AsyncSaver extends AsyncTask<ISensorReadout, Float, Integer> {
    private static final String TAG = AsyncSaver.class.getSimpleName();
    private static final int NSAMPLES = 100; // by default 20s
    private static final int SPORT_LABEL = 100;
    private final Consumer<Integer> finishedCallback;
    private final File targetDirectory;
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "id,ax,ay,az,gx,gy,gz,mx,my,mz";

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

            Log.i(TAG, "No. of events "
                    + heartRateSensorData.size() + " heart, "
                    + geoLocationData.size() + " geo, "
                    + acceleratorSensorData.size() + " acceleration."
                    + gyroscopeSensorData.size() + " geroscope."
                    + magneticSensorData.size() + " magnetic.");
            int len = acceleratorSensorData.size();
            len = Math.max(len, gyroscopeSensorData.size());
            len = Math.max(len, magneticSensorData.size());
            FileWriter fw = null;

            try {
                /*old file creator
                FileOutputStream fos = new FileOutputStream(new File(targetDirectory, System.currentTimeMillis() + ".csv"));
                fos.write(FileItem.HEADER); // header
                fos.write(BitUtility.getBytes(FileItem.VERSION)); // version

                FileItem.writeField(fos, BitUtility.getBytes((short)0x1001), BitUtility.getBytes(sensorReadout.getSportActivityStartTimeRtc()));
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1002), BitUtility.getBytes(sensorReadout.getSportActivityStopTimeRtc()));
                long startTimestampNs = sensorReadout.getSportActivityStartTimeNs();
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1003), BitUtility.getBytes(startTimestampNs));
                long stopTimestampNs = sensorReadout.getSportActivityStopTimeNs();
                FileItem.writeField(fos, BitUtility.getBytes((short)0x1004), BitUtility.getBytes(stopTimestampNs));
                old file creator*/

                fw = new FileWriter(new File(targetDirectory, System.currentTimeMillis() + ".csv"));
                fw.append(String.valueOf(SPORT_LABEL));
                fw.append(NEW_LINE_SEPARATOR);
                fw.append(FILE_HEADER);
                fw.append(NEW_LINE_SEPARATOR);
                // store individual events
                for (int i = 0; i < len; i++) {
                    if (i < acceleratorSensorData.size()){
                        fw.append(String.valueOf(acceleratorSensorData.get(i).getAcceleration()[0]));
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(acceleratorSensorData.get(i).getAcceleration()[1]));
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(acceleratorSensorData.get(i).getAcceleration()[2]));
                    }
                    if (i < gyroscopeSensorData.size()){
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(gyroscopeSensorData.get(i).getGyroscope()[0]));
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(gyroscopeSensorData.get(i).getGyroscope()[1]));
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(gyroscopeSensorData.get(i).getGyroscope()[2]));
                    }
                    if (i < magneticSensorData.size()){
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(magneticSensorData.get(i).getMagnetic()[0]));
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(magneticSensorData.get(i).getMagnetic()[1]));
                        fw.append(COMMA_DELIMITER);
                        fw.append(String.valueOf(magneticSensorData.get(i).getMagnetic()[2]));
                    }
                    fw.append(NEW_LINE_SEPARATOR);
                }
                /*
                for (AcceleratorSensorData data : acceleratorSensorData) {
                    // 2 = data, 0 = n/a, 3 = acceleration, 1 = first version
                    fw.append(String.valueOf(data.getAcceleration()[0]));
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getAcceleration()[1]));
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getAcceleration()[2]));
                    //FileItem.writeField(fos, BitUtility.getBytes((short)0x2031), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getAcceleration()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (GyroscopeSensorData data : gyroscopeSensorData) {
                    // 2 = data, 0 = n/a, 5 = gyroscope, 1 = first version
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getGyroscope()[0]));
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getGyroscope()[1]));
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getGyroscope()[2]));
                    //FileItem.writeField(fos, BitUtility.getBytes((short)0x2051), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getGyroscope()), BitUtility.getBytes(data.getAccuracy()));
                }

                for (MagneticSensorData data : magneticSensorData) {
                    // 2 = data, 0 = n/a, 7 = magnetic, 1 = first version
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getMagnetic()[0]));
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getMagnetic()[1]));
                    fw.append(COMMA_DELIMITER);
                    fw.append(String.valueOf(data.getMagnetic()[2]));
                    //FileItem.writeField(fos, BitUtility.getBytes((short)0x2071), BitUtility.getBytes(data.getTimestamp()), BitUtility.getBytes(data.getMagnetic()), BitUtility.getBytes(data.getAccuracy()));
                }
                */
                // classify activity if collected sufficient data
                if (acceleratorSensorData.size()>NSAMPLES)
                    // TODO: classifier
                    // results = classifier.predictProbabilities(toFloatArray(data));
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
                //FileItem.writeField(fos, BitUtility.getBytes((short)0xffff));
                //fos.close();

                succeeded++;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to write data file " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            } finally {

                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    System.out.println("Error while flushing/closing fileWriter !!!");
                    e.printStackTrace();
                }

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
