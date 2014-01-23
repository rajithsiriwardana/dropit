package com.anghiari.dropit.fileserver.impl;

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
