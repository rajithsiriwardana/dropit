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
		fileServer.stabilize();
        fileServer.fixFingers();
	}

}
