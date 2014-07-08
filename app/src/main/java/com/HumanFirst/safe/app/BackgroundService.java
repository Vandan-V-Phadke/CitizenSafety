package com.HumanFirst.safe.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service {

    public BackgroundService() {
    }

    // GPSTracker class
    GPSTracker gps;
    PowerButtonReceiver mReceiver ;
    Handler handler;
    SharedPreferences.Editor editor ;
    ShakeEventListener mSensorListener;
    SensorManager mSensorManager;
    static int shakeCount ;


    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "MyWakeLock");
        wakeLock.acquire();

        //Register the BroadCast receiver for detecting power button presses
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new PowerButtonReceiver();
        registerReceiver(mReceiver , filter);

        mSensorListener = new ShakeEventListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // mSensorListener = new ShakeEventListener();

        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

        final long[] firstpresstime = new long[1];
        final long[] thirdpresstime = new long[1];

        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            public void onShake() {
                Log.d("Shake Detected","Shake Detected");
                shakeCount++ ;

                if(shakeCount == 1 ) {
                    firstpresstime[0] = System.currentTimeMillis();
                    Log.d("First shake detected","" + firstpresstime);
                }

                if (shakeCount == 2){
                    thirdpresstime[0] = System.currentTimeMillis();
                    if (thirdpresstime[0] - firstpresstime[0] <= 5000)
                        Toast.makeText(getApplicationContext(), "Emergency Detected", Toast.LENGTH_SHORT);
                    Log.d("Second shake detected","" + thirdpresstime);
                    Log.d("Emergency Detected","Emergency Detected");
                    shakeCount = 0 ;
                    handler.removeCallbacks(call);

                    Intent intent = new Intent(getBaseContext(),SOSActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);

                }
            }
        });

        //Getting the gps ccordinates
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GPS-Coordinates" , Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        gps = new GPSTracker(getApplicationContext() , 0);

        handler = new Handler();
        handler.postDelayed(call , 10000);
    }

    final Runnable call = new Runnable() {
        @Override
        public void run() {
            try {
                /*
                Code to update GPS Position after regular intervals of time
                Also call the same handler here
                */
                // create class object
                gps = new GPSTracker(getApplicationContext() , 0);

                // check if GPS enabled
                if (!gps.canGetLocation()) {
                    /*
                    can't get location
                    GPS or Network is not enabled
                    Here GPS settings have to be turned on programatically and then the location has to be taken
                    No permission to enable GPS by 3rd party apps
                    So prompt user while starting the service to turn the GPS on
                    */

                }

                //Check if the current latitudes and longitudes are obtained correctly(Non-Zero)
                double latitude = 0 ;
                double longitude = 0 ;

                while (latitude == 0 && longitude == 0 ) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    Log.d("App" , "Still in loop");

                }
                //Write these values in the sharedpreferences
                editor.putFloat("GPS-lat" , (float) latitude);
                editor.putFloat("GPS-long" , (float) longitude);
                
                Log.d("Sharedprefs updated" , "" + latitude + longitude);

                handler.postDelayed(this , 10000);

            }catch (Exception e){e.printStackTrace();}
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mSensorManager.unregisterListener(mSensorListener);
        handler.removeCallbacks(call);
    }
}
