package com.dezen.riccardo.smshandler;

import android.content.Context;

public class SMSManager extends CommunicationHandler<SMSMessage>{
    private static SMSManager instance;

    private Context owner;

    private SmsHandler smsHandler;
    private ReceivedMessageListener<SMSMessage> rListener;
    private SentMessageListener<SMSMessage> sListener;
    private DeliveredMessageListener<SMSMessage> dListener;
    private SmsHandler.OnSmsEventListener fullListener;

    public SMSManager getInstance(Context context){
        if(instance == null){
            instance = new SMSManager();
            owner = context;
        }
        return instance;
    }

    public void drop(Context context){
        if(context.equals(owner)){
            smsHandler.unregisterReceiver(owner);
            smsHandler.clearListener();
            owner = null;
        }
    }

    private SMSManager(){
        //prevent use of reflection to change constructor to public at runtime
        if (instance != null)
            throw new RuntimeException("This class uses the singleton building pattern. Use getInstance() to get a reference to the single instance of this class");
        smsHandler = new SmsHandler();
        fullListener = new SmsHandler.OnSmsEventListener() {
            @Override
            public void onReceive(SMSMessage message) {
                if(rListener != null) rListener.onMessageReceived(message);
            }

            @Override
            public void onSent(int resultCode, SMSMessage message) {
                if(sListener != null) sListener.onMessageSent(resultCode,message);
            }

            @Override
            public void onDelivered(int resultCode, SMSMessage message) {
                if(dListener != null) dListener.onMessageDelivered(resultCode,message);
            }
        };
    }

    @Override
    public void sendMessage(SMSMessage message) {
        smsHandler.sendSMS(owner,message.getPeer().getAddress(),message.getData());
    }

    @Override
    public void addReceiveListener(ReceivedMessageListener<SMSMessage> listener) {
        smsHandler.registerReceiver(owner);
        smsHandler.setListener(fullListener);
        rListener = listener;
    }

    @Override
    public void removeReceiveListener() {
        smsHandler.unregisterReceiver(owner);
    }

    //TODO implementation of sending and delivery confirm
}
