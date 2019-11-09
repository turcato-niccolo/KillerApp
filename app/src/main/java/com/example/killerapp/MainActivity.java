package com.example.killerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dezen.riccardo.smshandler.ReceivedMessageListener;
import com.dezen.riccardo.smshandler.SMSMessage;

import com.dezen.riccardo.smshandler.SMSManager;
import com.dezen.riccardo.smshandler.SMSPeer;

public class MainActivity extends AppCompatActivity implements ReceivedMessageListener<SMSMessage> {
    public final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    public final String SEND_SMS = "android.permission.SEND_SMS";
    public final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public final String READ_SMS = "android.permission.READ_SMS";

    public final String[] locationMessages = {"LOCATION_REQUEST", "LOCATION_RESPONSE"};
    public final String[] audioAlarmMessages = {"AUDIO_ALARM_REQUEST", "AUDIO_ALARM_RESPONSE"};
    public final int request = 0, response = 1;

    public final int REQUEST_CODE_SMS = 1;

    private EditText txtPhoneNumber;
    private Button sendButton;
    private EditText gpsLatitude;
    private EditText gpsLongitude;

    private SMSManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtPhoneNumber =findViewById(R.id.phoneNumber);
        sendButton=findViewById(R.id.sendButton);
        gpsLatitude=findViewById(R.id.gpsLatitude);
        gpsLongitude=findViewById(R.id.gpsLongitude);

        manager = manager.getInstance(getApplicationContext());

        requestSmsPermission();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SMSPeer requestPeer = new SMSPeer(txtPhoneNumber.getText().toString());
                String requestStringMessage = locationMessages[request] + " " + audioAlarmMessages[request];
                SMSMessage locationRequestMessage = new SMSMessage(requestPeer, requestStringMessage);
                manager.sendMessage(locationRequestMessage);
            }
        });
    }

    public void requestSmsPermission()
    {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{SEND_SMS, RECEIVE_SMS, READ_SMS}, REQUEST_CODE_SMS);
        }
    }

    /***
     *
     * @param message Received SMSMessage class of SmsHandler library
     */
    public  void onMessageReceived(SMSMessage message)
    {
        //TODO add specific functionality -> Feature branch
        Toast.makeText(getApplicationContext(), "Message Received",Toast.LENGTH_LONG).show();
    }

}
