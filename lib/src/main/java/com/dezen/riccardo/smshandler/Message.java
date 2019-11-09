package com.dezen.riccardo.smshandler;

public interface Message<D, P extends Peer>{
    /**
     * @return the data in this message
     */
    D getData();

    /**
     * @return the peer for this message
     */
    P getPeer();
}
