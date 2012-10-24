package pt.utl.ist.thesis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import pt.utl.ist.thesis.R;

import android.app.Activity;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AudioVideoTestActivity extends Activity {

    LocationManager lmng;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiovideo);


        lmng = (LocationManager) getSystemService(LOCATION_SERVICE);
        lmng.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() { // This request must be here, or GPS will refuse to load
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
            }
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
            }
        });
        lmng.addNmeaListener(new NmeaListener() {
            public void onNmeaReceived(long timestamp, String nmea) {
                TextView tv = ((TextView) findViewById(R.id.avTestTextView));

                // Write the received NMEA string
                tv.append(nmea + "\n");
            }
        });
    }

    // TODO: Adicionar NMEA listener, ou simplesmente manipular dados GPS (check)
    // TODO: Testar o uso de Sockets TCP (partial)
    // TODO: Manipular bússola

    public void addNewline(View v) {
        writeLine(getResources().getText(R.string.newlinetext).toString());
    }

    public void attachListener(View v) {

        // TODO Create and implement NMEAListener method 'onNMEAReceived()'
        // TODO Add NMEA Listener and make it write to the text view
        lmng.addNmeaListener(new NmeaListener() {
            public void onNmeaReceived(long timestamp, String nmea) {
                TextView tv = ((TextView) findViewById(R.id.avTestTextView));

                // Write the received NMEA string
                tv.append(nmea + "\n");
                Log.e("NMEA",""+timestamp+" "+nmea);
            }
        });
    }

    public void playSound(View v) {
        // Create player from mp3 resource (note that we don't call it 'submarine.mp3')
        MediaPlayer mp = MediaPlayer.create(AudioVideoTestActivity.this, R.raw.submarine);

        // Start playback, and set it to release the player at the end
        mp.start();
        mp.setOnCompletionListener(new OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                // Release the player
                mp.release();
            }

        });
    }

    private void writeLine(String line) {
        // Obtém vista para escrever
        TextView tv = ((TextView) findViewById(R.id.avTestTextView));

        // Escreve texto apropriado
        tv.append(line + "\n");
    }

    public void ping(View v) {
        // Get the IP resource
        String destIP = getResources().getText(R.string.hostIP).toString();

        // Get Address from IP
        InetAddress in = null;
        try {
            in = InetAddress.getByName(destIP);
            // Ping the server
            if(in.isReachable(5000)){
                // Target Replied
                writeLine("OK");
            }
            else{
                // Reply timeout
                writeLine("Timeout");
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}