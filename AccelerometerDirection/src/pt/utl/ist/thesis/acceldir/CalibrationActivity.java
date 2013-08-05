package pt.utl.ist.thesis.acceldir;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import pt.utl.ist.thesis.acceldir.R;
import pt.utl.ist.thesis.acceldir.util.AndroidUtils;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.MovingAverageFilter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalibrationActivity extends Activity {

	private Sensor accel;
	private SensorManager sm;
	private int calibrationRound;
	private double[][] readingMatrix;
	private CalibrationEventListener calibrationEventListener;
	private WakeLock mWakeLock;
	private final String[] instructions = {"Press start, and lay the cellphone with the screen facing down for 5 seconds",
			"Press start again, and place the cellphone in an upright position for 5 seconds.",
			"Press start one final time, and lay the cellphone on its left-side for 5 seconds.",
			"Calibration done. Press back to exit this prompt."};

	private final class CalibrationEventListener implements SensorEventListener {

		private int readingCount = 0;
		private final int readingLimit;
		private RawReadingSource rrs;
		private MovingAverageFilter maf;
		private Runnable calibrationUIUpdates = new Runnable() {
			@Override
			public void run() {
				// If there are rounds missing
				if(calibrationRound <= 2){
					// Re-enable button 
					((Button) findViewById(R.id.proceedButton)).setVisibility(View.VISIBLE);
				}
				
				// Set new instructions text
				((TextView) findViewById(R.id.calibrationView))
					.setText(instructions[calibrationRound]);
			}
		};

		public CalibrationEventListener(int averageSize, int limit) {
			super();
			// Set the the reading limit to the specified target
			readingLimit = limit;
			
			// Create a MovingAverageFilter attached to a RawReadingSource
			rrs = new RawReadingSource(limit);
			maf = new MovingAverageFilter(averageSize);
			rrs.plugFilterIntoOutput(maf);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// Compute the timestamp in nanos first
			long newtimestamp = AndroidUtils.computeJavaTimeStamp(event.timestamp);
			
			// ...then generate a formatted millis string
			String tsString = AndroidUtils.printNanosToMilis(newtimestamp);
			
			// Add value to RawReadingSource
			AccelReading accReading = new AccelReading(tsString,
					event.values);
			rrs.pushReading(accReading);

			// If the reading limit has been reached
			if(readingCount == readingLimit){
				// Collect average value and place it into local var
				CalibrationActivity.this.readingMatrix[calibrationRound] = 
						maf.getBuffer().getCurrentReading().getReading().clone();

				// Unregister event listener
				((SensorManager) getSystemService(SENSOR_SERVICE))
					.unregisterListener(calibrationEventListener);

				// If calibration round was last, then... 
				if(calibrationRound == 2) {
					// Compute calibrationMatrix (expected*reading^-1)
					DecompositionSolver solver = new LUDecompositionImpl(
							new Array2DRowRealMatrix(readingMatrix)).getSolver();
					RealMatrix calibrationMatrix = solver.solve(new Array2DRowRealMatrix(new double[][]
							{{0,0,-SensorManager.GRAVITY_EARTH},
							{0,SensorManager.GRAVITY_EARTH,0},
							{SensorManager.GRAVITY_EARTH,0,0}}));
					
					
					// Save it in preferences
					Editor editor = getSharedPreferences(AccelerometerDirectionApplication.COLLECTION_PREFERENCES,
							MODE_PRIVATE).edit();
					for (int i = 0; i < calibrationMatrix.getColumnDimension(); i++) {
						for (int j = 0; j < calibrationMatrix.getRowDimension(); j++) {
							editor.putString("calibrationM(" + i + ", " + j + ")",
									Double.valueOf(calibrationMatrix.getEntry(i,j)).toString());
						}
					}
					editor.commit();					
				}
				
				// Increment round number
				calibrationRound++;
				
				// Update UI entries
				runOnUiThread(calibrationUIUpdates);
			}
			readingCount++;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);

		// Sensor variables
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// State variables
		calibrationRound = 0;
		readingMatrix = new double[3][3];

		// Create screen-dim wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				getString(R.string.wake_lock_log));
		
		// Set the initial instructions text
		((TextView) findViewById(R.id.calibrationView))
			.setText(instructions[calibrationRound]);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_calibration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			default: return super.onOptionsItemSelected(item);
		}
	}

	public void calibration(View v){
		// Register the CalibrationEventListener
		calibrationEventListener = new CalibrationEventListener(250,250);
		sm.registerListener(calibrationEventListener, accel, SensorManager.SENSOR_DELAY_FASTEST);

		// Make button unclickable
		((Button) v).setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Acquire partial wake-lock
		mWakeLock.acquire();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the accelerometer listener
		sm.unregisterListener(calibrationEventListener);

		// Release the wake-lock
		mWakeLock.release();
	}	
}
