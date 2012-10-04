package pt.utl.ist.thesis.reflex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ReflexAppEntryActivity extends Activity {
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Obtain the 'ListView' and add the 'OnItemClick' listener
        ListView lv = (ListView) findViewById(R.id.entryListView);
        lv.setOnItemClickListener(new OnItemClickListener() { // TODO View if this method of issuing listeners is still current
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                // Open new view, depending on selection
                Intent intent = new Intent();
                switch (position) { // TODO Refactor into prettier menu selection behaviour
                case 0:             // Haptic Feedback Reflex test
                    intent.setClass(ReflexAppEntryActivity.this, HapticReflexActivity.class); break;
                case 1:             // Open the Network test activity (Text box to fill with IP, list with status, button to "ping")
                    intent.setClass(ReflexAppEntryActivity.this, null); break;
                default:
                    return;
                }
                startActivity(intent);
            } 
        });
    }
    
}