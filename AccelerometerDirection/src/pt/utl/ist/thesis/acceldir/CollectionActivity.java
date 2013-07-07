package pt.utl.ist.thesis.acceldir;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.utl.ist.thesis.acceldir.R;
import pt.utl.ist.thesis.acceldir.util.AndroidUtils;
import pt.utl.ist.thesis.acceldir.util.FileUtils;
import pt.utl.ist.thesis.acceldir.util.UIUpdater;
import pt.utl.ist.util.sensor.reading.AccelReading;
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
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class CollectionActivity extends Activity {

	// Inner-class Listeners
	private final class AccelGPSListener implements LocationListener, SensorEventListener {
		
		// Sensor-related attributes and methods (sized 4 to be compatible with 4x4 matrices)
		private float[] accel = new float[4];
		private float[] gravity = new float[4];
		private float[] magnet = new float[4];
		private float[] orientations = new float[3];
		private float wAccels[] = new float[4];
		
		private static final double KFACTOR = 10.5; // TODO Chosen heuristically. Should be computed?
													// 		Value before which a peak is always discarded.
													//		Gravity magnitude is a good pick.
		private static final double PEAKTHRESHFACTOR = 0.7; // Multiplication factor to lower the step threshold
															// Depends on the variance of intensity of each step
															// i.e. if a step is a lot smaller than the previous,
															// this value should be lower to accommodate it.
		
		private static final int CIRCBUFFSIZE = 50; // FIXME Circular buffer size (should be computed according to
													//		 sampling rate)
		private int accelI = 0;
		private double acum = 0;
		private boolean bufferIsWarm = false;
		private AccelReading[] accelReadings = new AccelReading[CIRCBUFFSIZE];
		private List<AccelReading> peakList = new ArrayList<AccelReading>();
		
		private float rot[] = new float[16];
		private float incl[] = null;

		@Override
		public void onSensorChanged(SensorEvent event) {
			// Write sensor values and the timestamp to the 'accelView'
			float[] values = event.values; // FIXME This should be removed, as there is no reason to use the copied values or event.values
			int accuracy = event.accuracy;

			// Compute the timestamp in nanos first
			long newtimestamp = AndroidUtils.computeJavaTimeStamp(event.timestamp);
			String tsString = AndroidUtils.printNanosToMilis(newtimestamp);

			switch(event.sensor.getType()){
			case Sensor.TYPE_LINEAR_ACCELERATION:
			case Sensor.TYPE_ACCELEROMETER:
				System.arraycopy(event.values, 0, accel, 0, 3);
				accelReadings[accelI] = new AccelReading(tsString, accel);
				String accelLine = "A" + LOGSEPARATOR + 
						tsString + LOGSEPARATOR + 
						values[0] + LOGSEPARATOR + 
						values[1] + LOGSEPARATOR + 
						values[2] + LOGSEPARATOR +
						accuracy + "\n";
				Log.v("AccelGPS", "Received acceleration value.");
				
				// If the buffer has filled once...
				if(bufferIsWarm){
					double forwardSlope = computeFwdSlope(accelI);
					double backwardSlope = computeBwdSlope(accelI);
					
					// Detect peak
					if(forwardSlope > 0 && backwardSlope < 0){
						// Store peak timestamp and value
						int peakIndex = previousIndex(accelI, CIRCBUFFSIZE);
						peakList.add(accelReadings[peakIndex]);
						
						// Acumulate peak value
						acum += accelReadings[peakIndex].getAccelerationNorm();
					}
					
					// TODO Detect "zero" crossings or some other peak validating mechanism
				}
				
				// If buffer has filled...
				if(accelI == CIRCBUFFSIZE - 1){
					// Count steps
					double peakAverage = acum/peakList.size();
					for(AccelReading ard : peakList){
						// Print them
						if(ard.getAccelerationNorm() > peakAverage * PEAKTHRESHFACTOR &&
								ard.getAccelerationNorm() > KFACTOR){
							accelLine += "S" + LOGSEPARATOR + 
									ard.getTimestampString() + LOGSEPARATOR +
									ard.getAcceleration()[0] + LOGSEPARATOR +
									ard.getAcceleration()[1] + LOGSEPARATOR +
									ard.getAcceleration()[2] + LOGSEPARATOR +
									ard.getAccelerationNorm() + "\n";
						}
					}
					
					// Reset acumulator and peak list
					acum = 0;
					peakList.clear();
					bufferIsWarm = true;
				}
				
				// Increment circular buffer index
				accelI = (accelI + 1) % CIRCBUFFSIZE;
				
				// Write them to a file
				writeToFile(accelLine);
				break;
			case Sensor.TYPE_GRAVITY:
				System.arraycopy(event.values, 0, gravity, 0, 3);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				System.arraycopy(event.values, 0, magnet, 0, 3);
				String magnetLine = "M" + LOGSEPARATOR + 
						tsString + LOGSEPARATOR + 
						values[0] + LOGSEPARATOR + 
						values[1] + LOGSEPARATOR + 
						values[2] + LOGSEPARATOR +
						accuracy + "\n";

				if (mAccelerometer != null && mMagnetometer != null) {
					incl = null;
					boolean success = SensorManager.getRotationMatrix(rot, incl, gravity, magnet);
					//SensorManager.remapCoordinateSystem(rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, rot);
					if (success) {
						// TODO Create temporary rotation matrix to optimize speed
						SensorManager.getOrientation(rot, orientations);
						magnetLine += "O" + LOGSEPARATOR +
								tsString + LOGSEPARATOR +
								orientations[0] + LOGSEPARATOR +			// Azimuth
								orientations[1] + LOGSEPARATOR +			// Pitch
								orientations[2] + "\n";						// Roll
						
						// Copy acceleration values into bigger array for multiplication
						android.opengl.Matrix.multiplyMV(wAccels, 0, rot, 0, gravity, 0);
//						android.opengl.Matrix.multiplyMV(wAccels, 0, rot, 0, accel, 0);
						magnetLine += "W" + LOGSEPARATOR +
								tsString + LOGSEPARATOR +
								wAccels[0] + LOGSEPARATOR +
								wAccels[1] + LOGSEPARATOR +
								wAccels[2] + "\n";
					}
				}

				// Write them to a file
				writeToFile(magnetLine);
				break;
			}
		}


		/**
		 * @param index TODO
		 * @param bufferSize TODO
		 * @return
		 */
		private int previousIndex(int index, int bufferSize) {
			return index == 0 ? bufferSize-1 : index-1;
		}

		
		/**
		 * Computes the forward slope on the appropriate
		 * range of acceleration values.
		 * 
		 * @return The slope value.
		 */
		private double computeFwdSlope(int index) {
			int prev = previousIndex(index, CIRCBUFFSIZE);
			return accelReadings[index].getAccelerationNorm() - 
					accelReadings[prev].getAccelerationNorm();
		}
		
		/**
		 * Computes the backward slope on the appropriate
		 * range of acceleration values.
		 * 
		 * @param index Index pointing to the end of the
		 * 				backward slope. 
		 * @return The slope value.
		 */
		private double computeBwdSlope(int index) {
			int prev = previousIndex(index, CIRCBUFFSIZE);
			int beforeLast = previousIndex(prev, CIRCBUFFSIZE);
			return accelReadings[prev].getAccelerationNorm() - 
					accelReadings[beforeLast].getAccelerationNorm();
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
			String line =   "L"+ LOGSEPARATOR + timestamp + LOGSEPARATOR +
					location.getLatitude() + LOGSEPARATOR + 
					location.getLongitude() + LOGSEPARATOR +
					location.getAltitude() + LOGSEPARATOR +
					location.getBearing() + LOGSEPARATOR +
					location.getSpeed() +  LOGSEPARATOR +
					satelliteNo + LOGSEPARATOR +
					location.getAccuracy() + "\n";
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
		String date = FileUtils.getDateForFilename();
		filename = logFolder + date + ".log";
		
		// Initialize and attach the Listener-related activity attributes
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
	private void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// To stop home button from exiting the app, but doesn't work
//		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
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
	
//	/**
//	 * Method placed to disable Home button.
//	 * 
//	 * Note: Taken from http://stackoverflow.com/questions/3898876/how-to-disable-the-home-key
//	 */
//	@Override
//	public void onAttachedToWindow() {
//	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
//	    super.onAttachedToWindow();
//	}
}
