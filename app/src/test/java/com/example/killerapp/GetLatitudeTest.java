package com.example.killerapp;

import com.dezen.riccardo.smshandler.SmsHandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class GetLatitudeTest {
    private final String latitudeTag = "<LT>";
    private final String latitudeTagEnd = "</LT>";
    @Test
    public void CorrectResult()
    {
        LocationManager locationManager = new LocationManager(SmsHandler.WAKE_KEY);
        String expected = "12.44";
        String received = latitudeTag + expected + latitudeTagEnd;
        assertEquals(expected, locationManager.getLatitude(received));
    }

}