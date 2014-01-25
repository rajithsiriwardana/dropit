package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;
import com.anghiari.dropit.fileserver.impl.FileServerNodeImpl;
import com.anghiari.dropit.fileserver.impl.RingSetupHandler;

/**
 * This operation handles a request sent by a new node which is in need of
 * connecting to the ring.
 * 
 * @author Sanka
 * 
 */
public class ReqJoinNodeOperation extends AbstractOperation {
	private DropItPacket responsePacket;
	private FileNode newNode;
	private RingSetupHandler setupHandler;
	private FileServerNodeImpl server;

	public ReqJoinNodeOperation(DropItPacket pkt) {

		responsePacket = new DropItPacket(Constants.RES_JOIN_NODE.toString());
		newNode = (FileNode) pkt
				.getAttribute(Constants.INET_ADDRESS.toString());
		setupNodeInRing();
	}

	private void setupNodeInRing() {
		server = (FileServerNodeImpl) this.fileServer;
		FileNode currentNode = server.getNode();
		FileNode successor = server.getSuccessor();

		responsePacket.setAttribute(Constants.SUCCESSOR.toString(), successor);
		responsePacket.setAttribute(Constants.PREDECESSOR.toString(),
				currentNode);

	}

	@Override
	public void sendResponse() {
		this.fileServer.sendMessage(responsePacket, newNode, setupHandler);
	}
}
