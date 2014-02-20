package com.anghiari.dropit.fileserver.impl;

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
		if(fileServer.getSuccessor() != null){
			fileServer.stabilize();
            fileServer.checkPredecessor();
            fileServer.fixFingers();
		}else{
			System.out.println("The successor list is not yet setup : FileServerRunAtInterval");
		}
	}

}
