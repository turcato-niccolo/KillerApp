package com.dezen.riccardo.smshandler;

public abstract class CommunicationHandler<M extends Message> {
    /**
     * method to send a Message to its associated Peer
     * @param message the valid message to send
     */
    public abstract void sendMessage(M message);

    /**
     * method that adds a listener to wait for incoming messages directed to the library
     * @param listener the listener to wake up when a message is received
     */
    public abstract void addReceiveListener(ReceivedMessageListener<M> listener);

    /**
     * method to clear the listener. Only the owning context should call this method
     * TODO add parameter to specify who's asking to remove the listener
     */
    public abstract void removeReceiveListener();
}
