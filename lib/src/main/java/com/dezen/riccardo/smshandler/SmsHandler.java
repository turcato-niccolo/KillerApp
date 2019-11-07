package com.dezen.riccardo.smshandler;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.dezen.riccardo.smshandler.database.SmsDatabase;
import com.dezen.riccardo.smshandler.database.SmsEntity;

import java.util.ArrayList;
import java.util.List;

public class SmsHandler {
    /**TODO add owner mechanism: every instance should have a private Context field containing the
     *  owner of the instance. This field should then be used to check who's calling the methods
     *  for example, it would be a bad idea to have someone who hasn't registered a receiver with
     *  registerReceiver(context) be able to cancel it freely.
     *  See SMSManager for an example of this mechanism, which should be implemented here albeit with a
     *  private local field instead of a static field (to allow chance to add full non-singleton support
     *  in the future
     */
    public static final String APP_KEY = "<#>";
    //string for future implementation of activity start
    public static final String WAKE_KEY = "<urgent>";
    public static final String SMS_HANDLER_RECEIVED_BROADCAST = "NEW_SMS";
    public static final String SMS_HANDLER_SENT_BROADCAST = "SMS_SENT";
    public static final String SMS_HANDLER_DELIVERED_BROADCAST = "SMS_DELIVERED";
    public static final String SMS_HANDLER_WAKE_BROADCAST = "FORCE_WAKE";
    public static final String SMS_HANDLER_LOCAL_DATABASE = "sms-db";
    /**
     * Contains references to all listeners belonging to instances of this class
     * which registered a receiver that listens for at least incoming sms.*/
    private static List<OnSmsEventListener> activeIncomingListeners = new ArrayList<>();

    private SmsManager smsManager;
    private String scAddress;

    //This instance's attached listener.
    private OnSmsEventListener listener;
    //This instance's not necessarily registered BroadcastReceiver.
    private SmsEventReceiver smsEventReceiver;
    //Whether the receiver for this instance is listening for at least incoming sms.
    private boolean listeningForIncoming;
    //Whether this instance should use pending intent to confirm sending
    private boolean shouldUsePendingIntentSending;
    //Whether this instance should use pending intent to confirm delivery
    private boolean shouldUsePendingIntentDelivery;
    /**
     * Default constructor. SmsManager.getDefault() can behave unpredictably if called from a
     * background thread in multi-SIM systems.
     */
    public SmsHandler(){
        smsManager = SmsManager.getDefault();
        scAddress = null;
        listener = null;
        smsEventReceiver = new SmsEventReceiver();
        listeningForIncoming = false;
        shouldUsePendingIntentSending = false;
        shouldUsePendingIntentDelivery = false;
    }

    /**
     * The SmsEventReceiver class handling all three the main events is intentional
     * in order to reduce system resource consumption from having three distinct BroadcastReceivers
     */
    private class SmsEventReceiver extends BroadcastReceiver{
        /**
         * Default method for BroadcastReceivers. Verifies that there are incoming, sent or delivered text messages and
         * forwards them to a listener, if avaiable.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null){
                if(intent.getAction().equals(SMS_HANDLER_RECEIVED_BROADCAST)) {
                    if (listener != null){
                        for(SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)){
                            if(message.getMessageBody().contains(APP_KEY)){
                                SMSMessage m = new SMSMessage(new SMSPeer(message.getOriginatingAddress()), message.getMessageBody());
                                listener.onReceive(m);
                            }
                        }
                    }
                }
                if(intent.getAction().equals(SMS_HANDLER_SENT_BROADCAST)){
                    SMSMessage m = new SMSMessage(
                            new SMSPeer(intent.getStringExtra("address")),
                            intent.getStringExtra("message")
                    );
                    Log.d("SmsHandler","Message for: "+intent.getStringExtra("address")+"\n"+intent.getStringExtra("message"));
                    if(listener != null) listener.onSent(getResultCode(),m);
                }
                if(intent.getAction().equals(SMS_HANDLER_DELIVERED_BROADCAST)){
                    SMSMessage m = new SMSMessage(
                            new SMSPeer(intent.getStringExtra("address")),
                            intent.getStringExtra("message")
                    );
                    if(listener != null) listener.onDelivered(getResultCode(),m);
                }
            }
        }
    }

    /**
     * Interface meant to be implemented by any class wanting to listen for incoming SMS messages.
     */
    public interface OnSmsEventListener {
        /**
         * Method called when an sms is received
         * @param message the message.
         */
        void onReceive(SMSMessage message);

        /**
         * Method called when an attempt to send an sms has been made
         * @param resultCode the result for the sending operation
         * @param message the message related to said operation
         */
        void onSent(int resultCode, SMSMessage message);

        /**
         * Method called when a message might have been delivered
         * @param resultCode the result for the delivery operation
         * @param message the message related to said operation
         */
        void onDelivered(int resultCode, SMSMessage message);
    }

    /**
     * Method that sends a text message through SmsManager
     * @param context the context asking to send the message
     * @param destination the VALID destination address for the message, in phone number format
     * @param message the VALID body of the message to be sent
     * TODO sendSMS should use SMSMessage object as parameter
     */
    public void sendSMS(Context context, String destination, @NonNull String message){
        PendingIntent sentIntent;
        PendingIntent deliveryIntent;
        if(shouldUsePendingIntentSending){
            Intent intent = new Intent(SMS_HANDLER_SENT_BROADCAST)
                    .putExtra("address",destination)
                    .putExtra("message",message);
            Log.d("SmsHandler", "Pending intent for message: "+intent.getStringExtra("message"));
            sentIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        else sentIntent = null;
        if(shouldUsePendingIntentDelivery){
            Intent intent = new Intent(SMS_HANDLER_DELIVERED_BROADCAST)
                    .putExtra("address",destination)
                    .putExtra("message",message);
            deliveryIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        else deliveryIntent = null;
        smsManager.sendTextMessage(destination,scAddress,APP_KEY+message,sentIntent,deliveryIntent);
    }

    /**
     * Method to quickly register for received sms only.
     * @param context the Context that wishes to register the receiver.
     */
    public void registerReceiver(Context context){
        //TODO add call to unregister previous receiver if context equals owner
        registerReceiver(context, true, false, false);
    }

    /**
     * Method that registers an instance of SmsReceiver
     * @param context the Context which wishes to register the receiver
     *                multiple calls should not be made before unregistering
     * The receiver must listen for received sms - this is a temporary solution for proper handling of shouldHandleIncomingSms
     * @param sent whether the receiver should listen for sent sms
     * @param delivered whether the receiver should listen for delivered sms
     * @throws IllegalStateException if trying to register the receiver with no action to be received
     * TODO declare and implement flags instead of boolean fields
     */
    public void registerReceiver(Context context, boolean received, boolean sent, boolean delivered) throws IllegalStateException{
        /**The boolean values allow to enable the three actions the receiver might want to listen to.
         * This is done to reduce the number of BroadcastReceivers the process registers for every
         * instance of the class in order to reduce waste of system resources.
         */
        if(!received && !sent && !delivered) throw new IllegalStateException("Shouldn't register a receiver with no action.");
        IntentFilter filter = new IntentFilter();
        listeningForIncoming = received;
        shouldUsePendingIntentSending = sent;
        shouldUsePendingIntentDelivery = delivered;
        if(received){
            filter.addAction(SMS_HANDLER_RECEIVED_BROADCAST);
            if(listener != null) activeIncomingListeners.add(listener);
        }
        if(sent){
            filter.addAction(SMS_HANDLER_SENT_BROADCAST);
        }
        if(delivered){
            filter.addAction(SMS_HANDLER_DELIVERED_BROADCAST);
        }
        context.registerReceiver(smsEventReceiver,filter);
    }

    /**
     * Method that unregisters the instance of SmsReceiver
     * @param context which wishes to unregister the receiver
     */
    public void unregisterReceiver(Context context){
        context.unregisterReceiver(smsEventReceiver);
        listeningForIncoming = false;
        shouldUsePendingIntentSending = false;
        shouldUsePendingIntentDelivery = false;
        if(listener != null) activeIncomingListeners.remove(listener);
    }

    /**
     * Method to set the listener for this instance. Listener needs to be cleared before a new one is set.
     * @param listener non-null new listener.
     * @throws IllegalStateException if a listener is already attached to this instance of SmsHandler.
     */
    public void setListener(@NonNull OnSmsEventListener listener) throws IllegalStateException{
        if(this.listener != null) throw new IllegalStateException("A listener is already attached to this instance.");
        this.listener = listener;
        if(listeningForIncoming) activeIncomingListeners.add(listener);
    }

    /**
     * Method to clear this instance's attached listener. Albeit not necessary, a listener should
     * only try to unregister itself.
     */
    public void clearListener(){
        if(listener == null) return;
        activeIncomingListeners.remove(listener);
        listener = null;
    }

    /**
     * Method to communicate whether at least one listener is attached to an instance of this class
     * whose BroadcastReceiver is listening for incoming Sms, and is thus requiring to be notified.
     * @return true if the activeIncomingListeners list is not empty.
     */
    public static boolean shouldHandleIncomingSms(){ return !activeIncomingListeners.isEmpty();}

    /**
     * Method to clear and forward the unread messages from the database to the listener. Due to database access restrictions
     * this method cannot be thrown from the main thread. If no listener is present, this method simply clears
     * the database returning the cleared values.
     * @param context the calling context, used to instantiate the database.
     * @return an array containing the SmsEntity object containing the unread sms data.
     * @throws IllegalStateException if it's run from the main Thread.
     * TODO method should not clear the database if no listener is present
     *  should return SMSMessage
     */
    public SmsEntity[] fetchUnreadMessages(Context context){
        SmsDatabase db = Room.databaseBuilder(context, SmsDatabase.class, SMS_HANDLER_LOCAL_DATABASE)
                .enableMultiInstanceInvalidation()
                .build();
        SmsEntity[] messages = db.access().loadAllSms();
        for(SmsEntity sms : messages){
            db.access().deleteSms(sms);
            SMSMessage m = new SMSMessage(new SMSPeer(sms.address),sms.body);
            if(listener != null) listener.onReceive(m);
            Log.e("Unread Message", sms.address+" "+sms.body);
        }
        return messages;
    }
}
