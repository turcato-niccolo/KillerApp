package com.example.killerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.dezen.riccardo.smshandler.ReceivedMessageListener;
import com.dezen.riccardo.smshandler.SMSMessage;

import com.dezen.riccardo.smshandler.SMSManager;
import com.dezen.riccardo.smshandler.SMSPeer;

public class MainActivity extends AppCompatActivity implements SmsHandler.OnSmsEventListener {
    public final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    public final int REQUEST_CODE_LOCATION = 0;
    public final int REQUEST_CODE_SMS = 1;
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";
    private final String longitudeTag = "<LG>";
    private final String longitudeTagEnd = "</LG>";
    private final String latitudeTag = "<LT>";
    private final String latitudeTagEnd = "</LT>";

    public TextView txtLocation;


    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private Location auxLocation;
    private PendingIntent locationIntent;
    private LocationRequest locationRequest;
    private SendResponseSms sendResponseSms;



    public final String SEND_SMS = "android.permission.SEND_SMS";
    public final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public final String READ_SMS = "android.permission.READ_SMS";

    public final String[] locationMessages = {"LOCATION_REQUEST", "LOCATION_RESPONSE"};
    public final String[] audioAlarmMessages = {"AUDIO_ALARM_REQUEST", "AUDIO_ALARM_RESPONSE"};
    public final int request = 0, response = 1;



    private EditText txtPhoneNumber;
    private Button sendButton;
    private EditText gpsLatitude;
    private EditText gpsLongitude;

    private SmsHandler handler;


    private final String MAPS_START_URL = "https://www.google.com/maps/search/?api=1&query=";
    //NOTE: concat latitude,longitude


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPhoneNumber =findViewById(R.id.phoneNumber);
        sendButton=findViewById(R.id.sendButton);
        gpsLatitude=findViewById(R.id.gpsLatitude);
        gpsLongitude=findViewById(R.id.gpsLongitude);

        handler = new SmsHandler();
        handler.registerReceiver(getApplicationContext(), true, false, false);
        handler.setListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestStringMessage = locationMessages[request] + " " + audioAlarmMessages[request];
                handler.sendSMS(getApplicationContext(),txtPhoneNumber.getText().toString(), requestStringMessage);
            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        if(!LocationPermissionsGranted())
            requestLocationPermissions();

        if(!SmsPermissionGranted())
            requestSmsPermission();
    }


    /***
     *  Checks ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION permissions
     *  Returns true is permissions are guaranteed, false if otherwise
     */
    public boolean LocationPermissionsGranted()
    {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }

    /***
     * Requests ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION permissions
     */
    public void requestLocationPermissions()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_LOCATION);
    }

    /***
     * Method that gets the last Location available of the device, and executes the imposed command
     * callind command.execute(foundLocation)
     *
     * @param command object of a class that implements interface Command
     */
    private void getLastLocation(final Command command)
    {
        Log.d(MAIN_ACTIVITY_TAG, "Getting last location");

        mFusedLocationClient.flushLocations(); //watch out, might cause problems
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.getLocationAvailability().addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
            @Override
            public void onSuccess(LocationAvailability locationAvailability) {
                Log.d(MAIN_ACTIVITY_TAG, "onSuccess: locationAvailability.isLocationAvailable " + locationAvailability.isLocationAvailable());

                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationIntent)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    Log.d(MAIN_ACTIVITY_TAG, "Update Result: " + task.getResult());
                                }
                            });

                    Log.d(MAIN_ACTIVITY_TAG, "Requested updated location: ");

                    mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Log.d(MAIN_ACTIVITY_TAG, "Completed lastLocation");
                            Log.d(MAIN_ACTIVITY_TAG, "Task<Location> successful " +  task.isSuccessful());

                            if (task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();
                                Log.d(MAIN_ACTIVITY_TAG, "Victory!" +mLastLocation.toString());
                                command.execute(mLastLocation);
                                //mLastLocation is used directly here because once out of OnComplete
                                //the variable becomes null, not clear why

                            } else if (!task.isSuccessful()) {
                                Log.d(MAIN_ACTIVITY_TAG, "Task<Location> not successful");
                            } else if (task.getResult() == null) {
                                Log.d(MAIN_ACTIVITY_TAG, "Task<Location> result is null");
                            }
                            Log.d(MAIN_ACTIVITY_TAG, "End of OnComplete " +mLastLocation.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(MAIN_ACTIVITY_TAG, "Task<Location>: " + e.getMessage());
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    Log.d(MAIN_ACTIVITY_TAG, "Task<Location> getLastLocation: Canceled");
                                }
                            });
            }
        })
        .addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(MAIN_ACTIVITY_TAG, "Task<Location>: Canceled");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(MAIN_ACTIVITY_TAG, "Task<Location>: " + e.getMessage());
            }
        });

        mFusedLocationClient.removeLocationUpdates(locationIntent);


    }


    /***
     * Action to execute when receiving a request Location
     * Send back current position
     */
    public class SendResponseSms implements Command<Location> {
        String receivingAddress;
        public void execute(Location foundLocation) {
            String responseMessage = locationMessages[response];
            responseMessage += latitudeTag + foundLocation.getLatitude() + latitudeTagEnd + " ";
            responseMessage += longitudeTag + foundLocation.getLongitude() + longitudeTag;
            handler.sendSMS(getApplicationContext(), receivingAddress, responseMessage);
        }
        public  SendResponseSms(String receiverAddress)
        {
            receivingAddress = receiverAddress;
        }
    }


    public void requestSmsPermission()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{SEND_SMS, RECEIVE_SMS, READ_SMS}, REQUEST_CODE_SMS);

    }

    public boolean SmsPermissionGranted()
    {
        return ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED);
    }

    /***
     * Based on the message's content, this method informs the user if it's a response message,
     * it responds to the message if it's a request message
     *
     * @param message Received SMSMessage class of SmsHandler library
     */
    public  void onReceive(SMSMessage message)
    {
        String responseStringMessage = "";
        String receivedStringMesage = message.getData();
        if(receivedStringMesage.contains(locationMessages[request]))
        {
            sendResponseSms = new SendResponseSms(message.getPeer().getAddress());
            getLastLocation(sendResponseSms);
        }

        if(receivedStringMesage.contains(locationMessages[response]))
        {
            Double longitude;
            Double latitude;
            try {
                longitude = Double.parseDouble(getLongitude(responseStringMessage));
                latitude = Double.parseDouble(getLatitude(responseStringMessage));
                OpenMapsUrl(latitude, longitude); //Might be not working
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Response message contains error",Toast.LENGTH_LONG).show();
            }

        }
        if(receivedStringMesage.contains(audioAlarmMessages[request]))
        {

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
        int start = receivedMessage.indexOf(latitudeTag) + latitudeTag.length();
        int end = receivedMessage.indexOf(latitudeTagEnd);
        return receivedMessage.substring(start, end);
    }

    public String getLongitude(String receivedMessage)
    {
        int start = receivedMessage.indexOf(longitudeTag) + longitudeTag.length();
        int end = receivedMessage.indexOf(longitudeTagEnd);
        return receivedMessage.substring(start, end);
    }

    public void OpenMapsUrl(Double mapsLatitude, Double mapsLongitude)
    {
        String url = MAPS_START_URL + mapsLatitude + "," + mapsLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

}

