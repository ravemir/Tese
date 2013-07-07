package pt.utl.ist.thesis.acceldir.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AutoGaitSegmentDataSource {
	// Database fields
	private SQLiteDatabase database;
	private SQLiteDBHelper dbHelper;
	private String[] allColumns = { SQLiteDBHelper.COLUMN_ID,
			SQLiteDBHelper.COLUMN_STEP_FREQUENCY, 
			SQLiteDBHelper.COLUMN_STEP_LENGTH };

	public AutoGaitSegmentDataSource(Context context) {
		dbHelper = new SQLiteDBHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public AutoGaitSegmentData createSegmentData(Double stepFrequency, Double stepLength) {
		// Create the segment data entry
		ContentValues values = new ContentValues();
		values.put(SQLiteDBHelper.COLUMN_STEP_FREQUENCY, stepFrequency);
		values.put(SQLiteDBHelper.COLUMN_STEP_LENGTH, stepLength);
		long insertId = database.insert(SQLiteDBHelper.TABLE_AUTOGAIT_DATA, null,
				values);
		
		// Move the cursor to the created entry
		Cursor cursor = database.query(SQLiteDBHelper.TABLE_AUTOGAIT_DATA,
				allColumns, SQLiteDBHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		
		// Return the created segment data object
		AutoGaitSegmentData newSegmentData = cursorToAutoGaitSegmentData(cursor);
		cursor.close();
		return newSegmentData;
	}

	public void deleteSegmentData(AutoGaitSegmentData segmentDataEntry) {
		// Get this entry's ID and use it to delete it from the table
		long id = segmentDataEntry.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(SQLiteDBHelper.TABLE_AUTOGAIT_DATA, SQLiteDBHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	/**
	 * Deletes all the data inside the database.
	 */
	public void deleteAllSegmentData(){
		for(AutoGaitSegmentData d : getAllSegmentData())
			deleteSegmentData(d);
	}

	public List<AutoGaitSegmentData> getAllSegmentData() {
		// Create the list to hold the data
		List<AutoGaitSegmentData> segmentData = new ArrayList<AutoGaitSegmentData>();

		// Move the cursor to the table, and target the first entry
		Cursor cursor = database.query(SQLiteDBHelper.TABLE_AUTOGAIT_DATA,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		
		// Run through the table's entries and insert them
		while (!cursor.isAfterLast()) {
			AutoGaitSegmentData segmentDataEntry = cursorToAutoGaitSegmentData(cursor);
			segmentData.add(segmentDataEntry);
			cursor.moveToNext();
		}
		
		// Make sure to close the cursor
		cursor.close();
		return segmentData;
	}

	/**
	 * Returns all the data in the form of an array of
	 * double samples.
	 * 
	 * @return A 2D double array containing the samples.
	 */
	public double[][] getAllSegmentDataSamples(){
		// Get all the segment data
		List<AutoGaitSegmentData> allData = getAllSegmentData();
		
		// If there is none, return an empty array
		if(allData.size()==0) return new double[][]{};
		
		// Run through every item and insert its values
		// into the respective sample
		double[][] samples = new double[allData.size()][2];
		for (int i = 0; i < allData.size(); i++) {
			AutoGaitSegmentData item = allData.get(i);
			samples[i] = new double[]{item.getStepFrequency(), 
					item.getStepLength()};
		}
		
		return samples;
	}
	

	private AutoGaitSegmentData cursorToAutoGaitSegmentData(Cursor cursor) {
		// Create the segment data object
		AutoGaitSegmentData segmentDataEntry = new AutoGaitSegmentData();
		
		// Set each attribute value to the cursor's 
		segmentDataEntry.setId(cursor.getLong(0));
		segmentDataEntry.setStepFrequency(cursor.getDouble(1));
		segmentDataEntry.setStepLength(cursor.getDouble(2));
		
		return segmentDataEntry;
	}
	
	/**
	 * Imports a database file into this source's
	 * database.
	 * 
	 * @param dbPath	The path to the DB file.
	 */
	public void importDataBase(String dbPath){
		// Import new database
		try {
			dbHelper.importDatabase(dbPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Re-open the database for business
		open();
	}
	
	/**
	 * Exports the current database to a file in the
	 * specified path.
	 * 
	 * @param path	The path to export the database to.
	 * @param fileName TODO
	 */
	public void exportDataBase(String path, String fileName){
		dbHelper.exportDatabase(path, fileName);
	}
}
