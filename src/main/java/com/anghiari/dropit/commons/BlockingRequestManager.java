package com.anghiari.dropit.commons;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author: sunimal
 */
public class BlockingRequestManager {

    //Send pkt to FileNode dest and wait for request. Returns dropit pkt.
    public DropItPacket sendMessageAndWaitForRequest(DropItPacket pkt, FileNode dest){

        ObjectOutputStream outStream;
        ObjectInputStream inStream;
        Socket clientSocket = null;
        Object returnObject=null;
        try {
            clientSocket = new Socket(dest.getIp(), dest.getPort_ring());
            outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outStream.writeObject(pkt);
            inStream=new ObjectInputStream(clientSocket.getInputStream());
            returnObject= inStream.readObject();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            return (DropItPacket)returnObject;
        }

    }

}
