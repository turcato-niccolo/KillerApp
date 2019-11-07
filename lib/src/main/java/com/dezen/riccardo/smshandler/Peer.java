package com.dezen.riccardo.smshandler;

public interface Peer<T> {
    /**
     * @return the address of this Peer
     */
    T getAddress();
}
