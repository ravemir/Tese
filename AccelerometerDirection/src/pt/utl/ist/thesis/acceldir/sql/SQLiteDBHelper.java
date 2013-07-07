package pt.utl.ist.thesis.acceldir.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import pt.utl.ist.thesis.acceldir.util.FileUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class SQLiteDBHelper extends SQLiteOpenHelper {

	public static final String TABLE_AUTOGAIT_DATA = "autogait_segment_data";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_STEP_FREQUENCY = "step_frequency";
	public static final String COLUMN_STEP_LENGTH = "step_length";

	private static final String DATABASE_NAME = "autogait_segments.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_AUTOGAIT_DATA + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_STEP_FREQUENCY 
			+ " real not null, " + COLUMN_STEP_LENGTH
			+ " real not null);";

	public SQLiteDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLiteDBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTOGAIT_DATA);
		onCreate(db);
	}

	public static String DB_FILEPATH = Environment.getDataDirectory() + File.separator + 
			"data" + File.separator + "pt.utl.ist.thesis.acceldir" + File.separator + 
			"databases" + File.separator + "autogait_segments.db";

	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 * 
	 * Note: Code copied from http://stackoverflow.com/questions/6540906/android-simple-export-import-of-sqlite-database
	 */
	public boolean importDatabase(String dbPath) throws IOException {

	    // Close the SQLiteOpenHelper so it will commit the created empty
	    // database to internal storage.
	    close();
	    File newDb = new File(dbPath);
	    File oldDb = new File(DB_FILEPATH);
	    if (newDb.exists()) {
	        FileUtils.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
	        // Access the copied database so SQLiteHelper will cache it and mark
	        // it as created.
	        getWritableDatabase().close();
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Copies the database file form the application to the
	 * specified folder. 
	 * 
	 * Note: Code based off http://stackoverflow.com/questions/6540906/android-simple-export-import-of-sqlite-database
	 * 
	 * @param folderPath	The path to export the file to.
	 * @param fileName		The name for the file to be exported.
	 */
	public boolean exportDatabase(String folderPath, String fileName) {

		// Create the destination file path
		File dstPath = new File(folderPath);

        if (dstPath.canWrite()) {
        	// Create the source file object
            File currentDB = new File(DB_FILEPATH);
            
			try {
				// Create the destination file, if it doesn't exist
				File dstFile = new File(dstPath, fileName);
	            
				// Create Streams and Channels
				FileInputStream srcIS = new FileInputStream(currentDB);
				FileOutputStream srcOS = new FileOutputStream(dstFile);
				FileChannel src = srcIS.getChannel();
				FileChannel dst = srcOS.getChannel();
				
				// Copy src into dst 
	            dst.transferFrom(src, 0, src.size());
	            
	            // Close everything up
	            src.close();
	            srcIS.close();
	            dst.close();
	            srcOS.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        return dstPath.canWrite();
	}
}
