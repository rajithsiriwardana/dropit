package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.operations.PingOperation;

/**
 * 
 * @author chinthaka316
 *
 */
public class FileServerRunAtInterval extends AbstractRunAtInterval{

	private FileServerNodeImpl fileServer;
	
	public FileServerRunAtInterval(int interval, FileServerNodeImpl fileServer) {
		super(interval);
		this.fileServer = fileServer;
	}

	@Override
	public void runClosure() {
		fileServer.fixFingers();
		
		if(fileServer.getSuccessor() != null){
//			fileServer.stabilize();
		}else{
			System.out.println("The successor list is not yet setup : FileServerRunAtInterval");
		}
	}

}
