package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Master N on 11/22/2015.
 */
public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private float mLightQuantity;
    TextView textLIGHT_available, textLIGHT_reading;
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_test);
        textLIGHT_reading = (TextView)findViewById(R.id.LIGHT_reading);
        textLIGHT_available = (TextView)findViewById(R.id.LIGHT_available);
        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if(mLightSensor != null){
            textLIGHT_available.setText("Sensor.TYPE_LIGHT Available");
        }else{
            textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        mLightQuantity = event.values[0];
        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            textLIGHT_reading.setText("LIGHT: " + event.values[0]);
        }
        // Do something with this sensor data.
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
