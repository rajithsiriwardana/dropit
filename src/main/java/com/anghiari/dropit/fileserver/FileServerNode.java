package com.anghiari.dropit.fileserver;


import com.anghiari.dropit.commons.KeyId;
import org.jboss.netty.channel.SimpleChannelHandler;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;

/**
 * @author: sunimal
 */
public interface FileServerNode {
	public void bootServer(FileNode node , boolean status);

    public FileNode getNode();

//    public FileNode findSuccessor(KeyId keyId);

    public FileNode findSuccessor(DropItPacket inPacket);

	public void sendMessage(DropItPacket packet, FileNode node);

	public void stabilize();
   
    public void sendMessage(DropItPacket packet, FileNode node, SimpleChannelHandler handler);
    
	/**
	 * Check whether the successor is alive
	 */
	// public void fixFingers();

	public void checkPredecessor();

	/**
	 * Sends a message to request the request server for a position in the ring.
	 * 
	 * @param packet
	 * @return 0 if successful, different value otherwise.
	 */
	public int requestNodePosition(DropItPacket packet);
}
