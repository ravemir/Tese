package pt.utl.ist.thesis.datacollector.util;

import android.app.Activity;
import android.os.Handler;
/**
 * A class used to perform updates on an Android UI,
 * specified inside a runnable object. An update interval
 * may be specified (otherwise, the class will update the 
 * UI every 1,5 seconds).
 * 
 * @author Carlos Simões
 */
public class UIUpdater {
		// Time interval for the UI updates
		private int UPDATE_INTERVAL = 1500;
		
		// Updater classes
		private Handler mHandler = new Handler();
		private Activity mActivity;
		private Runnable mUIUpdater;
		private Runnable mStatusChecker;
		
		/**
		 * Creates an UIUpdater object, that can be used to
		 * perform ui updates on the specified activity on
		 * a specified time interval.
		 * 
		 * @param activity	The activity on which the updates will 
		 * 					be performed
		 * @param uiUpdater A runnable containing the update routine.
		 */
		public UIUpdater(Activity activity, Runnable uiUpdater){
			mActivity = activity;
			mUIUpdater = uiUpdater;
			mStatusChecker = new Runnable() {
				@Override
				public void run() {
					// Run the passed runnable
					mActivity.runOnUiThread(mUIUpdater);
										
					// Re-run it after the update interval
					mHandler.postDelayed(this, UPDATE_INTERVAL);
				}
			};			
		}
		
		/**
		 * The same as the default constructor, but specifying the
		 * intended update interval.
		 * @param activity	The activity on which the updates will 
		 * 					be performed.
		 * @param uiUpdater A runnable containing the update routine.
		 * @param interval	The interval over which the routine
		 * 					should run (milliseconds).
		 */
		public UIUpdater(Activity activity, Runnable uiUpdater, int interval){
			this(activity, uiUpdater);
			UPDATE_INTERVAL = interval;
		}
		
		/**
		 * Starts the periodical update routine (mStatusChecker 
		 * adds the callback to the handler).
		 */
		public void startUpdates(){
			mStatusChecker.run();
		}
		
		/**
		 * Stops the periodical update routine from running,
		 * by removing the callback.
		 */
		public void stopUpdates(){
			mHandler.removeCallbacks(mStatusChecker);
		}
}
