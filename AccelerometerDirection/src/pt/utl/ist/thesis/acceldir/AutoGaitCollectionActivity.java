package pt.utl.ist.thesis.acceldir;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pt.utl.ist.thesis.acceldir.sql.AutoGaitSegmentDataSource;
import pt.utl.ist.thesis.acceldir.util.AndroidUtils;
import pt.utl.ist.thesis.acceldir.util.UIUpdater;
import pt.utl.ist.thesis.signalprocessor.AutoGaitModelerAnalyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.thesis.util.SampleRunnable;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.GPSReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class AutoGaitCollectionActivity extends Activity {

	// Inner-class Listeners
	private final class AccelGPSListener implements LocationListener, SensorEventListener {
		
		private static final int SAMPLERATE = 100;
		
		// Sensor-related attributes and methods (sized 4 to be compatible with 4x4 matrices)
		private float[] accel = new float[4];
		
		private static final int CIRCBUFFSIZE = SAMPLERATE; // FIXME Circular buffer size (should be computed according to
															//		 sampling rate)

		// AutoGait related attributes
		private RawReadingSource accelRS = new RawReadingSource(CIRCBUFFSIZE);
		private RawReadingSource locRS = new RawReadingSource();
		private ButterworthFilter filter = new ButterworthFilter(10, 5, SAMPLERATE, true);
		private StepAnalyser stepA = new StepAnalyser(SAMPLERATE);
		private AutoGaitModelerAnalyser agma = new AutoGaitModelerAnalyser();
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			// Compute the timestamp in nanos first
			long newtimestamp = AndroidUtils.computeJavaTimeStamp(event.timestamp);
			String tsString = AndroidUtils.printNanosToMilis(newtimestamp);

			switch(event.sensor.getType()){
			case Sensor.TYPE_LINEAR_ACCELERATION:
			case Sensor.TYPE_ACCELEROMETER:
				System.arraycopy(event.values, 0, accel, 0, 3);
				String accelLine = "A" + LOGSEPARATOR + 
						tsString + LOGSEPARATOR + 
						accel[0] + LOGSEPARATOR + 
						accel[1] + LOGSEPARATOR + 
						accel[2] + LOGSEPARATOR +
						event.accuracy + "\n";
				
				// Push Acceleration reading
				accelRS.pushReading(new AccelReading(tsString, accel));
				
				// Write it to a file
				writeToFile(accelLine);
				break;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}

		// Location related attributes methods
		private Location loc;

		@Override
		public void onLocationChanged(Location location) {
			// Get values from location (timestamp, satellite number,...)
			Double timestamp = (double) location.getTime();
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double altitude = location.getAltitude();
			double bearing = location.getBearing();
			double speed = location.getSpeed();
			int satelliteNo = location.getExtras().getInt("satellites");
			float accuracy = location.getAccuracy();
			
			// Push GPSReading to RawReadingSource
			locRS.pushReading(new GPSReading(timestamp, latitude, 
					longitude, bearing, speed));
			
			// Write the new coordinates to a file
			String line =   "L"+ LOGSEPARATOR + timestamp + LOGSEPARATOR +
					latitude + LOGSEPARATOR + 
					longitude + LOGSEPARATOR +
					altitude + LOGSEPARATOR +
					bearing + LOGSEPARATOR +
					speed +  LOGSEPARATOR +
					satelliteNo + LOGSEPARATOR +
					accuracy + "\n";
			writeToFile(line);

			loc = location;
		}

		public float[] getLatestAccel() {
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
			String lineHeader = "I" + LOGSEPARATOR + timestamp + LOGSEPARATOR;
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

		/**
		 * Attaches the appropriate Filter and Analyser objects
		 * to perform the AutoGait model calibration.
		 */
		public void attachFiltersAndAnalysers(){
			// Attach the Butterworth filter to the RawReadingSource
			accelRS.attachFilter(filter);
			
			// Attach StepAnalyser to filter
			filter.attachAnalyser(stepA);
			
			// Attach the StepAnalyser and the GPSReading
			// RawReadingSource to the AutoGaitModelerAnalyser
			stepA.attachAnalyser(agma);
			locRS.attachAnalyser(agma);
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
	private Sensor mGravity;
	private Sensor mMagnetometer;
	private AccelGPSListener mAccelGPSListener;

	// External storage attributes
	private String logFolder;
	private String filename;

	// Database attributes
	private AutoGaitSegmentDataSource agsds;
	
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
			float[] accel = mAccelGPSListener.getLatestAccel();
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

		// Get date from system and set the file name to save
		String date = getDateForFilename();
		filename = logFolder + date + ".log";
		
		// Create the DB DAO object
		agsds = new AutoGaitSegmentDataSource(this);
		agsds.open();
		
		// Initialize and attach the Listener-related attributes
		initializeListeners();
		attachListeners();

		// Display toast message with the filename being written to
		String message = getString(R.string.log_file_message) + filename + "\"";
		AndroidUtils.displayToast(getApplicationContext(), message);

		mUIUpdater = new UIUpdater(this, uiUpdaterRunnable);

		// Create screen-dim wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				getString(R.string.wake_lock_log));
	}

	/**
	 * 
	 */
	public void initializeAutoGaitModeler() {
		// Attach all the filter and analyser objects acoordingly
		mAccelGPSListener.attachFiltersAndAnalysers();

		// Get the stored samples and insert them into the model
		double[][] storedSamples = agsds.getAllSegmentDataSamples();
		mAccelGPSListener.agma.restoreDataSamples(storedSamples);
		
		// Set the SampleRunnable object to update the data model
		mAccelGPSListener.agma.setSampleUpdater(new SampleRunnable() {
			@Override
			public void run() {
				// Add the sample value to the application's DAO
				agsds.createSegmentData(sample[0], sample[1]);
			}
		});
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
		// Initialize sensor and location manager and magnetometer sensor
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		// Gravity and LinearAcceleration sensors only available from Gingerbread and up
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			initializeAcceleration();
		else
			initializeAccelerationPreGingerbread();

		// Define both the location and accelerometer listeners
		mAccelGPSListener = new AccelGPSListener();
		
		// Initialize the AutoGait model, if appropriate
		initializeAutoGaitModeler();
	}

	/**
	 * Initialize acceleration sensors on pre-Gingerbread phones.
	 */
	public void initializeAccelerationPreGingerbread() {
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGravity = mAccelerometer;
	}

	/**
	 * Initialize acceleration sensors on Gingerbread+ phones.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void initializeAcceleration() {
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	}

	/**
	 * This function attaches the listeners to both the sensors and location services.
	 * It also defines the behavior of both those listeners.
	 */
	private void attachListeners() {
		// Get the timestamp
		long timestamp = new Date().getTime();
		
		// Listen to accelerometer, magnetometer and GPS events
		if(mAccelerometer != null && mGravity != null){
			mSensorManager.registerListener(mAccelGPSListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
			mSensorManager.registerListener(mAccelGPSListener, mGravity, SensorManager.SENSOR_DELAY_FASTEST);
			writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + getString(R.string.accel_listener_attached));
		}
		if(mMagnetometer != null){
			mSensorManager.registerListener(mAccelGPSListener, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
			writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + getString(R.string.magnet_listener_attached));
		}
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mAccelGPSListener);
	}

	/**
	 * This function detaches the previously attached listeners for sensor
	 * and location services.
	 */
	private void detachListeners() {
		// Get the timestamp
		long timestamp = new Date().getTime();
		
		// Detach both Sensor and GPS listeners
		if(mAccelGPSListener != null) {
			mSensorManager.unregisterListener(mAccelGPSListener);
			writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + getString(R.string.accel_magnet_listeners_dettached));
		}
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

		// Write to file pause message
		long timestamp = new Date().getTime();
		writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + 
				getString(R.string.activity_paused_log));

		// Detach listeners
		detachListeners();

		// Stop the UI updater
		mUIUpdater.stopUpdates();

		// Release the wake-lock
		mWakeLock.release();
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
			AndroidUtils.displayToast(getApplicationContext(), 
					getString(R.string.twice_to_exit_message));

			// Schedule a regiter reset
			new Handler().postDelayed(new Runnable() {
				@Override public void run() {backWasPressed = false;}}, PERIOD);
		}
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
