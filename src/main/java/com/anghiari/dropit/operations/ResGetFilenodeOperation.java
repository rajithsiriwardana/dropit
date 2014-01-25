package com.anghiari.dropit.operations;

import com.anghiari.dropit.commons.Constants;
import com.anghiari.dropit.commons.DropItPacket;
import com.anghiari.dropit.commons.FileNode;

/**
 * This operation retrieves the response from the request server and sends a
 * request to join to the pointed node.
 * 
 * @author Sanka
 * 
 */
public class ResGetFilenodeOperation extends AbstractOperation {
	private FileNode node;
	private DropItPacket responsePacket;

	public ResGetFilenodeOperation(DropItPacket packet) {
		node = (FileNode) packet
				.getAttribute(Constants.INET_ADDRESS.toString());
		responsePacket = new DropItPacket(Constants.REQ_JOIN_NODE.toString());

		// send the current node data with the packet.
		// responsePacket.setAttribute(Constants.INET_ADDRESS.toString(), );
	}

	@Override
	public void sendRequest() {
		this.fileServer.sendMessage(responsePacket, node);
	}

}
