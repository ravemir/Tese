package pt.ist.utl.thesis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pt.utl.ist.thesis.datacollector.R;
import pt.utl.ist.thesis.exception.ExternalStorageUnavailableException;
import pt.utl.ist.thesis.exception.ExternalStorageWriteProtectedException;
import android.app.Activity;
import android.content.Context;
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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AccelGPSRecActivity extends Activity {

	// Inner-class Listeners
	private final class AccelGPSListener implements LocationListener, SensorEventListener{
		// Sensor-related attributes and methods
		// FIXME Figure out if we need these for the rotation matrix
		@SuppressWarnings("unused") private float[] accel = new float[3];
		@SuppressWarnings("unused") private float[] magnet = new float[3];
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			// Write sensor values and the timestamp to the 'accelView'
		    float[] values = event.values;
		    int accuracy = event.accuracy;
		    
		    // Compute decimal value
		    long javaTime = new Date().getTime();
			long nanoTime = System.nanoTime();
		    long newtimestamp = javaTime * 1000000 + 						// Compute the timestamp
		    		(event.timestamp - nanoTime);							// in nanos first
		    String longStr = Long.valueOf(newtimestamp).toString();
			String tsString = longStr.substring(0, longStr.length()-6) + 	// Format the output string
					"." + longStr.substring(longStr.length()-6);			// to have the comma in the
		    																// correct space.
		    switch(event.sensor.getType()){
			    case Sensor.TYPE_LINEAR_ACCELERATION:
			    case Sensor.TYPE_ACCELEROMETER:
			    	accel = event.values.clone();	
					String line = "A" + LOGSEPARATOR +				// TODO Write to accelerometer file 
					    		tsString + LOGSEPARATOR + 
					    		values[0] + LOGSEPARATOR + 
					    		values[1] + LOGSEPARATOR + 
					    		values[2] + LOGSEPARATOR +
					    		accuracy + "\n";
				    
//				    Log.v(SENSOR_SERVICE, "Gravity: " 
//				    		+ values[0] + ", " + values[1] + ", " + values[2]);
		
				    // Write them to a file
				    writeToFile(line);
				    break;
			    case Sensor.TYPE_MAGNETIC_FIELD:
			    	magnet = event.values.clone();
			    	break;
		    }
		    // TODO Test line, remove after used
//		    float[] R = new float[9], I = new float[9];
//		    SensorManager.getRotationMatrix(R, I, accel, magnet);
//		    Log.v(SENSOR_SERVICE, "Rotation: " 
//		    		+ R[0] + ", " + R[1] + ", " + R[2] + ", "
//		    		+ R[3] + ", " + R[4] + ", " + R[5] + ", " 
//		    		+ R[6] + ", " + R[7] + ", "  + R[8]);
//		    Log.v(SENSOR_SERVICE, "Inclination: " 
//		    		+ I[0] + ", " + I[1] + ", " + I[2] + ", "
//		    		+ I[3] + ", " + I[4] + ", " + I[5] + ", " 
//		    		+ I[6] + ", " + I[7] + ", " + I[8]);
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		// Location related methods
		@Override
		public void onLocationChanged(Location location) {
		    // Write the new coordinates to a file
		    long timestamp = location.getTime();
		    int satelliteNo = location.getExtras().getInt("satellites");
			String line =   "L"+ LOGSEPARATOR + timestamp + LOGSEPARATOR +               // TODO Convert to X,Y,Z
		            location.getLatitude() + LOGSEPARATOR + 
		            location.getLongitude() + LOGSEPARATOR +
		            location.getAltitude() + LOGSEPARATOR +
		            location.getBearing() + LOGSEPARATOR +
		            location.getSpeed() +  LOGSEPARATOR +
		            satelliteNo + LOGSEPARATOR +
		            location.getAccuracy() + "\n";
		    writeToFile(line);										// TODO Write to location file
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		    // Write to file saying that the LocationProvider changed its status
		    int numSatell = extras.getInt("satellites");
		    switch(status) { // TODO Get the timestamp for this update
		        case LocationProvider.AVAILABLE:
		            writeToFile("[L:ts_unavail]: Provider is available, with " + numSatell + " satellites\n");break;
		        case LocationProvider.TEMPORARILY_UNAVAILABLE:
		            writeToFile("[L:ts_unavail]: Provider is temporarily unavailable\n");break;
		        case LocationProvider.OUT_OF_SERVICE:
		            writeToFile("[L:ts_unavail]: Provider is out of service\n");break;
		    }
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}
	}
	
	private final class GPSLocationListener implements LocationListener {
		public void onStatusChanged(String provider, int status, Bundle extras) {
		    // Write to file saying that the LocationProvider changed its status
		    int numSatell = extras.getInt("satellites");
		    switch(status) { // TODO Get the timestamp for this update
		        case LocationProvider.AVAILABLE:
		            writeToFile("[L:ts_unavail]: Provider is available, with " + numSatell + " satellites\n");break;
		        case LocationProvider.TEMPORARILY_UNAVAILABLE:
		            writeToFile("[L:ts_unavail]: Provider is temporarily unavailable\n");break;
		        case LocationProvider.OUT_OF_SERVICE:
		            writeToFile("[L:ts_unavail]: Provider is out of service\n");break;
		    }
		}

		public void onLocationChanged(Location location) {
		    // Write the new coordinates to a file
		    long timestamp = location.getTime();
		    int satelliteNo = location.getExtras().getInt("satellites");
			String line =   "L"+ LOGSEPARATOR + timestamp + LOGSEPARATOR +               // TODO Convert to X,Y,Z
		            location.getLatitude() + LOGSEPARATOR + 
		            location.getLongitude() + LOGSEPARATOR +
		            location.getAltitude() + LOGSEPARATOR +
		            location.getBearing() + LOGSEPARATOR +
		            location.getSpeed() +  LOGSEPARATOR +
		            satelliteNo + LOGSEPARATOR +
		            location.getAccuracy() + "\n";
		    writeToFile(line);										// TODO Write to location file
		}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	}
	private final class AccelerometerSensorEventListener implements SensorEventListener {
		// FIXME Figure out if we need these for the rotation matrix
		@SuppressWarnings("unused") private float[] accel = new float[3];
		@SuppressWarnings("unused") private float[] magnet = new float[3];
		
		public void onSensorChanged(SensorEvent event) {
		    // Write sensor values and the timestamp to the 'accelView'
		    float[] values = event.values;
		    int accuracy = event.accuracy;
		    
		    // Compute decimal value
		    long javaTime = new Date().getTime();
			long nanoTime = System.nanoTime();
		    long newtimestamp = javaTime * 1000000 + 						// Compute the timestamp
		    		(event.timestamp - nanoTime);							// in nanos first
		    String longStr = Long.valueOf(newtimestamp).toString();
			String tsString = longStr.substring(0, longStr.length()-6) + 	// Format the output string
					"." + longStr.substring(longStr.length()-6);			// to have the comma in the
		    																// correct space.
		    switch(event.sensor.getType()){
			    case Sensor.TYPE_LINEAR_ACCELERATION:
			    case Sensor.TYPE_ACCELEROMETER:
			    	accel = event.values.clone();	
					String line = "A" + LOGSEPARATOR +				// TODO Write to accelerometer file 
					    		tsString + LOGSEPARATOR + 
					    		values[0] + LOGSEPARATOR + 
					    		values[1] + LOGSEPARATOR + 
					    		values[2] + LOGSEPARATOR +
					    		accuracy + "\n";
				    
//				    Log.v(SENSOR_SERVICE, "Gravity: " 
//				    		+ values[0] + ", " + values[1] + ", " + values[2]);
		
				    // Write them to a file
				    writeToFile(line);
				    break;
			    case Sensor.TYPE_MAGNETIC_FIELD:
			    	magnet = event.values.clone();
			    	break;
		    }
		    // TODO Test line, remove after used
//		    float[] R = new float[9], I = new float[9];
//		    SensorManager.getRotationMatrix(R, I, accel, magnet);
//		    Log.v(SENSOR_SERVICE, "Rotation: " 
//		    		+ R[0] + ", " + R[1] + ", " + R[2] + ", "
//		    		+ R[3] + ", " + R[4] + ", " + R[5] + ", " 
//		    		+ R[6] + ", " + R[7] + ", "  + R[8]);
//		    Log.v(SENSOR_SERVICE, "Inclination: " 
//		    		+ I[0] + ", " + I[1] + ", " + I[2] + ", "
//		    		+ I[3] + ", " + I[4] + ", " + I[5] + ", " 
//		    		+ I[6] + ", " + I[7] + ", " + I[8]);
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	}

	// Sensor-related attributes
//    private SensorManager mSensorManager;
    private SensorEventListener sensorEventListener;
//    private Sensor mAccelerometer;
//    private Sensor mMagnetometer;
    @SuppressWarnings("unused")	private Sensor mLinearAcceleration; // FIXME Figure out if this will be used or not

    // Location-related attributes
//    private LocationManager mLocationManager;
    private LocationListener locationListener;
    
    // Managers, sensors and listeners
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private AccelGPSListener mAccelGPSListener;

    // External storage attributes
    private boolean mExternalStorageAvailable;
    private boolean mExternalStorageWriteable;
    private String folder;
    private String filename;
    
    // Application attributes
    private WakeLock mWakeLock;
    
    // Log attributes
    private static final String LOGSEPARATOR = ",";

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

        // (Re)Initialize the Listener-related activity attributes
        initializeListeners();
        
        // Fill the storage state details
        updateStorageState();
        
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        folder = sdcardPath + File.separator + "AccelGPS" + File.separator + "logs" + File.separator;

        // Create directories if necessary
        File f = new File(folder);
        if(f.mkdirs())
            recView.append("Created application folder ("+ folder +")");
        
        // Obtain screen-on lock, acquiring it
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
        		"[AccelGPSRec]: Screen Dim Wake Lock created");
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

            // Get date from system and set the file name to save
            String date = getDateForFilename();
            filename = folder + date + ".log";

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
//        String date = c.get(Calendar.YEAR) + "-"
//                + (c.get(Calendar.MONTH) + 1) + "-"
//                + c.get(Calendar.DAY_OF_MONTH) + "_" 
//                + c.get(Calendar.HOUR_OF_DAY) + "h"
//                + c.get(Calendar.MINUTE);
        SimpleDateFormat sdf = (SimpleDateFormat) java.text.DateFormat.getDateTimeInstance();// new SimpleDateFormat("yyyy-MM-dd_HH'h'mm");
        sdf.applyPattern("yyyy-MM-dd_HH'h'mm");
        String date = sdf.format(c.getTime());
        
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
     * Method that initializes the Listener-related Activity attributes
     * (to be called on the activity's first run or on orientation changes)
     */
    private void initializeListeners(){
    	// Initialize manager and sensor attributes
    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        // Define both the location and accelerometer listeners
//        sensorEventListener = new AccelerometerSensorEventListener();
//        locationListener = new GPSLocationListener();
        mAccelGPSListener = new AccelGPSListener();
    }
    /**
     * This function attaches the listeners to both the sensors and location services.
     * It also defines the behavior of both those listeners.
     */
    private void attachListeners() {
        // Listen to accelerometer (Raw and Acceleration filtered) and magnetometer events
//        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(sensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
//      mSensorManager.registerListener(sensorEventListener, mLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);

        // Listen to GPS events
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    	
    	// Listen do Accelerometer, Magnetometer and GPS events
    	mSensorManager.registerListener(mAccelGPSListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    	mSensorManager.registerListener(mAccelGPSListener, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mAccelGPSListener);
    }

	@Override
	protected void onPause() {
		super.onPause();
		
        // Release the wake-lock
        mWakeLock.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
        // Acquire partial wake-lock
        mWakeLock.acquire();
	}
}
