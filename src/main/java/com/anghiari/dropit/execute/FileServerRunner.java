package com.anghiari.dropit.execute;

import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.commons.KeyId;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;
import com.anghiari.dropit.operations.PingOperation;

/**
 * @author: chinthaka
 * starts the file server
 */
public class FileServerRunner {

    public static void main(String[] args) {

        String ip = "127.0.0.1";
        int port = 14500;
        int key = 100;
        int numberOfNodes = Integer.parseInt(args[0]);

        FileNode node;
        FileServerNodeImpl fileServer;

        for(int i = 0; i < numberOfNodes; i++){
            node = new FileNode(ip, port++, new KeyId(key++));
            fileServer = new FileServerNodeImpl();
            fileServer.bootServer(node);
        }

        PingOperation op=new PingOperation(new FileServerNodeImpl(),ip,--port);
        op.sendRequest();

        //fileServer2.pingSuccessor(ip, 12500);
    	
    }
}
