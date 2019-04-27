package com.danielhstahl.plugin.avreceiver.onkyo.helpers;

public interface EiscpListener {
    void receivedIscpMessage(String message);

    void disconnected();
}
