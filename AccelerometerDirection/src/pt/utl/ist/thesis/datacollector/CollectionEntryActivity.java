package pt.utl.ist.thesis.datacollector;

import java.io.File;
import java.util.ArrayList;

import pt.utl.ist.thesis.datacollector.exception.ExternalStorageUnavailableException;
import pt.utl.ist.thesis.datacollector.exception.ExternalStorageWriteProtectedException;
import pt.utl.ist.thesis.datacollector.util.AndroidUtils;
import pt.utl.ist.thesis.datacollector.util.FileUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class CollectionEntryActivity extends Activity {

	protected static final String COLLECTION_PREFERENCES = "collection_preferences";

	private class GPSStatusChecker implements GpsStatus.Listener, LocationListener {

		private Boolean isGPSFix = false;
		private Location mLastLocation;
		
		public Boolean getIsGPSFix() {
			return isGPSFix;
		}
		
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				if (mLastLocation != null)
					isGPSFix = (SystemClock.elapsedRealtime() - mLastLocation.getTime()) < 3000;
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				// Do something.
				isGPSFix = true;
				break;
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			mLastLocation = location;
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}

	// External storage attributes
	private boolean mExternalStorageAvailable;
	private boolean mExternalStorageWriteable;
	private String logsFolder;
	private LocationManager lm;
	private GPSStatusChecker gpsc;
	private String archiveFolder;
	private Boolean isGPSFixDisabled;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection_entry);
		
		// Set scrollbar movement method for the TextView
		((TextView)findViewById(R.id.instructionsValue))
			.setMovementMethod(new ScrollingMovementMethod());

		// Fill the log directory attribute
		String sdcardPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		logsFolder = sdcardPath + File.separator + "AccelGPS" + 
				File.separator + "logs" + File.separator;
		archiveFolder = logsFolder + "archive" + File.separator;

		// Create directories if necessary
		File a = new File(archiveFolder);
		if(a.mkdirs()) {
			String message = getString(R.string.created_application_folder_message) + archiveFolder + ").";
			AndroidUtils.displayToast(getApplicationContext(), message);
		}

		// Fill the storage state details
		updateStorageState();
		
		// Retrieve preferences
		isGPSFixDisabled = !getSharedPreferences(COLLECTION_PREFERENCES, MODE_PRIVATE).
				getBoolean(getString(R.string.disable_gps_fix_preference), false);

		// Create the GPS status listener
		gpsc = new GPSStatusChecker();
	}

	/**
	 * Attaches the Listener classes responsible for determining
	 * the GPS location fix status.
	 */
	private void attachFixListeners() {
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
//		gpsc = new GPSStatusChecker();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsc);
		lm.addGpsStatusListener(gpsc);
	}

	
	/**
	 * Detaches the Listener classes responsible for determining
	 * the GPS location fix status.
	 */
	private void detachFixListeners(){
		lm.removeGpsStatusListener(gpsc);
		lm.removeUpdates(gpsc);
	}

	// Button-handling related methods
	/**
	 * This is the called function when the toggle button is pressed.
	 * 
	 * @param v The view whose interaction triggered this call (in this case, the button).
	 */
	public void toggleOnClick(View v) {
		// If we have a GPS Fix...
		Boolean isGPSFix = gpsc.getIsGPSFix();
		
		// ...or the GPS fix requirement is disabled
		if(isGPSFix || isGPSFixDisabled){
			// Start the collection activity
			startCollectionActivity();
		} else {
			// Display an error
			AndroidUtils.displayToast(getApplicationContext(),
					getString(R.string.no_fix_message));
		}
	}

	/**
	 * 
	 */
	private void startCollectionActivity() {
		try {
			// Check for external storage availability
			checkExternalStore();

			Intent i = new Intent(CollectionEntryActivity.this, CollectionActivity.class);
			i.putExtra("logFolder", logsFolder);
			startActivity(i);
		} catch (ExternalStorageUnavailableException esue) {
			AndroidUtils.displayToast(getApplicationContext(),
					getString(R.string.external_storage_unavailable_message));
			esue.printStackTrace();
		} catch (ExternalStorageWriteProtectedException eswpe) {
			AndroidUtils.displayToast(getApplicationContext(),
					getString(R.string.external_storage_unwritable_message));
			eswpe.printStackTrace();
		}
	}

	public void prepareEmail(View v){
		// Get the log folder's file URI's
		ArrayList<Uri> fileUri = new ArrayList<Uri>();
		File folder = new File(logsFolder);
		for(File f : folder.listFiles()){
			if(f.isFile())
				fileUri.add(Uri.fromFile(f));
		}

		// Call the 'email' method
		String	emailTo = getString(R.string.email_to_text),
				subject = getString(R.string.email_subject_text),
				emailText = getString(R.string.email_body_text);
		email(getApplicationContext(), emailTo, subject, emailText, fileUri);
	}

	/**
	 * Launch e-mail intent, including multiple attachments. Copied
	 * from "http://stackoverflow.com/questions/2264622/android
	 * -multiple-email-attachments-using-intent".
	 * 
	 * @param context The context from which this request is being sent.
	 * @param emailTo Intended "To:" field for the email.
	 * @param subject Intended "Subject:" field for the email.
	 * @param emailText The body to be sent in the email.
	 * @param fileURIs File paths for each intended attachment.
	 */
	public static void email(Context context, String emailTo, String subject,
			String emailText, ArrayList<Uri> fileURIs) {
		// For multiple attachments, we need the "send multiple" intent
		final Intent emailIntent = 
				new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);

		// Package the URI list and open an activity chooser
		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileURIs);
		context.startActivity(Intent.createChooser(emailIntent, "Send mail...")
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		
		// TODO Read if e-mail was successful, and archive the logs accordingly
	}

	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_collection, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		File dir = new File(logsFolder);
		switch (item.getItemId()) {
			case R.id.menu_toggle_gps_fix:
		    	// Toggle the requirement of a GPS fix to start a capture
				isGPSFixDisabled ^= true;
				getSharedPreferences(COLLECTION_PREFERENCES, MODE_PRIVATE).edit().
					putBoolean(getString(R.string.disable_gps_fix_preference), isGPSFixDisabled).commit();
		    	AndroidUtils.displayToast(getApplicationContext(),
		    			getString(R.string.toggle_gps_fix_requirement_message) + 
		    			(isGPSFixDisabled ? "disabled" : "enabled"));
		    	break;
			case R.id.menu_calibrate_sensors:
				// Launch CalibrationActivity
				Intent i = new Intent(CollectionEntryActivity.this, CalibrationActivity.class);
				startActivity(i);
				break;
		    case R.id.menu_clear_logs:
		    	// Delete all the files in the folder
		    	FileUtils.deleteFilesFromDir(dir);
		    	AndroidUtils.displayToast(getApplicationContext(),
		    			getString(R.string.logs_cleared_message));
		    	break;
		    case R.id.menu_archive_logs:
		    	// Archive all files in the folder
		    	FileUtils.moveAllFilesToDir(dir, archiveFolder);
		    	AndroidUtils.displayToast(getApplicationContext(),
		    			getString(R.string.logs_archived_message) + archiveFolder + "'");
		    	break;
		    default: return super.onOptionsItemSelected(item);
		}
		return true;
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
			// Something is wrong, what we know is we can neither read nor write 
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Re-attach the Fix Listeners
		attachFixListeners();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Detach Fix Listeners before pausing
		detachFixListeners();
	}
}
