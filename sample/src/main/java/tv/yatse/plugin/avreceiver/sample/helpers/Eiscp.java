package tv.yatse.plugin.avreceiver.sample.helpers;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 *  A class that wraps the communication to Onkyo/Integra devices using the
 *  ethernet Integra Serial Control Protocal (eISCP).
 *
 *
 */
public class Eiscp
{
    /**  A holder for this clients System File Separator.  */
    public final static String SYSTEM_FILE_SEPERATOR = File.separator;

    /**  A holder for this clients System line termination separator.  */
    public final static String SYSTEM_LINE_SEPERATOR =
            System.getProperty("line.separator");

    /**  The VM classpath (used in some methods)..  */
    public static String CLASSPATH = System.getProperty("class.path");

    /**  The users home ditrectory.  */
    public static String USERHOME = System.getProperty("user.home");

    /**  The users pwd ditrectory.  */
    public static String USERDIR = System.getProperty("user.dir");

    /** default receiver IP Address. **/
    private static final String DEFAULT_EISCP_IP = "192.168.0.109";

    /** default eISCP port. **/
    private static final int DEFAULT_EISCP_PORT = 60128;
    public static Socket eiscpSocket_ = null;
    private static ObjectOutputStream out_ = null;
    protected static DataInputStream in_ = null;
    protected static boolean connected_ = false;
    public static String ipAdress;
    public static int portNumber;


    /**
     * Connects to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
     **/
    public static boolean connectSocket() { return connectSocket(DEFAULT_EISCP_IP,DEFAULT_EISCP_PORT);}


    /**
     * Connects to the receiver by opening a socket connection through the DEFAULT eISCP port.
     **/
    public boolean connectSocket(String ip) { return connectSocket(ip,DEFAULT_EISCP_PORT);}


    /**
     * Connects to the receiver by opening a socket connection through the eISCP port.
     **/
    public static boolean connectSocket(String ip, int eiscpPort)
    {
//    if (ip==null || ip.equals("")||ip.equals("null")) ip=DEFAULT_EISCP_IP;
//    if (eiscpPort==0) eiscpPort=DEFAULT_EISCP_PORT;
        try
        {
            //1. creating a socket to connect to the server
            eiscpSocket_ = new Socket(ip, eiscpPort);
            System.out.println("Connected to "+ip+" on port "+eiscpPort);
            //2. get Input and Output streams
            out_ = new ObjectOutputStream(eiscpSocket_.getOutputStream());
            System.out.println("out_Init");
            out_.flush();
            // System.out.println("inInit");
            connected_ = true;
        }
        catch(UnknownHostException unknownHost)
        {
            System.err.println("You are trying to connect to an unknown host!");
            connected_ = false;
        }
        catch(ConnectException connectException){
            //JOptionPane.showMessageDialog(null, "Connection was refused, wait before trying again!");
        }
        catch(IOException ioException)
        {
            connected_=false;
            ioException.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
            connected_ = false;
        }

        return connected_;
    }


    /**
     * Closes the socket connection.
     * @return true if the closed successfully
     **/
    public static boolean closeSocket()
    {
        //4: Closing connection
        try
        {
            if (in_!=null) in_.close();
            if (out_!=null) out_.close();
            if (eiscpSocket_!=null) eiscpSocket_.close();
            System.out.println("closed connections");
            connected_ = false;
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
        return connected_;
    }


    /** Converts an ASCII String to a hex  String **/
    public static String convertStringToHex(String str)
    {
        return convertStringToHex( str, false);
    }


    /** Converts an ASCII String to a hex  String **/
    public static String convertStringToHex(String str,  boolean dumpOut)
    {
        char[] chars = str.toCharArray();
        String out_put = "";

        if (dumpOut) System.out.println("Ascii: "+str);
        if (dumpOut) System.out.print("Hex: ");
        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++)
        {
            out_put = Integer.toHexString(chars[i]);
            if (out_put.length()==1) hex.append("0");
            hex.append(out_put);
            if (dumpOut) System.out.print("0x"+(out_put.length()==1?"0":"")+ out_put+" ");
        }
        if (dumpOut) System.out.println("");

        return hex.toString();
    }


    /** Converts a hex String to an ASCII String **/
    public static String convertHexToString(String hex)
    {
        return convertHexToString( hex, false);
    }


    /** Converts a hex String to an ASCII String **/
    public static String convertHexToString(String hex,  boolean dumpOut)
    {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        String out_put = "";

        if (dumpOut) System.out.print("Hex: ");
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            out_put = hex.substring(i, (i + 2));
            if (dumpOut) System.out.print("0x"+out_put+" ");
            if (out_put.equals("00")) { sb.append("00");}
            //convert hex to decimal
            else {
                try
                {
                    int decimal = Integer.parseInt(out_put, 16);
                    //convert the decimal to character

                    sb.append((char)decimal);
                    temp.append(decimal);
                }
                catch (NumberFormatException e)
                {
                    System.out.println("heres the fail");
                }

            }
        }
        if (dumpOut) System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }


    /**
     * Wraps a command in a eiscp data message (data characters).
     *
     * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
     **/
    public static StringBuilder getEiscpMessage(String command)
    {
        StringBuilder sb = new StringBuilder();
        int eiscpDataSize = command.length() + 2 ; // this is the eISCP data size
        int eiscpMsgSize = eiscpDataSize + 1 + 16 ; // this is the eISCP data size

    /* This is where I construct the entire message
        character by character. Each char is represented by a 2 disgit hex value */
        sb.append("ISCP");
        // the following are all in HEX representing one char

        // 4 char Big Endian Header
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("10", 16));

        // 4 char  Big Endian data size
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        // the official ISCP docs say this is supposed to be just the data size  (eiscpDataSize)
        // ** BUT **
        // It only works if you send the size of the entire Message size (eiscpMsgSize)
        sb.append((char)Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16));

        // eiscp_version = "01";
        sb.append((char)Integer.parseInt("01", 16));

        // 3 chars reserved = "00"+"00"+"00";
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));

        //  eISCP data
        // Start Character
        sb.append("!");

        // eISCP data - unittype char '1' is receiver
        sb.append("1");

        // eISCP data - 3 char command and param    ie PWR01
        sb.append( command);

        // msg end - EOF   0D was standard
        sb.append((char)Integer.parseInt("0D", 16));

        System.out.println("eISCP data size: "+eiscpDataSize +"(0x"+Integer.toHexString(eiscpDataSize) +") chars");
        System.out.println("eISCP msg size: "+sb.length() +"(0x"+Integer.toHexString(sb.length()) +") chars");
        //System.out.println("eispc message: " + sb.toString());

        return sb;
    }
    public static void sendEnd(){
        StringBuilder sb= new StringBuilder();
        sb.append((char)Integer.parseInt("0D", 16));
        try {
            out_.writeBytes(sb.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Sends to command to the receiver and does not wait for a reply, leaves the socket open.
     *
     * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
     **/
    public static void sendCommand(String command)
    {
        if (connected_){
            sendCommand(command, false);
            System.out.println("Sent command: "+ command);
            wait(100);
        }
        else {
            System.out.println("not connected to send message!");
        }
    }
    protected static void wait(int millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    /**
     * Sends to command to the receiver and does not wait for a reply
     *
     * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
     * @param closeSocket flag to close the connection when done or leave it open.
     **/
    public static void sendCommand(String command, boolean closeSocket)
    {
        StringBuilder sb = getEiscpMessage(command);
        if(!connected_){
            connectSocket();
        }

        if(connected_)
        {
            try
            {
                System.out.println("sending "+sb.length() +" chars: ");
                convertStringToHex(sb.toString(), false);

                out_.writeBytes(sb.toString());
                out_.flush();
                System.out.println("sent!" );
//        try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			System.out.println("fuck");
//			e.printStackTrace();
//		}
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        }

        if (closeSocket) closeSocket();
    }







}
