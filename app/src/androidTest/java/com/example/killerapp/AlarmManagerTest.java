package com.example.killerapp;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;


import static org.junit.Assert.*;

public class AlarmManagerTest {



    @Test
    public void canStartAlarm() {
        AlarmManager alarmManager = new AlarmManager("FAKE_KEY");
        try
        {
            alarmManager.startAlarm(InstrumentationRegistry.getInstrumentation().getTargetContext());
        }
        catch(IllegalStateException e)
        {
            assertTrue(false);
        }
        assertTrue(true);
    }
}