package pt.utl.ist.util;

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
}
