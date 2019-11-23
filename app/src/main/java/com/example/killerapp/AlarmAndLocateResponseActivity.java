package com.example.killerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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

public class AlarmAndLocateResponseActivity extends AppCompatActivity {
    private final String AlarmAndLocateActivityTAG = "Alarm&LocateActivityTAG";
    private String receivedTextMessage;
    private String receivedMessageAddress;
    private Constants constants;
    private SmsHandler handler;
    private  SendResponseSms sendResponseSms;
    private MediaPlayer mediaPlayer;
    private LocationManager locationManager;
    private AlarmManager alarmManager;


    /**
     * This activity is created in all situations, for each request, so it needs to be executed also when screen is shut
     *
     * @param savedInstanceState system parameter
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Needed to open Activity if screen is shut
        final Window win= getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_alarm_and_locate);
        //
        constants = new Constants();
        locationManager = new LocationManager(SmsHandler.WAKE_KEY);
        alarmManager = new AlarmManager(SmsHandler.WAKE_KEY);

        //Params passed by methods tha called this activity
        receivedTextMessage = getIntent().getStringExtra(constants.receivedStringMessage);
        receivedMessageAddress = getIntent().getStringExtra(constants.receivedStringAddress);
        handler = new SmsHandler();


        if (locationManager.containsLocationRequest(receivedTextMessage)) {
            //Action to execute when device receives a Location request
            sendResponseSms = new SendResponseSms(receivedMessageAddress, handler, getApplicationContext());
            locationManager.getLastLocation(this, sendResponseSms);
        }

        if (alarmManager.containsAlarmRequest(receivedTextMessage))
            startAlarm(); //User has to close app manually to stop

    }



    /**
     * Starts and alarm with the default ringtone of the device, stops when activity is closed by user
     */
    public void startAlarm()
    {
        mediaPlayer =MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        AudioManager audioManager= (AudioManager) getSystemService((Context.AUDIO_SERVICE));
        try{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        }
        catch (Exception e) {
            Log.e(AlarmAndLocateActivityTAG, "Error in setStreamVolume: " + e.getMessage());
        }
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {

        handler.clearListener();
        handler.unregisterReceiver(getApplicationContext());
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        super.onDestroy();
    }

}
