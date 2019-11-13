package com.example.killerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class KillerAppClosedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Constants constants = new Constants();
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for(int i = 0; i < messages.length; i++) {
            Intent openAlarmAndLocateActivityIntent = new Intent(context, AlarmAndLocateActivity.class);
            openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringMessage, messages[i].getMessageBody());
            openAlarmAndLocateActivityIntent.putExtra(constants.receivedStringAddress, messages[i].getOriginatingAddress());
            openAlarmAndLocateActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openAlarmAndLocateActivityIntent);
        }
    }
}
