package com.example.killerapp;

import android.location.Location;

import com.dezen.riccardo.smshandler.SmsHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocationManagerTest {

    LocationManager locationManager;
    Constants constants;
    @Test
    public void testGetRequestStringMessage() {
        locationManager = new LocationManager(SmsHandler.WAKE_KEY);
        constants = new Constants();
        String locationRequest = locationManager.getRequestStringMessage();
        assertTrue(locationRequest, locationRequest.contains(SmsHandler.WAKE_KEY));
        assertTrue(locationRequest, locationRequest.contains(locationManager.locationMessages[locationManager.request]));
    }
    @Test
    public void testContainsLocationRequest()
    {
        locationManager = new LocationManager(SmsHandler.WAKE_KEY);
        constants = new Constants();
        String locationRequest = locationManager.getRequestStringMessage();
        assertTrue(locationManager.containsLocationRequest(locationRequest));
    }


}