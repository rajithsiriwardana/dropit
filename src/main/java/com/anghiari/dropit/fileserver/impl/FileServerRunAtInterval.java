package com.anghiari.dropit.fileserver.impl;

import com.anghiari.dropit.operations.PingOperation;

public class FileServerRunAtInterval extends AbstractRunAtInterval{

	private FileServerNodeImpl fileServer;
	
	public FileServerRunAtInterval(int interval, FileServerNodeImpl fileServer) {
		super(interval);
		this.fileServer = fileServer;
	}

	@Override
	public void runClosure() {
		this.fileServer.stabilize();
	}

}
