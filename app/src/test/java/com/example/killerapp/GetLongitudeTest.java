package com.example.killerapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class GetLongitudeTest {

    private final String longitudeTag = "<LG>";
    private final String longitudeTagEnd = "</LG>";
    @Test
    public void CorrectResult()
    {
        MainActivity mainActivity = new MainActivity();
        String expected = "12.44";
        String received = longitudeTag + expected + longitudeTagEnd;
        assertEquals(expected, mainActivity.getLongitude(received));
    }

    @Test
    public void CorrectResultNegative()
    {
        MainActivity mainActivity = new MainActivity();
        String expected = "-45.6";
        String received = longitudeTag + expected + longitudeTagEnd;
        assertEquals(expected, mainActivity.getLongitude(received));
    }
}