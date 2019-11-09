package com.example.killerapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class GetLatitudeTest {
    private final String latitudeTag = "<LT>";
    private final String latitudeTagEnd = "</LT>";
    @Test
    public void CorrectResult()
    {
        MainActivity mainActivity = new MainActivity();
        String expected = "12.44";
        String received = latitudeTag + expected + latitudeTagEnd;
        assertEquals(expected, mainActivity.getLatitude(received));
    }

}