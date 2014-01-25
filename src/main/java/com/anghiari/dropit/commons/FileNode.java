package com.anghiari.dropit.commons;

import java.util.ArrayList;

public class FileNode {

	private int id;
    private String ip;
    private int port;
    private int port_ring;
    private KeyId key;
    
    
//    private FileNode successor;
//    private ArrayList<FileNode> successors;
//    private FileNode predeccesor;
    

    public FileNode(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public FileNode(String ip, int port, int port_ring, KeyId key){
        this.ip = ip;
        this.port = port;
        this.port_ring = port_ring;
        this.key = key;
    }
    
    public FileNode(int id,String ip, int port, int port_ring, KeyId key){
    	this.setId(id);
        this.ip = ip;
        this.port = port;
        this.port_ring = port_ring;
        this.key = key;
    }

//	public FileNode(String ip, int port, KeyId key){
//		this.ip = ip;
//		this.port = port;
//		this.key = key;
//	}

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

    public int getPort_ring() {
        return port_ring;
    }

	public FileNode getSuccessor() {
		return successor;
	}

	public void setSuccessor(FileNode successor) {
		this.successor = successor;
	}

	public ArrayList<FileNode> getSuccessors() {
		return successors;
	}

	public void setSuccessors(ArrayList<FileNode> successors) {
		this.successors = successors;
	}

	public FileNode getPredeccesor() {
		return predeccesor;
	}

	public void setPredeccesor(FileNode predeccesor) {
		this.predeccesor = predeccesor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
