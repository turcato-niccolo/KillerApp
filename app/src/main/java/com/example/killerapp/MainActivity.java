package com.example.killerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
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

public class MainActivity extends AppCompatActivity implements SmsHandler.OnSmsEventListener {
    private EditText phoneNumber;
    private Button sendButton;
    private EditText gpsLatitude;
    private EditText gpsLongitude;
    private final String messaggeConstant="ciao";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final int REQUEST_GPS_COARSE_LOCATION=1;
    private final int REQUEST_GPS_FINE_LOCATION=1;


    private static final String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
    };

    private SmsHandler smsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneNumber=findViewById(R.id.phoneNumber);
        sendButton=findViewById(R.id.sendButton);
        gpsLatitude=findViewById(R.id.gpsLatitude);
        gpsLongitude=findViewById(R.id.gpsLongitude);

        smsHandler = new SmsHandler();
        smsHandler.registerReceiver(getApplicationContext(), true, false, false);
        smsHandler.setListener(this);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(phoneNumber.getText().toString(), messaggeConstant);
            }
        });

        requestPermissions();
    }
    public void sendMessage(String destination,String Coordinates)
    {
        smsHandler.sendSMS(this, destination, Coordinates);
    }
    //request permissions to get gps coordinates and to send sms
    public void requestPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)+
                ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, 0);
    }

    public void startAlarm()
    {
        MediaPlayer mediaPlayer =MediaPlayer.create(this,
                Settings.System.DEFAULT_RINGTONE_URI);
        AudioManager audioManager= (AudioManager) getSystemService((Context.AUDIO_SERVICE));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        mediaPlayer.start();

    }
    public void getCoordinates()
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
        if (((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) !=PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_GPS_COARSE_LOCATION );
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_GPS_FINE_LOCATION );
        }
        locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);
        Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        gpsLatitude.append("\n "+location.getLatitude());
        gpsLongitude.append("\n "+location.getLongitude());
    }
    /***
     *
     * @param message Received SMSMessage class of SmsHandler library
     */
    @Override
    public void onReceive(SMSMessage message)
    {   if(message.getData().contains("<#>ciao"))
    {
        startAlarm();
        getCoordinates();
        sendMessage(message.getPeer().toString(),gpsLongitude.getText().toString()+"\n"+gpsLatitude.getText().toString());
    }
    else

    {
        String coordinates=message.getData();
        gpsLongitude.append(coordinates);
    }
    }


    @Override
    public void onSent(int resultCode, SMSMessage message)
    {
        //TODO? probably
    }

    @Override
    public void onDelivered(int resultCode, SMSMessage message)
    {
        //TODO? Is it necessary? the other client should as well send a confirmation
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        smsHandler.clearListener();
        smsHandler.unregisterReceiver(getApplicationContext());
    }

}
