package com.dezen.riccardo.smshandler;

public interface ReceivedMessageListener<M extends Message> {
    /**
     * Called when a message is received
     * @param message the received message that needs to be forwarded
     */
    void onMessageReceived(M message);
}
