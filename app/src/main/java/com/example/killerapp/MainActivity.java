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

public class MainActivity extends AppCompatActivity {
    public final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    public final int REQUEST_CODE_LOCATION = 0;
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";

    public TextView txtLocation;
    public Button btnLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private PendingIntent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        txtLocation = findViewById(R.id.txtLocation);



        // mFusedLocationClient.getLastLocation().addOnSuccessListener(this, getLastLocation());

        btnLocation = findViewById(R.id.btnLocation);

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
                txtLocation.setText("Last Location: "+ mLastLocation.getLatitude() +"; " +mLastLocation.getAltitude());
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        CheckPermissions();
    }


    /***
     *  Checks ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION permissions
     *
     */
    public void CheckPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions();
    }

    /***
     * Requests ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION permissions
     */
    public void requestPermissions()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_LOCATION);
    }

    private Location getLastLocation()
    {
        //mFusedLocationClient.flushLocations();
        Log.d(MAIN_ACTIVITY_TAG, "Getting last location");


        mFusedLocationClient.requestLocationUpdates(LocationRequest.create(), intent)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Log.d("MainActivity", "Result: " + task.getResult());
                        mLastLocation = (Location)task.getResult();
                    }
                });




        mFusedLocationClient.getLocationAvailability().addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
            @Override
            public void onSuccess(LocationAvailability locationAvailability) {
                Log.d(MAIN_ACTIVITY_TAG, "onSuccess: locationAvailability.isLocationAvailable " + locationAvailability.isLocationAvailable());

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                        } else if (!task.isSuccessful()) {
                            Log.d(MAIN_ACTIVITY_TAG, "Task<Location> not successful");
                        } else if (task.getResult() == null) {
                            Log.d(MAIN_ACTIVITY_TAG, "Task<Location> result is null");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(MAIN_ACTIVITY_TAG, "Task<Location>: " + e.getMessage());
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

        return mLastLocation;
    }






}
