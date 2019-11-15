package com.example.killerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.dezen.riccardo.smshandler.SmsHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import androidx.core.content.ContextCompat;

import android.Manifest;
import android.widget.EditText;

import com.dezen.riccardo.smshandler.SMSMessage;

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


    /***
     * This method is executed both when the app is running or not.
     * Based on the message's content, opens AlarmAndLocateResponseActivity if it's a request message,
     * otherwise if it contains the location response (the only one expected) it opens the default maps application
     * to the received location
     *
     * @param message Received SMSMessage class of SmsHandler library
     */
    public  void onReceive(SMSMessage message)
    {
        String receivedStringMessage = message.getData();

        //Both Requests are handled by the other activity
        if (receivedStringMessage.contains(constants.locationMessages[constants.request])
                || receivedStringMessage.contains(constants.audioAlarmMessages[constants.request])) {
            OpenRequestsActivity(receivedStringMessage, message.getPeer().getAddress());
        }

        //The only expected response
        if(receivedStringMessage.contains(constants.locationMessages[constants.response])){
            Double longitude = 0.0;
            Double latitude = 0.0;
            try {
                longitude = Double.parseDouble(getLongitude(receivedStringMessage));
                latitude = Double.parseDouble(getLatitude(receivedStringMessage));
                OpenMapsUrl(latitude, longitude);
            }
            catch (Exception e){
                //Written in log for future users to report
                Log.e(MAIN_ACTIVITY_TAG,constants.locationMessages[constants.response] + e.getMessage());
            }

        }
    }

    /***
     * Opens the AlarmAndLocateResponseActivity, forwarding the receivedMessageText and the receivedMessageReturnAddress
     * The opened activity's task is to respond to the given requests, that can't be handled on this
     * activity because the app might be closed, so the response activity has to be forcedly opened.
     *
     * When app is closed the messages are received by KillerAppClosedReceiver,
     * secondary BroadcastReceiver that responds to the forced WAKE and has the same job as this method
     *
     * @param receivedMessageText the text of the request message
     * @param receivedMessageReturnAddress the return address of the request message
     */
    private void OpenRequestsActivity(String receivedMessageText, String receivedMessageReturnAddress)
    {
        Intent openAlarmAndLocateActivityIntent = new Intent(getApplicationContext(), AlarmAndLocateResponseActivity.class);
        openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringMessage, receivedMessageText);
        openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringAddress, receivedMessageReturnAddress);
        openAlarmAndLocateActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(openAlarmAndLocateActivityIntent);
    }



    @Override
    public void onSent(int resultCode, SMSMessage message)
    {
        //Required by interface OnSmsEventListener, not used
    }

    @Override
    public void onDelivered(int resultCode, SMSMessage message)
    {
        //Required by interface OnSmsEventListener, not used
    }

    /**
     * Safely deletes the listeners
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.clearListener();
        handler.unregisterReceiver(getApplicationContext());
    }

    /***
     * Extract the string contained between the latitude tags (if present)
     * Returns empty string if it doesn't find the tags
     *
      * @param receivedMessage string containing the text received sy sms
     * @return if present, the string contained between the latitude tags, empty string if it doesn't find the tags
     */
    public String getLatitude(String receivedMessage)
    {
        int start = receivedMessage.indexOf(constants.latitudeTag);
        int end = receivedMessage.indexOf(constants.latitudeTagEnd);
        if(start > -1 && end > -1)
        {
            start += constants.latitudeTag.length();
            return receivedMessage.substring(start, end);
        }
        return "";
    }

    /***
     * Extract the string contained between the longitude tags (if present)
     * Returns empty string if it doesn't find the tags
     *
     * @param receivedMessage string containing the text received sy sms
     * @return if present, the string contained between the longitude tags, empty string if it doesn't find the tags
     */
    public String getLongitude(String receivedMessage)
    {
        int start = receivedMessage.indexOf(constants.longitudeTag);
        int end = receivedMessage.indexOf(constants.longitudeTagEnd);
        if(start > -1 && end > -1)
        {
            start += constants.longitudeTag.length();
            return receivedMessage.substring(start, end);
        }
        return "";
    }

    /***
     * Opens the default maps application at the given Location(latitude, longitude)
     * @param mapsLatitude
     * @param mapsLongitude
     */
    public void OpenMapsUrl(Double mapsLatitude, Double mapsLongitude)
    {
        String url = MAPS_START_URL + mapsLatitude + "," + mapsLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }



}

