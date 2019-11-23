package com.example.killerapp;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.util.Log;

public class AlarmManager {
    final String[] audioAlarmMessages = {"AUDIO_ALARM_REQUEST", "AUDIO_ALARM_RESPONSE"};
    final int request = 0, response = 1;
    final String AlarmManagerTag = "AlarmManagerTag";
    private MediaPlayer mediaPlayer;

    String libraryWakeKey;
    /**
     * @author Turcato
     * @param wakeKey string key used by library to identify an urgent sms
     */
    public AlarmManager(String wakeKey)
    {
        libraryWakeKey = wakeKey;
    }

    /**
     *
     * @return returns a formatted String containing the alarm Request command
     */
    public String getRequestStringMessage()
    {
        return libraryWakeKey + audioAlarmMessages[request];
    }

    /**
     *
     * @param alarmStringRequest the text message received
     * @return true if the received text contains the (formatted) alarm Request
     */
    public boolean containsAlarmRequest(String alarmStringRequest)
    {
        return alarmStringRequest.contains(audioAlarmMessages[request]);
    }

    /**
     * Starts and alarm with the default ringtone of the device, stops when activity is closed by user
     */
    public void startAlarm(Context context)
    {
        mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        AudioManager audioManager= (AudioManager) context.getSystemService((Context.AUDIO_SERVICE));
        try{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        }
        catch (Exception e) {
            Log.e(AlarmManagerTag, "Error in setStreamVolume: " + e.getMessage());
        }
        mediaPlayer.start();
    }

}
