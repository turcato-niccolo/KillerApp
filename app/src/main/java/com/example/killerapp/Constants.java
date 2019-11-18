package com.example.killerapp;

/**
 * Contains the constants that need to be shared between activities of the application,
 * all of them are defined package-private
 */

//Reviewers expected: Scialpi, Ursino
class Constants {
    final String[] locationMessages = {"LOCATION_REQUEST", "LOCATION_RESPONSE"};
    final String[] audioAlarmMessages = {"AUDIO_ALARM_REQUEST", "AUDIO_ALARM_RESPONSE"};
    final int request = 0, response = 1;
    final String receivedStringMessage = "receivedStringMessage";
    final String receivedStringAddress = "receivedStringAddress";
    final String longitudeTag = "<LG>";
    final String longitudeTagEnd = "</LG>";
    final String latitudeTag = "<LT>";
    final String latitudeTagEnd = "</LT>";
}
