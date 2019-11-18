package com.example.killerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

//Reviewers expected: Scialpi, Ursino

public class KillerAppClosedReceiver extends BroadcastReceiver {
/***
 * This receiver responds when the app is closed, the Smshandler library's receiver can't be declared to work when app is closed
 * Waiting for a Library update to add a public method that can certificate messages sent by an instance of SmsHandler
 *
 * Opens the AlarmAndLocateResponseActivity, forwarding the receivedMessageText and the receivedMessageReturnAddress
 * The opened activity's task is to respond to the given requests, that can't be handled on the main
 * activity because the app might be closed, so the response activity has to be forcedly opened.
 *
 *
 */
    String receivedStringMessage;
    @Override
    public void onReceive(Context context, Intent intent) {
        Constants constants = new Constants();
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage message: messages) {
            //The messages built by this app fit in a single sms, so this cycle won't open the activity in more than one iteration
            receivedStringMessage = message.getMessageBody();
            if (receivedStringMessage.contains(constants.locationMessages[constants.request])
                    || receivedStringMessage.contains(constants.audioAlarmMessages[constants.request])) {
                Intent openAlarmAndLocateActivityIntent = new Intent(context, AlarmAndLocateResponseActivity.class);
                //Forwards message text and return address as parameters defined in the constants class
                openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringMessage, receivedStringMessage);
                openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringAddress, message.getOriginatingAddress());
                openAlarmAndLocateActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(openAlarmAndLocateActivityIntent);

            }
        }
    }
}
