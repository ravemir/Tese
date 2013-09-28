package pt.utl.ist.thesis.acceldir;

import pt.utl.ist.thesis.acceldir.sql.AutoGaitSegmentDataSource;
import pt.utl.ist.thesis.sensordir.R;
import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;
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
		AutoGaitModel agm = new AutoGaitModel(dataSamples);
		String[] entries = new String[dataSamples.length + 1];
		if (dataSamples.length > 0)
			entries[0] = "alpha: " + agm.getAlpha() + ", beta: " + agm.getBeta();
		else
			entries[0] = "alpha: 0, beta: 0";
		for (int i = 0; i < dataSamples.length; i++)
			entries[i+1] = dataSamples[i][0] + ", " + dataSamples[i][1];
		
		// Populate the ListView
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, entries);
		setListAdapter(adapter);
		
		agsds.close();
	}

}
