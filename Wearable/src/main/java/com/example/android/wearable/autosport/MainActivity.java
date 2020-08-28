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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.wear.ambient.AmbientModeSupport;

import com.example.android.wearable.autosport.fragments.CounterFragment;
import com.example.android.wearable.autosport.fragments.SettingsFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        implements AmbientModeSupport.AmbientCallbackProvider, SensorEventListener {

    private static final String TAG = "MainActivity";

    // An up-down movement that takes more than 2 seconds will not be registered (in nanoseconds).
    private static final long TIME_THRESHOLD_NS = TimeUnit.SECONDS.toNanos(2);

    /**
     * Earth gravity is around 9.8 m/s^2 but user may not completely direct his/her hand vertical
     * during the exercise so we leave some room. Basically, if the x-component of gravity, as
     * measured by the Gravity sensor, changes with a variation delta > 0.03 from the hand down
     * and hand up threshold we define below, we consider that a successful count.
     *
     * This is a very rudimentary formula and is by no means production accurate. You will want to
     * take into account Y and Z gravity changes to get a truly accurate jumping jack.
     *
     * This sample is just meant to show how to easily get sensor values and use them.
     */
    private static final float HAND_DOWN_GRAVITY_X_THRESHOLD = -.040f;
    private static final float HAND_UP_GRAVITY_X_THRESHOLD = -.010f;
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


    private ViewPager mPager;
    private CounterFragment mCounterPage;
    private SettingsFragment mSettingPage;
    private ImageView mSecondIndicator;
    private ImageView mFirstIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jumping_jack_layout);

        AmbientModeSupport.attach(this);

        setupViews();

        mJumpCounter = com.example.android.wearable.autosport.Utils.getCounterFromPreference(this);
        // Get a reference to the SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Get references to the sensors
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void setupViews() {
        mPager = findViewById(R.id.pager);
        mFirstIndicator = findViewById(R.id.indicator_0);
        mSecondIndicator = findViewById(R.id.indicator_1);

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());

        mCounterPage = new CounterFragment();
        mSettingPage = new SettingsFragment();

        adapter.addFragment(mCounterPage);
        adapter.addFragment(mSettingPage);
        setIndicator(0);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                // No-op.
            }

            @Override
            public void onPageSelected(int i) {
                setIndicator(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // No-op.
            }
        });

        mPager.setAdapter(adapter);
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
    /**
     * A very simple algorithm to detect a successful up-down movement of hand(s). The algorithm
     * is based on a delta of the handing being up vs. down and taking less than TIME_THRESHOLD_NS
     * to happen.
     *
     *
     * This algorithm isn't intended to be used in production but just to show what's possible with
     * sensors. You will want to take into account other components (y and z) and other sensors to
     * get a more accurate reading.
     */
    private void detectJump(float xGravity, long timestamp) {

        if ((xGravity <= HAND_DOWN_GRAVITY_X_THRESHOLD)
                || (xGravity >= HAND_UP_GRAVITY_X_THRESHOLD)) {

            if (timestamp - mLastTime < TIME_THRESHOLD_NS) {
                // Hand is down when yValue is negative.
                onJumpDetected(xGravity <= HAND_DOWN_GRAVITY_X_THRESHOLD);
            }

            mLastTime = timestamp;
        }
    }

    /**
     * Called on detection of a successful down -> up or up -> down movement of hand.
     */
    private void onJumpDetected(boolean handDown) {
        if (mHandDown != handDown) {
            mHandDown = handDown;

            // Only count when the hand is down (means the hand has gone up, then down).
            if (mHandDown) {
                mJumpCounter++;
                setCounter(mJumpCounter);
            }
        }
    }

    /**
     * Updates the counter on UI, saves it to preferences and vibrates the watch when counter
     * reaches a multiple of 10.
     */
    private void setCounter(int i) {
        mJumpCounter = i;
        mCounterPage.setCounter(i);
        com.example.android.wearable.autosport.Utils.saveCounterToPreference(this, i);
        if (i > 0 && i % 10 == 0) {
            com.example.android.wearable.autosport.Utils.vibrate(this, 0);
        }
    }

    public void resetCounter() {
        setCounter(0);
    }

    /**
     * Sets the page indicator for the ViewPager.
     */
    private void setIndicator(int i) {
        switch (i) {
            case 0:
                mFirstIndicator.setImageResource(R.drawable.full_10);
                mSecondIndicator.setImageResource(R.drawable.empty_10);
                break;
            case 1:
                mFirstIndicator.setImageResource(R.drawable.empty_10);
                mSecondIndicator.setImageResource(R.drawable.full_10);
                break;
        }
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

}
