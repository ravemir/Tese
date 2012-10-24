package pt.utl.ist.thesis;

import pt.utl.ist.thesis.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ThesisEntryActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Obtain the 'ListView' and add the 'OnItemClick' listener
        ListView lv = (ListView) findViewById(R.id.testListView);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                // Open new view, depending on selection
                Intent intent = new Intent();
                switch (position) { // TODO Refactor into prettier menu selection behaviour
                case 0:             // Button press test app, with sound and networking
                    intent.setClass(ThesisEntryActivity.this, AudioVideoTestActivity.class);break;
                case 1:
                    intent.setClass(ThesisEntryActivity.this, CompassTestActivity.class);break;
                case 2: 
                    // Open the accelerometer test activity
                    intent.setClass(ThesisEntryActivity.this, AccelTestActivity.class);break;
                case 3: 
                    // Open the Reaction Time activity (...)
                    intent.setClass(ThesisEntryActivity.this, AccelGPSRecActivity.class);break;
                default:
                    return;
                }
                startActivity(intent);
            }
        });
    }

}
