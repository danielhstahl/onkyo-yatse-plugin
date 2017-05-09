package com.danielhstahl.plugin.avreceiver.onkyo.helpers;

public interface EiscpController {
    public void receivedIscpMessage(String message);
    public boolean stopReceiving();
}
