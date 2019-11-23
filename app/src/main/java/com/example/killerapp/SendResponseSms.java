package com.example.killerapp;

import android.content.Context;
import android.location.Location;

import com.dezen.riccardo.smshandler.SMSManager;
import com.dezen.riccardo.smshandler.SmsHandler;

/***
 * @author Turcato
 * Action to execute when receiving a Location request
 * Sends back current position
 */
public class SendResponseSms implements Command<Location> {
    String receivingAddress;
    Constants constants;
    SmsHandler handler;
    Context applicationContext;
    LocationManager locationManager;

    /**
     * @author Turcato
     * Sends an sms message to the defined sms number with a text specifically formatted to contain
     * the position in foundlocation
     * @param foundLocation location to forward to given phone number
     */
    public void execute(Location foundLocation) {
        String responseMessage = locationManager.getResponseStringMessage(foundLocation);
        handler.sendSMS(applicationContext, receivingAddress, responseMessage);
    }

    /***
     * @author Turcato
     * @param receiverAddress receiver's phone number
     * @param handler SmsHandler's class object to use to send sms
     * @param context android application Context
     */
    public SendResponseSms(String receiverAddress, SmsHandler handler, Context context)
    {
        receivingAddress = receiverAddress;
        constants = new Constants();
        this.handler = handler;
        applicationContext = context;
        locationManager = new LocationManager(SmsHandler.WAKE_KEY);
    }
}
