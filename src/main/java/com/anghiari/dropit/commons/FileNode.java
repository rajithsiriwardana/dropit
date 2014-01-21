package com.anghiari.dropit.commons;

public class FileNode {


	public FileNode(String ip, int port){
		this.ip=ip;
		this.setPort(port);
		
	}
	private String ip;
	private int port;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	
}
