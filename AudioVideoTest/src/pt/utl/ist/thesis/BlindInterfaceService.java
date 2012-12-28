package pt.utl.ist.thesis;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BlindInterfaceService extends Service {
	
	// Direction state variables
	int left = 0;
	int right = 0;
	int direction = -100;
	
	// Binder object to this service
	private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
    	BlindInterfaceService getService(){
    		return BlindInterfaceService.this;
    	}
    }
    
    public void changeDirection(int newDir){
    	direction = newDir;
    }
    
    // The Sound Thread object
    private SoundThread sThread;
	
	public BlindInterfaceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
    	return mBinder;
    }
    
    @Override
    public void onCreate(){
    	// Create the SoundThread
    	sThread = new SoundThread(1);
    	
    	// Notify the user that the service has started
    	Toast.makeText(getApplicationContext(), "BlindInterface Service started", Toast.LENGTH_SHORT).show();
    	
    	// Start the thread
    	sThread.start();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
    	Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // Return sticky to assure the service is ran until explicitly stopped
        return START_STICKY;
    }
    
    @Override
    public void onDestroy(){
    	try {
        	// Stop the initiated SoundThread and wait for its termination
    		sThread.setRun(false);
			sThread.join();
			
	    	// Notify the user that the service has stopped
	    	Toast.makeText(getApplicationContext(), "BlindInterface Service stopped", Toast.LENGTH_SHORT).show();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public class SoundThread extends Thread {
		public volatile Boolean shouldRun = true;

		// SoundPool related variables 
		private SoundPool sPool;
		private int soundChannel;

		/**
		 * @param stereoSoundTestActivity
		 */
		SoundThread(Integer numStreams) {
			// Initialize the SoundPool, loading the respective sounds
			sPool = new SoundPool(numStreams, AudioManager.STREAM_MUSIC, 0);
			soundChannel = sPool.load(getBaseContext(), R.raw.fivehundred_hz, 1);
			
			// TODO Make another initialization of the same sample
			//		to the sound pool, in order to be played through 
			//		transitions. Either that, or find another for the
			//		sound not to cut between successive plays.
		}
		
		public void run() {
			// While scheduled to run...
			while(shouldRun){
				// Calculate left and right volume
				int left=0, right=0;
				if(direction >= 50){
					right = 2*(100 - direction);
				} else if(direction >= -50){
					left = 100 - (direction + 50);
					right = direction + 50;
				} else if(direction >= -100){
					left = 2*(100 + direction);
				}
				
				// Play sound
				@SuppressWarnings("unused") // FIXME This was used before. Why isn't it now?
				int streamID = sPool.play(soundChannel, (float) (left*0.01), (float) (right*0.01), 0, 0, 1);

				// Hold execution for some time
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// TODO Write output back to the view
			}
		}

		/**
		 * Sets the run state for this thread.
		 * 
		 * @param s The desired run state
		 */
		public void setRun(boolean s){
			shouldRun = s;
		}
	}
}
