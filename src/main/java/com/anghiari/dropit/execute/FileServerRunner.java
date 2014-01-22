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

        FileNode node1 = new FileNode(ip, 12500, new KeyId(100));
    	FileServerNodeImpl fileServer1 = new FileServerNodeImpl();
    	fileServer1.bootServer(node1);


        FileNode node2 = new FileNode(ip, 12501, new KeyId(110));
    	FileServerNodeImpl fileServer2 = new FileServerNodeImpl();
    	fileServer2.bootServer(node2);

        PingOperation op=new PingOperation(fileServer2,ip,12500);
        op.sendRequest();

        //fileServer2.pingSuccessor(ip, 12500);
    	
    }
}
