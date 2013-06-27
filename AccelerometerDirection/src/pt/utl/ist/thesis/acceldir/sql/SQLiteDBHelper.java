package pt.utl.ist.thesis.acceldir.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDBHelper extends SQLiteOpenHelper {

	public static final String TABLE_AUTOGAIT_DATA = "autogait_segment_data";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_STEP_FREQUENCY = "step_frequency";
	public static final String COLUMN_STEP_LENGTH = "step_length";

	private static final String DATABASE_NAME = "autogait_segments.db";
	private static final int DATABASE_VERSION = 1;

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

}
