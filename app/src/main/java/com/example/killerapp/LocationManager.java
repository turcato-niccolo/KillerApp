package com.example.killerapp;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

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

/**
 * @author Turcato
 */
class LocationManager {
    String[] locationMessages = {"LOCATION_REQUEST", "LOCATION_RESPONSE"};
    final int request = 0, response = 1;
    final String longitudeTag = "<LG>";
    final String longitudeTagEnd = "</LG>";
    final String latitudeTag = "<LT>";
    final String latitudeTagEnd = "</LT>";
    final String LocationManagerTag = "LocationManagerTag";

    LocationRequest locationRequest;
    PendingIntent locationIntent;
    Location mLastLocation;



    FusedLocationProviderClient mFusedLocationClient;

    String libraryWakeKey;

    /**
     * @author Turcato
     * @param wakeKey string key used by library to identify an urgent sms
     */
    public LocationManager(String wakeKey)
    {
        libraryWakeKey = wakeKey;
    }

    /**
     *
     * @return returns a formatted String containing the location Request command
     */
    public String getRequestStringMessage()
    {
        return libraryWakeKey + locationMessages[request];
    }

    /**
     *
     * @param locationStringRequest the text message received
     * @return true if the received text contains the (formatted) location Request
     */
    public boolean containsLocationRequest(String locationStringRequest)
    {
        return locationStringRequest.contains(locationMessages[request]);
    }

    /**
     *
     * @param foundLocation the found location the device needs to send back
     * @return a formatted string containing the location as <>longitude</> <>latitude</>
     */
    public String getResponseStringMessage(Location foundLocation)
    {
        String locationResponseMessage = locationMessages[response];
        locationResponseMessage += latitudeTag + foundLocation.getLatitude() + latitudeTagEnd + " ";
        locationResponseMessage += longitudeTag + foundLocation.getLongitude() + longitudeTagEnd;
        return locationResponseMessage;
    }

    /**
     *
     * @param locationStringResponse string containing the received txt message
     * @return true if the received message contains a location response message
     */
    public boolean containsLocationResponse(String locationStringResponse)
    {
        return locationStringResponse.contains(locationMessages[response]);
    }


    /***
     * @author Turcato
     * Extract the string contained between the latitude tags (if present)
     * Returns empty string if it doesn't find the tags
     *
     * @param receivedMessage string containing the text received sy sms
     * @return if present, the string contained between the latitude tags, empty string if it doesn't find the tags
     */
    public String getLatitude(String receivedMessage)
    {
        int start = receivedMessage.indexOf(latitudeTag);
        int end = receivedMessage.indexOf(latitudeTagEnd);
        if(start > -1 && end > -1)
        {
            start += latitudeTag.length();
            return receivedMessage.substring(start, end);
        }
        return "";
    }

    /***
     * @author Turcato
     * Extract the string contained between the longitude tags (if present)
     * Returns empty string if it doesn't find the tags
     *
     * @param receivedMessage string containing the text received sy sms
     * @return if present, the string contained between the longitude tags, empty string if it doesn't find the tags
     */
    public String getLongitude(String receivedMessage)
    {
        int start = receivedMessage.indexOf(longitudeTag);
        int end = receivedMessage.indexOf(longitudeTagEnd);
        if(start > -1 && end > -1)
        {
            start += longitudeTag.length();
            return receivedMessage.substring(start, end);
        }
        return "";
    }

    /***
     * Method that gets the last Location available of the device, and executes the imposed command
     * calling command.execute(foundLocation)
     *
     * @param command object of a class that implements interface Command
     */
    public void getLastLocation(Context applicationContext, final Command command)
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext);
        mFusedLocationClient.flushLocations();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.getLocationAvailability().addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
            @Override
            public void onSuccess(LocationAvailability locationAvailability) {
                Log.d(LocationManagerTag, "onSuccess: locationAvailability.isLocationAvailable " + locationAvailability.isLocationAvailable());

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationIntent)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                Log.d(LocationManagerTag, "Update Result: " + task.getResult());
                            }
                        });

                Log.d(LocationManagerTag, "Requested updated location: ");

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d(LocationManagerTag, "Completed lastLocation");
                        Log.d(LocationManagerTag, "Task<Location> successful " +  task.isSuccessful());

                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            Log.d(LocationManagerTag, "Victory!" +mLastLocation.toString());
                            command.execute(mLastLocation);
                            /*
                            mLastLocation is used directly here because once out of OnComplete
                            the Location isn't available and the variable that contains it
                            becomes null
                            */
                        } else if (!task.isSuccessful()) {
                            Log.d(LocationManagerTag, "Task<Location> not successful");
                        } else if (task.getResult() == null) {
                            Log.d(LocationManagerTag, "Task<Location> result is null");
                        }
                        Log.d(LocationManagerTag, "End of OnComplete " +mLastLocation.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LocationManagerTag, "Task<Location>: " + e.getMessage());
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(LocationManagerTag, "Task<Location> getLastLocation: Canceled");
                    }
                });
            }
        })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(LocationManagerTag, "Task<Location>: Canceled");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LocationManagerTag, "Task<Location>: " + e.getMessage());
                    }
                });

        //The request is high priority, this instruction removes it to be more efficient
        mFusedLocationClient.removeLocationUpdates(locationIntent);
    }

}
