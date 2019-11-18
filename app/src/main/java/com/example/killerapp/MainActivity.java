
package com.example.killerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SmsHandler;

/**
 * @author Pardeep Kumar
 * 
 */
public class MainActivity extends AppCompatActivity implements SmsHandler.OnSmsEventListener
{
    private EditText phoneNumber;
    Button sendButton;
    private EditText gpsCoordinates;
    LocationManager locationManager;
    LocationListener locationListener;
    int gpsCoordinatesRefreshTime=10000;
    int gpsCoordinatesRefreshDistance=0;
    private final String constantMsg="";
    final int REQUEST_GPS_COARSE_LOCATION=1;
    final int REQUEST_GPS_FINE_LOCATION=1;
    private static final String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE
    };
    private SmsHandler smsHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneNumber=findViewById(R.id.phoneNumber);
        sendButton=findViewById(R.id.sendButton);
        gpsCoordinates=findViewById(R.id.gpsCoordinates);
        smsHandler = new SmsHandler();
        smsHandler.registerReceiver(getApplicationContext(), true, false, false);
        smsHandler.setListener(this);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(phoneNumber.getText().toString(), constantMsg);
            }
        });
        requestPermissions();
    }


    /***
     * send a message to the inserted number
     * @param  message the message you want to send
     * @param telephoneNumber number to which you want to send the sms
     */
    public void sendMessage(String telephoneNumber,String message)
    {
        smsHandler.sendSMS(this, telephoneNumber, message);
    }


    /***
     * request all the permissions to run the app if they're not already granted
     */
    public void requestPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)+
                ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)+
        ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                !=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, 0);
    }


    /***
     * increase the alarm volume to maximum and start the default alarm
     * the alarm stops when the activity is closed
     */
    public void startAlarm()
    {
        MediaPlayer mediaPlayer =MediaPlayer.create(this,
                Settings.System.DEFAULT_RINGTONE_URI);
        AudioManager audioManager= (AudioManager) getSystemService((Context.AUDIO_SERVICE));
        assert audioManager != null;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        mediaPlayer.start();

    }


    /***
     * get the current phone's gps coordinates and write them on the "gps coordinates" editable text
     * @param gpsCoordinatesRefreshDistance refresh the gpsCoordinate when you travel gpsCoordinateRefreshDistance meters
     * @param gpsCoordinatesRefreshTime refresh the gpsCoordinate every gpsCoordinateRefreshTime seconds
     */
    public void getCoordinates(int gpsCoordinatesRefreshTime,int gpsCoordinatesRefreshDistance)
    {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) !=PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_GPS_COARSE_LOCATION );
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_GPS_FINE_LOCATION );
        }
        locationManager.requestLocationUpdates("gps", gpsCoordinatesRefreshTime, gpsCoordinatesRefreshDistance, locationListener);
        Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        assert location != null;
        gpsCoordinates.append("\n "+location.getLongitude()+"\n "+location.getLatitude());
    }


    /***
     * when a message is received with only the key word it open a new empty activity,start an alarm and send a message ,to the phone number
     * he received the message from ,with the device's current gps coordinates
     * @param message Received SMSMessage class of SmsHandler library
     */
    @Override
    public void onReceive(SMSMessage message)
    {
        if(message.getData().equals("<#>"))
        {
            Intent intent = new Intent(this, EmptyActivity.class);
            startActivity(intent);
            startAlarm();
            getCoordinates(gpsCoordinatesRefreshTime,gpsCoordinatesRefreshDistance);
            sendMessage(message.getPeer().toString(),gpsCoordinates.getText().toString());
        }
    else
        {
            String coordinates=message.getData();
            gpsCoordinates.append(coordinates);
        }
    }


    @Override
    public void onSent(int resultCode, SMSMessage message)
    {
    }


    @Override
    public void onDelivered(int resultCode, SMSMessage message)
    {
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        smsHandler.clearListener();
        smsHandler.unregisterReceiver(getApplicationContext());
    }

}