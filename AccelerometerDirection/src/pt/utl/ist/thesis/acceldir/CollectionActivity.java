package pt.utl.ist.thesis.acceldir;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.math.stat.descriptive.SynchronizedSummaryStatistics;

import pt.utl.ist.thesis.acceldir.sql.AutoGaitSegmentDataSource;
import pt.utl.ist.thesis.acceldir.util.AndroidUtils;
import pt.utl.ist.thesis.acceldir.util.UIUpdater;
import pt.utl.ist.thesis.signalprocessor.PositioningAnalyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.thesis.util.SensorReadingRunnable;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.GPSReading;
import pt.utl.ist.util.sensor.reading.OrientationReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;
import pt.utl.ist.util.source.filters.MovingAverageFilter;
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

public class CollectionActivity extends Activity {

	// Inner-class Listeners
	private final class AccelGPSListener implements LocationListener, SensorEventListener {

		private int SAMPLERATE = 50;
		private SynchronizedSummaryStatistics sss = new SynchronizedSummaryStatistics();
		private long lastTimestamp = -1;

		public AccelGPSListener(int sRate){
			SAMPLERATE = sRate;
			
			accelRS = new RawReadingSource(CIRCBUFFSIZE);
			oriRS = new RawReadingSource(CIRCBUFFSIZE);
			accelBWF = new ButterworthFilter(10, 5, SAMPLERATE, true);
			oriMAF = new MovingAverageFilter(SAMPLERATE);
			stepA = new StepAnalyser(SAMPLERATE);
			positioningA = new PositioningAnalyser(SAMPLERATE);
		}

		// Sensor-related attributes and methods (sized 4 to be compatible with 4x4 matrices)
		private float[] accel = new float[4];
		private float[] gravity = new float[4];
		private float[] magnet = new float[4];
		private float[] rot = new float[16];
		private float[] incl = new float[16];
		private float[] orientations = new float[3];

		private int CIRCBUFFSIZE = SAMPLERATE;	// FIXME Circular buffer size (should be computed according to
												//		 sampling rate)

		// Positioning related attributes
		private RawReadingSource accelRS;
		private RawReadingSource oriRS;
		private RawReadingSource locRS = new RawReadingSource();
		private ButterworthFilter accelBWF;
		private MovingAverageFilter oriMAF;
		private StepAnalyser stepA;
		private PositioningAnalyser positioningA;

		@Override
		public void onSensorChanged(SensorEvent event) {
			// Compute the timestamp in nanos first
			long newtimestamp = AndroidUtils.computeJavaTimeStamp(event.timestamp);

			// ...then generate a formatted millis string
			String tsString = AndroidUtils.printNanosToMilis(newtimestamp);

			switch(event.sensor.getType()){
			case Sensor.TYPE_LINEAR_ACCELERATION:
			case Sensor.TYPE_ACCELEROMETER:
				// Compute current rate average
				averageSampleRateComputation(event);				

				// Copy event values over and process them
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
			case Sensor.TYPE_GRAVITY:
				System.arraycopy(event.values, 0, gravity, 0, 3);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				System.arraycopy(event.values, 0, magnet, 0, 3);
				String magnetLine = "M" + LOGSEPARATOR + 
						tsString + LOGSEPARATOR + 
						magnet[0] + LOGSEPARATOR + 
						magnet[1] + LOGSEPARATOR + 
						magnet[2] + LOGSEPARATOR +
						event.accuracy + "\n";
				if (mAccelerometer != null && mMagnetometer != null) {
					incl = null;
					boolean success = SensorManager.getRotationMatrix(rot, incl, gravity, magnet);
					if (success) {
						// TODO Create temporary rotation matrix to optimize speed
						SensorManager.getOrientation(rot, orientations);
						magnetLine += "O" + LOGSEPARATOR +
								tsString + LOGSEPARATOR +
								orientations[0] + LOGSEPARATOR +			// Azimuth
								orientations[1] + LOGSEPARATOR +			// Pitch
								orientations[2] + "\n";						// Roll
						
						// Push OrientationReading
						oriRS.pushReading(new OrientationReading(tsString, orientations));
					}
				}

				// Write them to a file
				writeToFile(magnetLine);
				break;
			}
		}

		/**
		 * @param event
		 */
		public void averageSampleRateComputation(SensorEvent event) {
			if(lastTimestamp == -1)
				sss.addValue(SAMPLERATE);
			else
				sss.addValue(1 / ((event.timestamp-lastTimestamp)/1000000000D));

			lastTimestamp = event.timestamp;
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
		
		public float[] getLatestOri() {
			return orientations;
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
			// Attach the Butterworth accelBWF to the RawReadingSource
			accelRS.plugFilterIntoInput(accelBWF);

			// Attach StepAnalyser to accelBWF
			accelBWF.plugAnalyserIntoInput(stepA);

			// Attach an UnboundedOrientationFilter and plug a
			// MovingAverageFilter to the output end of it
			oriRS.addUnboundedOrientationFilter(SAMPLERATE);
			oriMAF.plugFilterIntoInput(oriRS.getFilters().get(0));
			
			// Attach the StepAnalyser and the Orientation
			// MovingAverageFilter to the PositioningAnalyser
			stepA.attachToAnalyser(positioningA);
			oriMAF.plugAnalyserIntoInput(positioningA);
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
			float[] ori = mAccelGPSListener.getLatestOri();
			Location loc = mAccelGPSListener.getLoc();

			// Update Accelerometer Views
			((TextView) findViewById(R.id.XValue)).setText(Float.valueOf(accel[0]).toString());
			((TextView) findViewById(R.id.YValue)).setText(Float.valueOf(accel[1]).toString());
			((TextView) findViewById(R.id.ZValue)).setText(Float.valueOf(accel[2]).toString());
			
			// Update Location Views
			((TextView) findViewById(R.id.LatValue)).setText(Double.valueOf(loc.getLatitude()).toString());
			((TextView) findViewById(R.id.LongValue)).setText(Double.valueOf(loc.getLongitude()).toString());
			((TextView) findViewById(R.id.SpeedValue)).setText(Double.valueOf(loc.getSpeed()).toString());

			// Update Orientation Views
			((TextView) findViewById(R.id.XOriValue)).setText(Float.valueOf(ori[0]).toString());
			((TextView) findViewById(R.id.YOriValue)).setText(Float.valueOf(ori[1]).toString());
			((TextView) findViewById(R.id.ZOriValue)).setText(Float.valueOf(ori[2]).toString());
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
		// Attach all the accelBWF and analyser objects acoordingly
		mAccelGPSListener.attachFiltersAndAnalysers();

		// Get the stored samples and insert them into the model
		double[][] storedSamples = agsds.getAllSegmentDataSamples();
		mAccelGPSListener.positioningA.restoreDataSamples(storedSamples);

		// Set the SampleRunnable object to update the data model
		mAccelGPSListener.positioningA.setSensorReadingUpdater(new SensorReadingRunnable() {
			@Override
			public void run() {
				// Write the new coordinates to a file
				String line = "P" + LOGSEPARATOR + reading.getTimestampString();
				for (double d : reading.getReading())
					line +=	LOGSEPARATOR + d;
				line += "\n";
					
				writeToFile(line);
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

		// Get average sample rate value
		double avgSampleRate = getIntent().getExtras().
				getDouble(AccelerometerDirectionApplication.ratePrefName);

		// Define both the location and accelerometer listeners
		mAccelGPSListener = new AccelGPSListener((int) Math.round(avgSampleRate));

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

		// Write pause message to file
		long timestamp = new Date().getTime();
		writeToFile("I" + LOGSEPARATOR + timestamp + LOGSEPARATOR + 
				getString(R.string.activity_paused_log));

		// Save the sample rate to the preferences
		float castSampleRate = (float) mAccelGPSListener.sss.getGeometricMean();
		getSharedPreferences(AccelerometerDirectionApplication.COLLECTION_PREFERENCES, MODE_PRIVATE).
			edit().putFloat(AccelerometerDirectionApplication.ratePrefName, castSampleRate).commit();

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

			// Schedule a register reset
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
