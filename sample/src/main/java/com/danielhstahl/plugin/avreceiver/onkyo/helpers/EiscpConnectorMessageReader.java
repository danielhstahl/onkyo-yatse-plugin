package com.danielhstahl.plugin.avreceiver.onkyo.helpers;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.nio.Buffer;

/**
 * Created by daniel on 5/8/17.
 */

public class EiscpConnectorMessageReader {
    private  boolean quit=false;
    private BufferedInputStream socketIn;
    private EiscpController messageHandler;
    public EiscpConnectorMessageReader(BufferedInputStream socketIn, EiscpController messageHandler){
        this.socketIn = socketIn;
        this.messageHandler=messageHandler;
    }
    public  void messageReader() {
        byte[] response = new byte[4];
        while (!quit&&!messageHandler.stopReceiving()) {
            //log.trace("readLoop");
            try {
                blockedReadQuadrupel(response);
                EiscpProtocolHelper.validateIscpSignature(response, 0);

                blockedReadQuadrupel(response);
                EiscpProtocolHelper.validateHeaderLengthSignature(response, 0);

                blockedReadQuadrupel(response);
                int messageSize = EiscpProtocolHelper.readMessageSize(response, 0);

                blockedReadQuadrupel(response);
                EiscpProtocolHelper.validateEiscpVersion(response, 0);

                // eISCP encapulation-header ends here - ISCP begins !1xxx

                byte[] iscpMessage = new byte[messageSize];
                for (int i = 0; i < messageSize; i++) {
                    iscpMessage[i] = (byte) socketIn.read();
                }

                String iscpResult = EiscpProtocolHelper.parseIscpMessage(iscpMessage);
                //return iscpResult;
                try {
                    fireReceivedIscpMessage(iscpResult);
                } catch (Throwable ex) {
                    //log.error("error in listener {}", ex.getMessage(), ex);
                }
            } catch (EiscpMessageFormatException ex) {
                //log.warn(ex.getMessage() + " - " + EiscpProtocolHelper.convertToHexString(response));
                //log.debug("skip bytes until EOF/CR");

                if (isEofMarkerfInArray(response)) {
                    //log.debug("found eof in response block");
                } else {
                    boolean eofFound = false;
                    try {
                        while (!eofFound) {
                            byte b = (byte) socketIn.read();
                            if (b == -1) {
                                //log.debug("end of stream");
                                quit();
                                eofFound = true;
                            } else {
                                // log.debug("discard " + EiscpProtocolHelper.convertToHexString(new byte[]{b}));
                                eofFound = EiscpProtocolHelper.isEofMarker(b);
                            }
                        }
                        ;
                        //log.trace("found EOF");
                    } catch (Exception ex2) {
                        //log.error("not handled", ex2);
                    }
                }
            } catch (Exception ex) {
                //log.warn(ex.getMessage());
                ex.printStackTrace();
                quit();
            }
        }
    }
    public void fireReceivedIscpMessage(String iscpResult) {
        messageHandler.receivedIscpMessage(iscpResult);
    }
    public boolean isEofMarkerfInArray(byte[] response) {
        boolean eofFound = false;
        for (int i=0; i<response.length; i++) {
            eofFound = eofFound || EiscpProtocolHelper.isEofMarker(response[i]);
        }
        return eofFound;
    }
    public void quit() {
        quit = true;
    }
    private  void blockedReadQuadrupel(byte[] bb) throws IOException {
        bb[0] = (byte) socketIn.read();
        bb[1] = (byte) socketIn.read();
        bb[2] = (byte) socketIn.read();
        bb[3] = (byte) socketIn.read();
    }

}
