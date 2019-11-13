package com.example.killerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import com.dezen.riccardo.smshandler.SmsHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dezen.riccardo.smshandler.SMSMessage;


import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SmsHandler.OnSmsEventListener {

    public final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    private static final String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_STATE
    };


    public final int REQUEST_CODE_LOCATION = 0;
    public final int REQUEST_CODE_SMS = 1;
    private final int REQUEST_CODE_PHONE_STATE = 2;
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";
    private Constants constants;

    private final String killerAppWakelockTag = "killerapp:wakelockTag";

    public TextView txtLocation;


    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private PendingIntent locationIntent;

    private LocationRequest locationRequest;


    private EditText txtPhoneNumber;
    private Button sendButton;
    private Button sendAlarmRequestButton;
    private Button sendLocationRequestButton;


    private PowerManager.WakeLock wakeLock;
    private SmsHandler handler;


    private final String MAPS_START_URL = "https://www.google.com/maps/search/?api=1&query=";
    //NOTE: concat latitude,longitude


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtPhoneNumber =findViewById(R.id.phoneNumber);
        sendButton=findViewById(R.id.sendButton);
        sendAlarmRequestButton = findViewById(R.id.sendAlarmRequestButton);
        sendLocationRequestButton = findViewById(R.id.sendLocationRequestButton);

        handler = new SmsHandler();
        handler.registerReceiver(getApplicationContext(), true, false, false);
        handler.setListener(this);

        constants = new Constants();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermissions();


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Wake key to indicate urgency to the device
                String requestStringMessage = SmsHandler.WAKE_KEY + constants.locationMessages[constants.request]
                        + " " + constants.audioAlarmMessages[constants.request];
                handler.sendSMS(getApplicationContext(),txtPhoneNumber.getText().toString(), requestStringMessage);
            }
        });

        sendLocationRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestStringMessage = SmsHandler.WAKE_KEY + constants.locationMessages[constants.request];
                handler.sendSMS(getApplicationContext(),txtPhoneNumber.getText().toString(), requestStringMessage);
            }
        });

        sendAlarmRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestStringMessage = SmsHandler.WAKE_KEY + constants.audioAlarmMessages[constants.request];
                handler.sendSMS(getApplicationContext(),txtPhoneNumber.getText().toString(), requestStringMessage);
            }
        });
    }



    @Override
    protected void onStart()
    {
        super.onStart();

    }
    /***
     * Requests Android permissions if not granted
     */
    public void requestPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)+
                ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, 0);
    }
    private void StartActivity(Context context)
    {
        Intent alarmIntent = new Intent("android.intent.action.MAIN");
        alarmIntent.setClass(context, AlarmAndLocateActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }

    /***
     * Based on the message's content, this method informs the user if it's a response message,
     * Opens AlarmAndLocateActivity if it's a request message,
     *
     * @param message Received SMSMessage class of SmsHandler library
     */
    public  void onReceive(SMSMessage message)
    {
        String receivedStringMessage = message.getData();
        Log.d(MAIN_ACTIVITY_TAG, "onReceive" + receivedStringMessage);

        if (receivedStringMessage.contains(constants.locationMessages[constants.request])
                || receivedStringMessage.contains(constants.audioAlarmMessages[constants.request])) {

            Intent openAlarmAndLocateActivityIntent = new Intent(getApplicationContext(), AlarmAndLocateActivity.class);
            openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringMessage, message.getData());
            openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringAddress, message.getPeer().getAddress());
            openAlarmAndLocateActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(openAlarmAndLocateActivityIntent);
        }

        if(receivedStringMessage.contains(constants.locationMessages[constants.response])){
            Double longitude = 0.0;
            Double latitude = 0.0;
            try {
                longitude = Double.parseDouble(getLongitude(receivedStringMessage));
                latitude = Double.parseDouble(getLatitude(receivedStringMessage));
                Log.d(MAIN_ACTIVITY_TAG, latitude.toString() + "," +longitude.toString());
                OpenMapsUrl(latitude, longitude); //Should be working
            }
            catch (Exception e){
                Log.d(MAIN_ACTIVITY_TAG, e.getMessage());
                Toast.makeText(getApplicationContext(), "Response message contains error",Toast.LENGTH_LONG).show();
            }

        }

        Toast.makeText(getApplicationContext(), "Message Received",Toast.LENGTH_LONG).show();
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
        handler.clearListener();
        handler.unregisterReceiver(getApplicationContext());
    }

    public String getLatitude(String receivedMessage)
    {
        int start = receivedMessage.indexOf(constants.latitudeTag) + constants.latitudeTag.length();
        int end = receivedMessage.indexOf(constants.latitudeTagEnd);
        Log.d(MAIN_ACTIVITY_TAG, start +" " +end);
        return receivedMessage.substring(start, end);
    }

    public String getLongitude(String receivedMessage)
    {
        Log.d(MAIN_ACTIVITY_TAG, "getLong: "+ receivedMessage);
        int start = receivedMessage.indexOf(constants.longitudeTag) + constants.longitudeTag.length();
        int end = receivedMessage.indexOf(constants.longitudeTagEnd);

        Log.d(MAIN_ACTIVITY_TAG, start +" " +end);
        return receivedMessage.substring(start, end);
    }

    public void OpenMapsUrl(Double mapsLatitude, Double mapsLongitude)
    {
        String url = MAPS_START_URL + mapsLatitude + "," + mapsLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }



}

