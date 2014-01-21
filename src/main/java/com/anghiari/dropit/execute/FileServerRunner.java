package com.anghiari.dropit.execute;

import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;

/**
 * @author: chinthaka
 * starts the file server
 */
public class FileServerRunner {

    public static void main(String[] args) {

    
    	FileServerNodeImpl fileServer2= new FileServerNodeImpl();
    	fileServer2.bootServer("192.248.8.244", 12501);
    	
//    	FileServerNodeImpl fileServer= new FileServerNodeImpl();
//    	fileServer.bootServer("127.0.0.1", 12500);
//    	
//    	fileServer.pingSuccessor("127.0.1", 12501);
    	
    }
}
