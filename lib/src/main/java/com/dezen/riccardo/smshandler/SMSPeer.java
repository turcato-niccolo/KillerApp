package com.dezen.riccardo.smshandler;

import androidx.annotation.NonNull;

public class SMSPeer implements Peer<String> {
    private String address;

    /**
     * @param address the address for the peer
     */
    public SMSPeer(String address){
        this.address = address;
    }

    /**
     * @return the address of the peer
     */
    @Override
    public String getAddress() {
        return address;
    }

    /**
     * @return true if address is empty string
     */
    public boolean isEmpty(){
        return address.isEmpty();
    }

    /**
     * @return true if this peer is valid
     */
    public boolean isValid(){
        return !isEmpty() && isAddressValid();
    }

    /**
     * @return true if address fulfills international phone address standards
     */
    private boolean isAddressValid(){
        //TODO add proper number validation through PhoneNumberUtils
        return true;
    }

    /**
     * @return String type representation of the Object
     */
    @NonNull
    public String toString(){
        return address;
    }
}
