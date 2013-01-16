package pt.utl.ist.thesis.datacollector;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pt.utl.ist.thesis.util.UIUpdater;
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
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class CollectionActivity extends Activity {

	// Inner-class Listeners
	private final class AccelGPSListener implements LocationListener, SensorEventListener {
		// Sensor-related attributes and methods
		private float[] accel = new float[3];
		@SuppressWarnings("unused") private float[] magnet = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event) {
			// Write sensor values and the timestamp to the 'accelView'
			float[] values = event.values;
			int accuracy = event.accuracy;

			// Compute the timestamp in nanos first
			long javaTime = new Date().getTime();
			long nanoTime = System.nanoTime();
			long newtimestamp = javaTime * 1000000 + 						
					(event.timestamp - nanoTime);
			String tsString = printNanosToMilis(newtimestamp);

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

				// Write them to a file
				writeToFile(line);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				magnet = event.values.clone();
				break;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}

		// Location related attributes methods
		private Location loc;
		
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
			
			loc = location;
		}

		public float[] getAccel() {
			return accel;
		}

		public Location getLoc() {
			if (loc == null){
				loc = new Location(LocationManager.GPS_PROVIDER);
				loc.setSpeed(0);
			}
			return loc;
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Write to file saying that the LocationProvider changed its status
			int numSatell = extras.getInt("satellites");
			long timestamp = new Date().getTime();
			String lineHeader = "L" + LOGSEPARATOR + timestamp + LOGSEPARATOR;
			switch(status) {
				case LocationProvider.AVAILABLE:
					writeToFile(lineHeader + getString(R.string.provider_available_log)
							+ numSatell + " satellites\n");break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					writeToFile(lineHeader + getString(R.string.provider_temporarily_unavailable_log));break;
				case LocationProvider.OUT_OF_SERVICE:
					writeToFile(lineHeader + getString(R.string.provider_out_of_service_log));break;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}
	}

	// Managers, sensors and listeners
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private AccelGPSListener mAccelGPSListener;

	// External storage attributes
	private String logFolder;
	private String filename;

	// Application attributes
	private WakeLock mWakeLock;
	private UIUpdater mUIUpdater;

	// Log attributes
	private static final String LOGSEPARATOR = ",";
	
	// Class runnables
	private final Runnable uiUpdaterRunnable = new Runnable() {
	     @Override 
	     public void run() {
	 		// Get values
	 		float[] accel = mAccelGPSListener.getAccel();
	 		Location loc = mAccelGPSListener.getLoc();
	 		
	 		// Update Accelerometer Views
	 		((TextView) findViewById(R.id.XValue)).setText(Float.valueOf(accel[0]).toString());
	 		((TextView) findViewById(R.id.YValue)).setText(Float.valueOf(accel[1]).toString());
	 		((TextView) findViewById(R.id.ZValue)).setText(Float.valueOf(accel[2]).toString());
	 		
	 		// Update Location Views
	 		((TextView) findViewById(R.id.LatValue)).setText(Double.valueOf(loc.getLatitude()).toString());
	 		((TextView) findViewById(R.id.LongValue)).setText(Double.valueOf(loc.getLongitude()).toString());
	 		((TextView) findViewById(R.id.SpeedValue)).setText(Double.valueOf(loc.getSpeed()).toString());
	     }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set it as a fullscreen activity
		setFullScreen();
		
		// Set the View content
		setContentView(R.layout.activity_collection);

		// Get extra attributes from extras bundle
		logFolder = (String) getIntent().getExtras().get("logFolder");

		// Initialize and attach the Listener-related activity attributes
		initializeListeners();
		attachListeners();

		// Get date from system and set the file name to save
		String date = getDateForFilename();
		filename = logFolder + date + ".log";

		// Display toast message with the filename being written to
		String message = getString(R.string.log_file_message) + filename + "\"";
		displayToast(getApplicationContext(), message);

		mUIUpdater = new UIUpdater(this, uiUpdaterRunnable);
		
		// Obtain screen-on lock, acquiring it
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				getString(R.string.wake_lock_log));
	}

	/**
	 * 
	 */
	private void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_collection, menu);
		return true;
	}


	/**
	 * This function opens a file, writes the given line and closes it.
	 * 
	 * @param line The line to be written.
	 */
	private void writeToFile(String line)  {
		try {
			// Write line to file
			FileWriter fw = new FileWriter(filename, true);
			fw.write(line);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Define both the location and accelerometer listeners
		mAccelGPSListener = new AccelGPSListener();
	}

	/**
	 * This function attaches the listeners to both the sensors and location services.
	 * It also defines the behavior of both those listeners.
	 */
	private void attachListeners() {
		// Listen to Accelerometer, Magnetometer and GPS events
		mSensorManager.registerListener(mAccelGPSListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(mAccelGPSListener, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mAccelGPSListener);
	}

	/**
	 * This function detaches the previously attached listeners for sensor
	 * and location services.
	 */
	private void detachListeners() {
		// Detach both Sensor and GPS listeners
		mSensorManager.unregisterListener(mAccelGPSListener);
		mLocationManager.removeUpdates(mAccelGPSListener);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Write to file pause error
		long timestamp = new Date().getTime();
		writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + 
				getString(R.string.activity_resumed_log));
		
		// Reattach listeners
		attachListeners();
		
		// Restart the UI updater
		mUIUpdater.startUpdates();

		// Acquire partial wake-lock
		mWakeLock.acquire();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Write to file pause error
		long timestamp = new Date().getTime();
		writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + 
				getString(R.string.activity_paused_log));
		Log.e(AUDIO_SERVICE, getString(R.string.activity_paused_log));

		// Detach listeners
		detachListeners();
		
		// Stop the UI updater
		mUIUpdater.stopUpdates();

		// Release the wake-lock
		mWakeLock.release();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// Back-button functionality
	private static final int PERIOD = 2000;
	private Boolean backWasPressed = false;
	
	@Override
	public void onBackPressed() {
		// Check if the button had already been pressed
		if(backWasPressed){
			// Perform expected functionality (exit)
			super.onBackPressed();
		} else {
			// Register the press and display message
			backWasPressed = true;
			displayToast(getApplicationContext(), 
					getString(R.string.twice_to_exit_message));
			
			// Schedule a regiter reset
			new Handler().postDelayed(new Runnable() {
				@Override public void run() {backWasPressed = false;}}, PERIOD);
		}
	}
	
	// Assorted functions
	/**
	 * Prints out the millisecond form of the provided 
	 * nanosecond timestamp.
	 * 
	 * @param nanosTimestamp The timestamp, in nanosecond units
	 * @return A String containing the millisecond form of the timestamp
	 */
	private static String printNanosToMilis(long nanosTimestamp) {
		// Convert to String type 
		String longStr = Long.valueOf(nanosTimestamp).toString();

		String tsString = null;

		// Format the output String to have the comma in the correct place
		if(nanosTimestamp >= 1000000){
			// ...if it has an integer part
			tsString = longStr.substring(0, longStr.length()-6) + 	
					"." + longStr.substring(longStr.length()-6);
		} else {
			// ...or if doesn't, return a string specifying the occurence
			tsString = "[< 1ms]";
		}

		return tsString;
	}

	/**
	 * This function displays a toast containing the given message.
	 * 
	 * @param context The context where this message will be displayed
	 * @param message The message to be displayed as a toast.
	 */
	public static void displayToast(Context context, String message) {
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 * Function used to return the current date, ready to be used in a filename.
	 * 
	 * @return A string representing the date in a "YYYY-MM-DD_HH:MM" format
	 */
	private String getDateForFilename() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = (SimpleDateFormat) java.text.DateFormat.getDateTimeInstance();
		sdf.applyPattern("yyyy-MM-dd_HH'h'mm");
		String date = sdf.format(c.getTime());

		return date;
	}
}
