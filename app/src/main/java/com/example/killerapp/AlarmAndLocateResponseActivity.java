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
    private String receivedStringMessage;
    private String receivedStringAddress;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private PendingIntent locationIntent;
    private Location mLastLocation;
    private Constants constants;
    private SmsHandler handler;
    private  SendResponseSms sendResponseSms;
    private MediaPlayer mediaPlayer;


    /**
     * This activity is created in all situations, so it needs to be executed also when screen is shut
     *
     * @param savedInstanceState
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

        //Params passed by methods tha called this activity
        receivedStringMessage = getIntent().getStringExtra(constants.receivedStringMessage);
        receivedStringAddress = getIntent().getStringExtra(constants.receivedStringAddress);
        handler = new SmsHandler();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (receivedStringMessage.contains(constants.locationMessages[constants.request])) {
            //Action to execute when device receives a Location request
            sendResponseSms = new SendResponseSms(receivedStringAddress);
            getLastLocation(sendResponseSms);
        }

        if (receivedStringMessage.contains(constants.audioAlarmMessages[constants.request])) {
            startAlarm(); //User has to close app manually to stop
        }
    }

    /***
     * Method that gets the last Location available of the device, and executes the imposed command
     * callind command.execute(foundLocation)
     *
     * @param command object of a class that implements interface Command
     */
    private void getLastLocation(final Command command)
    {
        mFusedLocationClient.flushLocations();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.getLocationAvailability().addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
            @Override
            public void onSuccess(LocationAvailability locationAvailability) {
                Log.d(AlarmAndLocateActivityTAG, "onSuccess: locationAvailability.isLocationAvailable " + locationAvailability.isLocationAvailable());

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationIntent)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                Log.d(AlarmAndLocateActivityTAG, "Update Result: " + task.getResult());
                            }
                        });

                Log.d(AlarmAndLocateActivityTAG, "Requested updated location: ");

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d(AlarmAndLocateActivityTAG, "Completed lastLocation");
                        Log.d(AlarmAndLocateActivityTAG, "Task<Location> successful " +  task.isSuccessful());

                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            Log.d(AlarmAndLocateActivityTAG, "Victory!" +mLastLocation.toString());
                            command.execute(mLastLocation);
                            /*
                            mLastLocation is used directly here because once out of OnComplete
                            the Location isn't available and the variable that contains it
                            becomes null
                            */
                        } else if (!task.isSuccessful()) {
                            Log.d(AlarmAndLocateActivityTAG, "Task<Location> not successful");
                        } else if (task.getResult() == null) {
                            Log.d(AlarmAndLocateActivityTAG, "Task<Location> result is null");
                        }
                        Log.d(AlarmAndLocateActivityTAG, "End of OnComplete " +mLastLocation.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(AlarmAndLocateActivityTAG, "Task<Location>: " + e.getMessage());
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(AlarmAndLocateActivityTAG, "Task<Location> getLastLocation: Canceled");
                    }
                });
            }
        })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(AlarmAndLocateActivityTAG, "Task<Location>: Canceled");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(AlarmAndLocateActivityTAG, "Task<Location>: " + e.getMessage());
                    }
                });

        //The request is high priority, this instruction removes it to be more efficient
        mFusedLocationClient.removeLocationUpdates(locationIntent);
    }

    /**
     * Starts and alarm with the default ringtone of the device, stops when activity is closed by user
     */
    public void startAlarm()
    {
        //Courtesy of P. Kumar
        mediaPlayer =MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        AudioManager audioManager= (AudioManager) getSystemService((Context.AUDIO_SERVICE));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.clearListener();
        handler.unregisterReceiver(getApplicationContext());
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    /***
     * Action to execute when receiving a request Location
     * Sends back current position
     */
    public class SendResponseSms implements Command<Location> {
        String receivingAddress;
        public void execute(Location foundLocation) {
            String responseMessage = constants.locationMessages[constants.response];
            responseMessage += constants.latitudeTag + foundLocation.getLatitude() + constants.latitudeTagEnd + " ";
            responseMessage += constants.longitudeTag + foundLocation.getLongitude() + constants.longitudeTagEnd;
            handler.sendSMS(getApplicationContext(), receivingAddress, responseMessage);
        }
        public  SendResponseSms(String receiverAddress)
        {
            receivingAddress = receiverAddress;
        }
    }
}
