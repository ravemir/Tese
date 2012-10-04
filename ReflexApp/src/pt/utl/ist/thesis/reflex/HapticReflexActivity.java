package pt.utl.ist.thesis.reflex;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class HapticReflexActivity extends Activity {
    /** Called when the activity is first created. */
    TextView chrono;
    int elapsedTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        chrono = (TextView) findViewById(R.id.chronometer);
    }

    private Handler mHandler = new Handler();
    public void actions() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 1); //1 is the number of milliseconds you want the text to be updated
    }
    Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
            elapsedTime++;
            chrono.setText(sdf.format(elapsedTime)); // formatTime is just for make it readable in HH:MM:SS:MS, but I assume you already have this
            mHandler.postDelayed(this, 1);
        }
    };

}