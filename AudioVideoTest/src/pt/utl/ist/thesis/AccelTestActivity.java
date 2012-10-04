package pt.utl.ist.thesis;

import pt.utl.ist.thesis.R;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class AccelTestActivity extends Activity implements SensorEventListener {

    // Sensor attributes
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    // Interface attributes
    private TextView accelTextView;
    private TextView accelAVGView;
    
    // Constant values
    private final Integer SAMPLES = 100;
    private final Double AVGSTARTVALUE = 20000000D;

    // Variables used to compute average over several values
    private Double avgUpdateTime = AVGSTARTVALUE; // nanoseconds
    private Double previousPoll = 0D;
    private Double polls = 1D;
    private Double[] avgValues = new Double[SAMPLES];
    private Double acum = 0D;
    private Integer currentIndex = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accel);

        // Get UI references
        accelTextView = (TextView) findViewById(R.id.accelTextView);
        accelAVGView = (TextView) findViewById(R.id.accelAvgView);

        // Listen to accelerometer events
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        
        // Set the first interval value
        avgValues[0] = AVGSTARTVALUE;
        
        // Warn the user that the system is collecting dat to compute data
        accelAVGView.setText("Collecting data to compute average...");
    }

    // Methods added in virtue of treating the Accelerometer events
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Clear previously written text from 'accelView'
        accelTextView.setText("");

        // Write sensor values and the timestamp to the 'accelView'
        float[] values = event.values;
        Double timestamp = new Double(event.timestamp);
        accelTextView.append(timestamp + "\n");
        for(float value : values) {
            accelTextView.append(value + ";\n");
        }

        if(previousPoll == 0) {                                     // On the first measurement, we can't determine how  
            previousPoll = (Double) (timestamp - AVGSTARTVALUE);    // long it took to get a reading from the start, so
        }                                                           // we calculate a timestamp from the starting average

        // Add the latest value to the subtracted 'acum'
        double latestUpdateTime = timestamp - previousPoll;
        acum += latestUpdateTime;

        // Determine the current index
        currentIndex = (int) (polls % SAMPLES);

        // If we have enough data
        if(polls >= SAMPLES) {

            // Subtract 'avgValues[polls%100]' from 'acum'
            acum -= avgValues[currentIndex];

            // Calculate new average and update form 
            avgUpdateTime = acum / SAMPLES;
            
            // Calculate the corresponding rate in Hz
            Double frequency = 1 / (avgUpdateTime * 0.000000001);
            
            // Print-out the new average and frequency
            accelAVGView.setText((avgUpdateTime * 0.000001) + " miliseconds\n" + frequency + " Hz");
        }

        // Insert the latest value into the array
        avgValues[currentIndex] = latestUpdateTime;
        
        // Record this timestamp
        previousPoll = timestamp;
        
        // Mark this poll
        polls++;
    }
}