package com.anghiari.dropit.commons;

public class FileNode {

    private String ip;
    private int port;
    private KeyId key;

    public FileNode(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

	public FileNode(String ip, int port, KeyId key){
		this.ip = ip;
		this.port = port;
		this.key = key;
	}

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

    public KeyId getKey() {
        return key;
    }

    public void setKey(KeyId key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object obj){
        if (FileNode.class.isInstance(obj)) {
            FileNode other = (FileNode) obj;
            return other.getKey().getHashId() == key.getHashId();
        }
        return false;
    }
}
