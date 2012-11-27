package pt.utl.ist.thesis;

import java.util.ArrayList;

import com.sun.script.util.BindingsImpl;
import com.sun.xml.internal.org.jvnet.staxex.NamespaceContextEx.Binding;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class StereoSoundTestActivity extends Activity {

	int left = 0;
	int right = 0;
	int direction = -100;
	
//	private SoundThread soundThread = new SoundThread(this);
	
	// Service-related attributes and methods
	private BlindInterfaceService mBoundService;
	private Boolean mIsBound = false;
	private ServiceConnection mBlindServiceConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Fill the service attribute, so we can access it in the future
			mBoundService = ((BlindInterfaceService.LocalBinder) service).getService();
		}
		
		public void onServiceDisconnected(ComponentName name) {
			// Erase the binding of the servers
			mBoundService = null;
		}
	};
	
	protected void bindBlindService(){
		// Do the binding and change the state
		mIsBound = bindService(new Intent(StereoSoundTestActivity.this, BlindInterfaceService.class),
				mBlindServiceConnection, Context.BIND_AUTO_CREATE);
		
		// Log the state change
		Log.i("INFO", "Service is " + (mIsBound ? "bound" : "not bound"));
	}
	
	protected void unbindBlindService() {
		if(mIsBound){
			// Detach existing connection
			unbindService(mBlindServiceConnection);
			mIsBound = false;
		}
	}

	// UI Elements
	ArrayAdapter<String> adapter;
	ArrayList<String> consoleList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stereo_sound_test);
		//        getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get the TextView, connect the adapter and make it auto-scrollable
		ListView consoleView = (ListView) findViewById(R.id.stereoConsole);
		adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, consoleList);
		consoleView.setAdapter(adapter);
		consoleView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		consoleView.setStackFromBottom(true);
		
		// Bind the service and get its interface
		bindBlindService();
		
		// Start the sound controlling thread
		Thread soundControlThread = new Thread(new Runnable() {
			public void run() {
				int dir = -100;
				
				// Wait until the Service is ACTUALLY bound
				while(mIsBound&&mBoundService==null);
				
				while(mIsBound){
					// Change direction
					mBoundService.changeDirection(dir);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// Update direction, keeping it within the limits
					dir += 1;
					if(dir >= 100)
						dir = -100;
				}
			}
		});
		soundControlThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_stereo_sound_test, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//        switch (item.getItemId()) {
		//            case android.R.id.home:
		//                NavUtils.navigateUpFromSameTask(this);
		//                return true;
		//        }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume(){
//		soundThread.start();
		super.onResume();
	}
	
	@Override
	public void onPause(){
//		soundThread.setRun(false);
		super.onPause();
	}
	
	@Override
	public void onStop(){
//		soundThread.setRun(false);
		super.onStop();
	}
	
	@Override
	public void onDestroy(){
		unbindBlindService();
		super.onDestroy();
	}
}
