package pt.ist.utl.thesis;

import java.io.File;
import java.util.ArrayList;

import pt.utl.ist.thesis.datacollector.CollectionActivity;
import pt.utl.ist.thesis.datacollector.R;
import pt.utl.ist.thesis.exception.ExternalStorageUnavailableException;
import pt.utl.ist.thesis.exception.ExternalStorageWriteProtectedException;
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
import android.view.View;

public class CollectionEntryActivity extends Activity {

	private class GPSStatusChecker implements GpsStatus.Listener, LocationListener{

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
	private String logFolder;
	private LocationManager lm;
	private GPSStatusChecker gpsc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection_entry);

		// Fill the log directory attribute
		String sdcardPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		logFolder = sdcardPath + File.separator + "AccelGPS" + 
				File.separator + "logs" + File.separator;

		// Create directories if necessary
		File f = new File(logFolder);
		if(f.mkdirs()) {
			String message = getString(R.string.created_application_folder_message) + logFolder + ").";
			CollectionActivity.displayToast(getApplicationContext(), message);
		}

		// Fill the storage state details
		updateStorageState();

		// Add a GPS status listener
		attachFixListeners();
	}

	/**
	 * Attaches the Listener classes responsible for determining
	 * the GPS location fix status.
	 */
	private void attachFixListeners() {
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		gpsc = new GPSStatusChecker();
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

	/**
	 * This is the called function when the toggle button is pressed.
	 * 
	 * @param v The view whose interaction triggered this call (in this case, the button).
	 */
	public void toggleOnClick(View v) {
		// If we have a GPS Fix..
		Boolean isGPSFix = gpsc.getIsGPSFix();
		if(isGPSFix){
			// Start the collection activity
			startCollectionActivity();
		} else {
			// Display an error
			CollectionActivity.displayToast(getApplicationContext(),
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
			i.putExtra("logFolder", logFolder);
			startActivity(i);
		} catch (ExternalStorageUnavailableException esue) {
			CollectionActivity.displayToast(getApplicationContext(),
					getString(R.string.external_storage_unavailable_message));
			esue.printStackTrace();
		} catch (ExternalStorageWriteProtectedException eswpe) {
			CollectionActivity.displayToast(getApplicationContext(),
					getString(R.string.external_storage_unwritable_message));
			eswpe.printStackTrace();
		}
	}

	public void prepareEmail(View v){
		// Get the log folder's file URI's
		ArrayList<Uri> fileUri = new ArrayList<Uri>();
		File folder = new File(logFolder);
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