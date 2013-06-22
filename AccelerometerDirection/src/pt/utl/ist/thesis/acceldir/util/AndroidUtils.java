package pt.utl.ist.thesis.acceldir.util;

import java.util.Date;

import android.content.Context;
import android.widget.Toast;

public class AndroidUtils {

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

	// Assorted functions
	/**
	 * Prints out the millisecond form of the provided 
	 * nanosecond timestamp.
	 * 
	 * @param nanosTimestamp The timestamp, in nanosecond units
	 * @return A String containing the millisecond form of the timestamp
	 */
	public static String printNanosToMilis(long nanosTimestamp) {
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
	 * @param eventTimestamp
	 * @return
	 */
	public static long computeJavaTimeStamp(long eventTimestamp) {
		long javaTime = new Date().getTime();
		long nanoTime = System.nanoTime();
		long newtimestamp = javaTime * 1000000 + 						
				(eventTimestamp - nanoTime);
		return newtimestamp;
	}
}
