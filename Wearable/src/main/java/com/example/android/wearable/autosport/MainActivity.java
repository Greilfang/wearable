/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.autosport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.wear.ambient.AmbientModeSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * The main activity for the Jumping Jack application. This activity registers itself to receive
 * sensor values.
 *
 * This activity includes a {@link ViewPager} with two pages, one that
 * shows the current count and one that allows user to reset the counter. the current value of the
 * counter is persisted so that upon re-launch, the counter picks up from the last value. At any
 * stage, user can set this counter to 0.
 */
public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider, SensorEventListener{

    private static final String TAG = "MainActivity";

    // An up-down movement that takes more than 2 seconds will not be registered (in nanoseconds).
    private static final long TIME_THRESHOLD_NS = TimeUnit.SECONDS.toNanos(2);

    private static final Object FILENAME = "autosport"+new Date().getTime() + ".txt";

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGyroscope;
    private Sensor mSensorMagnetic;
    private float[] mAccelerometer;
    private float[] mGeomagnetic;
    private float[] mGyroscope;
    private long mLastTime = 0;
    private int mJumpCounter = 0;
    private boolean mHandDown = true;

    public DemoItem[] mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AmbientModeSupport.attach(this);

        //setupViews();
        // Get a reference to the SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Get references to the sensors
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mItems = new DemoItem[]{
                new DemoItem(getString(R.string.send_file_by_intent), com.example.android.wearable.autosport.SendFileByIntentActivity.class),
                new DemoItem(getString(R.string.run_as_server), com.example.android.wearable.autosport.RunAsServerActivity.class),
                new DemoItem(getString(R.string.run_as_client), RunAsClientActivity.class)
        };

        initViews();
    }

    private void initViews() {
        ListView listView = (ListView) findViewById(R.id.demos_list);
        listView.setAdapter(new MyBaseAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, mItems[position].activityClass);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager.registerListener(this, mSensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL) &&
             mSensorManager.registerListener(this, mSensorGyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL) &&
                mSensorManager.registerListener(this, mSensorMagnetic,
                        SensorManager.SENSOR_DELAY_NORMAL)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Successfully registered for the sensors updates");
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unregistered for sensors events");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //TODO: check whether the senesors need calibrations
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mAccelerometer = new float[3];
            System.arraycopy(event.values, 0, mAccelerometer, 0, 3);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            mGyroscope = new float[3];
            System.arraycopy(event.values, 0, mGyroscope, 0, 3);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = new float[3];
            System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);
        }
        // for debugging
        if (mAccelerometer != null) {
            Log.d(TAG, "mx : "+mAccelerometer[0]+" my : "+mAccelerometer[1]+" mz : "+mAccelerometer[2]);
        }
        if (mGyroscope != null) {
            Log.d(TAG, "mx : "+mGyroscope[0]+" my : "+mGyroscope[1]+" mz : "+mGyroscope[2]);
        }
        if (mGeomagnetic != null) {
            Log.d(TAG, "mx : "+mGeomagnetic[0]+" my : "+mGeomagnetic[1]+" mz : "+mGeomagnetic[2]);
        }
        // organize data
        StringBuffer sportdata = null;
        sportdata.append(mAccelerometer[0] + "," + mAccelerometer[1] + "," + mAccelerometer[2] + "," +
                            mGyroscope[0] + "," + mGyroscope[1] + "," + mGyroscope[2] + "," +
                            mGeomagnetic[0] + "," + mGeomagnetic[1] + "," + mGeomagnetic[2]);
        // write to file
        try {
            appendToFile(sportdata);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No op.
    }

    /**
     * append data to file in internal storage
     * @param str: StringBuffer
     * @throws IOException
     */
    private void appendToFile(StringBuffer str) throws IOException {
        File file = getFileStreamPath((String) FILENAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream writer = openFileOutput(file.getName(), MODE_APPEND);
        writer.write(str.toString().getBytes());
        writer.flush();
        writer.close();
    }


    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    /** Customizes appearance for Ambient mode. (We don't do anything minus default.) */
    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
        }

        /**
         * Updates the display in ambient mode on the standard interval. Since we're using a custom
         * refresh cycle, this method does NOT update the data in the display. Rather, this method
         * simply updates the positioning of the data in the screen to avoid burn-in, if the display
         * requires it.
         */
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
        }
    }

    private class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = MainActivity.this.getLayoutInflater()
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            holder.nameTextView.setText(mItems[position].name);

            return convertView;
        }
    }

    static class ViewHolder {
        TextView nameTextView;

        ViewHolder(View parent) {
            nameTextView = (TextView) parent.findViewById(android.R.id.text1);
        }
    }

    static class DemoItem {
        String name;
        Class<?> activityClass;

        DemoItem(String name, Class<? extends Activity> clazz) {
            this.name = name;
            this.activityClass = clazz;
        }
    }

}
