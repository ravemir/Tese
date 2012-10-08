package pt.utl.ist.thesis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import pt.utl.ist.thesis.exception.ExternalStorageUnavailableException;
import pt.utl.ist.thesis.exception.ExternalStorageWriteProtectedException;
import pt.utl.ist.thesis.R;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AccelGPSRecActivity extends Activity {

    // Sensor-related attributes
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener sensorEventListener;

    // Location-related attributes
    private LocationManager mLocationManager;
    private LocationListener locationListener;

    // External storage attributes
    private boolean mExternalStorageAvailable;
    private boolean mExternalStorageWriteable;
    private String filename;

    // Interface attributes
    private TextView recView;

    /**
     * Called when the activity is first created. 
     * This is also where both the listeners' behavior is defined.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accel_gps_rec);

        // Get UI references
        recView = (TextView) findViewById(R.id.recView);

        // Fill the storage state details
        updateStorageState();

        // Define both the location and acceleromenter listeners
        sensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                // Write sensor values and the timestamp to the 'accelView'
                float[] values = event.values;
                Long timestamp = (new Date()).getTime() + ((event.timestamp - System.nanoTime()) / 1000000L);
                String line = "A, "+ timestamp +", ";															// TODO Write to location file
                line += values[0] + ", " + values[1]+ ", " + values[2];
                line += "\n";

                // Write them to a file
                writeToFile(line);
            }

            @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Write to file saying that the LocationProvider changed its status
                int numSatell = extras.getInt("satellites");
                switch(status) { // TODO Obter o timestamp desta actualização
                    case LocationProvider.AVAILABLE:
                        writeToFile("[L:ts_unavail]: Provider is available, with " + numSatell + " satellites\n");break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        writeToFile("[L:ts_unavail]: Provider is temporarily unavailable\n");break;
                    case LocationProvider.OUT_OF_SERVICE:
                        writeToFile("[L:ts_unavail]: Provider is out of service\n");break;
                }
            }

            @Override
            public void onLocationChanged(Location location) {
                // Write the new coordinates to a file
                long timestamp = location.getTime();
//                String line =   "[L:"+ timestamp +"]: " +               // TODO Convert to X,Y,Z
//                        location.getLatitude() + ", " + 
//                        location.getLongitude() + ";\n";
                Bundle extras = location.getExtras();
				String line =   "L, "+ timestamp +", " +               // TODO Convert to X,Y,Z
                        location.getLatitude() + ", " + 
                        location.getLongitude() + ", " +
                        location.getLatitude() + ", " +
                        location.getLongitude() + ", " +
                        extras.getInt("satellites") + ", " +
                        location.getSpeed() + "\n";
                writeToFile(line);										// TODO Write to location file
            }
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) {}
        };

        // Create directories if necessary
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = sdcardPath + File.separator + "AccelGPS" + File.separator + "logs" + File.separator;
        File f = new File(folder);
        if(f.mkdirs())
            recView.append("Created application folder ("+ folder +")");
    }

    /**
     * This is the called function when the toggle button is pressed.
     * 
     * @param v The view whose interaction triggered this call (in this case, the toggle button).
     */
    public void toggleOnClick(View v) {
        // Test if the button was toggled on or off
        ToggleButton tb = (ToggleButton) v; 
        if(tb.isChecked()) { 
            // Attach Location and Sensor listeners
            attachListeners();

            // Get date from system
            String date = getDateForFilename();
            String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            filename = sdcardPath + File.separator + "AccelGPS" + File.separator + date + ".log";

            // Display toast message with the filename being written
            String message = "Saving log to file \"" + filename + "\"";
            displayToast(message);
        } else {
            // Detach listeners
            mSensorManager.unregisterListener(sensorEventListener);
            mLocationManager.removeUpdates(locationListener);
        }
    }

    // TODO Make documentation
    public void toggleMockGps(View v) {
        // TODO Implement functionality
    }

    /**
     * This function displays a toast containing the given message.
     * 
     * @param message The message to be displayed as a toast.
     */
    private void displayToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Function used to return the current date, ready to be used in a filename.
     * 
     * @return A string representing the date in a "YYYY-MM-DD_HH:MM" format
     */
    private String getDateForFilename() {
        Calendar c = Calendar.getInstance();
        String date = c.get(Calendar.YEAR) + "-"
                + c.get(Calendar.MONTH) + "-"
                + c.get(Calendar.DAY_OF_MONTH) + "_" 
                + c.get(Calendar.HOUR_OF_DAY) + "h"
                + c.get(Calendar.MINUTE);
        return date;
    }

    /**
     * This function opens a file, writes the given line and closes it.
     * 
     * @param line The line to be written.
     */
    private void writeToFile(String line)  {
        try {
            // Check for external storage availability
            checkExternalStore();

            // Write line to file
            FileWriter fw = new FileWriter(filename, true);
            fw.write(line);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            recView.append(e.getMessage());
        } catch (ExternalStorageUnavailableException e) {
            e.printStackTrace();
            recView.append(e.getMessage());
        } catch (ExternalStorageWriteProtectedException e) {
            e.printStackTrace();
            recView.append(e.getMessage());
        }
    }

    /**
     * Method called to check for the availability of the external storage.
     * 
     * @throws ExternalStorageUnavailableException External storage was unavailable in the device.
     * @throws ExternalStorageWriteProtectedException External storage was write-protected.
     */
    private void checkExternalStore()
            throws ExternalStorageUnavailableException,
            ExternalStorageWriteProtectedException {
        updateStorageState();
        if(!mExternalStorageAvailable)
            throw new ExternalStorageUnavailableException(getString(R.string.ext_store_unavail));
        else if(!mExternalStorageWriteable)
            throw new ExternalStorageWriteProtectedException(getString(R.string.ext_store_wprotect));
    }

    /**
     * This function updates the state attributes regarding external storage availability.
     */
    private void updateStorageState() {
        // Check external storage availability
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    /**
     * This function attaches the listeners to both the sensors and location services.
     * It also defines the behavior of both those listeners.
     */
    private void attachListeners() {
        // Listen to accelerometer events
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        // Listen to GPS events
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
}
