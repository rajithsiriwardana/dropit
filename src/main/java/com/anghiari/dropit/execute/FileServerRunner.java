package com.anghiari.dropit.execute;

import com.anghiari.dropit.commons.Configurations;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;
import com.anghiari.dropit.fileserver.impl.RingCommunicationHandler;
import com.anghiari.dropit.operations.PingOperation;
import com.anghiari.dropit.operations.ReqServerForNodeOperation;

/**
 * @author: chinthaka
 * starts the file server
 */
public class FileServerRunner {

    public static void main(String[] args) {

        String ip = "127.0.0.1";
//        int numberOfNodes = Integer.parseInt(args[0]);
//
        int[] intPorts = Configurations.intPorts;
        int[] extPorts = Configurations.extPorts;
        int[] keys = Configurations.fileNodeKeys;
        int numberOfNodes = intPorts.length;
        FileNode node;
        FileServerNodeImpl fileServer;

        for(int i = 0; i < numberOfNodes; i++){
            node = new FileNode(ip, extPorts[i], intPorts[i], new KeyId(keys[i]));
            fileServer = new FileServerNodeImpl();
            fileServer.bootServer(node, true);
        }
//
//        PingOperation op=new PingOperation(new FileServerNodeImpl(),ip, 14501);
//        op.sendRequest();
//        
//        
//        FileServerNodeImpl newFileNode = new FileServerNodeImpl();
//
//        ReqServerForNodeOperation reqSerOp = new ReqServerForNodeOperation(newFileNode);
//
//    	reqSerOp.sendRequest();
    }
}
