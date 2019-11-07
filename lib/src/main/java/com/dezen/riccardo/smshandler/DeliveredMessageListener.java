package com.dezen.riccardo.smshandler;

public interface DeliveredMessageListener<M extends Message>{
    /**
     * Method called when a sent message might have been delivered to the associated peer
     * @param resultCode result code of the deliver operation (success or failure)
     * @param message the message the operation tried to deliver
     */
    void onMessageDelivered(int resultCode, M message);
}
