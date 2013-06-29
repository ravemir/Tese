package pt.utl.ist.thesis.acceldir;

import pt.utl.ist.thesis.acceldir.sql.AutoGaitSegmentDataSource;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class AutoGaitDBActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_gait_db);
		
		// Open the database
		AutoGaitSegmentDataSource agsds = new AutoGaitSegmentDataSource(this);
		agsds.open();
		
		// Get the data from the database and fill the entries
		double[][] dataSamples = agsds.getAllSegmentDataSamples();
		String[] entries = new String[dataSamples.length];
		for (int i = 0; i < dataSamples.length; i++)
			entries[i] = dataSamples[i][0] + ", " + dataSamples[i][1];
		
		// Populate the ListView
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, entries);
		setListAdapter(adapter);
		
		agsds.close();
	}

}
