package com.example.killerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


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

public class MainActivity extends AppCompatActivity {
    public final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    public final int REQUEST_CODE_LOCATION = 0;
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";

    public TextView txtLocation;
    public Button btnLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private Location auxLocation;
    private PendingIntent locationIntent;
    private LocationRequest locationRequest;
    private changeLabelCommand labelCommand;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        txtLocation = findViewById(R.id.txtLocation);
        labelCommand = new changeLabelCommand();

        // mFusedLocationClient.getLastLocation().addOnSuccessListener(this, getLastLocation());

        btnLocation = findViewById(R.id.btnLocation);

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation(labelCommand); //Changes the label too
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(!LocationPermissionsGranted())
            requestLocationPermissions();

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
        //At this point mLastLocation is null
        //Log.d(MAIN_ACTIVITY_TAG, mLastLocation.toString());

    }


    public class changeLabelCommand implements Command<Location> {
        public void execute(Location foundLocation) {
            txtLocation.setText("Last Location: " + foundLocation.getLatitude() + "; " + foundLocation.getLongitude());
        }
    }

}

