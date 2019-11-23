package com.example.killerapp;

import android.location.Location;

import com.dezen.riccardo.smshandler.SmsHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocationManagerTest {
    LocationManager locationManager;
    Constants constants;


    @Test
    public void testGetResponseMessage()
    {
        locationManager = new LocationManager(SmsHandler.WAKE_KEY);
        constants = new Constants();
        Double testLatitude = 12.1;
        Double testLongitude = 34.4;
        Location testLocation = new Location("testGetResponseStringMessage");
        testLocation.setLatitude(testLatitude);
        testLocation.setLongitude(testLongitude);
        testLocation.setLatitude(testLatitude);

        String expectedResponseMessage = locationManager.locationMessages[locationManager.response];
        expectedResponseMessage += locationManager.latitudeTag + testLatitude + locationManager.latitudeTagEnd + " ";
        expectedResponseMessage += locationManager.longitudeTag + testLongitude + locationManager.longitudeTagEnd;

        assertEquals(expectedResponseMessage, locationManager.getResponseStringMessage(testLocation));
    }

    @Test
    public void testContainsLocationResponse()
    {
        locationManager = new LocationManager(SmsHandler.WAKE_KEY);
        constants = new Constants();
        Double testLatitude = 12.1;
        Double testLongitude = 34.4;
        Location testLocation = new Location("testGetResponseStringMessage");
        testLocation.setLatitude(testLatitude);
        testLocation.setLongitude(testLongitude);
        testLocation.setLatitude(testLatitude);
        String expectedLocationResponse = locationManager.getResponseStringMessage(testLocation);
        assertTrue(locationManager.containsLocationResponse(expectedLocationResponse));
    }
}