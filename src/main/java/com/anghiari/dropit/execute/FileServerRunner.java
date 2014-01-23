package com.anghiari.dropit.execute;

import com.anghiari.dropit.commons.Configurations;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;
import com.anghiari.dropit.fileserver.impl.RingCommunicationHandler;
import com.anghiari.dropit.operations.PingOperation;

/**
 * @author: chinthaka
 * starts the file server
 */
public class FileServerRunner {

    public static void main(String[] args) {

        String ip = "127.0.0.1";
//        int numberOfNodes = Integer.parseInt(args[0]);
        int numberOfNodes = 5;
        int[] intPorts = Configurations.intPorts;
        int[] extPorts = Configurations.extPorts;
        int[] keys = Configurations.fileNodeKeys;

        FileNode node;
        FileServerNodeImpl fileServer;

        for(int i = 0; i < numberOfNodes-1; i++){
            node = new FileNode(ip, extPorts[i], intPorts[i], new KeyId(keys[i]));
            fileServer = new FileServerNodeImpl();
            fileServer.bootServer(node);
        }

        PingOperation op=new PingOperation(new FileServerNodeImpl(),ip, 14501);
        op.sendRequest();

        
//        PingOperation op1=new PingOperation(new FileServerNodeImpl(),ip,8001);
//        op1.sendRequest(new RingCommunicationHandler());
        
        //fileServer2.pingSuccessor(ip, 12500);
    	
    }
}
