package pt.utl.ist.thesis.acceldir;

import android.app.Application;

public class AccelerometerDirectionApplication extends Application {

	// Application preference fields
	protected static final String COLLECTION_PREFERENCES = "collection_preferences";
	protected static String fixPrefName;
	protected static String ratePrefName;

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Load all string constants
		fixPrefName = getString(R.string.disable_gps_fix_preference);
		ratePrefName = getString(R.string.sample_rate_preference);
	}

	
}
