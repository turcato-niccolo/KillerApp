package com.example.killerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SmsHandler;

public class MainActivity extends AppCompatActivity implements SmsHandler.OnSmsEventListener {
    public final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    public final String SEND_SMS = "android.permission.SEND_SMS";
    public final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public final String READ_SMS = "android.permission.READ_SMS";
    public final int REQUEST_CODE_SMS = 1;

    private SmsHandler smsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsHandler = new SmsHandler();
        smsHandler.registerReceiver(getApplicationContext(), true, false, false);
        smsHandler.setListener(this);

        requestSmsPermission();
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
    @Override
    public void onReceive(SMSMessage message)
    {
        //TODO add specific functionality -> Feature branch
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
}
