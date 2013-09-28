package pt.utl.ist.thesis.acceldir;

import java.util.List;

import pt.utl.ist.thesis.acceldir.sql.AutoGaitSegmentData;
import pt.utl.ist.thesis.acceldir.sql.AutoGaitSegmentDataSource;
import pt.utl.ist.thesis.sensordir.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DBFillingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dbfilling);
		
		//  Fill database with mock data
		AutoGaitSegmentDataSource agsds = new AutoGaitSegmentDataSource(this);
		agsds.open();
		double stepFrequency, stepLength;
		for(double i = 0 ; i < 60 ; i++){
			stepFrequency = 1 / ((i % 10) + 1);
			stepLength = ((i % 10)+3)*0.1;
			agsds.createSegmentData(stepFrequency, stepLength);
		}
		
		// To test deletions, delete all multiple of 5 entries
		List<AutoGaitSegmentData> data = agsds.getAllSegmentData();
		for (int i = 0; i < data.size(); i++)
			if(i%5 == 0) agsds.deleteSegmentData(data.get(i));
		
		agsds.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dbfilling, menu);
		return true;
	}

}
